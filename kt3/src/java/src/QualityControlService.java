import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public final class QualityControlService {
    private final List<ValidationRule> rules;

    public QualityControlService() {
        this.rules = Arrays.asList(new SpecificationPresenceRule(), new RangeValidationRule());
    }

    public List<Deviation> validateMeasurements(
            Batch batch, List<Measurement> measurements, List<Specification> specifications) {
        Specification specification = assignedSpecification(batch, specifications);
        Map<String, SpecificationLimit> limits = new HashMap<>();
        if (specification != null) {
            for (SpecificationLimit limit : specification.getLimits()) {
                limits.put(limit.getParameterId(), limit);
            }
        }
        assertUniqueMeasurementIds(measurements);
        List<Deviation> deviations = new ArrayList<>();
        for (Measurement measurement : measurements) {
            SpecificationLimit limit = limits.get(measurement.getParameterId());
            for (ValidationRule rule : rules) {
                rule.validate(batch, measurement, limit).ifPresent(deviations::add);
            }
        }
        return deviations;
    }

    public List<SpecificationConflict> assessBatchConflicts(
            Batch batch, List<Measurement> measurements, List<Specification> specifications) {
        Specification specification = assignedSpecification(batch, specifications);
        List<SpecificationLimit> limits = specification == null
                ? new ArrayList<>() : specification.getLimits();
        List<SpecificationConflict> conflicts = new ArrayList<>();
        for (Measurement measurement : measurements) {
            boolean matched = false;
            for (SpecificationLimit limit : limits) {
                if (!measurement.getParameterId().equals(limit.getParameterId())) {
                    continue;
                }
                matched = true;
                double value = measurement.getValue();
                if (value < limit.getMinimum() || value > limit.getMaximum()) {
                    double distance = Math.min(
                            Math.abs(value - limit.getMinimum()),
                            Math.abs(value - limit.getMaximum()));
                    conflicts.add(new SpecificationConflict(batch.getId(), measurement.getId(),
                            specification.getId(), limit.getId(), distance));
                }
            }
            if (!matched) {
                conflicts.add(new SpecificationConflict(batch.getId(), measurement.getId(),
                        specification == null ? null : specification.getId(), null, null));
            }
        }
        return conflicts;
    }

    public BatchProtocol formBatchProtocol(Batch batch, List<Measurement> measurements,
                                            List<Deviation> deviations,
                                            List<SpecificationConflict> conflicts) {
        return new BatchProtocol(batch, measurements, deviations, conflicts);
    }

    public Map<String, Object> process(InputData data) {
        List<Deviation> deviations = validateMeasurements(
                data.getBatch(), data.getMeasurements(), data.getSpecifications());
        List<SpecificationConflict> conflicts = assessBatchConflicts(
                data.getBatch(), data.getMeasurements(), data.getSpecifications());
        BatchProtocol protocol = formBatchProtocol(
                data.getBatch(), data.getMeasurements(), deviations, conflicts);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("deviations", maps(deviations, Deviation::toMap));
        result.put("conflicts", maps(conflicts, SpecificationConflict::toMap));
        result.put("protocol", protocol.toMap());
        Map<String, Object> reference = new LinkedHashMap<>();
        reference.put("raw_material_lots", maps(data.getRawMaterialLots(), RawMaterialLot::toMap));
        reference.put("parameters", maps(data.getParameters(), Parameter::toMap));
        reference.put("operators", maps(data.getOperators(), Employee::toMap));
        result.put("reference_data", reference);
        return result;
    }

    private Specification assignedSpecification(Batch batch, List<Specification> specifications) {
        for (Specification specification : specifications) {
            if (specification.getId().equals(batch.getSpecificationId())) {
                return specification;
            }
        }
        return null;
    }

    private void assertUniqueMeasurementIds(List<Measurement> measurements) {
        Set<String> ids = new HashSet<>();
        for (Measurement measurement : measurements) {
            if (!ids.add(measurement.getId())) {
                throw new IllegalArgumentException("Повторный id измерения: " + measurement.getId());
            }
        }
    }

    public static <T> List<Map<String, Object>> maps(
            List<T> values, Function<T, Map<String, Object>> converter) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (T value : values) {
            result.add(converter.apply(value));
        }
        return result;
    }
}
