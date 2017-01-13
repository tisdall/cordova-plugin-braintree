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
                    Log.d("BRAINTREE", "Braintree Fragment initialization exception: " + exception.getMessage());

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
        Log.d("BRAINTREE", "onCancel");

        firstTime = false;

        Intent intent = new Intent();
        intent.putExtra("userCancelled", true);
        setResult(Activity.RESULT_CANCELED, intent);

        finish();
    }

    @Override
    public void onError(Exception error) {
        Log.d("BRAINTREE", "onError");
        Log.d("BRAINTREE", error.getMessage());

        firstTime = false;

        Intent intent = new Intent();
        intent.putExtra("error", error.getMessage());
        setResult(Activity.RESULT_CANCELED, intent);

        finish();
    }

    @Override
    public void onPaymentMethodNonceCreated(PaymentMethodNonce paymentMethodNonce) {
        Log.d("BRAINTREE", "onPaymentMethodNonceCreated");

        CardNonce cardNonce = (CardNonce) paymentMethodNonce;
        String threeDSecureNonce = paymentMethodNonce.getNonce();
        boolean liabilityShifted = cardNonce.getThreeDSecureInfo().isLiabilityShifted();
        boolean liabilityShiftPossible = cardNonce.getThreeDSecureInfo().isLiabilityShiftPossible();

        firstTime = false;

        Intent intent = new Intent();
        intent.putExtra("threeDSecureNonce", threeDSecureNonce);
        intent.putExtra("liabilityShifted", liabilityShifted);
        intent.putExtra("liabilityShiftPossible", liabilityShiftPossible);
        setResult(Activity.RESULT_OK, intent);

        finish();
    }
}
