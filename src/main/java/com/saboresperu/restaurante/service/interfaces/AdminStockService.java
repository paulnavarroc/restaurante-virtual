package com.saboresperu.restaurante.service.interfaces;

import com.saboresperu.restaurante.dto.StockAdjustRequest;
import com.saboresperu.restaurante.dto.StockEntryRequest;
import com.saboresperu.restaurante.dto.StockRowDto;

import java.util.List;

public interface AdminStockService {
    List<StockRowDto> listByBranch(Integer branchId);
    void registerEntry(StockEntryRequest request, Integer userId);
    void adjust(StockAdjustRequest request, Integer userId);
}