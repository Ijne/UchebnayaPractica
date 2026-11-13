import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public final class App {
    private App() {}

    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println("Использование: java -cp out App ../python/example_input.json");
            System.exit(2);
        }
        String json = Files.readString(Path.of(args[0]), StandardCharsets.UTF_8);
        Map<String, Object> root = (Map<String, Object>) SimpleJson.parse(json);
        InputData data = InputData.fromMap(root);
        Map<String, Object> result = new QualityControlService().process(data);
        System.out.println(SimpleJson.stringify(result));
    }
}
