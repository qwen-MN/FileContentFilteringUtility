import java.nio.file.Path;
import java.nio.file.Paths;

public class ProcessingConfig {
    public enum StatsMode {
        NONE,
        SIMPLE,
        FULL
    }
    private Path outputDirectory;
    private String filePrefix;
    private boolean appendMode;
    private StatsMode statsMode;
    private final Path[] inputFiles;

    public static final String DEFAULT_INTEGERS_FILE = "integers.txt";
    public static final String DEFAULT_FLOATS_FILE = "floats.txt";
    public static final String DEFAULT_STRINGS_FILE = "strings.txt";
    public static final Path DEFAULT_OUTPUT_DIR = Paths.get(".");

    private ProcessingConfig(Path outputDirectory, String filePrefix,
                             boolean appendMode, StatsMode statsMode,
                             Path[] inputFiles) {
        this.outputDirectory = outputDirectory != null ? outputDirectory : DEFAULT_OUTPUT_DIR;
        this.filePrefix = filePrefix != null ? filePrefix : "";
        this.appendMode = appendMode;
        this.statsMode = statsMode != null ? statsMode : StatsMode.NONE;
        this.inputFiles = inputFiles;

        if (this.inputFiles == null || this.inputFiles.length == 0) {
            throw new IllegalArgumentException("Input files must be specified");
        }

    }

    public Path getOutputDirectory() {
        return outputDirectory;
    }

    public String getFilePrefix() {
        return filePrefix;
    }

    public boolean isAppendMode() {
        return appendMode;
    }

    public StatsMode getStatsMode() {
        return statsMode;
    }

    public Path[] getInputFiles() {
        return inputFiles;
    }

    public Path getIntegersOutputPath() {
        return outputDirectory.resolve(filePrefix + DEFAULT_INTEGERS_FILE);
    }

    public Path getFloatsOutputPath() {
        return outputDirectory.resolve(filePrefix + DEFAULT_FLOATS_FILE);
    }

    public Path getStringsOutputPath() {
        return outputDirectory.resolve(filePrefix + DEFAULT_STRINGS_FILE);
    }

    public static class Builder {
        private Path outputDirectory = DEFAULT_OUTPUT_DIR;
        private String filePrefix = "";
        private boolean appendMode = false;
        private StatsMode statsMode = StatsMode.NONE;
        private Path[] inputFiles;

        public Builder outputDirectory(String path) {
            this.outputDirectory = Paths.get(path);
            return this;
        }

        public Builder outputDirectory(Path path) {
            this.outputDirectory = path;
            return this;
        }

        public Builder filePrefix(String prefix) {
            this.filePrefix = prefix != null ? prefix : "";
            return this;
        }

        public Builder appendMode(boolean append) {
            this.appendMode = append;
            return this;
        }

        public Builder statsMode(StatsMode mode) {
            this.statsMode = mode;
            return this;
        }

        public Builder inputFiles(Path... files) {
            this.inputFiles = files;
            return this;
        }

        public Builder inputFiles(String... filePaths) {
            Path[] paths = new Path[filePaths.length];
            for (int i = 0; i < filePaths.length; i++) {
                paths[i] = Paths.get(filePaths[i]);
            }
            this.inputFiles = paths;
            return this;
        }

        public ProcessingConfig build() {
            return new ProcessingConfig(
                    outputDirectory,
                    filePrefix,
                    appendMode,
                    statsMode,
                    inputFiles
            );
        }
    }
}
