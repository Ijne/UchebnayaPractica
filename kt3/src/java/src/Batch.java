import java.util.LinkedHashMap;
import java.util.Map;

public final class Batch {
    private final String id;
    private final String code;
    private final String date;
    private final String rawMaterialLotId;
    private final String specificationId;
    private final String status;

    public Batch(String id, String code, String date, String rawMaterialLotId,
                 String specificationId, String status) {
        this.id = id;
        this.code = code;
        this.date = date;
        this.rawMaterialLotId = rawMaterialLotId;
        this.specificationId = specificationId;
        this.status = status;
    }

    public String getId() { return id; }
    public String getSpecificationId() { return specificationId; }

    public Map<String, Object> toMap() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", id);
        result.put("code", code);
        result.put("date", date);
        result.put("raw_material_lot_id", rawMaterialLotId);
        result.put("specification_id", specificationId);
        result.put("status", status);
        return result;
    }
}
