package com.customer.invoices.repository;

import com.customer.invoices.repository.domain.Invoice;

import java.util.List;

public interface InvoiceRepository {

    void saveAll(List<Invoice> invoices);

    List<Invoice> findAll();

    List<Invoice> findAll(Integer vatNumber);

}
