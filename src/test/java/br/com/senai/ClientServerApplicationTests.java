package br.com.senai;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("dev")
@SpringBootTest(properties = {
        "spring.task.scheduling.enabled=false",
        "app.seed.enabled=false",
        "supabase.url=",
        "supabase.anon-key=",
        "supabase.service-role=",
        "supabase.storage-bucket=service-images",
        "spring.datasource.url=jdbc:h2:mem:chronora-test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
        "spring.liquibase.enabled=true",
        "spring.liquibase.change-log=classpath:db/changelog/db.changelog-master.yaml"
})
class ClientServerApplicationTests {

    @Test
    void contextLoads() {
    }

}
