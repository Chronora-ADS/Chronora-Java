package br.com.senai.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import br.com.senai.model.enums.ServiceModality;
import org.junit.jupiter.api.Test;

class ServiceModalityTest {

    @Test
    void deveAceitarFromCodigoDePresencial() {
        assertEquals(ServiceModality.PRESENCIAL, ServiceModality.fromCodigo(0));
    }

    @Test
    void deveAceitarFromCodigoDeRemoto() {
        assertEquals(ServiceModality.REMOTO, ServiceModality.fromCodigo(1));
    }

    @Test
    void deveLancarErroQuandoValorNaoForReconhecido() {
        assertThrows(IllegalArgumentException.class, () -> ServiceModality.fromCodigo(2));
    }
}
