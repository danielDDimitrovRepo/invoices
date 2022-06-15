package com.customer.invoices.repository.domain;

import com.customer.invoices.repository.domain.enumeration.InvoiceType;

import java.math.BigDecimal;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@Builder
@EqualsAndHashCode(of = "invoiceId")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Invoice {

    private Integer invoiceId;
    private Integer invoiceParentId;
    private InvoiceType type;
    private Integer vatNumber;
    private String customerName;
    private String currency;
    private BigDecimal total;

}
