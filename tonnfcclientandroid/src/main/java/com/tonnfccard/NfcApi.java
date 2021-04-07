package com.tonnfccard;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.nfc.NfcAdapter;
import android.provider.Settings;
import android.util.Log;

import com.tonnfccard.callback.NfcCallback;


import static com.tonnfccard.TonWalletApi.EXCEPTION_HELPER;
import static com.tonnfccard.TonWalletApi.JSON_HELPER;
import static com.tonnfccard.TonWalletConstants.*;
import static com.tonnfccard.helpers.ResponsesConstants.*;

/**
 * Class containing functions to work with NFC hardware on your Android device and check its state.
 */

public final class NfcApi {
    private static final String TAG = "NfcApi";

    private Context activity;

    public NfcApi(Context activity) {
        this.activity = activity;
    }

    public void setActivity(Context activity) {
        this.activity = activity;
    }

    /**
     * openNfcSettings
     */

     /**
     * Open NFC tab on Android device where one can turn off/on NFC function.
     */

    public void openNfcSettings(final NfcCallback callback) {
        new Thread(new Runnable() {
            public void run() {
                try {
                    String json = openNfcSettingsAndGetJson();
                    resolveJson(json, callback);
                    Log.d(TAG, "openNfcSettings response : " + json);
                } catch (Exception e) {
                    EXCEPTION_HELPER.handleException(e, callback, TAG);
                }
            }
        }).start();
    }

    public String openNfcSettingsAndGetJson() throws Exception  {
        try {
            if (activity == null) throw new Exception(ERROR_MSG_NO_CONTEXT);
            Intent intent = new Intent(Settings.ACTION_NFC_SETTINGS);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            activity.startActivity(intent);
            return JSON_HELPER.createResponseJson(DONE_MSG);
        }
        catch (Exception e) {
            throw new Exception(EXCEPTION_HELPER.makeFinalErrMsg(e), e);
        }
    }

    /**
     * checkIfNfcEnabled
     */

    /**
     * Check if NFC is turned on for your Android device.
     */

    public void checkIfNfcEnabled(final NfcCallback callback) {
        new Thread(new Runnable() {
            public void run() {
                try {
                    String json = checkIfNfcEnabledAndGetJson();
                    resolveJson(json, callback);
                    Log.d(TAG, "checkIfNfcEnabled response : " + json);
                } catch (Exception e) {
                    EXCEPTION_HELPER.handleException(e, callback, TAG);
                }
            }
        }).start();
    }

    public String checkIfNfcEnabledAndGetJson() throws Exception {
        try {
            if (activity == null) throw new Exception(ERROR_MSG_NO_CONTEXT);
            NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(activity);
            boolean res = nfcAdapter.isEnabled();
            return JSON_HELPER.createResponseJson(res ? TRUE_MSG : FALSE_MSG);
        }
        catch (Exception e) {
            throw new Exception(EXCEPTION_HELPER.makeFinalErrMsg(e), e);
        }
    }

    /**
     * checkIfNfcSupported
     */

    /**
     * Check if Android device has working NFC hardware.
     */

    public void checkIfNfcSupported(final NfcCallback callback) {
        new Thread(new Runnable() {
            public void run() {
                try {
                    String json = checkIfNfcSupportedAndGetJson();
                    resolveJson(json, callback);
                    Log.d(TAG, "checkIsNfcSupported response : " + json);
                } catch (Exception e) {
                    EXCEPTION_HELPER.handleException(e, callback, TAG);
                }
            }
        }).start();
    }

    public String checkIfNfcSupportedAndGetJson() throws Exception {
        try {
            if (activity == null) throw new Exception(ERROR_MSG_NO_CONTEXT);
            boolean res = activity.getPackageManager().hasSystemFeature(PackageManager.FEATURE_NFC);
            return JSON_HELPER.createResponseJson(res ? TRUE_MSG : FALSE_MSG);
        }
        catch (Exception e) {
            throw new Exception(EXCEPTION_HELPER.makeFinalErrMsg(e), e);
        }
    }

    void resolveJson(String json, NfcCallback callback){
        callback.getResolve().resolve(json);
        Log.d(TAG, "json = " + json);
    }
}
