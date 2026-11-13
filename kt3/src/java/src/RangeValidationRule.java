import java.util.Optional;

public final class RangeValidationRule implements ValidationRule {
    @Override
    public Optional<Deviation> validate(
            Batch batch, Measurement measurement, SpecificationLimit limit) {
        if (limit == null) {
            return Optional.empty();
        }
        double value = measurement.getValue();
        if (value < limit.getMinimum() || value > limit.getMaximum()) {
            return Optional.of(new Deviation(
                    batch.getId(), measurement.getId(), "значение вне допуска", "warning"));
        }
        return Optional.empty();
    }
}
