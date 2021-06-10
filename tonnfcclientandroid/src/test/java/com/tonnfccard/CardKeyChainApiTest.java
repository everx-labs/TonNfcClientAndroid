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
import com.tonnfccard.smartcard.TonWalletAppletApduCommands;
import com.tonnfccard.smartcard.TonWalletAppletStates;
import com.tonnfccard.utils.ByteArrayUtil;

import org.checkerframework.checker.units.qual.A;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.internal.DoNotInstrument;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static com.tonnfccard.NfcMockHelper.createSault;
import static com.tonnfccard.NfcMockHelper.mockAndroidKeyStore;
import static com.tonnfccard.NfcMockHelper.prepareAdvancedTagMock;
import static com.tonnfccard.NfcMockHelper.prepareHmacHelperMock;
import static com.tonnfccard.NfcMockHelper.prepareNfcApduRunnerMock;
import static com.tonnfccard.NfcMockHelper.prepareTagMock;
import static com.tonnfccard.NfcMockHelper.SW_SUCCESS;
import static com.tonnfccard.TonWalletApi.BYTE_ARR_HELPER;
import static com.tonnfccard.TonWalletApi.STR_HELPER;
import static com.tonnfccard.TonWalletConstants.DATA_FOR_SIGNING_MAX_SIZE;
import static com.tonnfccard.TonWalletConstants.DATA_PORTION_MAX_SIZE;
import static com.tonnfccard.TonWalletConstants.DEFAULT_PIN_STR;
import static com.tonnfccard.TonWalletConstants.DELETE_KEY_FROM_KEYCHAIN_STATE;
import static com.tonnfccard.TonWalletConstants.DONE_MSG;
import static com.tonnfccard.TonWalletConstants.FREE_SIZE_FIELD;
import static com.tonnfccard.TonWalletConstants.HMAC_SHA_SIG_SIZE;
import static com.tonnfccard.TonWalletConstants.KEY_HMAC_FIELD;
import static com.tonnfccard.TonWalletConstants.KEY_INDEX_FIELD;
import static com.tonnfccard.TonWalletConstants.KEY_LENGTH_FIELD;
import static com.tonnfccard.TonWalletConstants.MAX_KEY_SIZE_IN_KEYCHAIN;
import static com.tonnfccard.TonWalletConstants.MAX_NUMBER_OF_KEYS_IN_KEYCHAIN;
import static com.tonnfccard.TonWalletConstants.MESSAGE_FIELD;
import static com.tonnfccard.TonWalletConstants.NUMBER_OF_KEYS_FIELD;
import static com.tonnfccard.TonWalletConstants.OCCUPIED_SIZE_FIELD;
import static com.tonnfccard.TonWalletConstants.PASSWORD_SIZE;
import static com.tonnfccard.TonWalletConstants.PERSONALIZED_STATE;
import static com.tonnfccard.TonWalletConstants.PUBLIC_KEY_LEN;
import static com.tonnfccard.TonWalletConstants.SAULT_LENGTH;
import static com.tonnfccard.TonWalletConstants.SHA_HASH_SIZE;
import static com.tonnfccard.TonWalletConstants.STATUS_FIELD;
import static com.tonnfccard.TonWalletConstants.SUCCESS_STATUS;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_KEY_DATA_PORTION_INCORRECT_LEN;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_APPLET_DOES_NOT_WAIT_TO_DELETE_KEY;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_APPLET_IS_NOT_PERSONALIZED;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_DATA_FOR_SIGNING_LEN_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_DATA_FOR_SIGNING_NOT_HEX;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_DELETE_KEY_CHUNK_RESPONSE_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_DELETE_KEY_CHUNK_RESPONSE_LEN_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_DELETE_KEY_RECORD_RESPONSE_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_DELETE_KEY_RECORD_RESPONSE_LEN_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_FREE_SIZE_RESPONSE_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_GET_DELETE_KEY_CHUNK_NUM_OF_PACKETS_RESPONSE_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_GET_DELETE_KEY_CHUNK_NUM_OF_PACKETS_RESPONSE_LEN_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_GET_DELETE_KEY_RECORD_NUM_OF_PACKETS_RESPONSE_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_GET_DELETE_KEY_RECORD_NUM_OF_PACKETS_RESPONSE_LEN_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_GET_FREE_SIZE_RESPONSE_LEN_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_GET_HMAC_RESPONSE_LEN_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_GET_KEY_INDEX_IN_STORAGE_AND_LEN_RESPONSE_LEN_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_GET_NUMBER_OF_KEYS_RESPONSE_LEN_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_GET_OCCUPIED_SIZE_RESPONSE_LEN_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_KEY_HMAC_LEN_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_KEY_HMAC_NOT_HEX;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_KEY_INDEX_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_KEY_INDEX_STRING_NOT_NUMERIC;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_KEY_INDEX_VALUE_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_KEY_LENGTH_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_KEY_LEN_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_KEY_NOT_HEX;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_KEY_SIZE_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_NEW_KEY_LEN_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_NUMBER_OF_KEYS_RESPONSE_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_NUM_OF_KEYS_INCORRECT_AFTER_ADD;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_NUM_OF_KEYS_INCORRECT_AFTER_CHANGE;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_OCCUPIED_SIZE_RESPONSE_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_PASSWORD_LEN_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_PASSWORD_NOT_HEX;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_PUBLIC_KEY_RESPONSE_LEN_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_SEND_CHUNK_RESPONSE_LEN_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_SIG_RESPONSE_LEN_INCORRECT;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.DELETE_KEY_CHUNK_LE;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.DELETE_KEY_RECORD_LE;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.GET_APP_INFO_APDU;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.GET_DELETE_KEY_CHUNK_NUM_OF_PACKETS_LE;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.GET_FREE_SIZE_LE;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.GET_KEY_INDEX_IN_STORAGE_AND_LEN_LE;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.GET_PUB_KEY_WITH_DEFAULT_PATH_APDU;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.GET_SAULT_APDU;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.GET_SERIAL_NUMBER_APDU;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.INS_ADD_KEY_CHUNK;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.INS_CHANGE_KEY_CHUNK;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.SELECT_TON_WALLET_APPLET_APDU;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.SEND_CHUNK_LE;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.getCheckAvailableVolForNewKeyAPDU;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.getCheckKeyHmacConsistencyAPDU;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.getDeleteKeyChunkAPDU;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.getDeleteKeyChunkNumOfPacketsAPDU;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.getDeleteKeyRecordAPDU;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.getDeleteKeyRecordNumOfPacketsAPDU;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.getGetFreeSizeAPDU;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.getGetHmacAPDU;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.getGetIndexAndLenOfKeyInKeyChainAPDU;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.getGetKeyChunkAPDU;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.getGetOccupiedSizeAPDU;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.getInitiateChangeOfKeyAPDU;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.getInitiateDeleteOfKeyAPDU;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.getNumberOfKeysAPDU;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.getPublicKeyAPDU;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.getResetKeyChainAPDU;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.getSendKeyChunkAPDU;
import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = Build.VERSION_CODES.P)
@DoNotInstrument
public class CardKeyChainApiTest {
    private static final ExceptionHelper EXCEPTION_HELPER = ExceptionHelper.getInstance();
    private static final StringHelper STRING_HELPER = StringHelper.getInstance();
    private static final ByteArrayUtil BYTE_ARRAY_HELPER = ByteArrayUtil.getInstance();
    private static final JsonHelper JSON_HELPER = JsonHelper.getInstance();
    private static final HmacHelper HMAC_HELPER = HmacHelper.getInstance();
    private CardKeyChainApi cardKeyChainApi;
    private NfcApduRunner nfcApduRunner;
    private final Random random = new Random();

    private final CardApiInterface<List<String>> getFreeStorageSize = list -> cardKeyChainApi.getFreeStorageSizeAndGetJson();
    private final CardApiInterface<List<String>> getOccupiedStorageSize = list -> cardKeyChainApi.getOccupiedStorageSizeAndGetJson();
    private final CardApiInterface<List<String>> getNumberOfKeys = list -> cardKeyChainApi.getNumberOfKeysAndGetJson();
    private final CardApiInterface<List<String>> getHmac = list -> cardKeyChainApi.getHmacAndGetJson(list.get(0));
    private final CardApiInterface<List<String>> addKeyIntoKeyChain = list -> cardKeyChainApi.addKeyIntoKeyChainAndGetJson(list.get(0));
    private final CardApiInterface<List<String>> changeKeyInKeyChain = list -> cardKeyChainApi.changeKeyInKeyChainAndGetJson(list.get(0), list.get(1));
    private final CardApiInterface<List<String>> checkKeyHmacConsistency = list -> cardKeyChainApi.checkKeyHmacConsistencyAndGetJson(list.get(0));
    private final CardApiInterface<List<String>> deleteKeyFromKeyChain = list -> cardKeyChainApi.deleteKeyFromKeyChainAndGetJson(list.get(0));
    private final CardApiInterface<List<String>> finishDeleteKeyFromKeyChainAfterInterruption = list -> cardKeyChainApi.finishDeleteKeyFromKeyChainAfterInterruptionAndGetJson();
    private final CardApiInterface<List<String>> getDeleteKeyChunkNumOfPackets = list -> cardKeyChainApi.getDeleteKeyChunkNumOfPacketsAndGetJson();
    private final CardApiInterface<List<String>> getDeleteKeyRecordNumOfPackets = list -> cardKeyChainApi.getDeleteKeyRecordNumOfPacketsAndGetJson();
    private final CardApiInterface<List<String>> getIndexAndLenOfKeyInKeyChain = list -> cardKeyChainApi.getIndexAndLenOfKeyInKeyChainAndGetJson(list.get(0));
    private final CardApiInterface<List<String>> getKeyChainDataAboutAllKeys = list -> cardKeyChainApi.getKeyChainDataAboutAllKeysAndGetJson();
    private final CardApiInterface<List<String>> getKeyChainInfo = list -> cardKeyChainApi.getKeyChainInfoAndGetJson();
    private final CardApiInterface<List<String>> getKeyFromKeyChain = list -> cardKeyChainApi.getKeyFromKeyChainAndGetJson(list.get(0));
    private final CardApiInterface<List<String>> resetKeyChain = list -> cardKeyChainApi.resetKeyChainAndGetJson();
    private final CardApiInterface<List<String>> checkAvailableVolForNewKey = list -> cardKeyChainApi.checkAvailableVolForNewKeyAndGetJson(Short.parseShort(list.get(0)));


    @Before
    public void init() throws Exception {
        Context context = ApplicationProvider.getApplicationContext();
        nfcApduRunner = NfcApduRunner.getInstance(context);
        cardKeyChainApi = new CardKeyChainApi(context, nfcApduRunner);
    }

    /**
     * Test for successfull response from applet
     **/

    @Test
    public void testAppletSuccessfullOperations() throws Exception {
        Map<CardApiInterface<List<String>>, String> opToMsg = new LinkedHashMap<>();
        opToMsg.put(checkKeyHmacConsistency, DONE_MSG);
        opToMsg.put(checkAvailableVolForNewKey, DONE_MSG);
        opToMsg.put(resetKeyChain, DONE_MSG);
        opToMsg.put(getFreeStorageSize, "32767");
        opToMsg.put(getOccupiedStorageSize, "32");
        opToMsg.put(getNumberOfKeys, "1023");
        opToMsg.put(getDeleteKeyChunkNumOfPackets, "1");
        opToMsg.put(getDeleteKeyRecordNumOfPackets, "10");
        byte[] sault = createSault();
        NfcApduRunner nfcApduRunnerMock = prepareNfcApduRunnerMock(nfcApduRunner);
        TonWalletAppletApduCommands.setHmacHelper(prepareHmacHelperMock(HMAC_HELPER));
        IsoDep tag = prepareAdvancedTagMock(sault, PERSONALIZED_STATE);
        when(tag.transceive(getResetKeyChainAPDU(sault).getBytes())).thenReturn(SW_SUCCESS);
        when(tag.transceive(getGetFreeSizeAPDU(sault).getBytes())).thenReturn(BYTE_ARRAY_HELPER.bConcat(new byte[]{(byte) 0x7f, (byte) 0xff}, SW_SUCCESS));
        when(tag.transceive(getGetOccupiedSizeAPDU(sault).getBytes())).thenReturn(BYTE_ARRAY_HELPER.bConcat(new byte[]{(byte) 0x00, (byte) 0x20}, SW_SUCCESS));
        when(tag.transceive(getNumberOfKeysAPDU(sault).getBytes())).thenReturn(BYTE_ARRAY_HELPER.bConcat(new byte[]{(byte) 0x03, (byte) 0xFF}, SW_SUCCESS));
        when(tag.transceive(getDeleteKeyChunkNumOfPacketsAPDU(sault).getBytes())).thenReturn(BYTE_ARRAY_HELPER.bConcat(new byte[]{(byte) 0x00, (byte) 0x01}, SW_SUCCESS));
        when(tag.transceive(getDeleteKeyRecordNumOfPacketsAPDU(sault).getBytes())).thenReturn(BYTE_ARRAY_HELPER.bConcat(new byte[]{(byte) 0x00, (byte) 0x0A}, SW_SUCCESS));
        short keySize = (short) 1024;
        when(tag.transceive(getCheckAvailableVolForNewKeyAPDU(keySize, sault).getBytes())).thenReturn(SW_SUCCESS);
        String mac = STRING_HELPER.randomHexString(2 * SHA_HASH_SIZE);
        when(tag.transceive(getCheckKeyHmacConsistencyAPDU(BYTE_ARRAY_HELPER.bytes(mac), sault).getBytes())).thenReturn(SW_SUCCESS);
        nfcApduRunnerMock.setCardTag(tag);
        cardKeyChainApi.setApduRunner(nfcApduRunnerMock);
        mockAndroidKeyStore();
        int i = 0;
        for (CardApiInterface<List<String>> op : opToMsg.keySet()) {
            try {
                if (i >= 6) {
                    when(tag.transceive(GET_APP_INFO_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bConcat(new byte[]{DELETE_KEY_FROM_KEYCHAIN_STATE}, BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS)));
                }
                String response = op.accept(i == 0 ? Collections.singletonList(mac) :
                        i == 1? Collections.singletonList(String.valueOf(keySize)) : Collections.emptyList());
                System.out.println(response);
                assertEquals(response.toLowerCase(), JSON_HELPER.createResponseJson(opToMsg.get(op)).toLowerCase());
                i++;
            } catch (Exception e) {
                System.out.println(e.getMessage());
                fail();
            }
        }
    }

    @Test
    public void testGetKeyChainInfoAppletSuccessfullOperation() throws Exception {
        byte[] sault = createSault();
        NfcApduRunner nfcApduRunnerMock = prepareNfcApduRunnerMock(nfcApduRunner);
        TonWalletAppletApduCommands.setHmacHelper(prepareHmacHelperMock(HMAC_HELPER));
        IsoDep tag = prepareAdvancedTagMock(sault, PERSONALIZED_STATE);
        when(tag.transceive(getGetFreeSizeAPDU(sault).getBytes())).thenReturn(BYTE_ARRAY_HELPER.bConcat(new byte[]{(byte) 0x7f, (byte) 0xff}, SW_SUCCESS));
        when(tag.transceive(getGetOccupiedSizeAPDU(sault).getBytes())).thenReturn(BYTE_ARRAY_HELPER.bConcat(new byte[]{(byte) 0x00, (byte) 0x20}, SW_SUCCESS));
        when(tag.transceive(getNumberOfKeysAPDU(sault).getBytes())).thenReturn(BYTE_ARRAY_HELPER.bConcat(new byte[]{(byte) 0x03, (byte) 0xFF}, SW_SUCCESS));
        nfcApduRunnerMock.setCardTag(tag);
        cardKeyChainApi.setApduRunner(nfcApduRunnerMock);
        mockAndroidKeyStore();
        try {
            String response = cardKeyChainApi.getKeyChainInfoAndGetJson();
            System.out.println(response);
            JSONObject obj = new JSONObject(response);
            assertEquals(obj.get(TonWalletConstants.STATUS_FIELD), SUCCESS_STATUS);
            assertEquals(obj.get(NUMBER_OF_KEYS_FIELD), 1023);
            assertEquals(obj.get(OCCUPIED_SIZE_FIELD), 32);
            assertEquals(obj.get(FREE_SIZE_FIELD), 32767);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            fail();
        }
    }

    @Test
    public void testGetHmacAppletSuccessfullOperation() throws Exception {
        String keyHmac = STRING_HELPER.randomHexString(2 * SHA_HASH_SIZE);
        byte len = 5;
        byte[] sault = createSault();
        String keyIndex = "1";
        byte[] ind = new byte[]{0, 1};
        NfcApduRunner nfcApduRunnerMock = prepareNfcApduRunnerMock(nfcApduRunner);
        TonWalletAppletApduCommands.setHmacHelper(prepareHmacHelperMock(HMAC_HELPER));
        IsoDep tag = prepareAdvancedTagMock(sault, PERSONALIZED_STATE);
        when(tag.transceive(getGetHmacAPDU(ind, sault).getBytes()))
                .thenReturn(BYTE_ARRAY_HELPER.bConcat(BYTE_ARRAY_HELPER.bytes(keyHmac), new byte[]{0, len}, SW_SUCCESS));
        nfcApduRunnerMock.setCardTag(tag);
        cardKeyChainApi.setApduRunner(nfcApduRunnerMock);
        mockAndroidKeyStore();
        try {
            String response = cardKeyChainApi.getHmacAndGetJson(keyIndex);
            System.out.println(response);
            JSONObject obj = new JSONObject(response);
            assertEquals(obj.get(TonWalletConstants.STATUS_FIELD), SUCCESS_STATUS);
            assertEquals(obj.getString(KEY_HMAC_FIELD).toLowerCase(), keyHmac.toLowerCase());
            assertEquals(obj.getInt(KEY_LENGTH_FIELD), len);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            fail();
        }
    }

    @Test
    public void testGetIndexAndLenOfKeyInKeyChainAppletSuccessfullOperation() throws Exception {
        String keyHmac = STRING_HELPER.randomHexString(2 * SHA_HASH_SIZE);
        byte[] sault = createSault();
        NfcApduRunner nfcApduRunnerMock = prepareNfcApduRunnerMock(nfcApduRunner);
        TonWalletAppletApduCommands.setHmacHelper(prepareHmacHelperMock(HMAC_HELPER));
        IsoDep tag = prepareAdvancedTagMock(sault, PERSONALIZED_STATE);
        when(tag.transceive(getGetIndexAndLenOfKeyInKeyChainAPDU(BYTE_ARRAY_HELPER.bytes(keyHmac), sault).getBytes()))
                .thenReturn(BYTE_ARRAY_HELPER.bConcat(new byte[]{(byte) 0x00, (byte) 0x01, 0x00, 0x20}, SW_SUCCESS));
        nfcApduRunnerMock.setCardTag(tag);
        cardKeyChainApi.setApduRunner(nfcApduRunnerMock);
        mockAndroidKeyStore();
        try {
            String response = cardKeyChainApi.getIndexAndLenOfKeyInKeyChainAndGetJson(keyHmac);
            System.out.println(response);
            JSONObject obj = new JSONObject(response);
            assertEquals(obj.get(TonWalletConstants.STATUS_FIELD), SUCCESS_STATUS);
            JSONObject obj1 = new JSONObject(obj.getString(MESSAGE_FIELD));
            assertEquals(obj1.get(KEY_INDEX_FIELD), 1);
            assertEquals(obj1.get(KEY_LENGTH_FIELD), 32);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            fail();
        }
    }

    @Test
    public void testGetKeyChainDataAboutAllKeysAppletSuccessfullOperation() throws Exception {
        byte[] hmac1 = new byte[SHA_HASH_SIZE];
        byte[] hmac2 = new byte[SHA_HASH_SIZE];
        random.nextBytes(hmac1);
        random.nextBytes(hmac2);
        byte[] sault = createSault();
        byte len1 = 2;
        byte len2 = 100;
        NfcApduRunner nfcApduRunnerMock = prepareNfcApduRunnerMock(nfcApduRunner);
        TonWalletAppletApduCommands.setHmacHelper(prepareHmacHelperMock(HMAC_HELPER));
        IsoDep tag = prepareAdvancedTagMock(sault, PERSONALIZED_STATE);
        when(tag.transceive(getNumberOfKeysAPDU(sault).getBytes())).thenReturn(BYTE_ARRAY_HELPER.bConcat(new byte[]{(byte) 0x00, (byte) 0x02}, SW_SUCCESS));
        when(tag.transceive(getGetHmacAPDU(new byte[]{0x00, 0x00}, sault).getBytes()))
                .thenReturn(BYTE_ARRAY_HELPER.bConcat(hmac1, new byte[]{0x00, len1}, SW_SUCCESS));
        when(tag.transceive(getGetHmacAPDU(new byte[]{0x00, 0x01}, sault).getBytes()))
                .thenReturn(BYTE_ARRAY_HELPER.bConcat(hmac2, new byte[]{0x00, len2}, SW_SUCCESS));
        nfcApduRunnerMock.setCardTag(tag);
        cardKeyChainApi.setApduRunner(nfcApduRunnerMock);
        mockAndroidKeyStore();
        try {
            String response = cardKeyChainApi.getKeyChainDataAboutAllKeysAndGetJson();
            System.out.println(response);
            JSONObject obj = new JSONObject(response);
            assertEquals(obj.get(TonWalletConstants.STATUS_FIELD), SUCCESS_STATUS);
            JSONArray arr = obj.getJSONArray(MESSAGE_FIELD);
            System.out.println(arr);
            assertEquals(arr.getJSONObject(0).get(KEY_HMAC_FIELD), BYTE_ARRAY_HELPER.hex(hmac1));
            assertEquals(arr.getJSONObject(0).get(KEY_LENGTH_FIELD), Byte.valueOf(len1).toString());
            assertEquals(arr.getJSONObject(1).get(KEY_HMAC_FIELD), BYTE_ARRAY_HELPER.hex(hmac2));
            assertEquals(arr.getJSONObject(1).get(KEY_LENGTH_FIELD), Byte.valueOf(len2).toString());
        } catch (Exception e) {
            System.out.println(e.getMessage());
            fail();
        }
    }

    @Test
    public void testGetKeyFromKeyChainAppletSuccessfullOperation1() throws Exception {
        String keyHmac = STRING_HELPER.randomHexString(2 * SHA_HASH_SIZE);
        byte[] sault = createSault();
        short len = 550;
        byte[] ind = new byte[]{(byte) 0x00, (byte) 0x01};
        short tailLen = (short) (len % DATA_PORTION_MAX_SIZE);
        NfcApduRunner nfcApduRunnerMock = prepareNfcApduRunnerMock(nfcApduRunner);
        TonWalletAppletApduCommands.setHmacHelper(prepareHmacHelperMock(HMAC_HELPER));
        IsoDep tag = prepareAdvancedTagMock(sault, PERSONALIZED_STATE);
        when(tag.transceive(getGetIndexAndLenOfKeyInKeyChainAPDU(BYTE_ARRAY_HELPER.bytes(keyHmac), sault).getBytes()))
                .thenReturn(BYTE_ARRAY_HELPER.bConcat(ind, new byte[]{0x02, 0x26}, SW_SUCCESS));
        byte[] key = new byte[len];
        random.nextBytes(key);
        for(int i = 0 ; i <= 4 ; i++) {
            int portionLen = i == 4 ?  tailLen :  DATA_PORTION_MAX_SIZE;
            short start = (short) (i * DATA_PORTION_MAX_SIZE);
            byte[] portion = BYTE_ARRAY_HELPER.bSub(key, start, portionLen);
            when(tag.transceive(getGetKeyChunkAPDU(ind, start, sault, (byte) portionLen).getBytes()))
                    .thenReturn(BYTE_ARRAY_HELPER.bConcat(portion, SW_SUCCESS));
        }
        nfcApduRunnerMock.setCardTag(tag);
        cardKeyChainApi.setApduRunner(nfcApduRunnerMock);
        mockAndroidKeyStore();
        try {
            String response = cardKeyChainApi.getKeyFromKeyChainAndGetJson(keyHmac);
            System.out.println(response);
            assertEquals(response.toLowerCase(), JSON_HELPER.createResponseJson(BYTE_ARRAY_HELPER.hex(key)).toLowerCase());
        } catch (Exception e) {
            System.out.println(e.getMessage());
            fail();
        }
    }

    @Test
    public void testGetKeyFromKeyChainAppletSuccessfullOperation2() throws Exception {
        String keyHmac = STRING_HELPER.randomHexString(2 * SHA_HASH_SIZE);
        byte[] sault = createSault();
        short len = 120;
        byte[] ind = new byte[]{(byte) 0x00, (byte) 0x01};
        NfcApduRunner nfcApduRunnerMock = prepareNfcApduRunnerMock(nfcApduRunner);
        TonWalletAppletApduCommands.setHmacHelper(prepareHmacHelperMock(HMAC_HELPER));
        IsoDep tag = prepareAdvancedTagMock(sault, PERSONALIZED_STATE);
        when(tag.transceive(getGetIndexAndLenOfKeyInKeyChainAPDU(BYTE_ARRAY_HELPER.bytes(keyHmac), sault).getBytes()))
                .thenReturn(BYTE_ARRAY_HELPER.bConcat(ind, new byte[]{0x00, (byte) 120}, SW_SUCCESS));
        byte[] key = new byte[len];
        random.nextBytes(key);
        when(tag.transceive(getGetKeyChunkAPDU(ind, (short) 0, sault, (byte) len).getBytes()))
                .thenReturn(BYTE_ARRAY_HELPER.bConcat(key, SW_SUCCESS));
        nfcApduRunnerMock.setCardTag(tag);
        cardKeyChainApi.setApduRunner(nfcApduRunnerMock);
        mockAndroidKeyStore();
        try {
            String response = cardKeyChainApi.getKeyFromKeyChainAndGetJson(keyHmac);
            System.out.println(response);
            assertEquals(response.toLowerCase(), JSON_HELPER.createResponseJson(BYTE_ARRAY_HELPER.hex(key)).toLowerCase());
        } catch (Exception e) {
            System.out.println(e.getMessage());
            fail();
        }
    }

    @Test
    public void testAddKeyIntoKeyChainAppletSuccessfullOperation() throws Exception {
        byte[] sault = createSault();
        short len = 550;
        short tailLen = (short) (len % DATA_PORTION_MAX_SIZE);
        NfcApduRunner nfcApduRunnerMock = prepareNfcApduRunnerMock(nfcApduRunner);
        HmacHelper hmacHelperMock = prepareHmacHelperMock(HMAC_HELPER);
        TonWalletAppletApduCommands.setHmacHelper(hmacHelperMock);
        TonWalletApi.setHmacHelper(hmacHelperMock);
        IsoDep tag = prepareAdvancedTagMock(sault, PERSONALIZED_STATE);
        when(tag.transceive(getCheckAvailableVolForNewKeyAPDU(len, sault).getBytes())).thenReturn(SW_SUCCESS);
        when(tag.transceive(getNumberOfKeysAPDU(sault).getBytes())).thenReturn(BYTE_ARRAY_HELPER.bConcat(new byte[]{(byte) 0x00, (byte) 0x02}, SW_SUCCESS));
        byte[] key = new byte[len];
        random.nextBytes(key);
        for(int i = 0 ; i <= 4 ; i++) {
            int portionLen = i == 4 ?  tailLen :  DATA_PORTION_MAX_SIZE;
            short start = (short) (i * DATA_PORTION_MAX_SIZE);
            byte[] portion = BYTE_ARRAY_HELPER.bSub(key, start, portionLen);
            byte p1 = i == 0 ? (byte) 0x00 : (byte) 0x01;
            when(tag.transceive(getSendKeyChunkAPDU(INS_ADD_KEY_CHUNK, p1, portion, sault).getBytes()))
                    .thenReturn(SW_SUCCESS);
        }
        byte[] mac = hmacHelperMock.computeMac(key);
        when(tag.transceive(getSendKeyChunkAPDU(INS_ADD_KEY_CHUNK, (byte) 0x02, mac, sault).getBytes()))
                .thenReturn(BYTE_ARRAY_HELPER.bConcat(new byte[]{(byte) 0x00, (byte) 0x03}, SW_SUCCESS));
        nfcApduRunnerMock.setCardTag(tag);
        cardKeyChainApi.setApduRunner(nfcApduRunnerMock);
        mockAndroidKeyStore();
        try {
            String response = cardKeyChainApi.addKeyIntoKeyChainAndGetJson(BYTE_ARRAY_HELPER.hex(key));
            System.out.println(response);
            assertEquals(response.toLowerCase(), JSON_HELPER.createResponseJson(BYTE_ARRAY_HELPER.hex(mac)).toLowerCase());
        } catch (Exception e) {
            System.out.println(e.getMessage());
            fail();
        }
    }

    @Test
    public void testChangeKeyInKeyChainAppletSuccessfullOperation() throws Exception {
        String oldHmac = STRING_HELPER.randomHexString(2 * SHA_HASH_SIZE);
        byte[] ind = new byte[]{(byte) 0x00, (byte) 0x01};
        byte[] sault = createSault();
        short len = 550;
        short tailLen = (short) (len % DATA_PORTION_MAX_SIZE);
        NfcApduRunner nfcApduRunnerMock = prepareNfcApduRunnerMock(nfcApduRunner);
        HmacHelper hmacHelperMock = prepareHmacHelperMock(HMAC_HELPER);
        TonWalletAppletApduCommands.setHmacHelper(hmacHelperMock);
        TonWalletApi.setHmacHelper(hmacHelperMock);
        IsoDep tag = prepareAdvancedTagMock(sault, PERSONALIZED_STATE);
        when(tag.transceive(getGetIndexAndLenOfKeyInKeyChainAPDU(BYTE_ARRAY_HELPER.bytes(oldHmac), sault).getBytes()))
                .thenReturn(BYTE_ARRAY_HELPER.bConcat(ind, new byte[]{0x02, 0x26}, SW_SUCCESS));
        when(tag.transceive(getInitiateChangeOfKeyAPDU(ind, sault).getBytes()))
                .thenReturn(SW_SUCCESS);
        when(tag.transceive(getNumberOfKeysAPDU(sault).getBytes())).thenReturn(BYTE_ARRAY_HELPER.bConcat(new byte[]{(byte) 0x00, (byte) 0x02}, SW_SUCCESS));
        byte[] key = new byte[len];
        random.nextBytes(key);
        for(int i = 0 ; i <= 4 ; i++) {
            int portionLen = i == 4 ?  tailLen :  DATA_PORTION_MAX_SIZE;
            short start = (short) (i * DATA_PORTION_MAX_SIZE);
            byte[] portion = BYTE_ARRAY_HELPER.bSub(key, start, portionLen);
            byte p1 = i == 0 ? (byte) 0x00 : (byte) 0x01;
            when(tag.transceive(getSendKeyChunkAPDU(INS_CHANGE_KEY_CHUNK, p1, portion, sault).getBytes()))
                    .thenReturn(SW_SUCCESS);
        }
        byte[] mac = hmacHelperMock.computeMac(key);
        when(tag.transceive(getSendKeyChunkAPDU(INS_CHANGE_KEY_CHUNK, (byte) 0x02, mac, sault).getBytes()))
                .thenReturn(BYTE_ARRAY_HELPER.bConcat(new byte[]{(byte) 0x00, (byte) 0x02}, SW_SUCCESS));
        nfcApduRunnerMock.setCardTag(tag);
        cardKeyChainApi.setApduRunner(nfcApduRunnerMock);
        mockAndroidKeyStore();
        try {
            String response = cardKeyChainApi.changeKeyInKeyChainAndGetJson(BYTE_ARRAY_HELPER.hex(key), oldHmac);
            System.out.println(response);
            assertEquals(response.toLowerCase(), JSON_HELPER.createResponseJson(BYTE_ARRAY_HELPER.hex(mac)).toLowerCase());
        } catch (Exception e) {
            System.out.println(e.getMessage());
            fail();
        }
    }

    @Test
    public void testDeleteKeyAndFinishDeleteAppletSuccessfullOperation() throws Exception {
        List<CardApiInterface<List<String>>> delepeOps = Arrays.asList(deleteKeyFromKeyChain, finishDeleteKeyFromKeyChainAfterInterruption);
        byte numOfKeysAfterDelete = 1;
        String mac = STRING_HELPER.randomHexString(2 * SHA_HASH_SIZE);
        byte[] ind = new byte[]{(byte) 0x00, (byte) 0x01};
        byte[] sault = createSault();
        NfcApduRunner nfcApduRunnerMock = prepareNfcApduRunnerMock(nfcApduRunner);
        HmacHelper hmacHelperMock = prepareHmacHelperMock(HMAC_HELPER);
        TonWalletAppletApduCommands.setHmacHelper(hmacHelperMock);
        TonWalletApi.setHmacHelper(hmacHelperMock);
        for(int i = 0 ; i < delepeOps.size(); i++) {
            try {
                byte state = i == 0 ? PERSONALIZED_STATE : DELETE_KEY_FROM_KEYCHAIN_STATE;
                IsoDep tag = prepareAdvancedTagMock(sault, state);
                when(tag.transceive(getGetIndexAndLenOfKeyInKeyChainAPDU(BYTE_ARRAY_HELPER.bytes(mac), sault).getBytes()))
                        .thenReturn(BYTE_ARRAY_HELPER.bConcat(ind, new byte[]{0x02, 0x26}, SW_SUCCESS));
                when(tag.transceive(getInitiateDeleteOfKeyAPDU(ind, sault).getBytes()))
                        .thenReturn(BYTE_ARRAY_HELPER.bConcat(new byte[]{0x02, 0x26}, SW_SUCCESS));
                when(tag.transceive(getNumberOfKeysAPDU(sault).getBytes())).thenReturn(BYTE_ARRAY_HELPER.bConcat(new byte[]{(byte) 0x00, numOfKeysAfterDelete}, SW_SUCCESS));
                when(tag.transceive(getDeleteKeyChunkAPDU(sault).getBytes()))
                        .thenReturn(BYTE_ARRAY_HELPER.bConcat(new byte[]{(byte) 0x00}, SW_SUCCESS))
                        .thenReturn(BYTE_ARRAY_HELPER.bConcat(new byte[]{(byte) 0x00}, SW_SUCCESS))
                        .thenReturn(BYTE_ARRAY_HELPER.bConcat(new byte[]{(byte) 0x00}, SW_SUCCESS))
                        .thenReturn(BYTE_ARRAY_HELPER.bConcat(new byte[]{(byte) 0x01}, SW_SUCCESS));
                when(tag.transceive(getDeleteKeyRecordAPDU(sault).getBytes()))
                        .thenReturn(BYTE_ARRAY_HELPER.bConcat(new byte[]{(byte) 0x00}, SW_SUCCESS))
                        .thenReturn(BYTE_ARRAY_HELPER.bConcat(new byte[]{(byte) 0x00}, SW_SUCCESS))
                        .thenReturn(BYTE_ARRAY_HELPER.bConcat(new byte[]{(byte) 0x01}, SW_SUCCESS));
                nfcApduRunnerMock.setCardTag(tag);
                cardKeyChainApi.setApduRunner(nfcApduRunnerMock);
                mockAndroidKeyStore();
                String response = delepeOps.get(i).accept(i == 0 ? Collections.singletonList(mac) : Collections.emptyList());
                System.out.println(response);
                assertEquals(response.toLowerCase(), JSON_HELPER.createResponseJson(Byte.toString(numOfKeysAfterDelete)).toLowerCase());
            } catch (Exception e) {
                System.out.println(e.getMessage());
                fail();
            }
        }
    }

    /**
     * Test for applet fail with some SW
     **/

    @Test
    public void testAppletFailOperations() throws Exception {
        RAPDU rapdu = new RAPDU(BYTE_ARRAY_HELPER.hex(ErrorCodes.SW_WRONG_DATA));
        List<CardApiInterface<List<String>>> ops = Arrays.asList(
                getKeyFromKeyChain, checkKeyHmacConsistency, getIndexAndLenOfKeyInKeyChain, getHmac, checkAvailableVolForNewKey, getKeyChainDataAboutAllKeys,
                resetKeyChain, getFreeStorageSize, getOccupiedStorageSize, getNumberOfKeys, getKeyChainInfo, getDeleteKeyChunkNumOfPackets,
                getDeleteKeyRecordNumOfPackets);
        byte[] sault = createSault();
        NfcApduRunner nfcApduRunnerMock = prepareNfcApduRunnerMock(nfcApduRunner);
        TonWalletAppletApduCommands.setHmacHelper(prepareHmacHelperMock(HMAC_HELPER));
        IsoDep tag = prepareAdvancedTagMock(sault, PERSONALIZED_STATE);
        when(tag.transceive(getResetKeyChainAPDU(sault).getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_WRONG_DATA));
        when(tag.transceive(getGetFreeSizeAPDU(sault).getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_WRONG_DATA));
        when(tag.transceive(getGetOccupiedSizeAPDU(sault).getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_WRONG_DATA));
        String mac = STRING_HELPER.randomHexString(2 * SHA_HASH_SIZE);
        when(tag.transceive(getCheckKeyHmacConsistencyAPDU(BYTE_ARRAY_HELPER.bytes(mac), sault).getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_WRONG_DATA));
        when(tag.transceive(getGetIndexAndLenOfKeyInKeyChainAPDU(BYTE_ARRAY_HELPER.bytes(mac), sault).getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_WRONG_DATA));
        byte[] ind = new byte[]{0, 0};
        when(tag.transceive(getGetHmacAPDU(ind, sault).getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_WRONG_DATA));
        short len = 120;
        when(tag.transceive(getCheckAvailableVolForNewKeyAPDU(len, sault).getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_WRONG_DATA));
        when(tag.transceive(getNumberOfKeysAPDU(sault).getBytes()))
                .thenReturn(BYTE_ARRAY_HELPER.bConcat(new byte[]{(byte) 0x00, (byte) 0x01}, SW_SUCCESS))
                .thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_WRONG_DATA));
        when(tag.transceive(getDeleteKeyChunkNumOfPacketsAPDU(sault).getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_WRONG_DATA));
        when(tag.transceive(getDeleteKeyRecordNumOfPacketsAPDU(sault).getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_WRONG_DATA));
        nfcApduRunnerMock.setCardTag(tag);
        cardKeyChainApi.setApduRunner(nfcApduRunnerMock);
        mockAndroidKeyStore();
        int i = -1;
        for (CardApiInterface<List<String>> op : ops) {
            try {
                i++;
                if (i >= 11) {
                    when(tag.transceive(GET_APP_INFO_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bConcat(new byte[]{DELETE_KEY_FROM_KEYCHAIN_STATE}, BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS)));
                }
                op.accept(i <= 2 ? Collections.singletonList(mac)
                        : i == 3 ? Collections.singletonList("0")
                        : i == 4 ? Collections.singletonList(Short.toString(len)) : Collections.emptyList());
                fail();
            } catch (Exception e) {
                System.out.println(e.getMessage());
                String errMsg = JSON_HELPER.createErrorJsonForCardException(rapdu.prepareSwFormatted(), nfcApduRunnerMock.getLastSentAPDU());
                Assert.assertEquals(e.getMessage(), errMsg);
            }
        }
    }

    @Test
    public void testDeleteKeyAndFinishDeleteAppletFailOperation() throws Exception {
        List<CardApiInterface<List<String>>> ops = Arrays.asList(deleteKeyFromKeyChain, finishDeleteKeyFromKeyChainAfterInterruption);
        RAPDU rapdu = new RAPDU(BYTE_ARRAY_HELPER.hex(ErrorCodes.SW_WRONG_LENGTH));
        String mac = STRING_HELPER.randomHexString(2 * SHA_HASH_SIZE);
        byte[] ind = new byte[]{(byte) 0x00, (byte) 0x01};
        byte[] sault = createSault();
        NfcApduRunner nfcApduRunnerMock = prepareNfcApduRunnerMock(nfcApduRunner);
        HmacHelper hmacHelperMock = prepareHmacHelperMock(HMAC_HELPER);
        TonWalletAppletApduCommands.setHmacHelper(hmacHelperMock);
        TonWalletApi.setHmacHelper(hmacHelperMock);
        for(int j = 0 ; j < ops.size() ; j++) {
            byte state = j == 0 ? PERSONALIZED_STATE : DELETE_KEY_FROM_KEYCHAIN_STATE;
            IsoDep tag = prepareAdvancedTagMock(sault, state);
            when(tag.transceive(getGetIndexAndLenOfKeyInKeyChainAPDU(BYTE_ARRAY_HELPER.bytes(mac), sault).getBytes()))
                    .thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_WRONG_LENGTH))
                    .thenReturn(BYTE_ARRAY_HELPER.bConcat(ind, new byte[]{0x02, 0x26}, SW_SUCCESS));
            when(tag.transceive(getInitiateDeleteOfKeyAPDU(ind, sault).getBytes()))
                    .thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_WRONG_LENGTH))
                    .thenReturn(BYTE_ARRAY_HELPER.bConcat(new byte[]{0x00, 0x05}, SW_SUCCESS));
            when(tag.transceive(getDeleteKeyChunkAPDU(sault).getBytes()))
                    .thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_WRONG_LENGTH))
                    .thenReturn(BYTE_ARRAY_HELPER.bConcat(new byte[]{(byte) 0x00}, SW_SUCCESS))
                    .thenReturn(BYTE_ARRAY_HELPER.bConcat(new byte[]{(byte) 0x01}, SW_SUCCESS));
            when(tag.transceive(getDeleteKeyRecordAPDU(sault).getBytes()))
                    .thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_WRONG_LENGTH))
                    .thenReturn(BYTE_ARRAY_HELPER.bConcat(new byte[]{(byte) 0x00}, SW_SUCCESS))
                    .thenReturn(BYTE_ARRAY_HELPER.bConcat(new byte[]{(byte) 0x00}, SW_SUCCESS))
                    .thenReturn(BYTE_ARRAY_HELPER.bConcat(new byte[]{(byte) 0x01}, SW_SUCCESS));
            when(tag.transceive(getNumberOfKeysAPDU(sault).getBytes())).thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_WRONG_LENGTH));
            nfcApduRunnerMock.setCardTag(tag);
            cardKeyChainApi.setApduRunner(nfcApduRunnerMock);
            mockAndroidKeyStore();
            int numOfIterations = j == 0 ? 5 : 3;
            for (int i = 0; i < numOfIterations; i++) {
                try {
                    ops.get(j).accept(j == 0 ? Collections.singletonList(mac) : Collections.emptyList());
                    fail();
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    String errMsg = JSON_HELPER.createErrorJsonForCardException(rapdu.prepareSwFormatted(), nfcApduRunnerMock.getLastSentAPDU());
                    Assert.assertEquals(e.getMessage(), errMsg);
                }
            }
        }
    }

    @Test
    public void testAddKeyIntoKeyChainAppletFailedOperation() throws Exception {
        RAPDU rapdu = new RAPDU(BYTE_ARRAY_HELPER.hex(ErrorCodes.SW_COMMAND_ABORTED));
        byte[] sault = createSault();
        short len = 550;
        NfcApduRunner nfcApduRunnerMock = prepareNfcApduRunnerMock(nfcApduRunner);
        HmacHelper hmacHelperMock = prepareHmacHelperMock(HMAC_HELPER);
        TonWalletAppletApduCommands.setHmacHelper(hmacHelperMock);
        TonWalletApi.setHmacHelper(hmacHelperMock);
        IsoDep tag = prepareAdvancedTagMock(sault, PERSONALIZED_STATE);
        when(tag.transceive(getCheckAvailableVolForNewKeyAPDU(len, sault).getBytes()))
                .thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_COMMAND_ABORTED))
                .thenReturn(SW_SUCCESS);
        when(tag.transceive(getNumberOfKeysAPDU(sault).getBytes()))
                .thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_COMMAND_ABORTED))
                .thenReturn(BYTE_ARRAY_HELPER.bConcat(new byte[]{(byte) 0x00, (byte) 0x02}, SW_SUCCESS));
        byte[] key = new byte[len];
        random.nextBytes(key);
        byte[] portion1 = BYTE_ARRAY_HELPER.bSub(key, (short) 0x00, DATA_PORTION_MAX_SIZE);
        when(tag.transceive(getSendKeyChunkAPDU(INS_ADD_KEY_CHUNK, (byte) 0x00, portion1, sault).getBytes()))
                .thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_COMMAND_ABORTED));
        nfcApduRunnerMock.setCardTag(tag);
        cardKeyChainApi.setApduRunner(nfcApduRunnerMock);
        mockAndroidKeyStore();
        for (int i = 0; i < 3; i++) {
            try {
                cardKeyChainApi.addKeyIntoKeyChainAndGetJson(BYTE_ARRAY_HELPER.hex(key));
                fail();
            } catch (Exception e) {
                System.out.println(e.getMessage());
                String errMsg = JSON_HELPER.createErrorJsonForCardException(rapdu.prepareSwFormatted(), nfcApduRunnerMock.getLastSentAPDU());
                Assert.assertEquals(e.getMessage(), errMsg);
            }
        }
    }

    @Test
    public void testChangeKeyInKeyChainAppletFailedOperation() throws Exception {
        RAPDU rapdu = new RAPDU(BYTE_ARRAY_HELPER.hex(ErrorCodes.SW_COMMAND_ABORTED));
        String oldHmac = STRING_HELPER.randomHexString(2 * SHA_HASH_SIZE);
        byte[] sault = createSault();
        short len = 550;
        byte[] ind = new byte[]{(byte) 0x00, (byte) 0x01};
        NfcApduRunner nfcApduRunnerMock = prepareNfcApduRunnerMock(nfcApduRunner);
        HmacHelper hmacHelperMock = prepareHmacHelperMock(HMAC_HELPER);
        TonWalletAppletApduCommands.setHmacHelper(hmacHelperMock);
        TonWalletApi.setHmacHelper(hmacHelperMock);
        IsoDep tag = prepareAdvancedTagMock(sault, PERSONALIZED_STATE);
        when(tag.transceive(getGetIndexAndLenOfKeyInKeyChainAPDU(BYTE_ARRAY_HELPER.bytes(oldHmac), sault).getBytes()))
                .thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_COMMAND_ABORTED))
                .thenReturn(BYTE_ARRAY_HELPER.bConcat(ind, new byte[]{0x02, 0x26}, SW_SUCCESS));
        when(tag.transceive(getInitiateChangeOfKeyAPDU(ind, sault).getBytes()))
                .thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_COMMAND_ABORTED))
                .thenReturn(SW_SUCCESS);
        when(tag.transceive(getNumberOfKeysAPDU(sault).getBytes()))
                .thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_COMMAND_ABORTED))
                .thenReturn(BYTE_ARRAY_HELPER.bConcat(new byte[]{0x00, 0x02}, SW_SUCCESS));
        byte[] key = new byte[len];
        random.nextBytes(key);
        byte[] portion1 = BYTE_ARRAY_HELPER.bSub(key, (short) 0x00, DATA_PORTION_MAX_SIZE);
        when(tag.transceive(getSendKeyChunkAPDU(INS_CHANGE_KEY_CHUNK, (byte) 0x00, portion1, sault).getBytes()))
                .thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_COMMAND_ABORTED));
        nfcApduRunnerMock.setCardTag(tag);
        cardKeyChainApi.setApduRunner(nfcApduRunnerMock);
        mockAndroidKeyStore();
        for (int i = 0; i < 4; i++) {
            try {
                cardKeyChainApi.changeKeyInKeyChainAndGetJson(BYTE_ARRAY_HELPER.hex(key), oldHmac);
                fail();
            } catch (Exception e) {
                System.out.println(e.getMessage());
                String errMsg = JSON_HELPER.createErrorJsonForCardException(rapdu.prepareSwFormatted(), nfcApduRunnerMock.getLastSentAPDU());
                Assert.assertEquals(e.getMessage(), errMsg);
            }
        }
    }


    /**
     * Test bad num of keys
     **/

    @Test
    public void testChangeKeyInKeyChainBadNumOfKeys() throws Exception {
        String oldHmac = STRING_HELPER.randomHexString(2 * SHA_HASH_SIZE);
        byte[] ind = new byte[]{(byte) 0x00, (byte) 0x01};
        byte[] sault = createSault();
        short len = 150;
        short tailLen = (short) (len % DATA_PORTION_MAX_SIZE);
        NfcApduRunner nfcApduRunnerMock = prepareNfcApduRunnerMock(nfcApduRunner);
        HmacHelper hmacHelperMock = prepareHmacHelperMock(HMAC_HELPER);
        TonWalletAppletApduCommands.setHmacHelper(hmacHelperMock);
        TonWalletApi.setHmacHelper(hmacHelperMock);
        IsoDep tag = prepareAdvancedTagMock(sault, PERSONALIZED_STATE);
        when(tag.transceive(getGetIndexAndLenOfKeyInKeyChainAPDU(BYTE_ARRAY_HELPER.bytes(oldHmac), sault).getBytes()))
                .thenReturn(BYTE_ARRAY_HELPER.bConcat(ind, new byte[]{0x00, (byte) 0x96}, SW_SUCCESS));
        when(tag.transceive(getInitiateChangeOfKeyAPDU(ind, sault).getBytes()))
                .thenReturn(SW_SUCCESS);
        int numOfKeys = 0x02;
        when(tag.transceive(getNumberOfKeysAPDU(sault).getBytes())).thenReturn(BYTE_ARRAY_HELPER.bConcat(new byte[]{(byte) 0x00, (byte) numOfKeys}, SW_SUCCESS));
        byte[] key = new byte[len];
        random.nextBytes(key);
        byte[] portion1 = BYTE_ARRAY_HELPER.bSub(key, (short) 0x00, DATA_PORTION_MAX_SIZE);
        when(tag.transceive(getSendKeyChunkAPDU(INS_CHANGE_KEY_CHUNK, (byte) 0x00, portion1, sault).getBytes()))
                .thenReturn(SW_SUCCESS);
        byte[] portion2 = BYTE_ARRAY_HELPER.bSub(key, DATA_PORTION_MAX_SIZE, tailLen);
        when(tag.transceive(getSendKeyChunkAPDU(INS_CHANGE_KEY_CHUNK, (byte) 0x01, portion2, sault).getBytes()))
                .thenReturn(SW_SUCCESS);
        byte[] mac = hmacHelperMock.computeMac(key);
        int badNumOfKeys = 0x03;
        when(tag.transceive(getSendKeyChunkAPDU(INS_CHANGE_KEY_CHUNK, (byte) 0x02, mac, sault).getBytes()))
                .thenReturn(BYTE_ARRAY_HELPER.bConcat(new byte[]{(byte) 0x00, (byte) badNumOfKeys}, SW_SUCCESS));
        nfcApduRunnerMock.setCardTag(tag);
        cardKeyChainApi.setApduRunner(nfcApduRunnerMock);
        mockAndroidKeyStore();
        try {
            cardKeyChainApi.changeKeyInKeyChainAndGetJson(BYTE_ARRAY_HELPER.hex(key), oldHmac);
            fail();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            Assert.assertEquals(e.getMessage(), EXCEPTION_HELPER.makeFinalErrMsg(new Exception(ERROR_MSG_NUM_OF_KEYS_INCORRECT_AFTER_CHANGE)));
        }
    }

    @Test
    public void testAddKeyIntoKeyChainBadNumOfKeys() throws Exception {
        byte[] sault = createSault();
        short len = 200;
        short tailLen = (short) (len % DATA_PORTION_MAX_SIZE);
        NfcApduRunner nfcApduRunnerMock = prepareNfcApduRunnerMock(nfcApduRunner);
        HmacHelper hmacHelperMock = prepareHmacHelperMock(HMAC_HELPER);
        TonWalletAppletApduCommands.setHmacHelper(hmacHelperMock);
        TonWalletApi.setHmacHelper(hmacHelperMock);
        IsoDep tag = prepareAdvancedTagMock(sault, PERSONALIZED_STATE);
        when(tag.transceive(getCheckAvailableVolForNewKeyAPDU(len, sault).getBytes())).thenReturn(SW_SUCCESS);
        int numOfKeys = 0x02;
        when(tag.transceive(getNumberOfKeysAPDU(sault).getBytes())).thenReturn(BYTE_ARRAY_HELPER.bConcat(new byte[]{(byte) 0x00, (byte) numOfKeys}, SW_SUCCESS));
        byte[] key = new byte[len];
        random.nextBytes(key);
        byte[] portion1 = BYTE_ARRAY_HELPER.bSub(key, (short) 0x00, DATA_PORTION_MAX_SIZE);
        when(tag.transceive(getSendKeyChunkAPDU(INS_ADD_KEY_CHUNK, (byte) 0x00, portion1, sault).getBytes()))
                .thenReturn(SW_SUCCESS);
        byte[] portion2 = BYTE_ARRAY_HELPER.bSub(key, DATA_PORTION_MAX_SIZE, tailLen);
        when(tag.transceive(getSendKeyChunkAPDU(INS_ADD_KEY_CHUNK, (byte) 0x01, portion2, sault).getBytes()))
                .thenReturn(SW_SUCCESS);
        byte[] mac = hmacHelperMock.computeMac(key);
        int badNumOfKeys = 0x04;
        when(tag.transceive(getSendKeyChunkAPDU(INS_ADD_KEY_CHUNK, (byte) 0x02, mac, sault).getBytes()))
                .thenReturn(BYTE_ARRAY_HELPER.bConcat(new byte[]{(byte) 0x00, (byte) badNumOfKeys}, SW_SUCCESS));
        nfcApduRunnerMock.setCardTag(tag);
        cardKeyChainApi.setApduRunner(nfcApduRunnerMock);
        mockAndroidKeyStore();
        try {
            cardKeyChainApi.addKeyIntoKeyChainAndGetJson(BYTE_ARRAY_HELPER.hex(key));
            fail();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            Assert.assertEquals(e.getMessage(), EXCEPTION_HELPER.makeFinalErrMsg(new Exception(ERROR_MSG_NUM_OF_KEYS_INCORRECT_AFTER_ADD)));
        }
    }

    /**
     * Test bad key length
     **/

    @Test
    public void testChangeKeyInKeyChainBadNewKeyLength() throws Exception {
        String oldHmac = STRING_HELPER.randomHexString(2 * SHA_HASH_SIZE);
        byte[] ind = new byte[]{(byte) 0x00, (byte) 0x01};
        byte[] sault = createSault();
        short newLen = 150;
        short oldLen = 151;
        NfcApduRunner nfcApduRunnerMock = prepareNfcApduRunnerMock(nfcApduRunner);
        HmacHelper hmacHelperMock = prepareHmacHelperMock(HMAC_HELPER);
        TonWalletAppletApduCommands.setHmacHelper(hmacHelperMock);
        TonWalletApi.setHmacHelper(hmacHelperMock);
        IsoDep tag = prepareAdvancedTagMock(sault, PERSONALIZED_STATE);
        when(tag.transceive(getGetIndexAndLenOfKeyInKeyChainAPDU(BYTE_ARRAY_HELPER.bytes(oldHmac), sault).getBytes()))
                .thenReturn(BYTE_ARRAY_HELPER.bConcat(ind, new byte[]{0x00, (byte) oldLen}, SW_SUCCESS));
        byte[] key = new byte[newLen];
        random.nextBytes(key);
        nfcApduRunnerMock.setCardTag(tag);
        cardKeyChainApi.setApduRunner(nfcApduRunnerMock);
        mockAndroidKeyStore();
        try {
            cardKeyChainApi.changeKeyInKeyChainAndGetJson(BYTE_ARRAY_HELPER.hex(key), oldHmac);
            fail();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            Assert.assertEquals(e.getMessage(), EXCEPTION_HELPER.makeFinalErrMsg(new Exception(ERROR_MSG_NEW_KEY_LEN_INCORRECT + oldLen + ".")));
        }
    }


    /**
     * Test bad applet state
     **/

    @Test
    public void testForDeleteFromKeychainState() throws Exception {
        String keyHmac = STRING_HELPER.randomHexString(2 * SHA_HASH_SIZE);
        List<CardApiInterface<List<String>>> ops = Arrays.asList(changeKeyInKeyChain, addKeyIntoKeyChain, deleteKeyFromKeyChain);
        byte[] sault = createSault();
        NfcApduRunner nfcApduRunnerMock = prepareNfcApduRunnerMock(nfcApduRunner);
        TonWalletAppletApduCommands.setHmacHelper(prepareHmacHelperMock(HMAC_HELPER));
        IsoDep tag = prepareAdvancedTagMock(sault, DELETE_KEY_FROM_KEYCHAIN_STATE);
        when(tag.transceive(getGetIndexAndLenOfKeyInKeyChainAPDU(BYTE_ARRAY_HELPER.bytes(keyHmac), sault).getBytes()))
                .thenReturn(BYTE_ARRAY_HELPER.bConcat(new byte[]{(byte) 0x00, (byte) 0x01, 0x00, 0x20}, SW_SUCCESS));
        nfcApduRunnerMock.setCardTag(tag);
        cardKeyChainApi.setApduRunner(nfcApduRunnerMock);
        mockAndroidKeyStore();
        String errMsg = ERROR_MSG_APPLET_IS_NOT_PERSONALIZED + TonWalletAppletStates.findByStateValue(DELETE_KEY_FROM_KEYCHAIN_STATE).getDescription() + ".";
        for (int i = 0; i < ops.size(); i++) {
            List<String> args = i == 0 ? Arrays.asList(STRING_HELPER.randomHexString(64), keyHmac)
                    : i == 1 ? Collections.singletonList(STRING_HELPER.randomHexString(20))
                    : Collections.singletonList(keyHmac);
            try {
                ops.get(i).accept(args);
                fail();
            } catch (Exception e) {
                System.out.println(e.getMessage());
                Assert.assertEquals(e.getMessage(), EXCEPTION_HELPER.makeFinalErrMsg(new Exception(errMsg)));
            }
        }
    }

    @Test
    public void testForPersonalizedState() throws Exception {
        byte[] sault = createSault();
        List<CardApiInterface<List<String>>> ops = Arrays.asList(finishDeleteKeyFromKeyChainAfterInterruption, getDeleteKeyRecordNumOfPackets, getDeleteKeyChunkNumOfPackets);
        NfcApduRunner nfcApduRunnerMock = prepareNfcApduRunnerMock(nfcApduRunner);
        TonWalletAppletApduCommands.setHmacHelper(prepareHmacHelperMock(HMAC_HELPER));
        IsoDep tag = prepareAdvancedTagMock(sault, PERSONALIZED_STATE);
        nfcApduRunnerMock.setCardTag(tag);
        cardKeyChainApi.setApduRunner(nfcApduRunnerMock);
        mockAndroidKeyStore();
        String errMsg = ERROR_MSG_APPLET_DOES_NOT_WAIT_TO_DELETE_KEY + TonWalletAppletStates.findByStateValue(PERSONALIZED_STATE).getDescription() + ".";
        for (int i = 0; i < ops.size(); i++) {
            try {
                ops.get(i).accept(Collections.emptyList());
                fail();
            } catch (Exception e) {
                System.out.println(e.getMessage());
                Assert.assertEquals(e.getMessage(), EXCEPTION_HELPER.makeFinalErrMsg(new Exception(errMsg)));
            }
        }
    }


    /** Tests for incorrect card responses **/

    /**
     * Invalid RAPDU object/responses from card tests
     **/

    @Test
    public void testGetHmacAndRelatedInvalidResponse() throws Exception {
        TonWalletAppletApduCommands.setHmacHelper(prepareHmacHelperMock(HMAC_HELPER));
        byte[] sault = createSault();
        NfcApduRunner nfcApduRunnerMock = prepareNfcApduRunnerMock(nfcApduRunner);
        List<CardApiInterface<List<String>>> ops = Arrays.asList(getHmac, getKeyChainDataAboutAllKeys);
        List<RAPDU> badRapdus = Arrays.asList(null, new RAPDU(STRING_HELPER.randomHexString(2 * SHA_HASH_SIZE + 6) + "9000"),
                new RAPDU(STRING_HELPER.randomHexString(2 * SHA_HASH_SIZE + 2) + "9000"));
        mockAndroidKeyStore();
        byte[] ind = new byte[]{0, 0};
        CAPDU capdu = getGetHmacAPDU(ind, sault);
        IsoDep tag = prepareAdvancedTagMock(sault, PERSONALIZED_STATE);
        when(tag.transceive(getNumberOfKeysAPDU(sault).getBytes())).thenReturn(BYTE_ARRAY_HELPER.bConcat(new byte[]{(byte) 0x00, (byte) 0x01}, SW_SUCCESS));
        nfcApduRunnerMock.setCardTag(tag);
        badRapdus.forEach(rapdu -> {
            try {
                Mockito.doReturn(rapdu).when(nfcApduRunnerMock).sendAPDU(capdu);
                cardKeyChainApi.setApduRunner(nfcApduRunnerMock);
                for (int i = 0; i < ops.size(); i++) {
                    try {
                        System.out.println(rapdu);
                        ops.get(i).accept(i == 0 ? Collections.singletonList("0") : Collections.emptyList());
                        fail();
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                        Assert.assertEquals(e.getMessage(), EXCEPTION_HELPER.makeFinalErrMsg(new Exception(ERROR_MSG_GET_HMAC_RESPONSE_LEN_INCORRECT)));
                    }
                }
            } catch (Exception e) {

                e.printStackTrace();
                fail();
            }
        });
    }

    @Test
    public void testGetKeyInvalidResponse() throws Exception {
        String keyHmac = STRING_HELPER.randomHexString(2 * SHA_HASH_SIZE);
        TonWalletAppletApduCommands.setHmacHelper(prepareHmacHelperMock(HMAC_HELPER));
        byte[] sault = createSault();
        NfcApduRunner nfcApduRunnerMock = prepareNfcApduRunnerMock(nfcApduRunner);
        List<RAPDU> badRapdus = Arrays.asList(null, new RAPDU("9000"), new RAPDU(STRING_HELPER.randomHexString(2 * DATA_PORTION_MAX_SIZE + 2) + "9000"),
                new RAPDU(STRING_HELPER.randomHexString(2 * DATA_PORTION_MAX_SIZE - 2) + "9000"));
        mockAndroidKeyStore();
        byte[] ind = new byte[]{0, 0};
        CAPDU capdu = getGetKeyChunkAPDU(ind, (short) 0, sault, (byte) DATA_PORTION_MAX_SIZE);
        IsoDep tag = prepareAdvancedTagMock(sault, PERSONALIZED_STATE);
        when(tag.transceive(getGetIndexAndLenOfKeyInKeyChainAPDU(BYTE_ARRAY_HELPER.bytes(keyHmac), sault).getBytes()))
                .thenReturn(BYTE_ARRAY_HELPER.bConcat(new byte[]{(byte) 0x00, (byte) 0x00, 0x00, (byte) 0x80}, SW_SUCCESS));
        nfcApduRunnerMock.setCardTag(tag);
        badRapdus.forEach(rapdu -> {
            try {
                Mockito.doReturn(rapdu).when(nfcApduRunnerMock).sendAPDU(capdu);
                cardKeyChainApi.setApduRunner(nfcApduRunnerMock);
                System.out.println(rapdu);
                cardKeyChainApi.getKeyFromKeyChainAndGetJson(keyHmac);
                fail();
            } catch (Exception e) {
                System.out.println(e.getMessage());
                Assert.assertEquals(e.getMessage(), EXCEPTION_HELPER.makeFinalErrMsg(new Exception(ERROR_KEY_DATA_PORTION_INCORRECT_LEN + DATA_PORTION_MAX_SIZE)));
            }
        });
    }


    @Test
    public void testGetGetIndexAndLenOfKeyInKeyChainAndChangeKeyAndDeleteInvalidResponse() throws Exception {
        String hmac = STRING_HELPER.randomHexString(2 * SHA_HASH_SIZE);
        TonWalletAppletApduCommands.setHmacHelper(prepareHmacHelperMock(HMAC_HELPER));
        byte[] sault = createSault();
        List<CardApiInterface<List<String>>> ops = Arrays.asList(getIndexAndLenOfKeyInKeyChain, getKeyFromKeyChain, deleteKeyFromKeyChain, changeKeyInKeyChain);
        NfcApduRunner nfcApduRunnerMock = prepareNfcApduRunnerMock(nfcApduRunner);
        Map<RAPDU, String> badRapdusToErrMsg = new LinkedHashMap<>();
        badRapdusToErrMsg.put(null, ERROR_MSG_GET_KEY_INDEX_IN_STORAGE_AND_LEN_RESPONSE_LEN_INCORRECT);
        badRapdusToErrMsg.put(new RAPDU("9000"), ERROR_MSG_GET_KEY_INDEX_IN_STORAGE_AND_LEN_RESPONSE_LEN_INCORRECT);
        badRapdusToErrMsg.put(new RAPDU(STRING_HELPER.randomHexString(2 * GET_KEY_INDEX_IN_STORAGE_AND_LEN_LE + 2) + "9000"), ERROR_MSG_GET_KEY_INDEX_IN_STORAGE_AND_LEN_RESPONSE_LEN_INCORRECT);
        badRapdusToErrMsg.put(new RAPDU(STRING_HELPER.randomHexString(2 * GET_KEY_INDEX_IN_STORAGE_AND_LEN_LE - 2) + "9000"), ERROR_MSG_GET_KEY_INDEX_IN_STORAGE_AND_LEN_RESPONSE_LEN_INCORRECT);
        badRapdusToErrMsg.put(new RAPDU("FFFF00019000"), ERROR_MSG_KEY_INDEX_INCORRECT);
        badRapdusToErrMsg.put(new RAPDU("03FF00019000"), ERROR_MSG_KEY_INDEX_INCORRECT);
        badRapdusToErrMsg.put(new RAPDU("0001FFFF9000"), ERROR_MSG_KEY_LENGTH_INCORRECT);
        badRapdusToErrMsg.put(new RAPDU("000100009000"), ERROR_MSG_KEY_LENGTH_INCORRECT);
        badRapdusToErrMsg.put(new RAPDU("000120019000"), ERROR_MSG_KEY_LENGTH_INCORRECT);
        mockAndroidKeyStore();
        CAPDU capdu = getGetIndexAndLenOfKeyInKeyChainAPDU(BYTE_ARRAY_HELPER.bytes(hmac), sault);
        IsoDep tag = prepareAdvancedTagMock(sault, PERSONALIZED_STATE);
        nfcApduRunnerMock.setCardTag(tag);
        badRapdusToErrMsg.keySet().forEach(rapdu -> {
            for (int i = 0; i < ops.size(); i++) {
                try {
                    Mockito.doReturn(rapdu).when(nfcApduRunnerMock).sendAPDU(capdu);
                    cardKeyChainApi.setApduRunner(nfcApduRunnerMock);
                    System.out.println(i);
                    System.out.println(rapdu);
                    ops.get(i).accept(i <= 2 ? Collections.singletonList(hmac) : Arrays.asList(STRING_HELPER.randomHexString(2), hmac));
                    fail();
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    Assert.assertEquals(e.getMessage(), EXCEPTION_HELPER.makeFinalErrMsg(new Exception(badRapdusToErrMsg.get(rapdu))));
                }
            }
        });
    }

    @Test
    public void testGetFreeOccupiedNumberOfKeysInvalidResponseLength() throws Exception {
        TonWalletAppletApduCommands.setHmacHelper(prepareHmacHelperMock(HMAC_HELPER));
        byte[] sault = createSault();
        NfcApduRunner nfcApduRunnerMock = prepareNfcApduRunnerMock(nfcApduRunner);
        Map<CardApiInterface<List<String>>, String> map = new LinkedHashMap<>();
        map.put(getFreeStorageSize, ERROR_MSG_GET_FREE_SIZE_RESPONSE_LEN_INCORRECT);
        map.put(getOccupiedStorageSize, ERROR_MSG_GET_OCCUPIED_SIZE_RESPONSE_LEN_INCORRECT);
        map.put(getNumberOfKeys, ERROR_MSG_GET_NUMBER_OF_KEYS_RESPONSE_LEN_INCORRECT);
        map.put(getKeyChainInfo, ERROR_MSG_GET_NUMBER_OF_KEYS_RESPONSE_LEN_INCORRECT);
        List<RAPDU> badRapdus = Arrays.asList(null, new RAPDU("9000"),
                new RAPDU(STRING_HELPER.randomHexString(2 * GET_FREE_SIZE_LE + 2) + "9000"),
                new RAPDU(STRING_HELPER.randomHexString(2 * GET_FREE_SIZE_LE - 2) + "9000"));
        mockAndroidKeyStore();
        List<CAPDU> capduList = Arrays.asList(getGetOccupiedSizeAPDU(sault), getGetFreeSizeAPDU(sault), getNumberOfKeysAPDU(sault));
        IsoDep tag = prepareAdvancedTagMock(sault);
        when(tag.transceive(GET_APP_INFO_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bConcat(new byte[]{PERSONALIZED_STATE}, SW_SUCCESS));
        nfcApduRunnerMock.setCardTag(tag);
        badRapdus.forEach(rapdu -> {
            try {
                for(CAPDU capdu : capduList) {
                    Mockito.doReturn(rapdu).when(nfcApduRunnerMock).sendAPDU(capdu);
                }
                cardKeyChainApi.setApduRunner(nfcApduRunnerMock);
                for (CardApiInterface<List<String>> op : map.keySet()) {
                    try {
                        System.out.println(rapdu);
                        op.accept(Collections.emptyList());
                        fail();
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                        Assert.assertEquals(e.getMessage(), EXCEPTION_HELPER.makeFinalErrMsg(new Exception(map.get(op))));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                fail();
            }
        });
    }

    @Test
    public void testGetFreeOccupiedNumberOfKeysInvalidResponseValues() throws Exception {
        TonWalletAppletApduCommands.setHmacHelper(prepareHmacHelperMock(HMAC_HELPER));
        byte[] sault = createSault();
        NfcApduRunner nfcApduRunnerMock = prepareNfcApduRunnerMock(nfcApduRunner);
        Map<CardApiInterface<List<String>>, String> map = new LinkedHashMap<>();
        map.put(getFreeStorageSize, ERROR_MSG_FREE_SIZE_RESPONSE_INCORRECT);
        map.put(getOccupiedStorageSize, ERROR_MSG_OCCUPIED_SIZE_RESPONSE_INCORRECT);
        map.put(getNumberOfKeys, ERROR_MSG_NUMBER_OF_KEYS_RESPONSE_INCORRECT);
        map.put(getKeyChainInfo, ERROR_MSG_NUMBER_OF_KEYS_RESPONSE_INCORRECT);
        RAPDU rapdu = new RAPDU("FFFF9000");
        mockAndroidKeyStore();
        List<CAPDU> capduList = Arrays.asList(getGetOccupiedSizeAPDU(sault), getGetFreeSizeAPDU(sault), getNumberOfKeysAPDU(sault));
        IsoDep tag = prepareAdvancedTagMock(sault);
        when(tag.transceive(GET_APP_INFO_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bConcat(new byte[]{PERSONALIZED_STATE}, BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS)));
        nfcApduRunnerMock.setCardTag(tag);
        for(CAPDU capdu : capduList) {
            Mockito.doReturn(rapdu).when(nfcApduRunnerMock).sendAPDU(capdu);
        }
        cardKeyChainApi.setApduRunner(nfcApduRunnerMock);
        for (CardApiInterface<List<String>> op : map.keySet()) {
            try {
                op.accept(Collections.emptyList());
                fail();
            } catch (Exception e) {
                System.out.println(e.getMessage());
                Assert.assertEquals(e.getMessage(), EXCEPTION_HELPER.makeFinalErrMsg(new Exception(map.get(op))));
            }
        }
        List<CardApiInterface<List<String>>> ops = Arrays.asList(getNumberOfKeys, getKeyChainInfo);
        rapdu = new RAPDU("04009000");
        Mockito.doReturn(rapdu).when(nfcApduRunnerMock).sendAPDU(capduList.get(capduList.size() - 1));
        cardKeyChainApi.setApduRunner(nfcApduRunnerMock);
        for (CardApiInterface<List<String>> op : ops) {
            try {
                op.accept(Collections.emptyList());
                fail();
            } catch (Exception e) {
                System.out.println(e.getMessage());
                Assert.assertEquals(e.getMessage(), EXCEPTION_HELPER.makeFinalErrMsg(new Exception(ERROR_MSG_NUMBER_OF_KEYS_RESPONSE_INCORRECT)));
            }
        }
    }

    @Test
    public void testGetDeleteKeyChunksAndRecordsInvalidResponseLength() throws Exception {
        TonWalletAppletApduCommands.setHmacHelper(prepareHmacHelperMock(HMAC_HELPER));
        byte[] sault = createSault();
        NfcApduRunner nfcApduRunnerMock = prepareNfcApduRunnerMock(nfcApduRunner);
        Map<CardApiInterface<List<String>>, String> map = new LinkedHashMap<>();
        map.put(getDeleteKeyChunkNumOfPackets, ERROR_MSG_GET_DELETE_KEY_CHUNK_NUM_OF_PACKETS_RESPONSE_LEN_INCORRECT);
        map.put(getDeleteKeyRecordNumOfPackets, ERROR_MSG_GET_DELETE_KEY_RECORD_NUM_OF_PACKETS_RESPONSE_LEN_INCORRECT);
        List<RAPDU> badRapdus = Arrays.asList(null, new RAPDU("9000"),
                new RAPDU(STRING_HELPER.randomHexString(2 * GET_DELETE_KEY_CHUNK_NUM_OF_PACKETS_LE + 2) + "9000"),
                new RAPDU(STRING_HELPER.randomHexString(2 * GET_DELETE_KEY_CHUNK_NUM_OF_PACKETS_LE - 2) + "9000"));
        mockAndroidKeyStore();
        List<CAPDU> capduList = Arrays.asList(getDeleteKeyChunkNumOfPacketsAPDU(sault), getDeleteKeyRecordNumOfPacketsAPDU(sault));
        IsoDep tag = prepareAdvancedTagMock(sault);
        when(tag.transceive(GET_APP_INFO_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bConcat(new byte[]{DELETE_KEY_FROM_KEYCHAIN_STATE}, SW_SUCCESS));
        nfcApduRunnerMock.setCardTag(tag);
        badRapdus.forEach(rapdu -> {
            try {
                for(CAPDU capdu : capduList) {
                    Mockito.doReturn(rapdu).when(nfcApduRunnerMock).sendAPDU(capdu);
                }
                cardKeyChainApi.setApduRunner(nfcApduRunnerMock);
                for (CardApiInterface<List<String>> op : map.keySet()) {
                    try {
                        op.accept(Collections.emptyList());
                        fail();
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                        Assert.assertEquals(e.getMessage(), EXCEPTION_HELPER.makeFinalErrMsg(new Exception(map.get(op))));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                fail();
            }
        });
    }

    @Test
    public void testGetDeleteKeyChunksAndRecordsInvalidResponseValues() throws Exception {
        TonWalletAppletApduCommands.setHmacHelper(prepareHmacHelperMock(HMAC_HELPER));
        byte[] sault = createSault();
        NfcApduRunner nfcApduRunnerMock = prepareNfcApduRunnerMock(nfcApduRunner);
        Map<CardApiInterface<List<String>>, String> map = new LinkedHashMap<>();
        map.put(getDeleteKeyChunkNumOfPackets, ERROR_MSG_GET_DELETE_KEY_CHUNK_NUM_OF_PACKETS_RESPONSE_INCORRECT);
        map.put(getDeleteKeyRecordNumOfPackets, ERROR_MSG_GET_DELETE_KEY_RECORD_NUM_OF_PACKETS_RESPONSE_INCORRECT);
        RAPDU rapdu = new RAPDU("FFFF9000");
        mockAndroidKeyStore();
        List<CAPDU> capduList = Arrays.asList(getDeleteKeyChunkNumOfPacketsAPDU(sault), getDeleteKeyRecordNumOfPacketsAPDU(sault));
        IsoDep tag = prepareAdvancedTagMock(sault);
        when(tag.transceive(GET_APP_INFO_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bConcat(new byte[]{DELETE_KEY_FROM_KEYCHAIN_STATE}, BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS)));
        nfcApduRunnerMock.setCardTag(tag);
        for (CAPDU capdu : capduList) {
            Mockito.doReturn(rapdu).when(nfcApduRunnerMock).sendAPDU(capdu);
        }
        cardKeyChainApi.setApduRunner(nfcApduRunnerMock);
        for (CardApiInterface<List<String>> op : map.keySet()) {
            try {
                op.accept(Collections.emptyList());
                fail();
            } catch (Exception e) {
                System.out.println(e.getMessage());
                Assert.assertEquals(e.getMessage(), EXCEPTION_HELPER.makeFinalErrMsg(new Exception(map.get(op))));
            }
        }
    }

    @Test
    public void testDeleteAndFinishDeleteInvalidResponseValueForGetNumberOfKeys() throws Exception {
        TonWalletAppletApduCommands.setHmacHelper(prepareHmacHelperMock(HMAC_HELPER));
        byte[] sault = createSault();
        NfcApduRunner nfcApduRunnerMock = prepareNfcApduRunnerMock(nfcApduRunner);
        IsoDep tag = prepareAdvancedTagMock(sault);
        String mac = STRING_HELPER.randomHexString(2 * SHA_HASH_SIZE);
        byte[] ind = new byte[]{(byte) 0x00, (byte) 0x01};
        when(tag.transceive(getGetIndexAndLenOfKeyInKeyChainAPDU(BYTE_ARRAY_HELPER.bytes(mac), sault).getBytes()))
                .thenReturn(BYTE_ARRAY_HELPER.bConcat(ind, new byte[]{0x02, 0x26}, SW_SUCCESS));
        when(tag.transceive(getInitiateDeleteOfKeyAPDU(ind, sault).getBytes()))
                .thenReturn(BYTE_ARRAY_HELPER.bConcat(new byte[]{0x02, 0x26}, SW_SUCCESS));
        when(tag.transceive(getDeleteKeyChunkAPDU(sault).getBytes()))
                .thenReturn(BYTE_ARRAY_HELPER.bConcat(new byte[]{(byte) 0x00}, SW_SUCCESS))
                .thenReturn(BYTE_ARRAY_HELPER.bConcat(new byte[]{(byte) 0x01}, SW_SUCCESS));
        when(tag.transceive(getDeleteKeyRecordAPDU(sault).getBytes()))
                .thenReturn(BYTE_ARRAY_HELPER.bConcat(new byte[]{(byte) 0x00}, SW_SUCCESS))
                .thenReturn(BYTE_ARRAY_HELPER.bConcat(new byte[]{(byte) 0x01}, SW_SUCCESS));
        nfcApduRunnerMock.setCardTag(tag);
        List<CardApiInterface<List<String>>> ops = Arrays.asList(deleteKeyFromKeyChain, finishDeleteKeyFromKeyChainAfterInterruption);
        RAPDU rapdu = new RAPDU("04009000");
        mockAndroidKeyStore();
        CAPDU capdu = getNumberOfKeysAPDU(sault);
        Mockito.doReturn(rapdu).when(nfcApduRunnerMock).sendAPDU(capdu);
        cardKeyChainApi.setApduRunner(nfcApduRunnerMock);
        for (int i = 0 ; i < ops.size(); i++) {
            try {
                byte state = i == 0 ? PERSONALIZED_STATE : DELETE_KEY_FROM_KEYCHAIN_STATE;
                when(tag.transceive(GET_APP_INFO_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bConcat(new byte[]{state}, SW_SUCCESS));
                ops.get(i).accept(i == 0 ? Collections.singletonList(mac) : Collections.emptyList());
                fail();
            } catch (Exception e) {
                System.out.println(e.getMessage());
                Assert.assertEquals(e.getMessage(), EXCEPTION_HELPER.makeFinalErrMsg(new Exception(ERROR_MSG_NUMBER_OF_KEYS_RESPONSE_INCORRECT)));
            }
        }
    }

    @Test
    public void testChangeKeyInKeyChainAndAddKeyInvalidRAPDUAndInvalidResponseLengthAndVals() throws Exception {
        String oldHmac = STRING_HELPER.randomHexString(2 * SHA_HASH_SIZE);
        byte[] ind = new byte[]{(byte) 0x00, (byte) 0x01};
        byte[] sault = createSault();
        short len = 10;
        List<CardApiInterface<List<String>>> ops = Arrays.asList(changeKeyInKeyChain, addKeyIntoKeyChain);
        List<RAPDU> badRapdus1 = Arrays.asList(null, new RAPDU("9000"), new RAPDU(STRING_HELPER.randomHexString(2 * SEND_CHUNK_LE + 2) + "9000"),
                new RAPDU(STRING_HELPER.randomHexString(2 * SEND_CHUNK_LE - 2) + "9000"));
        List<RAPDU> badRapdus2 = Arrays.asList(new RAPDU("00009000"),
                new RAPDU("FFFF9000"), new RAPDU("04009000"));
        NfcApduRunner nfcApduRunnerMock = prepareNfcApduRunnerMock(nfcApduRunner);
        HmacHelper hmacHelperMock = prepareHmacHelperMock(HMAC_HELPER);
        TonWalletAppletApduCommands.setHmacHelper(hmacHelperMock);
        TonWalletApi.setHmacHelper(hmacHelperMock);
        IsoDep tag = prepareAdvancedTagMock(sault, PERSONALIZED_STATE);
        when(tag.transceive(getCheckAvailableVolForNewKeyAPDU(len, sault).getBytes())).thenReturn(SW_SUCCESS);
        when(tag.transceive(getGetIndexAndLenOfKeyInKeyChainAPDU(BYTE_ARRAY_HELPER.bytes(oldHmac), sault).getBytes()))
                .thenReturn(BYTE_ARRAY_HELPER.bConcat(ind, new byte[]{0x00, 0x0A}, SW_SUCCESS));
        when(tag.transceive(getInitiateChangeOfKeyAPDU(ind, sault).getBytes()))
                .thenReturn(SW_SUCCESS);
        when(tag.transceive(getNumberOfKeysAPDU(sault).getBytes())).thenReturn(BYTE_ARRAY_HELPER.bConcat(new byte[]{(byte) 0x00, (byte) 0x01}, SW_SUCCESS));
        byte[] key = new byte[len];
        random.nextBytes(key);
        when(tag.transceive(getSendKeyChunkAPDU(INS_CHANGE_KEY_CHUNK, (byte) 0x00, key, sault).getBytes()))
                .thenReturn(SW_SUCCESS);
        when(tag.transceive(getSendKeyChunkAPDU(INS_ADD_KEY_CHUNK, (byte) 0x00, key, sault).getBytes()))
                .thenReturn(SW_SUCCESS);
        byte[] mac = hmacHelperMock.computeMac(key);
        List<CAPDU> capduList = Arrays.asList(getSendKeyChunkAPDU(INS_CHANGE_KEY_CHUNK, (byte) 0x02, mac, sault), getSendKeyChunkAPDU(INS_ADD_KEY_CHUNK, (byte) 0x02, mac, sault));
        nfcApduRunnerMock.setCardTag(tag);
        mockAndroidKeyStore();
        runTestForSpecificErrMsg(key, oldHmac, nfcApduRunnerMock, badRapdus1, capduList, ops, ERROR_MSG_SEND_CHUNK_RESPONSE_LEN_INCORRECT);
        runTestForSpecificErrMsg(key, oldHmac, nfcApduRunnerMock, badRapdus2, capduList, ops, ERROR_MSG_NUMBER_OF_KEYS_RESPONSE_INCORRECT);
    }

    private void runTestForSpecificErrMsg(byte[] key, String oldHmac, NfcApduRunner nfcApduRunnerMock, List<RAPDU> badRapdus, List<CAPDU> capduList, List<CardApiInterface<List<String>>> ops, String errMsg) {
        badRapdus.forEach(rapdu -> {
            for (int i = 0; i < ops.size(); i++) {
                try {
                    for (int j = 0; j < capduList.size(); j++) {
                        Mockito.doReturn(rapdu).when(nfcApduRunnerMock).sendAPDU(capduList.get(j));
                    }
                    cardKeyChainApi.setApduRunner(nfcApduRunnerMock);
                    ops.get(i).accept(i == 0 ? Arrays.asList(BYTE_ARRAY_HELPER.hex(key), oldHmac) : Collections.singletonList(BYTE_ARRAY_HELPER.hex(key)));
                    fail();
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    Assert.assertEquals(e.getMessage(), EXCEPTION_HELPER.makeFinalErrMsg(new Exception(errMsg)));
                }
            }
        });
    }

    @Test
    public void testGetDeleteKeyChunkInvalidRAPDUAndInvalidResponseLengthAndVals() throws Exception {
        String mac = STRING_HELPER.randomHexString(2 * SHA_HASH_SIZE);
        byte[] ind = new byte[]{(byte) 0x00, (byte) 0x01};
        NfcApduRunner nfcApduRunnerMock = prepareNfcApduRunnerMock(nfcApduRunner);
        HmacHelper hmacHelperMock = prepareHmacHelperMock(HMAC_HELPER);
        TonWalletAppletApduCommands.setHmacHelper(hmacHelperMock);
        TonWalletApi.setHmacHelper(hmacHelperMock);
        byte[] sault = createSault();
        IsoDep tag = prepareAdvancedTagMock(sault);
        when(tag.transceive(getGetIndexAndLenOfKeyInKeyChainAPDU(BYTE_ARRAY_HELPER.bytes(mac), sault).getBytes()))
                .thenReturn(BYTE_ARRAY_HELPER.bConcat(ind, new byte[]{0x02, 0x26}, SW_SUCCESS));
        when(tag.transceive(getInitiateDeleteOfKeyAPDU(ind, sault).getBytes()))
                .thenReturn(BYTE_ARRAY_HELPER.bConcat(new byte[]{0x00, 0x0A}, SW_SUCCESS));
        mockAndroidKeyStore();
        testGetDeleteKeyChunkInvalidRAPDUAndInvalidResponseLengthAndVals(sault, deleteKeyFromKeyChain, tag, nfcApduRunnerMock, Collections.singletonList(mac), PERSONALIZED_STATE);
        testGetDeleteKeyChunkInvalidRAPDUAndInvalidResponseLengthAndVals(sault, finishDeleteKeyFromKeyChainAfterInterruption, tag, nfcApduRunnerMock, Collections.singletonList(mac), DELETE_KEY_FROM_KEYCHAIN_STATE);

    }

    private void testGetDeleteKeyChunkInvalidRAPDUAndInvalidResponseLengthAndVals(byte[] sault, CardApiInterface<List<String>> op, IsoDep tag, NfcApduRunner nfcApduRunnerMock, List<String> args, byte state) throws Exception {
        List<RAPDU> badRapdus1 = Arrays.asList(null, new RAPDU(STRING_HELPER.randomHexString(2 * DELETE_KEY_CHUNK_LE + 2) + "9000"),
                new RAPDU(STRING_HELPER.randomHexString(2 * DELETE_KEY_CHUNK_LE - 2) + "9000"));
        List<RAPDU> badRapdus2 = Arrays.asList(new RAPDU("FF9000"),
                new RAPDU("039000"));
        when(tag.transceive(GET_APP_INFO_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bConcat(new byte[]{state}, BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS)));
        nfcApduRunnerMock.setCardTag(tag);
        CAPDU capdu = getDeleteKeyChunkAPDU(sault);
        runTestForSpecificErrMsg(args, nfcApduRunnerMock, badRapdus1, capdu, op, ERROR_MSG_DELETE_KEY_CHUNK_RESPONSE_LEN_INCORRECT);
        runTestForSpecificErrMsg(args, nfcApduRunnerMock, badRapdus2, capdu, op, ERROR_MSG_DELETE_KEY_CHUNK_RESPONSE_INCORRECT);
    }

    @Test
    public void testGetDeleteKeyRecordInvalidRAPDUAndInvalidResponseLengthAndVals() throws Exception {
        String mac = STRING_HELPER.randomHexString(2 * SHA_HASH_SIZE);
        byte[] ind = new byte[]{(byte) 0x00, (byte) 0x01};
        NfcApduRunner nfcApduRunnerMock = prepareNfcApduRunnerMock(nfcApduRunner);
        HmacHelper hmacHelperMock = prepareHmacHelperMock(HMAC_HELPER);
        TonWalletAppletApduCommands.setHmacHelper(hmacHelperMock);
        TonWalletApi.setHmacHelper(hmacHelperMock);
        byte[] sault = createSault();
        IsoDep tag = prepareAdvancedTagMock(sault);
        when(tag.transceive(getGetIndexAndLenOfKeyInKeyChainAPDU(BYTE_ARRAY_HELPER.bytes(mac), sault).getBytes()))
                .thenReturn(BYTE_ARRAY_HELPER.bConcat(ind, new byte[]{0x02, 0x26}, SW_SUCCESS));
        when(tag.transceive(getInitiateDeleteOfKeyAPDU(ind, sault).getBytes()))
                .thenReturn(BYTE_ARRAY_HELPER.bConcat(new byte[]{0x00, 0x0A}, SW_SUCCESS));
        when(tag.transceive(getDeleteKeyChunkAPDU(sault).getBytes()))
                .thenReturn(BYTE_ARRAY_HELPER.bConcat(new byte[]{(byte) 0x01}, SW_SUCCESS));
        mockAndroidKeyStore();
        testGetDeleteKeyRecordInvalidRAPDUAndInvalidResponseLengthAndVals(sault, deleteKeyFromKeyChain, tag, nfcApduRunnerMock, Collections.singletonList(mac), PERSONALIZED_STATE);
        testGetDeleteKeyRecordInvalidRAPDUAndInvalidResponseLengthAndVals(sault, finishDeleteKeyFromKeyChainAfterInterruption, tag, nfcApduRunnerMock, Collections.singletonList(mac), DELETE_KEY_FROM_KEYCHAIN_STATE);
    }

    private void testGetDeleteKeyRecordInvalidRAPDUAndInvalidResponseLengthAndVals(byte[] sault, CardApiInterface<List<String>> op, IsoDep tag, NfcApduRunner nfcApduRunnerMock, List<String> args, byte state) throws Exception {
        List<RAPDU> badRapdus1 = Arrays.asList(null, new RAPDU(STRING_HELPER.randomHexString(2 * DELETE_KEY_RECORD_LE + 2) + "9000"),
                new RAPDU(STRING_HELPER.randomHexString(2 * DELETE_KEY_RECORD_LE - 2) + "9000"));
        List<RAPDU> badRapdus2 = Arrays.asList(new RAPDU("FF9000"),
                new RAPDU("039000"));
        when(tag.transceive(GET_APP_INFO_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bConcat(new byte[]{state}, BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS)));
        nfcApduRunnerMock.setCardTag(tag);
        CAPDU capdu = getDeleteKeyRecordAPDU(sault);
        runTestForSpecificErrMsg(args, nfcApduRunnerMock, badRapdus1, capdu, op, ERROR_MSG_DELETE_KEY_RECORD_RESPONSE_LEN_INCORRECT);
        runTestForSpecificErrMsg(args, nfcApduRunnerMock, badRapdus2, capdu, op, ERROR_MSG_DELETE_KEY_RECORD_RESPONSE_INCORRECT);
    }

    private void runTestForSpecificErrMsg(List<String> args, NfcApduRunner nfcApduRunnerMock, List<RAPDU> badRapdus, CAPDU capdu, CardApiInterface<List<String>> op, String errMsg) {
        badRapdus.forEach(rapdu -> {
            try {
                System.out.println(rapdu);
                Mockito.doReturn(rapdu).when(nfcApduRunnerMock).sendAPDU(capdu);
                cardKeyChainApi.setApduRunner(nfcApduRunnerMock);
                op.accept(args);
                fail();
            } catch (Exception e) {
                System.out.println(e.getMessage());
                Assert.assertEquals(e.getMessage(), EXCEPTION_HELPER.makeFinalErrMsg(new Exception(errMsg)));
            }
        });
    }


    /** Tests for incorrect input arguments **/

    @Test
    public void testBadHmac() {
        List<CardApiInterface<List<String>>> cardOperationsListToCheckBadData = Arrays.asList(
                changeKeyInKeyChain, getIndexAndLenOfKeyInKeyChain, getKeyFromKeyChain, deleteKeyFromKeyChain,
                checkKeyHmacConsistency);
        Map<String, String> badHmacToErrMsg  = new LinkedHashMap<String, String>() {{
            put(null, ERROR_MSG_KEY_HMAC_NOT_HEX);
            put("", ERROR_MSG_KEY_HMAC_NOT_HEX);
            put("ABC", ERROR_MSG_KEY_HMAC_NOT_HEX);
            put("98777ff", ERROR_MSG_KEY_HMAC_NOT_HEX);
            put("ssAA", ERROR_MSG_KEY_HMAC_NOT_HEX);
            put("12n", ERROR_MSG_KEY_HMAC_NOT_HEX);
            put("1234k7", ERROR_MSG_KEY_HMAC_NOT_HEX);
            put(STRING_HELPER.randomHexString(2 * HMAC_SHA_SIG_SIZE - 2), ERROR_MSG_KEY_HMAC_LEN_INCORRECT);
            put(STRING_HELPER.randomHexString(2 * HMAC_SHA_SIG_SIZE + 2), ERROR_MSG_KEY_HMAC_LEN_INCORRECT);
        }};
        badHmacToErrMsg.keySet().forEach(hmac -> {
            for(int i = 0 ; i < cardOperationsListToCheckBadData.size(); i++) {
                try {
                    cardOperationsListToCheckBadData.get(i).accept(i == 0 ? Arrays.asList("44",  hmac)
                            : Collections.singletonList(hmac));
                    fail();
                }
                catch (Exception e){
                    Assert.assertEquals(e.getMessage(), EXCEPTION_HELPER.makeFinalErrMsg(new Exception(badHmacToErrMsg.get(hmac))));
                }
            }
        });
    }

    @Test
    public void testBadKey() {
        List<CardApiInterface<List<String>>> cardOperationsListToCheckBadData = Arrays.asList(
                changeKeyInKeyChain, addKeyIntoKeyChain);
        Map<String, String> badKeyToErrMsg  = new LinkedHashMap<String, String>() {{
            put(null, ERROR_MSG_KEY_NOT_HEX);
            put("", ERROR_MSG_KEY_NOT_HEX);
            put("ABC", ERROR_MSG_KEY_NOT_HEX);
            put("98777ff", ERROR_MSG_KEY_NOT_HEX);
            put("ssAA", ERROR_MSG_KEY_NOT_HEX);
            put("12n", ERROR_MSG_KEY_NOT_HEX);
            put("1234k7", ERROR_MSG_KEY_NOT_HEX);
            put(STRING_HELPER.randomHexString(2 * MAX_KEY_SIZE_IN_KEYCHAIN + 2), ERROR_MSG_KEY_LEN_INCORRECT);
            put(STRING_HELPER.randomHexString(2 * MAX_KEY_SIZE_IN_KEYCHAIN + 100), ERROR_MSG_KEY_LEN_INCORRECT);
        }};
        badKeyToErrMsg.keySet().forEach(key -> {
            for(int i = 0 ; i < cardOperationsListToCheckBadData.size(); i++) {
                try {
                    cardOperationsListToCheckBadData.get(i).accept(i == 0 ? Arrays.asList(key,  STRING_HELPER.randomHexString(2 * SHA_HASH_SIZE))
                            : Collections.singletonList(key));
                    fail();
                }
                catch (Exception e){
                    Assert.assertEquals(e.getMessage(), EXCEPTION_HELPER.makeFinalErrMsg(new Exception(badKeyToErrMsg.get(key))));
                }
            }
        });
    }

    @Test
    public void testBadIndex() {
        Map<String, String> badIndexToErrMsg = new LinkedHashMap<String, String>() {{
            put(null, ERROR_MSG_KEY_INDEX_STRING_NOT_NUMERIC);
            put("", ERROR_MSG_KEY_INDEX_STRING_NOT_NUMERIC);
            put("ABC", ERROR_MSG_KEY_INDEX_STRING_NOT_NUMERIC);
            put("98777ff", ERROR_MSG_KEY_INDEX_STRING_NOT_NUMERIC);
            put("ssAA", ERROR_MSG_KEY_INDEX_STRING_NOT_NUMERIC);
            put("12n", ERROR_MSG_KEY_INDEX_STRING_NOT_NUMERIC);
            put("1234k7", ERROR_MSG_KEY_INDEX_STRING_NOT_NUMERIC);
            put("-9", ERROR_MSG_KEY_INDEX_STRING_NOT_NUMERIC);
            put(String.valueOf(MAX_NUMBER_OF_KEYS_IN_KEYCHAIN), ERROR_MSG_KEY_INDEX_VALUE_INCORRECT);
            put("34000", ERROR_MSG_KEY_INDEX_VALUE_INCORRECT);
        }};
        badIndexToErrMsg.keySet().forEach(index -> {
            try {
                cardKeyChainApi.getHmacAndGetJson(index);
                fail();
            }
            catch (Exception e){
                Assert.assertEquals(e.getMessage(), EXCEPTION_HELPER.makeFinalErrMsg(new Exception(badIndexToErrMsg.get(index))));
            }
        });
    }

    @Test
    public void testBadKeySize() {
        List<Short> badKeySize = Arrays.asList((short) -1, (short) (MAX_KEY_SIZE_IN_KEYCHAIN + 1));
        badKeySize.forEach(size -> {
            try {
                cardKeyChainApi.checkAvailableVolForNewKeyAndGetJson(size);
                fail();
            }
            catch (Exception e){
                Assert.assertEquals(e.getMessage(), EXCEPTION_HELPER.makeFinalErrMsg(new Exception(ERROR_MSG_KEY_SIZE_INCORRECT)));
            }
        });
    }
}