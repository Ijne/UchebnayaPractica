import java.util.Map;

public final class Deviation extends QualityIssue {
    private final String reason;
    private final String severity;

    public Deviation(String batchId, String measurementId, String reason, String severity) {
        super(batchId, measurementId);
        this.reason = reason;
        this.severity = severity;
    }

    public String getReason() { return reason; }
    public String getSeverity() { return severity; }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> result = baseMap();
        result.put("reason", reason);
        result.put("severity", severity);
        return result;
    }
}
