package com.customer.invoices.datasource;

import com.customer.invoices.repository.domain.Invoice;

import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class InMemoryBucketDataSource implements BucketDataSource<Invoice> {

    private final Set<Invoice> invoiceDb = new HashSet<>();

    public boolean add(Invoice invoice) {
        return invoiceDb.add(invoice);
    }

    public Set<Invoice> getBucket() {
        return invoiceDb;
    }

}
