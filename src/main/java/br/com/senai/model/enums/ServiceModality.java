package br.com.senai.model.enums;

import lombok.Getter;

@Getter
public enum ServiceModality {
<<<<<<< Updated upstream
    PRESENCIAL(0), REMOTO(1);
=======
    PRESENCIAL("Presencial"),
    REMOTO("Remoto");
>>>>>>> Stashed changes

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
        throw new IllegalArgumentException("Código inválido: " + codigo + ".");
    }

    public static ServiceModality fromString(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Modalidade inválida.");
        }

        String normalizedText = text.trim().toUpperCase();
        if (normalizedText.equals(ServiceModality.PRESENCIAL.name())) {
            return ServiceModality.PRESENCIAL;
        }

<<<<<<< Updated upstream
        if (normalizedText.equals(ServiceModality.REMOTO.name())) {
            return ServiceModality.REMOTO;
        }

        throw new IllegalArgumentException("Modalidade inválida " + text + ".");
=======
        return Arrays.stream(values())
                .filter(modality -> modality.name().equals(normalized))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Modalidade invalida. Use Remoto ou Presencial."
                ));
>>>>>>> Stashed changes
    }
}