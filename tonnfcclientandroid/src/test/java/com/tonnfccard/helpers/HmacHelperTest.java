package com.tonnfccard.helpers;

import com.tonnfccard.utils.ByteArrayUtil;

import org.junit.Test;

import static com.tonnfccard.TonWalletConstants.SHA_HASH_SIZE;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_ERR_DATA_BYTES_FOR_HMAC_SHA256_IS_NULL;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_ERR_KEY_BYTES_FOR_HMAC_SHA256_IS_NULL;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_ERR_KEY_BYTES_FOR_HMAC_SHA256_IS_TOO_SHORT;
import static org.junit.Assert.*;

public class HmacHelperTest {
    public static final HmacHelper HMAC_HELPER = HmacHelper.getInstance();

    @Test
    public void testNullKeyForComputeMac()   {
        try {
            HMAC_HELPER.computeMac(null, null);
            fail();
        }
        catch (Exception e) {
            assertEquals(e.getMessage(), ERROR_MSG_ERR_KEY_BYTES_FOR_HMAC_SHA256_IS_NULL);
        }
    }

    @Test
    public void testTooShortKeyForComputeMac()   {
        try {
            HMAC_HELPER.computeMac(new byte[SHA_HASH_SIZE - 1], null);
            fail();
        }
        catch (Exception e) {
            assertEquals(e.getMessage(), ERROR_MSG_ERR_KEY_BYTES_FOR_HMAC_SHA256_IS_TOO_SHORT);
        }
    }

    @Test
    public void testNullDataForComputeMac()   {
        try {
            byte[] mac = HMAC_HELPER.computeMac(new byte[SHA_HASH_SIZE], null);
            fail();
        }
        catch (Exception e) {
            assertEquals(e.getMessage(), ERROR_MSG_ERR_DATA_BYTES_FOR_HMAC_SHA256_IS_NULL);
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




}