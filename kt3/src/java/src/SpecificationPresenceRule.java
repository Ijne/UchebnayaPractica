import java.util.Optional;

public final class SpecificationPresenceRule implements ValidationRule {
    @Override
    public Optional<Deviation> validate(
            Batch batch, Measurement measurement, SpecificationLimit limit) {
        if (limit == null) {
            return Optional.of(new Deviation(
                    batch.getId(), measurement.getId(), "норматив не задан", "critical"));
        }
        return Optional.empty();
    }
}
