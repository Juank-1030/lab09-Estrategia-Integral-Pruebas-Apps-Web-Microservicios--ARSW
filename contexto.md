# ARSW | Estrategia Integral de Pruebas
## Guía de Laboratorio
## Estrategia Integral de Pruebas para Aplicaciones Web y Microservicios

---

| **Campo** | **Detalle** |
|---|---|
| Asignatura | Arquitecturas de Software - ARSW |
| Duración sugerida | 3 horas |
| Modalidad | Individual o parejas |
| Nivel | Intermedio |
| Tecnologías | Spring Boot, JUnit, Mockito, MockMvc, Testcontainers, Playwright, k6, GitHub Actions |
| Enfoque | Calidad, confiabilidad, automatización y validación por capas |

> **Idea central:** Probar no es solamente verificar que una funcionalidad responde. Probar es construir evidencia de calidad sobre comportamiento, integración, rendimiento, experiencia de usuario y confiabilidad del sistema.

---

## Contenido

1. Propósito del laboratorio
2. Resultados de aprendizaje
3. Estrategia de pruebas por capas
4. Proyecto base Spring Boot
5. Pruebas unitarias con JUnit y Mockito
6. Pruebas de API con MockMvc
7. Pruebas de integración con Spring Boot y Testcontainers
8. Pruebas automáticas de frontend con Playwright
9. Pruebas de carga con k6
10. Estrategia de pruebas en CI/CD
11. Actividades, reto final y rúbrica
12. Cierre del laboratorio

---

## 1. Propósito del laboratorio

En una arquitectura moderna no basta con que una aplicación funcione de forma manual en el equipo del desarrollador. Un sistema debe demostrar, mediante evidencia repetible, que sus componentes cumplen el comportamiento esperado, que sus servicios se integran correctamente, que sus APIs responden de forma consistente, que el frontend ejecuta los flujos críticos sin errores y que la aplicación soporta cierto nivel de carga.

Este laboratorio propone una estrategia integral de pruebas para aplicaciones web y microservicios. La guía combina pruebas unitarias, pruebas de integración, pruebas de API, pruebas end-to-end de frontend y pruebas de carga. Además, conecta las pruebas con una estrategia de validación dentro de un pipeline de integración continua.

> **Relación con arquitectura:** Una buena estrategia de pruebas protege atributos de calidad como confiabilidad, mantenibilidad, rendimiento, seguridad, disponibilidad y capacidad de evolución.

---

## 2. Resultados de aprendizaje

* Diferenciar pruebas unitarias, de integración, de API, end-to-end, contrato y carga.
* Diseñar una pirámide de pruebas coherente para una aplicación web.
* Implementar pruebas unitarias con JUnit y Mockito.
* Validar endpoints REST con MockMvc.
* Ejecutar pruebas de integración con Spring Boot.
* Comprender el uso de Testcontainers para dependencias reales en pruebas.
* Automatizar flujos de frontend con Playwright.
* Diseñar y ejecutar pruebas de carga con k6.
* Interpretar métricas básicas de rendimiento: latencia, errores, p95 y throughput.
* Proponer una estrategia de pruebas para CI/CD.

---

## 3. Estrategia de pruebas por capas

La estrategia del laboratorio se organiza en capas. Cada capa tiene un propósito diferente, un costo diferente y un nivel distinto de confianza.

```
Pruebas unitarias
   ↓
Pruebas de API
   ↓
Pruebas de integración
   ↓
Pruebas end-to-end de frontend
   ↓
Pruebas de carga
   ↓
Pipeline de validación
```

| **Tipo de prueba** | **Qué valida** | **Herramientas sugeridas** |
|---|---|---|
| Unitaria | Lógica de una clase o función aislada. | JUnit, Mockito |
| API | Códigos HTTP, JSON, validaciones, contrato básico de endpoint. | MockMvc, REST Assured |
| Integración | Interacción entre servicio, repositorio y base de datos. | SpringBootTest, Testcontainers |
| Frontend automática | Flujos críticos desde la perspectiva del usuario. | Playwright, Cypress, Testing Library |
| Carga | Comportamiento bajo múltiples usuarios o solicitudes concurrentes. | k6, JMeter, Gatling |
| Pipeline | Ejecución repetible para evitar regresiones. | GitHub Actions, GitLab CI, Jenkins |

> **Recomendación:** No todas las pruebas deben ejecutarse en cada commit. Las pruebas rápidas deben ejecutarse con frecuencia; las pruebas más costosas deben reservarse para pull requests, releases o ambientes controlados.

---

## 4. Proyecto base Spring Boot

El laboratorio usará una API simple de pedidos. El objetivo no es construir una aplicación completa de comercio electrónico, sino contar con una base suficiente para aplicar diferentes tipos de pruebas.

```
Cliente / Frontend
   ↓
Order API - Spring Boot
   ↓
Order Service
   ↓
Order Repository
   ↓
Base de datos
```

### 4.1 Crear proyecto

* Java 17 o superior.
* Maven.
* Spring Web.
* Spring Data JPA.
* Validation.
* H2 Database.
* Spring Boot Test.

```
src/main/java/edu/eci/arsw/testing
├── TestingApplication.java
├── controller
│   └── OrderController.java
├── service
│   └── OrderService.java
├── repository
│   └── OrderRepository.java
├── model
│   └── Order.java
└── dto
    ├── CreateOrderRequest.java
    └── OrderResponse.java

src/test/java/edu/eci/arsw/testing
├── service
│   └── OrderServiceTest.java
├── controller
│   └── OrderControllerTest.java
└── integration
    └── OrderIntegrationTest.java
```

### 4.2 Entidad Order

```java
package edu.eci.arsw.testing.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    private String id;

    private String customerId;
    private double total;
    private String status;
    private Instant createdAt;

    protected Order() {}

    public Order(String id, String customerId, double total, String status, Instant createdAt) {
        this.id = id;
        this.customerId = customerId;
        this.total = total;
        this.status = status;
        this.createdAt = createdAt;
    }

    public String getId() { return id; }
    public String getCustomerId() { return customerId; }
    public double getTotal() { return total; }
    public String getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
}
```

### 4.3 DTO de creación

```java
package edu.eci.arsw.testing.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record CreateOrderRequest(
        @NotBlank String customerId,
        @Min(1) double total
) {}
```

### 4.4 DTO de respuesta

```java
package edu.eci.arsw.testing.dto;

import java.time.Instant;

public record OrderResponse(
        String id,
        String customerId,
        double total,
        String status,
        Instant createdAt
) {}
```

### 4.5 Repositorio

```java
package edu.eci.arsw.testing.repository;

import edu.eci.arsw.testing.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, String> {
}
```

### 4.6 Servicio

```java
package edu.eci.arsw.testing.service;

import edu.eci.arsw.testing.dto.CreateOrderRequest;
import edu.eci.arsw.testing.dto.OrderResponse;
import edu.eci.arsw.testing.model.Order;
import edu.eci.arsw.testing.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class OrderService {

    private final OrderRepository repository;

    public OrderService(OrderRepository repository) {
        this.repository = repository;
    }

    public OrderResponse createOrder(CreateOrderRequest request) {
        if (request.total() > 5_000_000) {
            throw new IllegalArgumentException("El pedido supera el valor máximo permitido");
        }

        Order order = new Order(
                "ORD-" + UUID.randomUUID(),
                request.customerId(),
                request.total(),
                "CREATED",
                Instant.now()
        );

        Order saved = repository.save(order);

        return new OrderResponse(
                saved.getId(), saved.getCustomerId(), saved.getTotal(),
                saved.getStatus(), saved.getCreatedAt()
        );
    }

    public OrderResponse findById(String id) {
        Order order = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado"));

        return new OrderResponse(
                order.getId(), order.getCustomerId(), order.getTotal(),
                order.getStatus(), order.getCreatedAt()
        );
    }
}
```

### 4.7 Controlador REST

```java
package edu.eci.arsw.testing.controller;

import edu.eci.arsw.testing.dto.CreateOrderRequest;
import edu.eci.arsw.testing.dto.OrderResponse;
import edu.eci.arsw.testing.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService service;

    public OrderController(OrderService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse create(@Valid @RequestBody CreateOrderRequest request) {
        return service.createOrder(request);
    }

    @GetMapping("/{id}")
    public OrderResponse findById(@PathVariable String id) {
        return service.findById(id);
    }
}
```

---

## 5. Pruebas unitarias con JUnit y Mockito

Las pruebas unitarias validan una unidad pequeña de código, normalmente una clase o método, aislando sus dependencias. En este caso se probará OrderService simulando OrderRepository.

| **Elemento** | **Descripción** |
|---|---|
| Clase bajo prueba | OrderService |
| Dependencia simulada | OrderRepository |
| Herramientas | JUnit 5 y Mockito |
| Objetivo | Validar reglas de negocio sin levantar toda la aplicación |

```java
package edu.eci.arsw.testing.service;

import edu.eci.arsw.testing.dto.CreateOrderRequest;
import edu.eci.arsw.testing.dto.OrderResponse;
import edu.eci.arsw.testing.model.Order;
import edu.eci.arsw.testing.repository.OrderRepository;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class OrderServiceTest {

    @Test
    void shouldCreateOrderWhenRequestIsValid() {
        OrderRepository repository = mock(OrderRepository.class);
        OrderService service = new OrderService(repository);

        Order savedOrder = new Order("ORD-1", "CUS-01", 120000, "CREATED", Instant.now());
        when(repository.save(any(Order.class))).thenReturn(savedOrder);

        CreateOrderRequest request = new CreateOrderRequest("CUS-01", 120000);
        OrderResponse response = service.createOrder(request);

        assertNotNull(response);
        assertEquals("ORD-1", response.id());
        assertEquals("CUS-01", response.customerId());
        assertEquals(120000, response.total());
        assertEquals("CREATED", response.status());
        verify(repository, times(1)).save(any(Order.class));
    }

    @Test
    void shouldRejectOrderWhenTotalExceedsLimit() {
        OrderRepository repository = mock(OrderRepository.class);
        OrderService service = new OrderService(repository);

        CreateOrderRequest request = new CreateOrderRequest("CUS-01", 6000000);

        assertThrows(IllegalArgumentException.class, () -> service.createOrder(request));
        verify(repository, never()).save(any(Order.class));
    }
}
```

> **Actividad 1:** Agregue una prueba unitaria para validar que findById retorna un pedido existente y otra para validar que lanza excepción cuando el pedido no existe.

---

## 6. Pruebas de API con MockMvc

Las pruebas de API verifican que los endpoints REST respondan correctamente. Validan códigos HTTP, cuerpo JSON, serialización, validaciones y comportamiento de la capa web.

```java
package edu.eci.arsw.testing.controller;

import edu.eci.arsw.testing.dto.OrderResponse;
import edu.eci.arsw.testing.service.OrderService;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService service;

    @Test
    void shouldCreateOrder() throws Exception {
        when(service.createOrder(any())).thenReturn(
            new OrderResponse("ORD-1", "CUS-01", 120000, "CREATED", Instant.now())
        );

        mockMvc.perform(post("/orders")
                        .contentType(APPLICATION_JSON)
                        .content("""
                            {
                                "customerId": "CUS-01",
                                "total": 120000
                            }
                            """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("ORD-1"))
                .andExpect(jsonPath("$.customerId").value("CUS-01"))
                .andExpect(jsonPath("$.status").value("CREATED"));
    }

    @Test
    void shouldRejectInvalidRequest() throws Exception {
        mockMvc.perform(post("/orders")
                        .contentType(APPLICATION_JSON)
                        .content("""
                            {
                                "customerId": "",
                                "total": -10
                            }
                            """))
                .andExpect(status().isBadRequest());
    }
}
```

> **Actividad 2:** Agregue una prueba para GET /orders/{id}. Debe validar HTTP 200, id del pedido, customerId y status.

---

## 7. Pruebas de integración con Spring Boot y Testcontainers

Las pruebas de integración verifican que varios componentes trabajen juntos. Una prueba de integración puede levantar el contexto de Spring y validar la interacción entre servicio, repositorio y base de datos.

```java
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
}
```

### 7.1 Extensión con Testcontainers

Testcontainers permite levantar una base de datos real en Docker durante la prueba. Esto reduce diferencias entre el ambiente de pruebas y un entorno productivo. Para usar PostgreSQL con Testcontainers, agregue dependencias de testcontainers y postgres en el alcance de pruebas.

```xml
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>junit-jupiter</artifactId>
    <scope>test</scope>
</dependency>

<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>postgresql</artifactId>
    <scope>test</scope>
</dependency>
```

> **Actividad 3:** Explique la diferencia entre una prueba unitaria del servicio, una prueba del controlador con MockMvc y una prueba de integración con SpringBootTest. Analice rapidez, confianza y costo de mantenimiento.

---

## 8. Pruebas automáticas de frontend con Playwright

Las pruebas frontend automáticas validan flujos completos desde la perspectiva del usuario. Para este laboratorio se recomienda Playwright, porque permite ejecutar pruebas end-to-end sobre navegadores reales y automatizar interacciones de usuario.

* **Vitest o Jest:** pruebas unitarias de componentes.
* **Testing Library:** validación de interacción con componentes.
* **Playwright:** pruebas end-to-end en navegador real.
* **Cypress:** alternativa popular para pruebas E2E.

### 8.1 Crear proyecto Playwright

```bash
mkdir frontend-tests
cd frontend-tests
npm init playwright@latest
```

### 8.2 Prueba E2E básica

```javascript
import { test, expect } from '@playwright/test';

test('usuario puede consultar la página principal', async ({ page }) => {
  await page.goto('http://localhost:5173');
  await expect(page).toHaveTitle(/Orders|Pedidos|App/);
});

test('usuario puede crear un pedido', async ({ page }) => {
  await page.goto('http://localhost:5173');

  await page.fill('[data-testid="customer-id"]', 'CUS-01');
  await page.fill('[data-testid="order-total"]', '120000');
  await page.click('[data-testid="create-order"]');

  await expect(page.locator('[data-testid="order-status"]')).toContainText('CREATED');
});
```

### 8.3 Recomendación para frontend

Para que las pruebas sean estables, los componentes del frontend deben incluir identificadores de prueba. Esto evita depender de textos, estilos o posiciones visuales que cambian frecuentemente.

```html
<input data-testid="customer-id" />
<input data-testid="order-total" />
<button data-testid="create-order">Crear pedido</button>
<div data-testid="order-status"></div>
```

```bash
npx playwright test
npx playwright show-report
```

> **Actividad 4:** Diseñe tres pruebas E2E: crear pedido exitosamente, mostrar error si el total es inválido y consultar un pedido por ID. Para cada una indique flujo, datos de entrada y resultado esperado.

---

## 9. Pruebas de carga con k6

Las pruebas de carga permiten evaluar cómo se comporta el sistema bajo múltiples usuarios o solicitudes concurrentes. Validan latencia, throughput, errores, capacidad y degradación bajo carga.

### 9.1 Instalación

```bash
# macOS
brew install k6

# Windows
winget install k6

# Docker
docker run --rm -i grafana/k6 run - < load-test.js
```

### 9.2 Script de carga básico

```javascript
import http from 'k6/http';
import { sleep, check } from 'k6';

export const options = {
  vus: 10,
  duration: '30s',
};

export default function () {
  const response = http.get('http://localhost:8080/orders/ORD-1');

  check(response, {
    'status is 200': (r) => r.status === 200,
    'response time < 500ms': (r) => r.timings.duration < 500,
  });

  sleep(1);
}
```

```bash
k6 run load-test.js
```

### 9.3 Script de carga con stages y thresholds

```javascript
import http from 'k6/http';
import { sleep, check } from 'k6';

export const options = {
  stages: [
    { duration: '20s', target: 10 },
    { duration: '30s', target: 30 },
    { duration: '20s', target: 0 },
  ],
  thresholds: {
    http_req_failed: ['rate<0.05'],
    http_req_duration: ['p(95)<800'],
  },
};

export default function () {
  const payload = JSON.stringify({
    customerId: `CUS-${__VU}`,
    total: 120000,
  });

  const params = {
    headers: { 'Content-Type': 'application/json' },
  };

  const response = http.post('http://localhost:8080/orders', payload, params);

  check(response, {
    'created': (r) => r.status === 201,
    'duration < 800ms': (r) => r.timings.duration < 800,
  });

  sleep(1);
}
```

| **Métrica k6** | **Interpretación** |
|---|---|
| http_req_duration | Tiempo de respuesta de las solicitudes. |
| http_req_failed | Porcentaje de solicitudes fallidas. |
| http_reqs | Número total de solicitudes. |
| vus | Usuarios virtuales activos. |
| iterations | Cantidad de iteraciones ejecutadas. |
| checks | Validaciones ejecutadas y su resultado. |
| p(95) | Percentil 95 de latencia: el 95% de las solicitudes respondió por debajo de ese valor. |

> **Actividad 5:** Ejecute una prueba de carga con k6 y documente usuarios virtuales, duración, total de solicitudes, porcentaje de fallos, p95 de latencia, resultado de thresholds y conclusión técnica.

---

## 10. Estrategia de pruebas en CI/CD

Una estrategia de pruebas debe integrarse al ciclo de desarrollo. No todas las pruebas se ejecutan en el mismo momento. Las pruebas rápidas pueden ejecutarse en cada commit, mientras que las pruebas de carga o E2E completas pueden reservarse para pull requests, releases o ambientes controlados.

```
Cada commit:
  - Compilación
  - Pruebas unitarias
  - Pruebas de API rápidas

Pull request:
  - Unitarias
  - Integración
  - API
  - Frontend E2E principales

Antes de release:
  - Integración completa
  - E2E completo
  - Prueba de carga controlada
  - Reportes y evidencia
```

### 10.1 Ejemplo de GitHub Actions

```yaml
name: ARSW Testing Pipeline

on:
  push:
    branches: [ main ]
  pull_request:

jobs:
  backend-tests:
    runs-on: ubuntu-latest

    steps:
      - uses: actions/checkout@v4

      - name: Setup Java
        uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: 17

      - name: Run backend tests
        run: mvn test
```

---

## 11. Actividades, reto final y rúbrica

### 11.1 Actividad integradora

Diseñe una estrategia de pruebas para una aplicación de comercio electrónico con frontend React, backend Spring Boot, base de datos PostgreSQL, API REST, autenticación y despliegue en AWS.

* Tipos de pruebas que aplicaría.
* Herramientas seleccionadas.
* Capa que valida cada prueba.
* Momento de ejecución en el pipeline.
* Errores que podría detectar.
* Evidencia que genera.

### 11.2 Reto final

13. Implementar una prueba unitaria funcional.
14. Implementar una prueba de API con MockMvc.
15. Implementar una prueba de integración.
16. Proponer o implementar una prueba E2E de frontend.
17. Crear un script k6 de carga.
18. Ejecutar las pruebas y anexar evidencia.
19. Analizar métricas de carga.
20. Proponer un pipeline de pruebas.
21. Reflexionar sobre qué pruebas aportan más valor al sistema.

### Rúbrica

| **Criterio** | **Descripción** | **Peso** |
|---|---|---|
| Pruebas unitarias | Valida lógica de negocio con aislamiento. | 15% |
| Pruebas de API | Verifica endpoints, estados HTTP y JSON. | 15% |
| Pruebas de integración | Valida interacción entre capas. | 20% |
| Pruebas frontend | Propone o implementa flujo E2E automatizado. | 15% |
| Pruebas de carga | Diseña y ejecuta prueba con k6. | 20% |
| Análisis técnico | Interpreta resultados y propone mejoras. | 15% |

---

## 12. Cierre del laboratorio

Las pruebas son un mecanismo fundamental para asegurar calidad en una arquitectura de software. No todas las pruebas cumplen la misma función ni tienen el mismo costo. Una estrategia equilibrada combina pruebas unitarias rápidas, pruebas de integración confiables, pruebas de API orientadas a contrato, pruebas E2E para flujos críticos, pruebas de carga para validar comportamiento bajo demanda y pruebas en pipeline para evitar regresiones.

> **Aprendizaje central:** Probar no significa únicamente ver si funciona. Probar significa construir evidencia de calidad sobre comportamiento, estabilidad, rendimiento y confiabilidad del sistema.

---

*Guía de laboratorio - Pruebas de software, integración, frontend y carga*