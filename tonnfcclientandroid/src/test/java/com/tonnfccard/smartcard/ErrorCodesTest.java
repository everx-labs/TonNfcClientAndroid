package com.tonnfccard.smartcard;

import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_APDU_DATA_FIELD_IS_NULL;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_APDU_RESPONSE_IS_NULL;
import static com.tonnfccard.smartcard.CoinManagerApduCommands.getCoinManagerApduCommandName;
import static com.tonnfccard.smartcard.ErrorCodes.getMsg;
import static org.junit.Assert.*;
import org.junit.Test;

public class ErrorCodesTest {

    @Test
    public void getMsgTestNullData() {
        try {
            RAPDU rapdu = null;
            getMsg(rapdu);
            fail();
        }
        catch (Exception e) {
            assertEquals(e.getMessage(), ERROR_MSG_APDU_RESPONSE_IS_NULL);
        }
        try {
            String rapdu = null;
            getMsg(rapdu);
            fail();
        }
        catch (Exception e) {
            assertEquals(e.getMessage(), ERROR_MSG_APDU_RESPONSE_IS_NULL);
        }
    }

    @Test
    public void getMsgTest() {
        try {
            RAPDU rapdu = new RAPDU(new byte[]{(byte)0x90, (byte)0x00});
            String msg = getMsg(rapdu);
            assertEquals("No error.", msg);

            rapdu = new RAPDU(new byte[]{(byte)0x8F, (byte)0x01});
            msg = getMsg(rapdu);
            assertEquals("Incorrect sault.", msg);

            msg = getMsg("8F02");
            assertEquals("Data integrity corrupted.", msg);

            msg = getMsg("6999");
            assertEquals("Applet select failed.", msg);
        }
        catch (Exception e) {
            fail();
        }
    }

}