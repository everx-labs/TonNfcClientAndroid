package com.tonnfccard.helpers;

import android.util.Log;

import com.tonnfccard.TonWalletConstants;
import com.tonnfccard.callback.NfcCallback;

import org.json.JSONObject;
import org.junit.Test;

import static com.tonnfccard.helpers.ResponsesConstants.ANDROID_INTERNAL_ERRORS;
import static com.tonnfccard.helpers.ResponsesConstants.ANDROID_INTERNAL_ERROR_TYPE_ID;
import static com.tonnfccard.helpers.ResponsesConstants.ANDROID_INTERNAL_ERROR_TYPE_MSG;
import static com.tonnfccard.helpers.ResponsesConstants.ANDROID_NFC_ERRORS;
import static com.tonnfccard.helpers.ResponsesConstants.ANDROID_NFC_ERROR_TYPE_ID;
import static com.tonnfccard.helpers.ResponsesConstants.ANDROID_NFC_ERROR_TYPE_MSG;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_ERR_MSG_IS_NULL;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_EXCEPTION_OBJECT_IS_NULL;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_NFC_CONNECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_PASSWORD_LEN_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.INPUT_DATA_FORMAT_ERRORS;
import static com.tonnfccard.helpers.ResponsesConstants.INPUT_DATA_FORMAT_ERROR_TYPE_ID;
import static com.tonnfccard.helpers.ResponsesConstants.INPUT_DATA_FORMAT_ERROR_TYPE_MSG;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.fail;

public class ExceptionHelperTest {
    public static final String ERR_MSG_GENERATED_AFTER_APPLET_FAIL = "{\n" +
            "\"errorType\": \"Applet fail: card operation error\",\n" +
            "\"errorTypeId\": \"0\",\n" +
            "\"сode\": \"6E00\",\n" +
            "\"message\": \"CLA value not supported.\",\n" +
            "\"cardInstruction\": \"GET_APP_INFO\",\n" +
            "\"apdu\": \"B0 C1 00 00 \",\n" +
            "\"status\": \"fail\"\n" +
            "}";

    public static final ExceptionHelper EXCEPTION_HELPER = ExceptionHelper.getInstance();

    @Test
    public void testhHandleException()  {
        Exception e = new Exception(ERROR_MSG_NFC_CONNECT);
        EXCEPTION_HELPER.handleException(e, null, "TAG");
        assertTrue(true);
    }

    @Test
    public void testhHandleException2()  {
        Exception e = new Exception(ERROR_MSG_NFC_CONNECT);
        EXCEPTION_HELPER.handleException(e, new NfcCallback(System.out::println, errorMsg -> {
            checkForNfcError(errorMsg, ERROR_MSG_NFC_CONNECT);
        }),"TAG");
    }

    @Test
    public void testhHandleException3()  {
        EXCEPTION_HELPER.handleException(null, new NfcCallback(System.out::println, errorMsg -> {
            checkForAndroidInternalError(errorMsg,  ERROR_MSG_EXCEPTION_OBJECT_IS_NULL);
        }),"TAG");
    }

    @Test
    public void testhHandleException4()  {
        String msg = null;
        EXCEPTION_HELPER.handleException(new Exception(msg), new NfcCallback(System.out::println, errorMsg -> {
            checkForAndroidInternalError(errorMsg,  ERROR_MSG_ERR_MSG_IS_NULL);
        }),"TAG");
    }

    @Test
    public void testhHandleException5()  {
        EXCEPTION_HELPER.handleException(new Exception(ERR_MSG_GENERATED_AFTER_APPLET_FAIL), new NfcCallback(System.out::println, errorMsg -> {
            try {
                System.out.println(errorMsg);
                assertEquals(errorMsg, ERR_MSG_GENERATED_AFTER_APPLET_FAIL);
            }
            catch (Exception e1) {
                fail();
            }
        }),"TAG");
    }

    @Test
    public void testMakeErrMsgWithNullException()  {
        Exception e = null;
        String msg = EXCEPTION_HELPER.makeFinalErrMsg(e);
        checkForAndroidInternalError(msg, ERROR_MSG_EXCEPTION_OBJECT_IS_NULL);
    }

    @Test
    public void testMakeErrMsgWithExceptionHavingNullMsg()  {
        String s = null;
        String msg = EXCEPTION_HELPER.makeFinalErrMsg(new Exception(s));
        checkForAndroidInternalError(msg, ERROR_MSG_ERR_MSG_IS_NULL);
    }

    @Test
    public void testMakeErrMsgForAppletError()  {
        String msg = EXCEPTION_HELPER.makeFinalErrMsg(new Exception(ERR_MSG_GENERATED_AFTER_APPLET_FAIL));
        assertEquals(msg, ERR_MSG_GENERATED_AFTER_APPLET_FAIL);
        msg = EXCEPTION_HELPER.makeFinalErrMsg(ERR_MSG_GENERATED_AFTER_APPLET_FAIL);
        assertEquals(msg, ERR_MSG_GENERATED_AFTER_APPLET_FAIL);
    }

    @Test
    public void testMakeErrMsgForAppletError2()  {
        String msg = EXCEPTION_HELPER.makeFinalErrMsg(ERR_MSG_GENERATED_AFTER_APPLET_FAIL);
        assertEquals(msg, ERR_MSG_GENERATED_AFTER_APPLET_FAIL);
        msg = EXCEPTION_HELPER.makeFinalErrMsg(ERR_MSG_GENERATED_AFTER_APPLET_FAIL);
        assertEquals(msg, ERR_MSG_GENERATED_AFTER_APPLET_FAIL);
    }

    @Test
    public void testMakeErrMsgForOtherError()  {
        String msg = EXCEPTION_HELPER.makeFinalErrMsg(new Exception(ERROR_MSG_NFC_CONNECT));
        checkForNfcError(msg, ERROR_MSG_NFC_CONNECT);
    }

    @Test
    public void testMakeErrMsgForOtherError2()  {
        String msg = EXCEPTION_HELPER.makeFinalErrMsg(ERROR_MSG_NFC_CONNECT);
        checkForNfcError(msg, ERROR_MSG_NFC_CONNECT);
    }

    @Test
    public void testMakeErrMsgWithNullMsg()  {
        String s = null;
        String msg = EXCEPTION_HELPER.makeFinalErrMsg(s);
        checkForAndroidInternalError(msg, ERROR_MSG_ERR_MSG_IS_NULL);
    }

    private void checkForAndroidInternalError(String msg, String correctMsg){
        System.out.println(msg);
        try {
            JSONObject obj = new JSONObject(msg);
            assertEquals(obj.get(TonWalletConstants.MESSAGE_FIELD), correctMsg);
            assertEquals(obj.get(TonWalletConstants.ERROR_TYPE_ID_FIELD), ANDROID_INTERNAL_ERROR_TYPE_ID);
            assertEquals(obj.get(TonWalletConstants.STATUS_FIELD), TonWalletConstants.FAIL_STATUS);
            assertEquals(obj.get(TonWalletConstants.ERROR_TYPE_FIELD), ANDROID_INTERNAL_ERROR_TYPE_MSG);
            String code = (String)(obj.get(TonWalletConstants.ERROR_CODE_FIELD));
            assertTrue(code.endsWith(String.valueOf(ANDROID_INTERNAL_ERRORS.indexOf(correctMsg))));
            assertEquals(obj.length(), 5);
        }
        catch (Exception e) {
            fail();
        }
    }

    private void checkForNfcError(String msg, String correctMsg){
        System.out.println(msg);
        try {
            JSONObject obj = new JSONObject(msg);
            assertEquals(obj.get(TonWalletConstants.MESSAGE_FIELD), correctMsg);
            assertEquals(obj.get(TonWalletConstants.ERROR_TYPE_ID_FIELD), ANDROID_NFC_ERROR_TYPE_ID);
            assertEquals(obj.get(TonWalletConstants.STATUS_FIELD), TonWalletConstants.FAIL_STATUS);
            assertEquals(obj.get(TonWalletConstants.ERROR_TYPE_FIELD), ANDROID_NFC_ERROR_TYPE_MSG);
            String code = (String)(obj.get(TonWalletConstants.ERROR_CODE_FIELD));
            assertTrue(code.endsWith(String.valueOf(ANDROID_NFC_ERRORS.indexOf(correctMsg))));
            assertEquals(obj.length(), 5);
        }
        catch (Exception e) {
            fail();
        }
    }

}