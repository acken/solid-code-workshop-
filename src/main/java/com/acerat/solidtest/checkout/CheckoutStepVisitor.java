package com.acerat.solidtest.checkout;

import com.acerat.solidtest.checkout.state.CheckoutState;

public interface CheckoutStepVisitor {
    CheckoutState visit(CheckoutState checkoutState);
}
