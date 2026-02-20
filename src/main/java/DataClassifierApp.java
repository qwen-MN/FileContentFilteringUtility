public class DataClassifierApp {
    public static void main(String[] args) {
        if(args.length == 0) {
            ArgsParser.printUsage();
            System.exit(1);
        }

        ProcessingConfig config = null;

        try {
            config = ArgsParser.parse(args);
        } catch (IllegalAccessError e) {
            System.err.println("ОШИБКА В АРГУМЕНТАХ: " + e.getMessage());
            System.err.println();
            ArgsParser.printUsage();
            System.exit(1);
        } catch (Exception e) {
            System.err.println("КРИТИЧЕСКАЯ ОШИБКА ПРИ ПАРСИНГЕ АРГУМЕНТОВ: " + e.getMessage());
            System.err.println();
            ArgsParser.printUsage();
            System.exit(1);
        }

        try {
            DataClassifier classifier = new DataClassifier(config);
            boolean success = classifier.process();

            System.exit(success ? 0 : 1);
        } catch (Exception e) {
            System.err.println("КРИТИЧЕСКАЯ ОШИБКА ПРИ ВЫПОЛНЕНИИ:");
            System.err.println("  " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
