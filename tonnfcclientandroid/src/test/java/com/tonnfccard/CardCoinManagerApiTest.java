package com.tonnfccard;

import android.content.Context;
import android.nfc.NfcAdapter;
import android.nfc.tech.IsoDep;
import android.os.Build;

import androidx.test.core.app.ApplicationProvider;

import com.tonnfccard.callback.NfcCallback;
import com.tonnfccard.callback.NfcRejecter;
import com.tonnfccard.callback.NfcResolver;
import com.tonnfccard.helpers.ExceptionHelper;
import com.tonnfccard.helpers.JsonHelper;
import com.tonnfccard.helpers.ResponsesConstants;
import com.tonnfccard.helpers.StringHelper;
import com.tonnfccard.nfc.NfcApduRunner;
import com.tonnfccard.smartcard.CAPDU;
import com.tonnfccard.smartcard.ErrorCodes;
import com.tonnfccard.smartcard.RAPDU;
import com.tonnfccard.smartcard.wrappers.CAPDUTest;
import com.tonnfccard.utils.ByteArrayUtil;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.Result;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.internal.DoNotInstrument;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;

import static com.tonnfccard.TonWalletConstants.DEFAULT_PIN;
import static com.tonnfccard.TonWalletConstants.DEFAULT_PIN_STR;
import static com.tonnfccard.TonWalletConstants.DONE_MSG;
import static com.tonnfccard.TonWalletConstants.GENERATED_MSG;
import static com.tonnfccard.TonWalletConstants.MAX_PIN_TRIES;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_BAD_RESPONSE;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_APDU_EMPTY;
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
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_NFC_CONNECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_NFC_DISABLED;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_NO_NFC;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_NO_TAG;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_PIN_FORMAT_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_PIN_LEN_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_TRANSCEIVE;
import static com.tonnfccard.nfc.NfcApduRunner.TIME_OUT;
import static com.tonnfccard.smartcard.CoinManagerApduCommands.GET_CSN_APDU;
import static com.tonnfccard.smartcard.CoinManagerApduCommands.GET_ROOT_KEY_STATUS_APDU;
import static com.tonnfccard.smartcard.CoinManagerApduCommands.LABEL_LENGTH;
import static com.tonnfccard.smartcard.CoinManagerApduCommands.SELECT_COIN_MANAGER_APDU;
import static com.tonnfccard.smartcard.CoinManagerApduCommands.getSetDeviceLabelAPDU;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.SELECT_TON_WALLET_APPLET_APDU;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = Build.VERSION_CODES.P)
@DoNotInstrument
public class CardCoinManagerApiTest extends TonWalletApiTest {

    private CardCoinManagerApi cardCoinManagerApi;
    private final ExceptionHelper EXCEPTION_HELPER = ExceptionHelper.getInstance();
    private final StringHelper STRING_HELPER = StringHelper.getInstance();
    private final  ByteArrayUtil BYTE_ARRAY_HELPER = ByteArrayUtil.getInstance();
    private final JsonHelper JSON_HELPER = JsonHelper.getInstance();
    private Random random = new Random();
    private NfcApduRunner nfcApduRunner;
    private Context context;
    private final RAPDU SUCCESS_RAPDU = new RAPDU(BYTE_ARRAY_HELPER.hex(ErrorCodes.SW_SUCCESS));

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

    List<CardApiInterface<List<String>>> cardOperationsList = Arrays.asList(setDeviceLabel, generateSeed, changePin, getDeviceLabel,
            getAppsList, getAvailableMemory, getCsn, getMaxPinTries, getRootKeyStatus, getSeVersion, getRemainingPinTries, resetWallet);

    @Before
    public  void init() throws Exception {
        context = ApplicationProvider.getApplicationContext();
        nfcApduRunner = NfcApduRunner.getInstance(context);
        cardCoinManagerApi = new CardCoinManagerApi(context, nfcApduRunner);
    }

    /** Common tests for NFC errors**/

    private void prepareNfcTest(String errMsg) {
        for(int i = 0 ; i < cardOperationsList.size(); i++) {
            List<String> args = i == 0 ? Collections.singletonList(STRING_HELPER.randomHexString(2 * LABEL_LENGTH))
                    : i == 1 ? Collections.singletonList(DEFAULT_PIN_STR)
                    : i == 2 ? Arrays.asList(DEFAULT_PIN_STR, DEFAULT_PIN_STR)
                    : Collections.emptyList();
            CardApiInterface<List<String>> op = cardOperationsList.get(i);

            try {
                op.accept(args);
                fail();
            }
            catch (Exception e){
                assertEquals(e.getMessage(), EXCEPTION_HELPER.makeFinalErrMsg(new Exception(errMsg)));
            }
        }
    }

    @Test
    public void testNoNfcTag() throws Exception{
        IsoDep isoDep = null;
        nfcApduRunner.setCardTag(isoDep);
        cardCoinManagerApi.setApduRunner(nfcApduRunner);
        prepareNfcTest(ERROR_MSG_NO_TAG);
    }

    @Test
    public void testNoNfc() {
        mockNfcAdapterToBeNull(nfcApduRunner);
        IsoDep isoDep = mock(IsoDep.class);
        nfcApduRunner.setCardTag(isoDep);
        cardCoinManagerApi.setApduRunner(nfcApduRunner);
        prepareNfcTest(ERROR_MSG_NO_NFC);
    }

    @Test
    public void testNfcDisabled() {
        mockNfcAdapter(nfcApduRunner, false);
        IsoDep isoDep = mock(IsoDep.class);
        nfcApduRunner.setCardTag(isoDep);
        cardCoinManagerApi.setApduRunner(nfcApduRunner);
        prepareNfcTest(ERROR_MSG_NFC_DISABLED);
    }

    @Test
    public void testNfcConnectFail() throws Exception{
        mockNfcAdapter(nfcApduRunner, true);
        IsoDep isoDep = mock(IsoDep.class);
        Mockito.doThrow(new IOException()).when(isoDep).connect();
        nfcApduRunner.setCardTag(isoDep);
        cardCoinManagerApi.setApduRunner(nfcApduRunner);
        prepareNfcTest(ERROR_MSG_NFC_CONNECT);
    }

    @Test
    public void testNfcTransceiveFail() throws Exception{
        mockNfcAdapter(nfcApduRunner, true);
        IsoDep tag = prepareTagMock();
        Mockito.doThrow(new IOException()).when(tag).transceive(any());
        nfcApduRunner.setCardTag(tag);
        cardCoinManagerApi.setApduRunner(nfcApduRunner);
        prepareNfcTest(ERROR_TRANSCEIVE + ", More details: null");
    }

    @Test
    public void testNfcTooShortResponse() throws Exception{
        mockNfcAdapter(nfcApduRunner, true);
        for (int i = 0; i < 3 ; i++) {
            IsoDep tag = prepareTagMock();
            if (i <  2) {
                when(tag.transceive(any())).thenReturn(new byte[i]);
            }
            else {
                when(tag.transceive(any())).thenReturn(null);
            }
            nfcApduRunner.setCardTag(tag);
            cardCoinManagerApi.setApduRunner(nfcApduRunner);
            prepareNfcTest(ERROR_BAD_RESPONSE);
        }
    }

    /** Test for successfull response from applet **/

    @Test
    public void testAppletSuccessfullOperations() throws Exception {
        NfcApduRunner nfcApduRunnerMock = prepareNfcApduRunnerMock(nfcApduRunner);
        Mockito.doReturn(SUCCESS_RAPDU).when(nfcApduRunnerMock).sendAPDU(SELECT_COIN_MANAGER_APDU);
        Map<CardApiInterface<List<String>>, String> map = new LinkedHashMap<>();
        map.put(setDeviceLabel, BYTE_ARRAY_HELPER.hex(ErrorCodes.SW_SUCCESS));
        map.put(generateSeed, BYTE_ARRAY_HELPER.hex(ErrorCodes.SW_SUCCESS));
        map.put(changePin, BYTE_ARRAY_HELPER.hex(ErrorCodes.SW_SUCCESS));
        map.put(getDeviceLabel, STRING_HELPER.randomHexString(2 * LABEL_LENGTH) + BYTE_ARRAY_HELPER.hex(ErrorCodes.SW_SUCCESS));
        map.put(getAppsList, STRING_HELPER.randomHexString(60) + BYTE_ARRAY_HELPER.hex(ErrorCodes.SW_SUCCESS));
        map.put(getAvailableMemory, STRING_HELPER.randomHexString(10) + BYTE_ARRAY_HELPER.hex(ErrorCodes.SW_SUCCESS));
        map.put(getCsn, STRING_HELPER.randomHexString(20) + BYTE_ARRAY_HELPER.hex(ErrorCodes.SW_SUCCESS));
        map.put(getMaxPinTries, "0A" + BYTE_ARRAY_HELPER.hex(ErrorCodes.SW_SUCCESS));
        map.put(getRootKeyStatus, "5A" + BYTE_ARRAY_HELPER.hex(ErrorCodes.SW_SUCCESS));
        map.put(getSeVersion, "1008" + BYTE_ARRAY_HELPER.hex(ErrorCodes.SW_SUCCESS));
        map.put(getRemainingPinTries,"09" + BYTE_ARRAY_HELPER.hex(ErrorCodes.SW_SUCCESS));
        map.put(resetWallet, BYTE_ARRAY_HELPER.hex(ErrorCodes.SW_SUCCESS));
        for(int i = 0 ; i < cardOperationsList.size(); i++) {
            List<String> args = i == 0 ? Collections.singletonList(STRING_HELPER.randomHexString(2 * LABEL_LENGTH))
                    : i == 1 ? Collections.singletonList(DEFAULT_PIN_STR)
                    : i == 2 ? Arrays.asList(DEFAULT_PIN_STR, DEFAULT_PIN_STR)
                    : Collections.emptyList();
            CardApiInterface<List<String>> op = cardOperationsList.get(i);
            IsoDep tag = prepareTagMock();
            when(tag.transceive(any())).thenReturn(BYTE_ARRAY_HELPER.bytes(map.get(op)));
            nfcApduRunnerMock.setCardTag(tag);
            cardCoinManagerApi.setApduRunner(nfcApduRunnerMock);
            String response = op.accept(args);
            String res = map.get(op).substring(0, map.get(op).length() - 4);
            if (op == getMaxPinTries || op == getRemainingPinTries) {
                res = Byte.toString(BYTE_ARRAY_HELPER.bytes(res)[0]);
            }
            else if (op == getRootKeyStatus) {
                res = GENERATED_MSG;
            }
            String msg = map.get(op).equals(BYTE_ARRAY_HELPER.hex(ErrorCodes.SW_SUCCESS)) ? DONE_MSG : res;
            assertEquals(response.toLowerCase(), JSON_HELPER.createResponseJson(msg).toLowerCase());
        }
    }

    /** Test for applet fail with some SW**/

    @Test
    public void testAppletFailedOperation() throws Exception{
        RAPDU rapdu = new RAPDU(BYTE_ARRAY_HELPER.hex(ErrorCodes.SW_INS_NOT_SUPPORTED));
        NfcApduRunner nfcApduRunnerMock = prepareNfcApduRunnerMock(nfcApduRunner);
        Mockito.doReturn(SUCCESS_RAPDU).when(nfcApduRunnerMock).sendAPDU(SELECT_COIN_MANAGER_APDU);
        IsoDep tag = prepareTagMock();
        when(tag.transceive(any())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_INS_NOT_SUPPORTED));
        nfcApduRunnerMock.setCardTag(tag);
        cardCoinManagerApi.setApduRunner(nfcApduRunnerMock);
        for(int i = 0 ; i < cardOperationsList.size(); i++) {
            List<String> args = i == 0 ? Collections.singletonList(STRING_HELPER.randomHexString(2 * LABEL_LENGTH))
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
        List<String> incorrectLabels = Arrays.asList(null, "ABC", "12345", "ssAA", "1234k7");
        incorrectLabels.forEach(label -> {
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

    /** Tests for incorrect card responses **/

    @Test
    public void getPinTriesTestWrongValResponse() throws Exception {
        NfcApduRunner nfcApduRunnerMock = prepareNfcApduRunnerMock(nfcApduRunner);
        Mockito.doReturn(SUCCESS_RAPDU).when(nfcApduRunnerMock).sendAPDU(SELECT_COIN_MANAGER_APDU);
        List<CardApiInterface<List<String>>> ops = Arrays.asList(getMaxPinTries, getRemainingPinTries);
        List<Byte> wrongTriesNum = Arrays.asList((byte)-120, (byte)-1, (byte)(MAX_PIN_TRIES+1), (byte)100);
        for(CardApiInterface<List<String>> op : ops) {
            for (Byte n : wrongTriesNum) {
                IsoDep tag = prepareTagMock();
                when(tag.transceive(any())).thenReturn(BYTE_ARRAY_HELPER.bConcat(new byte[]{n}, BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS)));
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
        NfcApduRunner nfcApduRunnerMock = Mockito.spy(nfcApduRunner);
        Mockito.doReturn(null).when(nfcApduRunnerMock).sendCoinManagerAppletAPDU(any());
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
    }

    @Test
    public void getDeviceLabelCheckWrongResponseLength() throws Exception {
        NfcApduRunner nfcApduRunnerMock = Mockito.spy(nfcApduRunner);
        List<Integer> badLens = Arrays.asList(LABEL_LENGTH + 1, LABEL_LENGTH + 3);
        badLens.forEach(len -> {
            System.out.println(len);
            try {
                Mockito.doReturn(new RAPDU(new byte[len])).when(nfcApduRunnerMock).sendCoinManagerAppletAPDU(any());
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