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

import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static com.tonnfccard.NfcMockHelper.SW_SUCCESS;
import static com.tonnfccard.NfcMockHelper.prepareNfcApduRunnerMock;
import static com.tonnfccard.NfcMockHelper.prepareTagMock;
import static com.tonnfccard.TonWalletConstants.DATA_RECOVERY_PORTION_MAX_SIZE;
import static com.tonnfccard.TonWalletConstants.DONE_MSG;
import static com.tonnfccard.TonWalletConstants.PERSONALIZED_STATE;
import static com.tonnfccard.TonWalletConstants.RECOVERY_DATA_MAX_SIZE;
import static com.tonnfccard.TonWalletConstants.SHA_HASH_SIZE;
import static com.tonnfccard.TonWalletConstants.TRUE_MSG;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_IS_RECOVERY_DATA_SET_RESPONSE_LEN_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_RECOVERY_DATA_HASH_RESPONSE_LEN_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_RECOVERY_DATA_LENGTH_RESPONSE_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_RECOVERY_DATA_LENGTH_RESPONSE_LEN_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_RECOVERY_DATA_LEN_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_RECOVERY_DATA_NOT_HEX;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_RECOVERY_DATA_PORTION_INCORRECT_LEN;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.GET_APP_INFO_APDU;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.GET_RECOVERY_DATA_HASH_APDU;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.GET_RECOVERY_DATA_LEN_APDU;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.IS_RECOVERY_DATA_SET_APDU;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.RESET_RECOVERY_DATA_APDU;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.SELECT_TON_WALLET_APPLET_APDU;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.getAddRecoveryDataPartAPDU;
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
    private final Random random = new Random();
    private NfcApduRunner nfcApduRunner;

    private RecoveryDataApi recoveryDataApi;

    private final CardApiInterface<List<String>> addRecoveryData = list -> recoveryDataApi.addRecoveryDataAndGetJson(list.get(0));
    private final CardApiInterface<List<String>> getRecoveryData = list -> recoveryDataApi.getRecoveryDataAndGetJson();
    private final CardApiInterface<List<String>> getRecoveryDataHash = list -> recoveryDataApi.getRecoveryDataHashAndGetJson();
    private final CardApiInterface<List<String>> getRecoveryDataLen = list -> recoveryDataApi.getRecoveryDataLenAndGetJson();
    private final CardApiInterface<List<String>> isRecoveryDataSet = list -> recoveryDataApi.isRecoveryDataSetAndGetJson();
    private final CardApiInterface<List<String>> resetRecovery = list -> recoveryDataApi.resetRecoveryDataAndGetJson();

    @Before
    public  void init() throws Exception {
        Context context = ApplicationProvider.getApplicationContext();
        nfcApduRunner = NfcApduRunner.getInstance(context);
        recoveryDataApi = new RecoveryDataApi(context, nfcApduRunner);
    }

    /** Tests for incorrect card responses **/

    /** Invalid RAPDU object/responses from card tests **/

    @Test
    public void testWrongResponse() throws Exception{
        Map<CardApiInterface<List<String>>, String> opsErrors = new LinkedHashMap<>();
        opsErrors.put(getRecoveryDataHash, ERROR_MSG_RECOVERY_DATA_HASH_RESPONSE_LEN_INCORRECT);
        opsErrors.put(getRecoveryDataLen, ERROR_MSG_RECOVERY_DATA_LENGTH_RESPONSE_LEN_INCORRECT);
        opsErrors.put(isRecoveryDataSet, ERROR_IS_RECOVERY_DATA_SET_RESPONSE_LEN_INCORRECT);
        NfcApduRunner nfcApduRunnerMock = prepareNfcApduRunnerMock(nfcApduRunner);
        IsoDep tag = prepareTagMock();
        nfcApduRunnerMock.setCardTag(tag);
        Map<CardApiInterface<List<String>>, List<RAPDU>> opsBadRapdu  = new LinkedHashMap<>();
        opsBadRapdu.put(getRecoveryDataHash,  Arrays.asList(null, new RAPDU(new byte[2]), new RAPDU(new byte[SHA_HASH_SIZE + 3]), new RAPDU(new byte[SHA_HASH_SIZE + 5])));
        opsBadRapdu.put(getRecoveryDataLen, Arrays.asList(null,  new RAPDU(new byte[2]),  new RAPDU(new byte[3]),  new RAPDU(new byte[5])));
        opsBadRapdu.put(isRecoveryDataSet, Arrays.asList(null,  new RAPDU(new byte[2]), new RAPDU(new byte[4]),new RAPDU(new byte[5])));
        for(CardApiInterface<List<String>> op : opsErrors.keySet()) {
            List<RAPDU> badRapdus = opsBadRapdu.get(op);
            String errMsg = opsErrors.get(op);
            badRapdus.forEach(rapdu -> {
                try {
                    Mockito.doReturn(rapdu).when(nfcApduRunnerMock).sendTonWalletAppletAPDU(any());
                    recoveryDataApi.setApduRunner(nfcApduRunnerMock);
                    op.accept(Collections.emptyList());
                    fail();
                }
                catch (Exception e){
                    assertEquals(e.getMessage(), EXCEPTION_HELPER.makeFinalErrMsg(new Exception(errMsg)));
                }
            });
        }
    }

    @Test
    public void testWrongResponseGetRecoveryData() throws Exception{
        NfcApduRunner nfcApduRunnerMock = prepareNfcApduRunnerMock(nfcApduRunner);
        IsoDep tag = prepareTagMock();
        nfcApduRunnerMock.setCardTag(tag);
        byte tailLen = (byte) 120;
        Mockito.doReturn(new RAPDU(BYTE_ARRAY_HELPER.bConcat(new byte[]{0,  tailLen}, BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS))))
                .when(nfcApduRunnerMock).sendTonWalletAppletAPDU(GET_RECOVERY_DATA_LEN_APDU);
        List<RAPDU> badRapdus = Arrays.asList(null, new RAPDU(new byte[2]), new RAPDU(new byte[tailLen + 1]), new RAPDU(new byte[tailLen + 3]));
        badRapdus.forEach(rapdu -> {
            try {
                Mockito.doReturn(rapdu).when(nfcApduRunnerMock).sendTonWalletAppletAPDU(getGetRecoveryDataPartAPDU(new byte[]{0, 0}, tailLen));
                recoveryDataApi.setApduRunner(nfcApduRunnerMock);
                getRecoveryData.accept(Collections.emptyList());
                fail();
            }
            catch (Exception e){
                System.out.println(e.getMessage());
                assertEquals(e.getMessage(), EXCEPTION_HELPER.makeFinalErrMsg(new Exception(ERROR_RECOVERY_DATA_PORTION_INCORRECT_LEN + tailLen)));
            }
        });
    }


    @Test
    public void getRecoveryDataLenTestWrongValResponse() throws Exception {
        NfcApduRunner nfcApduRunnerMock = prepareNfcApduRunnerMock(nfcApduRunner);
        IsoDep tag = prepareAdvancedTagMock();
        List<Short> wrongLens = Arrays.asList((short) -120, (short) -1, (short) 0, (short)(RECOVERY_DATA_MAX_SIZE + 1));
        for (Short len : wrongLens) {
            byte[] lenBytes = new byte[2];
            BYTE_ARRAY_HELPER.setShort(lenBytes, (short)0, len);
            when(tag.transceive(GET_RECOVERY_DATA_LEN_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bConcat(lenBytes, BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS)));
            nfcApduRunnerMock.setCardTag(tag);
            recoveryDataApi.setApduRunner(nfcApduRunnerMock);
            try {
                recoveryDataApi.getRecoveryDataLenAndGetJson();
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
        List<CardApiInterface<List<String>>> cardOperationsListShort = Arrays.asList(resetRecovery,  getRecoveryDataHash, getRecoveryDataLen,
                isRecoveryDataSet);
        NfcApduRunner nfcApduRunnerMock = prepareNfcApduRunnerMock(nfcApduRunner);
        IsoDep tag = prepareAdvancedTagMock();
        short recoveryDataLen = (short) Math.abs(random.nextInt(RECOVERY_DATA_MAX_SIZE) + 1);
        byte[] lenBytes = new byte[2];
        BYTE_ARRAY_HELPER.setShort(lenBytes, 0x00, recoveryDataLen);
        when(tag.transceive(GET_RECOVERY_DATA_LEN_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bConcat(lenBytes, SW_SUCCESS));
        when(tag.transceive(RESET_RECOVERY_DATA_APDU.getBytes())).thenReturn(SW_SUCCESS);
        when(tag.transceive(IS_RECOVERY_DATA_SET_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bConcat(new byte[]{0x01}, SW_SUCCESS));
        byte[] recoveryDataHashBytes = new byte[SHA_HASH_SIZE];
        random.nextBytes(recoveryDataHashBytes);
        when(tag.transceive(GET_RECOVERY_DATA_HASH_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bConcat(recoveryDataHashBytes, SW_SUCCESS));
        nfcApduRunnerMock.setCardTag(tag);
        recoveryDataApi.setApduRunner(nfcApduRunnerMock);
        for(int i = 0 ; i < cardOperationsListShort.size(); i++) {
            String msg = i == 0 ? DONE_MSG :
                    i == 1 ? BYTE_ARRAY_HELPER.hex(recoveryDataHashBytes) :
                    i == 2 ? Short.toString(recoveryDataLen) : TRUE_MSG;
            String response = cardOperationsListShort.get(i).accept(Collections.emptyList());
            System.out.println(response.toLowerCase());
            assertEquals(response.toLowerCase(), JSON_HELPER.createResponseJson(msg).toLowerCase());
        }
    }

    @Test
    public void addRecoveryDataTestAppletSuccessfullOperations() throws Exception {
        NfcApduRunner nfcApduRunnerMock = prepareNfcApduRunnerMock(nfcApduRunner);
        IsoDep tag = prepareAdvancedTagMock();
        short tailLen = 50;
        int portionsNum = 2;
        short recoveryDataLen = (short) (portionsNum * DATA_RECOVERY_PORTION_MAX_SIZE + tailLen);
        String recoveryData = STRING_HELPER.randomHexString(2 * recoveryDataLen);
        byte[] recoveryDataBytes = BYTE_ARRAY_HELPER.bytes(recoveryData);
        for (int i = 0; i < portionsNum + 1; i++) {
            short start = (short)(i * DATA_RECOVERY_PORTION_MAX_SIZE);
            short len = i == portionsNum ? tailLen : DATA_RECOVERY_PORTION_MAX_SIZE;
            byte[] portion = BYTE_ARRAY_HELPER.bSub(recoveryDataBytes, start, len);
            byte p1 = i == 0 ? (byte) 0x00 : (byte) 0x01;
            when(tag.transceive(getAddRecoveryDataPartAPDU(p1, portion).getBytes())).thenReturn(SW_SUCCESS);
        }
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(recoveryDataBytes);
        when(tag.transceive(getAddRecoveryDataPartAPDU((byte) 0x02, hash).getBytes())).thenReturn(SW_SUCCESS);
        nfcApduRunnerMock.setCardTag(tag);
        recoveryDataApi.setApduRunner(nfcApduRunnerMock);
        String response = recoveryDataApi.addRecoveryDataAndGetJson(recoveryData);
        System.out.println(response.toLowerCase());
        assertEquals(response.toLowerCase(), JSON_HELPER.createResponseJson(DONE_MSG).toLowerCase());
    }

    @Test
    public void getRecoveryDataTestAppletSuccessfullOperations1() throws Exception {
        NfcApduRunner nfcApduRunnerMock = prepareNfcApduRunnerMock(nfcApduRunner);
        IsoDep tag = prepareAdvancedTagMock();
        short tailLen = 20;
        int portionsNum = 8;
        short recoveryDataLen = (short) (portionsNum * DATA_RECOVERY_PORTION_MAX_SIZE + tailLen);
        byte[] recoveryDataLenBytes = new byte[2];
        BYTE_ARRAY_HELPER.setShort(recoveryDataLenBytes, (short) 0, recoveryDataLen);
        String recoveryData = STRING_HELPER.randomHexString(2 * recoveryDataLen);
        byte[] recoveryDataBytes = BYTE_ARRAY_HELPER.bytes(recoveryData);
        when(tag.transceive(GET_RECOVERY_DATA_LEN_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bConcat(recoveryDataLenBytes, SW_SUCCESS));
        for (int i = 0; i < portionsNum + 1; i++) {
            short start = (short)(i * DATA_RECOVERY_PORTION_MAX_SIZE);
            short len = i == portionsNum ? tailLen : DATA_RECOVERY_PORTION_MAX_SIZE;
            byte[] portion = BYTE_ARRAY_HELPER.bSub(recoveryDataBytes, start, len);
            byte[] startBytes = new byte[2];
            BYTE_ARRAY_HELPER.setShort(startBytes, (short) 0, start);
            System.out.println(BYTE_ARRAY_HELPER.hex(startBytes));
            when(tag.transceive(getGetRecoveryDataPartAPDU(startBytes, (byte)len).getBytes())).thenReturn(BYTE_ARRAY_HELPER.bConcat(portion, BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS)));
        }
        nfcApduRunnerMock.setCardTag(tag);
        recoveryDataApi.setApduRunner(nfcApduRunnerMock);
        String response = recoveryDataApi.getRecoveryDataAndGetJson();
        System.out.println(response.toLowerCase());
        assertEquals(response.toLowerCase(), JSON_HELPER.createResponseJson(recoveryData).toLowerCase());
    }

    @Test
    public void getRecoveryDataTestAppletSuccessfullOperations2() throws Exception {
        NfcApduRunner nfcApduRunnerMock = prepareNfcApduRunnerMock(nfcApduRunner);
        IsoDep tag = prepareAdvancedTagMock();
        short recoveryDataLen = 20;
        byte[] recoveryDataLenBytes = new byte[2];
        BYTE_ARRAY_HELPER.setShort(recoveryDataLenBytes, (short) 0, recoveryDataLen);
        String recoveryData = STRING_HELPER.randomHexString(2 * recoveryDataLen);
        when(tag.transceive(GET_RECOVERY_DATA_LEN_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bConcat(recoveryDataLenBytes, SW_SUCCESS));
        byte[] startBytes = new byte[2];
        when(tag.transceive(getGetRecoveryDataPartAPDU(startBytes, (byte) recoveryDataLen).getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(recoveryData + BYTE_ARRAY_HELPER.hex(ErrorCodes.SW_SUCCESS)));
        nfcApduRunnerMock.setCardTag(tag);
        recoveryDataApi.setApduRunner(nfcApduRunnerMock);
        String response = recoveryDataApi.getRecoveryDataAndGetJson();
        System.out.println(response.toLowerCase());
        assertEquals(response.toLowerCase(), JSON_HELPER.createResponseJson(recoveryData).toLowerCase());
    }

    /** Test for applet fail with some SW**/

    @Test
    public void testAppletFailedOperation() throws Exception{
        List<CardApiInterface<List<String>>> cardOperationsListShort = Arrays.asList(addRecoveryData, resetRecovery,  getRecoveryDataHash, getRecoveryDataLen,
                isRecoveryDataSet, getRecoveryData);
        NfcApduRunner nfcApduRunnerMock = prepareNfcApduRunnerMock(nfcApduRunner);
        IsoDep tag = prepareAdvancedTagMock();
        short recoveryDataLen = 100;
        byte[] recoveryDataLenBytes = new byte[2];
        BYTE_ARRAY_HELPER.setShort(recoveryDataLenBytes, (short) 0, recoveryDataLen);
        String recoveryData = STRING_HELPER.randomHexString(2 * recoveryDataLen);
        byte[] recoveryDataBytes = BYTE_ARRAY_HELPER.bytes(recoveryData);
        RAPDU rapdu = new RAPDU(BYTE_ARRAY_HELPER.hex(ErrorCodes.SW_INS_NOT_SUPPORTED));
        when(tag.transceive(GET_RECOVERY_DATA_LEN_APDU.getBytes()))
                .thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_INS_NOT_SUPPORTED))
                .thenReturn(BYTE_ARRAY_HELPER.bConcat(recoveryDataLenBytes, BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS)));
        when(tag.transceive(RESET_RECOVERY_DATA_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_INS_NOT_SUPPORTED));
        when(tag.transceive(IS_RECOVERY_DATA_SET_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_INS_NOT_SUPPORTED));
        when(tag.transceive(GET_RECOVERY_DATA_HASH_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_INS_NOT_SUPPORTED));
        when(tag.transceive(getAddRecoveryDataPartAPDU((byte) 0x00, recoveryDataBytes).getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_INS_NOT_SUPPORTED));
        byte[] startBytes = new byte[2];
        when(tag.transceive(getGetRecoveryDataPartAPDU(startBytes, (byte) recoveryDataLen).getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_INS_NOT_SUPPORTED));
        nfcApduRunnerMock.setCardTag(tag);
        recoveryDataApi.setApduRunner(nfcApduRunnerMock);
        for(int i = 0 ; i < cardOperationsListShort.size(); i++) {
            try {
                cardOperationsListShort.get(i).accept(i == 0 ? Collections.singletonList(recoveryData)
                        : Collections.emptyList());
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

    private IsoDep prepareAdvancedTagMock() throws Exception {
        IsoDep tag = prepareTagMock();
        when(tag.transceive(SELECT_TON_WALLET_APPLET_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS));
        when(tag.transceive(GET_APP_INFO_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bConcat(new byte[]{PERSONALIZED_STATE}, BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS)));
        return tag;
    }
}