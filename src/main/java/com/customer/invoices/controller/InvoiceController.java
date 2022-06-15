package com.customer.invoices.controller;

import com.customer.invoices.calculator.InvoiceSumCalculator;
import com.customer.invoices.controller.model.InvoiceRequest;
import com.customer.invoices.parser.MultipartFileParser;
import com.customer.invoices.repository.domain.Invoice;
import com.customer.invoices.service.InvoiceService;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

import javax.validation.Valid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static org.springframework.util.CollectionUtils.isEmpty;

@Slf4j
@RestController
@RequestMapping("invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceServiceImpl;
    private final InvoiceSumCalculator invoiceSumCalculator;
    private final MultipartFileParser csvMultipartFileParser;

    /**
     * This endpoint is responsible for persisting new invoices and returning an aggregate of all invoices.
     * You can filter the result per customer.
     *
     * @param invoiceFile - mandatory
     * @param invoiceRequest - not optional (except for 'vatNumber' property)
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> processInvoices(
            @RequestPart MultipartFile invoiceFile,
            @RequestPart @Valid InvoiceRequest invoiceRequest) {

        invoiceServiceImpl.saveAll(csvMultipartFileParser.parseInvoices(invoiceFile));

        List<Invoice> allInvoices;

        if (invoiceRequest.getVatNumber() == null) {
            allInvoices = invoiceServiceImpl.findAll();
            if (isEmpty(allInvoices)) {
                return ResponseEntity.ok("No invoices found in the database");
            }
        } else {
            allInvoices = invoiceServiceImpl.findAll(invoiceRequest.getVatNumber());
            if (isEmpty(allInvoices)) {
                return ResponseEntity.ok("No invoices found for VAT number: " + invoiceRequest.getVatNumber());
            }
        }

        BigDecimal invoicesTotal = invoiceSumCalculator.calculate(
                allInvoices,
                invoiceRequest.getCurrencySymbolToExchangeRate(),
                invoiceRequest.getOutputCurrencyCode());

        return ResponseEntity.ok(invoicesTotal.toString());
    }

}
