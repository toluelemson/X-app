package org.example.xchange.data.models;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FxRateListWrapper extends FxRateWrapper {

    @JsonProperty("FxRate")
    private List<FxRate> fxRates = new ArrayList<>();


    @Setter
    @Getter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class FxRate {

        @JsonProperty("Tp")
        private String type;

        @JsonProperty("Dt")
        private String date;

        @JsonProperty("CcyAmt")
        private List<CurrencyAmount> currencyAmountList;


        @Setter
        @Getter
        @JsonInclude(JsonInclude.Include.NON_NULL)
        public static class CurrencyAmount {

            @JsonProperty("Ccy")
            private String currency;

            @JsonProperty("Amt")
            private BigDecimal amount;

        }
    }

    @Setter
    @Getter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ApiError {

        @JsonProperty("Err")
        private Error error;

        @JsonProperty("Desc")
        private String errorDescription;



        @Setter
        @Getter
        @JsonInclude(JsonInclude.Include.NON_NULL)
        public static class Error {

            @JsonProperty("Prtry")
            private String errorCode;
        }
    }

}
