package com.saboresperu.restaurante.service.impl;

import com.saboresperu.restaurante.service.interfaces.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {
    private final CustomerAddressRepository customerAddressRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CustomerAddress> listByCustomerId(Integer customerId) {
        return customerAddressRepository
                .findByCustomerIdAndIsActiveTrueOrderByIsPrimaryDesc(customerId);
    }
}