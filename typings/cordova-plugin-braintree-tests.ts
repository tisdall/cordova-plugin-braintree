/// <reference path="cordova-plugin-braintree.d.ts" />

BraintreePlugin.initialize("a");
BraintreePlugin.initialize("a", () => {});
BraintreePlugin.initialize("a", () => {}, () => {});

const paymentUIOptions: BraintreePlugin.PaymentUIOptions = {
    amount: "49.99",
    primaryDescription: "Your Item"
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

const threeDVerificationOptions: BraintreePlugin.ThreeDVerificationOptions = {
    amount: "49.99",
    creditCardNonce: "123-456-789"
};
BraintreePlugin.presentThreeDSecureVerification(threeDVerificationOptions);
BraintreePlugin.presentThreeDSecureVerification(threeDVerificationOptions, (result: BraintreePlugin.PaymentUIResult) => {});
BraintreePlugin.presentThreeDSecureVerification(threeDVerificationOptions, (result: BraintreePlugin.PaymentUIResult) => {}, () => {});