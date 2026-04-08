package br.com.ofisy.domain.customer;

import br.com.ofisy.domain.customer.exceptions.InvalidCpfCnpjException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CpfCnpjTest {

    private static final String VALID_CPF = "52998224725";
    private static final String VALID_CNPJ = "11222333000181";

    @Nested
    class ValidCpf {

        @Test
        void shouldCreateWithValidUnformattedCpf() {
            var cpfCnpj = new CpfCnpj(VALID_CPF);

            assertThat(cpfCnpj.getValue()).isEqualTo(VALID_CPF);
        }

        @Test
        void shouldCreateWithFormattedCpf() {
            var cpfCnpj = new CpfCnpj("529.982.247-25");

            assertThat(cpfCnpj.getValue()).isEqualTo(VALID_CPF);
        }

        @Test
        void shouldCreateCpfWithCheckDigitZeroFromRemainderLessThanTwo() {
            var cpfCnpj = new CpfCnpj("10000000108");

            assertThat(cpfCnpj.getValue()).isEqualTo("10000000108");
        }
    }

    @Nested
    class ValidCnpj {

        @Test
        void shouldCreateWithValidUnformattedCnpj() {
            var cpfCnpj = new CpfCnpj(VALID_CNPJ);
           
            assertThat(cpfCnpj.getValue()).isEqualTo(VALID_CNPJ);
        }

        @Test
        void shouldCreateWithFormattedCnpj() {
            var cpfCnpj = new CpfCnpj("11.222.333/0001-81");

            assertThat(cpfCnpj.getValue()).isEqualTo(VALID_CNPJ);
        }

        @Test
        void shouldCreateCnpjWithCheckDigitZeroFromRemainderLessThanTwo() {
            var cpfCnpj = new CpfCnpj("01000000000405");

            assertThat(cpfCnpj.getValue()).isEqualTo("01000000000405");
        }
    }

    @Nested
    class InvalidInput {

        @Test
        void shouldThrowExceptionForNullValue() {
            assertThatThrownBy(() -> new CpfCnpj(null))
                    .isInstanceOf(InvalidCpfCnpjException.class);
        }

@ParameterizedTest
@ValueSource(strings = {
    "",
    "   ",
    "abcdefghijk",
    "12345",
    "123456789012345"
})
void shouldThrowExceptionForInvalidInputs(String invalidValue) {
    assertThatThrownBy(() -> new CpfCnpj(invalidValue))
            .isInstanceOf(InvalidCpfCnpjException.class);
}

        @Test
        void shouldIncludeInvalidValueInExceptionMessage() {
            var invalidValue = "invalid-doc";

            assertThatThrownBy(() -> new CpfCnpj(invalidValue))
                    .isInstanceOf(InvalidCpfCnpjException.class)
                    .hasMessageContaining(invalidValue);
        }
    }

    @Nested
    class InvalidCpf {

        @Test
        void shouldThrowExceptionForCpfWithAllSameDigits() {
            assertThatThrownBy(() -> new CpfCnpj("11111111111"))
                    .isInstanceOf(InvalidCpfCnpjException.class);
        }

        @Test
        void shouldThrowExceptionForAllZeroCpf() {
            assertThatThrownBy(() -> new CpfCnpj("00000000000"))
                    .isInstanceOf(InvalidCpfCnpjException.class);
        }

        @Test
        void shouldThrowExceptionForCpfWithInvalidCheckDigit() {
            assertThatThrownBy(() -> new CpfCnpj("52998224720"))
                    .isInstanceOf(InvalidCpfCnpjException.class);
        }

        @Test
        void shouldThrowExceptionForCpfWithValidFirstButInvalidSecondCheckDigit() {
            assertThatThrownBy(() -> new CpfCnpj("10000000010"))
                    .isInstanceOf(InvalidCpfCnpjException.class);
        }
    }

    @Nested
    class InvalidCnpj {

        @Test
        void shouldThrowExceptionForCnpjWithAllSameDigits() {
            assertThatThrownBy(() -> new CpfCnpj("11111111111111"))
                    .isInstanceOf(InvalidCpfCnpjException.class);
        }

        @Test
        void shouldThrowExceptionForAllZeroCnpj() {
            assertThatThrownBy(() -> new CpfCnpj("00000000000000"))
                    .isInstanceOf(InvalidCpfCnpjException.class);
        }

        @Test
        void shouldThrowExceptionForCnpjWithInvalidCheckDigit() {
            assertThatThrownBy(() -> new CpfCnpj("11222333000199"))
                    .isInstanceOf(InvalidCpfCnpjException.class);
        }

        @Test
        void shouldThrowExceptionForCnpjWithValidFirstButInvalidSecondCheckDigit() {
            assertThatThrownBy(() -> new CpfCnpj("01000000000074"))
                    .isInstanceOf(InvalidCpfCnpjException.class);
        }
    }

    @Nested
    class Equality {

        @Test
        void shouldBeEqualForSameValue() {
            var cpf1 = new CpfCnpj(VALID_CPF);
            var cpf2 = new CpfCnpj(VALID_CPF);

            assertThat(cpf1).isEqualTo(cpf2);
            assertThat(cpf1.hashCode()).hasSameHashCodeAs(cpf2.hashCode());
        }

        @Test
        void shouldBeEqualForFormattedAndUnformattedCpf() {
            var cpf1 = new CpfCnpj("529.982.247-25");
            var cpf2 = new CpfCnpj(VALID_CPF);

            assertThat(cpf1).isEqualTo(cpf2);
        }

        @Test
        void shouldNotBeEqualForDifferentValues() {
            var cpf = new CpfCnpj(VALID_CPF);
            var cnpj = new CpfCnpj(VALID_CNPJ);

            assertThat(cpf).isNotEqualTo(cnpj);
        }

        @Test
        void shouldReturnValueInToString() {
            var cpfCnpj = new CpfCnpj(VALID_CPF);

            assertThat(cpfCnpj.toString()).contains(VALID_CPF);
        }
    }
}