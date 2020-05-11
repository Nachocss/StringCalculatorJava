import java.util.Arrays;
import java.util.stream.DoubleStream;

public class StringCalculator {

    private String separators;
    private final String defaultSeparators = "[,\n]";

    public String add(String input) {
        if (input.isEmpty()) {
            return "0";
        }
        setSeparators(input);
        input = cleanInput(input);
        boolean endsInSeparator = input.substring(input.length()-1).matches(separators);
        if(endsInSeparator) {
            return "Number expected but EOF found.";
        }

        double[] nums = Arrays.stream(input.split(separators))
                .map(String::trim).mapToDouble(Double::parseDouble).toArray();
        double sum = DoubleStream.of(nums).sum();
        boolean sumHasDecimals = sum % 1 != 0;
        return sumHasDecimals ? String.valueOf(sum) : String.valueOf((int) sum);
    }

    private void setSeparators(String input) {
        boolean hasCustomSeparators = input.matches("//(.|\n)*\n(.)*");
        if (hasCustomSeparators) {
            separators = input.substring(input.indexOf("//")+2, input.indexOf("\n"));
            if (separators.equals("|")) separators = "\\|";
        } else {
            separators = defaultSeparators;
        }
    }

    private String cleanInput(String text) {
        if (!separators.equals(defaultSeparators)) {
            return text.substring(text.indexOf("\n")+1);
        }
        return text;
    }
}
