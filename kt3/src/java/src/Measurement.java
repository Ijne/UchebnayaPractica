import java.util.LinkedHashMap;
import java.util.Map;

public final class Measurement {
    private final String id;
    private final String batchId;
    private final String parameterId;
    private final double value;
    private final String operatorId;
    private final String measuredAt;

    public Measurement(String id, String batchId, String parameterId, double value,
                       String operatorId, String measuredAt) {
        this.id = id;
        this.batchId = batchId;
        this.parameterId = parameterId;
        this.value = value;
        this.operatorId = operatorId;
        this.measuredAt = measuredAt;
    }

    public String getId() { return id; }
    public String getBatchId() { return batchId; }
    public String getParameterId() { return parameterId; }
    public double getValue() { return value; }

    public Map<String, Object> toMap() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", id);
        result.put("batch_id", batchId);
        result.put("parameter_id", parameterId);
        result.put("value", value);
        result.put("operator_id", operatorId);
        result.put("measured_at", measuredAt);
        return result;
    }
}
