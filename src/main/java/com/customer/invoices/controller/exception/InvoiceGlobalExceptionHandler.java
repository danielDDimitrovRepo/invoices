package com.customer.invoices.controller.exception;

import com.customer.invoices.calculator.exception.BaseCurrencyNotFoundException;
import com.customer.invoices.calculator.exception.CurrencyExchangeRateNotFoundException;
import com.customer.invoices.parser.exception.ParsingException;
import com.customer.invoices.repository.exception.InvoiceExistsException;
import com.customer.invoices.service.exception.ParentIsMissingException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.stream.Collectors;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ControllerAdvice
public class InvoiceGlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @Setter(onMethod_ = {@Value("#{'${spring.servlet.multipart.max-file-size}'}")})
    private String supportedFileSize;

    @Setter(onMethod_ = {@Value("#{'${spring.servlet.multipart.max-request-size}'}")})
    private String supportedRequestSize;

    @ExceptionHandler({Exception.class})
    public ResponseEntity<String> handleInternalServerError(Exception e) {
        log.error(e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unexpected error occurred during processing");
    }

    /**
     * This method creates a read-friendly response body for validation constraint violations
     */
    @Override
    public ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException e,
            HttpHeaders headers,
            HttpStatus status,
            WebRequest request) {

        String errorsAsText = e.getBindingResult().getAllErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining(", "));

        return ResponseEntity.status(status).body(errorsAsText);
    }

    @Override
    public ResponseEntity<Object> handleMissingServletRequestPart(
            MissingServletRequestPartException ex,
            HttpHeaders headers,
            HttpStatus status,
            WebRequest request) {

        return ResponseEntity.status(status).body(ex.getMessage());
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<String> handleMaxSizeException(MaxUploadSizeExceededException e) {
        log.error(e.getMessage());
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(
                String.format("Request size is too large. Supported file size: %s, supported total request size: %s",
                        supportedFileSize,
                        supportedRequestSize
                ));
    }

    @ExceptionHandler({
            ParsingException.class,
            ParentIsMissingException.class,
            BaseCurrencyNotFoundException.class,
            CurrencyExchangeRateNotFoundException.class,
            IllegalArgumentException.class})
    public ResponseEntity<String> handleBadRequest(RuntimeException e) {
        log.error(e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }

    @ExceptionHandler(InvoiceExistsException.class)
    public ResponseEntity<String> handleDuplicates(RuntimeException e) {
        log.info(e.getMessage());
        return ResponseEntity.status(HttpStatus.OK).body(e.getMessage());
    }

}
