/*
  Sabores Perú – Schema v3 (English, PascalCase)
  SQL Server – create from scratch
*/
USE master;
GO

IF DB_ID(N'SaboresPeru') IS NULL
BEGIN
    CREATE DATABASE SaboresPeru;
END
GO

USE SaboresPeru;
GO

SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
GO

/* =========================================================
   1. Security – users & roles
   ========================================================= */

CREATE TABLE dbo.Roles (
    IdRole          INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_Roles PRIMARY KEY,
    Name            NVARCHAR(50)  NOT NULL,
    Description     NVARCHAR(200) NULL,
    IsActive        BIT NOT NULL CONSTRAINT DF_Roles_IsActive DEFAULT (1),
    CreatedAt       DATETIME2(0) NOT NULL CONSTRAINT DF_Roles_CreatedAt DEFAULT (SYSUTCDATETIME()),
    CONSTRAINT UQ_Roles_Name UNIQUE (Name)
);
GO

CREATE TABLE dbo.Users (
    IdUser          INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_Users PRIMARY KEY,
    FirstName       NVARCHAR(100) NOT NULL,
    LastName        NVARCHAR(100) NOT NULL,
    Email           NVARCHAR(150) NOT NULL,
    PasswordHash    NVARCHAR(255) NOT NULL,
    Phone           NVARCHAR(30)  NULL,
    IsActive        BIT NOT NULL CONSTRAINT DF_Users_IsActive DEFAULT (1),
    CreatedAt       DATETIME2(0) NOT NULL CONSTRAINT DF_Users_CreatedAt DEFAULT (SYSUTCDATETIME()),
    LastAccessAt    DATETIME2(0) NULL,
    LastIp          NVARCHAR(45)  NULL,
    LastUserAgent   NVARCHAR(500) NULL,
    CONSTRAINT UQ_Users_Email UNIQUE (Email)
);
GO

CREATE TABLE dbo.UserRoles (
    IdUser INT NOT NULL,
    IdRole INT NOT NULL,
    CONSTRAINT PK_UserRoles PRIMARY KEY (IdUser, IdRole),
    CONSTRAINT FK_UserRoles_User FOREIGN KEY (IdUser) REFERENCES dbo.Users(IdUser),
    CONSTRAINT FK_UserRoles_Role FOREIGN KEY (IdRole) REFERENCES dbo.Roles(IdRole)
);
GO

CREATE TABLE dbo.Customers (
    IdCustomer      INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_Customers PRIMARY KEY,
    IdUser          INT NOT NULL,
    DocumentType    NVARCHAR(20)  NULL,
    DocumentNumber  NVARCHAR(30)  NULL,
    CreatedAt       DATETIME2(0) NOT NULL CONSTRAINT DF_Customers_CreatedAt DEFAULT (SYSUTCDATETIME()),
    CONSTRAINT UQ_Customers_User UNIQUE (IdUser),
    CONSTRAINT FK_Customers_User FOREIGN KEY (IdUser) REFERENCES dbo.Users(IdUser)
);
GO

/* =========================================================
   2. Branches & shifts
   ========================================================= */

CREATE TABLE dbo.Branches (
    IdBranch            INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_Branches PRIMARY KEY,
    Name                NVARCHAR(120) NOT NULL,
    Address             NVARCHAR(300) NOT NULL,
    Lat                 DECIMAL(10,7) NOT NULL,
    Lng                 DECIMAL(10,7) NOT NULL,
    CoverageRadiusKm    DECIMAL(8,2)  NOT NULL CONSTRAINT DF_Branches_Radius DEFAULT (5.00),
    IsActive            BIT NOT NULL CONSTRAINT DF_Branches_IsActive DEFAULT (1),
    TenantKey           NVARCHAR(50)  NULL,
    Phone               NVARCHAR(30)  NULL,
    CreatedAt           DATETIME2(0) NOT NULL CONSTRAINT DF_Branches_CreatedAt DEFAULT (SYSUTCDATETIME())
);
GO

CREATE TABLE dbo.BranchShifts (
    IdBranchShift   INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_BranchShifts PRIMARY KEY,
    IdBranch        INT NOT NULL,
    Shift           NVARCHAR(20) NOT NULL,  -- BREAKFAST | LUNCH | DINNER
    StartTime       TIME(0) NOT NULL,
    EndTime         TIME(0) NOT NULL,
    IsActive        BIT NOT NULL CONSTRAINT DF_BranchShifts_IsActive DEFAULT (1),
    CONSTRAINT FK_BranchShifts_Branch FOREIGN KEY (IdBranch) REFERENCES dbo.Branches(IdBranch),
    CONSTRAINT UQ_BranchShifts UNIQUE (IdBranch, Shift),
    CONSTRAINT CK_BranchShifts_Shift CHECK (Shift IN (N'BREAKFAST', N'LUNCH', N'DINNER'))
);
GO

CREATE TABLE dbo.BranchSettings (
    IdBranchSetting INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_BranchSettings PRIMARY KEY,
    IdBranch        INT NOT NULL,
    SettingKey      NVARCHAR(80) NOT NULL,
    SettingValue    NVARCHAR(500) NULL,
    CONSTRAINT FK_BranchSettings_Branch FOREIGN KEY (IdBranch) REFERENCES dbo.Branches(IdBranch),
    CONSTRAINT UQ_BranchSettings UNIQUE (IdBranch, SettingKey)
);
GO

CREATE TABLE dbo.BranchConfigs (
    IdBranch                INT NOT NULL CONSTRAINT PK_BranchConfigs PRIMARY KEY,
    MinOrderAmount          DECIMAL(10,2) NULL,
    DefaultDeliveryFee      DECIMAL(10,2) NULL,
    FreeDeliveryFromAmount  DECIMAL(10,2) NULL,
    AvgPrepTimeMinutes      INT NULL,
    AcceptsOnlineOrders     BIT NOT NULL CONSTRAINT DF_BranchConfigs_Online DEFAULT (1),
    OpenTime                TIME(0) NULL,
    CloseTime               TIME(0) NULL,
    UpdatedAt               DATETIME2(0) NOT NULL CONSTRAINT DF_BranchConfigs_UpdatedAt DEFAULT (SYSUTCDATETIME()),
    CONSTRAINT FK_BranchConfigs_Branch FOREIGN KEY (IdBranch) REFERENCES dbo.Branches(IdBranch)
);
GO

/* =========================================================
   3. Geo & customer addresses
   ========================================================= */

CREATE TABLE dbo.GeoDepartments (
    IdDepartment    CHAR(2) NOT NULL CONSTRAINT PK_GeoDepartments PRIMARY KEY,
    Name            NVARCHAR(100) NOT NULL
);
GO

CREATE TABLE dbo.GeoProvinces (
    IdProvince      CHAR(4) NOT NULL CONSTRAINT PK_GeoProvinces PRIMARY KEY,
    IdDepartment    CHAR(2) NOT NULL,
    Name            NVARCHAR(100) NOT NULL,
    CONSTRAINT FK_GeoProvinces_Department FOREIGN KEY (IdDepartment)
        REFERENCES dbo.GeoDepartments(IdDepartment)
);
GO

CREATE TABLE dbo.GeoDistricts (
    IdDistrict      CHAR(6) NOT NULL CONSTRAINT PK_GeoDistricts PRIMARY KEY,
    IdProvince      CHAR(4) NOT NULL,
    Name            NVARCHAR(100) NOT NULL,
    CenterLat       DECIMAL(10,7) NULL,
    CenterLng       DECIMAL(10,7) NULL,
    CONSTRAINT FK_GeoDistricts_Province FOREIGN KEY (IdProvince)
        REFERENCES dbo.GeoProvinces(IdProvince)
);
GO

CREATE TABLE dbo.CustomerAddresses (
    IdAddress       INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_CustomerAddresses PRIMARY KEY,
    IdCustomer      INT NOT NULL,
    IdDistrict      CHAR(6) NOT NULL,
    Alias           NVARCHAR(50)  NULL,
    Urbanization    NVARCHAR(120) NULL,
    Street          NVARCHAR(200) NOT NULL,
    Number          NVARCHAR(30)  NULL,
    Reference       NVARCHAR(300) NULL,
    Lat             DECIMAL(10,7) NULL,
    Lng             DECIMAL(10,7) NULL,
    IsPrimary       BIT NOT NULL CONSTRAINT DF_CustomerAddresses_IsPrimary DEFAULT (0),
    IsActive        BIT NOT NULL CONSTRAINT DF_CustomerAddresses_IsActive DEFAULT (1),
    CreatedAt       DATETIME2(0) NOT NULL CONSTRAINT DF_CustomerAddresses_CreatedAt DEFAULT (SYSUTCDATETIME()),
    CONSTRAINT FK_CustomerAddresses_Customer FOREIGN KEY (IdCustomer) REFERENCES dbo.Customers(IdCustomer),
    CONSTRAINT FK_CustomerAddresses_District FOREIGN KEY (IdDistrict) REFERENCES dbo.GeoDistricts(IdDistrict)
);
GO

CREATE INDEX IX_CustomerAddresses_Customer ON dbo.CustomerAddresses(IdCustomer, IsActive);
GO

/* =========================================================
   4. Catalog, recipes, stock
   ========================================================= */

CREATE TABLE dbo.Categories (
    IdCategory      INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_Categories PRIMARY KEY,
    Name            NVARCHAR(100) NOT NULL,
    Description     NVARCHAR(300) NULL,
    IsActive        BIT NOT NULL CONSTRAINT DF_Categories_IsActive DEFAULT (1),
    CONSTRAINT UQ_Categories_Name UNIQUE (Name)
);
GO

CREATE TABLE dbo.Regions (
    IdRegion        INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_Regions PRIMARY KEY,
    Name            NVARCHAR(50) NOT NULL,  -- COAST | HIGHLANDS | JUNGLE
    Description     NVARCHAR(300) NULL,
    IsActive        BIT NOT NULL CONSTRAINT DF_Regions_IsActive DEFAULT (1),
    CONSTRAINT UQ_Regions_Name UNIQUE (Name)
);
GO

CREATE TABLE dbo.Products (
    IdProduct               INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_Products PRIMARY KEY,
    IdCategory              INT NOT NULL,
    IdRegion                INT NULL,
    Name                    NVARCHAR(150) NOT NULL,
    Description             NVARCHAR(1000) NULL,
    BasePrice               DECIMAL(10,2) NOT NULL,
    ImageUrl                NVARCHAR(500) NULL,
    PrepTimeMinutes         INT NULL,
    IsUnlimitedStock        BIT NOT NULL CONSTRAINT DF_Products_Unlimited DEFAULT (0),
    IsActive                BIT NOT NULL CONSTRAINT DF_Products_IsActive DEFAULT (1),
    CreatedAt               DATETIME2(0) NOT NULL CONSTRAINT DF_Products_CreatedAt DEFAULT (SYSUTCDATETIME()),
    CONSTRAINT FK_Products_Category FOREIGN KEY (IdCategory) REFERENCES dbo.Categories(IdCategory),
    CONSTRAINT FK_Products_Region FOREIGN KEY (IdRegion) REFERENCES dbo.Regions(IdRegion),
    CONSTRAINT CK_Products_BasePrice CHECK (BasePrice >= 0)
);
GO

CREATE TABLE dbo.Ingredients (
    IdIngredient        INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_Ingredients PRIMARY KEY,
    Name                NVARCHAR(150) NOT NULL,
    Unit                NVARCHAR(20)  NOT NULL, -- kg, g, L, ml, unit
    IsUnlimitedStock    BIT NOT NULL CONSTRAINT DF_Ingredients_Unlimited DEFAULT (0),
    MinStockAlert       DECIMAL(12,4) NULL,
    IsActive            BIT NOT NULL CONSTRAINT DF_Ingredients_IsActive DEFAULT (1),
    CONSTRAINT UQ_Ingredients_Name UNIQUE (Name)
);
GO

CREATE TABLE dbo.Recipes (
    IdRecipe        INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_Recipes PRIMARY KEY,
    IdProduct       INT NOT NULL,
    Version         INT NOT NULL CONSTRAINT DF_Recipes_Version DEFAULT (1),
    IsActive        BIT NOT NULL CONSTRAINT DF_Recipes_IsActive DEFAULT (1),
    CONSTRAINT UQ_Recipes_Product UNIQUE (IdProduct),
    CONSTRAINT FK_Recipes_Product FOREIGN KEY (IdProduct) REFERENCES dbo.Products(IdProduct)
);
GO

CREATE TABLE dbo.RecipeItems (
    IdRecipeItem    INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_RecipeItems PRIMARY KEY,
    IdRecipe        INT NOT NULL,
    IdIngredient    INT NOT NULL,
    Quantity        DECIMAL(12,4) NOT NULL,
    CONSTRAINT FK_RecipeItems_Recipe FOREIGN KEY (IdRecipe) REFERENCES dbo.Recipes(IdRecipe),
    CONSTRAINT FK_RecipeItems_Ingredient FOREIGN KEY (IdIngredient) REFERENCES dbo.Ingredients(IdIngredient),
    CONSTRAINT UQ_RecipeItems UNIQUE (IdRecipe, IdIngredient),
    CONSTRAINT CK_RecipeItems_Quantity CHECK (Quantity > 0)
);
GO

CREATE TABLE dbo.IngredientStocks (
    IdIngredientStock   INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_IngredientStocks PRIMARY KEY,
    IdBranch            INT NOT NULL,
    IdIngredient        INT NOT NULL,
    QuantityAvailable   DECIMAL(12,4) NOT NULL CONSTRAINT DF_IngredientStocks_Available DEFAULT (0),
    QuantityReserved    DECIMAL(12,4) NOT NULL CONSTRAINT DF_IngredientStocks_Reserved DEFAULT (0),
    AverageCost         DECIMAL(12,4) NULL,
    LastCost            DECIMAL(12,4) NULL,
    MinStockAlert       DECIMAL(12,4) NULL,
    Version             INT NOT NULL CONSTRAINT DF_IngredientStocks_Version DEFAULT (0),
    CONSTRAINT FK_IngredientStocks_Branch FOREIGN KEY (IdBranch) REFERENCES dbo.Branches(IdBranch),
    CONSTRAINT FK_IngredientStocks_Ingredient FOREIGN KEY (IdIngredient) REFERENCES dbo.Ingredients(IdIngredient),
    CONSTRAINT UQ_IngredientStocks UNIQUE (IdBranch, IdIngredient),
    CONSTRAINT CK_IngredientStocks_Available CHECK (QuantityAvailable >= 0),
    CONSTRAINT CK_IngredientStocks_Reserved CHECK (QuantityReserved >= 0)
);
GO

CREATE TABLE dbo.StockMovements (
    IdStockMovement     INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_StockMovements PRIMARY KEY,
    IdBranch            INT NOT NULL,
    IdIngredient        INT NOT NULL,
    MovementType        NVARCHAR(20) NOT NULL,
    Quantity            DECIMAL(12,4) NOT NULL,
    UnitCost            DECIMAL(12,4) NULL,
    IdOrder             INT NULL, -- FK after Orders
    IdUser              INT NULL,
    Notes               NVARCHAR(400) NULL,
    CreatedAt           DATETIME2(0) NOT NULL CONSTRAINT DF_StockMovements_CreatedAt DEFAULT (SYSUTCDATETIME()),
    CONSTRAINT FK_StockMovements_Branch FOREIGN KEY (IdBranch) REFERENCES dbo.Branches(IdBranch),
    CONSTRAINT FK_StockMovements_Ingredient FOREIGN KEY (IdIngredient) REFERENCES dbo.Ingredients(IdIngredient),
    CONSTRAINT FK_StockMovements_User FOREIGN KEY (IdUser) REFERENCES dbo.Users(IdUser),
    CONSTRAINT CK_StockMovements_Type CHECK (MovementType IN (
        N'IN', N'OUT', N'ADJUST', N'RESERVE', N'CONSUME', N'RELEASE'
    )),
    CONSTRAINT CK_StockMovements_Quantity CHECK (Quantity > 0)
);
GO

CREATE INDEX IX_StockMovements_Branch_Ingredient
    ON dbo.StockMovements(IdBranch, IdIngredient, CreatedAt DESC);
GO

/* =========================================================
   5. Daily menu
   ========================================================= */

CREATE TABLE dbo.DailyMenus (
    IdDailyMenu         INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_DailyMenus PRIMARY KEY,
    IdBranch            INT NOT NULL,
    MenuDate            DATE NOT NULL,
    Status              NVARCHAR(20) NOT NULL CONSTRAINT DF_DailyMenus_Status DEFAULT (N'DRAFT'),
    IdPublishedByUser   INT NULL,
    PublishedAt         DATETIME2(0) NULL,
    CONSTRAINT FK_DailyMenus_Branch FOREIGN KEY (IdBranch) REFERENCES dbo.Branches(IdBranch),
    CONSTRAINT FK_DailyMenus_User FOREIGN KEY (IdPublishedByUser) REFERENCES dbo.Users(IdUser),
    CONSTRAINT UQ_DailyMenus_Branch_Date UNIQUE (IdBranch, MenuDate),
    CONSTRAINT CK_DailyMenus_Status CHECK (Status IN (N'DRAFT', N'PUBLISHED', N'CLOSED'))
);
GO

CREATE TABLE dbo.DailyMenuItems (
    IdDailyMenuItem     INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_DailyMenuItems PRIMARY KEY,
    IdDailyMenu         INT NOT NULL,
    IdProduct           INT NOT NULL,
    IsAvailable         BIT NOT NULL CONSTRAINT DF_DailyMenuItems_Available DEFAULT (1),
    PriceOverride       DECIMAL(10,2) NULL,
    MaxQuota            INT NULL,
    SoldQuantity        INT NOT NULL CONSTRAINT DF_DailyMenuItems_Sold DEFAULT (0),
    CONSTRAINT FK_DailyMenuItems_Menu FOREIGN KEY (IdDailyMenu) REFERENCES dbo.DailyMenus(IdDailyMenu),
    CONSTRAINT FK_DailyMenuItems_Product FOREIGN KEY (IdProduct) REFERENCES dbo.Products(IdProduct),
    CONSTRAINT UQ_DailyMenuItems UNIQUE (IdDailyMenu, IdProduct),
    CONSTRAINT CK_DailyMenuItems_Price CHECK (PriceOverride IS NULL OR PriceOverride >= 0)
);
GO

CREATE TABLE dbo.DailyMenuItemShifts (
    IdDailyMenuItem INT NOT NULL,
    IdBranchShift   INT NOT NULL,
    CONSTRAINT PK_DailyMenuItemShifts PRIMARY KEY (IdDailyMenuItem, IdBranchShift),
    CONSTRAINT FK_DailyMenuItemShifts_Item FOREIGN KEY (IdDailyMenuItem) REFERENCES dbo.DailyMenuItems(IdDailyMenuItem),
    CONSTRAINT FK_DailyMenuItemShifts_Shift FOREIGN KEY (IdBranchShift) REFERENCES dbo.BranchShifts(IdBranchShift)
);
GO

/* =========================================================
   6. Promotions & coupons
   ========================================================= */

CREATE TABLE dbo.Promotions (
    IdPromotion         INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_Promotions PRIMARY KEY,
    Code                NVARCHAR(40)  NULL,
    Name                NVARCHAR(150) NOT NULL,
    Description         NVARCHAR(500) NULL,
    /* PERCENT | FIXED_AMOUNT | FREE_DELIVERY */
    PromoType           NVARCHAR(30)  NOT NULL,
    DiscountPercent     DECIMAL(5,2)  NULL,
    DiscountAmount      DECIMAL(10,2) NULL,
    /* PRODUCT | CATEGORY | ORDER | DELIVERY */
    Scope               NVARCHAR(20)  NOT NULL,
    StartsAt            DATETIME2(0)  NOT NULL,
    EndsAt              DATETIME2(0)  NOT NULL,
    Priority            INT NOT NULL CONSTRAINT DF_Promotions_Priority DEFAULT (0),
    IsStackable         BIT NOT NULL CONSTRAINT DF_Promotions_Stackable DEFAULT (0),
    IsAutomatic         BIT NOT NULL CONSTRAINT DF_Promotions_Automatic DEFAULT (1),
    MinOrderAmount      DECIMAL(10,2) NULL,
    MaxDiscountAmount   DECIMAL(10,2) NULL,
    IdBranch            INT NULL,  -- NULL = all branches
    IsActive            BIT NOT NULL CONSTRAINT DF_Promotions_IsActive DEFAULT (1),
    CreatedAt           DATETIME2(0) NOT NULL CONSTRAINT DF_Promotions_CreatedAt DEFAULT (SYSUTCDATETIME()),
    IdCreatedByUser     INT NULL,
    CONSTRAINT FK_Promotions_Branch FOREIGN KEY (IdBranch) REFERENCES dbo.Branches(IdBranch),
    CONSTRAINT FK_Promotions_CreatedBy FOREIGN KEY (IdCreatedByUser) REFERENCES dbo.Users(IdUser),
    CONSTRAINT CK_Promotions_Type CHECK (PromoType IN (N'PERCENT', N'FIXED_AMOUNT', N'FREE_DELIVERY')),
    CONSTRAINT CK_Promotions_Scope CHECK (Scope IN (N'PRODUCT', N'CATEGORY', N'ORDER', N'DELIVERY'))
);
GO

CREATE TABLE dbo.PromotionProducts (
    IdPromotion INT NOT NULL,
    IdProduct   INT NOT NULL,
    CONSTRAINT PK_PromotionProducts PRIMARY KEY (IdPromotion, IdProduct),
    CONSTRAINT FK_PromotionProducts_Promo FOREIGN KEY (IdPromotion) REFERENCES dbo.Promotions(IdPromotion),
    CONSTRAINT FK_PromotionProducts_Product FOREIGN KEY (IdProduct) REFERENCES dbo.Products(IdProduct)
);
GO

CREATE TABLE dbo.PromotionCategories (
    IdPromotion INT NOT NULL,
    IdCategory  INT NOT NULL,
    CONSTRAINT PK_PromotionCategories PRIMARY KEY (IdPromotion, IdCategory),
    CONSTRAINT FK_PromotionCategories_Promo FOREIGN KEY (IdPromotion) REFERENCES dbo.Promotions(IdPromotion),
    CONSTRAINT FK_PromotionCategories_Category FOREIGN KEY (IdCategory) REFERENCES dbo.Categories(IdCategory)
);
GO

CREATE TABLE dbo.PromotionExclusions (
    IdPromotionExclusion INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_PromotionExclusions PRIMARY KEY,
    IdPromotion          INT NOT NULL,
    IdProduct            INT NULL,
    IdCategory           INT NULL,
    CONSTRAINT FK_PromotionExclusions_Promo FOREIGN KEY (IdPromotion) REFERENCES dbo.Promotions(IdPromotion),
    CONSTRAINT FK_PromotionExclusions_Product FOREIGN KEY (IdProduct) REFERENCES dbo.Products(IdProduct),
    CONSTRAINT FK_PromotionExclusions_Category FOREIGN KEY (IdCategory) REFERENCES dbo.Categories(IdCategory),
    CONSTRAINT CK_PromotionExclusions_Target CHECK (
        (IdProduct IS NOT NULL AND IdCategory IS NULL)
        OR (IdProduct IS NULL AND IdCategory IS NOT NULL)
    )
);
GO

CREATE TABLE dbo.PromotionAuditLog (
    IdPromotionAudit    BIGINT IDENTITY(1,1) NOT NULL CONSTRAINT PK_PromotionAuditLog PRIMARY KEY,
    IdPromotion         INT NOT NULL,
    IdUser              INT NULL,
    Action              NVARCHAR(40) NOT NULL,  -- CREATED | UPDATED | ACTIVATED | DEACTIVATED
    Detail              NVARCHAR(MAX) NULL,
    CreatedAt           DATETIME2(0) NOT NULL CONSTRAINT DF_PromotionAudit_CreatedAt DEFAULT (SYSUTCDATETIME()),
    CONSTRAINT FK_PromotionAudit_Promo FOREIGN KEY (IdPromotion) REFERENCES dbo.Promotions(IdPromotion),
    CONSTRAINT FK_PromotionAudit_User FOREIGN KEY (IdUser) REFERENCES dbo.Users(IdUser)
);
GO

CREATE TABLE dbo.Coupons (
    IdCoupon            INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_Coupons PRIMARY KEY,
    Code                NVARCHAR(40) NOT NULL,
    Description         NVARCHAR(300) NULL,
    DiscountType        NVARCHAR(20) NOT NULL, -- PERCENT | FIXED
    DiscountValue       DECIMAL(10,2) NOT NULL,
    MinOrderAmount      DECIMAL(10,2) NULL,
    MaxUses             INT NULL,
    UsedCount           INT NOT NULL CONSTRAINT DF_Coupons_Used DEFAULT (0),
    StartsAt            DATETIME2(0) NOT NULL,
    EndsAt              DATETIME2(0) NOT NULL,
    IsActive            BIT NOT NULL CONSTRAINT DF_Coupons_IsActive DEFAULT (1),
    CONSTRAINT UQ_Coupons_Code UNIQUE (Code),
    CONSTRAINT CK_Coupons_Type CHECK (DiscountType IN (N'PERCENT', N'FIXED'))
);
GO

CREATE TABLE dbo.CouponUsages (
    IdCouponUsage   INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_CouponUsages PRIMARY KEY,
    IdCoupon        INT NOT NULL,
    IdUser          INT NOT NULL,
    IdOrder         INT NULL,
    UsedAt          DATETIME2(0) NOT NULL CONSTRAINT DF_CouponUsages_UsedAt DEFAULT (SYSUTCDATETIME()),
    CONSTRAINT FK_CouponUsages_Coupon FOREIGN KEY (IdCoupon) REFERENCES dbo.Coupons(IdCoupon),
    CONSTRAINT FK_CouponUsages_User FOREIGN KEY (IdUser) REFERENCES dbo.Users(IdUser)
);
GO

/* =========================================================
   7. Cart items  & Orders
   ========================================================= */

CREATE TABLE dbo.CartItems (
    IdCartItem      INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_CartItems PRIMARY KEY,
    IdCustomer      INT NOT NULL,
    IdProduct       INT NOT NULL,
    IdDailyMenuItem INT NULL,
    Quantity        INT NOT NULL,
    UnitPrice       DECIMAL(10,2) NOT NULL,
    ProductName     NVARCHAR(150) NOT NULL,
    ImageUrl        NVARCHAR(500) NULL,
    UpdatedAt       DATETIME2 NOT NULL CONSTRAINT DF_CartItems_UpdatedAt DEFAULT SYSUTCDATETIME(),
    CONSTRAINT FK_CartItems_Customer FOREIGN KEY (IdCustomer) REFERENCES dbo.Customers(IdCustomer),
    CONSTRAINT FK_CartItems_Product  FOREIGN KEY (IdProduct)  REFERENCES dbo.Products(IdProduct),
    CONSTRAINT CK_CartItems_Qty CHECK (Quantity > 0)
);
GO

CREATE INDEX IX_CartItems_Customer ON dbo.CartItems (IdCustomer);
GO

CREATE TABLE dbo.OrderStatuses (
    IdOrderStatus   INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_OrderStatuses PRIMARY KEY,
    Code            NVARCHAR(40) NOT NULL,
    Name            NVARCHAR(80) NOT NULL,
    SortOrder       INT NOT NULL CONSTRAINT DF_OrderStatuses_Sort DEFAULT (0),
    IsActive        BIT NOT NULL CONSTRAINT DF_OrderStatuses_IsActive DEFAULT (1),
    CONSTRAINT UQ_OrderStatuses_Code UNIQUE (Code)
);
GO

CREATE TABLE dbo.Orders (
    IdOrder             INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_Orders PRIMARY KEY,
    IdCustomer          INT NOT NULL,
    IdBranch            INT NOT NULL,
    IdAddress           INT NOT NULL,
    IdOrderStatus       INT NOT NULL,
    OrderedAt           DATETIME2(0) NOT NULL CONSTRAINT DF_Orders_OrderedAt DEFAULT (SYSUTCDATETIME()),
    Subtotal            DECIMAL(10,2) NOT NULL CONSTRAINT DF_Orders_Subtotal DEFAULT (0),
    DeliveryFee         DECIMAL(10,2) NOT NULL CONSTRAINT DF_Orders_Delivery DEFAULT (0),
    Discount            DECIMAL(10,2) NOT NULL CONSTRAINT DF_Orders_Discount DEFAULT (0),
    Total               DECIMAL(10,2) NOT NULL CONSTRAINT DF_Orders_Total DEFAULT (0),
    Notes               NVARCHAR(500) NULL,
    CouponCode          NVARCHAR(40) NULL,
    CONSTRAINT FK_Orders_Customer FOREIGN KEY (IdCustomer) REFERENCES dbo.Customers(IdCustomer),
    CONSTRAINT FK_Orders_Branch FOREIGN KEY (IdBranch) REFERENCES dbo.Branches(IdBranch),
    CONSTRAINT FK_Orders_Address FOREIGN KEY (IdAddress) REFERENCES dbo.CustomerAddresses(IdAddress),
    CONSTRAINT FK_Orders_Status FOREIGN KEY (IdOrderStatus) REFERENCES dbo.OrderStatuses(IdOrderStatus),
    CONSTRAINT CK_Orders_Money CHECK (Subtotal >= 0 AND DeliveryFee >= 0 AND Discount >= 0 AND Total >= 0)
);
GO

CREATE TABLE dbo.OrderItems (
    IdOrderItem         INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_OrderItems PRIMARY KEY,
    IdOrder             INT NOT NULL,
    IdProduct           INT NOT NULL,
    IdDailyMenuItem     INT NULL,
    ProductNameSnapshot NVARCHAR(150) NOT NULL,
    UnitPrice           DECIMAL(10,2) NOT NULL,
    Quantity            INT NOT NULL,
    LineDiscount        DECIMAL(10,2) NOT NULL CONSTRAINT DF_OrderItems_Discount DEFAULT (0),
    LineTotal           DECIMAL(10,2) NOT NULL,
    Notes               NVARCHAR(300) NULL,
    CONSTRAINT FK_OrderItems_Order FOREIGN KEY (IdOrder) REFERENCES dbo.Orders(IdOrder),
    CONSTRAINT FK_OrderItems_Product FOREIGN KEY (IdProduct) REFERENCES dbo.Products(IdProduct),
    CONSTRAINT FK_OrderItems_DailyMenuItem FOREIGN KEY (IdDailyMenuItem) REFERENCES dbo.DailyMenuItems(IdDailyMenuItem),
    CONSTRAINT CK_OrderItems_Quantity CHECK (Quantity > 0)
);
GO

CREATE TABLE dbo.OrderDiscounts (
    IdOrderDiscount     INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_OrderDiscounts PRIMARY KEY,
    IdOrder             INT NOT NULL,
    /* PROMOTION | COUPON | MANUAL */
    SourceType          NVARCHAR(20) NOT NULL,
    IdPromotion         INT NULL,
    IdCoupon            INT NULL,
    CodeSnapshot        NVARCHAR(40) NULL,
    NameSnapshot        NVARCHAR(150) NOT NULL,
    DiscountAmount      DECIMAL(10,2) NOT NULL,
    CONSTRAINT FK_OrderDiscounts_Order FOREIGN KEY (IdOrder) REFERENCES dbo.Orders(IdOrder),
    CONSTRAINT FK_OrderDiscounts_Promotion FOREIGN KEY (IdPromotion) REFERENCES dbo.Promotions(IdPromotion),
    CONSTRAINT FK_OrderDiscounts_Coupon FOREIGN KEY (IdCoupon) REFERENCES dbo.Coupons(IdCoupon),
    CONSTRAINT CK_OrderDiscounts_Source CHECK (SourceType IN (N'PROMOTION', N'COUPON', N'MANUAL')),
    CONSTRAINT CK_OrderDiscounts_Amount CHECK (DiscountAmount >= 0)
);
GO

CREATE TABLE dbo.OrderHistory (
    IdOrderHistory  INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_OrderHistory PRIMARY KEY,
    IdOrder         INT NOT NULL,
    IdOrderStatus   INT NOT NULL,
    IdUser          INT NULL,
    Notes           NVARCHAR(300) NULL,
    CreatedAt       DATETIME2(0) NOT NULL CONSTRAINT DF_OrderHistory_CreatedAt DEFAULT (SYSUTCDATETIME()),
    CONSTRAINT FK_OrderHistory_Order FOREIGN KEY (IdOrder) REFERENCES dbo.Orders(IdOrder),
    CONSTRAINT FK_OrderHistory_Status FOREIGN KEY (IdOrderStatus) REFERENCES dbo.OrderStatuses(IdOrderStatus),
    CONSTRAINT FK_OrderHistory_User FOREIGN KEY (IdUser) REFERENCES dbo.Users(IdUser)
);
GO

CREATE TABLE dbo.OrderStockReservations (
    IdReservation       INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_OrderStockReservations PRIMARY KEY,
    IdOrder             INT NOT NULL,
    IdBranch            INT NOT NULL,
    IdIngredient        INT NOT NULL,
    Quantity            DECIMAL(12,4) NOT NULL,
    Status              NVARCHAR(20) NOT NULL, -- RESERVED | CONSUMED | RELEASED
    CreatedAt           DATETIME2(0) NOT NULL CONSTRAINT DF_OrderStockRes_CreatedAt DEFAULT (SYSUTCDATETIME()),
    CONSTRAINT FK_OrderStockRes_Order FOREIGN KEY (IdOrder) REFERENCES dbo.Orders(IdOrder),
    CONSTRAINT FK_OrderStockRes_Branch FOREIGN KEY (IdBranch) REFERENCES dbo.Branches(IdBranch),
    CONSTRAINT FK_OrderStockRes_Ingredient FOREIGN KEY (IdIngredient) REFERENCES dbo.Ingredients(IdIngredient),
    CONSTRAINT CK_OrderStockRes_Status CHECK (Status IN (N'RESERVED', N'CONSUMED', N'RELEASED')),
    CONSTRAINT CK_OrderStockRes_Qty CHECK (Quantity > 0)
);
GO

ALTER TABLE dbo.StockMovements
    ADD CONSTRAINT FK_StockMovements_Order
    FOREIGN KEY (IdOrder) REFERENCES dbo.Orders(IdOrder);
GO

ALTER TABLE dbo.CouponUsages
    ADD CONSTRAINT FK_CouponUsages_Order
    FOREIGN KEY (IdOrder) REFERENCES dbo.Orders(IdOrder);
GO

CREATE TABLE dbo.PaymentMethods (
    IdPaymentMethod INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_PaymentMethods PRIMARY KEY,
    Code            NVARCHAR(40) NOT NULL,  -- CASH | CARD | YAPE | PLIN | TRANSFER
    Name            NVARCHAR(80) NOT NULL,
    IsOnline        BIT NOT NULL CONSTRAINT DF_PaymentMethods_Online DEFAULT (0),
    IsActive        BIT NOT NULL CONSTRAINT DF_PaymentMethods_IsActive DEFAULT (1),
    SortOrder       INT NOT NULL CONSTRAINT DF_PaymentMethods_Sort DEFAULT (0),
    CONSTRAINT UQ_PaymentMethods_Code UNIQUE (Code)
);
GO

CREATE TABLE dbo.Payments (
    IdPayment           INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_Payments PRIMARY KEY,
    IdOrder             INT NOT NULL,
    IdPaymentMethod     INT NOT NULL,
    Amount              DECIMAL(10,2) NOT NULL,
    /* PENDING | AUTHORIZED | PAID | FAILED | REFUNDED */
    Status              NVARCHAR(20) NOT NULL,
    ExternalReference   NVARCHAR(120) NULL,
    PaidAt              DATETIME2(0) NULL,
    Notes               NVARCHAR(300) NULL,
    IdRegisteredByUser  INT NULL,
    CreatedAt           DATETIME2(0) NOT NULL CONSTRAINT DF_Payments_CreatedAt DEFAULT (SYSUTCDATETIME()),
    CONSTRAINT FK_Payments_Order FOREIGN KEY (IdOrder) REFERENCES dbo.Orders(IdOrder),
    CONSTRAINT FK_Payments_Method FOREIGN KEY (IdPaymentMethod) REFERENCES dbo.PaymentMethods(IdPaymentMethod),
    CONSTRAINT FK_Payments_User FOREIGN KEY (IdRegisteredByUser) REFERENCES dbo.Users(IdUser),
    CONSTRAINT CK_Payments_Status CHECK (Status IN (
        N'PENDING', N'AUTHORIZED', N'PAID', N'FAILED', N'REFUNDED'
    )),
    CONSTRAINT CK_Payments_Amount CHECK (Amount > 0)
);
GO

CREATE INDEX IX_Payments_Order ON dbo.Payments(IdOrder);
GO

CREATE TABLE dbo.DeliveryTariffs (
    IdDeliveryTariff    INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_DeliveryTariffs PRIMARY KEY,
    IdBranch            INT NOT NULL,
    Name                NVARCHAR(100) NOT NULL,
    /* DISTANCE_KM | DISTRICT */
    RuleType            NVARCHAR(20) NOT NULL,
    MinDistanceKm       DECIMAL(8,2) NULL,
    MaxDistanceKm       DECIMAL(8,2) NULL,
    IdDistrict          CHAR(6) NULL,
    Fee                 DECIMAL(10,2) NOT NULL,
    IsActive            BIT NOT NULL CONSTRAINT DF_DeliveryTariffs_IsActive DEFAULT (1),
    SortOrder           INT NOT NULL CONSTRAINT DF_DeliveryTariffs_Sort DEFAULT (0),
    CONSTRAINT FK_DeliveryTariffs_Branch FOREIGN KEY (IdBranch) REFERENCES dbo.Branches(IdBranch),
    CONSTRAINT FK_DeliveryTariffs_District FOREIGN KEY (IdDistrict) REFERENCES dbo.GeoDistricts(IdDistrict),
    CONSTRAINT CK_DeliveryTariffs_Rule CHECK (RuleType IN (N'DISTANCE_KM', N'DISTRICT')),
    CONSTRAINT CK_DeliveryTariffs_Fee CHECK (Fee >= 0)
);
GO

CREATE INDEX IX_DeliveryTariffs_Branch ON dbo.DeliveryTariffs(IdBranch, IsActive);
GO

/* =========================================================
   8. Kitchen staff, couriers, deliveries
   ========================================================= */

CREATE TABLE dbo.KitchenStaff (
    IdKitchenStaff  INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_KitchenStaff PRIMARY KEY,
    IdUser          INT NOT NULL,
    IdBranch        INT NOT NULL,
    JobTitle        NVARCHAR(50) NULL,
    IsActive        BIT NOT NULL CONSTRAINT DF_KitchenStaff_IsActive DEFAULT (1),
    AssignedAt      DATETIME2(0) NOT NULL CONSTRAINT DF_KitchenStaff_AssignedAt DEFAULT (SYSUTCDATETIME()),
    CONSTRAINT UQ_KitchenStaff_User UNIQUE (IdUser),
    CONSTRAINT FK_KitchenStaff_User FOREIGN KEY (IdUser) REFERENCES dbo.Users(IdUser),
    CONSTRAINT FK_KitchenStaff_Branch FOREIGN KEY (IdBranch) REFERENCES dbo.Branches(IdBranch)
);
GO

CREATE TABLE dbo.Couriers (
    IdCourier       INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_Couriers PRIMARY KEY,
    IdUser          INT NOT NULL,
    IdBranch        INT NOT NULL,
    Phone           NVARCHAR(30) NULL,
    Vehicle         NVARCHAR(50) NULL,
    Status          NVARCHAR(20) NOT NULL, -- OFFLINE | AVAILABLE | ON_DELIVERY
    IsActive        BIT NOT NULL CONSTRAINT DF_Couriers_IsActive DEFAULT (1),
    LastLat         DECIMAL(10,7) NULL,
    LastLng         DECIMAL(10,7) NULL,
    CONSTRAINT UQ_Couriers_User UNIQUE (IdUser),
    CONSTRAINT FK_Couriers_User FOREIGN KEY (IdUser) REFERENCES dbo.Users(IdUser),
    CONSTRAINT FK_Couriers_Branch FOREIGN KEY (IdBranch) REFERENCES dbo.Branches(IdBranch),
    CONSTRAINT CK_Couriers_Status CHECK (Status IN (N'OFFLINE', N'AVAILABLE', N'ON_DELIVERY'))
);
GO

CREATE TABLE dbo.Deliveries (
    IdDelivery      INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_Deliveries PRIMARY KEY,
    IdOrder         INT NOT NULL,
    IdCourier       INT NULL,
    Status          NVARCHAR(30) NOT NULL, -- PENDING | ASSIGNED | ON_ROUTE | DELIVERED | FAILED
    AssignedAt      DATETIME2(0) NULL,
    PickedUpAt      DATETIME2(0) NULL,
    DeliveredAt     DATETIME2(0) NULL,
    Notes           NVARCHAR(300) NULL,
    CONSTRAINT UQ_Deliveries_Order UNIQUE (IdOrder),
    CONSTRAINT FK_Deliveries_Order FOREIGN KEY (IdOrder) REFERENCES dbo.Orders(IdOrder),
    CONSTRAINT FK_Deliveries_Courier FOREIGN KEY (IdCourier) REFERENCES dbo.Couriers(IdCourier),
    CONSTRAINT CK_Deliveries_Status CHECK (Status IN (
        N'PENDING', N'ASSIGNED', N'ON_ROUTE', N'DELIVERED', N'FAILED'
    ))
);
GO

/* =========================================================
   9. ACL – permissions, groups, menus
   ========================================================= */

CREATE TABLE dbo.Permissions (
    IdPermission    INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_Permissions PRIMARY KEY,
    Code            NVARCHAR(80) NOT NULL,
    Module          NVARCHAR(80) NOT NULL,
    Name            NVARCHAR(150) NOT NULL,  -- UI label key / English name
    Description     NVARCHAR(300) NULL,
    IsActive        BIT NOT NULL CONSTRAINT DF_Permissions_IsActive DEFAULT (1),
    CONSTRAINT UQ_Permissions_Code UNIQUE (Code)
);
GO

CREATE TABLE dbo.PermissionGroups (
    IdPermissionGroup INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_PermissionGroups PRIMARY KEY,
    Code              NVARCHAR(80) NOT NULL,
    Name              NVARCHAR(150) NOT NULL,
    Description       NVARCHAR(300) NULL,
    IsActive          BIT NOT NULL CONSTRAINT DF_PermissionGroups_IsActive DEFAULT (1),
    CONSTRAINT UQ_PermissionGroups_Code UNIQUE (Code)
);
GO

CREATE TABLE dbo.PermissionGroupItems (
    IdPermissionGroup INT NOT NULL,
    IdPermission      INT NOT NULL,
    CONSTRAINT PK_PermissionGroupItems PRIMARY KEY (IdPermissionGroup, IdPermission),
    CONSTRAINT FK_PGI_Group FOREIGN KEY (IdPermissionGroup) REFERENCES dbo.PermissionGroups(IdPermissionGroup),
    CONSTRAINT FK_PGI_Permission FOREIGN KEY (IdPermission) REFERENCES dbo.Permissions(IdPermission)
);
GO

CREATE TABLE dbo.RolePermissionGroups (
    IdRole            INT NOT NULL,
    IdPermissionGroup INT NOT NULL,
    CONSTRAINT PK_RolePermissionGroups PRIMARY KEY (IdRole, IdPermissionGroup),
    CONSTRAINT FK_RPG_Role FOREIGN KEY (IdRole) REFERENCES dbo.Roles(IdRole),
    CONSTRAINT FK_RPG_Group FOREIGN KEY (IdPermissionGroup) REFERENCES dbo.PermissionGroups(IdPermissionGroup)
);
GO

CREATE TABLE dbo.RolePermissions (
    IdRole       INT NOT NULL,
    IdPermission INT NOT NULL,
    Effect       NVARCHAR(10) NOT NULL, -- ALLOW | DENY
    CONSTRAINT PK_RolePermissions PRIMARY KEY (IdRole, IdPermission),
    CONSTRAINT FK_RP_Role FOREIGN KEY (IdRole) REFERENCES dbo.Roles(IdRole),
    CONSTRAINT FK_RP_Permission FOREIGN KEY (IdPermission) REFERENCES dbo.Permissions(IdPermission),
    CONSTRAINT CK_RolePermissions_Effect CHECK (Effect IN (N'ALLOW', N'DENY'))
);
GO

CREATE TABLE dbo.UserPermissionConfigs (
    IdUser          INT NOT NULL CONSTRAINT PK_UserPermissionConfigs PRIMARY KEY,
    PermissionMode  NVARCHAR(20) NOT NULL, -- INHERIT | RESTRICT | CUSTOM
    UpdatedAt       DATETIME2(0) NOT NULL CONSTRAINT DF_UPC_UpdatedAt DEFAULT (SYSUTCDATETIME()),
    CONSTRAINT FK_UPC_User FOREIGN KEY (IdUser) REFERENCES dbo.Users(IdUser),
    CONSTRAINT CK_UPC_Mode CHECK (PermissionMode IN (N'INHERIT', N'RESTRICT', N'CUSTOM'))
);
GO

CREATE TABLE dbo.UserPermissionOverrides (
    IdUser       INT NOT NULL,
    IdPermission INT NOT NULL,
    Effect       NVARCHAR(10) NOT NULL, -- ALLOW | DENY
    CONSTRAINT PK_UserPermissionOverrides PRIMARY KEY (IdUser, IdPermission),
    CONSTRAINT FK_UPO_User FOREIGN KEY (IdUser) REFERENCES dbo.Users(IdUser),
    CONSTRAINT FK_UPO_Permission FOREIGN KEY (IdPermission) REFERENCES dbo.Permissions(IdPermission),
    CONSTRAINT CK_UPO_Effect CHECK (Effect IN (N'ALLOW', N'DENY'))
);
GO

CREATE TABLE dbo.MenuItems (
    IdMenuItem      INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_MenuItems PRIMARY KEY,
    Code            NVARCHAR(80) NOT NULL,
    Label           NVARCHAR(120) NOT NULL,  -- default label; UI may translate to Spanish
    Route           NVARCHAR(200) NOT NULL,
    Icon            NVARCHAR(50)  NULL,
    MenuGroup       NVARCHAR(80)  NULL,
    Context         NVARCHAR(20)  NOT NULL, -- ADMIN | OPERATIONS | KITCHEN | COURIER | CUSTOMER
    SortOrder       INT NOT NULL CONSTRAINT DF_MenuItems_Sort DEFAULT (0),
    IsActive        BIT NOT NULL CONSTRAINT DF_MenuItems_IsActive DEFAULT (1),
    CONSTRAINT UQ_MenuItems_Code UNIQUE (Code),
    CONSTRAINT CK_MenuItems_Context CHECK (Context IN (
        N'ADMIN', N'OPERATIONS', N'KITCHEN', N'COURIER', N'CUSTOMER'
    ))
);
GO

CREATE TABLE dbo.MenuItemPermissions (
    IdMenuItem   INT NOT NULL,
    IdPermission INT NOT NULL,
    CONSTRAINT PK_MenuItemPermissions PRIMARY KEY (IdMenuItem, IdPermission),
    CONSTRAINT FK_MIP_Menu FOREIGN KEY (IdMenuItem) REFERENCES dbo.MenuItems(IdMenuItem),
    CONSTRAINT FK_MIP_Permission FOREIGN KEY (IdPermission) REFERENCES dbo.Permissions(IdPermission)
);
GO

CREATE TABLE dbo.SecurityConfig (
    IdSecurityConfig    INT NOT NULL CONSTRAINT PK_SecurityConfig PRIMARY KEY
                        CONSTRAINT CK_SecurityConfig_Single CHECK (IdSecurityConfig = 1),
    SecurityVersion     INT NOT NULL CONSTRAINT DF_SecurityConfig_Version DEFAULT (1),
    UpdatedAt           DATETIME2(0) NOT NULL CONSTRAINT DF_SecurityConfig_UpdatedAt DEFAULT (SYSUTCDATETIME())
);
GO

INSERT INTO dbo.SecurityConfig (IdSecurityConfig, SecurityVersion) VALUES (1, 1);
GO

CREATE TABLE dbo.SecurityAuditLog (
    IdSecurityAudit     BIGINT IDENTITY(1,1) NOT NULL CONSTRAINT PK_SecurityAuditLog PRIMARY KEY,
    IdUser              INT NULL,
    Action              NVARCHAR(50) NOT NULL,
    EntityType          NVARCHAR(50) NOT NULL,
    EntityId            NVARCHAR(50) NULL,
    Detail              NVARCHAR(MAX) NULL,
    CreatedAt           DATETIME2(0) NOT NULL CONSTRAINT DF_SecurityAuditLog_CreatedAt DEFAULT (SYSUTCDATETIME()),
    CONSTRAINT FK_SecurityAuditLog_User FOREIGN KEY (IdUser) REFERENCES dbo.Users(IdUser)
);
GO

CREATE TABLE dbo.UserAccessLogs (
    IdAccessLog         BIGINT IDENTITY(1,1) NOT NULL CONSTRAINT PK_UserAccessLogs PRIMARY KEY,
    IdUser              INT NULL,
    EmailAttempted      NVARCHAR(150) NULL,
    EventType           NVARCHAR(30) NOT NULL,
    IsSuccess           BIT NOT NULL CONSTRAINT DF_UserAccessLogs_Success DEFAULT (1),
    Ip                  NVARCHAR(45) NULL,
    UserAgent           NVARCHAR(500) NULL,
    Browser             NVARCHAR(80) NULL,
    OperatingSystem     NVARCHAR(80) NULL,
    Device              NVARCHAR(40) NULL,
    SessionId           NVARCHAR(100) NULL,
    Detail              NVARCHAR(300) NULL,
    CreatedAt           DATETIME2(0) NOT NULL CONSTRAINT DF_UserAccessLogs_CreatedAt DEFAULT (SYSUTCDATETIME()),
    CONSTRAINT FK_UserAccessLogs_User FOREIGN KEY (IdUser) REFERENCES dbo.Users(IdUser)
);
GO

CREATE INDEX IX_UserAccessLogs_User_Created ON dbo.UserAccessLogs(IdUser, CreatedAt DESC);
GO


CREATE INDEX IX_Orders_Customer ON dbo.Orders(IdCustomer, OrderedAt DESC);
CREATE INDEX IX_Orders_Branch_Status ON dbo.Orders(IdBranch, IdOrderStatus);
CREATE INDEX IX_Promotions_Active_Dates ON dbo.Promotions(IsActive, StartsAt, EndsAt);
CREATE INDEX IX_DailyMenus_Branch_Date ON dbo.DailyMenus(IdBranch, MenuDate);

/* =========================================================
   10. Seed – roles & order statuses (minimum)
   ========================================================= */

INSERT INTO dbo.Roles (Name, Description) VALUES
(N'ADMINISTRATOR', N'Full system access'),
(N'MANAGER', N'Store / operations manager'),
(N'OPERATIONS', N'Daily operations'),
(N'WAREHOUSE', N'Stock and ingredients'),
(N'CHEF', N'Kitchen staff'),
(N'COURIER', N'Delivery staff'),
(N'CUSTOMER_SERVICE', N'Customer support'),
(N'CASHIER', N'Payments / POS'),
(N'CUSTOMER', N'End customer');
GO

INSERT INTO dbo.OrderStatuses (Code, Name, SortOrder) VALUES
(N'PENDING', N'Pending', 10),
(N'CONFIRMED', N'Confirmed', 20),
(N'IN_PREPARATION', N'In preparation', 30),
(N'READY', N'Ready', 40),
(N'OUT_FOR_DELIVERY', N'Out for delivery', 50),
(N'DELIVERED', N'Delivered', 60),
(N'CANCELLED', N'Cancelled', 90);
GO

INSERT INTO dbo.PaymentMethods (Code, Name, IsOnline, SortOrder) VALUES
(N'CASH', N'Cash', 0, 10),
(N'CARD', N'Card', 1, 20),
(N'YAPE', N'Yape', 1, 30),
(N'PLIN', N'Plin', 1, 40),
(N'TRANSFER', N'Bank transfer', 0, 50);