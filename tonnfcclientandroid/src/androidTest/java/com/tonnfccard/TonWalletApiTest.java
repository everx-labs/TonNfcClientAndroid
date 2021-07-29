package com.tonnfccard;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.tonnfccard.helpers.ExceptionHelper;
import com.tonnfccard.helpers.HmacHelper;
import com.tonnfccard.helpers.JsonHelper;
import com.tonnfccard.helpers.StringHelper;
import com.tonnfccard.nfc.NfcApduRunner;
import com.tonnfccard.utils.ByteArrayUtil;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.security.KeyStore;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Random;

import javax.crypto.Mac;

import static com.tonnfccard.TonWalletConstants.COMMON_SECRET_SIZE;
import static com.tonnfccard.TonWalletConstants.DONE_MSG;
import static com.tonnfccard.TonWalletConstants.EMPTY_SERIAL_NUMBER;
import static com.tonnfccard.TonWalletConstants.FALSE_MSG;
import static com.tonnfccard.TonWalletConstants.HMAC_KEYS_ARE_NOT_FOUND_MSG;
import static com.tonnfccard.TonWalletConstants.PASSWORD_SIZE;
import static com.tonnfccard.TonWalletConstants.SERIAL_NUMBER_SIZE;
import static com.tonnfccard.TonWalletConstants.SUCCESS_STATUS;
import static com.tonnfccard.TonWalletConstants.TRUE_MSG;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_KEY_FOR_HMAC_DOES_NOT_EXIST_IN_ANDROID_KEYSTORE;
import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class TonWalletApiTest {

    private TonWalletApi tonWalletApi;
    private final ExceptionHelper EXCEPTION_HELPER = ExceptionHelper.getInstance();
    private final StringHelper STRING_HELPER = StringHelper.getInstance();
    private final ByteArrayUtil BYTE_ARRAY_HELPER = ByteArrayUtil.getInstance();
    private static final HmacHelper HMAC_HELPER = HmacHelper.getInstance();
    private final JsonHelper JSON_HELPER = JsonHelper.getInstance();
    private final Random random = new Random();

    @Before
    public void init() throws Exception{
        Context context = ApplicationProvider.getApplicationContext();
        NfcApduRunner nfcApduRunner = NfcApduRunner.getInstance(context);
        tonWalletApi = new CardActivationApi(context, nfcApduRunner);
        clearKeyStore();
    }

    private void clearKeyStore() throws Exception {
        final KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);
        Enumeration<String> aliases = keyStore.aliases();
        while (aliases.hasMoreElements()) {
            String alias = aliases.nextElement();
            if (alias.startsWith(HmacHelper.HMAC_KEY_ALIAS))
                keyStore.deleteEntry(alias);
        }
    }

    @Test
    public void testCreateKeyForHmacSuccessfullOperation()  {
        try {
            String sn = STRING_HELPER.randomDigitalString(SERIAL_NUMBER_SIZE);
            String password = STRING_HELPER.randomHexString(2 * PASSWORD_SIZE);
            String cs = STRING_HELPER.randomHexString(2 * COMMON_SECRET_SIZE);
            String response = tonWalletApi.createKeyForHmacAndGetJson(password, cs, sn);
            assertEquals(response.toLowerCase(), JSON_HELPER.createResponseJson(DONE_MSG).toLowerCase());
            System.out.println(response);
            final KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            String keyAlias = HmacHelper.HMAC_KEY_ALIAS + sn;
            keyStore.load(null);
            assertTrue(keyStore.containsAlias(keyAlias));
            response = tonWalletApi.getCurrentSerialNumberAndGetJson();
            assertEquals(response.toLowerCase(), JSON_HELPER.createResponseJson(sn).toLowerCase());
            byte[] data = new byte[20];
            random.nextBytes(data);
            KeyStore.Entry entry = keyStore.getEntry(keyAlias, null);
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            sha256_HMAC.init(((KeyStore.SecretKeyEntry) entry).getSecretKey());
            byte[] sig1 = sha256_HMAC.doFinal(data);
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] key = HMAC_HELPER.computeMac(digest.digest(BYTE_ARRAY_HELPER.bytes(password)), BYTE_ARRAY_HELPER.bytes(cs));
            byte[] sig2 = HMAC_HELPER.computeMac(key, data);
            assertArrayEquals(sig1, sig2);
        }
        catch (Exception e) {
            fail();
            e.printStackTrace();
        }
    }

    @Test
    public void testIsKeyForHmacExistTrue()  {
        try {
            String sn = STRING_HELPER.randomDigitalString(SERIAL_NUMBER_SIZE);
            String password = STRING_HELPER.randomHexString(2 * PASSWORD_SIZE);
            String cs = STRING_HELPER.randomHexString(2 * COMMON_SECRET_SIZE);
            tonWalletApi.createKeyForHmacAndGetJson(password, cs, sn);
            String response = tonWalletApi.isKeyForHmacExistAndGetJson(sn);
            System.out.println(response);
            assertEquals(response.toLowerCase(), JSON_HELPER.createResponseJson(TRUE_MSG).toLowerCase());
            response = tonWalletApi.getCurrentSerialNumberAndGetJson();
            assertEquals(response.toLowerCase(), JSON_HELPER.createResponseJson(sn).toLowerCase());
        }
        catch (Exception e) {
            fail();
            e.printStackTrace();
        }
    }

    @Test
    public void testIsKeyForHmacExistFalse()  {
        try {
            String sn = STRING_HELPER.randomDigitalString(SERIAL_NUMBER_SIZE);
            String response = tonWalletApi.isKeyForHmacExistAndGetJson(sn);
            System.out.println(response);
            assertEquals(response.toLowerCase(), JSON_HELPER.createResponseJson(FALSE_MSG).toLowerCase());
        }
        catch (Exception e) {
            fail();
            e.printStackTrace();
        }
    }

    @Test
    public void testDeleteKeyForHmacFail()  {
        try {
            String sn = STRING_HELPER.randomDigitalString(SERIAL_NUMBER_SIZE);
            tonWalletApi.deleteKeyForHmacAndGetJson(sn);
            fail();
        }
        catch (Exception e) {
            assertEquals(e.getMessage(), EXCEPTION_HELPER.makeFinalErrMsg(new Exception(ERROR_MSG_KEY_FOR_HMAC_DOES_NOT_EXIST_IN_ANDROID_KEYSTORE)));
        }
    }

    @Test
    public void testDeleteKeyForHmacSuccessfullOperation()  {
        try {
            String sn = STRING_HELPER.randomDigitalString(SERIAL_NUMBER_SIZE);
            String password = STRING_HELPER.randomHexString(2 * PASSWORD_SIZE);
            String cs = STRING_HELPER.randomHexString(2 * COMMON_SECRET_SIZE);
            tonWalletApi.createKeyForHmacAndGetJson(password, cs, sn);
            String response = tonWalletApi.deleteKeyForHmacAndGetJson(sn);
            assertEquals(response.toLowerCase(), JSON_HELPER.createResponseJson(DONE_MSG).toLowerCase());
            response = tonWalletApi.isKeyForHmacExistAndGetJson(sn);
            System.out.println(response);
            assertEquals(response.toLowerCase(), JSON_HELPER.createResponseJson(FALSE_MSG).toLowerCase());
            response = tonWalletApi.getCurrentSerialNumberAndGetJson();
            assertEquals(response.toLowerCase(), JSON_HELPER.createResponseJson(EMPTY_SERIAL_NUMBER).toLowerCase());
        }
        catch (Exception e) {
            fail();
            e.printStackTrace();
        }
    }

    @Test
    public void testDeleteKeyForHmacSuccessfullOperation2()  {
        try {
            clearKeyStore();
            List<String> serialNumbers = new ArrayList<>();
            for(int i = 0; i < 100; i++) {
                String sn = STRING_HELPER.randomDigitalString(SERIAL_NUMBER_SIZE);
                String password = STRING_HELPER.randomHexString(2 * PASSWORD_SIZE);
                String cs = STRING_HELPER.randomHexString(2 * COMMON_SECRET_SIZE);
                tonWalletApi.createKeyForHmacAndGetJson(password, cs, sn);
                serialNumbers.add(sn);
            }
            String response = tonWalletApi.deleteKeyForHmacAndGetJson(serialNumbers.get(43));
            assertEquals(response.toLowerCase(), JSON_HELPER.createResponseJson(DONE_MSG).toLowerCase());
            response = tonWalletApi.isKeyForHmacExistAndGetJson(serialNumbers.get(43));
            System.out.println(response);
            assertEquals(response.toLowerCase(), JSON_HELPER.createResponseJson(FALSE_MSG).toLowerCase());
            response = tonWalletApi.getCurrentSerialNumberAndGetJson();
            assertEquals(response.toLowerCase(), JSON_HELPER.createResponseJson(serialNumbers.get(serialNumbers.size() - 1)).toLowerCase());
            int counter = 0;
            final KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            Enumeration<String> aliases = keyStore.aliases();
            while (aliases.hasMoreElements()) {
                String alias = aliases.nextElement();
                if (alias.startsWith(HmacHelper.HMAC_KEY_ALIAS))
                    counter++;
            }
            assertEquals(counter, 99);
        }
        catch (Exception e) {
            fail();
            e.printStackTrace();
        }
    }

    @Test
    public void testGetAllSerialNumbersSuccessfullOperation()  {
        try {
            clearKeyStore();
            List<String> serialNumbers = new ArrayList<>();
            for(int i = 0; i < 100; i++) {
                String sn = STRING_HELPER.randomDigitalString(SERIAL_NUMBER_SIZE);
                String password = STRING_HELPER.randomHexString(2 * PASSWORD_SIZE);
                String cs = STRING_HELPER.randomHexString(2 * COMMON_SECRET_SIZE);
                tonWalletApi.createKeyForHmacAndGetJson(password, cs, sn);
                serialNumbers.add(sn);
            }
            String response = tonWalletApi.getAllSerialNumbersAndGetJson();
            System.out.println(response);
            JSONObject obj = new JSONObject(response);
            assertEquals(obj.get(TonWalletConstants.STATUS_FIELD), SUCCESS_STATUS);
            JSONArray sns = obj.getJSONArray(TonWalletConstants.MESSAGE_FIELD);
            assertEquals(sns.length(), 100);
            for (int i = 0 ; i < sns.length(); i++) {
                assertTrue(serialNumbers.contains(sns.getString(i)));
            }
            response = tonWalletApi.getCurrentSerialNumberAndGetJson();
            System.out.println(response);
            assertEquals(response.toLowerCase(), JSON_HELPER.createResponseJson(serialNumbers.get(serialNumbers.size() - 1)).toLowerCase());
        }
        catch (Exception e) {
            fail();
            e.printStackTrace();
        }
    }

    @Test
    public void testGetAllSerialNumbersAndDeleteSuccessfullOperation()  {
        try {
            clearKeyStore();
            List<String> serialNumbers = new ArrayList<>();
            for(int i = 0; i < 100; i++) {
                String sn = STRING_HELPER.randomDigitalString(SERIAL_NUMBER_SIZE);
                String password = STRING_HELPER.randomHexString(2 * PASSWORD_SIZE);
                String cs = STRING_HELPER.randomHexString(2 * COMMON_SECRET_SIZE);
                tonWalletApi.createKeyForHmacAndGetJson(password, cs, sn);
                serialNumbers.add(sn);
            }
            tonWalletApi.deleteKeyForHmacAndGetJson(serialNumbers.get(21));
            String response = tonWalletApi.getAllSerialNumbersAndGetJson();
            System.out.println(response);
            JSONObject obj = new JSONObject(response);
            assertEquals(obj.get(TonWalletConstants.STATUS_FIELD), SUCCESS_STATUS);
            JSONArray sns = obj.getJSONArray(TonWalletConstants.MESSAGE_FIELD);
            assertEquals(sns.length(), 99);
            serialNumbers.remove(21);
            for (int i = 0 ; i < sns.length(); i++) {
                assertTrue(serialNumbers.contains(sns.getString(i)));
            }
            response = tonWalletApi.getCurrentSerialNumberAndGetJson();
            System.out.println(response);
            assertEquals(response.toLowerCase(), JSON_HELPER.createResponseJson(serialNumbers.get(serialNumbers.size() - 1)).toLowerCase());
        }
        catch (Exception e) {
            fail();
            e.printStackTrace();
        }
    }

    @Test
    public void testGetAllSerialNumbersEmpty()  {
        try {
            clearKeyStore();
            String response = tonWalletApi.getAllSerialNumbersAndGetJson();
            assertEquals(response.toLowerCase(), JSON_HELPER.createResponseJson(HMAC_KEYS_ARE_NOT_FOUND_MSG).toLowerCase());
        }
        catch (Exception e) {
            fail();
            e.printStackTrace();
        }
    }

    @Test
    public void testSelectKeyForHmacFail()  {
        try {
            String sn = STRING_HELPER.randomDigitalString(SERIAL_NUMBER_SIZE);
            tonWalletApi.selectKeyForHmacAndGetJson(sn);
            fail();
        }
        catch (Exception e) {
            assertEquals(e.getMessage(), EXCEPTION_HELPER.makeFinalErrMsg(new Exception(ERROR_MSG_KEY_FOR_HMAC_DOES_NOT_EXIST_IN_ANDROID_KEYSTORE)));

        }
    }

    @Test
    public void testSelectKeyForHmacSuccessfullOperation()  {
        try {
            clearKeyStore();
            List<String> serialNumbers = new ArrayList<>();
            for(int i = 0; i < 100; i++) {
                String sn = STRING_HELPER.randomDigitalString(SERIAL_NUMBER_SIZE);
                String password = STRING_HELPER.randomHexString(2 * PASSWORD_SIZE);
                String cs = STRING_HELPER.randomHexString(2 * COMMON_SECRET_SIZE);
                tonWalletApi.createKeyForHmacAndGetJson(password, cs, sn);
                serialNumbers.add(sn);
            }
            String response = tonWalletApi.selectKeyForHmacAndGetJson(serialNumbers.get(56));
            assertEquals(response.toLowerCase(), JSON_HELPER.createResponseJson(DONE_MSG).toLowerCase());
            response = tonWalletApi.isKeyForHmacExistAndGetJson(serialNumbers.get(56));
            System.out.println(response);
            assertEquals(response.toLowerCase(), JSON_HELPER.createResponseJson(TRUE_MSG).toLowerCase());
            response = tonWalletApi.getCurrentSerialNumberAndGetJson();
            assertEquals(response.toLowerCase(), JSON_HELPER.createResponseJson(serialNumbers.get(56)).toLowerCase());
            int counter = 0;
            final KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            Enumeration<String> aliases = keyStore.aliases();
            while (aliases.hasMoreElements()) {
                String alias = aliases.nextElement();
                if (alias.startsWith(HmacHelper.HMAC_KEY_ALIAS))
                    counter++;
            }
            assertEquals(counter, 100);
        }
        catch (Exception e) {
            fail();
            e.printStackTrace();
        }
    }
}