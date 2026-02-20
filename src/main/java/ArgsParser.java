import java.util.ArrayList;

public class ArgsParser {
    public static ProcessingConfig parse(String[] args) {
        ProcessingConfig.Builder builder = new ProcessingConfig.Builder();

        ArrayList<String> inputFiles = new ArrayList<>();
        String outputDir = null;
        String prefix = null;
        boolean appendMode = false;
        ProcessingConfig.StatsMode statsMode = ProcessingConfig.StatsMode.NONE;

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];

            if (arg.startsWith("-") && arg.length() > 1) {
                switch (arg) {
                    case "-o":
                        if (i + 1 >= args.length) {
                            throw new IllegalArgumentException("Опция -o требует путь в качестве аргумента");
                        }
                        if (args[i+1].startsWith("-")) {
                            throw new IllegalArgumentException(
                                    String.format("Опция -o требует путь, но получила: %s", args[i+1]));
                        }
                        if (args[i+1].trim().isEmpty()) {
                            throw new IllegalArgumentException("Опция -o требует непустой путь");
                        }
                        outputDir = args[++i];
                        break;

                    case "-p":
                        if (i + 1 >= args.length) {
                            throw new IllegalArgumentException("Опция -p требует префикс в качестве аргумента");
                        }
                        if (args[i+1].startsWith("-")) {
                            throw new IllegalArgumentException(
                                    String.format("Опция -p требует префикс, но получил: %s", args[i+1]));
                        }

                        String rawPrefix = args[++i];

                        if (rawPrefix.trim().isEmpty()) {
                            throw new IllegalArgumentException("Префикс не может быть пустым");
                        }
                        if (rawPrefix.contains("/") || rawPrefix.contains("\\") || rawPrefix.contains("..")) {
                            throw new IllegalArgumentException("Префикс не может содержать символы путей(/ \\ ..)");
                        }

                        String invalidChars = "<>:\"|?*&;$";
                        ArrayList<Character> invalidList = new ArrayList<>();
                        for (char c : invalidChars.toCharArray()) {
                            if (rawPrefix.indexOf(c) >= 0) {
                                invalidList.add(c);
                            }
                        }

                        if (!invalidList.isEmpty()) {
                            StringBuilder sb = new StringBuilder("Префикс содержит недопустимые символы: ");
                            for (int index = 0; index < invalidList.size(); index++) {
                                if (index > 0) {
                                    sb.append(", ");
                                }
                                sb.append("\"").append(invalidList.get(index)).append("\"");
                            }
                            throw new IllegalArgumentException(sb.toString());
                        }

                        if (Character.isWhitespace(rawPrefix.charAt(0)) ||
                                Character.isWhitespace(rawPrefix.charAt(rawPrefix.length() - 1))) {
                            System.out.println("Удалены пробелы в начале и конце префикса");
                            rawPrefix = rawPrefix.trim();
                        }

                        if (rawPrefix.length() > 100) {
                            throw new IllegalArgumentException("Длина префикса больше 100 символов");
                        }

                        prefix = rawPrefix;
                        break;

                    case "-a":
                        appendMode = true;
                        break;

                    case "-s":
                        if (statsMode != ProcessingConfig.StatsMode.NONE) {
                            throw new IllegalArgumentException("Укажите только один режим статистики (-s или -f)");
                        }
                        statsMode = ProcessingConfig.StatsMode.SIMPLE;
                        break;

                    case "-f":
                        if (statsMode != ProcessingConfig.StatsMode.NONE) {
                            throw new IllegalArgumentException("Укажите только один режим статистики (-s или -f)");
                        }
                        statsMode = ProcessingConfig.StatsMode.FULL;
                        break;

                    default:
                        throw new IllegalArgumentException("Неизвестная опция: "+ arg);
                }
            } else {
                inputFiles.add(arg);
            }
        }

        if (inputFiles.isEmpty()) {
            throw new IllegalArgumentException("Укажите хотя бы один входной файл");
        }

        if (outputDir != null) {
            builder.outputDirectory(outputDir);
        }
        if (prefix != null) {
            builder.filePrefix(prefix);
        }
        builder.appendMode(appendMode);
        builder.statsMode(statsMode);
        builder.inputFiles(inputFiles.toArray(new String[0]));

        return builder.build();
    }

    public static void printUsage() {
        System.out.println("==========================================================");
        System.out.println("|РУКОВОДСТВО ПО ЭКСПЛУАТАЦИИ УТИЛИТЫ КЛАССИФИКАЦИИ ДАННЫХ|");
        System.out.println("==========================================================");
        System.out.println();
        System.out.println("Использование:");
        System.out.println("    java DataClassifierApp [опции] <входной-файл>...");
        System.out.println();
        System.out.println("Опции:");
        System.out.println("    -o <путь>       Путь для выходных файлов(текущая директория по умолчанию)");
        System.out.println("    -p <префикс>    Префикс имен выходных файлов (без префикса по умолчанию)");
        System.out.println("    -a              Режим добавления данных в уже существующий файл (перезапись по умолчанию)");
        System.out.println("    -s              Краткая статистика по обработанным данным (только количество)");
        System.out.println("    -f              Полная статистика (количество, мин, макс, сумма, среднее)");
        System.out.println();
        System.out.println("Выходные файлы:");
        System.out.println("    integers.txt    - целые числа");
        System.out.println("    floats.txt      - дробные числа");
        System.out.println("    strings.txt     - строки");
        System.out.println();
        System.out.println("Примеры:");
        System.out.println("    java DataClassifierApp input.txt");
        System.out.println("    java DataClassifierApp -o /tmp -p result_ -a -f input1.txt input2.txt");
        System.out.println("    java DataClassifierApp -s data.txt");
        System.out.println();
    }
}