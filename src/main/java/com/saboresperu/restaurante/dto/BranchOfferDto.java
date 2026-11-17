package com.saboresperu.restaurante.dto;

import java.math.BigDecimal;
import java.util.List;

public record BranchOfferDto(
        Integer branchId,
        String branchName,
        Integer dailyMenuItemId,
        Integer remainingQuota,
        boolean available
) {}