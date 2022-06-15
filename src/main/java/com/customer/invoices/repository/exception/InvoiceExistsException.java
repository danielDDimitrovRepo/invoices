package com.customer.invoices.repository.exception;

public class InvoiceExistsException extends RuntimeException {

    public InvoiceExistsException(String message) {
        super(message);
    }

}
