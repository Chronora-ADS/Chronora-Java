package br.com.senai.model.enums;

import lombok.Getter;

@Getter
public enum ServiceModality {
    PRESENCIAL(0),
    REMOTO(1);

    private final int codigo;

    ServiceModality(int codigo) {
        this.codigo = codigo;
    }

    public static ServiceModality fromCodigo(int codigo) {
        for (ServiceModality m : values()) {
            if (m.codigo == codigo) {
                return m;
            }
        }
        throw new IllegalArgumentException("Código inválido: " + codigo);
    }

    public static ServiceModality fromString(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Modalidade inválida");
        }

        String normalizedText = text.trim().toUpperCase();
        if (normalizedText.equals(ServiceModality.PRESENCIAL.name())) {
            return ServiceModality.PRESENCIAL;
        }

        if (normalizedText.equals(ServiceModality.REMOTO.name())) {
            return ServiceModality.REMOTO;
        }

        throw new IllegalArgumentException("Modalidade inválida " + text);
    }
}