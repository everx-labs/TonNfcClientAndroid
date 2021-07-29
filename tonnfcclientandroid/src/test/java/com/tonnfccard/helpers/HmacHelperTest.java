package com.tonnfccard.helpers;

import com.tonnfccard.utils.ByteArrayUtil;

import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static com.tonnfccard.TonWalletConstants.EMPTY_SERIAL_NUMBER;
import static com.tonnfccard.TonWalletConstants.SHA_HASH_SIZE;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_CURRENT_SERIAL_NUMBER_IS_NOT_SET_IN_ANDROID_KEYSTORE;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_ERR_CURRENT_SERIAL_NUMBER_IS_NULL;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_ERR_DATA_BYTES_FOR_HMAC_SHA256_IS_NULL;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_ERR_KEY_BYTES_FOR_HMAC_SHA256_IS_NULL;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_ERR_KEY_BYTES_FOR_HMAC_SHA256_IS_TOO_SHORT;
import static org.junit.Assert.*;

public class HmacHelperTest {
    public static final HmacHelper HMAC_HELPER = HmacHelper.getInstance();

    @Test
    public void testDifferentKeysForComputeMac()   {
        Map<byte[], String> keyToErrMsg = new LinkedHashMap<>();
        keyToErrMsg.put(null, ERROR_MSG_ERR_KEY_BYTES_FOR_HMAC_SHA256_IS_NULL);
        keyToErrMsg.put(new byte[SHA_HASH_SIZE - 1], ERROR_MSG_ERR_KEY_BYTES_FOR_HMAC_SHA256_IS_TOO_SHORT);
        keyToErrMsg.put(new byte[SHA_HASH_SIZE], ERROR_MSG_ERR_DATA_BYTES_FOR_HMAC_SHA256_IS_NULL);
        for(byte[] key : keyToErrMsg.keySet()) {
            try {
                HMAC_HELPER.computeMac(key, null);
                fail();
            } catch (Exception e) {
                assertEquals(e.getMessage(), keyToErrMsg.get(key));
            }
        }
    }

    @Test
    // hash to verify taken from https://www.liavaag.org/English/SHA-Generator/HMAC/
    public void testNullDataForComputeMc()   {
        try {
            byte[] mac = HMAC_HELPER.computeMac(new byte[SHA_HASH_SIZE], ByteArrayUtil.getInstance().bytes("00aa"));
            assertEquals("89c3c76721b45a2314a3f750a6fc31ebfa17bca96979ca95a7f4849ac814501f", ByteArrayUtil.getInstance().hex(mac).toLowerCase());
        }
        catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testDifferentDataForComputeMac()   {
        try {
            HMAC_HELPER.computeMac(null);
            fail();
        }
        catch (Exception e) {
            assertEquals(e.getMessage(), ERROR_MSG_ERR_DATA_BYTES_FOR_HMAC_SHA256_IS_NULL);
        }
    }


    @Test
    public void testNullSerialNumber()   {
        try {
            HMAC_HELPER.setCurrentSerialNumber(null);
            byte[] mac = HMAC_HELPER.computeMac(new byte[1]);
            fail();
        }
        catch (Exception e) {
            assertEquals(e.getMessage(), ERROR_MSG_ERR_CURRENT_SERIAL_NUMBER_IS_NULL);
        }
    }

    @Test
    public void testEmptySerialNumber()   {
        try {
            HMAC_HELPER.setCurrentSerialNumber(EMPTY_SERIAL_NUMBER);
            byte[] mac = HMAC_HELPER.computeMac(new byte[1]);
            fail();
        }
        catch (Exception e) {
            assertEquals(e.getMessage(), ERROR_MSG_CURRENT_SERIAL_NUMBER_IS_NOT_SET_IN_ANDROID_KEYSTORE);
        }
    }



}