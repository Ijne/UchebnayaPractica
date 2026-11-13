import java.util.LinkedHashMap;
import java.util.Map;

public final class Parameter {
    private final String id;
    private final String name;
    private final String unit;

    public Parameter(String id, String name, String unit) {
        this.id = id;
        this.name = name;
        this.unit = unit;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", id);
        result.put("name", name);
        result.put("unit", unit);
        return result;
    }
}
