package com.saboresperu.restaurante.service.interfaces;

import com.saboresperu.restaurante.entity.Customer;

public interface CustomerService {
    Customer requireByUserEmail(String email);
}