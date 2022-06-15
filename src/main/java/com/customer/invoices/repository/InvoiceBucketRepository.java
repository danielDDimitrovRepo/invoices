package com.customer.invoices.repository;

import com.customer.invoices.datasource.BucketDataSource;
import com.customer.invoices.repository.domain.Invoice;
import com.customer.invoices.repository.exception.InvoiceExistsException;

import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;

import static org.springframework.util.CollectionUtils.isEmpty;

@Repository
@AllArgsConstructor
public class InvoiceBucketRepository implements InvoiceRepository {

    private final BucketDataSource<Invoice> inMemoryBucketDataSource;

    /**
     * This method is the persistence layer for all invoices.
     * It stores non-duplicate invoice data.
     *
     * @param invoices
     * @throws {@link InvoiceExistsException} - error message will contain all invoice ids for duplicates
     */
    @Override
    public void saveAll(List<Invoice> invoices) {

        List<Invoice> duplicates = invoices.stream()
                .filter(invoice -> !inMemoryBucketDataSource.add(invoice))
                .toList();

        if (!isEmpty(duplicates)) {
            String duplicateIds = duplicates.stream()
                    .map(Invoice::getInvoiceId)
                    .map(String::valueOf)
                    .collect(Collectors.joining(", "));

            throw new InvoiceExistsException("Skipping duplicate invoices with IDs: " + duplicateIds);
        }
    }

    @Override
    public List<Invoice> findAll() {
        return new ArrayList<>(inMemoryBucketDataSource.getBucket());
    }

    @Override
    public List<Invoice> findAll(Integer vatNumber) {
        return inMemoryBucketDataSource.getBucket().stream().filter(i -> i.getVatNumber().equals(vatNumber)).toList();
    }

}
