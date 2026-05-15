"""add device pairing schema

Revision ID: 202605160001
Revises: 202605150001
Create Date: 2026-05-16
"""

from typing import Sequence, Union

from alembic import op
import sqlalchemy as sa
from sqlalchemy.dialects import postgresql

revision: str = "202605160001"
down_revision: Union[str, None] = "202605150001"
branch_labels: Union[str, Sequence[str], None] = None
depends_on: Union[str, Sequence[str], None] = None


def upgrade() -> None:
    op.create_table(
        "devices",
        sa.Column("id", postgresql.UUID(as_uuid=True), primary_key=True),
        sa.Column("install_id_hash", sa.String(length=64), nullable=False),
        sa.Column("role", sa.String(length=20), nullable=False),
        sa.Column("display_name", sa.String(length=100), nullable=False),
        sa.Column("fcm_token", sa.Text(), nullable=True),
        sa.Column("token_hash", sa.String(length=64), nullable=True),
        sa.Column("created_at", sa.DateTime(timezone=True), nullable=False, server_default=sa.text("now()")),
        sa.Column("last_seen_at", sa.DateTime(timezone=True), nullable=False, server_default=sa.text("now()")),
        sa.CheckConstraint("role in ('senior', 'guardian')", name="ck_devices_role"),
        sa.UniqueConstraint("install_id_hash", name="uq_devices_install_id_hash"),
    )
    op.create_index("ix_devices_install_id_hash", "devices", ["install_id_hash"])

    op.add_column("pairing_codes", sa.Column("senior_device_id", postgresql.UUID(as_uuid=True), nullable=True))
    op.add_column("pairing_codes", sa.Column("consumed_at", sa.DateTime(timezone=True), nullable=True))
    op.create_index("ix_pairing_codes_senior_device_id", "pairing_codes", ["senior_device_id"])
    op.create_foreign_key(
        "fk_pairing_codes_senior_device_id_devices",
        "pairing_codes",
        "devices",
        ["senior_device_id"],
        ["id"],
        ondelete="CASCADE",
    )

    op.add_column("pairings", sa.Column("senior_device_id", postgresql.UUID(as_uuid=True), nullable=True))
    op.add_column("pairings", sa.Column("guardian_device_id", postgresql.UUID(as_uuid=True), nullable=True))
    op.add_column("pairings", sa.Column("active", sa.Boolean(), nullable=False, server_default=sa.text("true")))
    op.add_column("pairings", sa.Column("disconnected_at", sa.DateTime(timezone=True), nullable=True))
    op.create_index("ix_pairings_senior_device_id", "pairings", ["senior_device_id"])
    op.create_index("ix_pairings_guardian_device_id", "pairings", ["guardian_device_id"])
    op.create_foreign_key(
        "fk_pairings_senior_device_id_devices",
        "pairings",
        "devices",
        ["senior_device_id"],
        ["id"],
        ondelete="CASCADE",
    )
    op.create_foreign_key(
        "fk_pairings_guardian_device_id_devices",
        "pairings",
        "devices",
        ["guardian_device_id"],
        ["id"],
        ondelete="CASCADE",
    )
    op.create_unique_constraint(
        "uq_pairings_senior_guardian_device",
        "pairings",
        ["senior_device_id", "guardian_device_id"],
    )


def downgrade() -> None:
    op.drop_constraint("uq_pairings_senior_guardian_device", "pairings", type_="unique")
    op.drop_constraint("fk_pairings_guardian_device_id_devices", "pairings", type_="foreignkey")
    op.drop_constraint("fk_pairings_senior_device_id_devices", "pairings", type_="foreignkey")
    op.drop_index("ix_pairings_guardian_device_id", table_name="pairings")
    op.drop_index("ix_pairings_senior_device_id", table_name="pairings")
    op.drop_column("pairings", "disconnected_at")
    op.drop_column("pairings", "active")
    op.drop_column("pairings", "guardian_device_id")
    op.drop_column("pairings", "senior_device_id")

    op.drop_constraint("fk_pairing_codes_senior_device_id_devices", "pairing_codes", type_="foreignkey")
    op.drop_index("ix_pairing_codes_senior_device_id", table_name="pairing_codes")
    op.drop_column("pairing_codes", "consumed_at")
    op.drop_column("pairing_codes", "senior_device_id")

    op.drop_index("ix_devices_install_id_hash", table_name="devices")
    op.drop_table("devices")
