package br.com.senai;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("dev")
@SpringBootTest(properties = {
        "spring.task.scheduling.enabled=false",
        "app.seed.enabled=false"
})
class ClientServerApplicationTests {

    @Test
    void contextLoads() {
    }

}
