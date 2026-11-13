import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class SimpleJson {
    private SimpleJson() {}

    public static Object parse(String text) {
        Parser parser = new Parser(text);
        Object value = parser.readValue();
        parser.skipWhitespace();
        if (!parser.atEnd()) {
            throw new IllegalArgumentException("Лишние данные после JSON");
        }
        return value;
    }

    public static String stringify(Object value) {
        StringBuilder builder = new StringBuilder();
        writeValue(value, builder, 0);
        return builder.toString();
    }

    @SuppressWarnings("unchecked")
    private static void writeValue(Object value, StringBuilder out, int depth) {
        if (value == null) {
            out.append("null");
        } else if (value instanceof String) {
            out.append('"').append(escape((String) value)).append('"');
        } else if (value instanceof Number || value instanceof Boolean) {
            if (value instanceof Double) {
                double number = (Double) value;
                if (number == Math.rint(number)) {
                    out.append(String.format(java.util.Locale.ROOT, "%.1f", number));
                    return;
                }
            }
            out.append(value);
        } else if (value instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) value;
            out.append("{\n");
            int index = 0;
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                indent(out, depth + 1);
                out.append('"').append(escape(entry.getKey())).append("\": ");
                writeValue(entry.getValue(), out, depth + 1);
                if (++index < map.size()) out.append(',');
                out.append('\n');
            }
            indent(out, depth);
            out.append('}');
        } else if (value instanceof List) {
            List<Object> list = (List<Object>) value;
            out.append('[');
            if (!list.isEmpty()) out.append('\n');
            for (int i = 0; i < list.size(); i++) {
                indent(out, depth + 1);
                writeValue(list.get(i), out, depth + 1);
                if (i + 1 < list.size()) out.append(',');
                out.append('\n');
            }
            if (!list.isEmpty()) indent(out, depth);
            out.append(']');
        } else {
            throw new IllegalArgumentException("Неподдерживаемый тип JSON: " + value.getClass());
        }
    }

    private static void indent(StringBuilder out, int depth) {
        for (int i = 0; i < depth * 2; i++) out.append(' ');
    }

    private static String escape(String text) {
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private static final class Parser {
        private final String text;
        private int index;

        Parser(String text) { this.text = text; }
        boolean atEnd() { return index >= text.length(); }

        void skipWhitespace() {
            while (!atEnd() && Character.isWhitespace(text.charAt(index))) index++;
        }

        Object readValue() {
            skipWhitespace();
            if (atEnd()) throw error("Неожиданный конец JSON");
            char current = text.charAt(index);
            if (current == '{') return readObject();
            if (current == '[') return readArray();
            if (current == '"') return readString();
            if (current == '-' || Character.isDigit(current)) return readNumber();
            if (text.startsWith("true", index)) { index += 4; return true; }
            if (text.startsWith("false", index)) { index += 5; return false; }
            if (text.startsWith("null", index)) { index += 4; return null; }
            throw error("Неизвестное значение");
        }

        Map<String, Object> readObject() {
            expect('{');
            Map<String, Object> result = new LinkedHashMap<>();
            skipWhitespace();
            if (peek('}')) { index++; return result; }
            while (true) {
                skipWhitespace();
                String key = readString();
                skipWhitespace();
                expect(':');
                result.put(key, readValue());
                skipWhitespace();
                if (peek('}')) { index++; return result; }
                expect(',');
            }
        }

        List<Object> readArray() {
            expect('[');
            List<Object> result = new ArrayList<>();
            skipWhitespace();
            if (peek(']')) { index++; return result; }
            while (true) {
                result.add(readValue());
                skipWhitespace();
                if (peek(']')) { index++; return result; }
                expect(',');
            }
        }

        String readString() {
            expect('"');
            StringBuilder result = new StringBuilder();
            while (!atEnd()) {
                char current = text.charAt(index++);
                if (current == '"') return result.toString();
                if (current != '\\') {
                    result.append(current);
                    continue;
                }
                if (atEnd()) throw error("Незавершённая escape-последовательность");
                char escaped = text.charAt(index++);
                switch (escaped) {
                    case '"': result.append('"'); break;
                    case '\\': result.append('\\'); break;
                    case '/': result.append('/'); break;
                    case 'b': result.append('\b'); break;
                    case 'f': result.append('\f'); break;
                    case 'n': result.append('\n'); break;
                    case 'r': result.append('\r'); break;
                    case 't': result.append('\t'); break;
                    case 'u':
                        if (index + 4 > text.length()) throw error("Неверный Unicode escape");
                        result.append((char) Integer.parseInt(text.substring(index, index + 4), 16));
                        index += 4;
                        break;
                    default: throw error("Неизвестный escape: " + escaped);
                }
            }
            throw error("Незавершённая строка");
        }

        Number readNumber() {
            int start = index;
            if (peek('-')) index++;
            while (!atEnd() && Character.isDigit(text.charAt(index))) index++;
            if (!atEnd() && text.charAt(index) == '.') {
                index++;
                while (!atEnd() && Character.isDigit(text.charAt(index))) index++;
            }
            if (!atEnd() && (text.charAt(index) == 'e' || text.charAt(index) == 'E')) {
                index++;
                if (!atEnd() && (text.charAt(index) == '+' || text.charAt(index) == '-')) index++;
                while (!atEnd() && Character.isDigit(text.charAt(index))) index++;
            }
            String token = text.substring(start, index);
            return token.contains(".") || token.contains("e") || token.contains("E")
                    ? Double.parseDouble(token) : Long.parseLong(token);
        }

        void expect(char expected) {
            skipWhitespace();
            if (atEnd() || text.charAt(index) != expected) {
                throw error("Ожидался символ " + expected);
            }
            index++;
        }

        boolean peek(char expected) {
            return !atEnd() && text.charAt(index) == expected;
        }

        IllegalArgumentException error(String message) {
            return new IllegalArgumentException(message + " в позиции " + index);
        }
    }
}
