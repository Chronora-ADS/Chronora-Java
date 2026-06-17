package br.com.senai.model.enums;

import lombok.Getter;

import java.text.Normalizer;
import java.util.Locale;

@Getter
public enum ServiceModality {
    PRESENCIAL(0),
    REMOTO(1);

    private final int codigo;

    ServiceModality(int codigo) {
        this.codigo = codigo;
    }

    public static ServiceModality fromCodigo(int codigo) {
        for (ServiceModality modality : values()) {
            if (modality.codigo == codigo) {
                return modality;
            }
        }
        throw new IllegalArgumentException("Codigo invalido: " + codigo + ".");
    }

    public static ServiceModality fromString(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Modalidade invalida.");
        }

        String normalizedText = Normalizer.normalize(text.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replace('-', '_')
                .replace(' ', '_')
                .toUpperCase(Locale.ROOT);

        if (normalizedText.equals("PRESENCIAL")) {
            return PRESENCIAL;
        }

        if (normalizedText.equals("REMOTO")
                || normalizedText.equals("REMOTE")
                || normalizedText.equals("A_DISTANCIA")) {
            return REMOTO;
        }

        throw new IllegalArgumentException(
                "Modalidade inválida. Use Remoto ou Presencial."
        );
    }
}
