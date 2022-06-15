package com.customer.invoices.controller.model;

import java.math.BigDecimal;
import java.util.Map;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Positive;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class InvoiceRequest {

    @NotEmpty
    private Map<@NotBlank String, @Positive(message = "Rate cannot be negative or zero") BigDecimal> currencySymbolToExchangeRate;

    @NotBlank(message = "Output currency is missing")
    private String outputCurrencyCode;

    @Min(value = 1, message = "VAT number must greater than zero")
    @Max(value = Integer.MAX_VALUE, message = "VAT number must not be greater than integer max value")
    private Integer vatNumber;

}
