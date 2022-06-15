package com.customer.invoices.repository.domain.enumeration;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum InvoiceType {

    INVOICE, CREDIT_NOTE, DEBIT_NOTE

}
