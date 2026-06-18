package br.com.senai.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.junit.jupiter.api.Test;

class ServiceProgressTrackingMigrationTest {

    @Test
    void deveAplicarColunasDefaultsEConstraintsDaMetrica() throws Exception {
        String databaseUrl =
                "jdbc:h2:mem:service-progress-tracking;MODE=PostgreSQL;DB_CLOSE_DELAY=-1";
        try (Connection connection = DriverManager.getConnection(
                databaseUrl,
                "sa",
                ""
        )) {
            try (Statement statement = connection.createStatement()) {
                statement.execute("CREATE TABLE service (id BIGINT PRIMARY KEY)");
            }

            Database database = DatabaseFactory.getInstance()
                    .findCorrectDatabaseImplementation(new JdbcConnection(connection));
            try (Liquibase liquibase = new Liquibase(
                    "db/changelog/db.changelog-service-progress-tracking.yaml",
                    new ClassLoaderResourceAccessor(),
                    database
            )) {
                liquibase.update(new Contexts(), new LabelExpression());
            }
        }

        try (Connection connection = DriverManager.getConnection(databaseUrl, "sa", "");
             Statement statement = connection.createStatement()) {
            statement.execute("INSERT INTO service (id) VALUES (1)");
            try (ResultSet result = statement.executeQuery(
                    "SELECT tracking_type FROM service WHERE id = 1"
            )) {
                result.next();
                assertEquals("TIME", result.getString("tracking_type"));
            }

            assertThrows(SQLException.class, () -> statement.execute(
                    "INSERT INTO service (id, tracking_type) VALUES (2, 'OTHER')"
            ));
            assertThrows(SQLException.class, () -> statement.execute(
                    "INSERT INTO service (id, tracking_type) VALUES (3, 'CUSTOM')"
            ));

            statement.execute(
                    "INSERT INTO service (id, tracking_type, tracking_description) "
                            + "VALUES (4, 'CUSTOM', 'Por metro quadrado pintado')"
            );
        }
    }
}
