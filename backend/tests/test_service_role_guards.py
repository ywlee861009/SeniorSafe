import asyncio
from types import SimpleNamespace

import pytest
from fastapi import HTTPException

from app.services.fall_service import cancel_fall_event, get_fall_history, report_fall_event
from app.services.pairing_service import connect_senior, create_pairing_code, list_pairings


def run(coro):
    return asyncio.run(coro)


def user(user_type: str):
    return SimpleNamespace(user_type=user_type)


def assert_http_error(coro, status_code: int, detail: str):
    with pytest.raises(HTTPException) as exc:
        run(coro)
    assert exc.value.status_code == status_code
    assert exc.value.detail == detail


def test_only_seniors_can_create_pairing_codes():
    assert_http_error(
        create_pairing_code(db=object(), user=user("guardian")),
        status_code=403,
        detail="Only senior users can create pairing codes",
    )


def test_only_guardians_can_connect_seniors():
    assert_http_error(
        connect_senior(db=object(), guardian=user("senior"), code="A3F9K2"),
        status_code=403,
        detail="Only guardian users can connect seniors",
    )


def test_only_guardians_can_list_pairings():
    assert_http_error(
        list_pairings(db=object(), guardian=user("senior")),
        status_code=403,
        detail="Only guardian users can list pairings",
    )


def test_only_seniors_can_report_fall_events():
    assert_http_error(
        report_fall_event(db=object(), senior=user("guardian"), detected_at=None),
        status_code=403,
        detail="Only senior users can report fall events",
    )


def test_only_seniors_can_cancel_fall_events():
    assert_http_error(
        cancel_fall_event(db=object(), senior=user("guardian"), event_id="event-1"),
        status_code=403,
        detail="Only senior users can cancel fall events",
    )


def test_only_guardians_can_view_fall_history():
    assert_http_error(
        get_fall_history(db=object(), guardian=user("senior"), senior_id="senior-1"),
        status_code=403,
        detail="Only guardian users can view fall history",
    )

