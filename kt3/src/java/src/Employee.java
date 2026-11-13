import java.util.LinkedHashMap;
import java.util.Map;

public final class Employee {
    private final String id;
    private final String name;
    private final String role;

    public Employee(String id, String name, String role) {
        this.id = id;
        this.name = name;
        this.role = role;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", id);
        result.put("name", name);
        result.put("role", role);
        return result;
    }
}
