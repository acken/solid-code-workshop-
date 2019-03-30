package com.acerat.solidtest.checkout;

import com.acerat.solidtest.checkout.state.CheckoutState;
import com.acerat.solidtest.checkout.steps.*;
import com.acerat.solidtest.customers.Customer;
import com.acerat.solidtest.customers.CustomerPaymentMethod;
import com.acerat.solidtest.customers.CustomerRepository;

import java.util.ArrayList;
import java.util.List;

public class CheckoutProcessBuilder {
    private CustomerRepository customerRepository;
    private ShiptmentValidationStep shiptmentValidationStep;
    private CardPaymentStep cardPaymentStep;
    private InvoicePaymentStep invoicePaymentStep;
    private ShipmentActivationStep shipmentActivationStep;
    private WarehouseReservationStep warehouseReservationStep;

    public CheckoutProcessBuilder(CustomerRepository customerRepository, ShiptmentValidationStep shiptmentValidationStep, CardPaymentStep cardPaymentStep, InvoicePaymentStep invoicePaymentStep, ShipmentActivationStep shipmentActivationStep, WarehouseReservationStep warehouseReservationStep) {
        this.customerRepository = customerRepository;
        this.shiptmentValidationStep = shiptmentValidationStep;
        this.cardPaymentStep = cardPaymentStep;
        this.invoicePaymentStep = invoicePaymentStep;
        this.shipmentActivationStep = shipmentActivationStep;
        this.warehouseReservationStep = warehouseReservationStep;
    }

    public List<CheckoutStepVisitor> build(CheckoutState state) {
        Customer customer = customerRepository.get(state.getOrder().getCustomerId());
        List<CheckoutStepVisitor> steps = new ArrayList<>();
        steps.add(shiptmentValidationStep);
        steps.add(warehouseReservationStep);
        if (!state.isPaid()) {
            if (customer.getConfiguration().getPaymentMenthod() == CustomerPaymentMethod.CARD)
                steps.add(cardPaymentStep);
            else if (customer.getConfiguration().getPaymentMenthod() == CustomerPaymentMethod.INVOICE)
                steps.add(invoicePaymentStep);
        }
        steps.add(shipmentActivationStep);
        return steps;
    }
}
