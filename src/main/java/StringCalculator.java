import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static java.util.stream.Collectors.*;
import static java.util.Map.Entry.*;
import static java.util.Map.Entry.comparingByValue;

public class StringCalculator {

    private String separators;
    private final String defaultSeparators = "[,\n]";
    private Map<String,Integer> errors;
    private double[] nums;

    public StringCalculator() {
        this.errors = new HashMap<>();
    }

    public String add(String input) {
        errors.clear();
        if (input.isEmpty()) {
            return "0";
        }
        setSeparators(input);
        input = cleanInput(input);
        if(endsInSeparator(input)) {
            errors.put("Number expected but EOF found.", input.length()-1);
        }
        Optional<Integer> indexOfUnexpectedSeparator = findUnexpectedSeparator(input);
        if (indexOfUnexpectedSeparator.isPresent()) {
            errors.put(String.format("'%s' expected but '%s' found at position %d.",
                    separators.replaceAll("[\\[\\]]",""),
                    input.charAt(indexOfUnexpectedSeparator.get()),
                    indexOfUnexpectedSeparator.get())
                ,indexOfUnexpectedSeparator.get());
        }
        String regex = separators;
        if (indexOfUnexpectedSeparator.isPresent()) {
            regex += "|" + input.charAt(indexOfUnexpectedSeparator.get());
        }
        Optional<Object[]> numberWasExpected = wasNumberExpected(input,regex);
        numberWasExpected.ifPresent(objects -> errors.put((String) objects[0], (int) objects[1]));


        Stream<String> stream = Arrays.stream(input.split(regex));
        if (numberWasExpected.isPresent()) {
            nums = stream.filter(n -> !n.equals("")).map(String::trim).mapToDouble(Double::parseDouble).toArray();
        } else {
            nums = stream.map(String::trim).mapToDouble(Double::parseDouble).toArray();
        }

        Optional<Object[]> negativeNumbersFoundError = findNegativeNumbers(nums);
        negativeNumbersFoundError.ifPresent(objects -> errors.put((String) objects[0], (int) objects[1]));

        if (!errors.isEmpty()) return formatErrorOutput();

        double sum = DoubleStream.of(nums).sum();
        boolean sumHasDecimals = sum % 1 != 0;
        return sumHasDecimals ? String.valueOf(sum) : String.valueOf((int) sum);
    }

    private String formatErrorOutput() {
        return errors.entrySet()
                .stream()
                .sorted(comparingByValue())
                .map(Map.Entry::getKey)
                .collect(Collectors.joining("\n"));
    }

    private Optional<Object[]> wasNumberExpected(String input, String regex) {
        Stream<String> stream = Arrays.stream(input.split(regex));
        if(stream.anyMatch(n -> n.equals(""))) {
            Matcher matcher = Pattern.compile(separators+"{2}").matcher(input);
            if(matcher.find()){
                // I got the index already
                int index = (matcher.start()+1);
                String numberWasExpected = "Number expected but '" + matcher.group().substring(1) + "' found at position " + index + ".";
                return Optional.of(new Object[]{numberWasExpected,index});
            }
        }
        return Optional.empty();
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

    private Optional<Integer> findUnexpectedSeparator(String input) {
        Matcher matcher = Pattern.compile("[^[.]-0-9"+ separators + "]").matcher(input);
        if (matcher.find()) {
            int index = matcher.start();
            return Optional.of(index);
        }
        return Optional.empty();
    }

    private Optional<Object[]> findNegativeNumbers(double[] inputNumbers) {
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
            return Optional.of(new Object[]{errorMessage.toString(),indexFirstConcurrence});
        }
        return Optional.empty();
    }
}
