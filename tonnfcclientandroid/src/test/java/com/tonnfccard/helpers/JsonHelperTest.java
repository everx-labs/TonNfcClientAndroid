package com.tonnfccard.helpers;

import com.tonnfccard.TonWalletConstants;
import com.tonnfccard.smartcard.ApduHelper;
import com.tonnfccard.smartcard.CAPDU;
import com.tonnfccard.smartcard.ErrorCodes;
import com.tonnfccard.utils.ByteArrayUtil;

import org.json.JSONObject;
import org.junit.Test;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;

import static com.tonnfccard.TonWalletConstants.DEFAULT_PIN;
import static com.tonnfccard.TonWalletConstants.DONE_MSG;
import static com.tonnfccard.TonWalletConstants.SAULT_LENGTH;
import static com.tonnfccard.helpers.ResponsesConstants.ANDROID_INTERNAL_ERROR_TYPE_ID;
import static com.tonnfccard.helpers.ResponsesConstants.ANDROID_INTERNAL_ERROR_TYPE_MSG;
import static com.tonnfccard.helpers.ResponsesConstants.ANDROID_NFC_ERRORS;
import static com.tonnfccard.helpers.ResponsesConstants.ANDROID_NFC_ERROR_TYPE_ID;
import static com.tonnfccard.helpers.ResponsesConstants.ANDROID_NFC_ERROR_TYPE_MSG;
import static com.tonnfccard.helpers.ResponsesConstants.CARD_ERROR_TYPE_ID;
import static com.tonnfccard.helpers.ResponsesConstants.CARD_ERROR_TYPE_MSG;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_CAPDU_IS_NULL;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_ERR_CURRENT_SERIAL_NUMBER_IS_NULL;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_MALFORMED_JSON_MSG;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_MALFORMED_SW_FOR_JSON;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_NFC_CONNECT;
import static com.tonnfccard.smartcard.ErrorCodes.SW_INCORRECT_PIN;
import static com.tonnfccard.smartcard.ErrorCodes.SW_SET_CURVE_FAILED;
import static com.tonnfccard.smartcard.ErrorCodes.SW_SUCCESS;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.GET_PUB_KEY_WITH_DEFAULT_PATH_APDU;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.getVerifyPinAPDU;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.*;

public class JsonHelperTest {
    public static final JsonHelper JSON_HELPER = JsonHelper.getInstance();

    @Test
    public void testCreateResponseJsonForNullMsg()   {
        try {
            JSON_HELPER.createResponseJson(null);
            fail();
        }
        catch (IllegalArgumentException e) {
            assertEquals(e.getMessage(), ERROR_MSG_MALFORMED_JSON_MSG);
        }
        catch (Exception e) {
            fail();
        }
    }


    @Test
    public void testCreateResponseJson()   {
        try {
            String s = JSON_HELPER.createResponseJson(DONE_MSG);
            JSONObject obj = new JSONObject(s);
            assertEquals(obj.get(TonWalletConstants.MESSAGE_FIELD), DONE_MSG);
            assertEquals(obj.get(TonWalletConstants.STATUS_FIELD), TonWalletConstants.SUCCESS_STATUS);
            assertEquals(obj.length(), 2);
        }
        catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testCreateErrorJsonForCardExceptionForBadSw()  {
        List<String> badSws = Arrays.asList(null, "123456", "", "345", "s123");
        badSws.forEach(sw -> {
            try {
                JSON_HELPER.createErrorJsonForCardException(sw, null);
                fail();
            }
            catch (IllegalArgumentException e) {
                assertEquals(e.getMessage(), ERROR_MSG_MALFORMED_SW_FOR_JSON);
            }
            catch (Exception e) {
                fail();
            }
        });
    }

    @Test
    public void testCreateErrorJsonForCardExceptionForNullCapdu()   {
        try {
            JSON_HELPER.createErrorJsonForCardException(ByteArrayUtil.getInstance().hex(SW_SUCCESS), null);
            fail();
        }
        catch (IllegalArgumentException e) {
            assertEquals(e.getMessage(), ERROR_MSG_CAPDU_IS_NULL);
        }
        catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testCreateErrorJsonForCardException()   {
        try {
            String s = JSON_HELPER.createErrorJsonForCardException(ByteArrayUtil.getInstance().hex(SW_SET_CURVE_FAILED), GET_PUB_KEY_WITH_DEFAULT_PATH_APDU);
            JSONObject obj = new JSONObject(s);
            assertEquals(obj.get(TonWalletConstants.MESSAGE_FIELD), ErrorCodes.getMsg(SW_SET_CURVE_FAILED));
            assertEquals(obj.get(TonWalletConstants.STATUS_FIELD), TonWalletConstants.FAIL_STATUS);
            assertEquals(obj.get(TonWalletConstants.ERROR_TYPE_ID_FIELD), CARD_ERROR_TYPE_ID);
            assertEquals(obj.get(TonWalletConstants.ERROR_TYPE_FIELD), CARD_ERROR_TYPE_MSG);
            assertEquals(obj.get(TonWalletConstants.ERROR_CODE_FIELD), ByteArrayUtil.getInstance().hex(SW_SET_CURVE_FAILED));
            assertEquals(obj.get(TonWalletConstants.CARD_INSTRUCTION_FIELD), ApduHelper.getInstance().getApduCommandName(GET_PUB_KEY_WITH_DEFAULT_PATH_APDU));
            String apdu = (String)(obj.get(TonWalletConstants.APDU_FIELD));
            assertEquals(apdu.trim(), GET_PUB_KEY_WITH_DEFAULT_PATH_APDU.getFormattedApdu().toString().trim());
            assertEquals(obj.length(), 7);
        }
        catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testCreateErrorJsonForCardExceptionForUnknownSw()   {
        try {
            String unknownSw = "7568";
            String s = JSON_HELPER.createErrorJsonForCardException(unknownSw, GET_PUB_KEY_WITH_DEFAULT_PATH_APDU);
            JSONObject obj = new JSONObject(s);
            assertEquals(obj.get(TonWalletConstants.STATUS_FIELD), TonWalletConstants.FAIL_STATUS);
            assertEquals(obj.get(TonWalletConstants.ERROR_TYPE_ID_FIELD), CARD_ERROR_TYPE_ID);
            assertEquals(obj.get(TonWalletConstants.ERROR_TYPE_FIELD), CARD_ERROR_TYPE_MSG);
            assertEquals(obj.get(TonWalletConstants.ERROR_CODE_FIELD), unknownSw);
            assertEquals(obj.get(TonWalletConstants.CARD_INSTRUCTION_FIELD), ApduHelper.getInstance().getApduCommandName(GET_PUB_KEY_WITH_DEFAULT_PATH_APDU));
            String apdu = (String)(obj.get(TonWalletConstants.APDU_FIELD));
            assertEquals(apdu.trim(), GET_PUB_KEY_WITH_DEFAULT_PATH_APDU.getFormattedApdu().toString().trim());
            assertEquals(obj.length(), 6);
        }
        catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testCreateErrorJsonForCardExceptionForUnknownSwAndUnknownApdu()   {
        try {
            String unknownSw = "6677";
            CAPDU unknownApdu = new CAPDU((byte)40, (byte) 80, (byte) 0x00, (byte) 0x00, (byte)1);
            String s = JSON_HELPER.createErrorJsonForCardException(unknownSw,  new CAPDU((byte)40, (byte) 80, (byte) 0x00, (byte) 0x00, (byte)1));
            JSONObject obj = new JSONObject(s);
            assertEquals(obj.get(TonWalletConstants.STATUS_FIELD), TonWalletConstants.FAIL_STATUS);
            assertEquals(obj.get(TonWalletConstants.ERROR_TYPE_ID_FIELD), CARD_ERROR_TYPE_ID);
            assertEquals(obj.get(TonWalletConstants.ERROR_TYPE_FIELD), CARD_ERROR_TYPE_MSG);
            assertEquals(obj.get(TonWalletConstants.ERROR_CODE_FIELD), unknownSw);
            String apdu = (String)(obj.get(TonWalletConstants.APDU_FIELD));
            assertEquals(apdu.trim(), unknownApdu.getFormattedApdu().toString().trim());
            assertEquals(obj.length(), 5);
        }
        catch (Exception e) {
            fail();
        }
    }


    @Test
    public void testCreateErrorJsonNullMsg()   {
        try {
            JSON_HELPER.createErrorJson(null);
            fail();
        }
        catch (IllegalArgumentException e) {
            assertEquals(e.getMessage(), ERROR_MSG_MALFORMED_JSON_MSG);
        }
        catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testCreateErrorJson()   {
        try {
            String s = JSON_HELPER.createErrorJson(ERROR_MSG_NFC_CONNECT);
            JSONObject obj = new JSONObject(s);
            assertEquals(obj.get(TonWalletConstants.STATUS_FIELD), TonWalletConstants.FAIL_STATUS);
            assertEquals(obj.get(TonWalletConstants.MESSAGE_FIELD), ERROR_MSG_NFC_CONNECT);
            assertEquals(obj.get(TonWalletConstants.ERROR_TYPE_ID_FIELD), ANDROID_NFC_ERROR_TYPE_ID);
            assertEquals(obj.get(TonWalletConstants.ERROR_TYPE_FIELD), ANDROID_NFC_ERROR_TYPE_MSG);
            assertEquals(obj.get(TonWalletConstants.ERROR_CODE_FIELD), "20000");
            assertEquals(obj.length(), 5);
        }
        catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testCreateErrorJsonForUnknownMessage()   {
        try {
            String unknownMsg = "Some error";
            String s = JSON_HELPER.createErrorJson(unknownMsg);
            JSONObject obj = new JSONObject(s);
            assertEquals(obj.get(TonWalletConstants.STATUS_FIELD), TonWalletConstants.FAIL_STATUS);
            assertEquals(obj.get(TonWalletConstants.MESSAGE_FIELD), unknownMsg);
            assertEquals(obj.get(TonWalletConstants.ERROR_TYPE_ID_FIELD), ANDROID_INTERNAL_ERROR_TYPE_ID);
            assertEquals(obj.get(TonWalletConstants.ERROR_TYPE_FIELD), ANDROID_INTERNAL_ERROR_TYPE_MSG);
            assertEquals(obj.length(), 4);
        }
        catch (Exception e) {
            fail();
        }
    }



}