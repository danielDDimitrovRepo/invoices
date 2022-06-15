package com.customer.invoices.calculator;

import com.customer.invoices.calculator.exception.BaseCurrencyNotFoundException;
import com.customer.invoices.calculator.exception.CurrencyExchangeRateNotFoundException;
import com.customer.invoices.repository.domain.Invoice;
import com.customer.invoices.repository.domain.enumeration.InvoiceType;

import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;

@Component
public class InvoiceSumCalculator {

    /**
     * This method calculates total invoice amount for a given collection of invoices,
     * currency exchange rates configuration and output currency exchange rate.
     *
     * @param currencyToExchangeRate - a map of cyrrency code to exchange rate, e.g. base currency "EUR - 1",
     * currency pairs "EUR:USD - 0.987" etc.
     * @param outputCurrencyCode - currency for the conversion of the total amount
     * @return total invoice amount for parsed invoices, based on the exchange rate and the output currency
     * @throws {@link BaseCurrencyNotFoundException} - if no base currency is found (with rate 1)
     * @throws {@link IllegalArgumentException} - if base currency has an invalid code, expected is a single code, e.g. "EUR"
     * @throws {@link CurrencyExchangeRateNotFoundException} - if there's no exchange rate mapping for an invoice's currency
     */
    public BigDecimal calculate(List<Invoice> invoices,
            Map<String, BigDecimal> currencyToExchangeRate,
            String outputCurrencyCode) {

        String baseCurrencyCode = currencyToExchangeRate.entrySet()
                .stream()
                .filter(e -> e.getValue().equals(BigDecimal.ONE))
                .findFirst()
                .orElseThrow(BaseCurrencyNotFoundException::new)
                .getKey();

        Assert.isTrue(baseCurrencyCode.split(":").length == 1,
                "Base currency should be a pair of codes, but it was a single code: " + baseCurrencyCode);

        BigDecimal totalAmountToBaseCurrency = invoices.stream()
                .map(i -> new SumData(i.getType(), i.getCurrency(), i.getTotal()))
                .reduce(new SumData(baseCurrencyCode, BigDecimal.ZERO), (subtotal, currentAmount) -> {

                    BigDecimal currentAmountExchangeRate = getExchangeRate(currencyToExchangeRate, baseCurrencyCode, currentAmount.currencyCode);

                    BigDecimal currentAmountToBaseCurrency = currentAmount.total.multiply(currentAmountExchangeRate);

                    return switch (currentAmount.type) {
                        case INVOICE, DEBIT_NOTE -> new SumData(baseCurrencyCode, subtotal.total.add(currentAmountToBaseCurrency));
                        case CREDIT_NOTE -> new SumData(baseCurrencyCode, subtotal.total.subtract(currentAmountToBaseCurrency));
                    };

                }).total;

        BigDecimal outputCurrencyExchangeRate = getExchangeRate(currencyToExchangeRate, baseCurrencyCode, outputCurrencyCode);

        return totalAmountToBaseCurrency.multiply(outputCurrencyExchangeRate);
    }

    private BigDecimal getExchangeRate(Map<String, BigDecimal> currencySymbolToExchangeRate, String baseCurrencyCode, String currencyCode) {
        return currencySymbolToExchangeRate.entrySet().stream()
                .filter(e -> e.getKey().startsWith(baseCurrencyCode) && e.getKey().endsWith(currencyCode)) // valid for both "EUR:USD" & "EUR"
                .findFirst()
                .orElseThrow(CurrencyExchangeRateNotFoundException::new)
                .getValue();
    }

    @AllArgsConstructor
    @RequiredArgsConstructor
    private static class SumData {
        private InvoiceType type;
        private final String currencyCode;
        private final BigDecimal total;
    }

}
