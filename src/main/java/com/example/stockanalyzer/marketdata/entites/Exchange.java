package com.example.stockanalyzer.marketdata.entites;


import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "exchange")
public class Exchange extends BaseModel {


    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "country", nullable = false, length = 100)
    private String country;

    @Column(name = "currency", nullable = false, length = 10)
    private String currency;

    @Column(name = "open_time", nullable = false)
    private LocalDateTime openTime;

    @Column(name = "close_time", nullable = false)
    private LocalDateTime closeTime;

    @Column(name = "timezone", nullable = false, length = 50)
    private String timezone;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;
}
