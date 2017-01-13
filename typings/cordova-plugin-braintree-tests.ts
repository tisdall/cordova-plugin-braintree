/// <reference path="cordova-plugin-braintree.d.ts" />

BraintreePlugin.initialize("a");
BraintreePlugin.initialize("a", () => {});
BraintreePlugin.initialize("a", () => {}, () => {});

const paymentUIOptions: BraintreePlugin.PaymentUIOptions = {
    amount: "49.99",
    enableThreeDSecureVerification: true
};
BraintreePlugin.presentDropInPaymentUI();
BraintreePlugin.presentDropInPaymentUI(paymentUIOptions);
BraintreePlugin.presentDropInPaymentUI(paymentUIOptions, (result: BraintreePlugin.PaymentUIResult) => {});
BraintreePlugin.presentDropInPaymentUI(paymentUIOptions, (result: BraintreePlugin.PaymentUIResult) => {}, () => {});

const applePayOptions: BraintreePlugin.ApplePayOptions = {
    merchantId: "com.braintree.merchant.demoapp",
    currencyCode: "USD",
    countryCode: "US"
};
BraintreePlugin.setupApplePay(applePayOptions);
BraintreePlugin.setupApplePay(applePayOptions, () => {});
BraintreePlugin.setupApplePay(applePayOptions, () => {}, () => {});
