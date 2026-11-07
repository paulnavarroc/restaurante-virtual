/*
  V1_2__seed_demo.sql
  Producto + receta + stock + menú del día publicado (hoy)
  Ejecutar DESPUÉS de V1 y V1_1
*/
USE SaboresPeru;
GO

SET NOCOUNT ON;

/* ---- Categoría / región (por si el schema no las insertó) ---- */
IF NOT EXISTS (SELECT 1 FROM dbo.Categories WHERE Name = N'Platos de fondo')
    INSERT INTO dbo.Categories (Name, Description, IsActive)
    VALUES (N'Platos de fondo', N'Platos principales', 1);

IF NOT EXISTS (SELECT 1 FROM dbo.Regions WHERE Name = N'COAST')
    INSERT INTO dbo.Regions (Name, Description, IsActive)
    VALUES (N'COAST', N'Costa', 1);

DECLARE @IdCategory INT = (SELECT TOP 1 IdCategory FROM dbo.Categories WHERE Name = N'Platos de fondo');
DECLARE @IdRegion   INT = (SELECT TOP 1 IdRegion FROM dbo.Regions WHERE Name = N'COAST');
DECLARE @IdBranch   INT = (SELECT TOP 1 IdBranch FROM dbo.Branches WHERE IsActive = 1 ORDER BY IdBranch);

IF @IdBranch IS NULL
BEGIN
    RAISERROR(N'No hay sede activa. Revisa V1_1 seed.', 16, 1);
    RETURN;
END

/* ---- Producto ---- */
IF NOT EXISTS (SELECT 1 FROM dbo.Products WHERE Name = N'Ceviche clásico')
BEGIN
    INSERT INTO dbo.Products
        (IdCategory, IdRegion, Name, Description, BasePrice, PrepTimeMinutes, IsUnlimitedStock, IsActive)
    VALUES
        (@IdCategory, @IdRegion, N'Ceviche clásico',
         N'Pescado fresco, limón, cebolla, ají, camote', 32.00, 20, 0, 1);
END

DECLARE @IdProduct INT = (SELECT TOP 1 IdProduct FROM dbo.Products WHERE Name = N'Ceviche clásico');

/* ---- Insumos ---- */
IF NOT EXISTS (SELECT 1 FROM dbo.Ingredients WHERE Name = N'Pescado blanco')
    INSERT INTO dbo.Ingredients (Name, Unit, IsUnlimitedStock, MinStockAlert, IsActive)
    VALUES (N'Pescado blanco', N'kg', 0, 2.0000, 1);

IF NOT EXISTS (SELECT 1 FROM dbo.Ingredients WHERE Name = N'Limón')
    INSERT INTO dbo.Ingredients (Name, Unit, IsUnlimitedStock, MinStockAlert, IsActive)
    VALUES (N'Limón', N'kg', 0, 1.0000, 1);

DECLARE @IdPescado INT = (SELECT IdIngredient FROM dbo.Ingredients WHERE Name = N'Pescado blanco');
DECLARE @IdLimon   INT = (SELECT IdIngredient FROM dbo.Ingredients WHERE Name = N'Limón');

/* ---- Receta ---- */
IF NOT EXISTS (SELECT 1 FROM dbo.Recipes WHERE IdProduct = @IdProduct)
BEGIN
    INSERT INTO dbo.Recipes (IdProduct, Version, IsActive) VALUES (@IdProduct, 1, 1);
END

DECLARE @IdRecipe INT = (SELECT IdRecipe FROM dbo.Recipes WHERE IdProduct = @IdProduct);

IF NOT EXISTS (SELECT 1 FROM dbo.RecipeItems WHERE IdRecipe = @IdRecipe AND IdIngredient = @IdPescado)
    INSERT INTO dbo.RecipeItems (IdRecipe, IdIngredient, Quantity) VALUES (@IdRecipe, @IdPescado, 0.2500);

IF NOT EXISTS (SELECT 1 FROM dbo.RecipeItems WHERE IdRecipe = @IdRecipe AND IdIngredient = @IdLimon)
    INSERT INTO dbo.RecipeItems (IdRecipe, IdIngredient, Quantity) VALUES (@IdRecipe, @IdLimon, 0.1000);

/* ---- Stock por sede ---- */
IF NOT EXISTS (SELECT 1 FROM dbo.IngredientStocks WHERE IdBranch = @IdBranch AND IdIngredient = @IdPescado)
    INSERT INTO dbo.IngredientStocks (IdBranch, IdIngredient, QuantityAvailable, QuantityReserved, AverageCost, LastCost, MinStockAlert, Version)
    VALUES (@IdBranch, @IdPescado, 10.0000, 0, 18.0000, 18.0000, 2.0000, 0);

IF NOT EXISTS (SELECT 1 FROM dbo.IngredientStocks WHERE IdBranch = @IdBranch AND IdIngredient = @IdLimon)
    INSERT INTO dbo.IngredientStocks (IdBranch, IdIngredient, QuantityAvailable, QuantityReserved, AverageCost, LastCost, MinStockAlert, Version)
    VALUES (@IdBranch, @IdLimon, 5.0000, 0, 3.5000, 3.5000, 1.0000, 0);

/* ---- Menú del día (hoy, PUBLISHED) ---- */
DECLARE @Today DATE = CAST(SYSUTCDATETIME() AS DATE);

IF NOT EXISTS (SELECT 1 FROM dbo.DailyMenus WHERE IdBranch = @IdBranch AND MenuDate = @Today)
BEGIN
    INSERT INTO dbo.DailyMenus (IdBranch, MenuDate, Status, PublishedAt)
    VALUES (@IdBranch, @Today, N'PUBLISHED', SYSUTCDATETIME());
END
ELSE
BEGIN
    UPDATE dbo.DailyMenus
    SET Status = N'PUBLISHED', PublishedAt = SYSUTCDATETIME()
    WHERE IdBranch = @IdBranch AND MenuDate = @Today;
END

DECLARE @IdDailyMenu INT = (
    SELECT IdDailyMenu FROM dbo.DailyMenus WHERE IdBranch = @IdBranch AND MenuDate = @Today
);

IF NOT EXISTS (SELECT 1 FROM dbo.DailyMenuItems WHERE IdDailyMenu = @IdDailyMenu AND IdProduct = @IdProduct)
BEGIN
    INSERT INTO dbo.DailyMenuItems
        (IdDailyMenu, IdProduct, IsAvailable, PriceOverride, MaxQuota, SoldQuantity)
    VALUES
        (@IdDailyMenu, @IdProduct, 1, NULL, 50, 0);
END

/* ---- Método de pago (si no está en V1) ---- */
IF NOT EXISTS (SELECT 1 FROM dbo.PaymentMethods WHERE Code = N'CASH')
    INSERT INTO dbo.PaymentMethods (Code, Name, IsActive, SortOrder)
    VALUES (N'CASH', N'Efectivo', 1, 10);

/* ---- Config sede / tarifa simple (si tablas existen y vacías) ---- */
IF NOT EXISTS (SELECT 1 FROM dbo.BranchConfigs WHERE IdBranch = @IdBranch)
    INSERT INTO dbo.BranchConfigs (IdBranch, MinOrderAmount, DefaultDeliveryFee, FreeDeliveryFromAmount, AvgPrepTimeMinutes, AcceptsOnlineOrders)
    VALUES (@IdBranch, 20.00, 8.00, 80.00, 35, 1);

PRINT N'V1_2 seed demo OK';
GO