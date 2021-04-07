package com.tonnfccard.smartcard;

import com.tonnfccard.TonWalletConstants;
import com.tonnfccard.utils.ByteArrayUtil;

import static com.tonnfccard.TonWalletConstants.*;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_APDU_DATA_FIELD_IS_NULL;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_LABEL_BYTES_SIZE_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_PIN_BYTES_SIZE_INCORRECT;
import static com.tonnfccard.smartcard.CoinManagerApduCommands.*;
import static com.tonnfccard.smartcard.CommonConstants.LE;
import static org.junit.Assert.*;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class CoinManagerApduCommandsTest {
    private final static ByteArrayUtil BYTE_ARRAY_HELPER = ByteArrayUtil.getInstance();
    private final List<byte[]> badPins = Arrays.asList(null, new byte[PIN_SIZE - 2], new byte[PIN_SIZE - 1], new byte[PIN_SIZE + 1], new byte[PIN_SIZE + 2]);
    
    @Test
    public void getApduCommandNameTestNullData() {
        try {
            getCoinManagerApduCommandName(null);
            fail();
        }
        catch (Exception e) {
            assertEquals(e.getMessage(), ERROR_MSG_APDU_DATA_FIELD_IS_NULL);
        }
    }

    @Test
    public void getApduCommandNameTest() {
        try {
            assertEquals(getCoinManagerApduCommandName(BYTE_ARRAY_HELPER.hex(GET_ROOT_KEY_STATUS_APDU.getData())), GET_ROOT_KEY_STATUS_APDU_NAME);
            assertEquals(getCoinManagerApduCommandName(BYTE_ARRAY_HELPER.hex(GET_PIN_RTL_APDU.getData())), GET_PIN_RTL_APDU_NAME);
            assertEquals(getCoinManagerApduCommandName(BYTE_ARRAY_HELPER.hex(GET_PIN_TLT_APDU.getData())), GET_PIN_TLT_APDU_NAME);
            assertEquals(getCoinManagerApduCommandName(BYTE_ARRAY_HELPER.hex(RESET_WALLET_APDU.getData())), RESET_WALLET_APDU_NAME);
            assertEquals(getCoinManagerApduCommandName(BYTE_ARRAY_HELPER.hex(GET_AVAILABLE_MEMORY_APDU.getData())), GET_AVAILABLE_MEMORY_APDU_NAME);
            assertEquals(getCoinManagerApduCommandName(BYTE_ARRAY_HELPER.hex(GET_APPLET_LIST_APDU.getData())), GET_APPLET_LIST_APDU_NAME);
            assertEquals(getCoinManagerApduCommandName(BYTE_ARRAY_HELPER.hex(GET_SE_VERSION_APDU.getData())), GET_SE_VERSION_APDU_NAME);
            assertEquals(getCoinManagerApduCommandName(BYTE_ARRAY_HELPER.hex(GET_CSN_APDU.getData())), GET_CSN_APDU_NAME);
            assertEquals(getCoinManagerApduCommandName(BYTE_ARRAY_HELPER.hex(GET_DEVICE_LABEL_APDU.getData())), GET_DEVICE_LABEL_APDU_NAME);
            assertEquals(getCoinManagerApduCommandName(BYTE_ARRAY_HELPER.hex(getChangePinAPDU(DEFAULT_PIN, DEFAULT_PIN).getData())), CHANGE_PIN_APDU_NAME);
            assertEquals(getCoinManagerApduCommandName(BYTE_ARRAY_HELPER.hex(getGenerateSeedAPDU(DEFAULT_PIN).getData())), GENERATE_SEED_APDU_NAME);
            assertEquals(getCoinManagerApduCommandName(BYTE_ARRAY_HELPER.hex(getSetDeviceLabelAPDU(new byte[LABEL_LENGTH]).getData())), SET_DEVICE_LABEL_APDU_NAME);
            assertEquals(getCoinManagerApduCommandName("a234"), null);

        }
        catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testGetChangePinAPDU() {
        try {
            CAPDU capdu = CoinManagerApduCommands.getChangePinAPDU(DEFAULT_PIN, DEFAULT_PIN);
            assertEquals(capdu.getCla(), COIN_MANAGER_CLA);
            assertEquals(capdu.getIns(), COIN_MANAGER_INS);
            assertEquals(capdu.getP1(), COIN_MANAGER_P1);
            assertEquals(capdu.getP2(), COIN_MANAGER_P2);
            byte[] data = BYTE_ARRAY_HELPER.bConcat(BYTE_ARRAY_HELPER.bytes(CHANGE_PIN_DATA), new byte[]{PIN_SIZE}, DEFAULT_PIN, new byte[]{PIN_SIZE}, DEFAULT_PIN);
            assertArrayEquals(capdu.getData(), data);
            assertEquals(capdu.getLe(), LE);
        }
        catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testGetChangePinAPDUIncorrectPinFormat() {
        badPins.forEach(badPin -> {
            try {
                CoinManagerApduCommands.getChangePinAPDU(badPin, DEFAULT_PIN);
                fail();
            }
            catch (Exception e) {
                assertEquals(e.getMessage(), ERROR_MSG_PIN_BYTES_SIZE_INCORRECT);
            }
        });
        badPins.forEach(badPin -> {
            try {
                CoinManagerApduCommands.getChangePinAPDU(DEFAULT_PIN, badPin);
                fail();
            }
            catch (Exception e) {
                assertEquals(e.getMessage(), ERROR_MSG_PIN_BYTES_SIZE_INCORRECT);
            }
        });
    }

    @Test
    public void testGetGenerateSeedAPDU() {
        try {
            CAPDU capdu = CoinManagerApduCommands.getGenerateSeedAPDU(DEFAULT_PIN);
            assertEquals(capdu.getCla(), COIN_MANAGER_CLA);
            assertEquals(capdu.getIns(), COIN_MANAGER_INS);
            assertEquals(capdu.getP1(), COIN_MANAGER_P1);
            assertEquals(capdu.getP2(), COIN_MANAGER_P2);
            byte[] data = BYTE_ARRAY_HELPER.bConcat(BYTE_ARRAY_HELPER.bytes(GENERATE_SEED_DATA), new byte[]{PIN_SIZE}, DEFAULT_PIN);
            assertArrayEquals(capdu.getData(), data);
            assertEquals(capdu.getLe(), LE);
        }
        catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testGetGenerateSeedIncorrectPinFormat() {
        badPins.forEach(badPin -> {
            try {
                CAPDU capdu = CoinManagerApduCommands.getGenerateSeedAPDU(badPin);
                fail();
            }
            catch (Exception e) {
                assertEquals(e.getMessage(), ERROR_MSG_PIN_BYTES_SIZE_INCORRECT);
            }
        });
    }

    @Test
    public void testGetSetDeviceLabelAPDU() {
        try {
            CAPDU capdu = CoinManagerApduCommands.getSetDeviceLabelAPDU(new byte[LABEL_LENGTH]);
            assertEquals(capdu.getCla(), COIN_MANAGER_CLA);
            assertEquals(capdu.getIns(), COIN_MANAGER_INS);
            assertEquals(capdu.getP1(), COIN_MANAGER_P1);
            assertEquals(capdu.getP2(), COIN_MANAGER_P2);
            byte[] data =  BYTE_ARRAY_HELPER.bConcat(BYTE_ARRAY_HELPER.bytes(SET_DEVICE_LABEL_DATA), new byte[]{0x20}, new byte[LABEL_LENGTH]);
            assertArrayEquals(capdu.getData(), data);
            assertEquals(capdu.getLe(), LE);
        }
        catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testGetSetDeviceLabelIncorrectLabelFormat() {
        List<byte[]> badLabels = Arrays.asList(null, new byte[LABEL_LENGTH - 2], new byte[LABEL_LENGTH - 1], new byte[LABEL_LENGTH + 1], new byte[LABEL_LENGTH + 2]);
        badLabels.forEach(badLabel -> {
            try {
                CAPDU capdu = CoinManagerApduCommands.getSetDeviceLabelAPDU(badLabel);
                fail();
            } catch (Exception e) {
                assertEquals(e.getMessage(), ERROR_MSG_LABEL_BYTES_SIZE_INCORRECT);
            }
        });
    }
}