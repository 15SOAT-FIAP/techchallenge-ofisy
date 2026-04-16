package br.com.ofisy.domain.vehicle;

import br.com.ofisy.domain.vehicle.exceptions.InvalidLicensePlateException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LicensePlateTest {

    @Nested
    class ValidOldFormat {

        @ParameterizedTest
        @ValueSource(strings = {"ABC1234", "XYZ9999", "AAA0000"})
        void shouldAcceptValidOldFormatPlate(String plate) {
            var licensePlate = new LicensePlate(plate);

            assertThat(licensePlate.getValue()).isEqualTo(plate);
        }
    }

    @Nested
    class ValidMercosulFormat {

        @ParameterizedTest
        @ValueSource(strings = {"ABC1D23", "XYZ9A99", "AAA0B00"})
        void shouldAcceptValidMercosulFormatPlate(String plate) {
            var licensePlate = new LicensePlate(plate);

            assertThat(licensePlate.getValue()).isEqualTo(plate);
        }
    }

    @Nested
    class Sanitization {

        @Test
        void shouldRemoveHyphensAndAccept() {
            var licensePlate = new LicensePlate("ABC-1234");

            assertThat(licensePlate.getValue()).isEqualTo("ABC1234");
        }

        @Test
        void shouldRemoveWhitespaceAndAccept() {
            var licensePlate = new LicensePlate("ABC 1234");

            assertThat(licensePlate.getValue()).isEqualTo("ABC1234");
        }

        @Test
        void shouldConvertToUppercaseAndAccept() {
            var licensePlate = new LicensePlate("abc1234");

            assertThat(licensePlate.getValue()).isEqualTo("ABC1234");
        }

        @Test
        void shouldSanitizeMercosulWithHyphenAndLowercase() {
            var licensePlate = new LicensePlate("abc-1d23");

            assertThat(licensePlate.getValue()).isEqualTo("ABC1D23");
        }
    }

    @Nested
    class InvalidInput {

        @Test
        void shouldThrowNullPointerExceptionWhenNull() {
            assertThatThrownBy(() -> new LicensePlate(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("não pode ser nula");
        }

        @Test
        void shouldThrowInvalidLicensePlateExceptionWhenEmpty() {
            assertThatThrownBy(() -> new LicensePlate(""))
                    .isInstanceOf(InvalidLicensePlateException.class);
        }

        @Test
        void shouldThrowInvalidLicensePlateExceptionWhenBlank() {
            assertThatThrownBy(() -> new LicensePlate("   "))
                    .isInstanceOf(InvalidLicensePlateException.class);
        }
    }

    @Nested
    class InvalidFormat {

        @ParameterizedTest
        @ValueSource(strings = {"AB12345", "1234ABC", "ABC123456", "ABCD123", "ABC12", "12345678"})
        void shouldThrowInvalidLicensePlateExceptionForWrongFormat(String plate) {
            assertThatThrownBy(() -> new LicensePlate(plate))
                    .isInstanceOf(InvalidLicensePlateException.class)
                    .hasMessageContaining(plate);
        }

        @Test
        void shouldThrowInvalidLicensePlateExceptionWithOriginalValueInMessage() {
            var invalidPlate = "INVALID";

            assertThatThrownBy(() -> new LicensePlate(invalidPlate))
                    .isInstanceOf(InvalidLicensePlateException.class)
                    .hasMessageContaining(invalidPlate);
        }
    }

    @Nested
    class Equality {

        @Test
        void shouldBeEqualWhenSameValue() {
            var plate1 = new LicensePlate("ABC1234");
            var plate2 = new LicensePlate("ABC1234");

            assertThat(plate1).isEqualTo(plate2);
        }

        @Test
        void shouldHaveSameHashCodeWhenSameValue() {
            var plate1 = new LicensePlate("ABC1234");
            var plate2 = new LicensePlate("ABC1234");

            assertThat(plate1.hashCode()).hasSameHashCodeAs(plate2.hashCode());
        }

        @Test
        void shouldNotBeEqualWhenDifferentValue() {
            var plate1 = new LicensePlate("ABC1234");
            var plate2 = new LicensePlate("XYZ9999");

            assertThat(plate1).isNotEqualTo(plate2);
        }

        @Test
        void shouldBeEqualAfterSanitization() {
            var plate1 = new LicensePlate("ABC1234");
            var plate2 = new LicensePlate("abc-1234");

            assertThat(plate1).isEqualTo(plate2);
        }
    }
}
