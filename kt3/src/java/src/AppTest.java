import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class AppTest {
    private AppTest() {}

    public static void main(String[] args) {
        Specification oldSpecification = new Specification("SP-0", "old", "2025-01-01",
                Collections.singletonList(new SpecificationLimit("L-0", "p", 0, 1)));
        Specification currentSpecification = new Specification("SP-1", "current", "2026-01-01",
                Collections.singletonList(new SpecificationLimit("L-1", "p", 1, 3)));
        Batch batch = new Batch("B-1", "B", "2026-11-10", "R-1", "SP-1", "контроль");
        Measurement valid = new Measurement("M-1", "B-1", "p", 2, "U-1",
                "2026-11-10T10:00:00+03:00");
        Measurement invalid = new Measurement("M-2", "B-1", "p", 4, "U-1",
                "2026-11-10T10:05:00+03:00");
        Measurement missing = new Measurement("M-3", "B-1", "unknown", 1, "U-1",
                "2026-11-10T10:10:00+03:00");
        List<Specification> specifications = Arrays.asList(oldSpecification, currentSpecification);
        QualityControlService service = new QualityControlService();

        check(service.validateMeasurements(batch, Collections.singletonList(valid), specifications).isEmpty(),
                "должна использоваться назначенная, а не последняя/первая версия");
        List<Deviation> deviations = service.validateMeasurements(
                batch, Arrays.asList(invalid, missing), specifications);
        List<SpecificationConflict> conflicts = service.assessBatchConflicts(
                batch, Arrays.asList(invalid, missing), specifications);
        BatchProtocol protocol = service.formBatchProtocol(
                batch, Arrays.asList(invalid, missing), deviations, conflicts);
        check(deviations.size() == 2, "ожидались два отклонения");
        check(conflicts.size() == 2, "ожидались два конфликта");
        check(Double.valueOf(1.0).equals(conflicts.get(0).getDistance()), "неверное расстояние");
        check(protocol.getConflicts().size() == 2, "конфликты должны входить в протокол");
        check("требует решения технолога".equals(protocol.getStatus()), "неверный статус");
        System.out.println("Все проверки Java пройдены");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
