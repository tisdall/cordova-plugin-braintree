# Braintree Cordova Plugin

This is a [Cordova](http://cordova.apache.org/) plugin for the [Braintree](https://www.braintreepayments.com/) mobile payment processing SDK.

This version of the plugin uses versions `4.7.2` (iOS) and `2.3.12` (Android) of the Braintree mobile SDK. Documentation for the Braintree SDK can be found [here](https://developers.braintreepayments.com/start/overview).

**This plugin is still in development.**

# Install

To add the plugin to your Cordova project, install the latest version of the plugin directly from git:

    cordova plugin add https://github.com/taracque/cordova-plugin-braintree
    
be sure, that xcode npm module is installed:

    npm ls | grep xcode

# Usage

The plugin is available via a global variable named `BraintreePlugin`. It exposes the following properties and functions.

All functions accept optional success and failure callbacks as their last two arguments, where the failure callback will receive an error string as an argument unless otherwise noted.

A TypeScript definition file for the JavaScript interface is available in the `typings` directory as well as on [DefinitelyTyped](https://github.com/borisyankov/DefinitelyTyped) via the `tsd` tool.

## Initialize Braintree Client ##

Used to initialize the Braintree client. The client must be initialized before other methods can be used.

Method Signature:

`initialize(token, successCallback, failureCallback)`

Parameters:

* `token` (string): The unique client token or static tokenization key to use.

Example Usage:

```
var token = "YOUR_TOKEN";

BraintreePlugin.initialize(token,
    function () { console.log("init OK!"); },
    function (error) { console.error(error); });
```

## Show Drop-In Payment UI ##

Used to show Braintree's drop-in UI for accepting payments.

Method Signature:

`presentDropInPaymentUI(options, successCallback, failureCallback)`

Parameters:

* `options` (object): An optional argument used to configure the payment UI; see type definition for parameters.

Example Usage:

```
var options = {
    amount: "49.99",
    primaryDescription: "Your Item"
};

BraintreePlugin.presentDropInPaymentUI(options, function (result) {

    if (result.userCancelled) {
        console.debug("User cancelled payment dialog.");
    }
    else {
        console.info("User completed payment dialog.");
        console.info("Payment Nonce: " + result.nonce);
        console.debug("Payment Result.", result);
    }
});
```

## Apple Pay (iOS only) ##

To allow ApplePay payment you need to initialize Apple Pay framework before usign the Drop/In Payment UI. Read Braintree docs to setup Merchant account: https://developers.braintreepayments.com/guides/apple-pay/configuration/ios/v4?_ga=1.6058933.767761401.1478959986#apple-pay-certificate-request-and-provisioning

Method Signature:
`setupApplePay(options)`

Paramteres:

* `options` (object): Merchant settings object, with the following keys:
    *   `merchantId` (string): The merchant id generated on Apple Developer portal.
    *   `currencyCode` (string): The currency for payment, 3 letter code (ISO 4217)
    *   `countryCode` (string): The country code of merchant's residence. (ISO 3166-2)

Example Usage:

```
BraintreePlugin.setupApplePay({ merchantId : 'com.braintree.merchant.sandbox.demo-app', countryCode : 'US', currencyCode : 'USD'});
```

ApplePay shown in Drop-In UI only if `BraintreePlugin.setupApplePay` called before `BraintreePlugin.presentDropInPaymentUI`
