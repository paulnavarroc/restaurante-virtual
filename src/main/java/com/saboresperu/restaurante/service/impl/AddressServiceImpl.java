package com.saboresperu.restaurante.service.impl;

import com.saboresperu.restaurante.entity.CustomerAddress;
import com.saboresperu.restaurante.repository.CustomerAddressRepository;
import com.saboresperu.restaurante.service.interfaces.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {
    private final CustomerAddressRepository customerAddressRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CustomerAddress> listByCustomerId(Integer customerId) {
        return customerAddressRepository.findByCustomerIdAndIsActiveTrueOrderByIsPrimaryDesc(customerId);
    }
}