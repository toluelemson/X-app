package org.example.xchange.data.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Setter
@Getter
@Entity(name = "fx_rate")
public class FxRate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonProperty("Tp")
    private String type;

    @JsonProperty("Dt")
    private String date;

    @JsonProperty("CcyAmt")
    @OneToMany(cascade = CascadeType.ALL)
    private List<CurrencyAmount> currencyAmountList;


    @Setter
    @Getter
    @Entity(name = "currency_amount")
    public static class CurrencyAmount {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @JsonProperty("Ccy")
        private String currency;

        @JsonProperty("Amt")
        private BigDecimal amount;

    }
}

