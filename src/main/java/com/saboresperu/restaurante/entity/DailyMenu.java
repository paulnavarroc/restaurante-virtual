package com.saboresperu.restaurante.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "DailyMenus")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyMenu {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IdDailyMenu")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "IdBranch", nullable = false)
    private Branch branch;

    @Column(name = "MenuDate", nullable = false)
    private LocalDate menuDate;

    @Column(name = "Status", nullable = false, length = 20)
    private String status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdPublishedByUser")
    private User publishedBy;

    @Column(name = "PublishedAt")
    private LocalDateTime publishedAt;

    @OneToMany(mappedBy = "dailyMenu", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DailyMenuItem> items = new ArrayList<>();
}
