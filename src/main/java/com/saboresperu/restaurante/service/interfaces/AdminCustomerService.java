package com.saboresperu.restaurante.service.interfaces;

import com.saboresperu.restaurante.dto.CustomerAdminDetailDto;
import com.saboresperu.restaurante.dto.CustomerAdminRowDto;

import java.util.List;

public interface AdminCustomerService {
    List<CustomerAdminRowDto> search(String query);
    CustomerAdminDetailDto getDetail(Integer customerId);
    void setUserActive(Integer customerId, boolean active);
}