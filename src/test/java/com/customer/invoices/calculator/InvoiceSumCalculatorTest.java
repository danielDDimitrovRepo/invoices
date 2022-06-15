package com.customer.invoices.calculator;

import com.customer.invoices.calculator.exception.BaseCurrencyNotFoundException;
import com.customer.invoices.calculator.exception.CurrencyExchangeRateNotFoundException;
import com.customer.invoices.repository.domain.Invoice;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.customer.invoices.repository.domain.enumeration.InvoiceType.CREDIT_NOTE;
import static com.customer.invoices.repository.domain.enumeration.InvoiceType.DEBIT_NOTE;
import static com.customer.invoices.repository.domain.enumeration.InvoiceType.INVOICE;
import static com.customer.invoices.util.CommonTestUtils.setPrivateField;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InvoiceSumCalculatorTest {

    private InvoiceSumCalculator invoiceSumCalculator = new InvoiceSumCalculator();

    private List<Invoice> invoices;
    private Map<String, BigDecimal> currencySymbolToExchangeRate = new HashMap<>();
    private String outputCurrency = "GBP";

    @BeforeEach
    void setUp() {

        invoices = List.of(
                Invoice.builder().currency("USD").total(new BigDecimal(400)).type(INVOICE).build(),
                Invoice.builder().currency("EUR").total(new BigDecimal(900)).type(INVOICE).build(),
                Invoice.builder().currency("GBP").total(new BigDecimal(1300)).type(INVOICE).build(),
                Invoice.builder().currency("EUR").total(new BigDecimal(100)).type(CREDIT_NOTE).build(),
                Invoice.builder().currency("GBP").total(new BigDecimal(50)).type(DEBIT_NOTE).build(),
                Invoice.builder().currency("USD").total(new BigDecimal(200)).type(CREDIT_NOTE).build(),
                Invoice.builder().currency("EUR").total(new BigDecimal(100)).type(DEBIT_NOTE).build(),
                Invoice.builder().currency("EUR").total(new BigDecimal(1600)).type(INVOICE).build()
        );

        currencySymbolToExchangeRate.put("EUR", BigDecimal.ONE);
        currencySymbolToExchangeRate.put("EUR:USD", new BigDecimal("0.987"));
        currencySymbolToExchangeRate.put("EUR:GBP", new BigDecimal("0.878"));

    }

    @Test
    public void calculate_validInvoicesAndRatesAndOutputCurrency_returnsValidTotal() {
        BigDecimal total = invoiceSumCalculator.calculate(invoices, currencySymbolToExchangeRate, outputCurrency);

        assertThat(total).isEqualTo(new BigDecimal("3409.010600"));
    }

    @Test
    public void calculate_validInvoicesAndOutputCurrencyButMissingBaseCurrency_expectedExceptionIsThrown() {
        currencySymbolToExchangeRate.remove("EUR");

        assertThrows(BaseCurrencyNotFoundException.class, () -> invoiceSumCalculator.calculate(invoices, currencySymbolToExchangeRate, outputCurrency));
    }

    @Test
    public void calculate_validInvoicesAndOutputCurrencyButInvalidBaseCurrency_expectedExceptionIsThrown() {
        currencySymbolToExchangeRate.remove("EUR");
        currencySymbolToExchangeRate.put("EUR:INVALID_PART", BigDecimal.ONE);

        assertThrows(IllegalArgumentException.class, () -> invoiceSumCalculator.calculate(invoices, currencySymbolToExchangeRate, outputCurrency));
    }

    @Test
    public void calculate_invalidInvoiceCurrencyAndValidOutputCurrencyAndValidBaseCurrency_expectedExceptionIsThrown() {
        setPrivateField("currency", "INVALID_CURRENCY", invoices.get(0));

        assertThrows(CurrencyExchangeRateNotFoundException.class, () -> invoiceSumCalculator.calculate(invoices, currencySymbolToExchangeRate, outputCurrency));
    }

    @Test
    public void calculate_validInvoicesAndValidBaseCurrencyButInvalidOutputCurrency_expectedExceptionIsThrown() {
        outputCurrency = "INVALID_CURRENCY";
        assertThrows(CurrencyExchangeRateNotFoundException.class, () -> invoiceSumCalculator.calculate(invoices, currencySymbolToExchangeRate, outputCurrency));
    }

}
