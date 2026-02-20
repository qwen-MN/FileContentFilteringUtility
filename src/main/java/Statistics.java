public interface Statistics {
    void addValue(String value);
    String getReport();
    int getCount();
}

class IntStatistics implements Statistics {
    private int count = 0;
    private long sum = 0;
    private Integer min = null;
    private Integer max = null;
    private final boolean fullMode;

    public IntStatistics(boolean fullMode) {
        this.fullMode = fullMode;
    }

    @Override
    public void addValue(String value) {
        try {
            int num = Integer.parseInt(value);
            count++;
            sum += num;

            if (fullMode) {
                if (min == null || num < min) {
                    min = num;
                }
                if (max == null || num > max) {
                    max = num;
                }
            }
        } catch (NumberFormatException e) {

        }
    }

    @Override
    public int getCount() {
        return count;
    }

    @Override
    public String getReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("Integers: ").append(count);

        if (fullMode && count > 0) {
            double avg = (double) sum / count;
            sb.append(String.format(" | Min: %d | Max: %d | Sum: %d | Avg: %.2f",
                    min, max, sum, avg));
        }

        return sb.toString();
    }
}

class FloatStatistics implements Statistics {
    private int count = 0;
    private double sum = 0;
    private Double min = null;
    private Double max = null;
    private final boolean fullMode;

    public FloatStatistics(boolean fullMode) {
        this.fullMode = fullMode;
    }

    @Override
    public void addValue(String value) {
        try {
            double num = Double.parseDouble(value);
            count++;
            sum += num;

            if (fullMode) {
                if (min == null || num < min) {
                    min = num;
                }
                if (max == null || num > max) {
                    max = num;
                }
            }
        } catch (NumberFormatException e) {

        }
    }

    @Override
    public int getCount() {
        return count;
    }

    @Override
    public String getReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("Floats: ").append(count);

        if (fullMode && count > 0) {
            double avg = sum / count;
            sb.append(String.format(" | Min: %f | Max: %f | Sum: %f | Avg: %.2f",
                    min, max, sum, avg));
        }

        return sb.toString();
    }
}

class StringStatistics implements Statistics {
    private int count = 0;
    private Integer minLength = null;
    private Integer maxLength = null;
    private final boolean fullMode;

    public StringStatistics(boolean fullMode) {
        this.fullMode = fullMode;
    }

    @Override
    public void addValue(String value) {
        count++;

        if (fullMode) {
            int length = value.length();
            if (minLength == null || length < minLength) {
                minLength = length;
            }
            if (maxLength == null || length > minLength) {
                maxLength = length;
            }
        }
    }

    @Override
    public int getCount() {
        return count;
    }

    @Override
    public String getReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("Strings: ").append(count);

        if (fullMode && count > 0) {
            sb.append(String.format(" | Min length: %d | Max length: %d",
                    minLength, maxLength));
        }

        return sb.toString();
    }
}