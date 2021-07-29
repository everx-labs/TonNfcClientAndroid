package com.tonnfccard;

import com.tonnfccard.helpers.CardApiInterface;
import android.content.Context;
import android.nfc.tech.IsoDep;
import android.os.Build;

import androidx.test.core.app.ApplicationProvider;

import com.tonnfccard.helpers.ExceptionHelper;
import com.tonnfccard.helpers.HmacHelper;
import com.tonnfccard.helpers.JsonHelper;
import com.tonnfccard.helpers.StringHelper;
import com.tonnfccard.nfc.NfcApduRunner;
import com.tonnfccard.smartcard.CAPDU;
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
import static com.tonnfccard.NfcMockHelper.createSault;
import static com.tonnfccard.NfcMockHelper.mockAndroidKeyStore;
import static com.tonnfccard.NfcMockHelper.prepareAdvancedTagMock;
import static com.tonnfccard.NfcMockHelper.prepareHmacHelperMock;
import static com.tonnfccard.NfcMockHelper.prepareNfcApduRunnerMock;
import static com.tonnfccard.NfcMockHelper.prepareSimpleTagMock;
import static com.tonnfccard.TonWalletApi.BYTE_ARR_HELPER;
import static com.tonnfccard.TonWalletApi.STR_HELPER;
import static com.tonnfccard.TonWalletConstants.DATA_FOR_SIGNING_MAX_SIZE;
import static com.tonnfccard.TonWalletConstants.DATA_FOR_SIGNING_MAX_SIZE_FOR_CASE_WITH_PATH;
import static com.tonnfccard.TonWalletConstants.DEFAULT_PIN;
import static com.tonnfccard.TonWalletConstants.DEFAULT_PIN_STR;
import static com.tonnfccard.TonWalletConstants.DONE_MSG;
import static com.tonnfccard.TonWalletConstants.PERSONALIZED_STATE;
import static com.tonnfccard.TonWalletConstants.PUBLIC_KEY_LEN;
import static com.tonnfccard.TonWalletConstants.SIG_LEN;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_DATA_FOR_SIGNING_LEN_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_DATA_FOR_SIGNING_NOT_HEX;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_DATA_FOR_SIGNING_WITH_PATH_LEN_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_HD_INDEX_FORMAT_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_HD_INDEX_LEN_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_PIN_FORMAT_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_PIN_LEN_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_PUBLIC_KEY_RESPONSE_LEN_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_SIG_RESPONSE_LEN_INCORRECT;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.*;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import  com.tonnfccard.smartcard.TonWalletAppletApduCommands;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = Build.VERSION_CODES.P)
@DoNotInstrument
public class CardCryptoApiTest  {
    private static final ExceptionHelper EXCEPTION_HELPER = ExceptionHelper.getInstance();
    private static final StringHelper STRING_HELPER = StringHelper.getInstance();
    private static final ByteArrayUtil BYTE_ARRAY_HELPER = ByteArrayUtil.getInstance();
    private static final JsonHelper JSON_HELPER = JsonHelper.getInstance();
    private static final HmacHelper HMAC_HELPER = HmacHelper.getInstance();
    private CardCryptoApi cardCryptoApi;
    private final Random random = new Random();
    private NfcApduRunner nfcApduRunner;
    private final CardApiInterface<List<String>> sign = list -> cardCryptoApi.signAndGetJson(list.get(0), list.get(1));
    private final CardApiInterface<List<String>> verifyPinAndSign = list -> cardCryptoApi.verifyPinAndSignAndGetJson(list.get(0), list.get(1), list.get(2));
    private final CardApiInterface<List<String>> signForDefaultHdPath = list -> cardCryptoApi.signForDefaultHdPathAndGetJson(list.get(0));
    private final CardApiInterface<List<String>> verifyPinAndSignForDefaultHdPath = list -> cardCryptoApi.verifyPinAndSignForDefaultHdPathAndGetJson(list.get(0), list.get(1));
    private final CardApiInterface<List<String>> verifyPin = list -> cardCryptoApi.verifyPinAndGetJson(list.get(0));
    private final CardApiInterface<List<String>> getPublicKey = list -> cardCryptoApi.getPublicKeyAndGetJson(list.get(0));
    private final CardApiInterface<List<String>> getPublicKeyForDefaultPath = list -> cardCryptoApi.getPublicKeyForDefaultPathAndGetJson();

    @Before
    public  void init() throws Exception {
        Context context = ApplicationProvider.getApplicationContext();
        nfcApduRunner = NfcApduRunner.getInstance(context);
        cardCryptoApi = new CardCryptoApi(context, nfcApduRunner);
    }

    /** Test for successfull response from applet **/

    @Test
    public void testGetPublicKeyAppletSuccessfullOperations() throws Exception {
        byte[] pk = new byte[PUBLIC_KEY_LEN];
        random.nextBytes(pk);
        String hdInd = "111";
        NfcApduRunner nfcApduRunnerMock = prepareNfcApduRunnerMock(nfcApduRunner);;
        IsoDep tag =  prepareSimpleTagMock(PERSONALIZED_STATE);
        when(tag.transceive(GET_PUB_KEY_WITH_DEFAULT_PATH_APDU.getBytes()))
                .thenReturn(BYTE_ARRAY_HELPER.bConcat(pk, SW_SUCCESS));
        when(tag.transceive(getPublicKeyAPDU(BYTE_ARR_HELPER.bytes(STR_HELPER.asciiToHex(hdInd))).getBytes()))
                .thenReturn(BYTE_ARRAY_HELPER.bConcat(pk, SW_SUCCESS));
        nfcApduRunnerMock.setCardTag(tag);
        cardCryptoApi.setApduRunner(nfcApduRunnerMock);
        List<CardApiInterface<List<String>>> cardOperationsListShort = Arrays.asList(getPublicKeyForDefaultPath, getPublicKey);
        for(int i = 0 ; i < cardOperationsListShort.size(); i++) {
            try {
                String response = cardOperationsListShort.get(i).accept(i == 0 ? Collections.emptyList() : Collections.singletonList(hdInd));
                System.out.println(response);
                assertEquals(response.toLowerCase(), JSON_HELPER.createResponseJson(BYTE_ARRAY_HELPER.hex(pk)).toLowerCase());
            }
            catch (Exception e){
                System.out.println(e.getMessage());
                fail();
            }
        }
    }

    @Test
    public void testVerifyPinAppletSuccessfullOperation() throws Exception {
        byte[] sault = createSault();
        NfcApduRunner nfcApduRunnerMock = prepareNfcApduRunnerMock(nfcApduRunner);
        TonWalletAppletApduCommands.setHmacHelper(prepareHmacHelperMock(HMAC_HELPER));
        IsoDep tag =  prepareAdvancedTagMock(sault, PERSONALIZED_STATE);
        when(tag.transceive(getVerifyPinAPDU(DEFAULT_PIN, sault).getBytes())).thenReturn(SW_SUCCESS);
        nfcApduRunnerMock.setCardTag(tag);
        cardCryptoApi.setApduRunner(nfcApduRunnerMock);
        mockAndroidKeyStore();
        try {
            String response = cardCryptoApi.verifyPinAndGetJson(DEFAULT_PIN_STR);
            System.out.println(response);
            assertEquals(response.toLowerCase(), JSON_HELPER.createResponseJson(DONE_MSG).toLowerCase());
        }
        catch (Exception e){
            System.out.println(e.getMessage());
            fail();
        }
    }

    @Test
    public void testSignAppletSuccessfullOperations() throws Exception {
        byte[] sig = new byte[SIG_LEN];
        random.nextBytes(sig);
        byte[] sault = createSault();
        String hdInd = "123";
        String data = "1234567800";
        TonWalletAppletApduCommands.setHmacHelper(prepareHmacHelperMock(HMAC_HELPER));
        NfcApduRunner nfcApduRunnerMock = prepareNfcApduRunnerMock(nfcApduRunner);
        IsoDep tag =  prepareAdvancedTagMock(sault, PERSONALIZED_STATE);
        when(tag.transceive(getVerifyPinAPDU(DEFAULT_PIN, sault).getBytes())).thenReturn(SW_SUCCESS);
        when(tag.transceive(getSignShortMessageWithDefaultPathAPDU(BYTE_ARRAY_HELPER.bytes(data), sault).getBytes()))
                .thenReturn(BYTE_ARRAY_HELPER.bConcat(sig, SW_SUCCESS));
        when(tag.transceive(getSignShortMessageAPDU(BYTE_ARRAY_HELPER.bytes(data), BYTE_ARR_HELPER.bytes(STR_HELPER.asciiToHex(hdInd)), sault).getBytes()))
                .thenReturn(BYTE_ARRAY_HELPER.bConcat(sig, SW_SUCCESS));
        nfcApduRunnerMock.setCardTag(tag);
        cardCryptoApi.setApduRunner(nfcApduRunnerMock);
        mockAndroidKeyStore();
        List<CardApiInterface<List<String>>> cardOperationsListShort = Arrays.asList(sign, verifyPinAndSign, signForDefaultHdPath, verifyPinAndSignForDefaultHdPath);
        for(int i = 0 ; i < cardOperationsListShort.size(); i++) {
            try {
                String response = cardOperationsListShort.get(i).accept(i == 0 ? Arrays.asList(data, hdInd) :
                        i == 1 ? Arrays.asList(data, hdInd, DEFAULT_PIN_STR) :
                        i == 2 ? Collections.singletonList(data) :
                                Arrays.asList(data, DEFAULT_PIN_STR));
                System.out.println(response);
                assertEquals(response.toLowerCase(), JSON_HELPER.createResponseJson(BYTE_ARRAY_HELPER.hex(sig)).toLowerCase());
            }
            catch (Exception e){
                System.out.println(e.getMessage());
                fail();
            }
        }
    }

    /** Tests for incorrect card responses **/

    /** Invalid RAPDU object/responses from card tests **/


    @Test
    public void testGetPublicKeyInvalidRAPDUAndInvalidResponseLength() throws Exception {
        String hdInd = "1";
        NfcApduRunner nfcApduRunnerMock = prepareNfcApduRunnerMock(nfcApduRunner);
        List<CardApiInterface<List<String>>> cardOperationsListShort = Arrays.asList(getPublicKeyForDefaultPath, getPublicKey);
        List<RAPDU> badRapdus = Arrays.asList(null, new RAPDU("9000"),  new RAPDU(STRING_HELPER.randomHexString(2 * PUBLIC_KEY_LEN + 2) + "9000"),
                new RAPDU(STRING_HELPER.randomHexString(2 * PUBLIC_KEY_LEN - 2) + "9000"));
        badRapdus.forEach(rapdu -> {
            try {
                Mockito.doReturn(rapdu).when(nfcApduRunnerMock).sendTonWalletAppletAPDU(GET_PUB_KEY_WITH_DEFAULT_PATH_APDU);
                Mockito.doReturn(rapdu).when(nfcApduRunnerMock).sendTonWalletAppletAPDU(getPublicKeyAPDU(BYTE_ARR_HELPER.bytes(STR_HELPER.asciiToHex(hdInd))));
                cardCryptoApi.setApduRunner(nfcApduRunnerMock);
                for(int i = 0 ; i < cardOperationsListShort.size(); i++) {
                    try {
                        cardOperationsListShort.get(i).accept(i == 0 ? Collections.emptyList() : Collections.singletonList(hdInd));
                        fail();
                    }
                    catch (Exception e){
                        System.out.println(e.getMessage());
                        assertEquals(e.getMessage(), EXCEPTION_HELPER.makeFinalErrMsg(new Exception(ERROR_MSG_PUBLIC_KEY_RESPONSE_LEN_INCORRECT)));
                    }
                }
            }
            catch (Exception e) {
                fail();
            }
        });
    }

    @Test
    public void testSignInvalidRAPDUAndInvalidResponseLength() throws Exception {
        byte[] sault = createSault();
        String data = "1234567800";
        String hdInd = "1";
        TonWalletAppletApduCommands.setHmacHelper(prepareHmacHelperMock(HMAC_HELPER));
        NfcApduRunner nfcApduRunnerMock = prepareNfcApduRunnerMock(nfcApduRunner);
        IsoDep tag =  prepareAdvancedTagMock(sault, PERSONALIZED_STATE);
        when(tag.transceive(getVerifyPinAPDU(DEFAULT_PIN, sault).getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS));
        nfcApduRunnerMock.setCardTag(tag);
        cardCryptoApi.setApduRunner(nfcApduRunnerMock);
        mockAndroidKeyStore();
        List<CardApiInterface<List<String>>> cardOperationsListShort = Arrays.asList(signForDefaultHdPath, verifyPinAndSignForDefaultHdPath, sign, verifyPinAndSign);
        List<RAPDU> badRapdus = Arrays.asList(null, new RAPDU("9000"), new RAPDU(STRING_HELPER.randomHexString(2 * SIG_LEN + 2) + "9000"),
                new RAPDU(STRING_HELPER.randomHexString(2 * SIG_LEN - 2) + "9000"));
        CAPDU capdu1 = getSignShortMessageAPDU(BYTE_ARRAY_HELPER.bytes(data), BYTE_ARR_HELPER.bytes(STR_HELPER.asciiToHex(hdInd)), sault);
        CAPDU capdu2 = getSignShortMessageWithDefaultPathAPDU(BYTE_ARRAY_HELPER.bytes(data), sault);
        badRapdus.forEach(rapdu -> {
            try {
                Mockito.doReturn(rapdu).when(nfcApduRunnerMock).sendAPDU(capdu1);
                Mockito.doReturn(rapdu).when(nfcApduRunnerMock).sendAPDU(capdu2);
                cardCryptoApi.setApduRunner(nfcApduRunnerMock);
                for(int i = 0 ; i < cardOperationsListShort.size(); i++) {
                    try {
                        cardOperationsListShort.get(i).accept(i == 0 ? Collections.singletonList(data) :
                                i == 1 ? Arrays.asList(data, DEFAULT_PIN_STR) :
                                i == 2 ? Arrays.asList(data, hdInd) :
                                        Arrays.asList(data, hdInd, DEFAULT_PIN_STR));
                        fail();
                    }
                    catch (Exception e){
                        System.out.println(e.getMessage());
                        assertEquals(e.getMessage(), EXCEPTION_HELPER.makeFinalErrMsg(new Exception(ERROR_MSG_SIG_RESPONSE_LEN_INCORRECT)));
                    }
                }
            }
            catch (Exception e) {
                e.printStackTrace();
                fail();
            }
        });
    }


    /** Test for applet fail with some SW**/

    @Test
    public void testAppletFailedOperations() throws Exception {
        byte[] sault = createSault();
        String hdInd = "1";
        String data = "123456";
        TonWalletAppletApduCommands.setHmacHelper(prepareHmacHelperMock(HMAC_HELPER));
        RAPDU rapdu = new RAPDU(BYTE_ARRAY_HELPER.hex(ErrorCodes.SW_WRONG_DATA));
        TonWalletAppletApduCommands.setHmacHelper(prepareHmacHelperMock(HMAC_HELPER));
        NfcApduRunner nfcApduRunnerMock = prepareNfcApduRunnerMock(nfcApduRunner);
        IsoDep tag = prepareAdvancedTagMock(sault, PERSONALIZED_STATE);
        when(tag.transceive(GET_PUB_KEY_WITH_DEFAULT_PATH_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_WRONG_DATA));
        when(tag.transceive(getPublicKeyAPDU(BYTE_ARR_HELPER.bytes(STR_HELPER.asciiToHex(hdInd))).getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_WRONG_DATA));
        when(tag.transceive(getVerifyPinAPDU(DEFAULT_PIN, sault).getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_WRONG_DATA));
        nfcApduRunnerMock.setCardTag(tag);
        cardCryptoApi.setApduRunner(nfcApduRunnerMock);
        mockAndroidKeyStore();
        List<CardApiInterface<List<String>>> cardOperationsListShort = Arrays.asList(getPublicKeyForDefaultPath, getPublicKey, verifyPin, verifyPinAndSignForDefaultHdPath, verifyPinAndSign);
        for(int i = 0 ; i < cardOperationsListShort.size(); i++) {
            try {
                cardOperationsListShort.get(i).accept(i == 0 ? Collections.emptyList() :
                        i == 1 ? Collections.singletonList(hdInd) :
                        i == 2 ? Collections.singletonList(DEFAULT_PIN_STR) :
                        i == 3 ? Arrays.asList(data, DEFAULT_PIN_STR) : Arrays.asList(data, hdInd, DEFAULT_PIN_STR));
                fail();
            }
            catch (Exception e){
                System.out.println(e.getMessage());
                String errMsg = JSON_HELPER.createErrorJsonForCardException(rapdu.prepareSwFormatted(), nfcApduRunnerMock.getLastSentAPDU());
                assertEquals(e.getMessage(), errMsg);
            }
        }
    }


    @Test
    public void testAppletFailedOperation2() throws Exception {
        byte[] sault = createSault();
        String hdInd = "1";
        String data = "1234567800";
        RAPDU rapdu = new RAPDU(BYTE_ARRAY_HELPER.hex(ErrorCodes.SW_WRONG_LENGTH));
        TonWalletAppletApduCommands.setHmacHelper(prepareHmacHelperMock(HMAC_HELPER));
        NfcApduRunner nfcApduRunnerMock = prepareNfcApduRunnerMock(nfcApduRunner);
        IsoDep tag = prepareAdvancedTagMock(sault, PERSONALIZED_STATE);
        when(tag.transceive(getVerifyPinAPDU(DEFAULT_PIN, sault).getBytes())).thenReturn(SW_SUCCESS);
        when(tag.transceive(getSignShortMessageWithDefaultPathAPDU(BYTE_ARRAY_HELPER.bytes(data), sault).getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_WRONG_LENGTH));
        when(tag.transceive(getSignShortMessageAPDU(BYTE_ARRAY_HELPER.bytes(data), BYTE_ARR_HELPER.bytes(STR_HELPER.asciiToHex(hdInd)), sault).getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_WRONG_LENGTH));
        nfcApduRunnerMock.setCardTag(tag);
        cardCryptoApi.setApduRunner(nfcApduRunnerMock);
        mockAndroidKeyStore();
        List<CardApiInterface<List<String>>> cardOperationsListShort = Arrays.asList(signForDefaultHdPath, verifyPinAndSignForDefaultHdPath, sign, verifyPinAndSign);
        for(int i = 0 ; i < cardOperationsListShort.size(); i++) {
            System.out.println(i);
            try {
                cardOperationsListShort.get(i).accept(i == 0 ? Collections.singletonList(data) :
                        i == 1 ? Arrays.asList(data, DEFAULT_PIN_STR) :
                        i == 2 ? Arrays.asList(data, hdInd) : Arrays.asList(data, hdInd, DEFAULT_PIN_STR));
                fail();
            }
            catch (Exception e){
                System.out.println(e.getMessage());
                String errMsg = JSON_HELPER.createErrorJsonForCardException(rapdu.prepareSwFormatted(), nfcApduRunnerMock.getLastSentAPDU());
                assertEquals(e.getMessage(), errMsg);
            }
        }
    }

    /** Tests for incorrect input arguments **/

    @Test
    public void testBadData() {
        List<CardApiInterface<List<String>>> cardOperationsListToCheckBadData = Arrays.asList(signForDefaultHdPath, verifyPinAndSignForDefaultHdPath);
        Map<String, String> badDataToErrMsg  = new LinkedHashMap<String, String>() {{
            put(null, ERROR_MSG_DATA_FOR_SIGNING_NOT_HEX);
            put("", ERROR_MSG_DATA_FOR_SIGNING_NOT_HEX);
            put("ABC", ERROR_MSG_DATA_FOR_SIGNING_NOT_HEX);
            put("98777ff", ERROR_MSG_DATA_FOR_SIGNING_NOT_HEX);
            put("ssAA", ERROR_MSG_DATA_FOR_SIGNING_NOT_HEX);
            put("12n", ERROR_MSG_DATA_FOR_SIGNING_NOT_HEX);
            put("1234k7", ERROR_MSG_DATA_FOR_SIGNING_NOT_HEX);
            put(STRING_HELPER.randomHexString(2 * DATA_FOR_SIGNING_MAX_SIZE + 2), ERROR_MSG_DATA_FOR_SIGNING_LEN_INCORRECT);
            put(STRING_HELPER.randomHexString(2 * DATA_FOR_SIGNING_MAX_SIZE + 100), ERROR_MSG_DATA_FOR_SIGNING_LEN_INCORRECT);
        }};
        badDataToErrMsg.keySet().forEach(data -> {
            for(int i = 0 ; i < cardOperationsListToCheckBadData.size(); i++) {
                try {
                    cardOperationsListToCheckBadData.get(i).accept(i == 0 ? Collections.singletonList(data)
                            : Arrays.asList(data,  DEFAULT_PIN_STR));
                    fail();
                }
                catch (Exception e){
                    assertEquals(e.getMessage(), EXCEPTION_HELPER.makeFinalErrMsg(new Exception(badDataToErrMsg.get(data))));
                }
            }
        });
    }

    @Test
    public void testBadDataWithHd() {
        List<CardApiInterface<List<String>>> cardOperationsListToCheckBadDataWithoutHd = Arrays.asList(sign, verifyPinAndSign);
        Map<String, String> badDataToErrMsg  = new LinkedHashMap<String, String>() {{
            put(null, ERROR_MSG_DATA_FOR_SIGNING_NOT_HEX);
            put("", ERROR_MSG_DATA_FOR_SIGNING_NOT_HEX);
            put("ABC", ERROR_MSG_DATA_FOR_SIGNING_NOT_HEX);
            put("98777ff", ERROR_MSG_DATA_FOR_SIGNING_NOT_HEX);
            put("ssAA", ERROR_MSG_DATA_FOR_SIGNING_NOT_HEX);
            put("12n", ERROR_MSG_DATA_FOR_SIGNING_NOT_HEX);
            put("1234k7", ERROR_MSG_DATA_FOR_SIGNING_NOT_HEX);
            put(STRING_HELPER.randomHexString(2 * DATA_FOR_SIGNING_MAX_SIZE_FOR_CASE_WITH_PATH + 2), ERROR_MSG_DATA_FOR_SIGNING_WITH_PATH_LEN_INCORRECT);
            put(STRING_HELPER.randomHexString(2 * DATA_FOR_SIGNING_MAX_SIZE_FOR_CASE_WITH_PATH + 100), ERROR_MSG_DATA_FOR_SIGNING_WITH_PATH_LEN_INCORRECT);
        }};
        badDataToErrMsg.keySet().forEach(data -> {
            for(int i = 0 ; i < cardOperationsListToCheckBadDataWithoutHd.size(); i++) {
                try {
                    cardOperationsListToCheckBadDataWithoutHd.get(i).accept(i == 0 ? Arrays.asList(data, "1")
                            : Arrays.asList(data, "1", DEFAULT_PIN_STR));
                    fail();
                }
                catch (Exception e){
                    assertEquals(e.getMessage(), EXCEPTION_HELPER.makeFinalErrMsg(new Exception(badDataToErrMsg.get(data))));
                }
            }
        });
    }

    @Test
    public void testBadHdIndexArg() {
        List<CardApiInterface<List<String>>> cardOperationsListToCheckHdIndex = Arrays.asList(sign, verifyPinAndSign, getPublicKey);
        Map<String, String> badHdIndexToErrMsg  = new LinkedHashMap<String, String>() {{
            put(null, ERROR_MSG_HD_INDEX_FORMAT_INCORRECT);
            put("", ERROR_MSG_HD_INDEX_FORMAT_INCORRECT);
            put("012a", ERROR_MSG_HD_INDEX_FORMAT_INCORRECT);
            put("98777f", ERROR_MSG_HD_INDEX_FORMAT_INCORRECT);
            put("ssAA", ERROR_MSG_HD_INDEX_FORMAT_INCORRECT);
            put("12n", ERROR_MSG_HD_INDEX_FORMAT_INCORRECT);
            put("12345678901", ERROR_MSG_HD_INDEX_LEN_INCORRECT);
            put("233333344441", ERROR_MSG_HD_INDEX_LEN_INCORRECT);
        }};
        badHdIndexToErrMsg.keySet().forEach(hdIndex -> {
            for(int i = 0 ; i < cardOperationsListToCheckHdIndex.size(); i++) {
                try {
                    cardOperationsListToCheckHdIndex.get(i).accept(i == 0 ? Arrays.asList(STRING_HELPER.randomHexString(2 * DATA_FOR_SIGNING_MAX_SIZE_FOR_CASE_WITH_PATH), hdIndex)
                            : i == 1 ? Arrays.asList(STRING_HELPER.randomHexString(2 * DATA_FOR_SIGNING_MAX_SIZE_FOR_CASE_WITH_PATH), hdIndex, DEFAULT_PIN_STR)
                            : Collections.singletonList(hdIndex));
                    fail();
                }
                catch (Exception e){
                    assertEquals(e.getMessage(), EXCEPTION_HELPER.makeFinalErrMsg(new Exception(badHdIndexToErrMsg.get(hdIndex))));
                }
            }
        });
    }

    @Test
    public void testBadPinArg() {
        List<CardApiInterface<List<String>>> cardOperationsListToCheckPin = Arrays.asList(verifyPinAndSign, verifyPinAndSignForDefaultHdPath, verifyPin);
        Map<String, String> badPinToErrMsg  = new LinkedHashMap<String, String>() {{
            put(null, ERROR_MSG_PIN_FORMAT_INCORRECT);
            put("", ERROR_MSG_PIN_FORMAT_INCORRECT);
            put("012a", ERROR_MSG_PIN_FORMAT_INCORRECT);
            put("98777f", ERROR_MSG_PIN_FORMAT_INCORRECT);
            put("ssAA", ERROR_MSG_PIN_FORMAT_INCORRECT);
            put("12n", ERROR_MSG_PIN_FORMAT_INCORRECT);
            put("123", ERROR_MSG_PIN_LEN_INCORRECT);
            put("56733", ERROR_MSG_PIN_LEN_INCORRECT);
            put("233333344441", ERROR_MSG_PIN_LEN_INCORRECT);
        }};
        badPinToErrMsg.keySet().forEach(pin -> {
            for(int i = 0 ; i < cardOperationsListToCheckPin .size(); i++) {
                try {
                    cardOperationsListToCheckPin.get(i).accept(i == 0 ? Arrays.asList(STRING_HELPER.randomHexString(2 * DATA_FOR_SIGNING_MAX_SIZE_FOR_CASE_WITH_PATH), "1", pin)
                            : i == 1 ? Arrays.asList(STRING_HELPER.randomHexString(2 * DATA_FOR_SIGNING_MAX_SIZE_FOR_CASE_WITH_PATH), pin)
                            : Collections.singletonList(pin));
                    fail();
                }
                catch (Exception e){
                    assertEquals(e.getMessage(), EXCEPTION_HELPER.makeFinalErrMsg(new Exception(badPinToErrMsg.get(pin))));
                }
            }
        });
    }
}