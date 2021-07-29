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
import com.tonnfccard.smartcard.ErrorCodes;
import com.tonnfccard.smartcard.RAPDU;
import com.tonnfccard.smartcard.TonWalletAppletApduCommands;
import com.tonnfccard.utils.ByteArrayUtil;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.internal.DoNotInstrument;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.tonnfccard.NfcMockHelper.SW_SUCCESS;
import static com.tonnfccard.NfcMockHelper.createSault;
import static com.tonnfccard.NfcMockHelper.mockAndroidKeyStore;
import static com.tonnfccard.NfcMockHelper.mockNfcAdapter;
import static com.tonnfccard.NfcMockHelper.mockNfcAdapterToBeNull;
import static com.tonnfccard.NfcMockHelper.prepareHmacHelperMock;
import static com.tonnfccard.NfcMockHelper.prepareNfcApduRunnerMock;
import static com.tonnfccard.NfcMockHelper.prepareTagMock;
import static com.tonnfccard.TonWalletApiTest.HMAC_HELPER;
import static com.tonnfccard.TonWalletConstants.COMMON_SECRET_SIZE;
import static com.tonnfccard.TonWalletConstants.DATA_FOR_SIGNING_MAX_SIZE_FOR_CASE_WITH_PATH;
import static com.tonnfccard.TonWalletConstants.DEFAULT_PIN_STR;
import static com.tonnfccard.TonWalletConstants.IV_SIZE;
import static com.tonnfccard.TonWalletConstants.MAX_KEY_SIZE_IN_KEYCHAIN;
import static com.tonnfccard.TonWalletConstants.PASSWORD_SIZE;
import static com.tonnfccard.TonWalletConstants.RECOVERY_DATA_MAX_SIZE;
import static com.tonnfccard.TonWalletConstants.SHA_HASH_SIZE;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_BAD_RESPONSE;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_NFC_CONNECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_NFC_DISABLED;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_NO_NFC_HARDWARE;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_NO_TAG;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_TRANSCEIVE;
import static com.tonnfccard.smartcard.CoinManagerApduCommands.LABEL_LENGTH;
import static com.tonnfccard.smartcard.CoinManagerApduCommands.SELECT_COIN_MANAGER_APDU;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.GET_APP_INFO_APDU;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.GET_SAULT_APDU;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.GET_SERIAL_NUMBER_APDU;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.SELECT_TON_WALLET_APPLET_APDU;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = Build.VERSION_CODES.P)
@DoNotInstrument
public class CommonNfcApiTest {
    public  static final String SN = "050004030904080002040303090001010206080103020306";
    private final JsonHelper JSON_HELPER = JsonHelper.getInstance();
    private final ExceptionHelper EXCEPTION_HELPER = ExceptionHelper.getInstance();
    private final StringHelper STRING_HELPER = StringHelper.getInstance();
    private final ByteArrayUtil BYTE_ARRAY_HELPER = ByteArrayUtil.getInstance();
    private NfcApduRunner nfcApduRunner;
    private RecoveryDataApi recoveryDataApi;
    private CardCoinManagerApi cardCoinManagerApi;
    private CardCryptoApi cardCryptoApi;
    private CardActivationApi cardActivationApi;
    private CardKeyChainApi cardKeyChainApi;

    private final CardApiInterface<List<String>> getSault = list -> recoveryDataApi.getSaultAndGetJson();
    private final CardApiInterface<List<String>> getSerialNumber = list -> recoveryDataApi.getSerialNumberAndGetJson();
    private final CardApiInterface<List<String>> getTonAppletState = list -> recoveryDataApi.getTonAppletStateAndGetJson();

    private final CardApiInterface<List<String>> turnOnWallet = list -> cardActivationApi.turnOnWalletAndGetJson(list.get(0), list.get(1), list.get(2), list.get(3));
    private final CardApiInterface<List<String>> turnOnWalletWithoutPin = list -> cardActivationApi.turnOnWalletAndGetJson(list.get(0), list.get(1), list.get(2));
    private final CardApiInterface<List<String>> getHashOfEncryptedCommonSecret = list -> cardActivationApi.getHashOfEncryptedCommonSecretAndGetJson();
    private final CardApiInterface<List<String>> getHashOfEncryptedPassword = list -> cardActivationApi.getHashOfEncryptedPasswordAndGetJson();
    private final CardApiInterface<List<String>> getHashes = list -> cardActivationApi.getHashesAndGetJson();

    private final CardApiInterface<List<String>> addRecoveryData = list -> recoveryDataApi.addRecoveryDataAndGetJson(list.get(0));
    private final CardApiInterface<List<String>> getRecoveryData = list -> recoveryDataApi.getRecoveryDataAndGetJson();
    private final CardApiInterface<List<String>> getRecoveryDataHash = list -> recoveryDataApi.getRecoveryDataHashAndGetJson();
    private final CardApiInterface<List<String>> getRecoveryDataLen = list -> recoveryDataApi.getRecoveryDataLenAndGetJson();
    private final CardApiInterface<List<String>> isRecoveryDataSet = list -> recoveryDataApi.isRecoveryDataSetAndGetJson();
    private final CardApiInterface<List<String>> resetRecovery = list -> recoveryDataApi.resetRecoveryDataAndGetJson();

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

    private final CardApiInterface<List<String>> sign = list -> cardCryptoApi.signAndGetJson(list.get(0), list.get(1));
    private final CardApiInterface<List<String>> verifyPinAndSign = list -> cardCryptoApi.verifyPinAndSignAndGetJson(list.get(0), list.get(1), list.get(2));
    private final CardApiInterface<List<String>> signForDefaultHdPath = list -> cardCryptoApi.signForDefaultHdPathAndGetJson(list.get(0));
    private final CardApiInterface<List<String>> verifyPinAndSignForDefaultHdPath = list -> cardCryptoApi.verifyPinAndSignForDefaultHdPathAndGetJson(list.get(0), list.get(1));
    private final CardApiInterface<List<String>> verifyPin = list -> cardCryptoApi.verifyPinAndGetJson(list.get(0));
    private final CardApiInterface<List<String>> getPublicKey = list -> cardCryptoApi.getPublicKeyAndGetJson(list.get(0));
    private final CardApiInterface<List<String>> getPublicKeyForDefaultPath = list -> cardCryptoApi.getPublicKeyForDefaultPathAndGetJson();

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

    Map<CardApiInterface<List<String>>, List<String>> cardOpsMap = new LinkedHashMap<CardApiInterface<List<String>>, List<String>>(){
        {
            put(addRecoveryData, Collections.singletonList(STRING_HELPER.randomHexString(2 * RECOVERY_DATA_MAX_SIZE)));
            put(setDeviceLabel, Collections.singletonList(STRING_HELPER.randomHexString(2 * LABEL_LENGTH)));
            put(generateSeed, Collections.singletonList(DEFAULT_PIN_STR));
            put(changePin, Arrays.asList(DEFAULT_PIN_STR, DEFAULT_PIN_STR));
            put(turnOnWallet, Arrays.asList(DEFAULT_PIN_STR, STRING_HELPER.randomHexString(2 * PASSWORD_SIZE), STRING_HELPER.randomHexString(2 * COMMON_SECRET_SIZE), STRING_HELPER.randomHexString(2 * IV_SIZE)));
            put(turnOnWalletWithoutPin, Arrays.asList(STRING_HELPER.randomHexString(2 * PASSWORD_SIZE), STRING_HELPER.randomHexString(2 * COMMON_SECRET_SIZE), STRING_HELPER.randomHexString(2 * IV_SIZE)));
            put(sign, Arrays.asList(STRING_HELPER.randomHexString(2 * DATA_FOR_SIGNING_MAX_SIZE_FOR_CASE_WITH_PATH), "1"));
            put(verifyPinAndSign, Arrays.asList(STRING_HELPER.randomHexString(2 * DATA_FOR_SIGNING_MAX_SIZE_FOR_CASE_WITH_PATH), "2", DEFAULT_PIN_STR));
            put(signForDefaultHdPath, Collections.singletonList(STRING_HELPER.randomHexString(2 * DATA_FOR_SIGNING_MAX_SIZE_FOR_CASE_WITH_PATH)));
            put(verifyPinAndSignForDefaultHdPath, Arrays.asList(STRING_HELPER.randomHexString(2 * DATA_FOR_SIGNING_MAX_SIZE_FOR_CASE_WITH_PATH), DEFAULT_PIN_STR));
            put(verifyPin, Collections.singletonList(DEFAULT_PIN_STR));
            put(getPublicKey, Collections.singletonList("3"));
            put(changeKeyInKeyChain, Arrays.asList(STRING_HELPER.randomHexString(2 * MAX_KEY_SIZE_IN_KEYCHAIN), STRING_HELPER.randomHexString(2 * SHA_HASH_SIZE)));
            put(addKeyIntoKeyChain, Collections.singletonList(STRING_HELPER.randomHexString(2 * MAX_KEY_SIZE_IN_KEYCHAIN)));
            put(checkKeyHmacConsistency, Collections.singletonList(STRING_HELPER.randomHexString(2 * SHA_HASH_SIZE)));
            put(deleteKeyFromKeyChain, Collections.singletonList(STRING_HELPER.randomHexString(2 * SHA_HASH_SIZE)));
            put(getIndexAndLenOfKeyInKeyChain, Collections.singletonList(STRING_HELPER.randomHexString(2 * SHA_HASH_SIZE)));
            put(getKeyFromKeyChain, Collections.singletonList(STRING_HELPER.randomHexString(2 * SHA_HASH_SIZE)));
            put(getHmac, Collections.singletonList("100"));
            put(checkAvailableVolForNewKey, Collections.singletonList("100"));
            put(getFreeStorageSize, Collections.emptyList());
            put(getOccupiedStorageSize, Collections.emptyList());
            put(getNumberOfKeys, Collections.emptyList());
            put(finishDeleteKeyFromKeyChainAfterInterruption, Collections.emptyList());
            put(getDeleteKeyChunkNumOfPackets, Collections.emptyList());
            put(getDeleteKeyRecordNumOfPackets, Collections.emptyList());
            put(getKeyChainDataAboutAllKeys, Collections.emptyList());
            put(getKeyChainInfo, Collections.emptyList());
            put(resetKeyChain, Collections.emptyList());
            put(getPublicKeyForDefaultPath, Collections.emptyList());
            put(getDeviceLabel, Collections.emptyList());
            put(getAppsList, Collections.emptyList());
            put(getAvailableMemory, Collections.emptyList());
            put(getSeVersion, Collections.emptyList());
            put(getCsn, Collections.emptyList());
            put(getMaxPinTries, Collections.emptyList());
            put(getRemainingPinTries, Collections.emptyList());
            put(getRootKeyStatus, Collections.emptyList());
            put(resetWallet, Collections.emptyList());
            put(getRecoveryData, Collections.emptyList());
            put(getRecoveryDataHash, Collections.emptyList());
            put(getRecoveryDataLen, Collections.emptyList());
            put(isRecoveryDataSet, Collections.emptyList());
            put(resetRecovery, Collections.emptyList());
            put(getHashOfEncryptedCommonSecret, Collections.emptyList());
            put(getHashOfEncryptedPassword, Collections.emptyList());
            put(getHashes, Collections.emptyList());
            put(getSault, Collections.emptyList());
            put(getSerialNumber, Collections.emptyList());
            put(getTonAppletState, Collections.emptyList());
        }};

    List<CardApiInterface<List<String>>> cardTonWalletOperationsListNotSaulty = Arrays.asList(
            getHashOfEncryptedCommonSecret,
            getHashOfEncryptedPassword,
            getHashes,
            addRecoveryData,
            getRecoveryData,
            getRecoveryDataLen,
            getRecoveryDataHash,
            resetRecovery,
            isRecoveryDataSet,
            getPublicKey,
            getPublicKeyForDefaultPath
    );

    List<CardApiInterface<List<String>>> cardTonWalletOperationsList = Arrays.asList(
            sign,
            verifyPinAndSign,
            signForDefaultHdPath,
            verifyPinAndSignForDefaultHdPath,
            verifyPin,
            changeKeyInKeyChain,
            addKeyIntoKeyChain,
            checkKeyHmacConsistency,
            deleteKeyFromKeyChain,
            getIndexAndLenOfKeyInKeyChain,
            getKeyFromKeyChain,
            getHmac,
            checkAvailableVolForNewKey,
            getFreeStorageSize,
            getOccupiedStorageSize,
            getNumberOfKeys,
            finishDeleteKeyFromKeyChainAfterInterruption,
            getDeleteKeyChunkNumOfPackets,
            getDeleteKeyRecordNumOfPackets,
            getKeyChainDataAboutAllKeys,
            getKeyChainInfo,
            resetKeyChain,
            getSault,
            getSerialNumber,
            getTonAppletState
    );

    List<CardApiInterface<List<String>>> cardCoinManagerOperationsList = Arrays.asList(
            setDeviceLabel,
            generateSeed,
            changePin,
            getDeviceLabel,
            getAppsList,
            getAvailableMemory,
            getSeVersion,
            getCsn,
            getMaxPinTries,
            getRemainingPinTries,
            getRootKeyStatus,
            resetWallet
    );

    @Before
    public  void init() throws Exception {
        Context context = ApplicationProvider.getApplicationContext();
        nfcApduRunner = NfcApduRunner.getInstance(context);
        nfcApduRunner.setNumberOfRetries(1);
        nfcApduRunner.setRetryTimeOut(10);
        recoveryDataApi = new RecoveryDataApi(context, nfcApduRunner);
        cardKeyChainApi = new CardKeyChainApi(context, nfcApduRunner);
        cardCryptoApi = new CardCryptoApi(context, nfcApduRunner);
        cardActivationApi = new CardActivationApi(context, nfcApduRunner);
        cardCoinManagerApi = new CardCoinManagerApi(context, nfcApduRunner);
    }

    private void setNfcApduRunner(NfcApduRunner runner) {
        recoveryDataApi.setApduRunner(runner);
        cardKeyChainApi.setApduRunner(runner);
        cardCryptoApi.setApduRunner(runner);
        cardActivationApi.setApduRunner(runner);
        cardCoinManagerApi.setApduRunner(runner);
    }

    private void prepareNfcTest(String errMsg) {
        for(CardApiInterface<List<String>> op : cardOpsMap.keySet()) {
            try {
                op.accept(cardOpsMap.get(op));
                fail();
            }
            catch (Exception e){
                assertEquals(e.getMessage(), EXCEPTION_HELPER.makeFinalErrMsg(new Exception(errMsg)));
            }
        }
    }


    /** Common tests for NFC errors**/

    @Test
    public void testNoNfcTag() throws Exception{
        IsoDep isoDep = null;
        nfcApduRunner.setCardTag(isoDep);
        setNfcApduRunner(nfcApduRunner);
        prepareNfcTest(ERROR_MSG_NO_TAG);
    }

    @Test
    public void testNoNfc() {
        mockNfcAdapterToBeNull(nfcApduRunner);
        IsoDep isoDep = mock(IsoDep.class);
        nfcApduRunner.setCardTag(isoDep);
        setNfcApduRunner(nfcApduRunner);
        prepareNfcTest(ERROR_MSG_NO_NFC_HARDWARE);
    }

    @Test
    public void testNfcDisabled() {
        mockNfcAdapter(nfcApduRunner, false);
        IsoDep isoDep = mock(IsoDep.class);
        nfcApduRunner.setCardTag(isoDep);
        setNfcApduRunner(nfcApduRunner);
        prepareNfcTest(ERROR_MSG_NFC_DISABLED);
    }

    @Test
    public void testNfcConnectFail() throws Exception{
        mockNfcAdapter(nfcApduRunner, true);
        IsoDep isoDep = mock(IsoDep.class);
        Mockito.doThrow(new IOException()).when(isoDep).connect();
        nfcApduRunner.setCardTag(isoDep);
        setNfcApduRunner(nfcApduRunner);
        prepareNfcTest(ERROR_MSG_NFC_CONNECT);
    }

    @Test
    public void testNfcTransceiveFail() throws Exception{
        mockNfcAdapter(nfcApduRunner, true);;
        IsoDep tag = prepareTagMock();
        Mockito.doThrow(new IOException()).when(tag).transceive(any());
        nfcApduRunner.setCardTag(tag);
        setNfcApduRunner(nfcApduRunner);
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
            setNfcApduRunner(nfcApduRunner);
            prepareNfcTest(ERROR_BAD_RESPONSE);
        }
    }

    @Test
    public void testTonWalletAppletBasicOperationsFailsWithSault() throws Exception {
        RAPDU rapdu = new RAPDU(BYTE_ARRAY_HELPER.hex(ErrorCodes.SW_INS_NOT_SUPPORTED));
        byte[] sault = createSault();
        NfcApduRunner nfcApduRunnerMock = prepareNfcApduRunnerMock(nfcApduRunner);
        HmacHelper hmacHelperMock = prepareHmacHelperMock(HMAC_HELPER);
        TonWalletAppletApduCommands.setHmacHelper(hmacHelperMock);
        TonWalletApi.setHmacHelper(hmacHelperMock);
        IsoDep tag = prepareTagMock();
        when(tag.transceive(SELECT_TON_WALLET_APPLET_APDU.getBytes()))
                .thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_INS_NOT_SUPPORTED))
                .thenReturn(SW_SUCCESS);
        when(tag.transceive(GET_SERIAL_NUMBER_APDU.getBytes()))
                .thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_INS_NOT_SUPPORTED))
                .thenReturn(BYTE_ARRAY_HELPER.bConcat(BYTE_ARRAY_HELPER.bytes(SN), SW_SUCCESS));
        when(tag.transceive(GET_SAULT_APDU.getBytes()))
                .thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_INS_NOT_SUPPORTED))
                .thenReturn(BYTE_ARRAY_HELPER.bConcat(sault, SW_SUCCESS));
        when(tag.transceive(GET_APP_INFO_APDU.getBytes()))
                .thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_INS_NOT_SUPPORTED));
        nfcApduRunnerMock.setCardTag(tag);
        setNfcApduRunner(nfcApduRunnerMock);
        mockAndroidKeyStore();
        for(int i = 0 ; i < cardTonWalletOperationsList.size(); i++) {
            CardApiInterface<List<String>> op = cardTonWalletOperationsList.get(i);
            List<String> args = cardOpsMap.get(op);
            try {
                op.accept(args);
                fail();
            }
            catch (Exception e){
                System.out.println(e.getMessage());
                String errMsg = JSON_HELPER.createErrorJsonForCardException(rapdu.prepareSwFormatted(), nfcApduRunnerMock.getLastSentAPDU());
                Assert.assertEquals(e.getMessage(), errMsg);
            }
        }
    }

    @Test
    public void testTonWalletAppletBasicOperationsFailsWithoutSault() throws Exception {
        RAPDU rapdu = new RAPDU(BYTE_ARRAY_HELPER.hex(ErrorCodes.SW_INS_NOT_SUPPORTED));
        NfcApduRunner nfcApduRunnerMock = prepareNfcApduRunnerMock(nfcApduRunner);
        IsoDep tag = prepareTagMock();
        when(tag.transceive(SELECT_TON_WALLET_APPLET_APDU.getBytes()))
                .thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_INS_NOT_SUPPORTED))
                .thenReturn(SW_SUCCESS);
        when(tag.transceive(GET_APP_INFO_APDU.getBytes()))
                .thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_INS_NOT_SUPPORTED));
        nfcApduRunnerMock.setCardTag(tag);
        setNfcApduRunner(nfcApduRunnerMock);
        for(int i = 0 ; i < cardTonWalletOperationsListNotSaulty.size(); i++) {
            CardApiInterface<List<String>> op = cardTonWalletOperationsListNotSaulty.get(i);
            List<String> args = cardOpsMap.get(op);
            System.out.println();
            try {
                op.accept(args);
                fail();
            }
            catch (Exception e){
                System.out.println(e.getMessage());
                String errMsg = JSON_HELPER.createErrorJsonForCardException(rapdu.prepareSwFormatted(), nfcApduRunnerMock.getLastSentAPDU());
                Assert.assertEquals(e.getMessage(), errMsg);
            }
        }
    }

    @Test
    public void testCoinManagerAppletOperationsFails() throws Exception {
        RAPDU rapdu = new RAPDU(BYTE_ARRAY_HELPER.hex(ErrorCodes.SW_INS_NOT_SUPPORTED));
        NfcApduRunner nfcApduRunnerMock = prepareNfcApduRunnerMock(nfcApduRunner);
        IsoDep tag = prepareTagMock();
        when(tag.transceive(SELECT_COIN_MANAGER_APDU.getBytes()))
                .thenReturn(BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_INS_NOT_SUPPORTED));
        nfcApduRunnerMock.setCardTag(tag);
        setNfcApduRunner(nfcApduRunnerMock);
        for(int i = 0 ; i < cardCoinManagerOperationsList.size(); i++) {
            CardApiInterface<List<String>> op = cardCoinManagerOperationsList.get(i);
            List<String> args = cardOpsMap.get(op);
            try {
                op.accept(args);
                fail();
            }
            catch (Exception e){
                System.out.println(e.getMessage());
                String errMsg = JSON_HELPER.createErrorJsonForCardException(rapdu.prepareSwFormatted(), nfcApduRunnerMock.getLastSentAPDU());
                Assert.assertEquals(e.getMessage(), errMsg);
            }
        }
    }


}