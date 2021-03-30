package com.tonnfccard;

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
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.internal.DoNotInstrument;

import java.io.IOException;
import java.security.KeyStore;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static com.tonnfccard.NfcMockHelper.mockAndroidKeyStore;
import static com.tonnfccard.NfcMockHelper.prepareHmacHelperMock;
import static com.tonnfccard.NfcMockHelper.prepareNfcApduRunnerMock;
import static com.tonnfccard.NfcMockHelper.prepareTagMock;
import static com.tonnfccard.TonWalletApi.BYTE_ARR_HELPER;
import static com.tonnfccard.TonWalletApi.STR_HELPER;
import static com.tonnfccard.TonWalletConstants.DATA_FOR_SIGNING_MAX_SIZE;
import static com.tonnfccard.TonWalletConstants.DATA_FOR_SIGNING_MAX_SIZE_FOR_CASE_WITH_PATH;
import static com.tonnfccard.TonWalletConstants.DEFAULT_PIN;
import static com.tonnfccard.TonWalletConstants.DEFAULT_PIN_STR;
import static com.tonnfccard.TonWalletConstants.DONE_MSG;
import static com.tonnfccard.TonWalletConstants.PASSWORD_SIZE;
import static com.tonnfccard.TonWalletConstants.PERSONALIZED_STATE;
import static com.tonnfccard.TonWalletConstants.PERSONALIZED_STATE_MSG;
import static com.tonnfccard.TonWalletConstants.PUBLIC_KEY_LEN;
import static com.tonnfccard.TonWalletConstants.SAULT_LENGTH;
import static com.tonnfccard.TonWalletConstants.SHA_HASH_SIZE;
import static com.tonnfccard.TonWalletConstants.SIG_LEN;
import static com.tonnfccard.TonWalletConstants.WAITE_AUTHORIZATION_STATE;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_BAD_RESPONSE;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_DATA_FOR_SIGNING_LEN_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_DATA_FOR_SIGNING_NOT_HEX;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_DATA_FOR_SIGNING_WITH_PATH_LEN_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_HD_INDEX_FORMAT_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_HD_INDEX_LEN_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_NFC_CONNECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_NFC_DISABLED;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_NO_NFC;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_NO_TAG;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_PASSWORD_LEN_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_PASSWORD_NOT_HEX;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_PIN_FORMAT_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_PIN_LEN_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_PUBLIC_KEY_RESPONSE_LEN_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_SIG_RESPONSE_LEN_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_TRANSCEIVE;
import static com.tonnfccard.smartcard.CoinManagerApduCommands.LABEL_LENGTH;
import static com.tonnfccard.smartcard.CoinManagerApduCommands.RESET_WALLET_APDU;
import static com.tonnfccard.smartcard.CoinManagerApduCommands.SELECT_COIN_MANAGER_APDU;
import static com.tonnfccard.smartcard.CoinManagerApduCommands.getChangePinAPDU;
import static com.tonnfccard.smartcard.CoinManagerApduCommands.getGenerateSeedAPDU;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.*;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.SELECT_TON_WALLET_APPLET_APDU;
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
    private Random random = new Random();
    private NfcApduRunner nfcApduRunner;
    private Context context;
    private final RAPDU SUCCESS_RAPDU = new RAPDU(BYTE_ARRAY_HELPER.hex(ErrorCodes.SW_SUCCESS));

    private final CardApiInterface<List<String>> sign = list -> cardCryptoApi.signAndGetJson(list.get(0), list.get(1));
    private final CardApiInterface<List<String>> verifyPinAndSign = list -> cardCryptoApi.verifyPinAndSignAndGetJson(list.get(0), list.get(1), list.get(2));
    private final CardApiInterface<List<String>> signForDefaultHdPath = list -> cardCryptoApi.signForDefaultHdPathAndGetJson(list.get(0));
    private final CardApiInterface<List<String>> verifyPinAndSignForDefaultHdPath = list -> cardCryptoApi.verifyPinAndSignForDefaultHdPathAndGetJson(list.get(0), list.get(1));
    private final CardApiInterface<List<String>> verifyPin = list -> cardCryptoApi.verifyPinAndGetJson(list.get(0));
    private final CardApiInterface<List<String>> getPublicKey = list -> cardCryptoApi.getPublicKeyAndGetJson(list.get(0));
    private final CardApiInterface<List<String>> getPublicKeyForDefaultPath = list -> cardCryptoApi.getPublicKeyForDefaultPathAndGetJson();

    List<CardApiInterface<List<String>>> cardOperationsList = Arrays.asList(sign, verifyPinAndSign, signForDefaultHdPath, verifyPinAndSignForDefaultHdPath,
           verifyPin, getPublicKey, getPublicKeyForDefaultPath);

    private static final String SN = "050004030904080002040303090001010206080103020306";

    @Before
    public  void init() throws Exception {
        context = ApplicationProvider.getApplicationContext();
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
        IsoDep tag =  prepareTagMock();
        when(tag.transceive(SELECT_TON_WALLET_APPLET_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS));
        when(tag.transceive(GET_PUB_KEY_WITH_DEFAULT_PATH_APDU.getBytes()))
                .thenReturn(BYTE_ARRAY_HELPER.bConcat(pk, BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS)));
        when(tag.transceive(GET_APP_INFO_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bConcat(new byte[]{PERSONALIZED_STATE}, BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS)));;
        when(tag.transceive(getPublicKeyAPDU(BYTE_ARR_HELPER.bytes(STR_HELPER.asciiToHex(hdInd))).getBytes()))
                .thenReturn(BYTE_ARRAY_HELPER.bConcat(pk, BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS)));
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
        byte[] sault = new byte[SAULT_LENGTH];
        random.nextBytes(sault);
        NfcApduRunner nfcApduRunnerMock = prepareNfcApduRunnerMock(nfcApduRunner);
        TonWalletAppletApduCommands.setHmacHelper(prepareHmacHelperMock(HMAC_HELPER));
        IsoDep tag =  prepareTagMock();
        when(tag.transceive(SELECT_TON_WALLET_APPLET_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS));
        when(tag.transceive(GET_SERIAL_NUMBER_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bConcat(BYTE_ARRAY_HELPER.bytes(SN), BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS)));
        when(tag.transceive(GET_SAULT_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bConcat(sault, BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS)));
        when(tag.transceive(GET_APP_INFO_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bConcat(new byte[]{PERSONALIZED_STATE}, BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS)));;
        when(tag.transceive(getVerifyPinAPDU(DEFAULT_PIN, sault).getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS));
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
        byte[] sault = new byte[SAULT_LENGTH];
        random.nextBytes(sault);
        String hdInd = "123";
        String data = "1234567800";
        RAPDU rapdu = new RAPDU(BYTE_ARRAY_HELPER.hex(ErrorCodes.SW_WRONG_LENGTH));
        TonWalletAppletApduCommands.setHmacHelper(prepareHmacHelperMock(HMAC_HELPER));
        NfcApduRunner nfcApduRunnerMock = prepareNfcApduRunnerMock(nfcApduRunner);
        IsoDep tag =  prepareTagMock();
        when(tag.transceive(SELECT_TON_WALLET_APPLET_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS));
        when(tag.transceive(GET_SERIAL_NUMBER_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bConcat(BYTE_ARRAY_HELPER.bytes(SN), BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS)));
        when(tag.transceive(GET_SAULT_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bConcat(sault, BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS)));
        when(tag.transceive(GET_APP_INFO_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bConcat(new byte[]{PERSONALIZED_STATE}, BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS)));;
        when(tag.transceive(getVerifyPinAPDU(DEFAULT_PIN, sault).getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS));
        when(tag.transceive(getSignShortMessageWithDefaultPathAPDU(BYTE_ARRAY_HELPER.bytes(data), sault).getBytes()))
                .thenReturn(BYTE_ARRAY_HELPER.bConcat(sig, BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS)));;
        when(tag.transceive(getSignShortMessageAPDU(BYTE_ARRAY_HELPER.bytes(data), BYTE_ARR_HELPER.bytes(STR_HELPER.asciiToHex(hdInd)), sault).getBytes()))
                .thenReturn(BYTE_ARRAY_HELPER.bConcat(sig, BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS)));
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
        List<RAPDU> badRapdus = Arrays.asList(null, new RAPDU(STRING_HELPER.randomHexString(2 * PUBLIC_KEY_LEN + 2) + "9000"),
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
        byte[] sault = new byte[SAULT_LENGTH];
        random.nextBytes(sault);
        String data = "1234567800";
        String hdInd = "1";
        TonWalletAppletApduCommands.setHmacHelper(prepareHmacHelperMock(HMAC_HELPER));
        NfcApduRunner nfcApduRunnerMock = prepareNfcApduRunnerMock(nfcApduRunner);
        IsoDep tag =  prepareTagMock();
        when(tag.transceive(SELECT_TON_WALLET_APPLET_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS));
        when(tag.transceive(GET_SERIAL_NUMBER_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bConcat(BYTE_ARRAY_HELPER.bytes(SN), BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS)));
        when(tag.transceive(GET_SAULT_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bConcat(sault, BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS)));
        when(tag.transceive(GET_APP_INFO_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bConcat(new byte[]{PERSONALIZED_STATE}, BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS)));;
        when(tag.transceive(getVerifyPinAPDU(DEFAULT_PIN, sault).getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS));
        nfcApduRunnerMock.setCardTag(tag);
        cardCryptoApi.setApduRunner(nfcApduRunnerMock);
        mockAndroidKeyStore();
        List<CardApiInterface<List<String>>> cardOperationsListShort = Arrays.asList(signForDefaultHdPath, verifyPinAndSignForDefaultHdPath, sign, verifyPinAndSign);
        List<RAPDU> badRapdus = Arrays.asList(null, new RAPDU(STRING_HELPER.randomHexString(2 * SIG_LEN + 2) + "9000"),
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
    public void testGetPublicKeyAppletFailedOperation() throws Exception {
        String hdInd = "1";
        RAPDU rapdu = new RAPDU(BYTE_ARRAY_HELPER.hex(ErrorCodes.SW_WRONG_DATA));
        TonWalletAppletApduCommands.setHmacHelper(prepareHmacHelperMock(HMAC_HELPER));
        NfcApduRunner nfcApduRunnerMock = prepareNfcApduRunnerMock(nfcApduRunner);
        IsoDep tag =  prepareTagMock();
        when(tag.transceive(SELECT_TON_WALLET_APPLET_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS));
        when(tag.transceive(GET_PUB_KEY_WITH_DEFAULT_PATH_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_WRONG_DATA));
        when(tag.transceive(GET_APP_INFO_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bConcat(new byte[]{PERSONALIZED_STATE}, BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS)));;
        when(tag.transceive(getPublicKeyAPDU(BYTE_ARR_HELPER.bytes(STR_HELPER.asciiToHex(hdInd))).getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_WRONG_DATA));
        nfcApduRunnerMock.setCardTag(tag);
        cardCryptoApi.setApduRunner(nfcApduRunnerMock);
        List<CardApiInterface<List<String>>> cardOperationsListShort = Arrays.asList(getPublicKeyForDefaultPath, getPublicKey);
        for(int i = 0 ; i < cardOperationsListShort.size(); i++) {
            try {
                cardOperationsListShort.get(i).accept(i == 0 ? Collections.emptyList() : Collections.singletonList(hdInd));
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
    public void testVerifyPinAppletFailedOperation() throws Exception {
        byte[] sault = new byte[SAULT_LENGTH];
        random.nextBytes(sault);
        String hdInd = "1";
        String data = "123456";
        RAPDU rapdu = new RAPDU(BYTE_ARRAY_HELPER.hex(ErrorCodes.SW_INCORRECT_PIN));
        TonWalletAppletApduCommands.setHmacHelper(prepareHmacHelperMock(HMAC_HELPER));
        NfcApduRunner nfcApduRunnerMock = prepareNfcApduRunnerMock(nfcApduRunner);
        IsoDep tag =  prepareTagMock();
        when(tag.transceive(SELECT_TON_WALLET_APPLET_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS));
        when(tag.transceive(GET_SERIAL_NUMBER_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bConcat(BYTE_ARRAY_HELPER.bytes(SN), BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS)));
        when(tag.transceive(GET_SAULT_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bConcat(sault, BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS)));
        when(tag.transceive(GET_APP_INFO_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bConcat(new byte[]{PERSONALIZED_STATE}, BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS)));;
        when(tag.transceive(getVerifyPinAPDU(DEFAULT_PIN, sault).getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_INCORRECT_PIN));
        nfcApduRunnerMock.setCardTag(tag);
        cardCryptoApi.setApduRunner(nfcApduRunnerMock);
        mockAndroidKeyStore();
        List<CardApiInterface<List<String>>> cardOperationsListShort = Arrays.asList(verifyPin, verifyPinAndSignForDefaultHdPath, verifyPinAndSign);
        for(int i = 0 ; i < cardOperationsListShort.size(); i++) {
            try {
                cardOperationsListShort.get(i).accept(i == 0 ? Collections.singletonList(DEFAULT_PIN_STR) :
                        i == 1 ? Arrays.asList(data, DEFAULT_PIN_STR) : Arrays.asList(data, hdInd, DEFAULT_PIN_STR));
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
    public void testSignForDefaultHdPathAppletFailedOperation() throws Exception {
        byte[] sault = new byte[SAULT_LENGTH];
        random.nextBytes(sault);
        String data = "1234567800";
        RAPDU rapdu = new RAPDU(BYTE_ARRAY_HELPER.hex(ErrorCodes.SW_WRONG_LENGTH));
        TonWalletAppletApduCommands.setHmacHelper(prepareHmacHelperMock(HMAC_HELPER));
        NfcApduRunner nfcApduRunnerMock = prepareNfcApduRunnerMock(nfcApduRunner);
        IsoDep tag =  prepareTagMock();
        when(tag.transceive(SELECT_TON_WALLET_APPLET_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS));
        when(tag.transceive(GET_SERIAL_NUMBER_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bConcat(BYTE_ARRAY_HELPER.bytes(SN), BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS)));
        when(tag.transceive(GET_SAULT_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bConcat(sault, BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS)));
        when(tag.transceive(GET_APP_INFO_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bConcat(new byte[]{PERSONALIZED_STATE}, BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS)));;
        when(tag.transceive(getVerifyPinAPDU(DEFAULT_PIN, sault).getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS));
        when(tag.transceive(getSignShortMessageWithDefaultPathAPDU(BYTE_ARRAY_HELPER.bytes(data), sault).getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_WRONG_LENGTH));
        nfcApduRunnerMock.setCardTag(tag);
        cardCryptoApi.setApduRunner(nfcApduRunnerMock);
        mockAndroidKeyStore();
        List<CardApiInterface<List<String>>> cardOperationsListShort = Arrays.asList(signForDefaultHdPath, verifyPinAndSignForDefaultHdPath);
        for(int i = 0 ; i < cardOperationsListShort.size(); i++) {
            System.out.println(i);
            try {
                cardOperationsListShort.get(i).accept(i == 0 ? Collections.singletonList(data) : Arrays.asList(data, DEFAULT_PIN_STR));
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
    public void testSignAppletFailedOperation() throws Exception {
        byte[] sault = new byte[SAULT_LENGTH];
        random.nextBytes(sault);
        String hdInd = "1";
        String data = "1234567800";
        RAPDU rapdu = new RAPDU(BYTE_ARRAY_HELPER.hex(ErrorCodes.SW_WRONG_LENGTH));
        TonWalletAppletApduCommands.setHmacHelper(prepareHmacHelperMock(HMAC_HELPER));
        NfcApduRunner nfcApduRunnerMock = prepareNfcApduRunnerMock(nfcApduRunner);
        IsoDep tag =  prepareTagMock();
        when(tag.transceive(SELECT_TON_WALLET_APPLET_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS));
        when(tag.transceive(GET_SERIAL_NUMBER_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bConcat(BYTE_ARRAY_HELPER.bytes(SN), BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS)));
        when(tag.transceive(GET_SAULT_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bConcat(sault, BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS)));
        when(tag.transceive(GET_APP_INFO_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bConcat(new byte[]{PERSONALIZED_STATE}, BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS)));;
        when(tag.transceive(getVerifyPinAPDU(DEFAULT_PIN, sault).getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS));
        when(tag.transceive(getSignShortMessageAPDU(BYTE_ARRAY_HELPER.bytes(data), BYTE_ARR_HELPER.bytes(STR_HELPER.asciiToHex(hdInd)), sault).getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_WRONG_LENGTH));
        nfcApduRunnerMock.setCardTag(tag);
        cardCryptoApi.setApduRunner(nfcApduRunnerMock);
        mockAndroidKeyStore();
        List<CardApiInterface<List<String>>> cardOperationsListShort = Arrays.asList(sign, verifyPinAndSign);
        for(int i = 0 ; i < cardOperationsListShort.size(); i++) {
            System.out.println(i);
            try {
                cardOperationsListShort.get(i).accept(i == 0 ? Arrays.asList(data, hdInd) : Arrays.asList(data, hdInd, DEFAULT_PIN_STR));
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
                    cardOperationsListToCheckBadData.get(i).accept(getArgForDataTest(data, i));
                    fail();
                }
                catch (Exception e){
                    assertEquals(e.getMessage(), EXCEPTION_HELPER.makeFinalErrMsg(new Exception(badDataToErrMsg.get(data))));
                }
            }
        });
    }

    private List<String> getArgForDataTest(String data, int i) {
        return  i == 0 ? Arrays.asList(data)
                : Arrays.asList(data,  DEFAULT_PIN_STR);
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
                    cardOperationsListToCheckBadDataWithoutHd.get(i).accept(getArgForDataWithHdTest(data, i));
                    fail();
                }
                catch (Exception e){
                    assertEquals(e.getMessage(), EXCEPTION_HELPER.makeFinalErrMsg(new Exception(badDataToErrMsg.get(data))));
                }
            }
        });
    }

    private List<String> getArgForDataWithHdTest(String data, int i) {
        return  i == 0 ? Arrays.asList(data, "1")
                : Arrays.asList(data, "1", DEFAULT_PIN_STR);
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
                    cardOperationsListToCheckHdIndex.get(i).accept(getArgForHdIndexTest(hdIndex, i));
                    fail();
                }
                catch (Exception e){
                    assertEquals(e.getMessage(), EXCEPTION_HELPER.makeFinalErrMsg(new Exception(badHdIndexToErrMsg.get(hdIndex))));
                }
            }
        });
    }

    private List<String> getArgForHdIndexTest(String hdIndex, int i) {
        return  i == 0 ? Arrays.asList(STRING_HELPER.randomHexString(2 * DATA_FOR_SIGNING_MAX_SIZE_FOR_CASE_WITH_PATH), hdIndex)
                : i == 1 ? Arrays.asList(STRING_HELPER.randomHexString(2 * DATA_FOR_SIGNING_MAX_SIZE_FOR_CASE_WITH_PATH), hdIndex, DEFAULT_PIN_STR)
                : Collections.singletonList(hdIndex);
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
        badPinToErrMsg.keySet().forEach(hdIndex -> {
            for(int i = 0 ; i < cardOperationsListToCheckPin .size(); i++) {
                try {
                    cardOperationsListToCheckPin.get(i).accept(getArgForPinTest(hdIndex, i));
                    fail();
                }
                catch (Exception e){
                    assertEquals(e.getMessage(), EXCEPTION_HELPER.makeFinalErrMsg(new Exception(badPinToErrMsg.get(hdIndex))));
                }
            }
        });
    }

    private List<String> getArgForPinTest(String pin, int i) {
        return  i == 0 ? Arrays.asList(STRING_HELPER.randomHexString(2 * DATA_FOR_SIGNING_MAX_SIZE_FOR_CASE_WITH_PATH), "1", pin)
                : i == 1 ? Arrays.asList(STRING_HELPER.randomHexString(2 * DATA_FOR_SIGNING_MAX_SIZE_FOR_CASE_WITH_PATH), pin)
                : Collections.singletonList(pin);
    }
}