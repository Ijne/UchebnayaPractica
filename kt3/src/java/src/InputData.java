import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public final class InputData {
    private final List<RawMaterialLot> rawMaterialLots;
    private final List<Employee> operators;
    private final List<Parameter> parameters;
    private final Batch batch;
    private final List<Measurement> measurements;
    private final List<Specification> specifications;

    public InputData(List<RawMaterialLot> rawMaterialLots, List<Employee> operators,
                     List<Parameter> parameters, Batch batch,
                     List<Measurement> measurements, List<Specification> specifications) {
        this.rawMaterialLots = new ArrayList<>(rawMaterialLots);
        this.operators = new ArrayList<>(operators);
        this.parameters = new ArrayList<>(parameters);
        this.batch = batch;
        this.measurements = new ArrayList<>(measurements);
        this.specifications = new ArrayList<>(specifications);
    }

    public Batch getBatch() { return batch; }
    public List<Measurement> getMeasurements() { return Collections.unmodifiableList(measurements); }
    public List<Specification> getSpecifications() { return Collections.unmodifiableList(specifications); }
    public List<RawMaterialLot> getRawMaterialLots() { return Collections.unmodifiableList(rawMaterialLots); }
    public List<Employee> getOperators() { return Collections.unmodifiableList(operators); }
    public List<Parameter> getParameters() { return Collections.unmodifiableList(parameters); }

    @SuppressWarnings("unchecked")
    public static InputData fromMap(Map<String, Object> root) {
        List<RawMaterialLot> lots = new ArrayList<>();
        for (Object value : (List<Object>) root.get("raw_material_lots")) {
            Map<String, Object> item = (Map<String, Object>) value;
            lots.add(new RawMaterialLot(text(item, "id"), text(item, "supplier"),
                    text(item, "variety"), text(item, "received_at")));
        }

        List<Employee> employees = new ArrayList<>();
        for (Object value : (List<Object>) root.get("operators")) {
            Map<String, Object> item = (Map<String, Object>) value;
            employees.add(new Employee(text(item, "id"), text(item, "name"), text(item, "role")));
        }

        List<Parameter> parameters = new ArrayList<>();
        for (Object value : (List<Object>) root.get("parameters")) {
            Map<String, Object> item = (Map<String, Object>) value;
            parameters.add(new Parameter(text(item, "id"), text(item, "name"), text(item, "unit")));
        }

        Map<String, Object> batchMap = (Map<String, Object>) root.get("batch");
        Batch batch = new Batch(text(batchMap, "id"), text(batchMap, "code"),
                text(batchMap, "date"), text(batchMap, "raw_material_lot_id"),
                text(batchMap, "specification_id"), text(batchMap, "status"));

        List<Measurement> measurements = new ArrayList<>();
        for (Object value : (List<Object>) root.get("measurements")) {
            Map<String, Object> item = (Map<String, Object>) value;
            measurements.add(new Measurement(text(item, "id"), text(item, "batch_id"),
                    text(item, "parameter_id"), number(item, "value"),
                    text(item, "operator_id"), text(item, "measured_at")));
        }

        List<Specification> specifications = new ArrayList<>();
        for (Object value : (List<Object>) root.get("specifications")) {
            Map<String, Object> item = (Map<String, Object>) value;
            List<SpecificationLimit> limits = new ArrayList<>();
            for (Object limitValue : (List<Object>) item.get("limits")) {
                Map<String, Object> limit = (Map<String, Object>) limitValue;
                limits.add(new SpecificationLimit(text(limit, "id"), text(limit, "parameter_id"),
                        number(limit, "minimum"), number(limit, "maximum")));
            }
            specifications.add(new Specification(text(item, "id"), text(item, "version"),
                    text(item, "effective_from"), limits));
        }
        return new InputData(lots, employees, parameters, batch, measurements, specifications);
    }

    private static String text(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (!(value instanceof String)) {
            throw new IllegalArgumentException("Ожидалась строка: " + key);
        }
        return (String) value;
    }

    private static double number(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (!(value instanceof Number)) {
            throw new IllegalArgumentException("Ожидалось число: " + key);
        }
        return ((Number) value).doubleValue();
    }
}
