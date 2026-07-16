# Bitácora — Estrategia Integral de Pruebas para Aplicaciones Web y Microservicios

**Asignatura:** Arquitecturas de Software - ARSW  
**Tecnologías:** Spring Boot, JUnit, Mockito, MockMvc, Testcontainers, Playwright, k6, GitHub Actions  
**Duración:** 3 horas | **Modalidad:** Individual

---

## Índice

1. [Propósito del laboratorio](#1-propósito-del-laboratorio)
2. [Estructura del proyecto](#2-estructura-del-proyecto)
3. [Entidad Order y DTOs](#3-entidad-order-y-dtos)
4. [Repositorio](#4-repositorio)
5. [Capa de servicio — OrderService](#5-capa-de-servicio--orderservice)
6. [Capa de controlador — OrderController](#6-capa-de-controlador--ordercontroller)
7. [Pruebas unitarias con JUnit y Mockito](#7-pruebas-unitarias-con-junit-y-mockito)
8. [Pruebas de API con MockMvc](#8-pruebas-de-api-con-mockmvc)
9. [Pruebas de integración con SpringBootTest y H2](#9-pruebas-de-integración-con-springboottest-y-h2)
10. [Pruebas de integración con Testcontainers y PostgreSQL](#10-pruebas-de-integración-con-testcontainers-y-postgresql)
11. [Pruebas E2E de frontend con Playwright](#11-pruebas-e2e-de-frontend-con-playwright)
12. [Pruebas de carga con k6](#12-pruebas-de-carga-con-k6)
13. [Pipeline CI/CD con GitHub Actions](#13-pipeline-cicd-con-github-actions)
14. [Resultados de ejecución de pruebas](#14-resultados-de-ejecución-de-pruebas)
15. [Análisis técnico y conclusiones](#15-análisis-técnico-y-conclusiones)

---

## 1. Propósito del laboratorio

Construir una estrategia integral de pruebas para una API REST de pedidos (Orders). Se aplicaron pruebas en múltiples capas: unitarias, de API, de integración, end-to-end y de carga, conectadas mediante un pipeline de integración continua.

---

## 2. Estructura del proyecto

```
lab09-Estrategia-Integral-Pruebas-Apps-Web-Microservicios--ARSW/
├── pom.xml
├── README.md
├── images/
│   ├── Terminal_1_spring_boot_run.png
│   ├── Terminal_2_http-server.png
│   ├── Terminal_3_test-report.png
│   ├── Test_crear_pedido_exitosamente.png
│   ├── Test_error_total_invalido.png
│   └── Test_consultar_pedido_por_ID.png
├── src/main/java/edu/eci/arsw/testing/
│   ├── TestingApplication.java
│   ├── config/WebConfig.java
│   ├── model/Order.java
│   ├── dto/CreateOrderRequest.java
│   ├── dto/OrderResponse.java
│   ├── repository/OrderRepository.java
│   ├── service/OrderService.java
│   └── controller/OrderController.java
├── src/test/java/edu/eci/arsw/testing/
│   ├── service/OrderServiceTest.java
│   ├── controller/OrderControllerTest.java
│   └── integration/
│       ├── OrderIntegrationTest.java
│       └── OrderIntegrationTestcontainersTest.java
├── frontend/index.html
├── frontend-tests/
│   ├── package.json
│   ├── playwright.config.js
│   └── tests/orders.spec.js
├── load-tests/
│   ├── load-test.js
│   └── simple-test.js
└── .github/workflows/testing-pipeline.yml
```

---

## 3. Entidad Order y DTOs

**Archivos:**
- `src/main/java/edu/eci/arsw/testing/model/Order.java`
- `src/main/java/edu/eci/arsw/testing/dto/CreateOrderRequest.java`
- `src/main/java/edu/eci/arsw/testing/dto/OrderResponse.java`

La entidad `Order` se mapea a la tabla `orders` con JPA. Tiene los campos: `id`, `customerId`, `total`, `status`, `createdAt`.

`CreateOrderRequest` es un record con validaciones: `@NotBlank` en customerId y `@Min(1)` en total.

`OrderResponse` es un record inmutable devuelto por la API.

---

## 4. Repositorio

**Archivo:** `src/main/java/edu/eci/arsw/testing/repository/OrderRepository.java`

Extiende `JpaRepository<Order, String>` — proporciona operaciones CRUD básicas sin implementación adicional.

---

## 5. Capa de servicio — OrderService

**Archivo:** `src/main/java/edu/eci/arsw/testing/service/OrderService.java`

Reglas de negocio implementadas:
- `createOrder`: valida que el total no exceda $5.000.000. Si supera el límite, lanza `IllegalArgumentException`.
- `findById`: busca por ID; lanza `IllegalArgumentException` si no existe.

---

## 6. Capa de controlador — OrderController

**Archivo:** `src/main/java/edu/eci/arsw/testing/controller/OrderController.java`

Endpoints REST:
- `POST /orders` — crea un pedido, responde con `201 Created`
- `GET /orders/{id}` — consulta un pedido por ID, responde con `200 OK`

**Configuración CORS:** `WebConfig.java` — permite peticiones desde cualquier origen para que el frontend de prueba pueda consumir la API.

---

## 7. Pruebas unitarias con JUnit y Mockito

**Archivo:** `src/test/java/edu/eci/arsw/testing/service/OrderServiceTest.java`

**4 pruebas implementadas:**

| Prueba | Descripción | Resultado |
|---|---|---|
| `shouldCreateOrderWhenRequestIsValid` | Crea un pedido válido y verifica campos | ✅ |
| `shouldRejectOrderWhenTotalExceedsLimit` | Rechaza pedido con total > $5.000.000 | ✅ |
| `shouldReturnOrderWhenFindByIdExists` | Busca un pedido existente por ID | ✅ |
| `shouldThrowExceptionWhenFindByIdNotFound` | Lanza excepción cuando el ID no existe | ✅ |

Se usó `mock(OrderRepository.class)` para aislar el servicio de la base de datos.

---

## 8. Pruebas de API con MockMvc

**Archivo:** `src/test/java/edu/eci/arsw/testing/controller/OrderControllerTest.java`

**3 pruebas implementadas:**

| Prueba | Descripción | Resultado |
|---|---|---|
| `shouldCreateOrder` | POST /orders con datos válidos → 201 + JSON | ✅ |
| `shouldRejectInvalidRequest` | POST /orders con datos inválidos → 400 | ✅ |
| `shouldGetOrderById` | GET /orders/{id} → 200 + JSON | ✅ |

Se usó `@WebMvcTest(OrderController.class)` con `@MockBean` para el servicio.

---

## 9. Pruebas de integración con SpringBootTest y H2

**Archivo:** `src/test/java/edu/eci/arsw/testing/integration/OrderIntegrationTest.java`

**2 pruebas implementadas:**

| Prueba | Descripción | Resultado |
|---|---|---|
| `shouldCreateAndFindOrder` | Crea y recupera un pedido con H2 real | ✅ |
| `shouldCreateMultipleOrdersAndFindEach` | Crea 2 pedidos y verifica independencia | ✅ |

Usa `@SpringBootTest` con H2 en memoria — prueba real de servicio + repositorio + BD.

---

## 10. Pruebas de integración con Testcontainers y PostgreSQL

**Archivo:** `src/test/java/edu/eci/arsw/testing/integration/OrderIntegrationTestcontainersTest.java`

**2 pruebas diseñadas:**

| Prueba | Descripción | Resultado |
|---|---|---|
| `shouldCreateAndFindOrderWithRealDatabase` | Crea y recupera pedido con PostgreSQL vía Docker | ⚠️ Requiere Docker |
| `shouldThrowExceptionWhenOrderNotFound` | Verifica excepción para ID inexistente | ⚠️ Requiere Docker |

Usa `@Testcontainers` con PostgreSQL 15. Requiere Docker Desktop ejecutándose.

---

## 11. Pruebas E2E de frontend con Playwright

### Frontend de prueba

Se creó un frontend HTML mínimo en `frontend/index.html` con:
- Formulario para crear pedidos (POST a `/orders`)
- Sección para consultar pedidos por ID (GET a `/orders/{id}`)
- Elementos con `data-testid` para selectores estables
- CORS habilitado en el backend (`WebConfig.java`)

### 11.1 Diseño de 3 pruebas E2E

**Archivo:** `frontend-tests/tests/orders.spec.js`

#### Prueba 1: Crear pedido exitosamente

| Aspecto | Detalle |
|---|---|
| **Flujo** | 1. Navegar a `/`. 2. Escribir "CUS-01" en `[data-testid="customer-id"]`. 3. Escribir "120000" en `[data-testid="order-total"]`. 4. Clic en `[data-testid="create-order"]`. |
| **Datos** | customerId = "CUS-01", total = 120000 |
| **Resultado** | `[data-testid="order-status"]` contiene "CREATED". `[data-testid="order-id"]` visible. |

#### Prueba 2: Mostrar error si el total es inválido

| Aspecto | Detalle |
|---|---|
| **Flujo** | 1. Navegar a `/`. 2. Escribir "CUS-01" en customer-id. 3. Escribir "-10" en order-total. 4. Clic en create-order. |
| **Datos** | customerId = "CUS-01", total = -10 |
| **Resultado** | `[data-testid="error-message"]` visible (backend responde 400 por violar `@Min(1)`). |

#### Prueba 3: Consultar un pedido por ID

| Aspecto | Detalle |
|---|---|
| **Flujo** | 1. Crear un pedido. 2. Obtener su ID. 3. Escribir el ID en search-order-id. 4. Clic en search-order. |
| **Datos** | customerId = "CUS-01", total = 50000 (ID generado por backend) |
| **Resultado** | `[data-testid="order-detail"]` visible con info del pedido. |

### 11.2 Evidencia de ejecución

#### Paso 1: Backend Spring Boot

Terminal ejecutando `mvn spring-boot:run` — API en `http://localhost:8080`:

![Terminal 1 - Spring Boot run](images/Terminal_1_spring_boot_run.png)

*La terminal muestra Spring Boot iniciado con perfil default, H2 en memoria, servidor en puerto 8080.*

#### Paso 2: Servir frontend HTML

Terminal ejecutando `npx http-server frontend -p 3000`:

![Terminal 2 - HTTP Server](images/Terminal_2_http-server.png)

*http-server sirviendo el frontend estático en `http://localhost:3000`.*

#### Paso 3: Ejecutar Playwright tests

Terminal ejecutando `npm test` dentro de `frontend-tests/`:

![Terminal 3 - Test Report](images/Terminal_3_test-report.png)

*Resultado: 3 tests ejecutados, 3 pasaron. 2 pasaron (crear, error) y 1 requirió ajuste (consulta por ID).*

#### Test 1: Crear pedido exitosamente

![Test - Crear pedido](images/Test_usuario%20puede%20crear%20un%20pedido%20exitosamente.png)

*Captura del reporte HTML de Playwright mostrando el test de creación de pedido pasando correctamente, validando order-status = "CREATED" y order-id visible.*

#### Test 2: Error si el total es inválido

![Test - Error total inválido](images/Test_usuario%20ve%20error%20si%20el%20total%20es%20inv%C3%A1lido.png)

*Captura del reporte HTML mostrando el test que verifica que al enviar un total negativo (-10) se muestra el mensaje de error.*

#### Test 3: Consultar pedido por ID

![Test - Consultar pedido](images/Test_usuario%20puede%20consultar%20un%20pedido%20por%20ID.png)

*Captura del reporte HTML mostrando el test que crea un pedido, obtiene su ID real, lo busca y verifica que el detalle del pedido sea visible.*

---

## 12. Pruebas de carga con k6

**Scripts en `load-tests/`:**

### Script básico (`simple-test.js`)
- 10 VUs, duración 30s
- Consulta GET /orders/ORD-1
- Checks: status 200, tiempo < 500ms

### Script con stages y thresholds (`load-test.js`)
- Stages: 20s → 10 VUs, 30s → 30 VUs, 20s → 0 VUs
- Thresholds: `http_req_failed < 5%`, `p(95) < 800ms`
- Realiza POST + GET en cada iteración

**Ejecución:**
```bash
# Terminal 1: Iniciar backend
mvn spring-boot:run

# Terminal 2: Ejecutar k6
k6 run load-tests/load-test.js
```

**Métricas a interpretar:**
| Métrica | Interpretación |
|---|---|
| http_req_duration | Tiempo de respuesta promedio |
| p(95) | El 95% respondió por debajo de este valor |
| http_req_failed | Porcentaje de errores |
| throughput | Solicitudes por segundo |
| checks | Validaciones pasadas/fallidas |

---

## 13. Pipeline CI/CD con GitHub Actions

**Archivo:** `.github/workflows/testing-pipeline.yml`

**Jobs definidos:**

| Job | Trigger | Pasos |
|---|---|---|
| `backend-tests` | push a main + PRs | Java 17, `mvn test`, sube reportes |
| `frontend-tests` | después de backend-tests | Node 20, Playwright, reportes |

**Estrategia de ejecución sugerida:**
- **Cada commit:** compilación + unitarias + API rápidas
- **Pull request:** unitarias + integración + API + frontend E2E
- **Antes de release:** integración completa + E2E completo + carga controlada

---

## 14. Resultados de ejecución de pruebas

```
-------------------------------------------------------
T E S T S
-------------------------------------------------------
OrderServiceTest .............................. 4/4 ✅
OrderControllerTest ........................... 3/3 ✅
OrderIntegrationTest .......................... 2/2 ✅
OrderIntegrationTestcontainersTest ........... 0/1 ❌ (requiere Docker)
Playwright E2E ................................ 3/3 ✅
-------------------------------------------------------
Total backend: 9/10 pasaron · 0 fallos · 1 error (Docker)
Total E2E:     3/3 pasaron
```

---

## 15. Análisis técnico y conclusiones

### ¿Qué pruebas aportan más valor al sistema?

1. **Pruebas unitarias (rápidas, bajo costo):** Atrapan errores de lógica de negocio en milisegundos. Deberían ejecutarse en cada commit.
2. **Pruebas de API con MockMvc (costo medio):** Validan contrato REST sin levantar toda la app. Útiles para detectar cambios en serialización o validaciones.
3. **Pruebas de integración (mayor confianza):** Verifican que servicio, repositorio y BD funcionan juntos. Más lentas pero esenciales antes de un PR.
4. **Pruebas E2E (alto costo):** Validan flujos críticos del usuario. Reservar para PRs y releases.
5. **Pruebas de carga (alto valor estratégico):** Detectan degradación de rendimiento, memory leaks y cuellos de botella. Ejecutar antes de releases.

### Pirámide de pruebas

```
        ╱╲
       ╱ E2E ╲            ← pocas, costosas
      ╱────────╲
     ╱  Carga   ╲          ← bajo demanda
    ╱────────────╲
   ╱ Integración  ╲        ← varias, confiables
  ╱────────────────╲
 ╱  Unitarias + API ╲      ← muchas, rápidas
╱────────────────────╲
```

### Conclusión

Probar no es solo verificar que una funcionalidad responde. Probar es construir evidencia de calidad sobre comportamiento, integración, rendimiento, experiencia de usuario y confiabilidad del sistema. Una estrategia equilibrada combina pruebas rápidas en cada commit, pruebas de integración en PRs y pruebas de carga/E2E en releases.
