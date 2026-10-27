"""Функциональные проверки без пользовательских классов."""

from main import assess_batch_conflicts, form_batch_protocol, validate_measurements


def test_valid_measurement():
    batch = {"id": "B-1"}
    specs = [{"id": "S-1", "parameter_id": "p", "minimum": 1, "maximum": 3}]
    measurements = [{"id": "M-1", "parameter_id": "p", "value": 2}]
    assert validate_measurements(batch, measurements, specs) == []
    assert form_batch_protocol(batch, measurements, [])["status"] == "соответствует"


def test_out_of_range_and_missing_specification():
    batch = {"id": "B-1"}
    specs = [{"id": "S-1", "parameter_id": "p", "minimum": 1, "maximum": 3}]
    measurements = [
        {"id": "M-1", "parameter_id": "p", "value": 4},
        {"id": "M-2", "parameter_id": "unknown", "value": 1},
    ]
    deviations = validate_measurements(batch, measurements, specs)
    conflicts = assess_batch_conflicts(batch, measurements, specs)
    assert deviations[0]["reason"] == "значение вне допуска"
    assert deviations[1]["reason"] == "норматив не задан"
    assert conflicts[0]["distance"] == 1
    assert conflicts[1]["specification_id"] is None
    assert form_batch_protocol(batch, measurements, deviations)["status"] == "требует решения технолога"


def run_tests():
    test_valid_measurement()
    test_out_of_range_and_missing_specification()
    print("Все проверки пройдены")


if __name__ == "__main__":
    run_tests()
