package com.saboresperu.restaurante.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "UserAccessLogs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAccessLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IdAccessLog")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdUser")
    private User user;

    @Column(name = "EmailAttempted", length = 150)
    private String emailAttempted;

    @Column(name = "EventType", nullable = false, length = 30)
    private String eventType;

    @Column(name = "IsSuccess", nullable = false)
    private Boolean isSuccess;

    @Column(name = "Ip", length = 45)
    private String ip;

    @Column(name = "UserAgent", length = 500)
    private String userAgent;

    @Column(name = "Browser", length = 80)
    private String browser;

    @Column(name = "OperatingSystem", length = 80)
    private String operatingSystem;

    @Column(name = "Device", length = 40)
    private String device;

    @Column(name = "SessionId", length = 100)
    private String sessionId;

    @Column(name = "Detail", length = 300)
    private String detail;

    @Column(name = "CreatedAt", nullable = false)
    private LocalDateTime createdAt;
}
