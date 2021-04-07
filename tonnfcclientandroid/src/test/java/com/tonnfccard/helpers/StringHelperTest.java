package com.tonnfccard.helpers;

import android.app.Activity;

import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_ARRAY_ELEMENTS_ARE_NOT_DIGITS;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_ARRAY_TO_MAKE_DIGITAL_STR_MUST_NOT_BE_EMPTY;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_PIN_FORMAT_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_PIN_LEN_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_STRING_IS_NOT_ASCII;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_STRING_IS_NULL;
import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.*;

public class StringHelperTest {
    private static final StringHelper STRING_HELPER = StringHelper.getInstance();

    @Test
    public void isHexStringTest() {
        assertFalse(STRING_HELPER.isHexString(null));
        assertFalse(STRING_HELPER.isHexString(""));
        assertFalse(STRING_HELPER.isHexString("aa0"));
        assertFalse(STRING_HELPER.isHexString("S0ASJH"));
        assertTrue(STRING_HELPER.isHexString("0000AAFF"));
        assertTrue(STRING_HELPER.isHexString("0000AbFf"));
    }

    @Test
    public void isNumericStringTest() {
        assertTrue(STRING_HELPER.isHexString("0123456789"));
        assertFalse(STRING_HELPER.isHexString("0123456789A"));
        assertFalse(STRING_HELPER.isHexString(null));
    }

    @Test
    public void pinToHexTest() {
        Map<String, String> badPinToErrMsg = new LinkedHashMap<>();
        badPinToErrMsg.put(null, ERROR_MSG_PIN_FORMAT_INCORRECT); badPinToErrMsg.put("0123456789A", ERROR_MSG_PIN_FORMAT_INCORRECT);
        badPinToErrMsg.put("33333", ERROR_MSG_PIN_LEN_INCORRECT); badPinToErrMsg.put("222", ERROR_MSG_PIN_LEN_INCORRECT);
        for(String badPin : badPinToErrMsg.keySet()) {
            try {
                STRING_HELPER.pinToHex(badPin);
                fail();
            } catch (IllegalArgumentException e) {
                assertEquals(e.getMessage(), badPinToErrMsg.get(badPin));
            }
        }
        assertEquals(STRING_HELPER.pinToHex("3456"), "33343536");
        assertEquals(STRING_HELPER.pinToHex("5555"), "35353535");
    }

    @Test
    public void asciiToHexTest() {
        Map<String, String> badStrToErrMsg = new LinkedHashMap<>();
        badStrToErrMsg.put(null, ERROR_MSG_STRING_IS_NULL); badStrToErrMsg.put("whÿ", ERROR_MSG_STRING_IS_NOT_ASCII);
        for(String badStr : badStrToErrMsg.keySet()) {
            try {
                STRING_HELPER.asciiToHex(badStr);
                fail();
            } catch (IllegalArgumentException e) {
                assertEquals(e.getMessage(), badStrToErrMsg.get(badStr));
            }
        }
        assertEquals(STRING_HELPER.asciiToHex("12A"),"313241");
        assertEquals(STRING_HELPER.asciiToHex("123"), "313233");
        assertEquals(STRING_HELPER.asciiToHex("5555"), STRING_HELPER.pinToHex("5555"));
        assertEquals(STRING_HELPER.asciiToHex("3456"), STRING_HELPER.pinToHex("3456"));
        String t = new String(new byte[]{0, 1, 48, 2, 49});
        System.out.println(t);
        assertEquals(STRING_HELPER.asciiToHex(t), "0001300231");
    }

    @Test
    public void makeDigitalStringTest() {
        Map<byte[], String> badByteArrToErrMsg = new LinkedHashMap<>();
        badByteArrToErrMsg.put(null, ERROR_MSG_ARRAY_TO_MAKE_DIGITAL_STR_MUST_NOT_BE_EMPTY); badByteArrToErrMsg.put(new byte[0], ERROR_MSG_ARRAY_TO_MAKE_DIGITAL_STR_MUST_NOT_BE_EMPTY);
        badByteArrToErrMsg.put(new byte[]{10, 0}, ERROR_MSG_ARRAY_ELEMENTS_ARE_NOT_DIGITS);
        for(byte[] badArr : badByteArrToErrMsg.keySet()) {
            try {
                STRING_HELPER.makeDigitalString(badArr);
                fail();
            } catch (IllegalArgumentException e) {
                assertEquals(e.getMessage(), badByteArrToErrMsg.get(badArr));
            }
        }
        assertEquals(STRING_HELPER.makeDigitalString(new byte[]{9, 0, 3}), "903");
    }
}