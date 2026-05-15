"""initial schema

Revision ID: 202605150001
Revises:
Create Date: 2026-05-15
"""

from typing import Sequence, Union

from alembic import op
import sqlalchemy as sa
from sqlalchemy.dialects import postgresql

revision: str = "202605150001"
down_revision: Union[str, None] = None
branch_labels: Union[str, Sequence[str], None] = None
depends_on: Union[str, Sequence[str], None] = None


def upgrade() -> None:
    op.create_table(
        "users",
        sa.Column("id", postgresql.UUID(as_uuid=True), primary_key=True),
        sa.Column("email", sa.String(length=255), nullable=False),
        sa.Column("password_hash", sa.String(length=255), nullable=False),
        sa.Column("name", sa.String(length=100), nullable=False),
        sa.Column("phone", sa.String(length=50), nullable=False),
        sa.Column("user_type", sa.String(length=20), nullable=False),
        sa.Column("fcm_token", sa.Text(), nullable=True),
        sa.Column("is_active", sa.Boolean(), nullable=False, server_default=sa.text("true")),
        sa.Column("created_at", sa.DateTime(timezone=True), nullable=False, server_default=sa.text("now()")),
        sa.CheckConstraint("user_type in ('senior', 'guardian')", name="ck_users_user_type"),
        sa.UniqueConstraint("email", name="uq_users_email"),
    )
    op.create_index("ix_users_email", "users", ["email"])

    op.create_table(
        "pairings",
        sa.Column("id", postgresql.UUID(as_uuid=True), primary_key=True),
        sa.Column("senior_id", postgresql.UUID(as_uuid=True), nullable=False),
        sa.Column("guardian_id", postgresql.UUID(as_uuid=True), nullable=False),
        sa.Column("status", sa.String(length=20), nullable=False, server_default="active"),
        sa.Column("created_at", sa.DateTime(timezone=True), nullable=False, server_default=sa.text("now()")),
        sa.CheckConstraint("status in ('pending', 'active', 'disconnected')", name="ck_pairings_status"),
        sa.ForeignKeyConstraint(["guardian_id"], ["users.id"], ondelete="CASCADE"),
        sa.ForeignKeyConstraint(["senior_id"], ["users.id"], ondelete="CASCADE"),
        sa.UniqueConstraint("senior_id", "guardian_id", name="uq_pairings_senior_guardian"),
    )

    op.create_table(
        "fall_events",
        sa.Column("id", postgresql.UUID(as_uuid=True), primary_key=True),
        sa.Column("senior_id", postgresql.UUID(as_uuid=True), nullable=False),
        sa.Column("detected_at", sa.DateTime(timezone=True), nullable=False),
        sa.Column("cancelled", sa.Boolean(), nullable=False, server_default=sa.text("false")),
        sa.Column("acknowledged_at", sa.DateTime(timezone=True), nullable=True),
        sa.Column("created_at", sa.DateTime(timezone=True), nullable=False, server_default=sa.text("now()")),
        sa.ForeignKeyConstraint(["senior_id"], ["users.id"], ondelete="CASCADE"),
    )
    op.create_index("ix_fall_events_senior_detected", "fall_events", ["senior_id", "detected_at"])

    op.create_table(
        "pairing_codes",
        sa.Column("code", sa.String(length=6), primary_key=True),
        sa.Column("senior_id", postgresql.UUID(as_uuid=True), nullable=False),
        sa.Column("expires_at", sa.DateTime(timezone=True), nullable=False),
        sa.Column("created_at", sa.DateTime(timezone=True), nullable=False, server_default=sa.text("now()")),
        sa.ForeignKeyConstraint(["senior_id"], ["users.id"], ondelete="CASCADE"),
    )
    op.create_index("ix_pairing_codes_senior_id", "pairing_codes", ["senior_id"])


def downgrade() -> None:
    op.drop_index("ix_pairing_codes_senior_id", table_name="pairing_codes")
    op.drop_table("pairing_codes")
    op.drop_index("ix_fall_events_senior_detected", table_name="fall_events")
    op.drop_table("fall_events")
    op.drop_table("pairings")
    op.drop_index("ix_users_email", table_name="users")
    op.drop_table("users")
