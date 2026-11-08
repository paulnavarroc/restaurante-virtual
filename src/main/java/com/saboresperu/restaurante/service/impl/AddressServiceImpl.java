package com.saboresperu.restaurante.service.impl;

import com.saboresperu.restaurante.dto.AddressFormDto;
import com.saboresperu.restaurante.entity.Customer;
import com.saboresperu.restaurante.entity.CustomerAddress;
import com.saboresperu.restaurante.entity.GeoDistrict;
import com.saboresperu.restaurante.repository.CustomerAddressRepository;
import com.saboresperu.restaurante.repository.CustomerRepository;
import com.saboresperu.restaurante.repository.GeoDistrictRepository;
import com.saboresperu.restaurante.service.interfaces.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {
    private final CustomerAddressRepository customerAddressRepository;
    private final CustomerRepository customerRepository;
    private final GeoDistrictRepository geoDistrictRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CustomerAddress> listByCustomerId(Integer customerId) {
        return customerAddressRepository.findByCustomerIdAndIsActiveTrueOrderByIsPrimaryDesc(customerId);
    }

    @Override
    @Transactional
    public Integer create(Integer customerId, AddressFormDto form) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado"));
        GeoDistrict district = geoDistrictRepository.findById(form.districtId())
                .orElseThrow(() -> new IllegalArgumentException("Distrito no encontrado"));
        boolean isFirstAddress = customerAddressRepository
                .findByCustomerIdAndIsActiveTrueOrderByIsPrimaryDesc(customerId).isEmpty();

        CustomerAddress address = CustomerAddress.builder()
                .customer(customer)
                .district(district)
                .alias(form.alias())
                .urbanization(form.urbanization())
                .street(form.street())
                .number(form.number())
                .reference(form.reference())
                .isPrimary(isFirstAddress)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();

        return customerAddressRepository.save(address).getId();
    }
}
