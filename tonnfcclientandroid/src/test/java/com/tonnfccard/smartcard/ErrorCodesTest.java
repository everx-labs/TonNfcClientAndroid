package com.tonnfccard.smartcard;

import com.tonnfccard.utils.ByteArrayUtil;

import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_APDU_DATA_FIELD_IS_NULL;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_APDU_RESPONSE_IS_NULL;
import static com.tonnfccard.smartcard.CoinManagerApduCommands.getCoinManagerApduCommandName;
import static com.tonnfccard.smartcard.ErrorCodes.SW_APPLET_SELECT_FAILED;
import static com.tonnfccard.smartcard.ErrorCodes.SW_APPLET_SELECT_FAILED_MSG;
import static com.tonnfccard.smartcard.ErrorCodes.SW_DATA_INTEGRITY_CORRUPTED;
import static com.tonnfccard.smartcard.ErrorCodes.SW_DATA_INTEGRITY_CORRUPTED_MSG;
import static com.tonnfccard.smartcard.ErrorCodes.SW_INCORRECT_SAULT;
import static com.tonnfccard.smartcard.ErrorCodes.SW_INCORRECT_SAULT_MSG;
import static com.tonnfccard.smartcard.ErrorCodes.SW_SUCCESS;
import static com.tonnfccard.smartcard.ErrorCodes.SW_SUCCESS_MSG;
import static com.tonnfccard.smartcard.ErrorCodes.getMsg;
import static org.junit.Assert.*;
import org.junit.Test;

public class ErrorCodesTest {
    private final ByteArrayUtil BYTE_ARRAY_HELPER = ByteArrayUtil.getInstance();

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
            RAPDU rapdu = new RAPDU(BYTE_ARRAY_HELPER.hex(SW_SUCCESS));
            String msg = getMsg(rapdu);
            assertEquals(SW_SUCCESS_MSG, msg);

            rapdu = new RAPDU(BYTE_ARRAY_HELPER.hex(SW_INCORRECT_SAULT));
            msg = getMsg(rapdu);
            assertEquals(SW_INCORRECT_SAULT_MSG, msg);

            msg = getMsg(BYTE_ARRAY_HELPER.hex(SW_DATA_INTEGRITY_CORRUPTED));
            assertEquals(SW_DATA_INTEGRITY_CORRUPTED_MSG , msg);

            msg = getMsg(BYTE_ARRAY_HELPER.hex(SW_APPLET_SELECT_FAILED ));
            assertEquals(SW_APPLET_SELECT_FAILED_MSG, msg);
        }
        catch (Exception e) {
            fail();
        }
    }

}