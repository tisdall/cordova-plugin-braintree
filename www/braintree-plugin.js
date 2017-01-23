"use strict";

var exec = require("cordova/exec");

/**
 * The Cordova plugin ID for this plugin.
 */
var PLUGIN_ID = "BraintreePlugin";

/**
 * The plugin which will be exported and exposed in the global scope.
 */
var BraintreePlugin = {};

/**
 * Used to initialize the Braintree client.
 *
 * The client must be initialized before other methods can be used.
 *
 * @param {string} token - The client token or tokenization key to use with the Braintree client.
 * @param [function] successCallback - The success callback for this asynchronous function.
 * @param [function] failureCallback - The failure callback for this asynchronous function; receives an error string.
 */
BraintreePlugin.initialize = function initialize(token, successCallback, failureCallback) {

    if (!token || typeof(token) !== "string") {
        failureCallback("A non-null, non-empty string must be provided for the token parameter.");
        return;
    }

    exec(successCallback, failureCallback, PLUGIN_ID, "initialize", [token]);
};

/**
 * Shows Braintree's Apple Pay UI.
 *
 * @param {object} options - The options used to control the Apple Pay UI.
 * @param successCallback The success callback for this asynchronous function; receives a result object.
 * @param failureCallback The failure callback for this asynchronous function; receives an error string.
 */
BraintreePlugin.setupApplePay = function setupApplePay(options, successCallback, failureCallback) {
    if (!options) {
        options = {};
    }

    if (typeof(options.merchantId) !== "string") {
        failureCallback("Apple Pay Merchant ID must be provided");
    }
    if (typeof(options.currency) !== "string") {
        failureCallback("Apple Pay currency must be provided");
    }
    if (typeof(options.country) !== "string") {
        failureCallback("Apple Pay country must be provided");
    }

    var pluginOptions = [
        options.merchantId,
        options.currencyCode,
        options.countryCode
    ];

	exec(successCallback, failureCallback, PLUGIN_ID, "setupApplePay", pluginOptions);
};

/**
 * Shows Braintree's drop-in payment UI.
 *
 * @param {object} options - The options used to control the drop-in payment UI.
 * @param [function] successCallback - The success callback for this asynchronous function; receives a result object.
 * @param [function] failureCallback - The failure callback for this asynchronous function; receives an error string.
 */
BraintreePlugin.presentDropInPaymentUI = function showDropInUI(options, successCallback, failureCallback) {

    if (!options) {
        options = {};
    }

    if (typeof(options.amount) === "undefined") {
        options.amount = "0.00";
    }

    if (!isNaN(options.amount * 1)) {
        options.amount = (options.amount * 1).toFixed(2)
    }

    var pluginOptions = [
        options.amount,
        options.primaryDescription
    ];

    exec(successCallback, failureCallback, PLUGIN_ID, "presentDropInPaymentUI", pluginOptions);
};

/**
 * Shows Braintree's ThreeDSecure verification.
 *
 * @param {object} options - The options used to control the drop-in payment UI.
 * @param [function] successCallback - The success callback for this asynchronous function; receives a result object.
 * @param [function] failureCallback - The failure callback for this asynchronous function; receives an error string.
 */
BraintreePlugin.presentThreeDSecureVerification = function presentThreeDSecureVerification(options, successCallback, failureCallback) {

    if (!options) {
        failureCallback("Must provide options");
    }

    if (typeof(options.amount) === "undefined") {
        options.amount = "0.00";
    }
    if (!isNaN(options.amount * 1)) {
	    options.amount = (options.amount * 1).toFixed(2)
	}

    if (typeof(options.creditCardNonce) === "undefined") {
        failureCallback("Credit card nonce must be provided");
    }

    var pluginOptions = [
        options.amount,
        options.creditCardNonce
	];

	exec(successCallback, failureCallback, PLUGIN_ID, "presentThreeDSecureVerification", pluginOptions);
};

module.exports = BraintreePlugin;
