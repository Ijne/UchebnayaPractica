# Процедурная версия на Python

Требуется Python 3.12 или новее. Внешние зависимости не используются.

```powershell
cd src/python
python main.py example_input.json
python test_main.py
```

Партия явно содержит `specification_id`, поэтому алгоритмы используют назначенную версию спецификации, а не последнюю запись в массиве. Каждое измерение содержит `batch_id` и `measured_at`. Электронный протокол включает и отклонения, и результат `assess_batch_conflicts()`.

`validate_measurements()` строит словарь нормативов и имеет сложность O(n+m). `assess_batch_conflicts()` выполняет полный перебор нормативов для каждого измерения и имеет сложность O(n·m).
