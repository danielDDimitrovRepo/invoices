package com.customer.invoices.parser;

import com.customer.invoices.parser.exception.ParsingException;
import com.customer.invoices.repository.domain.Invoice;
import com.customer.invoices.repository.domain.enumeration.InvoiceType;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.IntStream;

import static com.customer.invoices.repository.domain.enumeration.InvoiceType.CREDIT_NOTE;
import static com.customer.invoices.repository.domain.enumeration.InvoiceType.DEBIT_NOTE;
import static com.customer.invoices.repository.domain.enumeration.InvoiceType.INVOICE;
import static org.springframework.util.ObjectUtils.isEmpty;

@Component
public class CsvMultipartFileParser implements MultipartFileParser {

    /**
     * A simple CSV parser
     *
     * @return - collection of parsed invoices
     * @throws {@link ParsingException} - a wrapper exception for all errors that could occur during the parsing
     */
    @Override
    public List<Invoice> parseInvoices(MultipartFile file) {

        byte[] bytes;
        String[] rows;

        try {
            bytes = file.getBytes();
            String csvFileAsString = new String(bytes);
            rows = csvFileAsString.split("\n");
        } catch (Exception e) {
            throw new ParsingException("Cannot parse CSV", e);
        }


        return IntStream.range(1, rows.length) // skipping CSV header at index 0
                .mapToObj(i -> {

                    try {

                        String[] col = rows[i].split(",");

                        return Invoice.builder()
                                .invoiceId(Integer.parseInt(col[2]))
                                .invoiceParentId(isEmpty(col[4]) ? null : Integer.parseInt(col[4]))
                                .type(getInvoiceTypeById(Integer.parseInt(col[3])))
                                .vatNumber(Integer.parseInt(col[1]))
                                .customerName(col[0])
                                .currency(col[5])
                                .total(new BigDecimal(col[6]))
                                .build();
                    } catch (NumberFormatException e) {
                        throw new ParsingException("Unexpected error during CSV parsing", e);
                    }

                }).toList();

    }

    private InvoiceType getInvoiceTypeById(int typeId) {
        return switch (typeId) {
            case 1 -> INVOICE;
            case 2 -> CREDIT_NOTE;
            case 3 -> DEBIT_NOTE;
            default -> throw new ParsingException("Invoice type not found for typeId: " + typeId);
        };
    }

}
