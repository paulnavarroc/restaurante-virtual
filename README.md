# Sabores Perú — Restaurante Virtual

Plataforma web de comida regional peruana (dark kitchen / delivery) construida con **Java 21**, **Spring Boot**, **Spring MVC + Thymeleaf**, **Spring Security**, **JPA/Hibernate** y **SQL Server**.

Versión objetivo de esta rama: **`release/1.0.0`** (modelo de dominio v2: sedes, menú del día, stock por recetas, Ubigeo, promociones, repartidores).

---

## Tabla de contenidos

1. [Descripción](#descripción)
2. [Características](#características)
3. [Stack tecnológico](#stack-tecnológico)
4. [Arquitectura](#arquitectura)
5. [Requisitos](#requisitos)
6. [Base de datos](#base-de-datos)
7. [Configuración](#configuración)
8. [Arranque](#arranque)
9. [Usuarios y roles](#usuarios-y-roles)
10. [Flujos principales](#flujos-principales)
11. [Estructura del proyecto](#estructura-del-proyecto)
12. [Medios e imágenes](#medios-e-imágenes)
13. [Seguridad](#seguridad)
14. [Dinero y decimales](#dinero-y-decimales)
15. [Smoke test](#smoke-test)
16. [Scripts SQL](#scripts-sql)
17. [Profiles](#profiles)
18. [Despliegue](#despliegue)
19. [Roadmap posterior a 1.0.0](#roadmap-posterior-a-100)
20. [Licencia / notas](#licencia--notas)

---

## Descripción

**Sabores Perú** permite a clientes pedir platos del menú del día de una o más cocinas (sedes), con:

- Asignación automática de la **cocina más cercana** según la dirección (Ubigeo + coordenadas).
- Control de **stock por insumos y recetas** (reserva al confirmar, consumo en preparación, liberación al cancelar).
- **Promociones** y **cupones**.
- Roles de **cocina**, **repartidor**, **admin/operaciones**.

No es solo un catálogo estático: el menú se **publica por sede y fecha**.

---

## Características

### Cliente
- Menú público del día (sin login para ver; login para pedir).
- Carrito en sesión.
- Direcciones con Ubigeo (departamento → provincia → distrito).
- Checkout con método de pago y cupón opcional.
- Historial y detalle de pedidos.

### Cocina (CHEF)
- Listado de pedidos por sede y estado.
- Transiciones: preparación → listo (y cancelación según reglas).
- Marcar ítems del menú como agotados (vía admin/ops o flujo acordado).

### Repartidor
- Disponible / offline.
- Toma o recibe asignación de entregas.
- En camino → entregado.

### Admin / Operaciones
- Dashboard (pedidos y ventas del día, stock bajo, últimos pedidos).
- CRUD productos (+ upload de imagen).
- Recetas por producto.
- Insumos y entradas de stock por sede.
- Menú del día (borrador → publicado → cerrado).
- Promociones y cupones.

---

## Stack tecnológico

| Capa | Tecnología |
|------|------------|
| Lenguaje | Java 21 |
| Framework | Spring Boot 3.x |
| Web | Spring MVC + Thymeleaf (+ Layout Dialect) |
| Seguridad | Spring Security (form login, roles) |
| Persistencia | Spring Data JPA + Hibernate |
| BD | Microsoft SQL Server |
| UI | Tailwind CSS v4 (paleta sky/slate) |
| Utilidades | Lombok; (opcional) Guava, Commons, POI, Logback |
| Build | Maven |

---

## Arquitectura

```text
Browser (Thymeleaf)
    ↓
Controllers (MVC)
    ↓
Services (reglas de negocio, @Transactional)
    ↓
Repositories (JPA)
    ↓
SQL Server (SaboresPeru) — multi-sede con IdSede
```

Principios aplicados:

- **MVC** + capas Service / Repository.
- **DTOs** hacia las vistas (evitar lazy en templates).
- **JOIN FETCH** / trabajo dentro de transacción en lecturas con colecciones.
- **Snapshots** de precios y descuentos en el pedido.
- Un **DataSource** principal; multi-tenant multi-BD queda como opción futura (`TenantKey` en `Sedes`).

### Modelo de negocio (v2)

```text
Sede (cocina) + turnos
Producto (maestro) + Receta + Insumos
StockInsumo por sede
MenuDia (fecha + sede) → MenuDiaItems → público
Pedido → reserva stock → cocina → entrega → repartidor
Promociones / Cupones → PricingService
```

---

## Requisitos

- **JDK 21+**
- **Maven 3.9+** (o wrapper `./mvnw`)
- **SQL Server** (local, Docker o instancia remota)
- IDE recomendado: IntelliJ IDEA
- Navegador moderno

Comprobar Java:

```bash
java -version
```

---

## Base de datos

### 1. Crear la base

```sql
CREATE DATABASE SaboresPeru;
```

### 2. Ejecutar scripts (orden)

1. `scripts/V1__schema.sql` — tablas, FKs, índices, catálogos base (roles, estados, métodos de pago, regiones, categorías).


### 3. Verificar

```sql
USE SaboresPeru;
SELECT name FROM sys.tables ORDER BY name;
SELECT * FROM dbo.Sedes;
SELECT * FROM dbo.UbigeoDistritos;
```

---

## Configuración

### Profiles

| Profile | Uso |
|---------|-----|
| `dev` | Desarrollo local (default) |
| `prod` | Producción (variables de entorno) |

### Variables importantes

| Variable | Descripción | Ejemplo |
|----------|-------------|---------|
| `SPRING_PROFILES_ACTIVE` | Profile activo | `dev` |
| `DB_HOST` | Host SQL Server | `localhost` |
| `DB_USER` | Usuario BD | `sa` |
| `DB_PASSWORD` | Password BD | `***` |
| `DB_URL` | JDBC completo (prod) | `jdbc:sqlserver://...` |
| `MEDIA_ROOT` | Carpeta de uploads | `./uploads` |
| `SERVER_PORT` | Puerto HTTP | `8080` |

### `application-dev.yml` (ejemplo)

```yaml
spring:
  datasource:
    url: jdbc:sqlserver://localhost;databaseName=SaboresPeru;encrypt=true;trustServerCertificate=true
    username: sa
    password: TuPassword
    driver-class-name: com.microsoft.sqlserver.jdbc.SQLServerDriver
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: true
    open-in-view: false

app:
  media-root: ./uploads
```

**No subas passwords reales a Git.** Usa `application.yml.example` como plantilla.

### JPA

- `ddl-auto: validate` — el schema lo manda el SQL, no Hibernate.
- `open-in-view: false` — obliga a fetch/DTO correctos.

---

## Arranque

### IntelliJ IDEA

1. Abrir el proyecto Maven.
2. Run Configuration → Active profiles: `dev`.
3. Variables de entorno si aplica (`DB_PASSWORD`, etc.).
4. Ejecutar `RestauranteVirtualApplication`.

### Maven

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

### URL local

```text
http://localhost:8080
```

---

## Usuarios y roles

Roles en BD (`Roles`):

| Rol | Uso |
|-----|-----|
| `CLIENTE` | Pedidos, direcciones, carrito |
| `CHEF` | Cocina |
| `REPARTIDOR` | Entregas |
| `ADMINISTRADOR` | Todo el backoffice |
| `OPERACIONES` | Menú, stock, promos |
| `SUPERVISOR` | Autorizaciones (reservado) |

### Usuarios demo (si está activo `DevUserSeed` en profile `dev`)

Crear o ajustar según tu seed:

| Correo | Rol típico | Password ejemplo |
|--------|------------|------------------|
| `cliente@saboresperu.com` | CLIENTE | `123456` |
| `admin@saboresperu.com` | ADMINISTRADOR | (definir en seed) |
| `cocina@saboresperu.com` | CHEF | (definir en seed) |
| `repartidor@saboresperu.com` | REPARTIDOR | (definir en seed) |

Las contraseñas se guardan con **BCrypt**.

### Redirección post-login

`RoleBasedAuthSuccessHandler`:

| Rol | Destino |
|-----|---------|
| ADMINISTRADOR / OPERACIONES | `/admin/dashboard` |
| CHEF | `/cocina/pedidos` |
| REPARTIDOR | `/repartidor/entregas` |
| CLIENTE | `/menu` |

---

## Flujos principales

### A. Operación diaria (admin)

```text
1. Entrada de insumos / stock por sede
2. Productos y recetas al día
3. Crear menú del día (sede + fecha) → agregar platos → Publicar
4. (Opcional) Activar promociones / cupones
5. Monitorear dashboard y alertas de stock
```

### B. Pedido (cliente)

```text
/menu → agregar al carrito → checkout (dirección + pago + cupón)
  → Geo: sede más cercana en cobertura
  → Pricing: promos + delivery
  → Reserva de stock
  → Pedido PENDIENTE / CONFIRMADO
```

### C. Cocina → reparto

```text
EN_PREPARACION (consume stock reservado)
  → LISTO (crea Entrega, asigna repartidor si hay disponible)
  → EN_CAMINO → ENTREGADO
```

### D. Cancelación

```text
Si el stock aún está RESERVADO → LIBERACION
```

---

## Estructura del proyecto

```text
src/main/java/com/saboresperu/restaurante/
├── RestauranteVirtualApplication.java
├── config/          # Security, Media, WebMvc, Properties, SuccessHandler
├── controller/      # MVC + APIs (Ubigeo, dev)
├── domain/          # Constantes de estados/tipos
├── dto/
├── entity/
├── exception/
├── repository/
├── service/
│   ├── interfaces/
│   └── impl/
└── util/            # SecurityUtils, etc.

src/main/resources/
├── application.yml
├── application-dev.yml
├── application-prod.yml
├── static/          # css, js
└── templates/
    ├── layout/
    ├── public/
    ├── cliente/
    ├── cocina/
    ├── repartidor/
    ├── admin/
    └── auth/

scripts/
├── V1__schema.sql

uploads/             # NO versionar (gitignored)
```

---

## Medios e imágenes

- Las imágenes de producto se guardan en disco bajo `MEDIA_ROOT` (default `./uploads/productos/`).
- En BD solo se persiste la URL/ruta, p. ej. `/media/productos/12_abc123.webp`.
- Resource handler: `/media/**` → carpeta de uploads.
- Límite orientativo: 5 MB; formatos: JPG, PNG, WEBP, GIF.
- En producción montar volumen persistente o migrar a S3/R2.

```gitignore
/uploads/
```

---

## Seguridad

- Autenticación por formulario.
- Autorización por roles en rutas (`/admin/**`, `/cocina/**`, `/cliente/**`, `/repartidor/**`).
- CSRF habilitado en formularios (tokens Thymeleaf/`_csrf` según config).
- Contraseñas con `BCryptPasswordEncoder`.
- `open-in-view: false`.
- Endpoints `/api/dev/**` solo con profile `dev` (no usar en prod).

---

## Dinero y decimales

| Capa | Tipo |
|------|------|
| SQL Server | `DECIMAL(10,2)` montos; `DECIMAL(12,4)` insumos |
| Java | `java.math.BigDecimal` |
| Redondeo | `RoundingMode.HALF_UP`, escala 2 en precios al cliente |

**No usar** `double`/`float` para dinero.  
Los totales del pedido se **persisten** (snapshot) al confirmar.

---

## Smoke test

Checklist mínimo antes de dar por buena la `1.0.0`:

1. [ ] Arranque sin error de `EntityManagerFactory` / validate.
2. [ ] Login admin → dashboard.
3. [ ] CRUD producto + upload imagen.
4. [ ] Insumo + entrada de stock + receta.
5. [ ] Menú del día publicado visible en `/menu`.
6. [ ] Cliente: dirección Ubigeo + ítem en carrito + checkout.
7. [ ] Filas en `Pedidos`, `ReservasStockPedido` (RESERVADA).
8. [ ] Cocina: EN_PREPARACION → CONSUMIDA; LISTO → `Entregas`.
9. [ ] Repartidor: EN_CAMINO → ENTREGADO.
10. [ ] Cupón o promo refleja descuento en detalle/pedido.

---

## Scripts SQL

| Script | Contenido |
|--------|-----------|
| `V1__schema.sql` | Schema completo v2 + inserts de roles, estados, métodos de pago, regiones, categorías |
| Seed Ubigeo | Departamentos/provincias/distritos (Lima mínimo) |
| Seed sede | Cocina Miraflores, turnos, `ConfiguracionSede`, `TarifasDelivery` |
| Seed demo (opcional) | Ceviche, insumos, stock, menú de hoy |

Ejecutar siempre sobre `SaboresPeru` y en orden de dependencias.

---

## Profiles

```bash
# Desarrollo
export SPRING_PROFILES_ACTIVE=dev

# Producción
export SPRING_PROFILES_ACTIVE=prod
export DB_URL="jdbc:sqlserver://host;databaseName=SaboresPeru;encrypt=true;..."
export DB_USER=...
export DB_PASSWORD=...
export MEDIA_ROOT=/var/app/uploads
```

En `prod`:

- No activar seeds de demo.
- No exponer `/api/dev/**`.
- `thymeleaf.cache=true`, `show-sql=false`.

---

## Despliegue

### Orientación general

1. SQL Server accesible y schema aplicado.
2. JAR:

```bash
./mvnw -DskipTests package
java -jar target/restaurante-virtual-*.jar --spring.profiles.active=prod
```

3. Variables de entorno para BD y `MEDIA_ROOT`.
4. Reverse proxy (Nginx) con HTTPS.
5. Volumen persistente para `uploads`.

### Docker (idea)

- Servicio `app` (JDK 21) + servicio `sqlserver` (o BD externa).
- Volume: `app_uploads:/var/app/uploads`.
- Healthcheck HTTP a `/` o actuator si se añade.

---

## Roadmap posterior a 1.0.0

- Tests unitarios/integración (`PricingService`, `StockService`, concurrencia).
- NxM (2x1) completo y reglas de combinación avanzadas.
- Upload/CDN (S3, Cloudflare R2).
- Pasarela de pagos (Yape/Plin/tarjeta) real.
- Geolocalización en vivo del repartidor.
- Multi-tenant multi-BD solo si un franquiciado lo requiere.
- App móvil / API pública versionada.

---

## Rutas útiles

| Ruta | Descripción |
|------|-------------|
| `/` o `/menu` | Menú público |
| `/login` | Acceso |
| `/cliente/carrito` | Carrito |
| `/cliente/pedidos` | Mis pedidos |
| `/cliente/direcciones` | Direcciones |
| `/cocina/pedidos` | Cocina |
| `/repartidor/entregas` | Entregas |
| `/admin/dashboard` | KPIs |
| `/admin/menus` | Menú del día |
| `/admin/productos` | Catálogo |
| `/admin/stock` | Stock |
| `/admin/promociones` | Promos |
| `/admin/cupones` | Cupones |

---

## Licencia / notas

Proyecto orientado a operación real de restaurante virtual de comida regional peruana.  
La rama **`release/1.0.0`** es el modelo v2 descrito en este documento.

---

**Sabores Perú** — de la carta del día a la puerta del cliente.