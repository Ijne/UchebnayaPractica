import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class BatchProtocol {
    private final Batch batch;
    private final List<Measurement> measurements;
    private final List<Deviation> deviations;
    private final List<SpecificationConflict> conflicts;
    private final String status;

    public BatchProtocol(Batch batch, List<Measurement> measurements,
                         List<Deviation> deviations, List<SpecificationConflict> conflicts) {
        this.batch = batch;
        this.measurements = new ArrayList<>(measurements);
        this.deviations = new ArrayList<>(deviations);
        this.conflicts = new ArrayList<>(conflicts);
        this.status = deviations.isEmpty() ? "соответствует" : "требует решения технолога";
    }

    public String getStatus() { return status; }
    public List<SpecificationConflict> getConflicts() { return new ArrayList<>(conflicts); }

    public Map<String, Object> toMap() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("batch", batch.toMap());
        result.put("measurements", QualityControlService.maps(measurements, Measurement::toMap));
        result.put("deviations", QualityControlService.maps(deviations, Deviation::toMap));
        result.put("conflicts", QualityControlService.maps(conflicts, SpecificationConflict::toMap));
        result.put("status", status);
        return result;
    }
}
