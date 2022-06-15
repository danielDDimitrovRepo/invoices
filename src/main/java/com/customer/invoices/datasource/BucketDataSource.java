package com.customer.invoices.datasource;

import com.customer.invoices.repository.domain.Invoice;

import java.util.Set;

public interface BucketDataSource<T> {

    boolean add(Invoice invoice);

    Set<T> getBucket();

}
