package boogi.apiserver.utils;


import io.restassured.RestAssured;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.MySQLContainer;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class AcceptanceTest {

    @LocalServerPort
    int port;

    protected static final MySQLContainer mySQLContainer = new MySQLContainer("mysql:8");

    @Autowired
    private DataBaseInitializer dataBaseInitializer;

    @BeforeEach
    public void setUp() {
        RestAssured.port = port;
        dataBaseInitializer.setup();
    }

    @AfterEach
    void clearDatabase() {
        dataBaseInitializer.clear();
    }
}
