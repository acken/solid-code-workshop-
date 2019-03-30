package com.acerat.solidtest.checkout.steps;

import com.acerat.solidtest.checkout.CheckoutStepVisitor;
import com.acerat.solidtest.checkout.state.CheckoutState;
import com.acerat.solidtest.checkout.state.InvoiceFailures;
import com.acerat.solidtest.customers.Address;
import com.acerat.solidtest.customers.Customer;
import com.acerat.solidtest.customers.CustomerRepository;
import com.acerat.solidtest.invoicing.Invoice;
import com.acerat.solidtest.invoicing.InvoiceHandler;
import com.acerat.solidtest.shoppingcart.Order;

public class InvoicePaymentStep implements CheckoutStepVisitor {
    private CustomerRepository customerRepository;
    private InvoiceHandler invoiceHandler;

    public InvoicePaymentStep(CustomerRepository customerRepository, InvoiceHandler invoiceHandler) {
        this.customerRepository = customerRepository;
        this.invoiceHandler = invoiceHandler;
    }

    @Override
    public CheckoutState visit(CheckoutState checkoutState) {
        Order order = checkoutState.getOrder();

        // Get customer
        Customer customer = customerRepository.get(order.getCustomerId());

        // Send invoice to customer
        Address invoiceAddress = customer.getInvoiceAddress();
        if (invoiceAddress == null) {
            checkoutState.failedToInvoiceCustomer(InvoiceFailures.MISSING_INVOICE_ADDRESS);
            return checkoutState;
        }
        if (
                invoiceAddress.getStreet() == null || invoiceAddress.getStreet().isEmpty() ||
                invoiceAddress.getZipCode() == null || invoiceAddress.getZipCode().isEmpty() ||
                invoiceAddress.getCity() == null || invoiceAddress.getCity().isEmpty()
        ) {
            checkoutState.failedToInvoiceCustomer(InvoiceFailures.INVALID_CUSTOMER_ADDRESS);
            return checkoutState;
        }
        Invoice invoice = invoiceHandler.produceInvoice(order, customer);
        checkoutState.invoiceSentSuccessfully(invoice.getInvoiceId());
        return checkoutState;
    }
}
