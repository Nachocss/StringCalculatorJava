import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class StringCalculatorShould {

    StringCalculator calculator;

    @Before
    public void setUp() {
        calculator = new StringCalculator();
    }

    @Test
    public void return_zero_when_parameter_is_empty_string() {
        assertThat(calculator.add("")).isEqualTo("0");
    }

    @Test
    public void return_its_value_when_string_is_a_single_number() {
        assertThat(calculator.add("1")).isEqualTo("1");
    }

    @Test
    public void return_its_sum_when_string_is_several_numbers() {
        assertThat(calculator.add("1,1")).isEqualTo("2");
        assertThat(calculator.add("1.2,1")).isEqualTo("2.2");
        assertThat(calculator.add("1,1,3.3")).isEqualTo("5.3");
    }

    @Test
    public void allow_newlines_as_separator() {
        assertThat(calculator.add("1\n1")).isEqualTo("2");
        assertThat(calculator.add("1\n1,6")).isEqualTo("8");
        assertThat(calculator.add("1\n1,6\n34\n1.1,1")).isEqualTo("44.1");
    }

    @Test
    public void not_allow_input_ending_in_separator() {
        assertThat(calculator.add("1,1,")).isEqualTo("Number expected but EOF found.");
        assertThat(calculator.add("1,1\n")).isEqualTo("Number expected but EOF found.");
        assertThat(calculator.add("1\n1\n")).isEqualTo("Number expected but EOF found.");
        assertThat(calculator.add("1\n1,")).isEqualTo("Number expected but EOF found.");
    }

    @Test
    public void allow_custom_separators() {
        assertThat(calculator.add("//;\n1;2")).isEqualTo("3");
        assertThat(calculator.add("//;\n1;2;")).isEqualTo("Number expected but EOF found.");
        assertThat(calculator.add("//;\n2;3;4")).isEqualTo("9");
        assertThat(calculator.add("//x\n2x3x4")).isEqualTo("9");
        assertThat(calculator.add("//sep\n2sep3")).isEqualTo("5");
        assertThat(calculator.add("//\\|\n22|33|44")).isEqualTo("99");
        assertThat(calculator.add("//\\|\n2|3.5")).isEqualTo("5.5");
        assertThat(calculator.add("//\\|\n1|2,3")).isEqualTo("'\\|' expected but ',' found at position 3.");
        assertThat(calculator.add("1,2;3")).isEqualTo("',\n' expected but ';' found at position 3.");
    }

    @Test
    public void not_allow_negative_numbers() {
        assertThat(calculator.add("-1,2")).isEqualTo("Negative not allowed : -1");
        assertThat(calculator.add("2,-4,-5")).isEqualTo("Negative not allowed : -4, -5");
    }

    @Test
    public void show_several_errors() {
       assertThat(calculator.add("-1,,2")).isEqualTo("Negative not allowed : -1\nNumber expected but ',' found at position 3.");
       assertThat(calculator.add("-1\n,2")).isEqualTo("Negative not allowed : -1\nNumber expected but ',' found at position 3.");
       assertThat(calculator.add("1\n,2,-2")).isEqualTo("Number expected but ',' found at position 2.\nNegative not allowed : -2");
    }

    @Test
    public void support_multiplications() {
        assertThat(calculator.multiply("2,2")).isEqualTo("4");
        assertThat(calculator.multiply("2,4")).isEqualTo("8");
        assertThat(calculator.multiply("2\n2")).isEqualTo("4");
        assertThat(calculator.multiply("//;\n10;2")).isEqualTo("20");
        assertThat(calculator.multiply("//;\n10.6;2")).isEqualTo("21.2");
    }

    @Test
    public void support_divisions() {
        assertThat(calculator.divide("8,2")).isEqualTo("4");
        assertThat(calculator.divide("8,2,2")).isEqualTo("2");
        assertThat(calculator.divide("15.2,3")).isEqualTo("5.066666666666666");
    }

    @Test
    public void support_subtractions() {
        assertThat(calculator.subtract("8,2")).isEqualTo("6");
    }
}