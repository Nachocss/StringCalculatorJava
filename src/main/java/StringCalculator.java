import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

public class StringCalculator {

    private String separators;
    private final String defaultSeparators = "[,\n]";
    private List<ErrorInfo> errors;
    private double[] factors;

    public StringCalculator() {
        this.errors = new ArrayList<>();
    }

    public String add(String input) {
        if (input.isEmpty()) return "0";
        performCommonOperations(input);
        if (!errors.isEmpty()) return formatErrorOutput();

        double result = DoubleStream.of(factors).sum();
        boolean resultHasDecimals = result % 1 != 0;
        return resultHasDecimals ? String.valueOf(result) : String.valueOf((int) result);
    }

    public String multiply(String input) {
        if (input.isEmpty()) return "0";
        performCommonOperations(input);
        if (!errors.isEmpty()) return formatErrorOutput();

        double result = DoubleStream.of(factors).reduce(1, (a, b) -> a * b);
        boolean resultHasDecimals = result % 1 != 0;
        return resultHasDecimals ? String.valueOf(result) : String.valueOf((int) result);
    }

    public String subtract(String input) {
        if (input.isEmpty()) return "0";
        performCommonOperations(input);
        if (!errors.isEmpty()) return formatErrorOutput();

        double result = factors[0];
        for (int i = 1; i < factors.length; i++) {
            result -= factors[i];
        }
        boolean resultHasDecimals = result % 1 != 0;
        return resultHasDecimals ? String.valueOf(result) : String.valueOf((int) result);
    }

    public String divide(String input) {
        if (input.isEmpty()) return "0";
        performCommonOperations(input);
        if (!errors.isEmpty()) return formatErrorOutput();

        double result = factors[0];
        for (int i = 1; i < factors.length; i++) {
            result /= factors[i];
        }
        boolean productHasDecimals = result % 1 != 0;
        return productHasDecimals ? String.valueOf(result) : String.valueOf((int) result);
    }

    private void performCommonOperations(String input) {
        setSeparators(input);
        input = cleanInput(input);
        setFactors(input);
    }

    private void setFactors(String input) {
        errors.clear();
        if(endsInSeparator(input)) {
            errors.add(new ErrorInfo("Number expected but EOF found.", input.length()-1));
        }
        Optional<ErrorInfo> indexOfUnexpectedSeparator = findUnexpectedSeparator(input);
        indexOfUnexpectedSeparator.ifPresent(error -> errors.add(error));

        String regex = separators;
        if (indexOfUnexpectedSeparator.isPresent()) {
            regex += "|" + input.charAt(indexOfUnexpectedSeparator.get().getIndex());
        }

        Optional<ErrorInfo> numberWasExpected = wasNumberExpected(input,regex);
        numberWasExpected.ifPresent(error -> errors.add(error));

        Stream<String> stream = Arrays.stream(input.split(regex));
        if (numberWasExpected.isPresent()) {
            factors = stream.filter(n -> !n.equals("")).map(String::trim).mapToDouble(Double::parseDouble).toArray();
        } else {
            factors = stream.map(String::trim).mapToDouble(Double::parseDouble).toArray();
        }

        Optional<ErrorInfo> negativeNumbersFoundError = findNegativeNumbers(factors);
        negativeNumbersFoundError.ifPresent(error -> errors.add(error));
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

    private Optional<ErrorInfo> findUnexpectedSeparator(String input) {
        Matcher matcher = Pattern.compile("[^[.]-0-9"+ separators + "]").matcher(input);
        if (matcher.find()) {
            int index = matcher.start();
            String message = String.format("'%s' expected but '%s' found at position %d.",
                    separators.replaceAll("[\\[\\]]",""),
                    input.charAt(index),index);
            return Optional.of(new ErrorInfo(message,index));
        }
        return Optional.empty();
    }

    private Optional<ErrorInfo> findNegativeNumbers(double[] inputNumbers) {
        double[] negativeNumbers = Arrays.stream(inputNumbers).filter(n -> n < 0).toArray();

        if (negativeNumbers.length > 0) {
            StringBuilder errorMessage = new StringBuilder("Negative not allowed : ");
            for (int i = 0; i < negativeNumbers.length; i++) {
                double number = negativeNumbers[i];
                errorMessage.append(String.format("%d", (int) number));
                if (i < negativeNumbers.length-1) {
                    errorMessage.append(", ");
                }
            }
            int indexFirstConcurrence = -1;
            for (int i = 0; i < inputNumbers.length; i++) {
                for (double negativeNumber : negativeNumbers) {
                    if (inputNumbers[i] == negativeNumber) {
                        indexFirstConcurrence = i;
                        break;
                    }
                }
            }
            return Optional.of(new ErrorInfo(errorMessage.toString(),indexFirstConcurrence));
        }
        return Optional.empty();
    }

    private Optional<ErrorInfo> wasNumberExpected(String input, String regex) {
        Stream<String> stream = Arrays.stream(input.split(regex));
        if(stream.anyMatch(n -> n.equals(""))) {
            Matcher matcher = Pattern.compile(separators+"{2}").matcher(input);
            if(matcher.find()){
                int index = (matcher.start()+1);
                String numberWasExpected = "Number expected but '" + matcher.group().substring(1) + "' found at position " + index + ".";
                return Optional.of(new ErrorInfo(numberWasExpected,index));
            }
        }
        return Optional.empty();
    }

    private String formatErrorOutput() {
        errors.sort(Comparator.comparingInt(ErrorInfo::getIndex));
        StringBuilder errorOutput = new StringBuilder();
        Iterator<ErrorInfo> iterator = errors.iterator();
        while (iterator.hasNext()) {
            errorOutput.append((iterator.next()).getMessage());
            if (iterator.hasNext()) errorOutput.append("\n");
        }
        return errorOutput.toString();
    }
}