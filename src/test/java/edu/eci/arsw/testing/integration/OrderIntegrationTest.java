package edu.eci.arsw.testing.integration;

import edu.eci.arsw.testing.dto.CreateOrderRequest;
import edu.eci.arsw.testing.dto.OrderResponse;
import edu.eci.arsw.testing.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class OrderIntegrationTest {

    @Autowired
    private OrderService service;

    @Test
    void shouldCreateAndFindOrder() {
        CreateOrderRequest request = new CreateOrderRequest("CUS-99", 250000);

        OrderResponse created = service.createOrder(request);
        OrderResponse found = service.findById(created.id());

        assertEquals(created.id(), found.id());
        assertEquals("CUS-99", found.customerId());
        assertEquals(250000, found.total());
        assertEquals("CREATED", found.status());
    }

    @Test
    void shouldCreateMultipleOrdersAndFindEach() {
        CreateOrderRequest req1 = new CreateOrderRequest("CUS-01", 100000);
        CreateOrderRequest req2 = new CreateOrderRequest("CUS-02", 250000);

        OrderResponse ord1 = service.createOrder(req1);
        OrderResponse ord2 = service.createOrder(req2);

        assertNotNull(ord1.id());
        assertNotNull(ord2.id());
        assertNotEquals(ord1.id(), ord2.id());

        OrderResponse found1 = service.findById(ord1.id());
        assertEquals("CUS-01", found1.customerId());
        assertEquals(100000, found1.total());

        OrderResponse found2 = service.findById(ord2.id());
        assertEquals("CUS-02", found2.customerId());
        assertEquals(250000, found2.total());
    }
}
