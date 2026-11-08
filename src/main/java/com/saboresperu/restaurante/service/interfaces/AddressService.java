package com.saboresperu.restaurante.service.interfaces;

import com.saboresperu.restaurante.dto.AddressFormDto;
import com.saboresperu.restaurante.entity.CustomerAddress;

import java.util.List;

public interface AddressService {

    List<CustomerAddress> listByCustomerId(Integer customerId);

    Integer create(Integer customerId, AddressFormDto form);

    void update(Integer addressId, Integer customerId, AddressFormDto form);

    void markAsPrimary(Integer addressId, Integer customerId);

    /** Soft-delete (IsActive = false). */
    void delete(Integer addressId, Integer customerId);

    CustomerAddress getOwned(Integer addressId, Integer customerId);
}