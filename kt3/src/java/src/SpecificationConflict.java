import java.util.Map;

public final class SpecificationConflict extends QualityIssue {
    private final String specificationId;
    private final String limitId;
    private final Double distance;

    public SpecificationConflict(
            String batchId,
            String measurementId,
            String specificationId,
            String limitId,
            Double distance) {
        super(batchId, measurementId);
        this.specificationId = specificationId;
        this.limitId = limitId;
        this.distance = distance;
    }

    public String getSpecificationId() { return specificationId; }
    public String getLimitId() { return limitId; }
    public Double getDistance() { return distance; }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> result = baseMap();
        result.put("specification_id", specificationId);
        result.put("limit_id", limitId);
        result.put("distance", distance);
        return result;
    }
}
