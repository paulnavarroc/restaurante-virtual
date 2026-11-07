package com.saboresperu.restaurante.service.interfaces;

import com.saboresperu.restaurante.dto.RegisterRequestDto;
import com.saboresperu.restaurante.entity.User;

public interface UserService {
    User registerCustomer(RegisterRequestDto form);
}