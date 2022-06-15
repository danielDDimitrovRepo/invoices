package com.customer.invoices.controller;

import com.customer.invoices.controller.model.InvoiceRequest;
import com.customer.invoices.datasource.BucketDataSource;
import com.customer.invoices.repository.domain.Invoice;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockPart;
import org.springframework.test.web.servlet.MockMvc;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.customer.invoices.util.CommonTestUtils.setPrivateField;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class InvoiceControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private BucketDataSource<Invoice> inMemoryBucketDataSource;

    @Value("classpath:test-invoices/valid-exchange-rate-data.csv")
    private Resource validInvoiceData;

    private MockMultipartFile invoiceFile;

    @BeforeEach
    void setUp() throws Exception {
        resetDataSource();
        objectMapper = objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        invoiceFile = new MockMultipartFile("invoiceFile", Files.readString(validInvoiceData.getFile().toPath()).getBytes());
    }

    // TODO more test cases

    @Test
    void processInvoices_validExchangeRatesAndInvoicesAndGbpCurrencyOutput_returnsCorrectTotal() throws Exception {
        mockMvc.perform(multipart("/invoices").file(invoiceFile).part(buildInvoiceRequestPart(buildInvoiceRequest())))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("3409.010600")));
    }

    @Test
    void processInvoices_validExchangeRatesAndInvoicesAndUsdCurrencyOutputAndVatNumber_returnsCorrectTotal() throws Exception {
        InvoiceRequest invoiceRequest = buildInvoiceRequest();
        setPrivateField("vatNumber", 123456789, invoiceRequest);
        setPrivateField("outputCurrencyCode", "USD", invoiceRequest);

        mockMvc.perform(multipart("/invoices").file(invoiceFile).part(buildInvoiceRequestPart(invoiceRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("1913.4969")));
    }

    @Test
    void processInvoices_missingExchangeRateAndValidInvoicesAndOutputCurrency_returnsBadRequest() throws Exception {
        InvoiceRequest invoiceRequest = buildInvoiceRequest();
        invoiceRequest.getCurrencySymbolToExchangeRate().remove("EUR:USD");

        mockMvc.perform(multipart("/invoices").file(invoiceFile).part(buildInvoiceRequestPart(invoiceRequest)))
                .andExpect(status().isBadRequest());
    }

    private MockPart buildInvoiceRequestPart(InvoiceRequest request) throws JsonProcessingException {
        MockPart invoiceRequestPart = new MockPart("invoiceRequest", "invoiceRequest", objectMapper.writeValueAsString(request).getBytes());
        invoiceRequestPart.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        return invoiceRequestPart;
    }

    private InvoiceRequest buildInvoiceRequest() {
        Map<String, BigDecimal> currencySymbolToExchangeRate = new HashMap<>();
        currencySymbolToExchangeRate.put("EUR", BigDecimal.ONE);
        currencySymbolToExchangeRate.put("EUR:USD", new BigDecimal("0.987"));
        currencySymbolToExchangeRate.put("EUR:GBP", new BigDecimal("0.878"));

        return InvoiceRequest.builder()
                .currencySymbolToExchangeRate(currencySymbolToExchangeRate)
                .outputCurrencyCode("GBP")
                .build();
    }

    private void resetDataSource() {
        String fieldName = "invoiceDb";
        try {
            Field field = inMemoryBucketDataSource.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);

            Set<?> invoiceDb = (Set<?>) field.get(inMemoryBucketDataSource);
            invoiceDb.clear();
        } catch (IllegalAccessException | NoSuchFieldException e) {
            fail("Cannot get private field:" + fieldName +
                            ", of class: " + inMemoryBucketDataSource.getClass(),
                    e);
        }
    }

}
