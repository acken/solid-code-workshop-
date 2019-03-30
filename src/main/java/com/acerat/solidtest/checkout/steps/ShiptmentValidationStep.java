package com.acerat.solidtest.checkout.steps;

import com.acerat.solidtest.checkout.CheckoutStepVisitor;
import com.acerat.solidtest.checkout.state.CheckoutState;
import com.acerat.solidtest.checkout.state.ShipmentFailures;
import com.acerat.solidtest.customers.Address;
import com.acerat.solidtest.customers.Customer;
import com.acerat.solidtest.customers.CustomerRepository;
import com.acerat.solidtest.logistics.ShipmentTracker;
import com.acerat.solidtest.shoppingcart.Order;

public class ShiptmentValidationStep implements CheckoutStepVisitor {
    private CustomerRepository customerRepository;
    private ShipmentTracker shipmentTracker;

    public ShiptmentValidationStep(CustomerRepository customerRepository, ShipmentTracker shipmentTracker) {
        this.customerRepository = customerRepository;
        this.shipmentTracker = shipmentTracker;
    }

    @Override
    public CheckoutState visit(CheckoutState checkoutState) {
        Order order = checkoutState.getOrder();

        // Get customer
        Customer customer = customerRepository.get(order.getCustomerId());

        // Validate shipping information
        if (customer.getShippingAddress() == null) {
            checkoutState.shipmentFailed(ShipmentFailures.MISSING_CUSTOMER_ADDRESS);
            return checkoutState;
        }
        Address shipmentAddress = customer.getShippingAddress();
        if (
                shipmentAddress.getStreet() == null || shipmentAddress.getStreet().isEmpty() ||
                shipmentAddress.getZipCode() == null || shipmentAddress.getZipCode().isEmpty() ||
                shipmentAddress.getCity() == null || shipmentAddress.getCity().isEmpty()
        ) {
            checkoutState.shipmentFailed(ShipmentFailures.INVALID_CUSTOMER_ADDRESS);
            return checkoutState;
        }
        if (!shipmentTracker.canShipToDestination(shipmentAddress)) {
            checkoutState.shipmentFailed(ShipmentFailures.CANNOT_SHIP_TO_DESTINATION);
            return checkoutState;
        }
        checkoutState.shipmentVerified();
        return checkoutState;
    }
}
