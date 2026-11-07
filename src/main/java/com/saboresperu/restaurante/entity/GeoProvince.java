package com.saboresperu.restaurante.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "GeoProvinces")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GeoProvince {
    @Id
    @Column(name = "IdProvince", length = 4, columnDefinition = "char(4)")
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "IdDepartment", nullable = false, columnDefinition = "char(2)")
    private GeoDepartment department;

    @Column(name = "Name", nullable = false, length = 100)
    private String name;
}
