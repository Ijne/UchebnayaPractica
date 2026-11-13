import java.util.LinkedHashMap;
import java.util.Map;

public final class RawMaterialLot {
    private final String id;
    private final String supplier;
    private final String variety;
    private final String receivedAt;

    public RawMaterialLot(String id, String supplier, String variety, String receivedAt) {
        this.id = id;
        this.supplier = supplier;
        this.variety = variety;
        this.receivedAt = receivedAt;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", id);
        result.put("supplier", supplier);
        result.put("variety", variety);
        result.put("received_at", receivedAt);
        return result;
    }
}
