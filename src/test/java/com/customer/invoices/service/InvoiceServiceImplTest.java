package com.customer.invoices.service;

import com.customer.invoices.repository.InvoiceRepository;
import com.customer.invoices.repository.domain.Invoice;
import com.customer.invoices.service.exception.ParentIsMissingException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InvoiceServiceImplTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @InjectMocks
    private InvoiceServiceImpl invoiceServiceImpl;

    // TODO Unit tests for all public methods

    @Test
    void saveAll_validInvoices_expectedRepositoryInteractions() {
        List<Invoice> input = List.of(Invoice.builder().invoiceId(1).build());

        invoiceServiceImpl.saveAll(input);

        verify(invoiceRepository).saveAll(eq(input));
    }

    @Test
    void saveAll_validInvoicesWithParentsInInput_expectedRepositoryInteractions() {
        List<Invoice> input = List.of(
                Invoice.builder().invoiceId(2).build(),
                Invoice.builder().invoiceId(1).invoiceParentId(2).build());

        invoiceServiceImpl.saveAll(input);

        verify(invoiceRepository).saveAll(eq(input));
    }

    @Test
    void saveAll_validInvoicesWithParentsInDb_expectedRepositoryInteractions() {
        List<Invoice> input = List.of(Invoice.builder().invoiceId(1).invoiceParentId(2).build());

        when(invoiceRepository.findAll()).thenReturn(List.of(Invoice.builder().invoiceId(2).build()));

        invoiceServiceImpl.saveAll(input);

        verify(invoiceRepository).saveAll(eq(input));
    }

    @Test
    void saveAll_validInvoicesWithParentIdButNoParent_expectedExceptionIsThrown() {
        List<Invoice> input = List.of(Invoice.builder().invoiceId(1).invoiceParentId(2).build());

        assertThrows(ParentIsMissingException.class, () -> invoiceServiceImpl.saveAll(input));
    }

}
