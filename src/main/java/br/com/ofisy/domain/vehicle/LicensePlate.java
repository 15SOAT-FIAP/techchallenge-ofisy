package br.com.ofisy.domain.vehicle;

import br.com.ofisy.domain.vehicle.exceptions.InvalidLicensePlateException;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Objects;

@Getter
@EqualsAndHashCode
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LicensePlate {

    private String value;

    public LicensePlate(String value) {
        Objects.requireNonNull(value, "A placa do veículo não pode ser nula");
        String licensePlate = sanitize(value);
        if (!isValid(licensePlate)) {
            throw new InvalidLicensePlateException(value);
        }
        this.value = licensePlate;
    }

    private String sanitize(String value) {
        return value.replaceAll("[\\s\\-]", "").toUpperCase();
    }

    private boolean isValid(String licensePlate) {
        if (licensePlate.isEmpty()) return false;

        return licensePlate.matches("^[A-Z]{3}\\d{4}$") ||          //Formato antigo:   AAA9999
               licensePlate.matches("^[A-Z]{3}\\d[A-Z]\\d{2}$");    //Formato Mercosul: AAA9A99
    }
}
