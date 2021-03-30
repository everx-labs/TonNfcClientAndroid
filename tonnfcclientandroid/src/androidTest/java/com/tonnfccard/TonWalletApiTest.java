package com.tonnfccard;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import com.tonnfccard.helpers.ExceptionHelper;
import com.tonnfccard.helpers.HmacHelper;
import com.tonnfccard.helpers.JsonHelper;
import com.tonnfccard.helpers.StringHelper;
import com.tonnfccard.nfc.NfcApduRunner;
import com.tonnfccard.smartcard.ErrorCodes;
import com.tonnfccard.smartcard.RAPDU;
import com.tonnfccard.utils.ByteArrayUtil;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Random;

import static com.tonnfccard.TonWalletConstants.COMMON_SECRET_SIZE;
import static com.tonnfccard.TonWalletConstants.DEFAULT_PIN;
import static com.tonnfccard.TonWalletConstants.DEFAULT_PIN_STR;
import static com.tonnfccard.TonWalletConstants.DONE_MSG;
import static com.tonnfccard.TonWalletConstants.EMPTY_SERIAL_NUMBER;
import static com.tonnfccard.TonWalletConstants.FALSE_MSG;
import static com.tonnfccard.TonWalletConstants.PASSWORD_SIZE;
import static com.tonnfccard.TonWalletConstants.PERSONALIZED_STATE;
import static com.tonnfccard.TonWalletConstants.PERSONALIZED_STATE_MSG;
import static com.tonnfccard.TonWalletConstants.SERIAL_NUMBER_SIZE;
import static com.tonnfccard.TonWalletConstants.SERIAl_NUMBERS_FIELD;
import static com.tonnfccard.TonWalletConstants.STATUS_FIELD;
import static com.tonnfccard.TonWalletConstants.SUCCESS_STATUS;
import static com.tonnfccard.TonWalletConstants.TRUE_MSG;
import static com.tonnfccard.TonWalletConstants.WAITE_AUTHORIZATION_STATE;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_KEY_FOR_HMAC_DOES_NOT_EXIST_IN_ANDROID_KEYCHAIN;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_NFC_DISCONNECT;
import static com.tonnfccard.smartcard.CoinManagerApduCommands.RESET_WALLET_APDU;
import static com.tonnfccard.smartcard.CoinManagerApduCommands.getChangePinAPDU;
import static com.tonnfccard.smartcard.CoinManagerApduCommands.getGenerateSeedAPDU;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.GET_APP_INFO_APDU;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.GET_HASH_OF_ENCRYPTED_COMMON_SECRET_APDU;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.GET_HASH_OF_ENCRYPTED_PASSWORD_APDU;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.GET_SERIAL_NUMBER_APDU;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.getVerifyPasswordAPDU;
import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.*;

public class TonWalletApiTest {

    private TonWalletApi tonWalletApi;
    private final ExceptionHelper EXCEPTION_HELPER = ExceptionHelper.getInstance();
    private final StringHelper STRING_HELPER = StringHelper.getInstance();
    private final ByteArrayUtil BYTE_ARRAY_HELPER = ByteArrayUtil.getInstance();
    private final JsonHelper JSON_HELPER = JsonHelper.getInstance();
    private Random random = new Random();
    private Context context;
    private NfcApduRunner nfcApduRunner;

    private static final String SERIAL_NUMBER = "504394802433901126813236";
    private static final String COMMON_SECRET = "7256EFE7A77AFC7E9088266EF27A93CB01CD9432E0DB66D600745D506EE04AC4";
    private static final String PASSWORD =  "F4B072E1DF2DB7CF6CD0CD681EC5CD2D071458D278E6546763CBB4860F8082FE14418C8A8A55E2106CBC6CB1174F4BA6D827A26A2D205F99B7E00401DA4C15ACC943274B92258114B5E11C16DA64484034F93771547FBE60DA70E273E6BD64F8A4201A9913B386BCA55B6678CFD7E7E68A646A7543E9E439DD5B60B9615079FE";

    @Before
    public void init() throws Exception{
        context = ApplicationProvider.getApplicationContext();
        nfcApduRunner = NfcApduRunner.getInstance(context);
        tonWalletApi = new CardActivationApi(context, nfcApduRunner);

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
            response = tonWalletApi.getCurrentSerialNumber();
            assertEquals(response.toLowerCase(), JSON_HELPER.createResponseJson(sn).toLowerCase());
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
            response = tonWalletApi.getCurrentSerialNumber();
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
    public void testDeleteKeyForHmactFail()  {
        try {
            String sn = STRING_HELPER.randomDigitalString(SERIAL_NUMBER_SIZE);
            tonWalletApi.deleteKeyForHmacAndGetJson(sn);
            fail();
        }
        catch (Exception e) {
            assertEquals(e.getMessage(), EXCEPTION_HELPER.makeFinalErrMsg(new Exception(ERROR_MSG_KEY_FOR_HMAC_DOES_NOT_EXIST_IN_ANDROID_KEYCHAIN)));

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
            response = tonWalletApi.getCurrentSerialNumber();
            assertEquals(response.toLowerCase(), JSON_HELPER.createResponseJson(EMPTY_SERIAL_NUMBER).toLowerCase());
        }
        catch (Exception e) {
            fail();
            e.printStackTrace();
        }
    }

    @Test
    public void testGetAllSerialNumbersSuccessfullOperation()  {
        try {
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
            JSONArray sns = obj.getJSONArray(SERIAl_NUMBERS_FIELD);
            assertEquals(sns.length(), 100);
            for (int i = 0 ; i < sns.length(); i++) {
                assertTrue(serialNumbers.contains(sns.getString(i)));
            }
            response = tonWalletApi.getCurrentSerialNumber();
            System.out.println(response);
            assertEquals(response.toLowerCase(), JSON_HELPER.createResponseJson(serialNumbers.get(serialNumbers.size() - 1)).toLowerCase());
        }
        catch (Exception e) {
            fail();
            e.printStackTrace();
        }
    }



}