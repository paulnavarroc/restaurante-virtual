# 🔥 Sabores Perú – Restaurante Virtual de Comida Regional Peruana

Aplicación web para la gestión de un restaurante virtual (Dark Kitchen) de comida regional peruana (Costa, Sierra y Selva).

Incluye módulos para **Cliente**, **Cocina**, **Repartidor** y **Administrador**, con soporte **multi-tenant**, autenticación por roles, carrito de compras, seguimiento de pedidos y reportes en Excel.

---

## 📋 Características principales

- Catálogo de platos por región (Costa, Sierra, Selva)
- Carrito de compras con sesión
- Gestión completa del ciclo de pedidos
- Roles: CLIENTE, CHEF, REPARTIDOR, ADMINISTRADOR
- Multi-tenant (múltiples bases de datos / sedes)
- Reportes de ventas y productos más vendidos (Apache POI)
- Seguridad con Spring Security + BCrypt
- Arquitectura MVC + Services + Repositories (SOLID)

---

## 🛠 Stack tecnológico

| Capa | Tecnología |
|------|------------|
| Backend | Java 21/25 + Spring Boot 3.x |
| Web | Spring MVC + Thymeleaf |
| Persistencia | Spring Data JPA + SQL Server |
| Seguridad | Spring Security |
| Reportes | Apache POI |
| Build | Maven |
| Otros | Lombok, Validation, HikariCP |

---

## 📁 Estructura del proyecto

```
com.saboresperu.restaurante
├── config/          # Multi-tenant, Security, DataInitializer
├── tenant/          # TenantContext, RoutingDataSource, Interceptor
├── entity/          # Entidades JPA
├── repository/      # Spring Data Repositories
├── dto/             # Data Transfer Objects
├── service/
│   ├── interfaces/
│   └── impl/
├── controller/      # Auth, Cliente, Cocina, Repartidor, Admin
├── security/        # UserDetailsService, SecurityConfig
├── exception/       # Excepciones + GlobalExceptionHandler
└── util/            # SecurityUtils
```

---

## ⚙️ Requisitos

- JDK 21 o 25
- Maven 3.9+
- SQL Server (local o remoto)
- IntelliJ IDEA / VS Code / Eclipse

---

## 🚀 Configuración e instalación

### 1. Crear las bases de datos

Ejecuta el script SQL (`restaurante_comida.sql`) en cada base:

- `RestauranteLima`
- `RestauranteArequipa` (opcional)
- `RestauranteCusco` (opcional)

### 2. Configurar `application.yml`

```yaml
tenants:
  default: lima
  configs:
    lima:
      url: jdbc:sqlserver://localhost:1433;databaseName=RestauranteLima;encrypt=true;trustServerCertificate=true
      username: sa
      password: ${DB_PASSWORD_LIMA:TuPassword}
      driver-class-name: com.microsoft.sqlserver.jdbc.SQLServerDriver
```

### 3. Ejecutar la aplicación

```bash
mvn spring-boot:run
```

O desde IntelliJ: ejecutar `RestauranteVirtualApplication`.

La aplicación estará en: **http://localhost:8080**

---

## 👤 Usuarios de prueba

Al arrancar por primera vez se cargan datos de prueba automáticamente.

| Correo | Password | Rol |
|--------|----------|-----|
| admin@saboresperu.com | 123456 | ADMINISTRADOR |
| cocina@saboresperu.com | 123456 | CHEF |
| repartidor@saboresperu.com | 123456 | REPARTIDOR |
| cliente@saboresperu.com | 123456 | CLIENTE |

---

## 🔄 Flujo de un pedido

```
1. Cliente navega el catálogo y agrega productos al carrito
2. Checkout → elige dirección y método de pago
3. Pedido creado con estado PENDIENTE
4. Cocina: PENDIENTE → EN_PREPARACION → LISTO
5. Repartidor: LISTO → EN_CAMINO → ENTREGADO
6. Cliente ve el seguimiento en tiempo real
```

---

## 🔐 Roles y rutas

| Rol | Rutas principales |
|-----|-------------------|
| CLIENTE | `/cliente/catalogo`, `/cliente/carrito`, `/cliente/pedidos` |
| CHEF | `/cocina/pedidos` |
| REPARTIDOR | `/repartidor/entregas` |
| ADMINISTRADOR | `/admin/dashboard`, `/admin/productos`, `/admin/reportes` |

---

## 📊 Reportes

- **Ventas**: listado de pedidos con totales
- **Productos más vendidos**: ranking por cantidad y monto

Descarga en Excel desde: `/admin/reportes`

---

## 🏢 Multi-tenant

El sistema soporta múltiples sedes/bases de datos mediante:

- `X-Tenant-ID` header
- Subdominio (ej: `lima.saboresperu.com`)
- Tenant por defecto configurado en `application.yml`

---

## 🧪 Principios aplicados

- **MVC** – separación de capas
- **SOLID** – services con interfaces
- **DAO / Repository** – Spring Data JPA
- **TDD ready** – estructura preparada para tests
- **Seguridad** – roles, BCrypt, validación de datos

---

## 📦 Librerías de apoyo

- Google Guava (opcional)
- Apache Commons Lang / IO
- Apache POI (reportes Excel)
- Logback (logging por defecto de Spring Boot)

---

## 📝 Notas

- Las contraseñas se almacenan con BCrypt
- El carrito usa sesión HTTP (no persistente)
- Los reportes se generan en memoria y se descargan como `.xlsx`
- El seed solo se ejecuta si no hay roles en la base de datos

---

## 👨‍💻 Autor

Proyecto académico / profesional – Restaurante Virtual de Comida Regional Peruana  
Modelo de negocio: **Dark Kitchen**

---

## 📄 Licencia

Uso educativo / interno del proyecto.
