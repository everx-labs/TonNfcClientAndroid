package com.tonnfccard.helpers;

import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.security.keystore.KeyProtection;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.tonnfccard.utils.ByteArrayUtil;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import static com.tonnfccard.helpers.HmacHelper.ANDROID_KEYSTORE;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_ERR_IS_NOT_SEC_KEY_ENTRY;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_KEY_FOR_HMAC_DOES_NOT_EXIST_IN_ANDROID_KEYSTORE;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class HmacHelperTest {
    public static final HmacHelper HMAC_HELPER = HmacHelper.getInstance();


    @Test
    public void testNotExistingSerialNumber()   {
        try {
            HMAC_HELPER.setCurrentSerialNumber("1111");
            byte[] mac = HMAC_HELPER.computeMac(new byte[1]);
            fail();
        }
        catch (Exception e) {
            e.printStackTrace();
            assertEquals(e.getMessage(), ERROR_MSG_KEY_FOR_HMAC_DOES_NOT_EXIST_IN_ANDROID_KEYSTORE);
        }
    }


    @Test
    public void testComputeMac()   {
        try {
            byte[] key = new byte[32];
            final SecretKey hmacSha256Key = new SecretKeySpec(key, 0, key.length, KeyProperties.KEY_ALGORITHM_HMAC_SHA256);
            final KeyStore keyStore = KeyStore.getInstance(ANDROID_KEYSTORE);
            String keyAlias = HmacHelper.HMAC_KEY_ALIAS + "777888999";
            keyStore.load(null);
            if (keyStore.containsAlias(keyAlias)) keyStore.deleteEntry(keyAlias);
            keyStore.setEntry(keyAlias,
                    new KeyStore.SecretKeyEntry(hmacSha256Key),
                    new KeyProtection.Builder(KeyProperties.PURPOSE_SIGN)
                            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                            .build());
            HMAC_HELPER.setCurrentSerialNumber("777888999");
            byte[] mac = HMAC_HELPER.computeMac(ByteArrayUtil.getInstance().bytes("00aa"));
            assertEquals("89c3c76721b45a2314a3f750a6fc31ebfa17bca96979ca95a7f4849ac814501f", ByteArrayUtil.getInstance().hex(mac).toLowerCase());
            System.out.println(ByteArrayUtil.getInstance().hex(mac));
        }
        catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testIncorrectKeyEntryType()   {
        try {

            final KeyStore keyStore = KeyStore.getInstance(ANDROID_KEYSTORE);
            keyStore.load(null);
            String keyAlias = HmacHelper.HMAC_KEY_ALIAS + "1212121414";

            KeyPairGenerator kpGenerator = KeyPairGenerator.getInstance("RSA", ANDROID_KEYSTORE);
            AlgorithmParameterSpec spec = null;
            spec = new KeyGenParameterSpec.Builder(keyAlias,
                    KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)
                    .setKeySize(1024)
                    .build();
            kpGenerator.initialize(spec);
            KeyPair kp = kpGenerator.generateKeyPair();

            HMAC_HELPER.setCurrentSerialNumber("1212121414");
            byte[] mac = HMAC_HELPER.computeMac(ByteArrayUtil.getInstance().bytes("00aa"));
            fail();
        }
        catch (Exception e) {
            e.printStackTrace();
            assertEquals(e.getMessage(), ERROR_MSG_ERR_IS_NOT_SEC_KEY_ENTRY);
        }
    }


}