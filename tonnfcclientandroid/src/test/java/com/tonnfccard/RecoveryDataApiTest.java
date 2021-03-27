package com.tonnfccard;

import android.content.Context;
import android.nfc.NfcAdapter;
import android.nfc.tech.IsoDep;
import android.os.Build;
import android.util.Log;

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
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.internal.DoNotInstrument;
import org.robolectric.shadows.ShadowLog;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static com.tonnfccard.TonWalletConstants.DATA_RECOVERY_PORTION_MAX_SIZE;
import static com.tonnfccard.TonWalletConstants.DEFAULT_PIN_STR;
import static com.tonnfccard.TonWalletConstants.DONE_MSG;
import static com.tonnfccard.TonWalletConstants.FALSE_MSG;
import static com.tonnfccard.TonWalletConstants.GENERATED_MSG;
import static com.tonnfccard.TonWalletConstants.MAX_PIN_TRIES;
import static com.tonnfccard.TonWalletConstants.RECOVERY_DATA_MAX_SIZE;
import static com.tonnfccard.TonWalletConstants.SHA_HASH_SIZE;
import static com.tonnfccard.TonWalletConstants.TRUE_MSG;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_BAD_RESPONSE;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_IS_RECOVERY_DATA_SET_RESPONSE_LEN_INCORRECT;
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
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_RECOVERY_DATA_HASH_RESPONSE_LEN_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_RECOVERY_DATA_LENGTH_RESPONSE_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_RECOVERY_DATA_LENGTH_RESPONSE_LEN_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_RECOVERY_DATA_LEN_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_RECOVERY_DATA_NOT_HEX;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_RECOVERY_DATA_PORTION_INCORRECT_LEN;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_TRANSCEIVE;
import static com.tonnfccard.nfc.NfcApduRunner.TIME_OUT;
import static com.tonnfccard.smartcard.CoinManagerApduCommands.LABEL_LENGTH;
import static com.tonnfccard.smartcard.CoinManagerApduCommands.SELECT_COIN_MANAGER_APDU;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.GET_APPLET_STATE_APDU_LIST;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.GET_RECOVERY_DATA_LEN_APDU;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.getGetRecoveryDataPartAPDU;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = Build.VERSION_CODES.P)
@DoNotInstrument
public class RecoveryDataApiTest {
    private final ExceptionHelper EXCEPTION_HELPER = ExceptionHelper.getInstance();
    private final StringHelper STRING_HELPER = StringHelper.getInstance();
    private final ByteArrayUtil BYTE_ARRAY_HELPER = ByteArrayUtil.getInstance();
    private final JsonHelper JSON_HELPER = JsonHelper.getInstance();
    private Random random = new Random();
    private NfcApduRunner nfcApduRunner;
    private Context context;
    private final RAPDU SUCCESS_RAPDU = new RAPDU(BYTE_ARRAY_HELPER.hex(ErrorCodes.SW_SUCCESS));

    private RecoveryDataApi recoveryDataApi;

    private final CardApiInterface<List<String>> addRecoveryData = list -> recoveryDataApi.addRecoveryDataAndGetJson(list.get(0));
    private final CardApiInterface<List<String>> getRecoveryData = list -> recoveryDataApi.getRecoveryDataAndGetJson();
    private final CardApiInterface<List<String>> getRecoveryDataHash = list -> recoveryDataApi.getRecoveryDataHashAndGetJson();
    private final CardApiInterface<List<String>> getRecoveryDataLen = list -> recoveryDataApi.getRecoveryDataLenAndGetJson();
    private final CardApiInterface<List<String>> isRecoveryDataSet = list -> recoveryDataApi.isRecoveryDataSetAndGetJson();
    private final CardApiInterface<List<String>> resetRecovery = list -> recoveryDataApi.resetRecoveryDataAndGetJson();

    List<CardApiInterface<List<String>>> cardOperationsList = Arrays.asList(addRecoveryData, getRecoveryData, getRecoveryDataHash, getRecoveryDataLen,
            isRecoveryDataSet, resetRecovery);
    List<CardApiInterface<List<String>>> cardOperationsListShort = Arrays.asList(addRecoveryData,  getRecoveryDataHash, getRecoveryDataLen,
            isRecoveryDataSet, resetRecovery);


    @Before
    public  void init() throws Exception {
        context = ApplicationProvider.getApplicationContext();
        nfcApduRunner = NfcApduRunner.getInstance(context);
        recoveryDataApi = new RecoveryDataApi(context, nfcApduRunner);
    }

    /** Common tests for NFC errors**/

    private void prepareNfcTest(String errMsg) {
        for(int i = 0 ; i < cardOperationsList.size(); i++) {
            List<String> args = i == 0 ? Collections.singletonList(STRING_HELPER.randomHexString(2 * RECOVERY_DATA_MAX_SIZE))
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
        recoveryDataApi.setApduRunner(nfcApduRunner);
        prepareNfcTest(ERROR_MSG_NO_TAG);
    }

    @Test
    public void testNoNfc() {
        IsoDep isoDep = mock(IsoDep.class);
        nfcApduRunner.setCardTag(isoDep);
        MockedStatic<NfcAdapter> nfcAdapterMockedStatic = Mockito.mockStatic(NfcAdapter.class);
        nfcAdapterMockedStatic
                .when(() -> NfcAdapter.getDefaultAdapter(any()))
                .thenReturn(null);
        nfcApduRunner.setNfcAdapter(null);
        recoveryDataApi.setApduRunner(nfcApduRunner);
        prepareNfcTest(ERROR_MSG_NO_NFC);
    }

    @Test
    public void testNfcDisabled() {
        IsoDep isoDep = mock(IsoDep.class);
        NfcAdapter nfcAdapterMock = mock(NfcAdapter.class);
        when(nfcAdapterMock.isEnabled())
                .thenReturn(false);
        nfcApduRunner.setCardTag(isoDep);
        nfcApduRunner.setNfcAdapter(nfcAdapterMock);
        recoveryDataApi.setApduRunner(nfcApduRunner);
        prepareNfcTest(ERROR_MSG_NFC_DISABLED);
    }

    @Test
    public void testNfcConnectFail() throws Exception{
        IsoDep isoDep = mock(IsoDep.class);
        Mockito.doThrow(new IOException()).when(isoDep).connect();
        NfcAdapter nfcAdapterMock = mock(NfcAdapter.class);
        when(nfcAdapterMock.isEnabled())
                .thenReturn(true);
        nfcApduRunner.setCardTag(isoDep);
        nfcApduRunner.setNfcAdapter(nfcAdapterMock);
        recoveryDataApi.setApduRunner(nfcApduRunner);
        prepareNfcTest(ERROR_MSG_NFC_CONNECT);
    }

    @Test
    public void testNfcTransceiveFail() throws Exception{
        NfcAdapter nfcAdapterMock = mock(NfcAdapter.class);
        when(nfcAdapterMock.isEnabled())
                .thenReturn(true);
        nfcApduRunner.setNfcAdapter(nfcAdapterMock);
        IsoDep tag = mock(IsoDep.class);
        when(tag.isConnected()).thenReturn(false);
        Mockito.doNothing().when(tag).connect();
        Mockito.doNothing().when(tag).setTimeout(TIME_OUT);
        Mockito.doThrow(new IOException()).when(tag).transceive(any());
        nfcApduRunner.setCardTag(tag);
        recoveryDataApi.setApduRunner(nfcApduRunner);
        prepareNfcTest(ERROR_TRANSCEIVE + ", More details: null");
    }

    @Test
    public void testNfcTooShortResponse() throws Exception{
        NfcAdapter nfcAdapterMock = mock(NfcAdapter.class);
        when(nfcAdapterMock.isEnabled())
                .thenReturn(true);
        nfcApduRunner.setNfcAdapter(nfcAdapterMock);
        for (int i = 0; i < 3 ; i++) {
            IsoDep tag = mock(IsoDep.class);
            when(tag.isConnected()).thenReturn(false);
            Mockito.doNothing().when(tag).connect();
            Mockito.doNothing().when(tag).setTimeout(TIME_OUT);
            if (i <  2) {
                when(tag.transceive(any())).thenReturn(new byte[i]);
            }
            else {
                when(tag.transceive(any())).thenReturn(null);
            }
            nfcApduRunner.setCardTag(tag);
            recoveryDataApi.setApduRunner(nfcApduRunner);
            prepareNfcTest(ERROR_BAD_RESPONSE);
        }
    }

    /** Tests for incorrect card responses **/

    /** Invalid RAPDU object/responses from card tests **/

    @Test
    public void testInvalidRAPDU() throws Exception {
        Map<CardApiInterface<List<String>>, String> opsErrors = new LinkedHashMap<>();
        opsErrors.put(getRecoveryDataHash, ERROR_MSG_RECOVERY_DATA_HASH_RESPONSE_LEN_INCORRECT);
        opsErrors.put(getRecoveryDataLen, ERROR_MSG_RECOVERY_DATA_LENGTH_RESPONSE_LEN_INCORRECT);
        opsErrors.put(isRecoveryDataSet, ERROR_IS_RECOVERY_DATA_SET_RESPONSE_LEN_INCORRECT);
        NfcApduRunner nfcApduRunnerMock = Mockito.spy(nfcApduRunner);
        Mockito.doReturn(null).when(nfcApduRunnerMock).sendTonWalletAppletAPDU(any());
        recoveryDataApi.setApduRunner(nfcApduRunnerMock);
        for(CardApiInterface<List<String>> op : opsErrors.keySet()) {
            try {
                op.accept(Collections.EMPTY_LIST);
                fail();
            }
            catch (Exception e){
                assertEquals(e.getMessage(), EXCEPTION_HELPER.makeFinalErrMsg(new Exception(opsErrors.get(op))));
            }
        }

        NfcApduRunner nfcApduRunnerMock1 = Mockito.spy(nfcApduRunner);
        byte tailLen = 2;
        Mockito.doReturn(new RAPDU(BYTE_ARRAY_HELPER.bConcat(new byte[]{0, tailLen}, BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS))))
                .when(nfcApduRunnerMock1).sendTonWalletAppletAPDU(GET_RECOVERY_DATA_LEN_APDU);
        Mockito.doReturn(null).when(nfcApduRunnerMock1).sendTonWalletAppletAPDU(getGetRecoveryDataPartAPDU(new byte[]{0, 0}, tailLen));
        recoveryDataApi.setApduRunner(nfcApduRunnerMock1);
        try {
            getRecoveryData.accept(Collections.EMPTY_LIST);
            fail();
        }
        catch (Exception e){
            assertEquals(e.getMessage(), EXCEPTION_HELPER.makeFinalErrMsg(new Exception(ERROR_RECOVERY_DATA_PORTION_INCORRECT_LEN + tailLen)));
        }
    }

    @Test
    public void testWrongResponseLength() throws Exception{
        Map<CardApiInterface<List<String>>, String> opsErrors = new LinkedHashMap<>();
        opsErrors.put(getRecoveryDataHash, ERROR_MSG_RECOVERY_DATA_HASH_RESPONSE_LEN_INCORRECT);
        opsErrors.put(getRecoveryDataLen, ERROR_MSG_RECOVERY_DATA_LENGTH_RESPONSE_LEN_INCORRECT);
        opsErrors.put(isRecoveryDataSet, ERROR_IS_RECOVERY_DATA_SET_RESPONSE_LEN_INCORRECT);
        Map<CardApiInterface<List<String>>, List<Integer>> opsLens  = new LinkedHashMap<>();
        opsLens.put(getRecoveryDataHash,  Arrays.asList(SHA_HASH_SIZE + 1, SHA_HASH_SIZE + 3));
        opsLens.put(getRecoveryDataLen, Arrays.asList(3, 5));
        opsLens.put(isRecoveryDataSet, Arrays.asList(2, 4));

        for(CardApiInterface<List<String>> op : opsErrors.keySet()) {
            NfcApduRunner nfcApduRunnerMock = Mockito.spy(nfcApduRunner);
            List<Integer> badLens = opsLens.get(op);
            String errMsg = opsErrors.get(op);
            badLens.forEach(len -> {
                System.out.println(len);
                try {
                    Mockito.doReturn(new RAPDU(new byte[len])).when(nfcApduRunnerMock).sendTonWalletAppletAPDU(any());
                    recoveryDataApi.setApduRunner(nfcApduRunnerMock);
                    op.accept(Collections.EMPTY_LIST);
                    fail();
                }
                catch (Exception e){
                    assertEquals(e.getMessage(), EXCEPTION_HELPER.makeFinalErrMsg(new Exception(errMsg)));
                }
            });
        }

        NfcApduRunner nfcApduRunnerMock1 = Mockito.spy(nfcApduRunner);
        byte tailLen = (byte) 120;
        Mockito.doReturn(new RAPDU(BYTE_ARRAY_HELPER.bConcat(new byte[]{0,  tailLen}, BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS))))
                .when(nfcApduRunnerMock1).sendTonWalletAppletAPDU(GET_RECOVERY_DATA_LEN_APDU);
        List<Integer> badLens = Arrays.asList(tailLen + 1, tailLen + 3);
        badLens.forEach(len -> {
            try {
                Mockito.doReturn(new RAPDU(new byte[len])).when(nfcApduRunnerMock1).sendTonWalletAppletAPDU(getGetRecoveryDataPartAPDU(new byte[]{0, 0}, tailLen));
                recoveryDataApi.setApduRunner(nfcApduRunnerMock1);
                getRecoveryData.accept(Collections.EMPTY_LIST);
                fail();
            }
            catch (Exception e){
                assertEquals(e.getMessage(), EXCEPTION_HELPER.makeFinalErrMsg(new Exception(ERROR_RECOVERY_DATA_PORTION_INCORRECT_LEN + tailLen)));
            }
        });
    }

    @Test
    public void getRecoveryDataLenTestWrongValResponse() throws Exception {
        NfcAdapter nfcAdapterMock = mock(NfcAdapter.class);
        when(nfcAdapterMock.isEnabled())
                .thenReturn(true);
        NfcApduRunner nfcApduRunnerMock = Mockito.spy(nfcApduRunner);
        Mockito.doReturn(new RAPDU(BYTE_ARRAY_HELPER.bConcat(new byte[]{(byte) 0x17}, BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS))))
                .when(nfcApduRunnerMock).sendAPDUList(GET_APPLET_STATE_APDU_LIST);
        nfcApduRunnerMock.setNfcAdapter(nfcAdapterMock);

        List<Short> wrongLens = Arrays.asList((short) -120, (short) -1, (short) 0, (short)(RECOVERY_DATA_MAX_SIZE + 1));
        for (Short len : wrongLens) {
            IsoDep tag = mock(IsoDep.class);
            when(tag.isConnected()).thenReturn(false);
            Mockito.doNothing().when(tag).connect();
            Mockito.doNothing().when(tag).setTimeout(TIME_OUT);
            byte[] lenBytes = new byte[2];
            BYTE_ARRAY_HELPER.setShort(lenBytes, (short)0, len);
            when(tag.transceive(any())).thenReturn(BYTE_ARRAY_HELPER.bConcat(lenBytes, BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS)));
            nfcApduRunnerMock.setCardTag(tag);
            recoveryDataApi.setApduRunner(nfcApduRunnerMock);
            try {
                getRecoveryDataLen.accept(Collections.EMPTY_LIST);
                fail();
            }
            catch (Exception e){
                assertEquals(e.getMessage(), EXCEPTION_HELPER.makeFinalErrMsg(new Exception((ERROR_MSG_RECOVERY_DATA_LENGTH_RESPONSE_INCORRECT))));
            }
        }
    }

    /** Tests for incorrect input arguments **/

    /** addRecoveryData **/
    @Test
    public void addRecoveryDataAndGetJsonTestBadInputDeviceLabel() {
        List<String> incorrectRecoveryData = Arrays.asList(null, "", "ABC", "12345", "ssAA", "1234k7", STRING_HELPER.randomHexString(2 * RECOVERY_DATA_MAX_SIZE + 1));
        incorrectRecoveryData.forEach(data -> {
            try {
                recoveryDataApi.addRecoveryDataAndGetJson(data);
                fail();
            }
            catch (Exception e){
                assertEquals(e.getMessage(), EXCEPTION_HELPER.makeFinalErrMsg(new Exception(ERROR_MSG_RECOVERY_DATA_NOT_HEX)));
            }
        });

        String data = STRING_HELPER.randomHexString(2 * (RECOVERY_DATA_MAX_SIZE + 1));
        try {
            recoveryDataApi.addRecoveryDataAndGetJson(data);
            fail();
        }
        catch (Exception e){
            assertEquals(e.getMessage(), EXCEPTION_HELPER.makeFinalErrMsg(new Exception(ERROR_MSG_RECOVERY_DATA_LEN_INCORRECT)));
        }
    }

    /** Test for successfull response from applet **/

    @Test
    public void testAppletSuccessfullOperations() throws Exception {
        NfcAdapter nfcAdapterMock = mock(NfcAdapter.class);
        when(nfcAdapterMock.isEnabled()).thenReturn(true);
        NfcApduRunner nfcApduRunnerMock = Mockito.spy(nfcApduRunner);
        Mockito.doReturn(new RAPDU(BYTE_ARRAY_HELPER.bConcat(new byte[]{(byte) 0x17}, BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS))))
                .when(nfcApduRunnerMock).sendAPDUList(GET_APPLET_STATE_APDU_LIST);
        nfcApduRunnerMock.setNfcAdapter(nfcAdapterMock);
        short recoveryDataLen = (short) Math.abs(random.nextInt(RECOVERY_DATA_MAX_SIZE) + 1);
        System.out.println(recoveryDataLen);
        String recoveryData = STRING_HELPER.randomHexString(2 * recoveryDataLen);
        Map<CardApiInterface<List<String>>, String> map = new LinkedHashMap<>();
        map.put(addRecoveryData, BYTE_ARRAY_HELPER.hex(ErrorCodes.SW_SUCCESS));
        map.put(resetRecovery, BYTE_ARRAY_HELPER.hex(ErrorCodes.SW_SUCCESS));
        map.put(getRecoveryDataHash, STRING_HELPER.randomHexString(2 * SHA_HASH_SIZE) + BYTE_ARRAY_HELPER.hex(ErrorCodes.SW_SUCCESS));
        map.put(getRecoveryDataLen, BYTE_ARRAY_HELPER.hex(recoveryDataLen) + BYTE_ARRAY_HELPER.hex(ErrorCodes.SW_SUCCESS));
        map.put(isRecoveryDataSet, "01" + BYTE_ARRAY_HELPER.hex(ErrorCodes.SW_SUCCESS));

        for(int i = 0 ; i < cardOperationsListShort.size(); i++) {
            List<String> args = i == 0 ? Collections.singletonList(recoveryData)
                    : Collections.emptyList();
            CardApiInterface<List<String>> op = cardOperationsListShort.get(i);
            IsoDep tag = mock(IsoDep.class);
            when(tag.isConnected()).thenReturn(false);
            Mockito.doNothing().when(tag).connect();
            Mockito.doNothing().when(tag).setTimeout(TIME_OUT);
            when(tag.transceive(any())).thenReturn(BYTE_ARRAY_HELPER.bytes(map.get(op)));
            nfcApduRunnerMock.setCardTag(tag);
            recoveryDataApi.setApduRunner(nfcApduRunnerMock);
            String response = op.accept(args);

            String res = map.get(op).substring(0, map.get(op).length() - 4);
            if (op == isRecoveryDataSet) {
                res = res.equals("00") ? FALSE_MSG : TRUE_MSG;
            }
            else if (op == getRecoveryDataLen) {
                res = String.valueOf(recoveryDataLen);
            }

            String msg = map.get(op).equals(BYTE_ARRAY_HELPER.hex(ErrorCodes.SW_SUCCESS)) ? DONE_MSG : res;

            System.out.println(response.toLowerCase());
            assertEquals(response.toLowerCase(), JSON_HELPER.createResponseJson(msg).toLowerCase());
        }
    }

    @Test
    public void getRecoveryDataTestAppletSuccessfullOperations1() throws Exception {
        NfcAdapter nfcAdapterMock = mock(NfcAdapter.class);
        when(nfcAdapterMock.isEnabled()).thenReturn(true);
        NfcApduRunner nfcApduRunnerMock = Mockito.spy(nfcApduRunner);
        nfcApduRunnerMock.setNfcAdapter(nfcAdapterMock);
        short tailLen = 20;
        int portionsNum = 8;
        short recoveryDataLen = (short) ( portionsNum * DATA_RECOVERY_PORTION_MAX_SIZE + tailLen);
        byte[] recoveryDataLenBytes = new byte[2];
        BYTE_ARRAY_HELPER.setShort(recoveryDataLenBytes, (short) 0, recoveryDataLen);
        String recoveryData = STRING_HELPER.randomHexString(2 * recoveryDataLen);
        byte[] recoveryDataBytes = BYTE_ARRAY_HELPER.bytes(recoveryData);

        Mockito.doReturn(new RAPDU(BYTE_ARRAY_HELPER.bConcat(recoveryDataLenBytes, BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS))))
                .when(nfcApduRunnerMock).sendTonWalletAppletAPDU(GET_RECOVERY_DATA_LEN_APDU);

        for (int i = 0; i < portionsNum + 1; i++) {
            short start = (short)(i * DATA_RECOVERY_PORTION_MAX_SIZE);
            short len = i == portionsNum ? tailLen : DATA_RECOVERY_PORTION_MAX_SIZE;
            byte[] portion = BYTE_ARRAY_HELPER.bSub(recoveryDataBytes, start, len);
            byte[] bytes = BYTE_ARRAY_HELPER.bConcat(portion, BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS));
            byte[] startBytes = new byte[2];
            BYTE_ARRAY_HELPER.setShort(startBytes, (short) 0, start);
            System.out.println(BYTE_ARRAY_HELPER.hex(startBytes));
            Mockito.doReturn(new RAPDU(bytes)).when(nfcApduRunnerMock).sendTonWalletAppletAPDU(getGetRecoveryDataPartAPDU(startBytes, (byte)len));
        }
        recoveryDataApi.setApduRunner(nfcApduRunnerMock);
        String response = recoveryDataApi.getRecoveryDataAndGetJson();
        System.out.println(response.toLowerCase());
        assertEquals(response.toLowerCase(), JSON_HELPER.createResponseJson(recoveryData).toLowerCase());
    }

    @Test
    public void getRecoveryDataTestAppletSuccessfullOperations2() throws Exception {
        NfcAdapter nfcAdapterMock = mock(NfcAdapter.class);
        when(nfcAdapterMock.isEnabled()).thenReturn(true);
        NfcApduRunner nfcApduRunnerMock = Mockito.spy(nfcApduRunner);
        nfcApduRunnerMock.setNfcAdapter(nfcAdapterMock);
        short recoveryDataLen = 20;
        byte[] recoveryDataLenBytes = new byte[2];
        BYTE_ARRAY_HELPER.setShort(recoveryDataLenBytes, (short) 0, recoveryDataLen);
        String recoveryData = STRING_HELPER.randomHexString(2 * recoveryDataLen);
        Mockito.doReturn(new RAPDU(BYTE_ARRAY_HELPER.bConcat(recoveryDataLenBytes, BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS))))
                .when(nfcApduRunnerMock).sendTonWalletAppletAPDU(GET_RECOVERY_DATA_LEN_APDU);
        Mockito.doReturn(new RAPDU(BYTE_ARRAY_HELPER.bConcat(new byte[]{(byte) 0x17}, BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS))))
                .when(nfcApduRunnerMock).sendAPDUList(GET_APPLET_STATE_APDU_LIST);
        IsoDep tag = mock(IsoDep.class);
        when(tag.isConnected()).thenReturn(false);
        Mockito.doNothing().when(tag).connect();
        Mockito.doNothing().when(tag).setTimeout(TIME_OUT);
        when(tag.transceive(any())).thenReturn(BYTE_ARRAY_HELPER.bytes(recoveryData + BYTE_ARRAY_HELPER.hex(ErrorCodes.SW_SUCCESS)));
        nfcApduRunnerMock.setCardTag(tag);
        recoveryDataApi.setApduRunner(nfcApduRunnerMock);
        String response = recoveryDataApi.getRecoveryDataAndGetJson();
        assertEquals(response.toLowerCase(), JSON_HELPER.createResponseJson(recoveryData).toLowerCase());
    }

    /** Test for applet fail with some SW**/

    @Test
    public void testAppletFailedOperation() throws Exception{
        NfcAdapter nfcAdapterMock = mock(NfcAdapter.class);
        when(nfcAdapterMock.isEnabled()).thenReturn(true);
        short recoveryDataLen = 100;
        String recoveryData = STRING_HELPER.randomHexString(2 * recoveryDataLen);
        RAPDU rapdu = new RAPDU(BYTE_ARRAY_HELPER.hex(ErrorCodes.SW_INS_NOT_SUPPORTED));
        NfcApduRunner nfcApduRunnerMock = Mockito.spy(nfcApduRunner);
        nfcApduRunnerMock.setNfcAdapter(nfcAdapterMock);
        Mockito.doReturn(new RAPDU(BYTE_ARRAY_HELPER.bConcat(new byte[]{(byte) 0x17}, BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS))))
                .when(nfcApduRunnerMock).sendAPDUList(GET_APPLET_STATE_APDU_LIST);
        IsoDep tag = mock(IsoDep.class);
        when(tag.isConnected()).thenReturn(false);
        Mockito.doNothing().when(tag).connect();
        Mockito.doNothing().when(tag).setTimeout(TIME_OUT);
        when(tag.transceive(any())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_INS_NOT_SUPPORTED));

        nfcApduRunnerMock.setCardTag(tag);
        recoveryDataApi.setApduRunner(nfcApduRunnerMock);

        for(int i = 0 ; i < cardOperationsListShort.size(); i++) {
            List<String> args = i == 0 ? Collections.singletonList(recoveryData)
                    : Collections.emptyList();
            CardApiInterface<List<String>> op = cardOperationsListShort.get(i);

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


    @Test
    public void getRecoveryDataTestAppletFailedOperation() throws Exception{
        NfcAdapter nfcAdapterMock = mock(NfcAdapter.class);
        when(nfcAdapterMock.isEnabled()).thenReturn(true);
        short recoveryDataLen = 30;
        byte[] recoveryDataLenBytes = new byte[2];
        BYTE_ARRAY_HELPER.setShort(recoveryDataLenBytes, (short) 0, recoveryDataLen);
        RAPDU rapdu = new RAPDU(BYTE_ARRAY_HELPER.hex(ErrorCodes.SW_INS_NOT_SUPPORTED));
        NfcApduRunner nfcApduRunnerMock = Mockito.spy(nfcApduRunner);
        nfcApduRunnerMock.setNfcAdapter(nfcAdapterMock);
        Mockito.doReturn(new RAPDU(BYTE_ARRAY_HELPER.bConcat(new byte[]{(byte) 0x17}, BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS))))
                .when(nfcApduRunnerMock).sendAPDUList(GET_APPLET_STATE_APDU_LIST);
        Mockito.doReturn(new RAPDU(BYTE_ARRAY_HELPER.bConcat(recoveryDataLenBytes, BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS))))
                .when(nfcApduRunnerMock).sendTonWalletAppletAPDU(GET_RECOVERY_DATA_LEN_APDU);
        IsoDep tag = mock(IsoDep.class);
        when(tag.isConnected()).thenReturn(false);
        Mockito.doNothing().when(tag).connect();
        Mockito.doNothing().when(tag).setTimeout(TIME_OUT);
        when(tag.transceive(any())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_INS_NOT_SUPPORTED));
        nfcApduRunnerMock.setCardTag(tag);
        recoveryDataApi.setApduRunner(nfcApduRunnerMock);
        try {
            recoveryDataApi.getRecoveryDataAndGetJson();
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