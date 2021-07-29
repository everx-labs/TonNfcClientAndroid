package com.tonnfccard.helpers;

import java.security.KeyStore;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import static com.tonnfccard.TonWalletConstants.DEFAULT_SERIAL_NUMBER;
import static com.tonnfccard.TonWalletConstants.EMPTY_SERIAL_NUMBER;
import static com.tonnfccard.TonWalletConstants.SHA_HASH_SIZE;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_CURRENT_SERIAL_NUMBER_IS_NOT_SET_IN_ANDROID_KEYSTORE;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_ERR_CURRENT_SERIAL_NUMBER_IS_NULL;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_ERR_DATA_BYTES_FOR_HMAC_SHA256_IS_NULL;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_ERR_IS_NOT_SEC_KEY_ENTRY;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_ERR_KEY_BYTES_FOR_HMAC_SHA256_IS_NULL;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_ERR_KEY_BYTES_FOR_HMAC_SHA256_IS_TOO_SHORT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_KEY_FOR_HMAC_DOES_NOT_EXIST_IN_ANDROID_KEYSTORE;

import androidx.annotation.RestrictTo;

/**
 *  HMAC-SHA256 signature to sign APDU commands sent to the card
 */

@RestrictTo(RestrictTo.Scope.LIBRARY)
public class HmacHelper {
    public static final String HMAC_SHA256_ALG = "HmacSHA256";
    public static final String HMAC_KEY_ALIAS = "hmac_key_alias_";
    public static final String ANDROID_KEYSTORE = "AndroidKeyStore";

    // serial number of currently active security card
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

    //Calculate symmetric key for HMAC SHA256 signature for the first time. Then it goes into Android keystore
    public byte[] computeMac(byte[] key, byte[] data) throws Exception {
        if (key == null) throw new IllegalArgumentException(ERROR_MSG_ERR_KEY_BYTES_FOR_HMAC_SHA256_IS_NULL);
        if (key.length < SHA_HASH_SIZE) throw new IllegalArgumentException(ERROR_MSG_ERR_KEY_BYTES_FOR_HMAC_SHA256_IS_TOO_SHORT);
        if (data == null) throw new IllegalArgumentException(ERROR_MSG_ERR_DATA_BYTES_FOR_HMAC_SHA256_IS_NULL);
        Mac sha256_HMAC = Mac.getInstance(HMAC_SHA256_ALG);
        SecretKeySpec secretKey = new SecretKeySpec(key, HMAC_SHA256_ALG);
        sha256_HMAC.init(secretKey);
        return sha256_HMAC.doFinal(data);
    }

    // Calculate hmac of data using key living in Android keystore
    // Key for currentSerialNumber must exits in keystore
    public byte[] computeMac(byte[] data) throws Exception {
        if (data == null) throw new IllegalArgumentException(ERROR_MSG_ERR_DATA_BYTES_FOR_HMAC_SHA256_IS_NULL);
        if (currentSerialNumber == null) throw new IllegalArgumentException(ERROR_MSG_ERR_CURRENT_SERIAL_NUMBER_IS_NULL);
        if (currentSerialNumber.equals(EMPTY_SERIAL_NUMBER)) throw new IllegalArgumentException(ERROR_MSG_CURRENT_SERIAL_NUMBER_IS_NOT_SET_IN_ANDROID_KEYSTORE);
        Mac sha256_HMAC = Mac.getInstance(HMAC_SHA256_ALG);
        KeyStore keyStore = KeyStore.getInstance(ANDROID_KEYSTORE);
        keyStore.load(null);
        String keyAlias = HmacHelper.HMAC_KEY_ALIAS + currentSerialNumber;
        if (!keyStore.containsAlias(keyAlias)) throw new Exception(ERROR_MSG_KEY_FOR_HMAC_DOES_NOT_EXIST_IN_ANDROID_KEYSTORE);
        KeyStore.Entry entry = keyStore.getEntry(keyAlias, null);
        if (!(entry instanceof KeyStore.SecretKeyEntry)) throw new Exception(ERROR_MSG_ERR_IS_NOT_SEC_KEY_ENTRY);
        sha256_HMAC.init(((KeyStore.SecretKeyEntry) entry).getSecretKey());
        return sha256_HMAC.doFinal(data);
    }
}

