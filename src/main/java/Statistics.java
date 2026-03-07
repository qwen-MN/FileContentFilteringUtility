import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

public interface Statistics {
    void addValue(String value);
    String getReport();
    int getCount();
}

class IntStatistics implements Statistics {
    private int count = 0;
    private BigInteger sum = BigInteger.ZERO;
    private BigInteger min = null;
    private BigInteger max = null;
    private final boolean fullMode;

    public IntStatistics(boolean fullMode) {
        this.fullMode = fullMode;
    }

    @Override
    public void addValue(String value) {
        try {
            BigInteger num = new BigInteger(value);
            count++;
            sum = sum.add(num);

            if (fullMode) {
                if (min == null || num.compareTo(min) < 0) {
                    min = num;
                }
                if (max == null || num.compareTo(max) > 0) {
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
            BigDecimal avg = new BigDecimal(sum)
                    .divide(new BigDecimal(count), 2, RoundingMode.HALF_UP);
            String avgStr = avg.stripTrailingZeros().toPlainString();

            sb.append(String.format(" | Min: %s | Max: %s | Sum: %s | Avg: %s",
                    min, max, sum, avgStr));
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