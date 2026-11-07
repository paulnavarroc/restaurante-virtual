package com.saboresperu.restaurante.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "GeoDepartments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GeoDepartment {
    @Id
    @Column(name = "IdDepartment", length = 2, columnDefinition = "char(2)")
    private String id;

    @Column(name = "Name", nullable = false, length = 100)
    private String name;
}