package com.saboresperu.restaurante.service.interfaces;

import com.saboresperu.restaurante.dto.CustomerProfileFormDto;
import com.saboresperu.restaurante.dto.ProfileUpdateResult;
import com.saboresperu.restaurante.entity.Customer;

public interface CustomerService {
    Customer requireByUserEmail(String email);
    CustomerProfileFormDto getProfileForm(Integer customerId);
    ProfileUpdateResult updateProfile(Integer customerId, CustomerProfileFormDto form);
}