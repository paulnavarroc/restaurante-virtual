/*
  Sabores Peru – V1_1 seed security & base catalogs
  Run after V1__schema.sql
*/
USE SaboresPeru;
GO

SET NOCOUNT ON;
GO

/* =========================================================
   1. Permissions catalog
   ========================================================= */

INSERT INTO dbo.Permissions (Code, Module, Name, Description) VALUES
-- Products
(N'PRODUCTS_VIEW',     N'PRODUCTS', N'View products', N'List and view products'),
(N'PRODUCTS_CREATE',   N'PRODUCTS', N'Create products', NULL),
(N'PRODUCTS_EDIT',     N'PRODUCTS', N'Edit products', NULL),
(N'PRODUCTS_DELETE',   N'PRODUCTS', N'Deactivate products', NULL),
-- Categories / regions
(N'CATEGORIES_VIEW',   N'CATALOG', N'View categories', NULL),
(N'CATEGORIES_MANAGE', N'CATALOG', N'Manage categories', NULL),
(N'REGIONS_MANAGE',    N'CATALOG', N'Manage regions', NULL),
-- Ingredients & stock
(N'INGREDIENTS_VIEW',   N'INVENTORY', N'View ingredients', NULL),
(N'INGREDIENTS_MANAGE', N'INVENTORY', N'Manage ingredients', NULL),
(N'STOCK_VIEW',         N'INVENTORY', N'View stock', NULL),
(N'STOCK_ENTRY',        N'INVENTORY', N'Register stock entry', NULL),
(N'STOCK_ADJUST',       N'INVENTORY', N'Adjust stock', NULL),
(N'RECIPES_MANAGE',     N'INVENTORY', N'Manage recipes', NULL),
-- Daily menu
(N'DAILY_MENU_VIEW',     N'DAILY_MENU', N'View daily menus', NULL),
(N'DAILY_MENU_MANAGE',   N'DAILY_MENU', N'Create/edit daily menus', NULL),
(N'DAILY_MENU_PUBLISH',  N'DAILY_MENU', N'Publish daily menus', NULL),
-- Promotions & coupons
(N'PROMOTIONS_VIEW',   N'PROMOTIONS', N'View promotions', NULL),
(N'PROMOTIONS_MANAGE', N'PROMOTIONS', N'Manage promotions', NULL),
(N'COUPONS_VIEW',      N'PROMOTIONS', N'View coupons', NULL),
(N'COUPONS_MANAGE',    N'PROMOTIONS', N'Manage coupons', NULL),
-- Orders
(N'ORDERS_VIEW',       N'ORDERS', N'View orders', NULL),
(N'ORDERS_CREATE',     N'ORDERS', N'Create orders', NULL),
(N'ORDERS_EDIT',       N'ORDERS', N'Edit orders', NULL),
(N'ORDERS_CANCEL',     N'ORDERS', N'Cancel orders', NULL),
(N'ORDERS_STATUS',     N'ORDERS', N'Change order status', NULL),
-- Customers
(N'CUSTOMERS_VIEW',    N'CUSTOMERS', N'View customers', NULL),
(N'CUSTOMERS_EDIT',    N'CUSTOMERS', N'Edit customers', NULL),
-- Sales / payments
(N'PAYMENTS_VIEW',     N'SALES', N'View payments', NULL),
(N'PAYMENTS_REGISTER', N'SALES', N'Register payments', NULL),
(N'REPORTS_VIEW',      N'REPORTS', N'View reports', NULL),
-- Kitchen
(N'KITCHEN_VIEW_ORDERS',    N'KITCHEN', N'View kitchen orders', NULL),
(N'KITCHEN_UPDATE_STATUS', N'KITCHEN', N'Update kitchen status', NULL),
(N'KITCHEN_MARK_SOLD_OUT',  N'KITCHEN', N'Mark item sold out today', NULL),
-- Courier
(N'COURIER_VIEW_DELIVERIES',    N'COURIER', N'View deliveries', NULL),
(N'COURIER_UPDATE_STATUS',      N'COURIER', N'Update delivery status', NULL),
(N'COURIER_ASSIGN',             N'COURIER', N'Assign courier to order', NULL),
-- Branches
(N'BRANCHES_VIEW',   N'BRANCHES', N'View branches', NULL),
(N'BRANCHES_MANAGE', N'BRANCHES', N'Manage branches and tariffs', NULL),
-- Users & security
(N'USERS_VIEW',            N'SECURITY', N'View users', NULL),
(N'USERS_CREATE',          N'SECURITY', N'Create users', NULL),
(N'USERS_EDIT',            N'SECURITY', N'Edit users', NULL),
(N'ROLES_VIEW',			   N'SECURITY', N'View roles', NULL),
(N'ROLES_MANAGE',          N'SECURITY', N'Manage roles', NULL),
(N'PERMISSIONS_VIEW',      N'SECURITY', N'View permissions and groups', NULL),
(N'PERMISSIONS_MANAGE',    N'SECURITY', N'Manage permissions and groups', NULL),
(N'USER_PERMISSIONS_MANAGE', N'SECURITY', N'Manage user permission overrides', NULL),
(N'SECURITY_AUDIT_VIEW',   N'SECURITY', N'View security audit', NULL),
(N'ACCESS_LOGS_VIEW',      N'SECURITY', N'View access logs', NULL),
-- Customer (storefront)
(N'CUSTOMER_MENU_VIEW',       N'CUSTOMER', N'View public menu', NULL),
(N'CUSTOMER_CART',            N'CUSTOMER', N'Use cart', NULL),
(N'CUSTOMER_ORDER_PLACE',     N'CUSTOMER', N'Place order', NULL),
(N'CUSTOMER_ORDER_VIEW_OWN',  N'CUSTOMER', N'View own orders', NULL),
(N'CUSTOMER_ADDRESS_MANAGE',  N'CUSTOMER', N'Manage own addresses', NULL),
(N'CUSTOMER_PROFILE',         N'CUSTOMER', N'Manage own profile', NULL),
-- Dashboard
(N'DASHBOARD_VIEW', N'DASHBOARD', N'View admin dashboard', NULL);
GO

/* =========================================================
   2. Permission groups
   ========================================================= */

INSERT INTO dbo.PermissionGroups (Code, Name, Description) VALUES
(N'GRP_PRODUCTS',     N'Product management', NULL),
(N'GRP_INVENTORY',    N'Inventory management', NULL),
(N'GRP_DAILY_MENU',   N'Daily menu management', NULL),
(N'GRP_PROMOTIONS',   N'Promotions and coupons', NULL),
(N'GRP_ORDERS',       N'Order management', NULL),
(N'GRP_CUSTOMERS',    N'Customer management', NULL),
(N'GRP_SALES',        N'Sales and payments', NULL),
(N'GRP_REPORTS',      N'Reports', NULL),
(N'GRP_KITCHEN',      N'Kitchen operations', NULL),
(N'GRP_COURIER',      N'Delivery operations', NULL),
(N'GRP_BRANCHES',     N'Branch configuration', NULL),
(N'GRP_SECURITY',     N'Users and security', NULL),
(N'GRP_CUSTOMER_APP', N'Customer storefront', NULL),
(N'GRP_DASHBOARD',    N'Dashboard', NULL);
GO

/* =========================================================
   3. Group → permissions
   ========================================================= */

DECLARE @g INT, @p INT;

-- GRP_PRODUCTS
SELECT @g = IdPermissionGroup FROM dbo.PermissionGroups WHERE Code = N'GRP_PRODUCTS';
INSERT INTO dbo.PermissionGroupItems (IdPermissionGroup, IdPermission)
SELECT @g, IdPermission FROM dbo.Permissions
WHERE Code IN (N'PRODUCTS_VIEW', N'PRODUCTS_CREATE', N'PRODUCTS_EDIT', N'PRODUCTS_DELETE',
               N'CATEGORIES_VIEW', N'CATEGORIES_MANAGE', N'REGIONS_MANAGE');

-- GRP_INVENTORY
SELECT @g = IdPermissionGroup FROM dbo.PermissionGroups WHERE Code = N'GRP_INVENTORY';
INSERT INTO dbo.PermissionGroupItems (IdPermissionGroup, IdPermission)
SELECT @g, IdPermission FROM dbo.Permissions
WHERE Code IN (N'INGREDIENTS_VIEW', N'INGREDIENTS_MANAGE', N'STOCK_VIEW', N'STOCK_ENTRY',
               N'STOCK_ADJUST', N'RECIPES_MANAGE');

-- GRP_DAILY_MENU
SELECT @g = IdPermissionGroup FROM dbo.PermissionGroups WHERE Code = N'GRP_DAILY_MENU';
INSERT INTO dbo.PermissionGroupItems (IdPermissionGroup, IdPermission)
SELECT @g, IdPermission FROM dbo.Permissions
WHERE Code IN (N'DAILY_MENU_VIEW', N'DAILY_MENU_MANAGE', N'DAILY_MENU_PUBLISH');

-- GRP_PROMOTIONS
SELECT @g = IdPermissionGroup FROM dbo.PermissionGroups WHERE Code = N'GRP_PROMOTIONS';
INSERT INTO dbo.PermissionGroupItems (IdPermissionGroup, IdPermission)
SELECT @g, IdPermission FROM dbo.Permissions
WHERE Code IN (N'PROMOTIONS_VIEW', N'PROMOTIONS_MANAGE', N'COUPONS_VIEW', N'COUPONS_MANAGE');

-- GRP_ORDERS
SELECT @g = IdPermissionGroup FROM dbo.PermissionGroups WHERE Code = N'GRP_ORDERS';
INSERT INTO dbo.PermissionGroupItems (IdPermissionGroup, IdPermission)
SELECT @g, IdPermission FROM dbo.Permissions
WHERE Code IN (N'ORDERS_VIEW', N'ORDERS_CREATE', N'ORDERS_EDIT', N'ORDERS_CANCEL', N'ORDERS_STATUS');

-- GRP_CUSTOMERS
SELECT @g = IdPermissionGroup FROM dbo.PermissionGroups WHERE Code = N'GRP_CUSTOMERS';
INSERT INTO dbo.PermissionGroupItems (IdPermissionGroup, IdPermission)
SELECT @g, IdPermission FROM dbo.Permissions
WHERE Code IN (N'CUSTOMERS_VIEW', N'CUSTOMERS_EDIT');

-- GRP_SALES
SELECT @g = IdPermissionGroup FROM dbo.PermissionGroups WHERE Code = N'GRP_SALES';
INSERT INTO dbo.PermissionGroupItems (IdPermissionGroup, IdPermission)
SELECT @g, IdPermission FROM dbo.Permissions
WHERE Code IN (N'PAYMENTS_VIEW', N'PAYMENTS_REGISTER');

-- GRP_REPORTS
SELECT @g = IdPermissionGroup FROM dbo.PermissionGroups WHERE Code = N'GRP_REPORTS';
INSERT INTO dbo.PermissionGroupItems (IdPermissionGroup, IdPermission)
SELECT @g, IdPermission FROM dbo.Permissions WHERE Code = N'REPORTS_VIEW';

-- GRP_KITCHEN
SELECT @g = IdPermissionGroup FROM dbo.PermissionGroups WHERE Code = N'GRP_KITCHEN';
INSERT INTO dbo.PermissionGroupItems (IdPermissionGroup, IdPermission)
SELECT @g, IdPermission FROM dbo.Permissions
WHERE Code IN (N'KITCHEN_VIEW_ORDERS', N'KITCHEN_UPDATE_STATUS', N'KITCHEN_MARK_SOLD_OUT');

-- GRP_COURIER
SELECT @g = IdPermissionGroup FROM dbo.PermissionGroups WHERE Code = N'GRP_COURIER';
INSERT INTO dbo.PermissionGroupItems (IdPermissionGroup, IdPermission)
SELECT @g, IdPermission FROM dbo.Permissions
WHERE Code IN (N'COURIER_VIEW_DELIVERIES', N'COURIER_UPDATE_STATUS', N'COURIER_ASSIGN');

-- GRP_BRANCHES
SELECT @g = IdPermissionGroup FROM dbo.PermissionGroups WHERE Code = N'GRP_BRANCHES';
INSERT INTO dbo.PermissionGroupItems (IdPermissionGroup, IdPermission)
SELECT @g, IdPermission FROM dbo.Permissions
WHERE Code IN (N'BRANCHES_VIEW', N'BRANCHES_MANAGE');

-- GRP_SECURITY
SELECT @g = IdPermissionGroup FROM dbo.PermissionGroups WHERE Code = N'GRP_SECURITY';
INSERT INTO dbo.PermissionGroupItems (IdPermissionGroup, IdPermission)
SELECT @g, IdPermission FROM dbo.Permissions
WHERE Code IN (N'USERS_VIEW', N'USERS_CREATE', N'USERS_EDIT', N'ROLES_VIEW', N'ROLES_MANAGE',
               N'PERMISSIONS_VIEW', N'PERMISSIONS_MANAGE', N'USER_PERMISSIONS_MANAGE',
               N'SECURITY_AUDIT_VIEW', N'ACCESS_LOGS_VIEW');

-- GRP_CUSTOMER_APP
SELECT @g = IdPermissionGroup FROM dbo.PermissionGroups WHERE Code = N'GRP_CUSTOMER_APP';
INSERT INTO dbo.PermissionGroupItems (IdPermissionGroup, IdPermission)
SELECT @g, IdPermission FROM dbo.Permissions
WHERE Code IN (N'CUSTOMER_MENU_VIEW', N'CUSTOMER_CART', N'CUSTOMER_ORDER_PLACE',
               N'CUSTOMER_ORDER_VIEW_OWN', N'CUSTOMER_ADDRESS_MANAGE', N'CUSTOMER_PROFILE');

-- GRP_DASHBOARD
SELECT @g = IdPermissionGroup FROM dbo.PermissionGroups WHERE Code = N'GRP_DASHBOARD';
INSERT INTO dbo.PermissionGroupItems (IdPermissionGroup, IdPermission)
SELECT @g, IdPermission FROM dbo.Permissions WHERE Code = N'DASHBOARD_VIEW';
GO

/* =========================================================
   4. Role → permission groups
   ========================================================= */

DECLARE @role INT, @grp INT;

-- ADMINISTRATOR: all groups
SELECT @role = IdRole FROM dbo.Roles WHERE Name = N'ADMINISTRATOR';
INSERT INTO dbo.RolePermissionGroups (IdRole, IdPermissionGroup)
SELECT @role, IdPermissionGroup FROM dbo.PermissionGroups WHERE IsActive = 1;

-- MANAGER: almost all except full security write (still has users view via optional later)
SELECT @role = IdRole FROM dbo.Roles WHERE Name = N'MANAGER';
INSERT INTO dbo.RolePermissionGroups (IdRole, IdPermissionGroup)
SELECT @role, IdPermissionGroup FROM dbo.PermissionGroups
WHERE Code IN (N'GRP_DASHBOARD', N'GRP_PRODUCTS', N'GRP_INVENTORY', N'GRP_DAILY_MENU',
               N'GRP_PROMOTIONS', N'GRP_ORDERS', N'GRP_CUSTOMERS', N'GRP_SALES',
               N'GRP_REPORTS', N'GRP_KITCHEN', N'GRP_COURIER', N'GRP_BRANCHES');

-- OPERATIONS
SELECT @role = IdRole FROM dbo.Roles WHERE Name = N'OPERATIONS';
INSERT INTO dbo.RolePermissionGroups (IdRole, IdPermissionGroup)
SELECT @role, IdPermissionGroup FROM dbo.PermissionGroups
WHERE Code IN (N'GRP_DASHBOARD', N'GRP_PRODUCTS', N'GRP_DAILY_MENU', N'GRP_PROMOTIONS',
               N'GRP_ORDERS', N'GRP_CUSTOMERS');

-- WAREHOUSE
SELECT @role = IdRole FROM dbo.Roles WHERE Name = N'WAREHOUSE';
INSERT INTO dbo.RolePermissionGroups (IdRole, IdPermissionGroup)
SELECT @role, IdPermissionGroup FROM dbo.PermissionGroups
WHERE Code IN (N'GRP_INVENTORY', N'GRP_PRODUCTS');

-- CHEF
SELECT @role = IdRole FROM dbo.Roles WHERE Name = N'CHEF';
INSERT INTO dbo.RolePermissionGroups (IdRole, IdPermissionGroup)
SELECT @role, IdPermissionGroup FROM dbo.PermissionGroups
WHERE Code IN (N'GRP_KITCHEN', N'GRP_DAILY_MENU');

-- COURIER
SELECT @role = IdRole FROM dbo.Roles WHERE Name = N'COURIER';
INSERT INTO dbo.RolePermissionGroups (IdRole, IdPermissionGroup)
SELECT @role, IdPermissionGroup FROM dbo.PermissionGroups WHERE Code = N'GRP_COURIER';

-- CUSTOMER_SERVICE
SELECT @role = IdRole FROM dbo.Roles WHERE Name = N'CUSTOMER_SERVICE';
INSERT INTO dbo.RolePermissionGroups (IdRole, IdPermissionGroup)
SELECT @role, IdPermissionGroup FROM dbo.PermissionGroups
WHERE Code IN (N'GRP_ORDERS', N'GRP_CUSTOMERS', N'GRP_DASHBOARD');

-- CASHIER
SELECT @role = IdRole FROM dbo.Roles WHERE Name = N'CASHIER';
INSERT INTO dbo.RolePermissionGroups (IdRole, IdPermissionGroup)
SELECT @role, IdPermissionGroup FROM dbo.PermissionGroups
WHERE Code IN (N'GRP_SALES', N'GRP_ORDERS', N'GRP_DASHBOARD');

-- CUSTOMER
SELECT @role = IdRole FROM dbo.Roles WHERE Name = N'CUSTOMER';
INSERT INTO dbo.RolePermissionGroups (IdRole, IdPermissionGroup)
SELECT @role, IdPermissionGroup FROM dbo.PermissionGroups WHERE Code = N'GRP_CUSTOMER_APP';
GO

/* =========================================================
   5. Menu items (UI labels can be Spanish in views; Label here EN/stable)
   ========================================================= */

INSERT INTO dbo.MenuItems (Code, Label, Route, Icon, MenuGroup, Context, SortOrder) VALUES
-- ADMIN
(N'MENU_ADMIN_DASHBOARD',    N'Dashboard',           N'/admin/dashboard',					N'bi-speedometer2',        N'Principal',           N'ADMIN', 10),
(N'MENU_ADMIN_PRODUCTS',     N'Products',            N'/admin/products',					N'bi-egg-fried',           N'Catalog',             N'ADMIN', 20),
(N'MENU_ADMIN_INGREDIENTS',  N'Ingredients',         N'/admin/ingredients',					N'bi-box-seam',            N'Catalog',             N'ADMIN', 30),
(N'MENU_ADMIN_STOCK',        N'Stock',               N'/admin/stock',						N'bi-clipboard-data',      N'Catalog',             N'ADMIN', 40),
(N'MENU_ADMIN_DAILY_MENUS',  N'Daily menus',         N'/admin/daily-menus',					N'bi-calendar-day',        N'Operations',          N'ADMIN', 50),
(N'MENU_ADMIN_PROMOTIONS',   N'Promotions',          N'/admin/promotions',					N'bi-percent',             N'Operations',          N'ADMIN', 60),
(N'MENU_ADMIN_COUPONS',      N'Coupons',             N'/admin/coupons',						N'bi-ticket-perforated',   N'Operations',          N'ADMIN', 70),
(N'MENU_ADMIN_ORDERS',       N'Orders',              N'/admin/orders',						N'bi-bag-check',           N'Operations',          N'ADMIN', 80),
(N'MENU_ADMIN_CUSTOMERS',    N'Customers',           N'/admin/customers',					N'bi-person-lines-fill',   N'People',              N'ADMIN', 90),
(N'MENU_ADMIN_USERS',        N'Users',               N'/admin/users',						N'bi-people',              N'People',              N'ADMIN', 100),
(N'MENU_ADMIN_ROLES',        N'Roles',               N'/admin/security?tab=roles',			N'bi-shield-check',        N'Security',            N'ADMIN', 110),
(N'MENU_ADMIN_PERMISSIONS',  N'Permissions',         N'/admin/security?tab=permissions',	N'bi-key',                 N'Security',            N'ADMIN', 120),
(N'MENU_ADMIN_AUDIT',        N'Access audit',        N'/admin/audit',						N'bi-shield-lock',         N'Security',            N'ADMIN', 130),
(N'MENU_ADMIN_BRANCHES',     N'Branches',            N'/admin/branches',					N'bi-shop',                N'Config',              N'ADMIN', 140),
-- KITCHEN
(N'MENU_KITCHEN_ORDERS',     N'Kitchen orders',      N'/kitchen/orders',					N'bi-egg-fried',           N'Kitchen',             N'KITCHEN', 10),
-- COURIER
(N'MENU_COURIER_DELIVERIES', N'Deliveries',          N'/courier/deliveries',				N'bi-bicycle',			   N'Delivery',            N'COURIER', 10),
-- CUSTOMER
(N'MENU_CUSTOMER_MENU',      N'Menu',                N'/menu',								N'bi-journal-richtext',    N'Store',               N'CUSTOMER', 10),
(N'MENU_CUSTOMER_CART',      N'Cart',                N'/customer/cart',						N'bi-cart3',               N'Store',               N'CUSTOMER', 20),
(N'MENU_CUSTOMER_ORDERS',    N'My orders',           N'/customer/orders',					N'bi-bag',                 N'Account',             N'CUSTOMER', 30),
(N'MENU_CUSTOMER_ADDRESSES', N'Addresses',           N'/customer/addresses',				N'bi-geo-alt',             N'Account',             N'CUSTOMER', 40),
(N'MENU_CUSTOMER_PROFILE',   N'Profile',             N'/customer/profile',					N'bi-person',              N'Account',             N'CUSTOMER', 50);
GO


/* =========================================================
   6. Menu → required permissions (at least one)
   ========================================================= */

INSERT INTO dbo.MenuItemPermissions (IdMenuItem, IdPermission)
SELECT m.IdMenuItem, p.IdPermission
FROM dbo.MenuItems m
CROSS JOIN dbo.Permissions p
WHERE
  (m.Code = N'MENU_ADMIN_DASHBOARD'   AND p.Code = N'DASHBOARD_VIEW')
  OR (m.Code = N'MENU_ADMIN_PRODUCTS'    AND p.Code = N'PRODUCTS_VIEW')
  OR (m.Code = N'MENU_ADMIN_INGREDIENTS' AND p.Code = N'INGREDIENTS_VIEW')
  OR (m.Code = N'MENU_ADMIN_STOCK'       AND p.Code = N'STOCK_VIEW')
  OR (m.Code = N'MENU_ADMIN_DAILY_MENUS' AND p.Code = N'DAILY_MENU_VIEW')
  OR (m.Code = N'MENU_ADMIN_PROMOTIONS'  AND p.Code = N'PROMOTIONS_VIEW')
  OR (m.Code = N'MENU_ADMIN_COUPONS'     AND p.Code = N'COUPONS_VIEW')
  OR (m.Code = N'MENU_ADMIN_ORDERS'      AND p.Code = N'ORDERS_VIEW')
  OR (m.Code = N'MENU_ADMIN_CUSTOMERS'   AND p.Code = N'CUSTOMERS_VIEW')
  OR (m.Code = N'MENU_ADMIN_USERS'       AND p.Code = N'USERS_VIEW')
  OR (m.Code = N'MENU_ADMIN_ROLES'       AND p.Code = N'ROLES_MANAGE')
  OR (m.Code = N'MENU_ADMIN_PERMISSIONS' AND p.Code = N'PERMISSIONS_MANAGE')
  OR (m.Code = N'MENU_ADMIN_AUDIT'       AND p.Code = N'ACCESS_LOGS_VIEW')
  OR (m.Code = N'MENU_ADMIN_BRANCHES'    AND p.Code = N'BRANCHES_VIEW')
  OR (m.Code = N'MENU_KITCHEN_ORDERS'    AND p.Code = N'KITCHEN_VIEW_ORDERS')
  OR (m.Code = N'MENU_COURIER_DELIVERIES'AND p.Code = N'COURIER_VIEW_DELIVERIES')
  OR (m.Code = N'MENU_CUSTOMER_MENU'     AND p.Code = N'CUSTOMER_MENU_VIEW')
  OR (m.Code = N'MENU_CUSTOMER_CART'     AND p.Code = N'CUSTOMER_CART')
  OR (m.Code = N'MENU_CUSTOMER_ORDERS'   AND p.Code = N'CUSTOMER_ORDER_VIEW_OWN')
  OR (m.Code = N'MENU_CUSTOMER_ADDRESSES'AND p.Code = N'CUSTOMER_ADDRESS_MANAGE')
  OR (m.Code = N'MENU_CUSTOMER_PROFILE'  AND p.Code = N'CUSTOMER_PROFILE');
GO

/* =========================================================
   7. Demo branch + shifts + config (optional operational seed)
   ========================================================= */

IF NOT EXISTS (SELECT 1 FROM dbo.Branches)
BEGIN
    INSERT INTO dbo.Branches (Name, Address, Lat, Lng, CoverageRadiusKm, IsActive, TenantKey, Phone)
    VALUES (N'Miraflores', N'Av. Larco 123, Miraflores, Lima', -12.1211000, -77.0297000, 6.00, 1, N'lima', N'014000000');

    DECLARE @branchId INT = SCOPE_IDENTITY();

    INSERT INTO dbo.BranchShifts (IdBranch, Shift, StartTime, EndTime, IsActive) VALUES
    (@branchId, N'BREAKFAST', '07:00', '11:00', 1),
    (@branchId, N'LUNCH',     '12:00', '16:00', 1),
    (@branchId, N'DINNER',    '18:00', '22:00', 1);

    INSERT INTO dbo.BranchConfigs (
        IdBranch, MinOrderAmount, DefaultDeliveryFee, FreeDeliveryFromAmount,
        AvgPrepTimeMinutes, AcceptsOnlineOrders, OpenTime, CloseTime
    ) VALUES (
        @branchId, 20.00, 8.00, 60.00, 35, 1, '07:00', '22:30'
    );

    INSERT INTO dbo.DeliveryTariffs (IdBranch, Name, RuleType, MinDistanceKm, MaxDistanceKm, Fee, SortOrder)
    VALUES
    (@branchId, N'0-3 km',  N'DISTANCE_KM', 0, 3, 6.00, 10),
    (@branchId, N'3-6 km',  N'DISTANCE_KM', 3, 6, 10.00, 20),
    (@branchId, N'6-10 km', N'DISTANCE_KM', 6, 10, 15.00, 30);
END
GO

/* =========================================================
   8. Catalog basics
   ========================================================= */

IF NOT EXISTS (SELECT 1 FROM dbo.Regions)
BEGIN
    INSERT INTO dbo.Regions (Name, Description) VALUES
    (N'COAST', N'Costa'),
    (N'HIGHLANDS', N'Sierra'),
    (N'JUNGLE', N'Selva');
END
GO

IF NOT EXISTS (SELECT 1 FROM dbo.Categories)
BEGIN
    INSERT INTO dbo.Categories (Name, Description) VALUES
    (N'Entradas', N'Starters'),
    (N'Fondos', N'Main courses'),
    (N'Bebidas', N'Drinks'),
    (N'Postres', N'Desserts');
END
GO

/* =========================================================
   9. Geo Lima sample (optional)
   ========================================================= */

IF NOT EXISTS (SELECT 1 FROM dbo.GeoDepartments WHERE IdDepartment = N'15')
BEGIN
    INSERT INTO dbo.GeoDepartments (IdDepartment, Name) VALUES (N'15', N'Lima');
    INSERT INTO dbo.GeoProvinces (IdProvince, IdDepartment, Name) VALUES (N'1501', N'15', N'Lima');
    INSERT INTO dbo.GeoDistricts (IdDistrict, IdProvince, Name, CenterLat, CenterLng) VALUES
    (N'150122', N'1501', N'Miraflores', -12.1211000, -77.0297000),
    (N'150140', N'1501', N'San Isidro', -12.0970000, -77.0365000),
    (N'150131', N'1501', N'San Borja', -12.1089000, -76.9989000),
    (N'150141', N'1501', N'Surco', -12.1364000, -76.9951000);
END
GO

PRINT N'Seed V1_1 security completed.';
GO

-- Categories/Regions already seeded
DECLARE @cat INT = (SELECT TOP 1 IdCategory FROM dbo.Categories WHERE Name = N'Fondos');
DECLARE @reg INT = (SELECT TOP 1 IdRegion FROM dbo.Regions WHERE Name = N'COAST');

IF @cat IS NOT NULL AND NOT EXISTS (SELECT 1 FROM dbo.Products)
BEGIN
    INSERT INTO dbo.Products (IdCategory, IdRegion, Name, Description, BasePrice, PrepTimeMinutes, IsUnlimitedStock, IsActive, CreatedAt)
    VALUES
    (@cat, @reg, N'Ceviche clásico', N'Pescado fresco, limón, cebolla y ají.', 32.00, 20, 1, 1, SYSUTCDATETIME()),
    (@cat, @reg, N'Lomo saltado', N'Lomo, cebolla, tomate y papas fritas.', 38.00, 25, 1, 1, SYSUTCDATETIME()),
    (@cat, @reg, N'Ají de gallina', N'Crema de ají amarillo con pollo.', 28.00, 25, 1, 1, SYSUTCDATETIME());
END
GO

INSERT INTO dbo.Ingredients (Name, Unit, IsUnlimitedStock, MinStockAlert, IsActive)
VALUES (N'Pescado fresco', N'kg', 0, 5, 1),
        (N'Limón', N'kg', 0, 2, 1),
        (N'Pollo', N'kg', 0, 5, 1);
GO
