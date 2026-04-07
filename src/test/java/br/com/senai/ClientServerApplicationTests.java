package br.com.senai;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "app.seed.enabled=false")
class ClientServerApplicationTests {

    @Test
    void contextLoads() {
    }

}
