import unittest

from main import assess_batch_conflicts, form_batch_protocol, validate_measurements


class ProcessTests(unittest.TestCase):
    def setUp(self):
        self.batch = {"id": "B-1"}
        self.specs = [{"id": "S-1", "parameter_id": "p", "minimum": 1, "maximum": 3}]

    def test_valid_measurement(self):
        measurements = [{"id": "M-1", "parameter_id": "p", "value": 2}]
        self.assertEqual([], validate_measurements(self.batch, measurements, self.specs))
        self.assertEqual("соответствует", form_batch_protocol(self.batch, measurements, [])["status"])

    def test_out_of_range_and_missing_specification(self):
        measurements = [{"id": "M-1", "parameter_id": "p", "value": 4},
                        {"id": "M-2", "parameter_id": "unknown", "value": 1}]
        self.assertEqual(2, len(validate_measurements(self.batch, measurements, self.specs)))
        self.assertEqual(2, len(assess_batch_conflicts(self.batch, measurements, self.specs)))


if __name__ == "__main__":
    unittest.main()
