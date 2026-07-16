package edu.eci.arsw.testing.integration;

import edu.eci.arsw.testing.dto.CreateOrderRequest;
import edu.eci.arsw.testing.dto.OrderResponse;
import edu.eci.arsw.testing.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest
class OrderIntegrationTestcontainersTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");
        registry.add("spring.jpa.properties.hibernate.dialect", () -> "org.hibernate.dialect.PostgreSQLDialect");
    }

    @Autowired
    private OrderService service;

    @Test
    void shouldCreateAndFindOrderWithRealDatabase() {
        CreateOrderRequest request = new CreateOrderRequest("CUS-TC-01", 300000);

        OrderResponse created = service.createOrder(request);
        OrderResponse found = service.findById(created.id());

        assertEquals(created.id(), found.id());
        assertEquals("CUS-TC-01", found.customerId());
        assertEquals(300000, found.total());
        assertEquals("CREATED", found.status());
    }

    @Test
    void shouldThrowExceptionWhenOrderNotFound() {
        assertThrows(IllegalArgumentException.class, () -> service.findById("NONEXISTENT"));
    }
}
