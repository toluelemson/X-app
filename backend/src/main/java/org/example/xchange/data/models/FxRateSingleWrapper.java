package org.example.xchange.data.models;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FxRateSingleWrapper extends FxRateWrapper {

    @JsonProperty("FxRate")
    private FxRateListWrapper.FxRate fxRates;

    public FxRateSingleWrapper(FxRateListWrapper.FxRate fxRate) {
        this.fxRates = fxRate;
    }
}
