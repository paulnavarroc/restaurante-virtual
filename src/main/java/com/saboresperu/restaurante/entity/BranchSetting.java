package com.saboresperu.restaurante.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "BranchSettings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BranchSetting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IdBranchSetting")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "IdBranch", nullable = false)
    private Branch branch;

    @Column(name = "SettingKey", nullable = false, length = 80)
    private String settingKey;

    @Column(name = "SettingValue", length = 500)
    private String settingValue;
}
