import java.util.LinkedHashMap;
import java.util.Map;

public abstract class QualityIssue {
    private final String batchId;
    private final String measurementId;

    protected QualityIssue(String batchId, String measurementId) {
        this.batchId = batchId;
        this.measurementId = measurementId;
    }

    public String getBatchId() { return batchId; }
    public String getMeasurementId() { return measurementId; }

    protected Map<String, Object> baseMap() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("batch_id", batchId);
        result.put("measurement_id", measurementId);
        return result;
    }

    public abstract Map<String, Object> toMap();
}
