import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;


public class DataClassifier {
    private final ProcessingConfig config;
    private final Statistics intStats;
    private final Statistics floatStats;
    private final Statistics stringStats;
    private final ArrayList<String> errors = new ArrayList<>();
    private final AtomicInteger processedFiles = new AtomicInteger(0);
    private final AtomicInteger failedFiles = new AtomicInteger(0);

    private BufferedWriter intWriter = null;
    private BufferedWriter floatWriter = null;
    private BufferedWriter stringWriter = null;

    public DataClassifier(ProcessingConfig config) {
        this.config = config;

        boolean fullStats = config.getStatsMode() == ProcessingConfig.StatsMode.FULL;
        this.intStats = new IntStatistics(fullStats);
        this.floatStats = new FloatStatistics(fullStats);
        this.stringStats = new StringStatistics(fullStats);
    }

    public boolean process() {
        if (!validateOutputDirectory()) {
            return false;
        }

        System.out.println("ОБРАБОТКА ВХОДНЫХ ФАЙЛОВ");
        System.out.println("ВЫХОДНАЯ ДИРЕКТОРИЯ: " + config.getOutputDirectory());
        System.out.println("ПРЕФИКС ФАЙЛОВ: \"" + config.getFilePrefix() + "\"");
        System.out.println("РЕЖИМ " + (config.isAppendMode() ? "ДОБАВЛЕНИЯ" : "ПЕРЕЗАПИСИ"));
        System.out.println();

        for (Path inputFile : config.getInputFiles()) {
            processFile(inputFile);
        }

        closeAllWriters();

        printSummary();

        return errors.isEmpty() || processedFiles.get() > 0;
    }

    private boolean validateOutputDirectory() {
        Path outputDir = config.getOutputDirectory();

        try {
            if (!Files.exists(outputDir)) {
                System.out.println("ДИРЕКТОРИИ " + outputDir + " НЕ СУЩЕСТВУЕТ. СОЗДАЮ");
                Files.createDirectories(outputDir);
                System.out.println("ДИРЕКТОРИЯ " + outputDir + " СОЗДАНА");
            } else if (!Files.isDirectory(outputDir)) {
                System.err.println("ОШИБКА: " + outputDir + " СУЩЕСТВУЕТ, НО ЭТО НЕ ДИРЕКТОРИЯ");
                return false;
            } else if (!Files.isWritable(outputDir)) {
                System.err.println("ОШИБКА: ДИРЕКТОРИЯ" + outputDir + " НЕДОСТУПНА ДЛЯ ЗАПИСИ");
                return false;
            } else if (!Files.isReadable(outputDir)) {
                System.err.println("ОШИБКА: ДИРЕКТОРИЯ" + outputDir + " НЕДОСТУПНА ДЛЯ ЧТЕНИЯ");
                return false;
            }

            FileStore store = Files.getFileStore(outputDir);
            long usableSpace = store.getUsableSpace();
            if (usableSpace < 1024 * 1024) {
                System.out.println("ВНИМАНИЕ: НА ДИСКЕ МАЛО СВОБОДНОГО МЕСТА (" +
                        formatBytes(usableSpace) + ")");
            }
            return true;
        } catch (IOException e) {
            System.err.println("ОШИБКА ПРИ РАБОТЕ С ДИРЕКТОРИЕЙ " + outputDir + ": " + e.getMessage());
            return false;
        } catch (SecurityException e) {
            System.err.println("НЕТ ПРАВ ДОСТУПА К ДИРЕКТОРИИ " + outputDir + ": "+ e.getMessage());
            return false;
        } catch (Exception e) {
            System.err.println("НЕИЗВЕСТНАЯ ОШИБКА ПРИ ВАЛИДАЦИИ ДИРЕКТОРИИ " + outputDir + ": " + e.getMessage());
            return false;
        }
    }

    private void processFile(Path inputFile) {
        System.out.println("ОБРАБОТКА ФАЙЛА: " + inputFile.getFileName());

        if (!Files.exists(inputFile)) {
            String error = "ФАЙЛ НЕ НАЙДЕН: " + inputFile;
            System.err.println("  " + error);
            errors.add(error);
            failedFiles.incrementAndGet();
            return;
        }

        if (!Files.isRegularFile(inputFile)) {
            String error = "ПУТЬ " + inputFile + " НЕ ЯВЛЯЕТСЯ ФАЙЛОМ (ВОЗМОЖНО, ДИРЕКТОРИЯ)";
            System.err.println("  " + error);
            errors.add(error);
            failedFiles.incrementAndGet();
            return;
        }

        if (!Files.isReadable(inputFile)) {
            String error = "ФАЙЛ НЕ ДОСТУПЕН ДЛЯ ЧТЕНИЯ: " + inputFile;
            System.err.println("  " + error);
            errors.add(error);
            failedFiles.incrementAndGet();
            return;
        }

        try {
            long size = Files.size(inputFile);
            if (size == 0) {
                System.out.println("ФАЙЛ ПУСТОЙ: " + inputFile);
                processedFiles.incrementAndGet();
                return;
            }
            if (size > 100 * 1024 * 1024) {
                System.out.println("БОЛЬШОЙ ФАЙЛ: " + inputFile + " (" + formatBytes(size) + ")");
            }
        } catch (IOException e) {
            System.err.println("НЕ УДАЛОСЬ ОПРЕДЕЛИТЬ РАЗМЕР ФАЙЛА: " + inputFile);
        }

        BufferedReader reader = null;
        try {
            reader = Files.newBufferedReader(inputFile, StandardCharsets.UTF_8);

            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                line = line.trim();

                if (line.isEmpty()) {
                    continue;
                }

                try {
                    processLine(line, inputFile, lineNumber);
                } catch (Exception e) {
                    String error = String.format("ОШИБКА ОБРАБОТКИ СТРОКИ %d В ФАЙЛЕ %s: %s",
                            lineNumber, inputFile.getFileName(), e.getMessage());
                    System.err.println("  " + error);
                    errors.add(error);
                }
            }

            processedFiles.incrementAndGet();
            System.out.println("УСПЕШНО ОБРАБОТАН");

        } catch (IOException e) {
            String error = "ОШИБКА ЧТЕНИЯ ФАЙЛА " + inputFile + ": " + e.getMessage();
            System.err.println("  " + error);
            errors.add(error);
            failedFiles.incrementAndGet();
        } catch (SecurityException e) {
            String error = "НЕТ ПРАВ ДОСТУПА К ФАЙЛУ " + inputFile + ": " + e.getMessage();
            System.err.println("  " + error);
            errors.add(error);
            failedFiles.incrementAndGet();
        } catch (OutOfMemoryError e) {
            String error = "НЕДОСТАТОЧНО ПАМЯТИ ДЛЯ ОБРАБОТКИ ФАЙЛА " + inputFile;
            System.err.println("  " + error);
            errors.add(error);
            failedFiles.incrementAndGet();
        } catch (Exception e) {
            String error = "НЕИЗВЕСТНАЯ ОШИБКА ПРИ ОБРАБОТКЕ ФАЙЛА " + inputFile + ": " + e.getMessage();
            System.err.println("  " + error);
            errors.add(error);
            failedFiles.incrementAndGet();
        } finally {
            closeQuietly(reader);
        }
    }

    private void processLine(String line, Path sourceFile, int lineNumber) {
        StringBuilder ints = new StringBuilder();
        StringBuilder floats = new StringBuilder();
        StringBuilder strings = new StringBuilder();

        try (Scanner scanner = new Scanner(line).useLocale(Locale.US)) {
            while (scanner.hasNext()) {
                try {
                    if (scanner.hasNextBigInteger()) {
                        String num = scanner.next();
                        if (!ints.isEmpty()) {
                            ints.append(' ');
                        }
                        ints.append(num);
                        intStats.addValue(num);
                    } else if (scanner.hasNextDouble()) {
                        String num = scanner.next();
                        if (!floats.isEmpty()) {
                            ints.append(' ');
                        }
                        floats.append(num);
                        floatStats.addValue(num);
                    } else {
                        String word = scanner.next();
                        if (!strings.isEmpty()) {
                            strings.append(' ');
                        }
                        strings.append(word);
                        stringStats.addValue(word);
                    }
                } catch (Exception e) {
                    continue;
                }
            }
        }

        try {
            if (!ints.isEmpty()) {
                getOrCreateWriter(() -> intWriter, w -> intWriter = w, config.getIntegersOutputPath());
                intWriter.write(ints.toString());
                intWriter.newLine();
            }

            if (!floats.isEmpty()) {
                getOrCreateWriter(() -> floatWriter, w -> floatWriter = w, config.getFloatsOutputPath());
                floatWriter.write(floats.toString());
                floatWriter.newLine();
            }

            if (!strings.isEmpty()) {
                getOrCreateWriter(() -> stringWriter, w -> stringWriter = w, config.getStringsOutputPath());
                stringWriter.write(strings.toString());
                stringWriter.newLine();
            }
        } catch (IOException e) {
            throw new RuntimeException("ОШИБКА ЗАПИСИ В ВЫХОДНОЙ ФАЙЛ: " + e.getMessage(), e);
        }
    }

    private BufferedWriter getOrCreateWriter(
            Supplier<BufferedWriter> getter,
            Consumer<BufferedWriter> setter,
            Path outputPath) throws IOException {

        BufferedWriter writer = getter.get();
        if (writer == null) {
            writer = createWriter(outputPath);
            setter.accept(writer);
        }
        return writer;
    }

    private BufferedWriter createWriter(Path path) throws IOException {
        Set<OpenOption> options = new HashSet<>();
        options.add(StandardOpenOption.CREATE);

        if (config.isAppendMode()) {
            options.add(StandardOpenOption.APPEND);
        } else {
            options.add(StandardOpenOption.TRUNCATE_EXISTING);
        }

        return Files.newBufferedWriter(path, StandardCharsets.UTF_8, options.toArray(new OpenOption[0]));
    }

    private void closeAllWriters() {
        closeQuietly(intWriter);
        closeQuietly(floatWriter);
        closeQuietly(stringWriter);

        intWriter = null;
        floatWriter = null;
        stringWriter = null;
    }

    private void closeQuietly(Closeable resource) {
        if (resource != null) {
            try {
                resource.close();
            } catch (Exception e) {
                System.err.println("ОШИБКА ЗАКРЫТИЯ РЕСУРСА: " + e.getMessage());
            }
        }
    }

    private String formatBytes(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        }
        if (bytes < 1024 * 1024) {
            return String.format("%.2f KB", bytes / 1024.0);
        }
        if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", bytes / (1024.0 * 1024.0));
        }
        return String.format("%.2f MB", bytes / (1024.0 * 1024.0 * 1024.0));
    }

    private void printSummary() {
        System.out.println();
        System.out.println("==========================================");
        System.out.println("ИТОГОВЫЙ ОТЧЕТ");
        System.out.println("==========================================");
        System.out.println();

        if (config.getStatsMode() != ProcessingConfig.StatsMode.NONE) {
            System.out.println("СТАТИСТИКА:");
            System.out.println("__________________________________________");

            if (config.getStatsMode() == ProcessingConfig.StatsMode.SIMPLE) {
                System.out.println("    Целые числа: " + intStats.getCount());
                System.out.println("    Дробные числа: " + floatStats.getCount());
                System.out.println("    Строки: " + stringStats.getCount());
            } else {
                System.out.println("    " + intStats.getReport());
                System.out.println("    " + floatStats.getReport());
                System.out.println("    " + stringStats.getReport());
            }
            System.out.println();
        }

        System.out.println("СОЗДАННЫЕ ФАЙЛЫ:");
        System.out.println("__________________________________________");

        boolean anyFileCreated = false;

        if (intStats.getCount() > 0) {
            System.out.println("УСПЕШНО СОЗДАН ФАЙЛ " + config.getIntegersOutputPath().getFileName());
            anyFileCreated = true;
        }

        if (floatStats.getCount() > 0) {
            System.out.println("УСПЕШНО СОЗДАН ФАЙЛ " + config.getFloatsOutputPath().getFileName());
            anyFileCreated = true;
        }

        if (stringStats.getCount() > 0) {
            System.out.println("УСПЕШНО СОЗДАН ФАЙЛ " + config.getStringsOutputPath().getFileName());
            anyFileCreated = true;
        }

        if (!anyFileCreated) {
            System.out.println("НИ ОДИН ФАЙЛ НЕ БЫЛ СОЗДАН (НЕТ ДАННЫХ ДЛЯ ЗАПИСИ)");
        }
        System.out.println();

        System.out.println("ОБРАБОТКА ФАЙЛОВ:");
        System.out.println("__________________________________________");
        System.out.println("    УСПЕШНО ОБРАБОТАНО: " + processedFiles.get());
        System.out.println("    ОШИБОК: " + failedFiles.get());
        System.out.println("    ВСЕГО ФАЙЛОВ: " + config.getInputFiles().length);

        if (!errors.isEmpty()) {
            System.out.println("ОБНАРУЖЕННЫЕ ОШИБКИ:");
            System.out.println("__________________________________________");
            for (int i = 0; i < errors.size(); i++) {
                System.out.println(" " + (i + 1) + ". " + errors.get(i));
            }
            System.out.println();
        }

        System.out.println("==========================================");
        if (failedFiles.get() == 0) {
            System.out.println("ОБРАБОТКА ЗАВЕРШЕНА УСПЕШНО");
        } else if (processedFiles.get() > 0) {
            System.out.println("ОБРАБОТКА ЗАВЕРШЕНА С ОШИБКАМИ");
        } else {
            System.out.println("ОБРАБОТКА НЕ УДАЛАСЬ");
        }
        System.out.println("==========================================");
    }
}
