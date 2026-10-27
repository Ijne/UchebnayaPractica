"""Процедурный прототип контроля партий картофельных хлопьев."""

from __future__ import annotations

import json
import sys


def validate_measurements(batch, measurements, specifications):
    """Проверить каждое измерение и вернуть найденные отклонения, O(n)."""
    limits = {item["parameter_id"]: item for item in specifications}
    deviations = []
    for measurement in measurements:
        limit = limits.get(measurement["parameter_id"])
        if limit is None:
            deviations.append({"batch_id": batch["id"], "measurement_id": measurement["id"],
                               "reason": "норматив не задан", "severity": "critical"})
        elif not limit["minimum"] <= measurement["value"] <= limit["maximum"]:
            deviations.append({"batch_id": batch["id"], "measurement_id": measurement["id"],
                               "reason": "значение вне допуска", "severity": "warning"})
    return deviations


def assess_batch_conflicts(batch, measurements, specifications):
    """Сопоставить параметры со всеми применимыми нормативами, O(n*m)."""
    conflicts = []
    for measurement in measurements:
        matched = False
        for specification in specifications:
            if measurement["parameter_id"] != specification["parameter_id"]:
                continue
            matched = True
            value = measurement["value"]
            if value < specification["minimum"] or value > specification["maximum"]:
                conflicts.append({"batch_id": batch["id"], "measurement_id": measurement["id"],
                                  "specification_id": specification["id"],
                                  "distance": min(abs(value - specification["minimum"]),
                                                  abs(value - specification["maximum"]))})
        if not matched:
            conflicts.append({"batch_id": batch["id"], "measurement_id": measurement["id"],
                              "specification_id": None, "distance": None})
    return conflicts


def form_batch_protocol(batch, measurements, deviations):
    """Сформировать электронный протокол партии."""
    return {"batch": batch, "measurements": measurements, "deviations": deviations,
            "status": "требует решения технолога" if deviations else "соответствует"}


def process(data):
    batch = data["batch"]
    deviations = validate_measurements(batch, data["measurements"], data["specifications"])
    conflicts = assess_batch_conflicts(batch, data["measurements"], data["specifications"])
    protocol = form_batch_protocol(batch, data["measurements"], deviations)
    return {
        "deviations": deviations,
        "conflicts": conflicts,
        "protocol": protocol,
        "reference_data": {
            "raw_material_lots": data.get("raw_material_lots", []),
            "parameters": data.get("parameters", []),
            "operators": data.get("operators", []),
        },
    }


def main(argv):
    if len(argv) != 2:
        print("Использование: python main.py example_input.json", file=sys.stderr)
        return 2
    with open(argv[1], encoding="utf-8") as source:
        result = process(json.load(source))
    print(json.dumps(result, ensure_ascii=False, indent=2))
    return 0


if __name__ == "__main__":
    raise SystemExit(main(sys.argv))
