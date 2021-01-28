package com.tonnfccard.smartcard.cryptoUtils;

import android.util.Log;

import java.security.KeyStore;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import static com.tonnfccard.api.utils.ResponsesConstants.ERROR_MSG_CURRENT_SERIAL_NUMBER_IS_NOT_SET_IN_ANDROID_KEYCHAIN;
import static com.tonnfccard.api.utils.ResponsesConstants.ERROR_MSG_KEY_FOR_HMAC_DOES_NOT_EXIST_IN_ANDROID_KEYCHAIN;
import static com.tonnfccard.smartcard.TonWalletAppletConstants.DEFAULT_SERIAL_NUMBER;
import static com.tonnfccard.smartcard.TonWalletAppletConstants.EMPTY_SERIAL_NUMBER;

public class HmacHelper {
    public static final String HMAC_KEY_ALIAS = "hmac_key_alias_";

    private String currentSerialNumber = DEFAULT_SERIAL_NUMBER;

    private static HmacHelper hmacHelper;

    public static HmacHelper getInstance() {
        if (hmacHelper == null) hmacHelper = new HmacHelper();
        return hmacHelper;
    }

    private HmacHelper(){}

    public String getCurrentSerialNumber() {
      return currentSerialNumber;
    }

    public void setCurrentSerialNumber(String currentSerialNumber) {
      this.currentSerialNumber = currentSerialNumber;
    }

    public byte[] computeMac(byte[] key, byte[] data) throws Exception {
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKey = new SecretKeySpec(key, "HmacSHA256");
        sha256_HMAC.init(secretKey);
        return sha256_HMAC.doFinal(data);
    }

    public byte[] computeMac(byte[] data) throws Exception {
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);
        if (currentSerialNumber.equals(EMPTY_SERIAL_NUMBER)) throw new Exception(ERROR_MSG_CURRENT_SERIAL_NUMBER_IS_NOT_SET_IN_ANDROID_KEYCHAIN);
        String keyAlias = HmacHelper.HMAC_KEY_ALIAS + currentSerialNumber;
        if (!keyStore.containsAlias(keyAlias)) throw new Exception(ERROR_MSG_KEY_FOR_HMAC_DOES_NOT_EXIST_IN_ANDROID_KEYCHAIN);
        KeyStore.Entry entry = keyStore.getEntry(keyAlias, null);
        if (!(entry instanceof KeyStore.SecretKeyEntry)) {
            Log.w("TAG", "Not an instance of a SecretKeyEntry");
            return null;
        }
        sha256_HMAC.init(((KeyStore.SecretKeyEntry) entry).getSecretKey());
        return sha256_HMAC.doFinal(data);
    }
}

