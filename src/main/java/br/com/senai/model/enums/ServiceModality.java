package br.com.senai.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.text.Normalizer;
import java.util.Arrays;
import java.util.Locale;

public enum ServiceModality {
    PRESENCIAL("Presencial"),
    REMOTO("Remoto"),
    HIBRIDO("Hibrido");

    private final String value;

    ServiceModality(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static ServiceModality fromValue(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .trim()
                .toUpperCase(Locale.ROOT);

        return Arrays.stream(values())
                .filter(modality -> modality.name().equals(normalized))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Modalidade invalida. Use Remoto, Presencial ou Hibrido."
                ));
    }
}
