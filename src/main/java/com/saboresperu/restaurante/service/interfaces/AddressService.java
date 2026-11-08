package com.saboresperu.restaurante.service.interfaces;

import com.saboresperu.restaurante.dto.AddressFormDto;
import com.saboresperu.restaurante.entity.CustomerAddress;
import java.util.List;

public interface AddressService {
    List<CustomerAddress> listByCustomerId(Integer customerId);
    Integer create(Integer customerId, AddressFormDto form);
}
