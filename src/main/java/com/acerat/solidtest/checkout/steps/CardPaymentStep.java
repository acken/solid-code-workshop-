package com.acerat.solidtest.checkout.steps;

import com.acerat.solidtest.cardpayments.CardDetails;
import com.acerat.solidtest.cardpayments.CardPaymentService;
import com.acerat.solidtest.checkout.CheckoutStepVisitor;
import com.acerat.solidtest.checkout.state.CardPaymentFailures;
import com.acerat.solidtest.checkout.state.CardPaymentResult;
import com.acerat.solidtest.checkout.state.CheckoutState;
import com.acerat.solidtest.customers.Customer;
import com.acerat.solidtest.customers.CustomerRepository;
import com.acerat.solidtest.encryptedstores.Encryption;
import com.acerat.solidtest.encryptedstores.TrustStore;
import com.acerat.solidtest.shoppingcart.Order;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class CardPaymentStep implements CheckoutStepVisitor {
    private CustomerRepository customerRepository;
    private TrustStore trustStore;
    private CardPaymentService cardPaymentService;

    public CardPaymentStep(CustomerRepository customerRepository, TrustStore trustStore, CardPaymentService cardPaymentService) {
        this.customerRepository = customerRepository;
        this.trustStore = trustStore;
        this.cardPaymentService = cardPaymentService;
    }

    @Override
    public CheckoutState visit(CheckoutState checkoutState) {
        Order order = checkoutState.getOrder();

        // Get customer
        Customer customer = customerRepository.get(order.getCustomerId());

        // If the customer is set up to pay by card use the card payment service
        // Decrypt card details for our customer
        byte[] encryptedCardDetails = trustStore.getCardDetailsByCustomerId(customer.getCustomerId());
        List<CardDetails> cardDetailsList = Encryption.decryptFromSecret(encryptedCardDetails, customer.getCustomerSecret());

        // Pick the currently valid credit card
        Optional<CardDetails> currentCardDetails = Optional.empty();
        for (CardDetails cardDetails : cardDetailsList) {
            if (cardDetails.getExpiresAt().isAfter(LocalDate.now())) {
                currentCardDetails = Optional.of(cardDetails);
                break;
            }
        }
        // If there is no valid card update checkout state
        if (!currentCardDetails.isPresent()) {
            checkoutState.cardPaymentFailed(CardPaymentFailures.NO_VALID_CREDIT_CARDS);
            return checkoutState;
        }

        CardPaymentResult cardPaymentResult = cardPaymentService.chargeCreditCard(currentCardDetails.get());
        if (!cardPaymentResult.succeeded()) {
            checkoutState.cardPaymentFailed(CardPaymentFailures.COULD_NOT_COMPLETE_CARD_PAYMENT);
            return checkoutState;
        }
        checkoutState.cardPaymentCompletedUsing(currentCardDetails.get().getCardDetailsReference());
        return checkoutState;
    }
}
