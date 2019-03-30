package com.acerat.solidtest.checkout;

import com.acerat.solidtest.checkout.state.CheckoutState;

import java.util.UUID;

public class CheckoutApi {
    private CheckoutStateStore checkoutStateStore;
    private CheckoutHandler checkoutHandler;
    private CheckoutProcessBuilder checkoutProcessBuilder;

    public CheckoutApi(CheckoutStateStore checkoutStateStore, CheckoutHandler checkoutHandler, CheckoutProcessBuilder checkoutProcessBuilder) {
        this.checkoutStateStore = checkoutStateStore;
        this.checkoutHandler = checkoutHandler;
        this.checkoutProcessBuilder = checkoutProcessBuilder;
    }

    public CheckoutState initateCheckout(UUID checkoutId) {
        CheckoutState state = checkoutStateStore.get(checkoutId);
        return checkoutHandler.checkout(state, checkoutProcessBuilder.build(state));
    }
}
