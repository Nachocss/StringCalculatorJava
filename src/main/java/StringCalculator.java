import java.util.Arrays;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
        if(endsInSeparator(input)) {
            return "Number expected but EOF found.";
        }
        Optional<Integer> indexOfUnexpectedSeparator = findUnexpectedSeparators(input);
        if (indexOfUnexpectedSeparator.isPresent()) {
            return String.format("'%s' expected but '%s' found at position %d.",
                    separators.replace("[","").replace("]",""),
                    input.charAt(indexOfUnexpectedSeparator.get()),
                    indexOfUnexpectedSeparator.get());
        }

        double[] nums = Arrays.stream(input.split(separators))
                .map(String::trim).mapToDouble(Double::parseDouble).toArray();

        Optional<String> negativeNumbersFoundError = findNegativeNumbers(nums);
        if (negativeNumbersFoundError.isPresent()) {
            return negativeNumbersFoundError.get();
        }

        double sum = DoubleStream.of(nums).sum();
        boolean sumHasDecimals = sum % 1 != 0;
        return sumHasDecimals ? String.valueOf(sum) : String.valueOf((int) sum);
    }

    private void setSeparators(String input) {
        boolean hasCustomSeparators = input.matches("//(.|\n)*\n(.)*");
        if (hasCustomSeparators) {
            separators = input.substring(2, input.indexOf("\n"));
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

    private boolean endsInSeparator(String input) {
        return input.substring(input.length()-1).matches(separators);
    }

    private Optional<Integer> findUnexpectedSeparators(String input) {
        Matcher matcher = Pattern.compile("[^[.]-0-9"+ separators + "]").matcher(input);
        if (matcher.find()) {
            int index = matcher.start();
            return Optional.of(index);
        }
        return Optional.empty();
    }

    private Optional<String> findNegativeNumbers(double[] nums) {
        double[] negativeNumbers = Arrays.stream(nums).filter(n -> n < 0).toArray();

        if (negativeNumbers.length > 0) {
            StringBuilder errorMessage = new StringBuilder("Negative not allowed : ");
            for (int i = 0; i < negativeNumbers.length; i++) {
                double number = negativeNumbers[i];
                errorMessage.append(String.format("%d", (int) number));
                if (i < negativeNumbers.length-1) {
                    errorMessage.append(", ");
                }
            }
            return Optional.of(errorMessage.toString());
        }
        return Optional.empty();
    }
}
