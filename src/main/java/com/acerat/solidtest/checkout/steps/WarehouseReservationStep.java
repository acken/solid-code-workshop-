package com.acerat.solidtest.checkout.steps;

import com.acerat.solidtest.checkout.CheckoutStepVisitor;
import com.acerat.solidtest.checkout.state.CheckoutState;
import com.acerat.solidtest.checkout.state.WarehouseReservationFailures;
import com.acerat.solidtest.logistics.Warehouse;
import com.acerat.solidtest.product.Product;
import com.acerat.solidtest.product.ProductStore;
import com.acerat.solidtest.shoppingcart.Order;
import com.acerat.solidtest.shoppingcart.OrderLine;

public class WarehouseReservationStep implements CheckoutStepVisitor {
    private Warehouse warehouse;
    private ProductStore productStore;

    public WarehouseReservationStep(Warehouse warehouse, ProductStore productStore) {
        this.warehouse = warehouse;
        this.productStore = productStore;
    }

    @Override
    public CheckoutState visit(CheckoutState checkoutState) {
        Order order = checkoutState.getOrder();
        // Make sure we reserve items in stock in case they have been released
        for (OrderLine orderLine : order.getOrderLines()) {
            Product product = productStore.getById(orderLine.getProductId());
            if (product == null) {
                checkoutState.warehouseReservationFailed(WarehouseReservationFailures.PRODUCT_NOT_FOUND);
                return checkoutState;
            }
            if (!product.isStoredInWarehouse())
                continue;
            if (!warehouse.isReservedInStock(orderLine.getUniqueOrderLineReference(), orderLine.getQty())) {
                if (!warehouse.tryReserveItems(orderLine.getUniqueOrderLineReference(), orderLine.getQty())) {
                    checkoutState.warehouseReservationFailed(WarehouseReservationFailures.COULD_NOT_RESERVE_ITEMS_IN_STOCK);
                    return checkoutState;
                }
            }
        }
        checkoutState.warehouseReservationSucceeded();
        return checkoutState;
    }
}
