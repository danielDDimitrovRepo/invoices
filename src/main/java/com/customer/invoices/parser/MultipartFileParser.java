package com.customer.invoices.parser;

import com.customer.invoices.repository.domain.Invoice;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface MultipartFileParser {

    List<Invoice> parseInvoices(MultipartFile file);

}
