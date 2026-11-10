"""Проверки процедурной реализации без пользовательских классов."""

from main import assess_batch_conflicts, form_batch_protocol, validate_measurements


def sample():
    batch = {"id": "B-1", "specification_id": "SP-1"}
    specifications = [
        {
            "id": "SP-0",
            "version": "old",
            "limits": [{"id": "L-0", "parameter_id": "p", "minimum": 0, "maximum": 1}],
        },
        {
            "id": "SP-1",
            "version": "current",
            "limits": [{"id": "L-1", "parameter_id": "p", "minimum": 1, "maximum": 3}],
        },
    ]
    return batch, specifications


def test_assigned_specification_is_used():
    batch, specifications = sample()
    measurements = [
        {
            "id": "M-1",
            "batch_id": "B-1",
            "parameter_id": "p",
            "value": 2,
            "measured_at": "2026-11-10T10:00:00+03:00",
        }
    ]
    assert validate_measurements(batch, measurements, specifications) == []


def test_protocol_contains_conflicts():
    batch, specifications = sample()
    measurements = [
        {
            "id": "M-1",
            "batch_id": "B-1",
            "parameter_id": "p",
            "value": 4,
            "measured_at": "2026-11-10T10:00:00+03:00",
        },
        {
            "id": "M-2",
            "batch_id": "B-1",
            "parameter_id": "unknown",
            "value": 1,
            "measured_at": "2026-11-10T10:05:00+03:00",
        },
    ]
    deviations = validate_measurements(batch, measurements, specifications)
    conflicts = assess_batch_conflicts(batch, measurements, specifications)
    protocol = form_batch_protocol(batch, measurements, deviations, conflicts)
    assert deviations[0]["reason"] == "значение вне допуска"
    assert deviations[1]["reason"] == "норматив не задан"
    assert conflicts[0]["distance"] == 1
    assert conflicts[1]["limit_id"] is None
    assert protocol["conflicts"] == conflicts
    assert protocol["status"] == "требует решения технолога"


def run_tests():
    test_assigned_specification_is_used()
    test_protocol_contains_conflicts()
    print("Все проверки Python пройдены")


if __name__ == "__main__":
    run_tests()
