"""Процедурный прототип контроля партий картофельных хлопьев."""

from __future__ import annotations

import json
import sys


def _assigned_specification(batch, specifications):
    """Вернуть спецификацию, явно назначенную партии."""
    specification_id = batch.get("specification_id")
    for specification in specifications:
        if specification["id"] == specification_id:
            return specification
    return None


def validate_measurements(batch, measurements, specifications):
    """Проверить измерения по назначенной партии спецификации, O(n+m)."""
    specification = _assigned_specification(batch, specifications)
    limits = {
        item["parameter_id"]: item
        for item in (specification.get("limits", []) if specification else [])
    }
    deviations = []
    for measurement in measurements:
        limit = limits.get(measurement["parameter_id"])
        if limit is None:
            deviations.append(
                {
                    "batch_id": batch["id"],
                    "measurement_id": measurement["id"],
                    "reason": "норматив не задан",
                    "severity": "critical",
                }
            )
        elif not limit["minimum"] <= measurement["value"] <= limit["maximum"]:
            deviations.append(
                {
                    "batch_id": batch["id"],
                    "measurement_id": measurement["id"],
                    "reason": "значение вне допуска",
                    "severity": "warning",
                }
            )
    return deviations


def assess_batch_conflicts(batch, measurements, specifications):
    """Сопоставить измерения со всеми нормативами назначенной спецификации, O(n*m)."""
    specification = _assigned_specification(batch, specifications)
    limits = specification.get("limits", []) if specification else []
    conflicts = []
    for measurement in measurements:
        matched = False
        for limit in limits:
            if measurement["parameter_id"] != limit["parameter_id"]:
                continue
            matched = True
            value = measurement["value"]
            if value < limit["minimum"] or value > limit["maximum"]:
                conflicts.append(
                    {
                        "batch_id": batch["id"],
                        "measurement_id": measurement["id"],
                        "specification_id": specification["id"],
                        "limit_id": limit["id"],
                        "distance": min(
                            abs(value - limit["minimum"]),
                            abs(value - limit["maximum"]),
                        ),
                    }
                )
        if not matched:
            conflicts.append(
                {
                    "batch_id": batch["id"],
                    "measurement_id": measurement["id"],
                    "specification_id": specification["id"] if specification else None,
                    "limit_id": None,
                    "distance": None,
                }
            )
    return conflicts


def form_batch_protocol(batch, measurements, deviations, conflicts):
    """Сформировать электронный протокол партии с отклонениями и конфликтами."""
    return {
        "batch": batch,
        "measurements": measurements,
        "deviations": deviations,
        "conflicts": conflicts,
        "status": "требует решения технолога" if deviations else "соответствует",
    }


def process(data):
    batch = data["batch"]
    measurements = data["measurements"]
    specifications = data["specifications"]
    deviations = validate_measurements(batch, measurements, specifications)
    conflicts = assess_batch_conflicts(batch, measurements, specifications)
    protocol = form_batch_protocol(batch, measurements, deviations, conflicts)
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
