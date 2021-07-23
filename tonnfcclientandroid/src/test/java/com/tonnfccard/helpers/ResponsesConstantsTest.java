package com.tonnfccard.helpers;

import org.junit.Test;

import static com.tonnfccard.helpers.ResponsesConstants.*;
import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.*;

public class ResponsesConstantsTest {

    @Test
    public void testGetErrorTypeMsg() {
        assertEquals(ResponsesConstants.getErrorTypeMsg(CARD_ERROR_TYPE_ID), CARD_ERROR_TYPE_MSG);
        assertEquals(ResponsesConstants.getErrorTypeMsg(ANDROID_INTERNAL_ERROR_TYPE_ID), ANDROID_INTERNAL_ERROR_TYPE_MSG);
        assertEquals(ResponsesConstants.getErrorTypeMsg(ANDROID_NFC_ERROR_TYPE_ID), ANDROID_NFC_ERROR_TYPE_MSG);
        assertEquals(ResponsesConstants.getErrorTypeMsg(INPUT_DATA_FORMAT_ERROR_TYPE_ID), INPUT_DATA_FORMAT_ERROR_TYPE_MSG );
        assertEquals(ResponsesConstants.getErrorTypeMsg(CARD_RESPONSE_DATA_ERROR_TYPE_ID), CARD_RESPONSE_DATA_ERROR_TYPE_MSG);
        assertEquals(ResponsesConstants.getErrorTypeMsg(IMPROPER_APPLET_STATE_ERROR_TYPE_ID), IMPROPER_APPLET_STATE_ERROR_TYPE_MSG);
        assertEquals(ResponsesConstants.getErrorTypeMsg(ANDROID_KEYSTORE_HMAC_KEY_ERROR_TYPE_ID), ANDROID_KEYSTORE_HMAC_KEY_ERROR_TYPE_MSG);
    }

    @Test
    public void testGetErrorCode() {
        for (int i = 0; i < ALL_NATIVE_ERROR_MESSAGES.size(); i++) {
            for (int j = 0; j < ALL_NATIVE_ERROR_MESSAGES.get(i).size(); j++) {
                String msg = ResponsesConstants.getErrorCode(ALL_NATIVE_ERROR_MESSAGES.get(i).get(j));
                assertTrue(msg.startsWith(String.valueOf(i + 1)));
                assertTrue(msg.endsWith(String.valueOf(j)));
                assertEquals(msg.length(), 5);
            }
        }

    }

    @Test
    public void testGetErrorType() {
        for (int i = 0; i < ALL_NATIVE_ERROR_MESSAGES.size(); i++) {
            for (int j = 0; j < ALL_NATIVE_ERROR_MESSAGES.get(i).size(); j++) {
                String msg = ResponsesConstants.getErrorType(ALL_NATIVE_ERROR_MESSAGES.get(i).get(j));
                assertEquals(msg.substring(0, 1), ERROR_TYPE_IDS.get(i + 1));
            }
        }
    }


}