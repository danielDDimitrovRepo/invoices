package com.customer.invoices.service;

import com.customer.invoices.repository.domain.Invoice;

import java.util.List;

public interface InvoiceService {

    void saveAll(List<Invoice> invoices);

    List<Invoice> findAll();
    List<Invoice> findAll(Integer customerId);

}
