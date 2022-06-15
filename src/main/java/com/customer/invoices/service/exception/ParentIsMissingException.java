package com.customer.invoices.service.exception;

public class ParentIsMissingException extends RuntimeException {

    public ParentIsMissingException(String message) {
        super(message);
    }

}
