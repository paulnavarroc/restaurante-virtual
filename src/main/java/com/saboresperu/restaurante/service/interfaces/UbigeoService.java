package com.saboresperu.restaurante.service.interfaces;

import com.saboresperu.restaurante.dto.UbigeoOptionDTO;
import java.util.List;

public interface UbigeoService {
    List<UbigeoOptionDTO> listarDepartamentos();
    List<UbigeoOptionDTO> listarProvincias(String idDepartamento);
    List<UbigeoOptionDTO> listarDistritos(String idProvincia);
}