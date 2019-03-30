package com.acerat.solidtest.checkout.steps;

import com.acerat.solidtest.checkout.CheckoutStepVisitor;
import com.acerat.solidtest.checkout.state.CheckoutState;
import com.acerat.solidtest.checkout.state.WarehouseSendFailures;
import com.acerat.solidtest.customers.Customer;
import com.acerat.solidtest.customers.CustomerRepository;
import com.acerat.solidtest.logistics.Warehouse;
import com.acerat.solidtest.product.Product;
import com.acerat.solidtest.product.ProductStore;
import com.acerat.solidtest.shoppingcart.Order;
import com.acerat.solidtest.shoppingcart.OrderLine;

public class ShipmentActivationStep implements CheckoutStepVisitor {
    private CustomerRepository customerRepository;
    private Warehouse warehouse;
    private ProductStore productStore;

    public ShipmentActivationStep(CustomerRepository customerRepository, Warehouse warehouse, ProductStore productStore) {
        this.customerRepository = customerRepository;
        this.warehouse = warehouse;
        this.productStore = productStore;
    }

    @Override
    public CheckoutState visit(CheckoutState checkoutState) {
        Order order = checkoutState.getOrder();

        // Get customer
        Customer customer = customerRepository.get(order.getCustomerId());
        // Send reserved items
        for (OrderLine orderLine : order.getOrderLines()) {
            Product product = productStore.getById(orderLine.getProductId());
            if (product == null) {
                checkoutState.shipmentActivationFailed(WarehouseSendFailures.PRODUCT_NOT_FOUND);
            }
            if (!product.isStoredInWarehouse())
                continue;
            if (!warehouse.activateShipment(orderLine.getUniqueOrderLineReference())) {
                checkoutState.shipmentActivationFailed(WarehouseSendFailures.COULD_NOT_ACTIVATE_SHIPMENT);
                return checkoutState;
            }
        }
        checkoutState.shipmentActivated();
        return checkoutState;
    }
}
