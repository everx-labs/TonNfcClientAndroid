package com.tonnfccard;

import com.tonnfccard.helpers.CardApiInterface;
import android.content.Context;
import android.nfc.tech.IsoDep;
import android.os.Build;

import androidx.test.core.app.ApplicationProvider;

import com.tonnfccard.helpers.ExceptionHelper;
import com.tonnfccard.helpers.JsonHelper;
import com.tonnfccard.helpers.StringHelper;
import com.tonnfccard.nfc.NfcApduRunner;
import com.tonnfccard.smartcard.ErrorCodes;
import com.tonnfccard.smartcard.RAPDU;
import com.tonnfccard.utils.ByteArrayUtil;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.internal.DoNotInstrument;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static com.tonnfccard.NfcMockHelper.SW_SUCCESS;
import static com.tonnfccard.NfcMockHelper.prepareNfcApduRunnerMock;
import static com.tonnfccard.NfcMockHelper.prepareTagMock;
import static com.tonnfccard.TonWalletConstants.DEFAULT_PIN;
import static com.tonnfccard.TonWalletConstants.DEFAULT_PIN_STR;
import static com.tonnfccard.TonWalletConstants.DONE_MSG;
import static com.tonnfccard.TonWalletConstants.GENERATED_MSG;
import static com.tonnfccard.TonWalletConstants.MAX_PIN_TRIES;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_DEVICE_LABEL_LEN_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_DEVICE_LABEL_NOT_HEX;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_GET_APPLET_LIST_RESPONSE_LEN_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_GET_AVAILABLE_MEMORY_RESPONSE_LEN_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_GET_CSN_RESPONSE_LEN_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_GET_DEVICE_LABEL_RESPONSE_LEN_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_GET_PIN_TLT_OR_RTL_RESPONSE_LEN_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_GET_PIN_TLT_OR_RTL_RESPONSE_VAL_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_GET_ROOT_KEY_STATUS_RESPONSE_LEN_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_GET_SE_VERSION_RESPONSE_LEN_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_PIN_FORMAT_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_PIN_LEN_INCORRECT;
import static com.tonnfccard.smartcard.CoinManagerApduCommands.GET_APPLET_LIST_APDU;
import static com.tonnfccard.smartcard.CoinManagerApduCommands.GET_AVAILABLE_MEMORY_APDU;
import static com.tonnfccard.smartcard.CoinManagerApduCommands.GET_CSN_APDU;
import static com.tonnfccard.smartcard.CoinManagerApduCommands.GET_DEVICE_LABEL_APDU;
import static com.tonnfccard.smartcard.CoinManagerApduCommands.GET_PIN_RTL_APDU;
import static com.tonnfccard.smartcard.CoinManagerApduCommands.GET_PIN_TLT_APDU;
import static com.tonnfccard.smartcard.CoinManagerApduCommands.GET_ROOT_KEY_STATUS_APDU;
import static com.tonnfccard.smartcard.CoinManagerApduCommands.GET_SE_VERSION_APDU;
import static com.tonnfccard.smartcard.CoinManagerApduCommands.LABEL_LENGTH;
import static com.tonnfccard.smartcard.CoinManagerApduCommands.RESET_WALLET_APDU;
import static com.tonnfccard.smartcard.CoinManagerApduCommands.SELECT_COIN_MANAGER_APDU;
import static com.tonnfccard.smartcard.CoinManagerApduCommands.getChangePinAPDU;
import static com.tonnfccard.smartcard.CoinManagerApduCommands.getGenerateSeedAPDU;
import static com.tonnfccard.smartcard.CoinManagerApduCommands.getSetDeviceLabelAPDU;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = Build.VERSION_CODES.P)
@DoNotInstrument
public class CardCoinManagerApiTest {
    private static final ExceptionHelper EXCEPTION_HELPER = ExceptionHelper.getInstance();
    private static final StringHelper STRING_HELPER = StringHelper.getInstance();
    private static final ByteArrayUtil BYTE_ARRAY_HELPER = ByteArrayUtil.getInstance();
    private static final JsonHelper JSON_HELPER = JsonHelper.getInstance();
    private CardCoinManagerApi cardCoinManagerApi;
    private NfcApduRunner nfcApduRunner;
    private final Random random = new Random();

    private final CardApiInterface<List<String>> setDeviceLabel = list ->  cardCoinManagerApi.setDeviceLabelAndGetJson(list.get(0));
    private final CardApiInterface<List<String>> generateSeed = list ->  cardCoinManagerApi.generateSeedAndGetJson(list.get(0));
    private final CardApiInterface<List<String>> changePin = list ->  cardCoinManagerApi.changePinAndGetJson(list.get(0), list.get(1));
    private final CardApiInterface<List<String>> getDeviceLabel = list ->  cardCoinManagerApi.getDeviceLabelAndGetJson();
    private final CardApiInterface<List<String>> getAppsList = list ->  cardCoinManagerApi.getAppsListAndGetJson();
    private final CardApiInterface<List<String>> getAvailableMemory = list ->  cardCoinManagerApi.getAvailableMemoryAndGetJson();
    private final CardApiInterface<List<String>> getCsn = list ->  cardCoinManagerApi.getCsnAndGetJson();
    private final CardApiInterface<List<String>> getMaxPinTries = list ->  cardCoinManagerApi.getMaxPinTriesAndGetJson();
    private final CardApiInterface<List<String>> getRootKeyStatus = list ->  cardCoinManagerApi.getRootKeyStatusAndGetJson();
    private final CardApiInterface<List<String>> getSeVersion = list ->  cardCoinManagerApi.getSeVersionAndGetJson();
    private final CardApiInterface<List<String>> getRemainingPinTries = list ->  cardCoinManagerApi.getRemainingPinTriesAndGetJson();
    private final CardApiInterface<List<String>> resetWallet = list ->  cardCoinManagerApi.resetWalletAndGetJson();

    List<CardApiInterface<List<String>>> cardOperationsList = Arrays.asList(setDeviceLabel, generateSeed, changePin, resetWallet, getDeviceLabel,
            getAppsList, getAvailableMemory, getCsn, getMaxPinTries, getRemainingPinTries, getRootKeyStatus, getSeVersion);


    @Before
    public  void init() throws Exception {
        Context context = ApplicationProvider.getApplicationContext();
        nfcApduRunner = NfcApduRunner.getInstance(context);
        cardCoinManagerApi = new CardCoinManagerApi(context, nfcApduRunner);
    }

    /** Test for successfull response from applet **/

    @Test
    public void testAppletSuccessfullOperations() throws Exception {
        NfcApduRunner nfcApduRunnerMock = prepareNfcApduRunnerMock(nfcApduRunner);
        IsoDep tag = prepareTagMock();
        when(tag.transceive(SELECT_COIN_MANAGER_APDU.getBytes())).thenReturn(SW_SUCCESS);
        byte[] label = new byte[LABEL_LENGTH];
        random.nextBytes(label);
        when(tag.transceive(GET_DEVICE_LABEL_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bConcat(label, SW_SUCCESS));
        when(tag.transceive(getSetDeviceLabelAPDU(label).getBytes())).thenReturn(SW_SUCCESS);
        when(tag.transceive(getGenerateSeedAPDU(DEFAULT_PIN).getBytes())).thenReturn(SW_SUCCESS);
        when(tag.transceive(getChangePinAPDU(DEFAULT_PIN, DEFAULT_PIN).getBytes())).thenReturn(SW_SUCCESS);
        when(tag.transceive(RESET_WALLET_APDU.getBytes())).thenReturn(SW_SUCCESS);
        byte[] csn = new byte[20];
        random.nextBytes(csn);
        when(tag.transceive(GET_CSN_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bConcat(csn, SW_SUCCESS));
        byte[] pinTries = new byte[]{0x0A};
        when(tag.transceive(GET_PIN_RTL_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bConcat(pinTries, SW_SUCCESS));
        when(tag.transceive(GET_PIN_TLT_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bConcat(pinTries, SW_SUCCESS));
        byte[] rootKeyStatus = new byte[]{0x5A};
        when(tag.transceive(GET_ROOT_KEY_STATUS_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bConcat(rootKeyStatus, SW_SUCCESS));
        byte[] seVersion = new byte[]{0x10, 0x08};
        when(tag.transceive(GET_SE_VERSION_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bConcat(seVersion, SW_SUCCESS));
        byte[] appsList = new byte[60];
        random.nextBytes(appsList);
        when(tag.transceive(GET_APPLET_LIST_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bConcat(appsList, SW_SUCCESS));
        byte[] availableMemory = new byte[10];
        random.nextBytes(availableMemory);
        when(tag.transceive(GET_AVAILABLE_MEMORY_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bConcat(availableMemory, SW_SUCCESS));
        nfcApduRunnerMock.setCardTag(tag);
        cardCoinManagerApi.setApduRunner(nfcApduRunnerMock);
        for(int i = 0 ; i < cardOperationsList.size(); i++) {
            String msg = i <= 3 ? DONE_MSG :
                    i == 4 ? BYTE_ARRAY_HELPER.hex(label) :
                    i == 5 ? BYTE_ARRAY_HELPER.hex(appsList) :
                    i == 6 ? BYTE_ARRAY_HELPER.hex(availableMemory) :
                    i == 7 ? BYTE_ARRAY_HELPER.hex(csn) :
                    i == 8 || i == 9 ? Byte.toString(pinTries[0]) :
                    i == 10 ? GENERATED_MSG : BYTE_ARRAY_HELPER.hex(seVersion);
            String response = cardOperationsList.get(i).accept(i == 0 ? Collections.singletonList(BYTE_ARRAY_HELPER.hex(label))
                    : i == 1 ? Collections.singletonList(DEFAULT_PIN_STR)
                    : i == 2 ? Arrays.asList(DEFAULT_PIN_STR, DEFAULT_PIN_STR)
                    : Collections.emptyList());
            assertEquals(response.toLowerCase(), JSON_HELPER.createResponseJson(msg).toLowerCase());
        }
    }

    /** Test for applet fail with some SW**/

    @Test
    public void testAppletFailedOperation() throws Exception{
        RAPDU rapdu = new RAPDU(BYTE_ARRAY_HELPER.hex(ErrorCodes.SW_INS_NOT_SUPPORTED));
        NfcApduRunner nfcApduRunnerMock = prepareNfcApduRunnerMock(nfcApduRunner);
        IsoDep tag = prepareTagMock();
        when(tag.transceive(SELECT_COIN_MANAGER_APDU.getBytes())).thenReturn(SW_SUCCESS);
        when(tag.transceive(GET_AVAILABLE_MEMORY_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_INS_NOT_SUPPORTED));
        when(tag.transceive(GET_CSN_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_INS_NOT_SUPPORTED));
        when(tag.transceive(GET_SE_VERSION_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_INS_NOT_SUPPORTED));
        when(tag.transceive(GET_APPLET_LIST_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_INS_NOT_SUPPORTED));
        when(tag.transceive(GET_ROOT_KEY_STATUS_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_INS_NOT_SUPPORTED));
        when(tag.transceive(GET_PIN_RTL_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_INS_NOT_SUPPORTED));
        when(tag.transceive(GET_PIN_TLT_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_INS_NOT_SUPPORTED));
        when(tag.transceive(GET_DEVICE_LABEL_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_INS_NOT_SUPPORTED));
        when(tag.transceive(RESET_WALLET_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_INS_NOT_SUPPORTED));
        byte[] label = new byte[LABEL_LENGTH];
        random.nextBytes(label);
        when(tag.transceive(getSetDeviceLabelAPDU(label).getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_INS_NOT_SUPPORTED));
        when(tag.transceive(getGenerateSeedAPDU(DEFAULT_PIN).getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_INS_NOT_SUPPORTED));
        when(tag.transceive(getChangePinAPDU(DEFAULT_PIN, DEFAULT_PIN).getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_INS_NOT_SUPPORTED));
        nfcApduRunnerMock.setCardTag(tag);
        cardCoinManagerApi.setApduRunner(nfcApduRunnerMock);
        for(int i = 0 ; i < cardOperationsList.size(); i++) {
            List<String> args = i == 0 ? Collections.singletonList(BYTE_ARRAY_HELPER.hex(label))
                    : i == 1 ? Collections.singletonList(DEFAULT_PIN_STR)
                    : i == 2 ? Arrays.asList(DEFAULT_PIN_STR, DEFAULT_PIN_STR)
                    : Collections.emptyList();
            CardApiInterface<List<String>> op = cardOperationsList.get(i);
            try {
                op.accept(args);
                fail();
            }
            catch (Exception e){
                String errMsg = JSON_HELPER.createErrorJsonForCardException(rapdu.prepareSwFormatted(), nfcApduRunnerMock.getLastSentAPDU());
                System.out.println(errMsg);
                System.out.println(e.getMessage());
                assertEquals(e.getMessage(), errMsg);
            }
        }
    }

    /** Tests for incorrect input arguments **/


    /** setDeviceLabel **/
    @Test
    public void setDeviceLabelAndGetJsonTestBadInputDeviceLabel() {
        Arrays.asList(null, "", "ABC", "12345", "ssAA", "1234k7").forEach(label -> {
            try {
                cardCoinManagerApi.setDeviceLabelAndGetJson(label);
                fail();
            }
            catch (Exception e){
                assertEquals(e.getMessage(), EXCEPTION_HELPER.makeFinalErrMsg(new Exception(ERROR_MSG_DEVICE_LABEL_NOT_HEX)));
            }
        });

        for(int i = 0 ; i < 10 ; i++) {
            int len = random.nextInt(100);
            if (len == 2 * LABEL_LENGTH || len % 2 != 0 || len == 0) continue;
            String l = STRING_HELPER.randomHexString(len);
            try {
                cardCoinManagerApi.setDeviceLabelAndGetJson(l);
                fail();
            }
            catch (Exception e){
                assertEquals(e.getMessage(), EXCEPTION_HELPER.makeFinalErrMsg(new Exception(ERROR_MSG_DEVICE_LABEL_LEN_INCORRECT)));
            }
        }
    }

    /** generateSeed **/

    @Test
    public void generateSeedAndGetJsonTestBadInputDeviceLabel() {
        List<String> incorrectPins = Arrays.asList(null,  "", "012a", "98777f", "ssAA", "12n");
        checkGenerateSeedArgs(incorrectPins, ERROR_MSG_PIN_FORMAT_INCORRECT);
        incorrectPins = Arrays.asList("012", "98777", "000000677", "12", "9");
        checkGenerateSeedArgs(incorrectPins, ERROR_MSG_PIN_LEN_INCORRECT);
    }

    private void checkGenerateSeedArgs(List<String> argsToTest, String errMsg) {
        argsToTest.forEach(pin -> {
            try {
                cardCoinManagerApi.generateSeedAndGetJson(pin);
                fail();
            }
            catch (Exception e){
                assertEquals(e.getMessage(), EXCEPTION_HELPER.makeFinalErrMsg(new Exception(errMsg)));
            }
        });
    }

    /** changePin **/

    @Test
    public void changePinAndGetJsonTestBadInputDeviceLabel() {
        List<List<String>> incorrectPins = Arrays.asList(
                Arrays.asList(null, "5555"),
                Arrays.asList("", "5555"),
                Arrays.asList("012a", "5555"),
                Arrays.asList("98777f", "5555"),
                Arrays.asList("6666",  null),
                Arrays.asList("6666678",  ""),
                Arrays.asList("0099787",  "34a3"),
                Arrays.asList("0099787",  "33f565c")
        );
        checkChangePinArgs(incorrectPins, ERROR_MSG_PIN_FORMAT_INCORRECT);
        incorrectPins = Arrays.asList(
                Arrays.asList("1", "5555"),
                Arrays.asList("23", "5555"),
                Arrays.asList("012", "5555"),
                Arrays.asList("98777", "5555"),
                Arrays.asList("6666",  "4"),
                Arrays.asList("5678",  "35"),
                Arrays.asList("0099",  "343"),
                Arrays.asList("9787",  "4757654375")
        );
        checkChangePinArgs(incorrectPins, ERROR_MSG_PIN_LEN_INCORRECT);
    }

    private void checkChangePinArgs(List<List<String>> argsToTest, String errMsg) {
        argsToTest.forEach(pins -> {
            try {
                cardCoinManagerApi.changePinAndGetJson(pins.get(0), pins.get(1));
                fail();
            }
            catch (Exception e){
                assertEquals(e.getMessage(), EXCEPTION_HELPER.makeFinalErrMsg(new Exception(errMsg)));
            }
        });
    }

    /** Tests for incorrect card responses values **/

    @Test
    public void getPinTriesTestWrongValResponse() throws Exception {
        NfcApduRunner nfcApduRunnerMock = prepareNfcApduRunnerMock(nfcApduRunner);
        IsoDep tag = prepareTagMock();
        when(tag.transceive(SELECT_COIN_MANAGER_APDU.getBytes())).thenReturn(SW_SUCCESS);
        List<CardApiInterface<List<String>>> ops = Arrays.asList(getMaxPinTries, getRemainingPinTries);
        List<Byte> wrongTriesNum = Arrays.asList((byte) -120, (byte) -1, (byte)(MAX_PIN_TRIES + 1), (byte) 100);
        for(CardApiInterface<List<String>> op : ops) {
            for (Byte val : wrongTriesNum) {
                when(tag.transceive(any())).thenReturn(BYTE_ARRAY_HELPER.bConcat(new byte[]{val}, BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS)));
                nfcApduRunnerMock.setCardTag(tag);
                cardCoinManagerApi.setApduRunner(nfcApduRunnerMock);
                try {
                    op.accept(Collections.emptyList());
                    fail();
                }
                catch (Exception e){
                    assertEquals(e.getMessage(), EXCEPTION_HELPER.makeFinalErrMsg(new Exception(ERROR_MSG_GET_PIN_TLT_OR_RTL_RESPONSE_VAL_INCORRECT)));
                }
            }
        }
    }

    /** Invalid RAPDU object/responses from card tests **/

    @Test
    public void testInvalidRAPDU() throws Exception {
        Map<CardApiInterface<List<String>>, String> opsErrors = new LinkedHashMap<>();
        opsErrors.put(getMaxPinTries, ERROR_MSG_GET_PIN_TLT_OR_RTL_RESPONSE_LEN_INCORRECT);
        opsErrors.put(getRemainingPinTries, ERROR_MSG_GET_PIN_TLT_OR_RTL_RESPONSE_LEN_INCORRECT);
        opsErrors.put(getAppsList, ERROR_MSG_GET_APPLET_LIST_RESPONSE_LEN_INCORRECT);
        opsErrors.put(getAvailableMemory, ERROR_MSG_GET_AVAILABLE_MEMORY_RESPONSE_LEN_INCORRECT);
        opsErrors.put(getRootKeyStatus, ERROR_MSG_GET_ROOT_KEY_STATUS_RESPONSE_LEN_INCORRECT);
        opsErrors.put(getCsn, ERROR_MSG_GET_CSN_RESPONSE_LEN_INCORRECT);
        opsErrors.put(getSeVersion, ERROR_MSG_GET_SE_VERSION_RESPONSE_LEN_INCORRECT);
        opsErrors.put(getDeviceLabel, ERROR_MSG_GET_DEVICE_LABEL_RESPONSE_LEN_INCORRECT);
        NfcApduRunner nfcApduRunnerMock = Mockito.spy(nfcApduRunner);
        IsoDep tag = prepareTagMock();
        when(tag.transceive(SELECT_COIN_MANAGER_APDU.getBytes())).thenReturn(SW_SUCCESS);
        nfcApduRunnerMock.setCardTag(tag);
        Mockito.doReturn(null).when(nfcApduRunnerMock).sendAPDU(any());
        cardCoinManagerApi.setApduRunner(nfcApduRunnerMock);
        for(CardApiInterface<List<String>> op : opsErrors.keySet()) {
            try {
                op.accept(Collections.emptyList());
                fail();
            }
            catch (Exception e){
                assertEquals(e.getMessage(), EXCEPTION_HELPER.makeFinalErrMsg(new Exception(opsErrors.get(op))));
            }
        }
        Arrays.asList(LABEL_LENGTH + 1, LABEL_LENGTH + 3).forEach(len -> {
            try {
                Mockito.doReturn(new RAPDU(new byte[len])).when(nfcApduRunnerMock).sendAPDU(any());
                cardCoinManagerApi.setApduRunner(nfcApduRunnerMock);
                cardCoinManagerApi.getDeviceLabelAndGetJson();
                fail();
            }
            catch (Exception e){
                assertEquals(e.getMessage(), EXCEPTION_HELPER.makeFinalErrMsg(new Exception(ERROR_MSG_GET_DEVICE_LABEL_RESPONSE_LEN_INCORRECT)));
            }
        });
    }
}