package com.tonnfccard;

import android.content.Context;
import android.nfc.NfcAdapter;
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
import com.tonnfccard.smartcard.TonWalletAppletApduCommands;
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
import java.util.List;
import java.util.Random;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import static com.tonnfccard.TonWalletApi.BYTE_ARR_HELPER;
import static com.tonnfccard.TonWalletApi.STR_HELPER;
import static com.tonnfccard.TonWalletConstants.COMMON_SECRET_SIZE;
import static com.tonnfccard.TonWalletConstants.DEFAULT_PIN;
import static com.tonnfccard.TonWalletConstants.DEFAULT_PIN_STR;
import static com.tonnfccard.TonWalletConstants.DONE_MSG;
import static com.tonnfccard.TonWalletConstants.PASSWORD_SIZE;
import static com.tonnfccard.TonWalletConstants.PERSONALIZED_STATE;
import static com.tonnfccard.TonWalletConstants.PUBLIC_KEY_LEN;
import static com.tonnfccard.TonWalletConstants.SAULT_LENGTH;
import static com.tonnfccard.TonWalletConstants.SERIAL_NUMBER_SIZE;
import static com.tonnfccard.TonWalletConstants.SHA_HASH_SIZE;
import static com.tonnfccard.TonWalletConstants.SIG_LEN;
import static com.tonnfccard.TonWalletConstants.STATE_MAP;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_BAD_RESPONSE;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_COMMON_SECRET_LEN_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_COMMON_SECRET_NOT_HEX;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_GET_SERIAL_NUMBER_RESPONSE_LEN_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_NFC_CONNECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_NFC_DISABLED;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_NFC_DISCONNECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_NO_NFC;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_NO_TAG;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_PASSWORD_LEN_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_PASSWORD_NOT_HEX;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_PIN_FORMAT_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_PIN_LEN_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_SAULT_RESPONSE_LEN_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_SERIAL_NUMBER_LEN_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_SERIAL_NUMBER_NOT_NUMERIC;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_SIG_RESPONSE_LEN_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_STATE_RESPONSE_LEN_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_TRANSCEIVE;
import static com.tonnfccard.nfc.NfcApduRunner.TIME_OUT;
import static com.tonnfccard.smartcard.CoinManagerApduCommands.LABEL_LENGTH;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.GET_APP_INFO_APDU;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.GET_PUB_KEY_WITH_DEFAULT_PATH_APDU;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.GET_SAULT_APDU;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.GET_SERIAL_NUMBER_APDU;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.SELECT_TON_WALLET_APPLET_APDU;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.getPublicKeyAPDU;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.getSignShortMessageAPDU;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.getSignShortMessageWithDefaultPathAPDU;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.getVerifyPasswordAPDU;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.getVerifyPinAPDU;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = Build.VERSION_CODES.P)
@DoNotInstrument
public class TonWalletApiTest {
    protected final ExceptionHelper EXCEPTION_HELPER = ExceptionHelper.getInstance();
    protected final StringHelper STRING_HELPER = StringHelper.getInstance();
    protected final ByteArrayUtil BYTE_ARRAY_HELPER = ByteArrayUtil.getInstance();
    protected final JsonHelper JSON_HELPER = JsonHelper.getInstance();
    protected static HmacHelper HMAC_HELPER = HmacHelper.getInstance();
    protected static boolean androidKeyStoreIsMocked = false;
    protected Random random = new Random();
    private NfcApduRunner nfcApduRunner;
    private Context context;
    private TonWalletApi tonWalletApi;

    private final CardApiInterface<List<String>> getSault = list -> tonWalletApi.getSaultAndGetJson();
    private final CardApiInterface<List<String>> getSerialNumber = list -> tonWalletApi.getSerialNumberAndGetJson();
    private final CardApiInterface<List<String>> getTonAppletState = list -> tonWalletApi.getTonAppletStateAndGetJson();

    private final CardApiInterface<List<String>> selectKeyForHmac = list -> tonWalletApi.selectKeyForHmacAndGetJson(list.get(0));
    private final CardApiInterface<List<String>> deleteKeyForHmac = list -> tonWalletApi.deleteKeyForHmacAndGetJson(list.get(0));
    private final CardApiInterface<List<String>> isKeyForHmacExist = list -> tonWalletApi.isKeyForHmacExistAndGetJson(list.get(0));
    private final CardApiInterface<List<String>> createKeyForHmac = list -> tonWalletApi.createKeyForHmacAndGetJson(list.get(0), list.get(1), list.get(2));


    List<CardApiInterface<List<String>>> cardOperationsList = Arrays.asList(getSault, getSerialNumber, getTonAppletState);
    List<CardApiInterface<List<String>>> keyOperationsList = Arrays.asList(selectKeyForHmac, deleteKeyForHmac, isKeyForHmacExist);

    public static final String SERIAL_NUMBER = "504394802433901126813236";
    public  static final String SN = "050004030904080002040303090001010206080103020306";

    @Before
    public  void init() throws Exception {
        context = ApplicationProvider.getApplicationContext();
        nfcApduRunner = NfcApduRunner.getInstance(context);
        tonWalletApi = new TonWalletApi(context, nfcApduRunner);
    }

    /** Disconnect test**/

    @Test
    public void testDisconnectNoNfcTag() throws Exception{
        IsoDep isoDep = null;
        nfcApduRunner.setCardTag(isoDep);
        tonWalletApi.setApduRunner(nfcApduRunner);
        try {
            tonWalletApi.disconnectCardAndGetJson();
        }
        catch (Exception e) {
            assertEquals(e.getMessage(), EXCEPTION_HELPER.makeFinalErrMsg(new Exception(ERROR_MSG_NO_TAG)));
        }
    }

    @Test
    public void testDisconnectNfcDisconnectFail() throws Exception{
        IsoDep isoDep = mock(IsoDep.class);
        Mockito.doThrow(new IOException()).when(isoDep).close();
        nfcApduRunner.setCardTag(isoDep);
        tonWalletApi.setApduRunner(nfcApduRunner);
        try {
            tonWalletApi.disconnectCardAndGetJson();
        }
        catch (Exception e) {
            assertEquals(e.getMessage(), EXCEPTION_HELPER.makeFinalErrMsg(new Exception(ERROR_MSG_NFC_DISCONNECT + ", more details: null")));
        }
    }

    @Test
    public void testDisconnectSuccess() throws Exception{
        IsoDep isoDep = mock(IsoDep.class);
        Mockito.doNothing().when(isoDep).close();
        nfcApduRunner.setCardTag(isoDep);
        tonWalletApi.setApduRunner(nfcApduRunner);
        try {
            String response = tonWalletApi.disconnectCardAndGetJson();
            assertEquals(response.toLowerCase(), JSON_HELPER.createResponseJson(DONE_MSG).toLowerCase());
        }
        catch (Exception e) {
            fail();
        }
    }

    /** Test for successfull response from applet **/

    @Test
    public void testGetSaultAppletSuccessfullOperation() throws Exception {
        byte[] sault = new byte[SAULT_LENGTH];
        random.nextBytes(sault);
        NfcApduRunner nfcApduRunnerMock = prepareNfcApduRunnerMock(nfcApduRunner);
        IsoDep tag =  prepareTagMock();
        when(tag.transceive(SELECT_TON_WALLET_APPLET_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS));
        when(tag.transceive(GET_SAULT_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bConcat(sault, BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS)));
        when(tag.transceive(GET_APP_INFO_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bConcat(new byte[]{PERSONALIZED_STATE}, BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS)));;
        nfcApduRunnerMock.setCardTag(tag);
        tonWalletApi.setApduRunner(nfcApduRunnerMock);
        try {
            String response = tonWalletApi.getSaultAndGetJson();
            System.out.println(response);
            assertEquals(response.toLowerCase(), JSON_HELPER.createResponseJson(BYTE_ARRAY_HELPER.hex(sault)).toLowerCase());
        }
        catch (Exception e){
            System.out.println(e.getMessage());
            fail();
        }
    }

    @Test
    public void testGetSerialNumberAppletSuccessfullOperation() throws Exception {
        byte[] sault = new byte[SAULT_LENGTH];
        random.nextBytes(sault);
        NfcApduRunner nfcApduRunnerMock = prepareNfcApduRunnerMock(nfcApduRunner);
        IsoDep tag =  prepareTagMock();
        when(tag.transceive(SELECT_TON_WALLET_APPLET_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS));
        when(tag.transceive(GET_APP_INFO_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bConcat(new byte[]{PERSONALIZED_STATE}, BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS)));;
        when(tag.transceive(GET_SERIAL_NUMBER_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bConcat(BYTE_ARRAY_HELPER.bytes(SN), BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS)));
        nfcApduRunnerMock.setCardTag(tag);
        tonWalletApi.setApduRunner(nfcApduRunnerMock);
        try {
            String response = tonWalletApi.getSerialNumberAndGetJson();
            System.out.println(response);
            assertEquals(response.toLowerCase(), JSON_HELPER.createResponseJson(SERIAL_NUMBER).toLowerCase());
        }
        catch (Exception e){
            System.out.println(e.getMessage());
            fail();
        }
    }

    @Test
    public void testGetAppInfoAppletSuccessfullOperation() throws Exception {
        NfcApduRunner nfcApduRunnerMock = prepareNfcApduRunnerMock(nfcApduRunner);
        IsoDep tag =  prepareTagMock();
        when(tag.transceive(SELECT_TON_WALLET_APPLET_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS));
        nfcApduRunnerMock.setCardTag(tag);
        tonWalletApi.setApduRunner(nfcApduRunnerMock);
        for(Byte state : STATE_MAP.keySet()) {
            when(tag.transceive(GET_APP_INFO_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bConcat(new byte[]{state}, BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS)));
            try {
                String response = tonWalletApi.getTonAppletStateAndGetJson();
                System.out.println(response);
                assertEquals(response.toLowerCase(), JSON_HELPER.createResponseJson(STATE_MAP.get(state)).toLowerCase());
            } catch (Exception e) {
                System.out.println(e.getMessage());
                fail();
            }
        }
    }

    /** Test for applet fail with some SW**/

    @Test
    public void testAppletFailedOperation() throws Exception {
        byte[] sault = new byte[SAULT_LENGTH];
        random.nextBytes(sault);
        RAPDU rapdu = new RAPDU(BYTE_ARRAY_HELPER.hex(ErrorCodes.SW_INS_NOT_SUPPORTED));
        NfcApduRunner nfcApduRunnerMock = prepareNfcApduRunnerMock(nfcApduRunner);
        IsoDep tag =  prepareTagMock();
        when(tag.transceive(SELECT_TON_WALLET_APPLET_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS));
        when(tag.transceive(GET_SERIAL_NUMBER_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_INS_NOT_SUPPORTED));
        when(tag.transceive(GET_SAULT_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_INS_NOT_SUPPORTED));
        when(tag.transceive(GET_APP_INFO_APDU.getBytes()))
                .thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_INS_NOT_SUPPORTED))
                .thenReturn(BYTE_ARRAY_HELPER.bConcat(new byte[]{PERSONALIZED_STATE}, BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS)));
        nfcApduRunnerMock.setCardTag(tag);
        tonWalletApi.setApduRunner(nfcApduRunnerMock);
        List<CardApiInterface<List<String>>> cardOperationsListShort = Arrays.asList(getTonAppletState, getSault, getSerialNumber);
        for(int i = 0 ; i < cardOperationsListShort.size(); i++) {
            try {
                cardOperationsListShort.get(i).accept(Collections.emptyList());
                fail();
            }
            catch (Exception e){
                System.out.println(e.getMessage());
                String errMsg = JSON_HELPER.createErrorJsonForCardException(rapdu.prepareSwFormatted(), nfcApduRunnerMock.getLastSentAPDU());
                assertEquals(e.getMessage(), errMsg);
            }
        }
    }

    /** Tests for incorrect card responses **/

    /** Invalid RAPDU object/responses from card tests **/

    @Test
    public void testGetSaultInvalidRAPDUAndInvalidResponseLength() throws Exception {
        byte[] sault = new byte[SAULT_LENGTH];
        random.nextBytes(sault);
        NfcApduRunner nfcApduRunnerMock = prepareNfcApduRunnerMock(nfcApduRunner);
        IsoDep tag =  prepareTagMock();
        when(tag.transceive(SELECT_TON_WALLET_APPLET_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS));
        when(tag.transceive(GET_APP_INFO_APDU.getBytes()))
                .thenReturn(BYTE_ARRAY_HELPER.bConcat(new byte[]{PERSONALIZED_STATE}, BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS)));
        nfcApduRunnerMock.setCardTag(tag);
        tonWalletApi.setApduRunner(nfcApduRunnerMock);
        List<RAPDU> badRapdus = Arrays.asList(null, new RAPDU(STRING_HELPER.randomHexString(2 * SAULT_LENGTH + 2) + "9000"),
                new RAPDU(STRING_HELPER.randomHexString(2 * SAULT_LENGTH - 2) + "9000"));
        badRapdus.forEach(rapdu -> {
            try {
                Mockito.doReturn(rapdu).when(nfcApduRunnerMock).sendAPDU(GET_SAULT_APDU);
                tonWalletApi.getSaultAndGetJson();
                fail();
            }
            catch (Exception e){
                System.out.println(e.getMessage());
                assertEquals(e.getMessage(), EXCEPTION_HELPER.makeFinalErrMsg(new Exception(ERROR_MSG_SAULT_RESPONSE_LEN_INCORRECT)));
            }
        });
    }

    @Test
    public void testGetSerialNumberInvalidRAPDUAndInvalidResponseLength() throws Exception {
        NfcApduRunner nfcApduRunnerMock = prepareNfcApduRunnerMock(nfcApduRunner);
        IsoDep tag =  prepareTagMock();
        when(tag.transceive(SELECT_TON_WALLET_APPLET_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS));
        when(tag.transceive(GET_APP_INFO_APDU.getBytes()))
                .thenReturn(BYTE_ARRAY_HELPER.bConcat(new byte[]{PERSONALIZED_STATE}, BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS)));
        nfcApduRunnerMock.setCardTag(tag);
        tonWalletApi.setApduRunner(nfcApduRunnerMock);
        List<RAPDU> badRapdus = Arrays.asList(null, new RAPDU(STRING_HELPER.randomHexString(2 * SERIAL_NUMBER_SIZE + 2) + "9000"),
                new RAPDU(STRING_HELPER.randomHexString(2 * SERIAL_NUMBER_SIZE - 2) + "9000"));
        badRapdus.forEach(rapdu -> {
            try {
                Mockito.doReturn(rapdu).when(nfcApduRunnerMock).sendAPDU(GET_SERIAL_NUMBER_APDU);
                tonWalletApi.getSerialNumberAndGetJson();
                fail();
            }
            catch (Exception e){
                System.out.println(e.getMessage());
                assertEquals(e.getMessage(), EXCEPTION_HELPER.makeFinalErrMsg(new Exception(ERROR_MSG_GET_SERIAL_NUMBER_RESPONSE_LEN_INCORRECT)));
            }
        });
    }

    @Test
    public void testGetTonAppletStateInvalidRAPDUAndInvalidResponseLength() throws Exception {
        NfcApduRunner nfcApduRunnerMock = prepareNfcApduRunnerMock(nfcApduRunner);
        IsoDep tag =  prepareTagMock();
        when(tag.transceive(SELECT_TON_WALLET_APPLET_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS));
        nfcApduRunnerMock.setCardTag(tag);
        tonWalletApi.setApduRunner(nfcApduRunnerMock);
        List<RAPDU> badRapdus = Arrays.asList(null, new RAPDU(STRING_HELPER.randomHexString(4) + "9000"), new RAPDU ("9000"));
        badRapdus.forEach(rapdu -> {
            try {
                Mockito.doReturn(rapdu).when(nfcApduRunnerMock).sendAPDU(GET_APP_INFO_APDU);
                tonWalletApi.getTonAppletStateAndGetJson();
                fail();
            }
            catch (Exception e){
                System.out.println(e.getMessage());
                assertEquals(e.getMessage(), EXCEPTION_HELPER.makeFinalErrMsg(new Exception(ERROR_MSG_STATE_RESPONSE_LEN_INCORRECT)));
            }
        });
    }

    /** Tests for incorrect input arguments **/

    /** createKeyForHmac **/

    @Test
    public void createKeyForHmacAndGetJsonTestBadPassword() {
        List<String> incorrectPasswords = Arrays.asList(null, "", "ABC", "12345", "ssAA", "1234k7");
        incorrectPasswords.forEach(password -> {
            try {
                tonWalletApi.createKeyForHmacAndGetJson(password, STRING_HELPER.randomHexString(2 * COMMON_SECRET_SIZE), STRING_HELPER.randomDigitalString(SERIAL_NUMBER_SIZE));
                fail();
            }
            catch (Exception e){
                assertEquals(e.getMessage(), EXCEPTION_HELPER.makeFinalErrMsg(new Exception(ERROR_MSG_PASSWORD_NOT_HEX)));
            }
        });

        for(int i = 0 ; i < 100 ; i++) {
            int len = random.nextInt(400);
            if (len == 2 * PASSWORD_SIZE || len % 2 != 0 || len == 0) continue;
            String password = STRING_HELPER.randomHexString(len);
            try {
                tonWalletApi.createKeyForHmacAndGetJson(password, STRING_HELPER.randomHexString(2 * COMMON_SECRET_SIZE), STRING_HELPER.randomDigitalString(SERIAL_NUMBER_SIZE));
                fail();
            }
            catch (Exception e){
                assertEquals(e.getMessage(), EXCEPTION_HELPER.makeFinalErrMsg(new Exception(ERROR_MSG_PASSWORD_LEN_INCORRECT)));
            }
        }
    }

    @Test
    public void createKeyForHmacAndGetJsonTestBadCommonSecret() {
        List<String> incorrectCS = Arrays.asList(null, "", "ABC", "12345", "ssAA", "1234k7", "123456789", "jj55667788aa");
        incorrectCS.forEach(cs -> {
            try {
                tonWalletApi.createKeyForHmacAndGetJson(STRING_HELPER.randomHexString(2 * PASSWORD_SIZE), cs, STRING_HELPER.randomDigitalString(SERIAL_NUMBER_SIZE));
                fail();
            }
            catch (Exception e){
                assertEquals(e.getMessage(), EXCEPTION_HELPER.makeFinalErrMsg(new Exception(ERROR_MSG_COMMON_SECRET_NOT_HEX)));
            }
        });

        for(int i = 0 ; i < 100 ; i++) {
            int len = random.nextInt(500);
            if (len == 2 * COMMON_SECRET_SIZE || len % 2 != 0 || len == 0) continue;
            String cs = STRING_HELPER.randomHexString(len);
            try {
                tonWalletApi.createKeyForHmacAndGetJson(STRING_HELPER.randomHexString(2 * PASSWORD_SIZE), cs, STRING_HELPER.randomDigitalString(SERIAL_NUMBER_SIZE));
                fail();
            }
            catch (Exception e){
                assertEquals(e.getMessage(), EXCEPTION_HELPER.makeFinalErrMsg(new Exception(ERROR_MSG_COMMON_SECRET_LEN_INCORRECT)));
            }
        }
    }

    @Test
    public void createKeyForHmacAndGetJsonTestBadSN() {
        List<String> incorrectSN = Arrays.asList(null, "", "ssAA", "1234k7", "1234f56789", "jj55667788aa");
        incorrectSN.forEach(sn -> {
            try {
                tonWalletApi.createKeyForHmacAndGetJson(STRING_HELPER.randomHexString(2 * PASSWORD_SIZE), STRING_HELPER.randomHexString(2 * COMMON_SECRET_SIZE), sn);
                fail();
            }
            catch (Exception e){
                assertEquals(e.getMessage(), EXCEPTION_HELPER.makeFinalErrMsg(new Exception(ERROR_MSG_SERIAL_NUMBER_NOT_NUMERIC)));
            }
        });

        for(int i = 0 ; i < 100 ; i++) {
            int len = random.nextInt(500);
            if (len == SERIAL_NUMBER_SIZE ||  len == 0) continue;
            String sn = STRING_HELPER.randomDigitalString(len);
            try {
                tonWalletApi.createKeyForHmacAndGetJson(STRING_HELPER.randomHexString(2 * PASSWORD_SIZE), STRING_HELPER.randomHexString(2 * COMMON_SECRET_SIZE), sn);
                fail();
            }
            catch (Exception e){
                assertEquals(e.getMessage(), EXCEPTION_HELPER.makeFinalErrMsg(new Exception(ERROR_MSG_SERIAL_NUMBER_LEN_INCORRECT)));
            }
        }
    }

    @Test
    public void testBadSN() {
        List<String> incorrectSN = Arrays.asList(null, "", "ssAA", "1234k7", "1234f56789", "jj55667788aa");
        for(int j = 0 ; j < keyOperationsList.size(); j++) {
            for ( String sn : incorrectSN) {
                try {
                    keyOperationsList.get(j).accept(Collections.singletonList(sn));
                    fail();
                }
                catch (Exception e){
                    assertEquals(e.getMessage(), EXCEPTION_HELPER.makeFinalErrMsg(new Exception(ERROR_MSG_SERIAL_NUMBER_NOT_NUMERIC)));
                }
            }

            for (int i = 0; i < 100; i++) {
                int len = random.nextInt(500);
                if (len == SERIAL_NUMBER_SIZE || len == 0) continue;
                String sn = STRING_HELPER.randomDigitalString(len);
                try {
                    keyOperationsList.get(j).accept(Collections.singletonList(sn));

                    fail();
                } catch (Exception e) {
                    assertEquals(e.getMessage(), EXCEPTION_HELPER.makeFinalErrMsg(new Exception(ERROR_MSG_SERIAL_NUMBER_LEN_INCORRECT)));
                }
            }
        }
    }

    protected IsoDep prepareTagMock() throws Exception {
        IsoDep tag = mock(IsoDep.class);
        when(tag.isConnected()).thenReturn(false);
        Mockito.doNothing().when(tag).connect();
        Mockito.doNothing().when(tag).setTimeout(TIME_OUT);
        return tag;
    }

    protected NfcApduRunner prepareNfcApduRunnerMock(NfcApduRunner nfcApduRunner) {
        NfcAdapter nfcAdapterMock = mock(NfcAdapter.class);
        when(nfcAdapterMock.isEnabled()).thenReturn(true);
        NfcApduRunner nfcApduRunnerMock = Mockito.spy(nfcApduRunner);
        nfcApduRunnerMock.setNfcAdapter(nfcAdapterMock);
        return nfcApduRunnerMock;
    }

    protected void mockNfcAdapter(NfcApduRunner nfcApduRunner, boolean adapterEnabled) {
        NfcAdapter nfcAdapterMock = mock(NfcAdapter.class);
        when(nfcAdapterMock.isEnabled())
                .thenReturn(adapterEnabled);
        nfcApduRunner.setNfcAdapter(nfcAdapterMock);
    }

    protected void mockNfcAdapterToBeNull(NfcApduRunner nfcApduRunner) {
        MockedStatic<NfcAdapter> nfcAdapterMockedStatic = Mockito.mockStatic(NfcAdapter.class);
        nfcAdapterMockedStatic
                .when(() -> NfcAdapter.getDefaultAdapter(any()))
                .thenReturn(null);
        nfcApduRunner.setNfcAdapter(null);
    }

    protected HmacHelper prepareHmacHelperMock(HmacHelper hmacHelper) throws Exception{
        HmacHelper hmacHelperMock = Mockito.spy(hmacHelper);
        byte[] mac = BYTE_ARRAY_HELPER.bytes(STRING_HELPER.randomHexString(SHA_HASH_SIZE * 2));
        Mockito.doReturn(mac).when(hmacHelperMock).computeMac(any());
        System.out.println(BYTE_ARRAY_HELPER.hex(hmacHelperMock.computeMac(new byte[1])));
        return hmacHelperMock;
    }

    protected void mockAndroidKeyStore() throws Exception{
        if (androidKeyStoreIsMocked) return;
        KeyStore keyStoreMock = mock(KeyStore.class);
        MockedStatic<KeyStore> keyStoreMockStatic = Mockito.mockStatic(KeyStore.class);
        keyStoreMockStatic
                .when(() -> KeyStore.getInstance(any()))
                .thenReturn(keyStoreMock);
        Mockito.doNothing().when(keyStoreMock).load(any());
        Mockito.doNothing().when(keyStoreMock).setEntry(any(), any(), any());
        Mockito.doReturn(true).when(keyStoreMock).containsAlias(any());
        androidKeyStoreIsMocked = true;
    }

}