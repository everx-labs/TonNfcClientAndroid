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
import com.tonnfccard.smartcard.ApduRunner;
import com.tonnfccard.utils.ByteArrayUtil;

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
import java.util.List;
import java.util.Random;

import static com.tonnfccard.NfcMockHelper.mockNfcAdapter;
import static com.tonnfccard.NfcMockHelper.mockNfcAdapterToBeNull;
import static com.tonnfccard.NfcMockHelper.prepareTagMock;
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
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_NO_NFC;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_NO_TAG;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_TRANSCEIVE;
import static com.tonnfccard.smartcard.CoinManagerApduCommands.LABEL_LENGTH;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = Build.VERSION_CODES.P)
@DoNotInstrument
public class CommonNfcApiTest {
    protected final ExceptionHelper EXCEPTION_HELPER = ExceptionHelper.getInstance();
    protected final StringHelper STRING_HELPER = StringHelper.getInstance();
    protected final ByteArrayUtil BYTE_ARRAY_HELPER = ByteArrayUtil.getInstance();
    protected Random random = new Random();
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
    private final CardApiInterface<List<String>> getHashOfEncryptedCommonSecret = list -> cardActivationApi.getHashOfEncryptedCommonSecretAndGetJson();
    private final CardApiInterface<List<String>> getHashOfEncryptedPassword = list -> cardActivationApi.getHashOfEncryptedPasswordAndGetJson();

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

    List<CardApiInterface<List<String>>> cardOperationsList = Arrays.asList(
            addRecoveryData,
            setDeviceLabel,
            generateSeed,
            changePin,
            turnOnWallet,
            sign,
            verifyPinAndSign,
            signForDefaultHdPath,
            verifyPinAndSignForDefaultHdPath,
            verifyPin,
            getPublicKey,
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
            getPublicKeyForDefaultPath,
            getDeviceLabel,
            getAppsList,
            getAvailableMemory,
            getSeVersion,
            getCsn,
            getMaxPinTries,
            getRemainingPinTries,
            getRootKeyStatus,
            resetWallet,
            getRecoveryData,
            getRecoveryDataHash,
            getRecoveryDataLen,
            isRecoveryDataSet,
            resetRecovery,
            getHashOfEncryptedCommonSecret,
            getHashOfEncryptedPassword,
            getSault,
            getSerialNumber,
            getTonAppletState
            );

    @Before
    public  void init() throws Exception {
        Context context = ApplicationProvider.getApplicationContext();
        nfcApduRunner = NfcApduRunner.getInstance(context);
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

    private List<String> prepareArgs(int i) {
        return i == 0 ? Collections.singletonList(STRING_HELPER.randomHexString(2 * RECOVERY_DATA_MAX_SIZE))
                : i == 1 ? Collections.singletonList(STRING_HELPER.randomHexString(2 * LABEL_LENGTH))
                : i == 2 ? Collections.singletonList(DEFAULT_PIN_STR)
                : i == 3 ? Arrays.asList(DEFAULT_PIN_STR, DEFAULT_PIN_STR)
                : i == 4 ? Arrays.asList(DEFAULT_PIN_STR, STRING_HELPER.randomHexString(2 * PASSWORD_SIZE), STRING_HELPER.randomHexString(2 * COMMON_SECRET_SIZE), STRING_HELPER.randomHexString(2 * IV_SIZE))
                : i == 5 ? Arrays.asList(STRING_HELPER.randomHexString(2 * DATA_FOR_SIGNING_MAX_SIZE_FOR_CASE_WITH_PATH), "1")
                : i == 6 ? Arrays.asList(STRING_HELPER.randomHexString(2 * DATA_FOR_SIGNING_MAX_SIZE_FOR_CASE_WITH_PATH), "2", DEFAULT_PIN_STR)
                : i == 7 ? Collections.singletonList(STRING_HELPER.randomHexString(2 * DATA_FOR_SIGNING_MAX_SIZE_FOR_CASE_WITH_PATH))
                : i == 8 ? Arrays.asList(STRING_HELPER.randomHexString(2 * DATA_FOR_SIGNING_MAX_SIZE_FOR_CASE_WITH_PATH), DEFAULT_PIN_STR)
                : i == 9 ? Collections.singletonList(DEFAULT_PIN_STR)
                : i == 10 ? Collections.singletonList("3")
                : i == 11 ? Arrays.asList(STRING_HELPER.randomHexString(2 * MAX_KEY_SIZE_IN_KEYCHAIN), STRING_HELPER.randomHexString(2 * SHA_HASH_SIZE))
                : i == 12 ? Collections.singletonList(STRING_HELPER.randomHexString(2 * MAX_KEY_SIZE_IN_KEYCHAIN))
                : i == 13 || i == 14 || i == 15 || i == 16? Collections.singletonList(STRING_HELPER.randomHexString(2 * SHA_HASH_SIZE))
                : i == 17 || i == 18 ? Collections.singletonList("100")
                : Collections.emptyList();
    }

    private void prepareNfcTest(String errMsg) {
        for(int i = 0 ; i < cardOperationsList.size(); i++) {
            List<String> args = prepareArgs(i);
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
        prepareNfcTest(ERROR_MSG_NO_NFC);
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
}