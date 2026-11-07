package com.saboresperu.restaurante.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalTime;

@Entity
@Table(name = "BranchShifts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BranchShift {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IdBranchShift")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "IdBranch", nullable = false)
    private Branch branch;

    @Column(name = "Shift", nullable = false, length = 20)
    private String shift;

    @Column(name = "StartTime", nullable = false)
    private LocalTime startTime;

    @Column(name = "EndTime", nullable = false)
    private LocalTime endTime;

    @Column(name = "IsActive", nullable = false)
    private Boolean isActive;
}