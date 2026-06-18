package br.com.senai.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.Locale;

public enum TrackingType {
    TIME,
    COMPLETION,
    CUSTOM;

    @JsonCreator
    public static TrackingType fromString(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return TrackingType.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Tipo de acompanhamento invalido. Use TIME, COMPLETION ou CUSTOM."
            );
        }
    }
}
