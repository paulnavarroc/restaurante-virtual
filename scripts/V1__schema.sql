USE master;
GO

/* ============================================================
   1. CREACIÓN DE BASE DE DATOS
   ============================================================ */

IF DB_ID('SaboresPeru') IS NULL
BEGIN
    CREATE DATABASE SaboresPeru;
END
GO

USE SaboresPeru;
GO


SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
GO



/* ---------------------------------------------------------
   1. Seguridad y personas
   --------------------------------------------------------- */

CREATE TABLE dbo.Roles (
    IdRol           INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_Roles PRIMARY KEY,
    Nombre          NVARCHAR(50)  NOT NULL,
    Descripcion     NVARCHAR(200) NULL,
    Activo          BIT NOT NULL CONSTRAINT DF_Roles_Activo DEFAULT (1),
    CONSTRAINT UQ_Roles_Nombre UNIQUE (Nombre)
);
GO

CREATE TABLE dbo.Usuarios (
    IdUsuario       INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_Usuarios PRIMARY KEY,
    Nombre          NVARCHAR(100) NOT NULL,
    Apellido        NVARCHAR(100) NOT NULL,
    Correo          NVARCHAR(150) NOT NULL,
    ContrasenaHash  NVARCHAR(255) NOT NULL,
    Telefono        NVARCHAR(30)  NULL,
    Activo          BIT NOT NULL CONSTRAINT DF_Usuarios_Activo DEFAULT (1),
    FechaRegistro   DATETIME2(0) NOT NULL CONSTRAINT DF_Usuarios_Fecha DEFAULT (SYSUTCDATETIME()),
	UltimoAcceso     DATETIME2(0) NULL,
    UltimaIp         NVARCHAR(45) NULL,
    UltimoUserAgent  NVARCHAR(500) NULL,
    CONSTRAINT UQ_Usuarios_Correo UNIQUE (Correo)
);
GO

CREATE TABLE dbo.UsuarioAccesos (
    IdAcceso        BIGINT IDENTITY(1,1) NOT NULL CONSTRAINT PK_UsuarioAccesos PRIMARY KEY,
    IdUsuario       INT NOT NULL,
    Evento          NVARCHAR(30) NOT NULL,  -- LOGIN_OK, LOGIN_FAIL, LOGOUT, SESSION_ACTIVITY
    Exitoso         BIT NOT NULL CONSTRAINT DF_Acceso_Ok DEFAULT (1),
    Ip              NVARCHAR(45) NULL,
    UserAgent       NVARCHAR(500) NULL,
    Navegador       NVARCHAR(80) NULL,
    SistemaOperativo NVARCHAR(80) NULL,
    Dispositivo     NVARCHAR(40) NULL, -- Desktop, Mobile, Tablet
    SessionId       NVARCHAR(100) NULL,
    Detalle         NVARCHAR(300) NULL,
    Fecha           DATETIME2(0) NOT NULL CONSTRAINT DF_Acceso_Fecha DEFAULT (SYSUTCDATETIME()),
    CONSTRAINT FK_Acceso_Usuario FOREIGN KEY (IdUsuario) REFERENCES dbo.Usuarios(IdUsuario)
);
GO

CREATE INDEX IX_Accesos_Usuario_Fecha ON dbo.UsuarioAccesos(IdUsuario, Fecha DESC);
CREATE INDEX IX_Accesos_Fecha ON dbo.UsuarioAccesos(Fecha DESC);
GO


CREATE TABLE dbo.UsuarioRoles (
    IdUsuario INT NOT NULL,
    IdRol     INT NOT NULL,
    CONSTRAINT PK_UsuarioRoles PRIMARY KEY (IdUsuario, IdRol),
    CONSTRAINT FK_UsuarioRoles_Usuario FOREIGN KEY (IdUsuario) REFERENCES dbo.Usuarios(IdUsuario),
    CONSTRAINT FK_UsuarioRoles_Rol FOREIGN KEY (IdRol) REFERENCES dbo.Roles(IdRol)
);
GO

CREATE TABLE dbo.Clientes (
    IdCliente       INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_Clientes PRIMARY KEY,
    IdUsuario       INT NOT NULL,
    DocumentoTipo   NVARCHAR(20)  NULL,
    DocumentoNumero NVARCHAR(30)  NULL,
    FechaRegistro   DATETIME2(0) NOT NULL CONSTRAINT DF_Clientes_Fecha DEFAULT (SYSUTCDATETIME()),
    CONSTRAINT UQ_Clientes_Usuario UNIQUE (IdUsuario),
    CONSTRAINT FK_Clientes_Usuario FOREIGN KEY (IdUsuario) REFERENCES dbo.Usuarios(IdUsuario)
);
GO


CREATE TABLE dbo.PersonalCocina (
    IdPersonalCocina INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_PersonalCocina PRIMARY KEY,
    IdUsuario        INT NOT NULL,
    IdSede           INT NOT NULL,
    Cargo            NVARCHAR(50) NULL, -- Chef, Ayudante, etc.
    Activo           BIT NOT NULL CONSTRAINT DF_PersonalCocina_Activo DEFAULT (1),
    FechaAsignacion  DATETIME2(0) NOT NULL CONSTRAINT DF_PersonalCocina_Fecha DEFAULT (SYSUTCDATETIME()),
    CONSTRAINT UQ_PersonalCocina_Usuario UNIQUE (IdUsuario),
    CONSTRAINT FK_PersonalCocina_Usuario FOREIGN KEY (IdUsuario) REFERENCES dbo.Usuarios(IdUsuario),
    CONSTRAINT FK_PersonalCocina_Sede FOREIGN KEY (IdSede) REFERENCES dbo.Sedes(IdSede)
);
GO

CREATE INDEX IX_PersonalCocina_Sede ON dbo.PersonalCocina(IdSede, Activo);
GO


/* ---------------------------------------------------------
   2. Sedes y turnos
   --------------------------------------------------------- */

CREATE TABLE dbo.Sedes (
    IdSede              INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_Sedes PRIMARY KEY,
    Nombre              NVARCHAR(120) NOT NULL,
    Direccion           NVARCHAR(300) NOT NULL,
    Lat                 DECIMAL(10,7) NOT NULL,
    Lng                 DECIMAL(10,7) NOT NULL,
    RadioCoberturaKm    DECIMAL(8,2)  NOT NULL CONSTRAINT DF_Sedes_Radio DEFAULT (5.00),
    Activa              BIT NOT NULL CONSTRAINT DF_Sedes_Activa DEFAULT (1),
    TenantKey           NVARCHAR(50)  NULL,
    Telefono            NVARCHAR(30)  NULL,
    FechaCreacion       DATETIME2(0) NOT NULL CONSTRAINT DF_Sedes_Fecha DEFAULT (SYSUTCDATETIME())
);
GO

CREATE TABLE dbo.SedeTurnos (
    IdSedeTurno INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_SedeTurnos PRIMARY KEY,
    IdSede      INT NOT NULL,
    Turno       NVARCHAR(20) NOT NULL,  -- DESAYUNO | ALMUERZO | CENA
    HoraInicio  TIME(0) NOT NULL,
    HoraFin     TIME(0) NOT NULL,
    Activo      BIT NOT NULL CONSTRAINT DF_SedeTurnos_Activo DEFAULT (1),
    CONSTRAINT FK_SedeTurnos_Sede FOREIGN KEY (IdSede) REFERENCES dbo.Sedes(IdSede),
    CONSTRAINT UQ_SedeTurnos UNIQUE (IdSede, Turno),
    CONSTRAINT CK_SedeTurnos_Turno CHECK (Turno IN (N'DESAYUNO', N'ALMUERZO', N'CENA'))
);
GO

/* ---------------------------------------------------------
   3. Ubigeo y direcciones
   --------------------------------------------------------- */

CREATE TABLE dbo.UbigeoDepartamentos (
    IdDepartamento CHAR(2) NOT NULL CONSTRAINT PK_UbigeoDepartamentos PRIMARY KEY, -- INEI
    Nombre         NVARCHAR(100) NOT NULL
);
GO

CREATE TABLE dbo.UbigeoProvincias (
    IdProvincia    CHAR(4) NOT NULL CONSTRAINT PK_UbigeoProvincias PRIMARY KEY,
    IdDepartamento CHAR(2) NOT NULL,
    Nombre         NVARCHAR(100) NOT NULL,
    CONSTRAINT FK_Provincias_Departamento FOREIGN KEY (IdDepartamento)
        REFERENCES dbo.UbigeoDepartamentos(IdDepartamento)
);
GO

CREATE TABLE dbo.UbigeoDistritos (
    IdDistrito  CHAR(6) NOT NULL CONSTRAINT PK_UbigeoDistritos PRIMARY KEY,
    IdProvincia CHAR(4) NOT NULL,
    Nombre      NVARCHAR(100) NOT NULL,
    LatCentro   DECIMAL(10,7) NULL,
    LngCentro   DECIMAL(10,7) NULL,
    CONSTRAINT FK_Distritos_Provincia FOREIGN KEY (IdProvincia)
        REFERENCES dbo.UbigeoProvincias(IdProvincia)
);
GO

CREATE TABLE dbo.DireccionesCliente (
    IdDireccion   INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_DireccionesCliente PRIMARY KEY,
    IdCliente     INT NOT NULL,
    IdDistrito    CHAR(6) NOT NULL,
    Alias         NVARCHAR(50)  NULL,
    Urbanizacion  NVARCHAR(120) NULL,
    Calle         NVARCHAR(200) NOT NULL,
    Numero        NVARCHAR(30)  NULL,
    Referencia    NVARCHAR(300) NULL,
    Lat           DECIMAL(10,7) NULL,
    Lng           DECIMAL(10,7) NULL,
    EsPrincipal   BIT NOT NULL CONSTRAINT DF_Dir_Principal DEFAULT (0),
    Activa        BIT NOT NULL CONSTRAINT DF_Dir_Activa DEFAULT (1),
    FechaCreacion DATETIME2(0) NOT NULL CONSTRAINT DF_Dir_Fecha DEFAULT (SYSUTCDATETIME()),
    CONSTRAINT FK_Dir_Cliente FOREIGN KEY (IdCliente) REFERENCES dbo.Clientes(IdCliente),
    CONSTRAINT FK_Dir_Distrito FOREIGN KEY (IdDistrito) REFERENCES dbo.UbigeoDistritos(IdDistrito)
);
GO

CREATE INDEX IX_Direcciones_Cliente ON dbo.DireccionesCliente(IdCliente, Activa);
GO

/* ---------------------------------------------------------
   4. Catálogo, recetas, stock
   --------------------------------------------------------- */

CREATE TABLE dbo.Categorias (
    IdCategoria INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_Categorias PRIMARY KEY,
    Nombre      NVARCHAR(100) NOT NULL,
    Descripcion NVARCHAR(300) NULL,
    Activo      BIT NOT NULL CONSTRAINT DF_Categorias_Activo DEFAULT (1),
    CONSTRAINT UQ_Categorias_Nombre UNIQUE (Nombre)
);
GO

CREATE TABLE dbo.Regiones (
    IdRegion    INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_Regiones PRIMARY KEY,
    Nombre      NVARCHAR(50) NOT NULL,  -- COSTA, SIERRA, SELVA
    Descripcion NVARCHAR(300) NULL,
    Activo      BIT NOT NULL CONSTRAINT DF_Regiones_Activo DEFAULT (1),
    CONSTRAINT UQ_Regiones_Nombre UNIQUE (Nombre)
);
GO

CREATE TABLE dbo.Productos (
    IdProducto            INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_Productos PRIMARY KEY,
    IdCategoria           INT NOT NULL,
    IdRegion              INT NULL,
    Nombre                NVARCHAR(150) NOT NULL,
    Descripcion           NVARCHAR(1000) NULL,
    PrecioBase            DECIMAL(10,2) NOT NULL,
    ImagenUrl             NVARCHAR(500) NULL,
    TiempoPreparacionMin  INT NULL,
    StockIlimitado        BIT NOT NULL CONSTRAINT DF_Prod_StockIlim DEFAULT (0),
    Activo                BIT NOT NULL CONSTRAINT DF_Prod_Activo DEFAULT (1),
    FechaRegistro         DATETIME2(0) NOT NULL CONSTRAINT DF_Prod_Fecha DEFAULT (SYSUTCDATETIME()),
    CONSTRAINT FK_Prod_Categoria FOREIGN KEY (IdCategoria) REFERENCES dbo.Categorias(IdCategoria),
    CONSTRAINT FK_Prod_Region FOREIGN KEY (IdRegion) REFERENCES dbo.Regiones(IdRegion),
    CONSTRAINT CK_Prod_Precio CHECK (PrecioBase >= 0)
);
GO

CREATE TABLE dbo.Insumos (
    IdInsumo           INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_Insumos PRIMARY KEY,
    Nombre             NVARCHAR(150) NOT NULL,
    Unidad             NVARCHAR(20)  NOT NULL, -- kg, g, L, ml, und
    StockIlimitado     BIT NOT NULL CONSTRAINT DF_Insumo_Ilim DEFAULT (0),
    StockMinimoAlerta  DECIMAL(12,4) NULL,
    Activo             BIT NOT NULL CONSTRAINT DF_Insumo_Activo DEFAULT (1),
    CONSTRAINT UQ_Insumos_Nombre UNIQUE (Nombre)
);
GO

CREATE TABLE dbo.Recetas (
    IdReceta    INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_Recetas PRIMARY KEY,
    IdProducto  INT NOT NULL,
    Version     INT NOT NULL CONSTRAINT DF_Receta_Version DEFAULT (1),
    Activa      BIT NOT NULL CONSTRAINT DF_Receta_Activa DEFAULT (1),
    CONSTRAINT UQ_Recetas_Producto UNIQUE (IdProducto),
    CONSTRAINT FK_Recetas_Producto FOREIGN KEY (IdProducto) REFERENCES dbo.Productos(IdProducto)
);
GO

CREATE TABLE dbo.RecetaDetalles (
    IdRecetaDetalle INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_RecetaDetalles PRIMARY KEY,
    IdReceta        INT NOT NULL,
    IdInsumo        INT NOT NULL,
    Cantidad        DECIMAL(12,4) NOT NULL,
    CONSTRAINT FK_RecDet_Receta FOREIGN KEY (IdReceta) REFERENCES dbo.Recetas(IdReceta),
    CONSTRAINT FK_RecDet_Insumo FOREIGN KEY (IdInsumo) REFERENCES dbo.Insumos(IdInsumo),
    CONSTRAINT UQ_Receta_Insumo UNIQUE (IdReceta, IdInsumo),
    CONSTRAINT CK_RecDet_Cantidad CHECK (Cantidad > 0)
);
GO

CREATE TABLE dbo.StockInsumos (
    IdStockInsumo       INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_StockInsumos PRIMARY KEY,
    IdSede              INT NOT NULL,
    IdInsumo            INT NOT NULL,
    CantidadDisponible  DECIMAL(12,4) NOT NULL CONSTRAINT DF_Stock_Disp DEFAULT (0),
    CantidadReservada   DECIMAL(12,4) NOT NULL CONSTRAINT DF_Stock_Res DEFAULT (0),
    CostoPromedio       DECIMAL(12,4) NULL,
    UltimoCosto         DECIMAL(12,4) NULL,
    StockMinimoAlerta   DECIMAL(12,4) NULL,
    Version             INT NOT NULL CONSTRAINT DF_Stock_Version DEFAULT (0),
    CONSTRAINT FK_Stock_Sede FOREIGN KEY (IdSede) REFERENCES dbo.Sedes(IdSede),
    CONSTRAINT FK_Stock_Insumo FOREIGN KEY (IdInsumo) REFERENCES dbo.Insumos(IdInsumo),
    CONSTRAINT UQ_Stock_Sede_Insumo UNIQUE (IdSede, IdInsumo),
    CONSTRAINT CK_Stock_Disp CHECK (CantidadDisponible >= 0),
    CONSTRAINT CK_Stock_Res CHECK (CantidadReservada >= 0)
);
GO

CREATE TABLE dbo.MovimientosStock (
    IdMovimiento   INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_MovimientosStock PRIMARY KEY,
    IdSede         INT NOT NULL,
    IdInsumo       INT NOT NULL,
    Tipo           NVARCHAR(20) NOT NULL,
    Cantidad       DECIMAL(12,4) NOT NULL,
    CostoUnitario  DECIMAL(12,4) NULL,
    IdPedido       INT NULL, -- FK se agrega después de Pedidos
    IdUsuario      INT NULL,
    Observacion    NVARCHAR(400) NULL,
    Fecha          DATETIME2(0) NOT NULL CONSTRAINT DF_MovStock_Fecha DEFAULT (SYSUTCDATETIME()),
    CONSTRAINT FK_MovStock_Sede FOREIGN KEY (IdSede) REFERENCES dbo.Sedes(IdSede),
    CONSTRAINT FK_MovStock_Insumo FOREIGN KEY (IdInsumo) REFERENCES dbo.Insumos(IdInsumo),
    CONSTRAINT FK_MovStock_Usuario FOREIGN KEY (IdUsuario) REFERENCES dbo.Usuarios(IdUsuario),
    CONSTRAINT CK_MovStock_Tipo CHECK (Tipo IN (
        N'ENTRADA', N'SALIDA', N'AJUSTE', N'RESERVA', N'CONSUMO', N'LIBERACION'
    )),
    CONSTRAINT CK_MovStock_Cantidad CHECK (Cantidad > 0)
);
GO

CREATE INDEX IX_MovStock_Sede_Insumo_Fecha
    ON dbo.MovimientosStock(IdSede, IdInsumo, Fecha DESC);
GO

/* ---------------------------------------------------------
   5. Menú del día
   --------------------------------------------------------- */

CREATE TABLE dbo.MenusDia (
    IdMenuDia              INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_MenusDia PRIMARY KEY,
    IdSede                 INT NOT NULL,
    Fecha                  DATE NOT NULL,
    Estado                 NVARCHAR(20) NOT NULL CONSTRAINT DF_MenuDia_Estado DEFAULT (N'BORRADOR'),
    IdUsuarioPublicacion   INT NULL,
    FechaPublicacion       DATETIME2(0) NULL,
    CONSTRAINT FK_MenuDia_Sede FOREIGN KEY (IdSede) REFERENCES dbo.Sedes(IdSede),
    CONSTRAINT FK_MenuDia_Usuario FOREIGN KEY (IdUsuarioPublicacion) REFERENCES dbo.Usuarios(IdUsuario),
    CONSTRAINT UQ_MenuDia_Sede_Fecha UNIQUE (IdSede, Fecha),
    CONSTRAINT CK_MenuDia_Estado CHECK (Estado IN (N'BORRADOR', N'PUBLICADO', N'CERRADO'))
);
GO

CREATE TABLE dbo.MenuDiaItems (
    IdMenuDiaItem   INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_MenuDiaItems PRIMARY KEY,
    IdMenuDia       INT NOT NULL,
    IdProducto      INT NOT NULL,
    Disponible      BIT NOT NULL CONSTRAINT DF_MenuItem_Disp DEFAULT (1),
    PrecioDia       DECIMAL(10,2) NULL,
    CupoMaximo      INT NULL,
    CantidadVendida INT NOT NULL CONSTRAINT DF_MenuItem_Vend DEFAULT (0),
    CONSTRAINT FK_MenuItem_Menu FOREIGN KEY (IdMenuDia) REFERENCES dbo.MenusDia(IdMenuDia),
    CONSTRAINT FK_MenuItem_Producto FOREIGN KEY (IdProducto) REFERENCES dbo.Productos(IdProducto),
    CONSTRAINT UQ_MenuItem UNIQUE (IdMenuDia, IdProducto),
    CONSTRAINT CK_MenuItem_Precio CHECK (PrecioDia IS NULL OR PrecioDia >= 0)
);
GO

CREATE TABLE dbo.MenuDiaItemTurnos (
    IdMenuDiaItem INT NOT NULL,
    IdSedeTurno   INT NOT NULL,
    CONSTRAINT PK_MenuDiaItemTurnos PRIMARY KEY (IdMenuDiaItem, IdSedeTurno),
    CONSTRAINT FK_MIT_Item FOREIGN KEY (IdMenuDiaItem) REFERENCES dbo.MenuDiaItems(IdMenuDiaItem),
    CONSTRAINT FK_MIT_Turno FOREIGN KEY (IdSedeTurno) REFERENCES dbo.SedeTurnos(IdSedeTurno)
);
GO

/* ---------------------------------------------------------
   6. Promociones y cupones
   --------------------------------------------------------- */

CREATE TABLE dbo.Promociones (
    IdPromocion      INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_Promociones PRIMARY KEY,
    Nombre           NVARCHAR(150) NOT NULL,
    Tipo             NVARCHAR(30)  NOT NULL,
    Valor            DECIMAL(10,2) NULL,
    LlevaN           INT NULL,
    PagaM            INT NULL,
    IdSede           INT NULL,
    FechaHoraInicio  DATETIME2(0) NOT NULL,
    FechaHoraFin     DATETIME2(0) NOT NULL,
    CupoGlobal       INT NULL,
    UsosActuales     INT NOT NULL CONSTRAINT DF_Promo_Usos DEFAULT (0),
    Combinable       BIT NOT NULL CONSTRAINT DF_Promo_Comb DEFAULT (1),
    RequiereCodigo   BIT NOT NULL CONSTRAINT DF_Promo_Codigo DEFAULT (0),
    Prioridad        INT NOT NULL CONSTRAINT DF_Promo_Prio DEFAULT (100),
    Activa           BIT NOT NULL CONSTRAINT DF_Promo_Activa DEFAULT (1),
    CONSTRAINT FK_Promo_Sede FOREIGN KEY (IdSede) REFERENCES dbo.Sedes(IdSede),
    CONSTRAINT CK_Promo_Tipo CHECK (Tipo IN (
        N'PORCENTAJE_PRODUCTO', N'MONTO_PRODUCTO', N'NX_M',
        N'CUPON', N'HAPPY_HOUR', N'DELIVERY_GRATIS'
    )),
    CONSTRAINT CK_Promo_Fechas CHECK (FechaHoraFin > FechaHoraInicio)
);
GO

CREATE TABLE dbo.PromocionProductos (
    IdPromocion INT NOT NULL,
    IdProducto  INT NOT NULL,
    CONSTRAINT PK_PromocionProductos PRIMARY KEY (IdPromocion, IdProducto),
    CONSTRAINT FK_PP_Promo FOREIGN KEY (IdPromocion) REFERENCES dbo.Promociones(IdPromocion),
    CONSTRAINT FK_PP_Prod FOREIGN KEY (IdProducto) REFERENCES dbo.Productos(IdProducto)
);
GO

CREATE TABLE dbo.PromocionCategorias (
    IdPromocion INT NOT NULL,
    IdCategoria INT NOT NULL,
    CONSTRAINT PK_PromocionCategorias PRIMARY KEY (IdPromocion, IdCategoria),
    CONSTRAINT FK_PC_Promo FOREIGN KEY (IdPromocion) REFERENCES dbo.Promociones(IdPromocion),
    CONSTRAINT FK_PC_Cat FOREIGN KEY (IdCategoria) REFERENCES dbo.Categorias(IdCategoria)
);
GO

CREATE TABLE dbo.PromocionExclusiones (
    IdPromocion         INT NOT NULL,
    IdPromocionExcluida INT NOT NULL,
    CONSTRAINT PK_PromocionExclusiones PRIMARY KEY (IdPromocion, IdPromocionExcluida),
    CONSTRAINT FK_PE_Promo FOREIGN KEY (IdPromocion) REFERENCES dbo.Promociones(IdPromocion),
    CONSTRAINT FK_PE_Excl FOREIGN KEY (IdPromocionExcluida) REFERENCES dbo.Promociones(IdPromocion),
    CONSTRAINT CK_PE_Diff CHECK (IdPromocion <> IdPromocionExcluida)
);
GO

CREATE TABLE dbo.Cupones (
    IdCupon           INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_Cupones PRIMARY KEY,
    IdPromocion       INT NOT NULL,
    Codigo            NVARCHAR(40) NOT NULL,
    UsoMaxGlobal      INT NULL,
    UsoMaxPorCliente  INT NULL,
    UsosActuales      INT NOT NULL CONSTRAINT DF_Cupon_Usos DEFAULT (0),
    Activo            BIT NOT NULL CONSTRAINT DF_Cupon_Activo DEFAULT (1),
    CONSTRAINT UQ_Cupones_Codigo UNIQUE (Codigo),
    CONSTRAINT FK_Cupon_Promo FOREIGN KEY (IdPromocion) REFERENCES dbo.Promociones(IdPromocion)
);
GO

CREATE TABLE dbo.PromocionAuditoria (
    IdAuditoria   INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_PromocionAuditoria PRIMARY KEY,
    IdPromocion   INT NOT NULL,
    IdUsuario     INT NOT NULL,
    Campo         NVARCHAR(100) NOT NULL,
    ValorAnterior NVARCHAR(500) NULL,
    ValorNuevo    NVARCHAR(500) NULL,
    Fecha         DATETIME2(0) NOT NULL CONSTRAINT DF_PromoAud_Fecha DEFAULT (SYSUTCDATETIME()),
    CONSTRAINT FK_PromoAud_Promo FOREIGN KEY (IdPromocion) REFERENCES dbo.Promociones(IdPromocion),
    CONSTRAINT FK_PromoAud_Usuario FOREIGN KEY (IdUsuario) REFERENCES dbo.Usuarios(IdUsuario)
);
GO

/* ---------------------------------------------------------
   7. Pedidos
   --------------------------------------------------------- */

CREATE TABLE dbo.EstadosPedido (
    IdEstadoPedido INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_EstadosPedido PRIMARY KEY,
    Nombre         NVARCHAR(30) NOT NULL,
    Descripcion    NVARCHAR(200) NULL,
    CONSTRAINT UQ_EstadosPedido_Nombre UNIQUE (Nombre)
);
GO

CREATE TABLE dbo.MetodosPago (
    IdMetodoPago INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_MetodosPago PRIMARY KEY,
    Nombre       NVARCHAR(50) NOT NULL,
    Activo       BIT NOT NULL CONSTRAINT DF_MetodosPago_Activo DEFAULT (1),
    CONSTRAINT UQ_MetodosPago_Nombre UNIQUE (Nombre)
);
GO

CREATE TABLE dbo.Pedidos (
    IdPedido         INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_Pedidos PRIMARY KEY,
    IdCliente        INT NOT NULL,
    IdDireccion      INT NOT NULL,
    IdSede           INT NOT NULL,
    IdEstadoPedido   INT NOT NULL,
    FechaPedido      DATETIME2(0) NOT NULL CONSTRAINT DF_Pedidos_Fecha DEFAULT (SYSUTCDATETIME()),
    Subtotal         DECIMAL(10,2) NOT NULL,
    DescuentoTotal   DECIMAL(10,2) NOT NULL CONSTRAINT DF_Pedidos_Desc DEFAULT (0),
    CostoDelivery    DECIMAL(10,2) NOT NULL CONSTRAINT DF_Pedidos_Del DEFAULT (0),
    Total            DECIMAL(10,2) NOT NULL,
    DistanciaKm      DECIMAL(8,2) NULL,
    Observaciones    NVARCHAR(500) NULL,
    CodigoCupon      NVARCHAR(40) NULL,
    CONSTRAINT FK_Pedidos_Cliente FOREIGN KEY (IdCliente) REFERENCES dbo.Clientes(IdCliente),
    CONSTRAINT FK_Pedidos_Direccion FOREIGN KEY (IdDireccion) REFERENCES dbo.DireccionesCliente(IdDireccion),
    CONSTRAINT FK_Pedidos_Sede FOREIGN KEY (IdSede) REFERENCES dbo.Sedes(IdSede),
    CONSTRAINT FK_Pedidos_Estado FOREIGN KEY (IdEstadoPedido) REFERENCES dbo.EstadosPedido(IdEstadoPedido),
    CONSTRAINT CK_Pedidos_Montos CHECK (
        Subtotal >= 0 AND DescuentoTotal >= 0 AND CostoDelivery >= 0 AND Total >= 0
    )
);
GO

CREATE INDEX IX_Pedidos_Cliente_Fecha ON dbo.Pedidos(IdCliente, FechaPedido DESC);
CREATE INDEX IX_Pedidos_Sede_Estado ON dbo.Pedidos(IdSede, IdEstadoPedido, FechaPedido DESC);
GO

CREATE TABLE dbo.DetallePedidos (
    IdDetallePedido     INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_DetallePedidos PRIMARY KEY,
    IdPedido            INT NOT NULL,
    IdProducto          INT NOT NULL,
    IdMenuDiaItem       INT NULL,
    Cantidad            INT NOT NULL,
    PrecioLista         DECIMAL(10,2) NOT NULL,
    PrecioUnitarioFinal DECIMAL(10,2) NOT NULL,
    DescuentoUnitario   DECIMAL(10,2) NOT NULL CONSTRAINT DF_Det_Desc DEFAULT (0),
    IdPromocion         INT NULL,
    Subtotal            DECIMAL(10,2) NOT NULL,
    CONSTRAINT FK_Det_Pedido FOREIGN KEY (IdPedido) REFERENCES dbo.Pedidos(IdPedido),
    CONSTRAINT FK_Det_Producto FOREIGN KEY (IdProducto) REFERENCES dbo.Productos(IdProducto),
    CONSTRAINT FK_Det_MenuItem FOREIGN KEY (IdMenuDiaItem) REFERENCES dbo.MenuDiaItems(IdMenuDiaItem),
    CONSTRAINT FK_Det_Promo FOREIGN KEY (IdPromocion) REFERENCES dbo.Promociones(IdPromocion),
    CONSTRAINT CK_Det_Cantidad CHECK (Cantidad > 0)
);
GO

CREATE TABLE dbo.PedidoDescuentos (
    IdPedidoDescuento INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_PedidoDescuentos PRIMARY KEY,
    IdPedido          INT NOT NULL,
    Tipo              NVARCHAR(30) NOT NULL,
    IdPromocion       INT NULL,
    IdCupon           INT NULL,
    Monto             DECIMAL(10,2) NOT NULL,
    Descripcion       NVARCHAR(200) NULL,
    CONSTRAINT FK_PedDesc_Pedido FOREIGN KEY (IdPedido) REFERENCES dbo.Pedidos(IdPedido),
    CONSTRAINT FK_PedDesc_Promo FOREIGN KEY (IdPromocion) REFERENCES dbo.Promociones(IdPromocion),
    CONSTRAINT FK_PedDesc_Cupon FOREIGN KEY (IdCupon) REFERENCES dbo.Cupones(IdCupon),
    CONSTRAINT CK_PedDesc_Monto CHECK (Monto >= 0)
);
GO

CREATE TABLE dbo.CuponUsos (
    IdCuponUso INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_CuponUsos PRIMARY KEY,
    IdCupon    INT NOT NULL,
    IdCliente  INT NOT NULL,
    IdPedido   INT NOT NULL,
    Fecha      DATETIME2(0) NOT NULL CONSTRAINT DF_CuponUso_Fecha DEFAULT (SYSUTCDATETIME()),
    CONSTRAINT FK_CuponUso_Cupon FOREIGN KEY (IdCupon) REFERENCES dbo.Cupones(IdCupon),
    CONSTRAINT FK_CuponUso_Cliente FOREIGN KEY (IdCliente) REFERENCES dbo.Clientes(IdCliente),
    CONSTRAINT FK_CuponUso_Pedido FOREIGN KEY (IdPedido) REFERENCES dbo.Pedidos(IdPedido)
);
GO

CREATE TABLE dbo.Pagos (
    IdPago             INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_Pagos PRIMARY KEY,
    IdPedido           INT NOT NULL,
    IdMetodoPago       INT NOT NULL,
    Monto              DECIMAL(10,2) NOT NULL,
    Estado             NVARCHAR(20) NOT NULL CONSTRAINT DF_Pagos_Estado DEFAULT (N'PENDIENTE'),
    FechaRegistro      DATETIME2(0) NOT NULL CONSTRAINT DF_Pagos_Fecha DEFAULT (SYSUTCDATETIME()),
    ReferenciaExterna  NVARCHAR(100) NULL,
    CONSTRAINT FK_Pagos_Pedido FOREIGN KEY (IdPedido) REFERENCES dbo.Pedidos(IdPedido),
    CONSTRAINT FK_Pagos_Metodo FOREIGN KEY (IdMetodoPago) REFERENCES dbo.MetodosPago(IdMetodoPago),
    CONSTRAINT CK_Pagos_Estado CHECK (Estado IN (N'PENDIENTE', N'PAGADO', N'FALLIDO', N'REEMBOLSADO'))
);
GO

CREATE TABLE dbo.HistorialPedidos (
    IdHistorial    INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_HistorialPedidos PRIMARY KEY,
    IdPedido       INT NOT NULL,
    IdEstadoPedido INT NOT NULL,
    IdUsuario      INT NULL,
    Observacion    NVARCHAR(400) NULL,
    FechaCambio    DATETIME2(0) NOT NULL CONSTRAINT DF_Hist_Fecha DEFAULT (SYSUTCDATETIME()),
    CONSTRAINT FK_Hist_Pedido FOREIGN KEY (IdPedido) REFERENCES dbo.Pedidos(IdPedido),
    CONSTRAINT FK_Hist_Estado FOREIGN KEY (IdEstadoPedido) REFERENCES dbo.EstadosPedido(IdEstadoPedido),
    CONSTRAINT FK_Hist_Usuario FOREIGN KEY (IdUsuario) REFERENCES dbo.Usuarios(IdUsuario)
);
GO

CREATE TABLE dbo.ReservasStockPedido (
    IdReserva  INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_ReservasStockPedido PRIMARY KEY,
    IdPedido   INT NOT NULL,
    IdSede     INT NOT NULL,
    IdInsumo   INT NOT NULL,
    Cantidad   DECIMAL(12,4) NOT NULL,
    Estado     NVARCHAR(20) NOT NULL CONSTRAINT DF_Reserva_Estado DEFAULT (N'RESERVADA'),
    CONSTRAINT FK_Reserva_Pedido FOREIGN KEY (IdPedido) REFERENCES dbo.Pedidos(IdPedido),
    CONSTRAINT FK_Reserva_Sede FOREIGN KEY (IdSede) REFERENCES dbo.Sedes(IdSede),
    CONSTRAINT FK_Reserva_Insumo FOREIGN KEY (IdInsumo) REFERENCES dbo.Insumos(IdInsumo),
    CONSTRAINT CK_Reserva_Estado CHECK (Estado IN (N'RESERVADA', N'CONSUMIDA', N'LIBERADA')),
    CONSTRAINT CK_Reserva_Cant CHECK (Cantidad > 0)
);
GO

/* FK diferida de movimientos → pedidos */
ALTER TABLE dbo.MovimientosStock
    ADD CONSTRAINT FK_MovStock_Pedido FOREIGN KEY (IdPedido) REFERENCES dbo.Pedidos(IdPedido);
GO

/* ---------------------------------------------------------
   8. Repartidores y entregas
   --------------------------------------------------------- */

CREATE TABLE dbo.Repartidores (
    IdRepartidor INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_Repartidores PRIMARY KEY,
    IdUsuario    INT NOT NULL,
    IdSede       INT NOT NULL,
    Telefono     NVARCHAR(30) NULL,
    Vehiculo     NVARCHAR(50) NULL,
    Estado       NVARCHAR(20) NOT NULL CONSTRAINT DF_Rep_Estado DEFAULT (N'OFFLINE'),
    Activo       BIT NOT NULL CONSTRAINT DF_Rep_Activo DEFAULT (1),
    UltimaLat    DECIMAL(10,7) NULL,
    UltimaLng    DECIMAL(10,7) NULL,
    CONSTRAINT UQ_Repartidores_Usuario UNIQUE (IdUsuario),
    CONSTRAINT FK_Rep_Usuario FOREIGN KEY (IdUsuario) REFERENCES dbo.Usuarios(IdUsuario),
    CONSTRAINT FK_Rep_Sede FOREIGN KEY (IdSede) REFERENCES dbo.Sedes(IdSede),
    CONSTRAINT CK_Rep_Estado CHECK (Estado IN (N'OFFLINE', N'DISPONIBLE', N'EN_ENTREGA'))
);
GO

CREATE TABLE dbo.Entregas (
    IdEntrega        INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_Entregas PRIMARY KEY,
    IdPedido         INT NOT NULL,
    IdRepartidor     INT NULL,
    Estado           NVARCHAR(30) NOT NULL CONSTRAINT DF_Entrega_Estado DEFAULT (N'PENDIENTE_ASIGNACION'),
    FechaAsignacion  DATETIME2(0) NULL,
    FechaRecojo      DATETIME2(0) NULL,
    FechaEntrega     DATETIME2(0) NULL,
    Observacion      NVARCHAR(400) NULL,
    CONSTRAINT UQ_Entregas_Pedido UNIQUE (IdPedido),
    CONSTRAINT FK_Entrega_Pedido FOREIGN KEY (IdPedido) REFERENCES dbo.Pedidos(IdPedido),
    CONSTRAINT FK_Entrega_Repartidor FOREIGN KEY (IdRepartidor) REFERENCES dbo.Repartidores(IdRepartidor),
    CONSTRAINT CK_Entrega_Estado CHECK (Estado IN (
        N'PENDIENTE_ASIGNACION', N'ASIGNADA', N'EN_CAMINO', N'ENTREGADA', N'FALLIDA'
    ))
);
GO

CREATE INDEX IX_Entregas_Repartidor_Estado ON dbo.Entregas(IdRepartidor, Estado);
GO

/* ---------------------------------------------------------
   9. Config delivery por sede
   --------------------------------------------------------- */

CREATE TABLE dbo.ConfiguracionSede (
    IdSede                     INT NOT NULL CONSTRAINT PK_ConfiguracionSede PRIMARY KEY,
    DeliveryGratisDesdeMonto   DECIMAL(10,2) NULL,
    DeliveryGratisHastaKm      DECIMAL(8,2)  NULL,
    CostoDeliveryMinimo        DECIMAL(10,2) NULL,
    PedidoMinimo               DECIMAL(10,2) NULL,
    CONSTRAINT FK_Config_Sede FOREIGN KEY (IdSede) REFERENCES dbo.Sedes(IdSede)
);
GO

CREATE TABLE dbo.TarifasDelivery (
    IdTarifa  INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_TarifasDelivery PRIMARY KEY,
    IdSede    INT NOT NULL,
    KmDesde   DECIMAL(8,2) NOT NULL,
    KmHasta   DECIMAL(8,2) NOT NULL,
    Costo     DECIMAL(10,2) NOT NULL,
    CONSTRAINT FK_Tarifa_Sede FOREIGN KEY (IdSede) REFERENCES dbo.Sedes(IdSede),
    CONSTRAINT CK_Tarifa_Rango CHECK (KmHasta > KmDesde),
    CONSTRAINT CK_Tarifa_Costo CHECK (Costo >= 0)
);
GO

/* ---------------------------------------------------------
   10. Datos mínimos de catálogo (estados / métodos / roles)
   --------------------------------------------------------- */

INSERT INTO dbo.Roles (Nombre, Descripcion) VALUES
 (N'CLIENTE', N'Cliente final'),
 (N'ADMINISTRADOR', N'Administrador general'),
 (N'CHEF', N'Personal de cocina'),
 (N'REPARTIDOR', N'Repartidor'),
 (N'OPERACIONES', N'Menú, stock y promociones'),
 (N'SUPERVISOR', N'Supervisión y autorizaciones');
GO

INSERT INTO dbo.EstadosPedido (Nombre, Descripcion) VALUES
 (N'PENDIENTE', N'Pedido registrado'),
 (N'CONFIRMADO', N'Pedido confirmado'),
 (N'EN_PREPARACION', N'En cocina'),
 (N'LISTO', N'Listo para recojo'),
 (N'EN_CAMINO', N'En ruta de entrega'),
 (N'ENTREGADO', N'Entregado al cliente'),
 (N'CANCELADO', N'Cancelado');
GO

INSERT INTO dbo.MetodosPago (Nombre) VALUES
 (N'EFECTIVO'), (N'TARJETA'), (N'YAPE'), (N'PLIN'), (N'TRANSFERENCIA');
GO

INSERT INTO dbo.Regiones (Nombre, Descripcion) VALUES
 (N'COSTA', N'Gastronomía de la costa'),
 (N'SIERRA', N'Gastronomía de la sierra'),
 (N'SELVA', N'Gastronomía de la selva');
GO

INSERT INTO dbo.Categorias (Nombre) VALUES
 (N'ENTRADAS'), (N'PLATOS DE FONDO'), (N'POSTRES'), (N'BEBIDAS'), (N'COMBOS');
GO


-- Departamento Lima
IF NOT EXISTS (SELECT 1 FROM dbo.UbigeoDepartamentos WHERE IdDepartamento = '15')
INSERT INTO dbo.UbigeoDepartamentos (IdDepartamento, Nombre)
VALUES ('15', N'Lima');
GO

-- Provincia Lima
IF NOT EXISTS (SELECT 1 FROM dbo.UbigeoProvincias WHERE IdProvincia = '1501')
INSERT INTO dbo.UbigeoProvincias (IdProvincia, IdDepartamento, Nombre)
VALUES ('1501', '15', N'Lima');
GO

-- Distritos de prueba (cobertura típica)
IF NOT EXISTS (SELECT 1 FROM dbo.UbigeoDistritos WHERE IdDistrito = '150101')
INSERT INTO dbo.UbigeoDistritos (IdDistrito, IdProvincia, Nombre, LatCentro, LngCentro) VALUES
('150101', '1501', N'Lima',          -12.0464000, -77.0428000),
('150122', '1501', N'Miraflores',    -12.1211000, -77.0297000),
('150131', '1501', N'San Isidro',    -12.0972000, -77.0350000),
('150140', '1501', N'Surco',         -12.1400000, -76.9920000),  -- Santiago de Surco
('150130', '1501', N'San Borja',     -12.1070000, -76.9980000),
('150113', '1501', N'Jesús María',   -12.0780000, -77.0480000),
('150120', '1501', N'Magdalena',     -12.0920000, -77.0700000),
('150108', '1501', N'Barranco',      -12.1440000, -77.0220000),
('150114', '1501', N'La Molina',     -12.0780000, -76.9450000),
('150132', '1501', N'San Miguel',    -12.0780000, -77.0930000);
GO



DECLARE @IdSede INT;

IF NOT EXISTS (SELECT 1 FROM dbo.Sedes WHERE Nombre = N'Cocina Miraflores')
BEGIN
    INSERT INTO dbo.Sedes (Nombre, Direccion, Lat, Lng, RadioCoberturaKm, Activa, Telefono)
    VALUES (
        N'Cocina Miraflores',
        N'Av. Larco 600, Miraflores',
        -12.1211000,
        -77.0297000,
        8.00,          -- 8 km de radio
        1,
        N'01-400-0000'
    );
END

SELECT @IdSede = IdSede FROM dbo.Sedes WHERE Nombre = N'Cocina Miraflores';

-- Turnos globales de la sede
IF NOT EXISTS (SELECT 1 FROM dbo.SedeTurnos WHERE IdSede = @IdSede)
BEGIN
    INSERT INTO dbo.SedeTurnos (IdSede, Turno, HoraInicio, HoraFin, Activo) VALUES
    (@IdSede, N'DESAYUNO',  '07:00', '11:00', 1),
    (@IdSede, N'ALMUERZO',  '11:00', '16:00', 1),
    (@IdSede, N'CENA',      '16:00', '22:00', 1);
END

-- Config delivery
IF NOT EXISTS (SELECT 1 FROM dbo.ConfiguracionSede WHERE IdSede = @IdSede)
BEGIN
    INSERT INTO dbo.ConfiguracionSede (
        IdSede,
        DeliveryGratisDesdeMonto,
        DeliveryGratisHastaKm,
        CostoDeliveryMinimo,
        PedidoMinimo
    ) VALUES (
        @IdSede,
        60.00,   -- gratis desde S/ 60
        2.00,    -- gratis hasta 2 km
        5.00,    -- mínimo de delivery si cobra
        15.00    -- pedido mínimo
    );
END

-- Tarifas por tramo (si distancia no es gratis)
IF NOT EXISTS (SELECT 1 FROM dbo.TarifasDelivery WHERE IdSede = @IdSede)
BEGIN
    INSERT INTO dbo.TarifasDelivery (IdSede, KmDesde, KmHasta, Costo) VALUES
    (@IdSede, 0.00,  2.00,  0.00),   -- cubierto por regla gratis km
    (@IdSede, 2.00,  5.00,  6.00),
    (@IdSede, 5.00,  8.00, 10.00),
    (@IdSede, 8.00, 15.00, 15.00);
END
GO



DECLARE @IdSede INT = (SELECT TOP 1 IdSede FROM dbo.Sedes WHERE Nombre = N'Cocina Miraflores');
DECLARE @IdCat INT = (SELECT TOP 1 IdCategoria FROM dbo.Categorias WHERE Nombre = N'PLATOS DE FONDO');
DECLARE @IdReg INT = (SELECT TOP 1 IdRegion FROM dbo.Regiones WHERE Nombre = N'COSTA');

-- Insumos
IF NOT EXISTS (SELECT 1 FROM dbo.Insumos WHERE Nombre = N'Pescado blanco')
INSERT INTO dbo.Insumos (Nombre, Unidad, StockIlimitado, StockMinimoAlerta, Activo)
VALUES (N'Pescado blanco', N'kg', 0, 2.0000, 1);

IF NOT EXISTS (SELECT 1 FROM dbo.Insumos WHERE Nombre = N'Limón')
INSERT INTO dbo.Insumos (Nombre, Unidad, StockIlimitado, StockMinimoAlerta, Activo)
VALUES (N'Limón', N'kg', 0, 1.0000, 1);

IF NOT EXISTS (SELECT 1 FROM dbo.Insumos WHERE Nombre = N'Cebolla roja')
INSERT INTO dbo.Insumos (Nombre, Unidad, StockIlimitado, StockMinimoAlerta, Activo)
VALUES (N'Cebolla roja', N'kg', 0, 1.0000, 1);

DECLARE @IdPescado INT = (SELECT IdInsumo FROM dbo.Insumos WHERE Nombre = N'Pescado blanco');
DECLARE @IdLimon INT = (SELECT IdInsumo FROM dbo.Insumos WHERE Nombre = N'Limón');
DECLARE @IdCebolla INT = (SELECT IdInsumo FROM dbo.Insumos WHERE Nombre = N'Cebolla roja');

-- Stock sede
MERGE dbo.StockInsumos AS t
USING (SELECT @IdSede AS IdSede, @IdPescado AS IdInsumo) s
ON t.IdSede = s.IdSede AND t.IdInsumo = s.IdInsumo
WHEN NOT MATCHED THEN
  INSERT (IdSede, IdInsumo, CantidadDisponible, CantidadReservada, UltimoCosto, Version)
  VALUES (@IdSede, @IdPescado, 20.0000, 0, 25.00, 0);

MERGE dbo.StockInsumos AS t
USING (SELECT @IdSede AS IdSede, @IdLimon AS IdInsumo) s
ON t.IdSede = s.IdSede AND t.IdInsumo = s.IdInsumo
WHEN NOT MATCHED THEN
  INSERT (IdSede, IdInsumo, CantidadDisponible, CantidadReservada, UltimoCosto, Version)
  VALUES (@IdSede, @IdLimon, 10.0000, 0, 3.50, 0);

MERGE dbo.StockInsumos AS t
USING (SELECT @IdSede AS IdSede, @IdCebolla AS IdInsumo) s
ON t.IdSede = s.IdSede AND t.IdInsumo = s.IdInsumo
WHEN NOT MATCHED THEN
  INSERT (IdSede, IdInsumo, CantidadDisponible, CantidadReservada, UltimoCosto, Version)
  VALUES (@IdSede, @IdCebolla, 8.0000, 0, 2.00, 0);

-- Producto
IF NOT EXISTS (SELECT 1 FROM dbo.Productos WHERE Nombre = N'Ceviche clásico')
INSERT INTO dbo.Productos (
  IdCategoria, IdRegion, Nombre, Descripcion, PrecioBase,
  ImagenUrl, TiempoPreparacionMin, StockIlimitado, Activo, FechaRegistro
) VALUES (
  @IdCat, @IdReg,
  N'Ceviche clásico',
  N'Pescado fresco, limón y cebolla morada',
  32.00,
  NULL, 20, 0, 1, SYSUTCDATETIME()
);

DECLARE @IdProd INT = (SELECT IdProducto FROM dbo.Productos WHERE Nombre = N'Ceviche clásico');

-- Receta (por 1 plato)
IF NOT EXISTS (SELECT 1 FROM dbo.Recetas WHERE IdProducto = @IdProd)
BEGIN
  INSERT INTO dbo.Recetas (IdProducto, Version, Activa) VALUES (@IdProd, 1, 1);
  DECLARE @IdReceta INT = SCOPE_IDENTITY();
  INSERT INTO dbo.RecetaDetalles (IdReceta, IdInsumo, Cantidad) VALUES
    (@IdReceta, @IdPescado, 0.2500),
    (@IdReceta, @IdLimon,   0.1000),
    (@IdReceta, @IdCebolla, 0.0500);
END

-- Menú de hoy
DECLARE @Hoy DATE = CAST(GETDATE() AS DATE);

IF NOT EXISTS (SELECT 1 FROM dbo.MenusDia WHERE IdSede = @IdSede AND Fecha = @Hoy)
INSERT INTO dbo.MenusDia (IdSede, Fecha, Estado, FechaPublicacion)
VALUES (@IdSede, @Hoy, N'PUBLICADO', SYSUTCDATETIME());

DECLARE @IdMenu INT = (SELECT IdMenuDia FROM dbo.MenusDia WHERE IdSede = @IdSede AND Fecha = @Hoy);

IF NOT EXISTS (SELECT 1 FROM dbo.MenuDiaItems WHERE IdMenuDia = @IdMenu AND IdProducto = @IdProd)
INSERT INTO dbo.MenuDiaItems (IdMenuDia, IdProducto, Disponible, PrecioDia, CupoMaximo, CantidadVendida)
VALUES (@IdMenu, @IdProd, 1, 32.00, 50, 0);

DECLARE @IdMenuItem INT = (
  SELECT IdMenuDiaItem FROM dbo.MenuDiaItems WHERE IdMenuDia = @IdMenu AND IdProducto = @IdProd
);

-- Turnos del ítem (todos los de la sede)
INSERT INTO dbo.MenuDiaItemTurnos (IdMenuDiaItem, IdSedeTurno)
SELECT @IdMenuItem, st.IdSedeTurno
FROM dbo.SedeTurnos st
WHERE st.IdSede = @IdSede
  AND NOT EXISTS (
    SELECT 1 FROM dbo.MenuDiaItemTurnos x
    WHERE x.IdMenuDiaItem = @IdMenuItem AND x.IdSedeTurno = st.IdSedeTurno
  );

SELECT @IdSede AS IdSede, @IdProd AS IdProducto, @IdMenuItem AS IdMenuDiaItem, @Hoy AS FechaMenu;
GO



-- 20% en todos los productos (automática, sin código), 7 días
INSERT INTO dbo.Promociones (
  Nombre, Tipo, Valor, IdSede, FechaHoraInicio, FechaHoraFin,
  Combinable, RequiereCodigo, Prioridad, Activa, UsosActuales
) VALUES (
  N'Promo costa 20%', N'PORCENTAJE_PRODUCTO', 20.00, NULL,
  DATEADD(day, -1, SYSUTCDATETIME()), DATEADD(day, 30, SYSUTCDATETIME()),
  1, 0, 10, 1, 0
);

-- Cupón BIENVENIDO10 = 10% sobre subtotal
INSERT INTO dbo.Promociones (
  Nombre, Tipo, Valor, FechaHoraInicio, FechaHoraFin,
  Combinable, RequiereCodigo, Prioridad, Activa, UsosActuales
) VALUES (
  N'Cupón bienvenida', N'CUPON', 10.00,
  DATEADD(day, -1, SYSUTCDATETIME()), DATEADD(day, 90, SYSUTCDATETIME()),
  1, 1, 50, 1, 0
);

DECLARE @IdPromoCupon INT = SCOPE_IDENTITY();

INSERT INTO dbo.Cupones (IdPromocion, Codigo, UsoMaxGlobal, UsoMaxPorCliente, UsosActuales, Activo)
VALUES (@IdPromoCupon, N'BIENVENIDO10', 1000, 1, 0, 1);

-- Delivery gratis desde S/ 50 (además de reglas de sede)
INSERT INTO dbo.Promociones (
  Nombre, Tipo, Valor, FechaHoraInicio, FechaHoraFin,
  Combinable, RequiereCodigo, Prioridad, Activa, UsosActuales
) VALUES (
  N'Delivery gratis S/50', N'DELIVERY_GRATIS', 50.00,
  DATEADD(day, -1, SYSUTCDATETIME()), DATEADD(day, 365, SYSUTCDATETIME()),
  1, 0, 20, 1, 0
);


