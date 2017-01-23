package net.justincredible;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.braintreepayments.api.BraintreeFragment;
import com.braintreepayments.api.exceptions.InvalidArgumentException;
import com.braintreepayments.api.interfaces.BraintreeCancelListener;
import com.braintreepayments.api.interfaces.BraintreeErrorListener;
import com.braintreepayments.api.interfaces.PaymentMethodNonceCreatedListener;
import com.braintreepayments.api.models.CardNonce;
import com.braintreepayments.api.models.PaymentMethodNonce;

public class ThreeDSecureVerification extends Activity implements PaymentMethodNonceCreatedListener,
        BraintreeCancelListener, BraintreeErrorListener {

    private static final String TAG = "3DSecureVerification";
    private boolean firstTime = true;

    @Override
    public void onStart() {
        super.onStart();
        if (firstTime) {
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                String amount = extras.getString("amount");
                String token = extras.getString("token");
                String paymentNonce = extras.getString("paymentNonce");

                try {
                    BraintreeFragment mBraintreeFragment = BraintreeFragment.newInstance(this, token);
                    com.braintreepayments.api.ThreeDSecure.performVerification(mBraintreeFragment, paymentNonce, amount);
                } catch (InvalidArgumentException exception) {
                    Log.e(TAG, "Braintree Fragment initialization exception: " + exception.getMessage());

                    Intent intent = new Intent();
                    intent.putExtra("error", exception.getMessage());
                    setResult(Activity.RESULT_CANCELED, intent);

                    finish();
                }
            }
        }
    }

    @Override
    public void onCancel(int requestCode) {
        Log.w(TAG, "User cancelled");

        firstTime = false;

        Intent intent = new Intent();
        intent.putExtra("userCancelled", true);
        setResult(Activity.RESULT_CANCELED, intent);

        finish();
    }

    @Override
    public void onError(Exception error) {
        Log.e(TAG, "Error: "+ error.getMessage());

        firstTime = false;

        Intent intent = new Intent();
        intent.putExtra("error", error.getMessage());
        setResult(Activity.RESULT_CANCELED, intent);

        finish();
    }

    @Override
    public void onPaymentMethodNonceCreated(PaymentMethodNonce paymentMethodNonce) {
        CardNonce cardNonce = (CardNonce) paymentMethodNonce;

        firstTime = false;

        Intent intent = new Intent();
        intent.putExtra("threeDSecureNonce", paymentMethodNonce.getNonce());
        intent.putExtra("liabilityShifted", cardNonce.getThreeDSecureInfo().isLiabilityShifted());
        intent.putExtra("liabilityShiftPossible", cardNonce.getThreeDSecureInfo().isLiabilityShiftPossible());
        setResult(Activity.RESULT_OK, intent);

        finish();
    }
}
