# Объектно ориентированная версия на Java

Требуется JDK 11 или новее. Внешние библиотеки и база данных не нужны.

```powershell
cd src/java
New-Item -ItemType Directory -Force out | Out-Null
javac -encoding UTF-8 -d out src/*.java
java -cp out App ../python/example_input.json
java -cp out AppTest
```

`Batch`, `Specification`, `SpecificationLimit`, `Measurement`, `Parameter`, `RawMaterialLot`, `Employee` и `Deviation` соответствуют исправленной логической модели КТ2. Поля закрыты, доступ предоставляется методами. Интерфейс `ValidationRule` имеет две реализации: `SpecificationPresenceRule` и `RangeValidationRule`. Абстрактный `QualityIssue` объединяет общие данные, а `Deviation` и `SpecificationConflict` уточняют формат двух видов проблемы.

Коллекции применены по назначению: `List` хранит упорядоченные измерения и результаты, `Map` обеспечивает поиск норматива за O(1) в `validateMeasurements()`, `Set` контролирует уникальность идентификаторов измерений. `assessBatchConflicts()` сохраняет полный вложенный перебор O(n·m), как в процедурной версии.
