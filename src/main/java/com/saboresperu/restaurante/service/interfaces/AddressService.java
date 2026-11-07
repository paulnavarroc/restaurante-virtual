package com.saboresperu.restaurante.service.interfaces;

import com.saboresperu.restaurante.entity.CustomerAddress;
import java.util.List;

public interface AddressService {
    List<CustomerAddress> listByCustomerId(Integer customerId);
}