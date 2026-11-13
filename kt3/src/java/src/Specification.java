import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Specification {
    private final String id;
    private final String version;
    private final String effectiveFrom;
    private final List<SpecificationLimit> limits;

    public Specification(String id, String version, String effectiveFrom,
                         List<SpecificationLimit> limits) {
        this.id = id;
        this.version = version;
        this.effectiveFrom = effectiveFrom;
        this.limits = new ArrayList<>(limits);
    }

    public String getId() { return id; }
    public String getVersion() { return version; }
    public String getEffectiveFrom() { return effectiveFrom; }
    public List<SpecificationLimit> getLimits() { return Collections.unmodifiableList(limits); }
}
