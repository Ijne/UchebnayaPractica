public final class SpecificationLimit {
    private final String id;
    private final String parameterId;
    private final double minimum;
    private final double maximum;

    public SpecificationLimit(String id, String parameterId, double minimum, double maximum) {
        if (minimum > maximum) {
            throw new IllegalArgumentException("Минимум не может превышать максимум");
        }
        this.id = id;
        this.parameterId = parameterId;
        this.minimum = minimum;
        this.maximum = maximum;
    }

    public String getId() { return id; }
    public String getParameterId() { return parameterId; }
    public double getMinimum() { return minimum; }
    public double getMaximum() { return maximum; }
}
