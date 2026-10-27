USE master;
GO

/* ============================================================
   1. CREACIÓN DE BASE DE DATOS
   ============================================================ */

IF DB_ID('RestauranteRegional') IS NULL
BEGIN
    CREATE DATABASE RestauranteRegional;
END
GO

USE RestauranteRegional;
GO

/* ============================================================
   2. TABLA: ROLES
   ============================================================ */

CREATE TABLE dbo.Roles
(
    IdRol           INT IDENTITY(1,1) NOT NULL,
    Nombre          VARCHAR(50) NOT NULL,
    Descripcion     VARCHAR(250) NULL,
    Activo          BIT NOT NULL CONSTRAINT DF_Roles_Activo DEFAULT 1,

    CONSTRAINT PK_Roles PRIMARY KEY (IdRol),
    CONSTRAINT UQ_Roles_Nombre UNIQUE (Nombre)
);
GO

/* ============================================================
   3. TABLA: USUARIOS
   ============================================================ */

CREATE TABLE dbo.Usuarios
(
    IdUsuario           INT IDENTITY(1,1) NOT NULL,
    Nombre              VARCHAR(100) NOT NULL,
    Apellido            VARCHAR(100) NOT NULL,
    Correo              VARCHAR(150) NOT NULL,
    ContrasenaHash      VARCHAR(500) NOT NULL,
    Telefono            VARCHAR(20) NULL,
    Activo              BIT NOT NULL CONSTRAINT DF_Usuarios_Activo DEFAULT 1,
    FechaRegistro       DATETIME2 NOT NULL CONSTRAINT DF_Usuarios_FechaRegistro DEFAULT SYSDATETIME(),
    UltimoAcceso        DATETIME2 NULL,

    CONSTRAINT PK_Usuarios PRIMARY KEY (IdUsuario),
    CONSTRAINT UQ_Usuarios_Correo UNIQUE (Correo)
);
GO

/* ============================================================
   4. RELACIÓN USUARIO - ROL
   ============================================================ */

CREATE TABLE dbo.UsuarioRoles
(
    IdUsuario       INT NOT NULL,
    IdRol           INT NOT NULL,

    CONSTRAINT PK_UsuarioRoles PRIMARY KEY (IdUsuario, IdRol),

    CONSTRAINT FK_UsuarioRoles_Usuario
        FOREIGN KEY (IdUsuario)
        REFERENCES dbo.Usuarios(IdUsuario),

    CONSTRAINT FK_UsuarioRoles_Rol
        FOREIGN KEY (IdRol)
        REFERENCES dbo.Roles(IdRol)
);
GO

/* ============================================================
   5. TABLA: CLIENTES
   ============================================================ */

CREATE TABLE dbo.Clientes
(
    IdCliente           INT IDENTITY(1,1) NOT NULL,
    IdUsuario           INT NOT NULL,
    FechaNacimiento     DATE NULL,
    DocumentoTipo       VARCHAR(20) NULL,
    DocumentoNumero     VARCHAR(30) NULL,
    FechaRegistro       DATETIME2 NOT NULL CONSTRAINT DF_Clientes_FechaRegistro DEFAULT SYSDATETIME(),

    CONSTRAINT PK_Clientes PRIMARY KEY (IdCliente),

    CONSTRAINT FK_Clientes_Usuario
        FOREIGN KEY (IdUsuario)
        REFERENCES dbo.Usuarios(IdUsuario),

    CONSTRAINT UQ_Clientes_Usuario UNIQUE (IdUsuario),

    CONSTRAINT UQ_Clientes_Documento
        UNIQUE (DocumentoTipo, DocumentoNumero)
);
GO

/* ============================================================
   6. TABLA: DIRECCIONES
   ============================================================ */

CREATE TABLE dbo.Direcciones
(
    IdDireccion        INT IDENTITY(1,1) NOT NULL,
    IdCliente          INT NOT NULL,
    Alias              VARCHAR(50) NULL,
    DireccionCompleta  VARCHAR(300) NOT NULL,
    Referencia         VARCHAR(300) NULL,
    Distrito           VARCHAR(100) NOT NULL,
    Provincia          VARCHAR(100) NOT NULL,
    Departamento       VARCHAR(100) NOT NULL,
    CodigoPostal       VARCHAR(20) NULL,
    Latitud             DECIMAL(10,7) NULL,
    Longitud            DECIMAL(10,7) NULL,
    EsPrincipal         BIT NOT NULL CONSTRAINT DF_Direcciones_EsPrincipal DEFAULT 0,
    Activo              BIT NOT NULL CONSTRAINT DF_Direcciones_Activo DEFAULT 1,

    CONSTRAINT PK_Direcciones PRIMARY KEY (IdDireccion),

    CONSTRAINT FK_Direcciones_Cliente
        FOREIGN KEY (IdCliente)
        REFERENCES dbo.Clientes(IdCliente)
);
GO

/* ============================================================
   7. TABLA: REGIONES
   ============================================================ */

CREATE TABLE dbo.Regiones
(
    IdRegion        INT IDENTITY(1,1) NOT NULL,
    Nombre          VARCHAR(50) NOT NULL,
    Descripcion     VARCHAR(250) NULL,
    Activo          BIT NOT NULL CONSTRAINT DF_Regiones_Activo DEFAULT 1,

    CONSTRAINT PK_Regiones PRIMARY KEY (IdRegion),
    CONSTRAINT UQ_Regiones_Nombre UNIQUE (Nombre)
);
GO

/* ============================================================
   8. TABLA: CATEGORIAS
   ============================================================ */

CREATE TABLE dbo.Categorias
(
    IdCategoria     INT IDENTITY(1,1) NOT NULL,
    Nombre          VARCHAR(100) NOT NULL,
    Descripcion     VARCHAR(250) NULL,
    Activo          BIT NOT NULL CONSTRAINT DF_Categorias_Activo DEFAULT 1,

    CONSTRAINT PK_Categorias PRIMARY KEY (IdCategoria),
    CONSTRAINT UQ_Categorias_Nombre UNIQUE (Nombre)
);
GO

/* ============================================================
   9. TABLA: PRODUCTOS
   ============================================================ */

CREATE TABLE dbo.Productos
(
    IdProducto          INT IDENTITY(1,1) NOT NULL,
    IdRegion            INT NOT NULL,
    IdCategoria         INT NOT NULL,
    Nombre              VARCHAR(150) NOT NULL,
    Descripcion         VARCHAR(500) NULL,
    Precio              DECIMAL(10,2) NOT NULL,
    ImagenUrl           VARCHAR(500) NULL,
    TiempoPreparacion   INT NULL,
    Disponible          BIT NOT NULL CONSTRAINT DF_Productos_Disponible DEFAULT 1,
    Activo              BIT NOT NULL CONSTRAINT DF_Productos_Activo DEFAULT 1,
    FechaRegistro       DATETIME2 NOT NULL CONSTRAINT DF_Productos_FechaRegistro DEFAULT SYSDATETIME(),

    CONSTRAINT PK_Productos PRIMARY KEY (IdProducto),

    CONSTRAINT FK_Productos_Region
        FOREIGN KEY (IdRegion)
        REFERENCES dbo.Regiones(IdRegion),

    CONSTRAINT FK_Productos_Categoria
        FOREIGN KEY (IdCategoria)
        REFERENCES dbo.Categorias(IdCategoria),

    CONSTRAINT CK_Productos_Precio
        CHECK (Precio >= 0),

    CONSTRAINT CK_Productos_TiempoPreparacion
        CHECK (TiempoPreparacion IS NULL OR TiempoPreparacion > 0)
);
GO

/* ============================================================
   10. TABLA: INGREDIENTES
   ============================================================ */

CREATE TABLE dbo.Ingredientes
(
    IdIngrediente       INT IDENTITY(1,1) NOT NULL,
    Nombre              VARCHAR(150) NOT NULL,
    UnidadMedida        VARCHAR(30) NOT NULL,
    StockActual         DECIMAL(12,3) NOT NULL CONSTRAINT DF_Ingredientes_Stock DEFAULT 0,
    StockMinimo         DECIMAL(12,3) NOT NULL CONSTRAINT DF_Ingredientes_StockMinimo DEFAULT 0,
    CostoUnitario       DECIMAL(10,2) NULL,
    Activo              BIT NOT NULL CONSTRAINT DF_Ingredientes_Activo DEFAULT 1,

    CONSTRAINT PK_Ingredientes PRIMARY KEY (IdIngrediente),
    CONSTRAINT UQ_Ingredientes_Nombre UNIQUE (Nombre),

    CONSTRAINT CK_Ingredientes_Stock
        CHECK (StockActual >= 0),

    CONSTRAINT CK_Ingredientes_StockMinimo
        CHECK (StockMinimo >= 0),

    CONSTRAINT CK_Ingredientes_Costo
        CHECK (CostoUnitario IS NULL OR CostoUnitario >= 0)
);
GO

/* ============================================================
   11. TABLA: PRODUCTO - INGREDIENTES
   ============================================================ */

CREATE TABLE dbo.ProductoIngredientes
(
    IdProducto          INT NOT NULL,
    IdIngrediente       INT NOT NULL,
    Cantidad            DECIMAL(12,3) NOT NULL,

    CONSTRAINT PK_ProductoIngredientes
        PRIMARY KEY (IdProducto, IdIngrediente),

    CONSTRAINT FK_ProductoIngredientes_Producto
        FOREIGN KEY (IdProducto)
        REFERENCES dbo.Productos(IdProducto),

    CONSTRAINT FK_ProductoIngredientes_Ingrediente
        FOREIGN KEY (IdIngrediente)
        REFERENCES dbo.Ingredientes(IdIngrediente),

    CONSTRAINT CK_ProductoIngredientes_Cantidad
        CHECK (Cantidad > 0)
);
GO

/* ============================================================
   12. TABLA: RECETAS
   ============================================================ */

CREATE TABLE dbo.Recetas
(
    IdReceta            INT IDENTITY(1,1) NOT NULL,
    IdProducto          INT NOT NULL,
    Nombre              VARCHAR(150) NOT NULL,
    Version             INT NOT NULL CONSTRAINT DF_Recetas_Version DEFAULT 1,
    Preparacion         VARCHAR(MAX) NOT NULL,
    TiempoPreparacion   INT NULL,
    Porciones           INT NULL,
    EsActual            BIT NOT NULL CONSTRAINT DF_Recetas_EsActual DEFAULT 1,
    FechaRegistro       DATETIME2 NOT NULL CONSTRAINT DF_Recetas_FechaRegistro DEFAULT SYSDATETIME(),

    CONSTRAINT PK_Recetas PRIMARY KEY (IdReceta),

    CONSTRAINT FK_Recetas_Producto
        FOREIGN KEY (IdProducto)
        REFERENCES dbo.Productos(IdProducto),

    CONSTRAINT CK_Recetas_Version
        CHECK (Version > 0),

    CONSTRAINT CK_Recetas_Tiempo
        CHECK (TiempoPreparacion IS NULL OR TiempoPreparacion > 0),

    CONSTRAINT CK_Recetas_Porciones
        CHECK (Porciones IS NULL OR Porciones > 0)
);
GO

/* ============================================================
   13. TABLA: RECETA - INGREDIENTES
   ============================================================ */

CREATE TABLE dbo.RecetaIngredientes
(
    IdReceta            INT NOT NULL,
    IdIngrediente       INT NOT NULL,
    Cantidad            DECIMAL(12,3) NOT NULL,
    UnidadMedida        VARCHAR(30) NOT NULL,

    CONSTRAINT PK_RecetaIngredientes
        PRIMARY KEY (IdReceta, IdIngrediente),

    CONSTRAINT FK_RecetaIngredientes_Receta
        FOREIGN KEY (IdReceta)
        REFERENCES dbo.Recetas(IdReceta),

    CONSTRAINT FK_RecetaIngredientes_Ingrediente
        FOREIGN KEY (IdIngrediente)
        REFERENCES dbo.Ingredientes(IdIngrediente),

    CONSTRAINT CK_RecetaIngredientes_Cantidad
        CHECK (Cantidad > 0)
);
GO

/* ============================================================
   14. TABLA: PROVEEDORES
   ============================================================ */

CREATE TABLE dbo.Proveedores
(
    IdProveedor         INT IDENTITY(1,1) NOT NULL,
    RazonSocial         VARCHAR(200) NOT NULL,
    Ruc                 VARCHAR(20) NULL,
    Contacto            VARCHAR(150) NULL,
    Telefono            VARCHAR(30) NULL,
    Correo              VARCHAR(150) NULL,
    Direccion           VARCHAR(300) NULL,
    Activo              BIT NOT NULL CONSTRAINT DF_Proveedores_Activo DEFAULT 1,

    CONSTRAINT PK_Proveedores PRIMARY KEY (IdProveedor),
    CONSTRAINT UQ_Proveedores_Ruc UNIQUE (Ruc)
);
GO

/* ============================================================
   15. TABLA: PROVEEDOR - INGREDIENTE
   ============================================================ */

CREATE TABLE dbo.ProveedorIngredientes
(
    IdProveedor         INT NOT NULL,
    IdIngrediente       INT NOT NULL,
    PrecioCompra        DECIMAL(10,2) NULL,
    EsPrincipal         BIT NOT NULL CONSTRAINT DF_ProveedorIngredientes_Principal DEFAULT 0,

    CONSTRAINT PK_ProveedorIngredientes
        PRIMARY KEY (IdProveedor, IdIngrediente),

    CONSTRAINT FK_ProveedorIngredientes_Proveedor
        FOREIGN KEY (IdProveedor)
        REFERENCES dbo.Proveedores(IdProveedor),

    CONSTRAINT FK_ProveedorIngredientes_Ingrediente
        FOREIGN KEY (IdIngrediente)
        REFERENCES dbo.Ingredientes(IdIngrediente),

    CONSTRAINT CK_ProveedorIngredientes_Precio
        CHECK (PrecioCompra IS NULL OR PrecioCompra >= 0)
);
GO

/* ============================================================
   16. ESTADOS DEL PEDIDO
   ============================================================ */

CREATE TABLE dbo.EstadosPedido
(
    IdEstadoPedido     INT IDENTITY(1,1) NOT NULL,
    Nombre             VARCHAR(50) NOT NULL,
    Descripcion        VARCHAR(250) NULL,

    CONSTRAINT PK_EstadosPedido PRIMARY KEY (IdEstadoPedido),
    CONSTRAINT UQ_EstadosPedido_Nombre UNIQUE (Nombre)
);
GO

/* ============================================================
   17. TABLA: PEDIDOS
   ============================================================ */

CREATE TABLE dbo.Pedidos
(
    IdPedido               INT IDENTITY(1,1) NOT NULL,
    IdCliente              INT NOT NULL,
    IdDireccion            INT NOT NULL,
    IdEstadoPedido         INT NOT NULL,
    FechaPedido            DATETIME2 NOT NULL CONSTRAINT DF_Pedidos_Fecha DEFAULT SYSDATETIME(),
    Subtotal               DECIMAL(10,2) NOT NULL CONSTRAINT DF_Pedidos_Subtotal DEFAULT 0,
    CostoDelivery           DECIMAL(10,2) NOT NULL CONSTRAINT DF_Pedidos_Delivery DEFAULT 0,
    Descuento               DECIMAL(10,2) NOT NULL CONSTRAINT DF_Pedidos_Descuento DEFAULT 0,
    Total                  DECIMAL(10,2) NOT NULL CONSTRAINT DF_Pedidos_Total DEFAULT 0,
    Observaciones          VARCHAR(500) NULL,

    CONSTRAINT PK_Pedidos PRIMARY KEY (IdPedido),

    CONSTRAINT FK_Pedidos_Cliente
        FOREIGN KEY (IdCliente)
        REFERENCES dbo.Clientes(IdCliente),

    CONSTRAINT FK_Pedidos_Direccion
        FOREIGN KEY (IdDireccion)
        REFERENCES dbo.Direcciones(IdDireccion),

    CONSTRAINT FK_Pedidos_Estado
        FOREIGN KEY (IdEstadoPedido)
        REFERENCES dbo.EstadosPedido(IdEstadoPedido),

    CONSTRAINT CK_Pedidos_Subtotal CHECK (Subtotal >= 0),
    CONSTRAINT CK_Pedidos_Delivery CHECK (CostoDelivery >= 0),
    CONSTRAINT CK_Pedidos_Descuento CHECK (Descuento >= 0),
    CONSTRAINT CK_Pedidos_Total CHECK (Total >= 0)
);
GO

/* ============================================================
   18. TABLA: DETALLE DEL PEDIDO
   ============================================================ */

CREATE TABLE dbo.DetallePedido
(
    IdDetallePedido    INT IDENTITY(1,1) NOT NULL,
    IdPedido           INT NOT NULL,
    IdProducto         INT NOT NULL,
    Cantidad           INT NOT NULL,
    PrecioUnitario     DECIMAL(10,2) NOT NULL,
    Subtotal            AS (Cantidad * PrecioUnitario) PERSISTED,

    CONSTRAINT PK_DetallePedido PRIMARY KEY (IdDetallePedido),

    CONSTRAINT FK_DetallePedido_Pedido
        FOREIGN KEY (IdPedido)
        REFERENCES dbo.Pedidos(IdPedido),

    CONSTRAINT FK_DetallePedido_Producto
        FOREIGN KEY (IdProducto)
        REFERENCES dbo.Productos(IdProducto),

    CONSTRAINT CK_DetallePedido_Cantidad
        CHECK (Cantidad > 0),

    CONSTRAINT CK_DetallePedido_Precio
        CHECK (PrecioUnitario >= 0)
);
GO

/* ============================================================
   19. MÉTODOS DE PAGO
   ============================================================ */

CREATE TABLE dbo.MetodosPago
(
    IdMetodoPago       INT IDENTITY(1,1) NOT NULL,
    Nombre             VARCHAR(50) NOT NULL,
    Activo              BIT NOT NULL CONSTRAINT DF_MetodosPago_Activo DEFAULT 1,

    CONSTRAINT PK_MetodosPago PRIMARY KEY (IdMetodoPago),
    CONSTRAINT UQ_MetodosPago_Nombre UNIQUE (Nombre)
);
GO

/* ============================================================
   20. PAGOS
   ============================================================ */

CREATE TABLE dbo.Pagos
(
    IdPago             INT IDENTITY(1,1) NOT NULL,
    IdPedido           INT NOT NULL,
    IdMetodoPago       INT NOT NULL,
    Monto              DECIMAL(10,2) NOT NULL,
    Estado             VARCHAR(30) NOT NULL,
    Referencia         VARCHAR(150) NULL,
    FechaPago          DATETIME2 NULL,
    FechaRegistro      DATETIME2 NOT NULL CONSTRAINT DF_Pagos_FechaRegistro DEFAULT SYSDATETIME(),

    CONSTRAINT PK_Pagos PRIMARY KEY (IdPago),

    CONSTRAINT FK_Pagos_Pedido
        FOREIGN KEY (IdPedido)
        REFERENCES dbo.Pedidos(IdPedido),

    CONSTRAINT FK_Pagos_Metodo
        FOREIGN KEY (IdMetodoPago)
        REFERENCES dbo.MetodosPago(IdMetodoPago),

    CONSTRAINT CK_Pagos_Monto
        CHECK (Monto > 0),

    CONSTRAINT CK_Pagos_Estado
        CHECK (Estado IN ('PENDIENTE', 'APROBADO', 'RECHAZADO', 'DEVUELTO'))
);
GO

/* ============================================================
   21. HISTORIAL DEL PEDIDO
   ============================================================ */

CREATE TABLE dbo.HistorialPedido
(
    IdHistorial        INT IDENTITY(1,1) NOT NULL,
    IdPedido           INT NOT NULL,
    IdEstadoPedido     INT NOT NULL,
    IdUsuario          INT NULL,
    Observacion        VARCHAR(500) NULL,
    FechaCambio        DATETIME2 NOT NULL CONSTRAINT DF_HistorialPedido_Fecha DEFAULT SYSDATETIME(),

    CONSTRAINT PK_HistorialPedido PRIMARY KEY (IdHistorial),

    CONSTRAINT FK_HistorialPedido_Pedido
        FOREIGN KEY (IdPedido)
        REFERENCES dbo.Pedidos(IdPedido),

    CONSTRAINT FK_HistorialPedido_Estado
        FOREIGN KEY (IdEstadoPedido)
        REFERENCES dbo.EstadosPedido(IdEstadoPedido),

    CONSTRAINT FK_HistorialPedido_Usuario
        FOREIGN KEY (IdUsuario)
        REFERENCES dbo.Usuarios(IdUsuario)
);
GO

/* ============================================================
   22. REPARTIDORES
   ============================================================ */

CREATE TABLE dbo.Repartidores
(
    IdRepartidor       INT IDENTITY(1,1) NOT NULL,
    IdUsuario          INT NOT NULL,
    Documento          VARCHAR(30) NULL,
    TipoVehiculo       VARCHAR(50) NULL,
    Placa               VARCHAR(20) NULL,
    Disponible          BIT NOT NULL CONSTRAINT DF_Repartidores_Disponible DEFAULT 1,
    Activo              BIT NOT NULL CONSTRAINT DF_Repartidores_Activo DEFAULT 1,

    CONSTRAINT PK_Repartidores PRIMARY KEY (IdRepartidor),

    CONSTRAINT FK_Repartidores_Usuario
        FOREIGN KEY (IdUsuario)
        REFERENCES dbo.Usuarios(IdUsuario),

    CONSTRAINT UQ_Repartidores_Usuario UNIQUE (IdUsuario)
);
GO

/* ============================================================
   23. ENTREGAS
   ============================================================ */

CREATE TABLE dbo.Entregas
(
    IdEntrega              INT IDENTITY(1,1) NOT NULL,
    IdPedido               INT NOT NULL,
    IdRepartidor           INT NULL,
    Estado                 VARCHAR(40) NOT NULL,
    FechaAsignacion        DATETIME2 NULL,
    FechaRecogida          DATETIME2 NULL,
    FechaEntrega           DATETIME2 NULL,
    LatitudEntrega         DECIMAL(10,7) NULL,
    LongitudEntrega        DECIMAL(10,7) NULL,
    Observacion            VARCHAR(500) NULL,

    CONSTRAINT PK_Entregas PRIMARY KEY (IdEntrega),

    CONSTRAINT FK_Entregas_Pedido
        FOREIGN KEY (IdPedido)
        REFERENCES dbo.Pedidos(IdPedido),

    CONSTRAINT FK_Entregas_Repartidor
        FOREIGN KEY (IdRepartidor)
        REFERENCES dbo.Repartidores(IdRepartidor),

    CONSTRAINT UQ_Entregas_Pedido UNIQUE (IdPedido),

    CONSTRAINT CK_Entregas_Estado
        CHECK (Estado IN
        (
            'PENDIENTE',
            'ASIGNADA',
            'RECOGIDA',
            'EN_CAMINO',
            'ENTREGADA',
            'CANCELADA'
        ))
);
GO

/* ============================================================
   24. PROMOCIONES
   ============================================================ */

CREATE TABLE dbo.Promociones
(
    IdPromocion        INT IDENTITY(1,1) NOT NULL,
    Nombre             VARCHAR(150) NOT NULL,
    Descripcion        VARCHAR(500) NULL,
    Codigo             VARCHAR(50) NULL,
    TipoDescuento      VARCHAR(20) NOT NULL,
    ValorDescuento     DECIMAL(10,2) NOT NULL,
    FechaInicio        DATETIME2 NOT NULL,
    FechaFin           DATETIME2 NOT NULL,
    MontoMinimo        DECIMAL(10,2) NULL,
    Activo              BIT NOT NULL CONSTRAINT DF_Promociones_Activo DEFAULT 1,

    CONSTRAINT PK_Promociones PRIMARY KEY (IdPromocion),

    CONSTRAINT UQ_Promociones_Codigo UNIQUE (Codigo),

    CONSTRAINT CK_Promociones_Tipo
        CHECK (TipoDescuento IN ('PORCENTAJE', 'MONTO')),

    CONSTRAINT CK_Promociones_Valor
        CHECK (ValorDescuento >= 0),

    CONSTRAINT CK_Promociones_Fechas
        CHECK (FechaFin > FechaInicio),

    CONSTRAINT CK_Promociones_MontoMinimo
        CHECK (MontoMinimo IS NULL OR MontoMinimo >= 0)
);
GO

/* ============================================================
   25. PEDIDOS - PROMOCIONES
   ============================================================ */

CREATE TABLE dbo.PedidoPromociones
(
    IdPedido           INT NOT NULL,
    IdPromocion        INT NOT NULL,
    MontoDescuento     DECIMAL(10,2) NOT NULL,

    CONSTRAINT PK_PedidoPromociones
        PRIMARY KEY (IdPedido, IdPromocion),

    CONSTRAINT FK_PedidoPromociones_Pedido
        FOREIGN KEY (IdPedido)
        REFERENCES dbo.Pedidos(IdPedido),

    CONSTRAINT FK_PedidoPromociones_Promocion
        FOREIGN KEY (IdPromocion)
        REFERENCES dbo.Promociones(IdPromocion),

    CONSTRAINT CK_PedidoPromociones_Descuento
        CHECK (MontoDescuento >= 0)
);
GO

/* ============================================================
   26. RESEÑAS
   ============================================================ */

CREATE TABLE dbo.Resenas
(
    IdResena           INT IDENTITY(1,1) NOT NULL,
    IdPedido            INT NOT NULL,
    IdCliente           INT NOT NULL,
    IdProducto          INT NULL,
    Calificacion        TINYINT NOT NULL,
    Comentario          VARCHAR(1000) NULL,
    FechaRegistro       DATETIME2 NOT NULL CONSTRAINT DF_Resenas_Fecha DEFAULT SYSDATETIME(),

    CONSTRAINT PK_Resenas PRIMARY KEY (IdResena),

    CONSTRAINT FK_Resenas_Pedido
        FOREIGN KEY (IdPedido)
        REFERENCES dbo.Pedidos(IdPedido),

    CONSTRAINT FK_Resenas_Cliente
        FOREIGN KEY (IdCliente)
        REFERENCES dbo.Clientes(IdCliente),

    CONSTRAINT FK_Resenas_Producto
        FOREIGN KEY (IdProducto)
        REFERENCES dbo.Productos(IdProducto),

    CONSTRAINT CK_Resenas_Calificacion
        CHECK (Calificacion BETWEEN 1 AND 5)
);
GO

/* ============================================================
   27. NOTIFICACIONES
   ============================================================ */

CREATE TABLE dbo.Notificaciones
(
    IdNotificacion     INT IDENTITY(1,1) NOT NULL,
    IdUsuario          INT NOT NULL,
    Titulo             VARCHAR(150) NOT NULL,
    Mensaje            VARCHAR(500) NOT NULL,
    Tipo               VARCHAR(50) NULL,
    Leida              BIT NOT NULL CONSTRAINT DF_Notificaciones_Leida DEFAULT 0,
    FechaEnvio         DATETIME2 NOT NULL CONSTRAINT DF_Notificaciones_Fecha DEFAULT SYSDATETIME(),

    CONSTRAINT PK_Notificaciones PRIMARY KEY (IdNotificacion),

    CONSTRAINT FK_Notificaciones_Usuario
        FOREIGN KEY (IdUsuario)
        REFERENCES dbo.Usuarios(IdUsuario)
);
GO

/* ============================================================
   28. ÍNDICES
   ============================================================ */

CREATE INDEX IX_Usuarios_Correo
ON dbo.Usuarios(Correo);
GO

CREATE INDEX IX_Productos_Region
ON dbo.Productos(IdRegion);
GO

CREATE INDEX IX_Productos_Categoria
ON dbo.Productos(IdCategoria);
GO

CREATE INDEX IX_Productos_Disponible
ON dbo.Productos(Disponible);
GO

CREATE INDEX IX_Pedidos_Cliente
ON dbo.Pedidos(IdCliente);
GO

CREATE INDEX IX_Pedidos_Estado
ON dbo.Pedidos(IdEstadoPedido);
GO

CREATE INDEX IX_Pedidos_Fecha
ON dbo.Pedidos(FechaPedido);
GO

CREATE INDEX IX_DetallePedido_Pedido
ON dbo.DetallePedido(IdPedido);
GO

CREATE INDEX IX_HistorialPedido_Pedido
ON dbo.HistorialPedido(IdPedido);
GO

CREATE INDEX IX_Entregas_Repartidor
ON dbo.Entregas(IdRepartidor);
GO

CREATE INDEX IX_Notificaciones_Usuario
ON dbo.Notificaciones(IdUsuario, Leida);
GO

/* ============================================================
   29. DATOS MAESTROS INICIALES
   ============================================================ */

/*
INSERT INTO dbo.Roles (Nombre, Descripcion)
VALUES
('CLIENTE', 'Cliente que realiza pedidos'),
('ADMINISTRADOR', 'Administrador general del restaurante'),
('CHEF', 'Personal encargado de cocina'),
('REPARTIDOR', 'Personal encargado del delivery');
GO

INSERT INTO dbo.Regiones (Nombre, Descripcion)
VALUES
('COSTA', 'Gastronomía tradicional de la costa peruana'),
('SIERRA', 'Gastronomía tradicional de la sierra peruana'),
('SELVA', 'Gastronomía tradicional de la selva peruana');
GO

INSERT INTO dbo.Categorias (Nombre, Descripcion)
VALUES
('ENTRADAS', 'Entradas y piqueos'),
('PLATOS DE FONDO', 'Platos principales'),
('POSTRES', 'Postres tradicionales'),
('BEBIDAS', 'Bebidas tradicionales'),
('COMBOS', 'Combos familiares y corporativos');
GO

INSERT INTO dbo.MetodosPago (Nombre)
VALUES
('EFECTIVO'),
('TARJETA'),
('YAPE'),
('PLIN'),
('TRANSFERENCIA');
GO

INSERT INTO dbo.EstadosPedido (Nombre, Descripcion)
VALUES
('PENDIENTE', 'Pedido registrado pendiente de procesamiento'),
('CONFIRMADO', 'Pedido confirmado'),
('EN_PREPARACION', 'Pedido en preparación en cocina'),
('LISTO', 'Pedido listo para ser recogido'),
('EN_CAMINO', 'Pedido en camino al cliente'),
('ENTREGADO', 'Pedido entregado al cliente'),
('CANCELADO', 'Pedido cancelado');
GO

*/