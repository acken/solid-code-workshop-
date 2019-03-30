package com.acerat.solidtest.configuration;

import com.acerat.solidtest.cardpayments.CardPaymentService;
import com.acerat.solidtest.checkout.CheckoutApi;
import com.acerat.solidtest.checkout.CheckoutHandler;
import com.acerat.solidtest.checkout.CheckoutProcessBuilder;
import com.acerat.solidtest.checkout.CheckoutStateStore;
import com.acerat.solidtest.checkout.steps.*;
import com.acerat.solidtest.customers.CustomerRepository;
import com.acerat.solidtest.encryptedstores.TrustStore;
import com.acerat.solidtest.invoicing.InvoiceHandler;
import com.acerat.solidtest.logistics.ShipmentTracker;
import com.acerat.solidtest.logistics.Warehouse;
import com.acerat.solidtest.product.ProductStore;

public class ApplicationBoostrapper {
    private CheckoutApi checkoutApi;

    public ApplicationBoostrapper() {
        CustomerRepository customerRepository = new CustomerRepository(ApplicationConfiguration.getConnectionString());
        CheckoutProcessBuilder checkoutProcessBuilder = new CheckoutProcessBuilder(
                customerRepository,
                new ShiptmentValidationStep(
                        customerRepository,
                        new ShipmentTracker(ApplicationConfiguration.getConnectionString())
                ),
                new CardPaymentStep(
                        customerRepository,
                        new TrustStore(ApplicationConfiguration.getTrustStoreCredentials()),
                        new CardPaymentService(ApplicationConfiguration.getCardPaymentConfiguration())
                ),
                new InvoicePaymentStep(
                        customerRepository,
                        new InvoiceHandler(ApplicationConfiguration.getConnectionString())
                ),
                new ShipmentActivationStep(
                        customerRepository,
                        new Warehouse(ApplicationConfiguration.getConnectionString()),
                        new ProductStore(ApplicationConfiguration.getConnectionString())
                ),
                new WarehouseReservationStep(
                        new Warehouse(ApplicationConfiguration.getConnectionString()),
                        new ProductStore(ApplicationConfiguration.getConnectionString())
                )
        );
        CheckoutStateStore checkoutStateStore = new CheckoutStateStore(ApplicationConfiguration.getConnectionString());
        this.checkoutApi = new CheckoutApi(
                checkoutStateStore,
                new CheckoutHandler(),
                checkoutProcessBuilder
        );
    }

    public CheckoutApi checkoutApi() {
        return checkoutApi;
    }
}
