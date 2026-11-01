package com.saboresperu.restaurante.service.interfaces;

import com.saboresperu.restaurante.dto.DashboardDTO;

public interface DashboardService {
    DashboardDTO obtener(Integer sedeId);
}