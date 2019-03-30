package com.acerat.solidtest.checkout;

import com.acerat.solidtest.checkout.state.CheckoutState;

import java.util.List;

public class CheckoutHandler {
    public CheckoutState checkout(CheckoutState checkoutState, List<CheckoutStepVisitor> steps) {
        for (CheckoutStepVisitor step : steps) {
            checkoutState = step.visit(checkoutState);
            if (checkoutState.hasFailed())
                break;
        }
        return checkoutState;
    }
}
