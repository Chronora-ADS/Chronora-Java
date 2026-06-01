package br.com.senai.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.text.Normalizer;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;

public enum ServiceModality {
    PRESENCIAL("Presencial", Set.of(
            "PRESENCIAL",
            "PRESENTIAL",
            "IN_PERSON",
            "INPERSON",
            "ONSITE",
            "ON_SITE"
    )),
    REMOTO("Remoto", Set.of(
            "REMOTO",
            "REMOTE",
            "ONLINE",
            "VIRTUAL",
            "A_DISTANCIA",
            "DISTANCIA"
    ));

    private final String value;
    private final Set<String> acceptedValues;

    ServiceModality(String value, Set<String> acceptedValues) {
        this.value = value;
        this.acceptedValues = acceptedValues;
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

        String normalized = normalize(value);

        return Arrays.stream(values())
                .filter(modality -> modality.acceptedValues.contains(normalized))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Modalidade inválida: " + value + ". Use Remoto ou Presencial."
                ));
    }

    private static String normalize(String value) {
        return Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .trim()
                .toUpperCase(Locale.ROOT)
                .replaceAll("[^A-Z0-9]+", "_")
                .replaceAll("^_+|_+$", "");
    }
}
