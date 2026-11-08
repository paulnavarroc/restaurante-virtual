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
        return customerAddressRepository.findActiveWithDistrict(customerId);
    }

    @Override
    @Transactional
    public Integer create(Integer customerId, AddressFormDto form) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado"));

        GeoDistrict district = requireDistrict(form.districtId());

        boolean isFirst = customerAddressRepository
                .findByCustomerIdAndIsActiveTrueOrderByIsPrimaryDesc(customerId)
                .isEmpty();

        CustomerAddress address = CustomerAddress.builder()
                .customer(customer)
                .district(district)
                .alias(blankToNull(form.alias()))
                .urbanization(blankToNull(form.urbanization()))
                .street(form.street().trim())
                .number(blankToNull(form.number()))
                .reference(blankToNull(form.reference()))
                .isPrimary(isFirst)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();

        return customerAddressRepository.save(address).getId();
    }

    @Override
    @Transactional
    public void update(Integer addressId, Integer customerId, AddressFormDto form) {
        CustomerAddress address = getOwnedActive(addressId, customerId);
        GeoDistrict district = requireDistrict(form.districtId());

        address.setDistrict(district);
        address.setAlias(blankToNull(form.alias()));
        address.setUrbanization(blankToNull(form.urbanization()));
        address.setStreet(form.street().trim());
        address.setNumber(blankToNull(form.number()));
        address.setReference(blankToNull(form.reference()));

        customerAddressRepository.save(address);
    }

    @Override
    @Transactional
    public void markAsPrimary(Integer addressId, Integer customerId) {
        CustomerAddress target = getOwnedActive(addressId, customerId);

        for (CustomerAddress a : customerAddressRepository
                .findByCustomerIdAndIsActiveTrueOrderByIsPrimaryDesc(customerId)) {
            a.setIsPrimary(a.getId().equals(target.getId()));
            customerAddressRepository.save(a);
        }
    }

    @Override
    @Transactional
    public void delete(Integer addressId, Integer customerId) {
        CustomerAddress address = getOwnedActive(addressId, customerId);
        boolean wasPrimary = Boolean.TRUE.equals(address.getIsPrimary());

        address.setIsActive(false);
        address.setIsPrimary(false);
        customerAddressRepository.save(address);

        // Si era principal, promover otra activa
        if (wasPrimary) {
            List<CustomerAddress> remaining = customerAddressRepository
                    .findByCustomerIdAndIsActiveTrueOrderByIsPrimaryDesc(customerId);
            if (!remaining.isEmpty()) {
                CustomerAddress next = remaining.get(0);
                next.setIsPrimary(true);
                customerAddressRepository.save(next);
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerAddress getOwned(Integer addressId, Integer customerId) {
        return customerAddressRepository
                .findByIdAndCustomerIdWithDistrict(addressId, customerId)
                .orElseThrow(() -> new IllegalArgumentException("Dirección no encontrada"));
    }

    private CustomerAddress getOwnedActive(Integer addressId, Integer customerId) {
        CustomerAddress address = getOwned(addressId, customerId);
        if (!Boolean.TRUE.equals(address.getIsActive())) {
            throw new IllegalArgumentException("Dirección no encontrada");
        }
        return address;
    }

    private GeoDistrict requireDistrict(String districtId) {
        if (districtId == null || districtId.isBlank()) {
            throw new IllegalArgumentException("Distrito obligatorio");
        }
        String id = districtId.trim();
        return geoDistrictRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Distrito no encontrado"));
    }

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}