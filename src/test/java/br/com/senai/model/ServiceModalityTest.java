package br.com.senai.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import br.com.senai.model.enums.ServiceModality;
import org.junit.jupiter.api.Test;

class ServiceModalityTest {

    @Test
    void deveAceitarVariantesDeRemoto() {
        assertEquals(ServiceModality.REMOTO, ServiceModality.fromValue("Remoto"));
        assertEquals(ServiceModality.REMOTO, ServiceModality.fromValue("REMOTE"));
        assertEquals(ServiceModality.REMOTO, ServiceModality.fromValue("online"));
        assertEquals(ServiceModality.REMOTO, ServiceModality.fromValue("\u00C0 dist\u00E2ncia"));
    }

    @Test
    void deveAceitarVariantesDePresencial() {
        assertEquals(ServiceModality.PRESENCIAL, ServiceModality.fromValue("Presencial"));
        assertEquals(ServiceModality.PRESENCIAL, ServiceModality.fromValue("PRESENTIAL"));
        assertEquals(ServiceModality.PRESENCIAL, ServiceModality.fromValue("in person"));
        assertEquals(ServiceModality.PRESENCIAL, ServiceModality.fromValue("on-site"));
    }

    @Test
    void deveRetornarNullQuandoValorForNuloOuVazio() {
        assertNull(ServiceModality.fromValue(null));
        assertNull(ServiceModality.fromValue(" "));
    }

    @Test
    void deveLancarErroQuandoValorNaoForReconhecido() {
        assertThrows(IllegalArgumentException.class, () -> ServiceModality.fromValue("misterioso"));
    }
}
