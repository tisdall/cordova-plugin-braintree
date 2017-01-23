package net.justincredible;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.braintreepayments.api.dropin.DropInActivity;
import com.braintreepayments.api.dropin.DropInRequest;
import com.braintreepayments.api.dropin.DropInResult;
import com.braintreepayments.api.models.CardNonce;
import com.braintreepayments.api.models.PayPalAccountNonce;
import com.braintreepayments.api.models.PaymentMethodNonce;
import com.braintreepayments.api.models.ThreeDSecureInfo;
import com.braintreepayments.api.models.VenmoAccountNonce;

import java.util.HashMap;
import java.util.Map;

public final class BraintreePlugin extends CordovaPlugin {

    private static final String TAG = "BraintreeCordovaPlugin";

    private static final int DROP_IN_REQUEST = 100;
    private static final int PAYMENT_BUTTON_REQUEST = 200;
    private static final int CUSTOM_REQUEST = 300;
    private static final int PAYPAL_REQUEST = 400;
    private static final int THREEDSECURE_REQUEST = 500;

    private DropInRequest dropInRequest = null;
    private CallbackContext dropInUICallbackContext = null;

    private String token;
    private String amount;

    @Override
    public synchronized boolean execute(String action, final JSONArray args, final CallbackContext callbackContext) throws JSONException {

        if (action == null) {
            return false;
        }

        if (action.equals("initialize")) {

            try {
                this.initialize(args, callbackContext);
            }
            catch (Exception exception) {
                Map<String, Object> resultMap = new HashMap<String, Object>();
                resultMap.put("message", "BraintreePlugin uncaught exception: " + exception.getMessage());
                resultMap.put("type", "plugin");
                callbackContext.error(new JSONObject(resultMap));
            }

            return true;
        }
        else if (action.equals("presentDropInPaymentUI")) {

            try {
                this.presentDropInPaymentUI(args, callbackContext);
            }
            catch (Exception exception) {
                Map<String, Object> resultMap = new HashMap<String, Object>();
                resultMap.put("message", "BraintreePlugin uncaught exception: " + exception.getMessage());
                resultMap.put("type", "plugin");
                callbackContext.error(new JSONObject(resultMap));
            }

            return true;
        }
        else if (action.equals("presentThreeDSecureVerification")) {

            try {
                this.presentThreeDSecureVerification(args, callbackContext);
            }
            catch (Exception exception) {
                Map<String, Object> resultMap = new HashMap<String, Object>();
                resultMap.put("message", "BraintreePlugin uncaught exception: " + exception.getMessage());
                resultMap.put("type", "plugin");
                callbackContext.error(new JSONObject(resultMap));
            }

            return true;
        }
        else {
            // The given action was not handled above.
            return false;
        }
    }

    private synchronized void initialize(final JSONArray args, final CallbackContext callbackContext) throws JSONException {

        // Ensure we have the correct number of arguments.
        if (args.length() != 1) {
            callbackContext.error("A token is required.");
            return;
        }

        // Obtain the arguments.
        token = args.getString(0);

        if (token == null || token.equals("")) {
            Map<String, Object> resultMap = new HashMap<String, Object>();
            resultMap.put("message", "A token is required");
            resultMap.put("type", "plugin");
            callbackContext.error(new JSONObject(resultMap));
            return;
        }

        dropInRequest = new DropInRequest().clientToken(token);

        if (dropInRequest == null) {
            Map<String, Object> resultMap = new HashMap<String, Object>();
            resultMap.put("message", "The Braintree client failed to initialize.");
            resultMap.put("type", "plugin");
            callbackContext.error(new JSONObject(resultMap));
            return;
        }

        callbackContext.success();
    }

    private synchronized void setupApplePay(final JSONArray args, final CallbackContext callbackContext) throws JSONException {
        // Apple Pay available on iOS only
        callbackContext.success();
    }

    private synchronized void presentDropInPaymentUI(final JSONArray args, final CallbackContext callbackContext) throws JSONException {

        // Ensure the client has been initialized.
        if (dropInRequest == null) {
            Map<String, Object> resultMap = new HashMap<String, Object>();
            resultMap.put("message", "The Braintree client must first be initialized via BraintreePlugin.initialize(token)");
            resultMap.put("type", "plugin");
            callbackContext.error(new JSONObject(resultMap));
            return;
        }

        // Ensure we have the correct number of arguments.
        if (args.length() < 1) {
            Map<String, Object> resultMap = new HashMap<String, Object>();
            resultMap.put("message", "Amount is required");
            resultMap.put("type", "plugin");
            callbackContext.error(new JSONObject(resultMap));
            return;
        }

        // Obtain the arguments.
        amount = args.getString(0);

        if (amount == null) {
            Map<String, Object> resultMap = new HashMap<String, Object>();
            resultMap.put("message", "Amount is required");
            resultMap.put("type", "plugin");
            callbackContext.error(new JSONObject(resultMap));
            return;
        }

        dropInRequest.amount(amount);

        this.cordova.setActivityResultCallback(this);
        this.cordova.startActivityForResult(this, dropInRequest.getIntent(this.cordova.getActivity()), DROP_IN_REQUEST);

        dropInUICallbackContext = callbackContext;
    }

    private synchronized void presentThreeDSecureVerification(final JSONArray args, final CallbackContext callbackContext) throws JSONException {

        // Ensure the client has been initialized.
        if (dropInRequest == null) {
            Map<String, Object> resultMap = new HashMap<String, Object>();
            resultMap.put("message", "The Braintree client must first be initialized via BraintreePlugin.initialize(token)");
            resultMap.put("type", "plugin");
            callbackContext.error(new JSONObject(resultMap));
            return;
        }

        // Ensure we have the correct number of arguments.
        if (args.length() < 2) {
            Map<String, Object> resultMap = new HashMap<String, Object>();
            resultMap.put("message", "Credit card nonce and amount required.");
            resultMap.put("type", "plugin");
            callbackContext.error(new JSONObject(resultMap));
            return;
        }

        // Obtain the arguments.
        amount = args.getString(0);
        if (amount == null) {
            Map<String, Object> resultMap = new HashMap<String, Object>();
            resultMap.put("message", "Amount is required.");
            resultMap.put("type", "plugin");
            callbackContext.error(new JSONObject(resultMap));
            return;
        }

        String creditCardNonce = args.getString(1);
        if (creditCardNonce == null) {
            Map<String, Object> resultMap = new HashMap<String, Object>();
            resultMap.put("message", "Credit card nonce is required.");
            resultMap.put("type", "plugin");
            callbackContext.error(new JSONObject(resultMap));
            return;
        }

        Intent intent = new Intent("net.justincredible.ThreeDSecureVerification");
        intent.putExtra("amount", amount);
        intent.putExtra("token", token);
        intent.putExtra("paymentNonce", creditCardNonce);

        //this.cordova.setActivityResultCallback(this);
        this.cordova.startActivityForResult(this, intent, THREEDSECURE_REQUEST);

        dropInUICallbackContext = callbackContext;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (dropInUICallbackContext == null) {
            return;
        }

        if (requestCode == DROP_IN_REQUEST) {
            this.handleDropInPaymentUiResult(resultCode, intent);
        }
        else if (requestCode == PAYMENT_BUTTON_REQUEST) {
            //TODO implement
            Map<String, Object> resultMap = new HashMap<String, Object>();
            resultMap.put("message", "Activity result handler for PAYMENT_BUTTON_REQUEST not implemented.");
            resultMap.put("type", "plugin");
            dropInUICallbackContext.error(new JSONObject(resultMap));
        }
        else if (requestCode == CUSTOM_REQUEST) {
            //TODO implement
            Map<String, Object> resultMap = new HashMap<String, Object>();
            resultMap.put("message", "Activity result handler for CUSTOM_REQUEST not implemented.");
            resultMap.put("type", "plugin");
            dropInUICallbackContext.error(new JSONObject(resultMap));
        }
        else if (requestCode == PAYPAL_REQUEST) {
            //TODO implement
            Map<String, Object> resultMap = new HashMap<String, Object>();
            resultMap.put("message", "Activity result handler for PAYPAL_REQUEST not implemented.");
            resultMap.put("type", "plugin");
            dropInUICallbackContext.error(new JSONObject(resultMap));
        }
        else if (requestCode == THREEDSECURE_REQUEST) {
            this.handleThreeDSecureVerificationResult(resultCode, intent.getExtras());
        }
    }

    /**
     * Helper used to handle the result of the ThreeDSecureVerification activity.
     *
     * @param resultCode Indicates the result of the UI.
     * @param intentExtras Contains payment information from the ThreeDSecureVerification activity.
     */
    private void handleThreeDSecureVerificationResult(int resultCode, Bundle intentExtras) {
        if (resultCode == Activity.RESULT_OK) {
            Map<String, Object> resultMap = new HashMap<String, Object>();
            resultMap.put("nonce", intentExtras.getString("threeDSecureNonce"));
            Map<String, Object> verificationDetails = new HashMap<String, Object>();
            verificationDetails.put("liabilityShifted", intentExtras.getBoolean("liabilityShifted"));
            verificationDetails.put("liabilityShiftPossible", intentExtras.getBoolean("liabilityShiftPossible"));
            resultMap.put("verificationDetails", verificationDetails);

            dropInUICallbackContext.success(new JSONObject(resultMap));
            dropInUICallbackContext = null;
        } else if (resultCode == Activity.RESULT_CANCELED && intentExtras.getBoolean("userCancelled")) {
            Map<String, Object> resultMap = new HashMap<String, Object>();
            resultMap.put("userCancelled", true);

            dropInUICallbackContext.success(new JSONObject(resultMap));
            dropInUICallbackContext = null;
        } else {
            String error = intentExtras.getString("error");

            Log.e(TAG, "ThreeDSecureVerification error: " + error);

            Map<String, Object> resultMap = new HashMap<String, Object>();
            resultMap.put("message", error);
            resultMap.put("type", "braintree");
            dropInUICallbackContext.error(new JSONObject(resultMap));
            dropInUICallbackContext = null;
        }
    }

    /**
     * Helper used to handle the result of the drop-in payment UI.
     *
     * @param resultCode Indicates the result of the UI.
     * @param intent Contains information about payment.
     */
    private void handleDropInPaymentUiResult(int resultCode, Intent intent) {
        if (dropInUICallbackContext == null) {
            return;
        }

        if (resultCode == Activity.RESULT_OK) {
            DropInResult result = intent.getParcelableExtra(DropInResult.EXTRA_DROP_IN_RESULT);
            PaymentMethodNonce paymentMethodNonce = result.getPaymentMethodNonce();

            Map<String, Object> resultMap = this.getPaymentUINonceResult(paymentMethodNonce);

            dropInUICallbackContext.success(new JSONObject(resultMap));
            dropInUICallbackContext = null;
        } else if (resultCode == Activity.RESULT_CANCELED) {
            Map<String, Object> resultMap = new HashMap<String, Object>();
            resultMap.put("userCancelled", true);

            dropInUICallbackContext.success(new JSONObject(resultMap));
            dropInUICallbackContext = null;
        } else {
            String error = ((Exception) intent.getSerializableExtra(DropInActivity.EXTRA_ERROR)).getMessage();

            Log.e(TAG, "DropInPayment error: " + error);

            Map<String, Object> resultMap = new HashMap<String, Object>();
            resultMap.put("message", error);
            resultMap.put("type", "braintree");

            dropInUICallbackContext.error(new JSONObject(resultMap));
            dropInUICallbackContext = null;
        }
    }

    /**
     * Helper used to return a dictionary of values from the given payment method nonce.
     * Handles several different types of nonces (eg for cards, PayPal, etc).
     *
     * @param paymentMethodNonce The nonce used to build a dictionary of data from.
     * @return The dictionary of data populated via the given payment method nonce.
     */
    private Map<String, Object> getPaymentUINonceResult(PaymentMethodNonce paymentMethodNonce) {

        Map<String, Object> resultMap = new HashMap<String, Object>();

        resultMap.put("nonce", paymentMethodNonce.getNonce());
        resultMap.put("type", paymentMethodNonce.getTypeLabel());
        resultMap.put("localizedDescription", paymentMethodNonce.getDescription());

        // Card
        if (paymentMethodNonce instanceof CardNonce) {
            CardNonce cardNonce = (CardNonce)paymentMethodNonce;

            Map<String, Object> innerMap = new HashMap<String, Object>();
            innerMap.put("lastTwo", cardNonce.getLastTwo());
            innerMap.put("network", cardNonce.getCardType());

            resultMap.put("card", innerMap);
        }

        // PayPal
        if (paymentMethodNonce instanceof PayPalAccountNonce) {
            PayPalAccountNonce payPalAccountNonce = (PayPalAccountNonce)paymentMethodNonce;

            Map<String, Object> innerMap = new HashMap<String, Object>();
            resultMap.put("email", payPalAccountNonce.getEmail());
            resultMap.put("firstName", payPalAccountNonce.getFirstName());
            resultMap.put("lastName", payPalAccountNonce.getLastName());
            resultMap.put("phone", payPalAccountNonce.getPhone());
            //resultMap.put("billingAddress", payPalAccountNonce.getBillingAddress()); //TODO
            //resultMap.put("shippingAddress", payPalAccountNonce.getShippingAddress()); //TODO
            resultMap.put("clientMetadataId", payPalAccountNonce.getClientMetadataId());
            resultMap.put("payerId", payPalAccountNonce.getPayerId());

            resultMap.put("payPalAccount", innerMap);
        }

        // 3D Secure
        if (paymentMethodNonce instanceof CardNonce) {
            CardNonce cardNonce = (CardNonce) paymentMethodNonce;
            ThreeDSecureInfo threeDSecureInfo = cardNonce.getThreeDSecureInfo();

            if (threeDSecureInfo != null) {
                Map<String, Object> innerMap = new HashMap<String, Object>();
                innerMap.put("liabilityShifted", threeDSecureInfo.isLiabilityShifted());
                innerMap.put("liabilityShiftPossible", threeDSecureInfo.isLiabilityShiftPossible());

                resultMap.put("threeDSecureCard", innerMap);
            }
        }

        // Venmo
        if (paymentMethodNonce instanceof VenmoAccountNonce) {
            VenmoAccountNonce venmoAccountNonce = (VenmoAccountNonce) paymentMethodNonce;

            Map<String, Object> innerMap = new HashMap<String, Object>();
            innerMap.put("username", venmoAccountNonce.getUsername());

            resultMap.put("venmoAccount", innerMap);
        }

        return resultMap;
    }
}
