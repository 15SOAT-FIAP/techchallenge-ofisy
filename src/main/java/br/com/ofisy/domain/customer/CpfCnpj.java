package br.com.ofisy.domain.customer;

import br.com.ofisy.domain.customer.exceptions.InvalidCpfCnpjException;
import lombok.*;

@Getter
@EqualsAndHashCode
@ToString
public class CpfCnpj {

    private String value;

    public CpfCnpj(String value) {
        String digits = sanitize(value);
        if (!isValid(digits)) {
            throw new InvalidCpfCnpjException(value);
        }
        this.value = digits;
    }

    private String sanitize(String value) {
        if (value == null) return "";
        return value.replaceAll("\\D", "");
    }

    private boolean isValid(String digits) {
        if (digits.isEmpty()) {
            return false;
        }

        if (digits.length() == 11) {
            return isCpfValid(digits);
        } else if (digits.length() == 14) {
            return isCnpjValid(digits);
        }

        return false;
    }

    private boolean isCpfValid(String digits) {
        if (digits.chars().distinct().count() == 1) return false;

        return checkCpfDigits(digits, 10) && checkCpfDigits(digits, 11);
    }

    private boolean checkCpfDigits(String digits, int weight) {
        int sum = 0;
        for (int i = 0; i < weight - 1; i++) {
            sum += Character.getNumericValue(digits.charAt(i)) * (weight - i);
        }
        int remainder = sum % 11;
        int expected = remainder < 2 ? 0 : 11 - remainder;
        return expected == Character.getNumericValue(digits.charAt(weight - 1));
    }

    private boolean isCnpjValid(String digits) {
        if (digits.chars().distinct().count() == 1) return false;

        int[] firstWeights = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        int[] secondWeights = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};

        return checkCnpjDigit(digits, firstWeights, 12) && checkCnpjDigit(digits, secondWeights, 13);
    }

    private boolean checkCnpjDigit(String digits, int[] weights, int position) {
        int sum = 0;
        for (int i = 0; i < weights.length; i++) {
            sum += Character.getNumericValue(digits.charAt(i)) * weights[i];
        }
        int remainder = sum % 11;
        int expected = remainder < 2 ? 0 : 11 - remainder;
        return expected == Character.getNumericValue(digits.charAt(position));
    }
}
