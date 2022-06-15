package com.customer.invoices.service;

import com.customer.invoices.repository.InvoiceRepository;
import com.customer.invoices.repository.domain.Invoice;
import com.customer.invoices.service.exception.ParentIsMissingException;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;

import static java.lang.String.format;

@Service
@RequiredArgsConstructor
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceBucketRepository;

    /**
     * This method is responsible for the persistence layer interaction for all invoices.
     * It validates that invoices with 'parentId' have existing parents (either in input or in DB).
     *
     * @param invoices
     * @throws {@link ParentIsMissingException} - if no parent exists for a child invoice
     */
    @Override
    public void saveAll(List<Invoice> invoices) {

        for (Invoice invoice : invoices) { // Validating for missing parents before saving the batch

            if (invoice.getInvoiceParentId() != null) {
                Optional<Invoice> parent = invoices.stream()
                        .filter(inboundInvoice -> inboundInvoice.getInvoiceId().equals(invoice.getInvoiceParentId()))
                        .findFirst();

                if (parent.isEmpty()) {
                    invoiceBucketRepository.findAll().stream()
                            .filter(dbInvoice -> dbInvoice.getInvoiceId().equals(invoice.getInvoiceParentId()))
                            .findFirst()
                            .orElseThrow(() -> new ParentIsMissingException(
                                    format("Cannot store invoice: %s, with parentId: %s, but without a parent",
                                            invoice.getInvoiceId(),
                                            invoice.getInvoiceParentId())));
                }

            }

        }

        invoiceBucketRepository.saveAll(invoices);
    }

    @Override
    public List<Invoice> findAll() {
        return invoiceBucketRepository.findAll();
    }

    @Override
    public List<Invoice> findAll(Integer customerId) {
        return invoiceBucketRepository.findAll(customerId);
    }

}
