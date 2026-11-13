import java.util.Optional;

public interface ValidationRule {
    Optional<Deviation> validate(Batch batch, Measurement measurement, SpecificationLimit limit);
}
