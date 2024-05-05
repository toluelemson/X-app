package org.example.xchange.data.models;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@ToString
public class CurrencyList implements Serializable {

        private List<Currency> currencies = new ArrayList<>();


    @Setter
    @Getter
    @ToString
    public static class Currency {
        private String isoCode;
    }

}

