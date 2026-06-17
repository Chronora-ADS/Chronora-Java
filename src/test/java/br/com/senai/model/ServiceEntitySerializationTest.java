package br.com.senai.model;

import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.senai.model.entity.ServiceEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class ServiceEntitySerializationTest {

    @Test
    void deveSerializarExpiracaoDoCodigoComUtcExplicito() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        ServiceEntity service = new ServiceEntity();
        service.setVerificationCodeExpiresAt(
                LocalDateTime.of(2026, 6, 17, 22, 32, 8)
        );

        String json = mapper.writeValueAsString(service);

        assertTrue(json.contains(
                "\"verificationCodeExpiresAt\":\"2026-06-17T22:32:08.000Z\""
        ));
    }
}
