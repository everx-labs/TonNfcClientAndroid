package com.tonnfccard.smartcard;

import android.security.keystore.KeyProperties;
import android.security.keystore.KeyProtection;

import com.tonnfccard.helpers.HmacHelper;
import com.tonnfccard.utils.ByteArrayUtil;

import static com.tonnfccard.TonWalletConstants.DATA_FOR_SIGNING_MAX_SIZE;
import static com.tonnfccard.TonWalletConstants.DATA_FOR_SIGNING_MAX_SIZE_FOR_CASE_WITH_PATH;
import static com.tonnfccard.TonWalletConstants.DATA_PORTION_MAX_SIZE;
import static com.tonnfccard.TonWalletConstants.DATA_RECOVERY_PORTION_MAX_SIZE;
import static com.tonnfccard.TonWalletConstants.DEFAULT_PIN;
import static com.tonnfccard.TonWalletConstants.HMAC_SHA_SIG_SIZE;
import static com.tonnfccard.TonWalletConstants.IV_SIZE;
import static com.tonnfccard.TonWalletConstants.KEYCHAIN_KEY_INDEX_LEN;
import static com.tonnfccard.TonWalletConstants.MAX_HD_INDEX_SIZE;
import static com.tonnfccard.TonWalletConstants.MAX_KEY_SIZE_IN_KEYCHAIN;
import static com.tonnfccard.TonWalletConstants.PASSWORD_SIZE;
import static com.tonnfccard.TonWalletConstants.PIN_SIZE;
import static com.tonnfccard.TonWalletConstants.PUBLIC_KEY_LEN;
import static com.tonnfccard.TonWalletConstants.SAULT_LENGTH;
import static com.tonnfccard.TonWalletConstants.SIG_LEN;
import static com.tonnfccard.helpers.HmacHelper.ANDROID_KEYSTORE;
import static com.tonnfccard.helpers.HmacHelperTest.HMAC_HELPER;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_ACTIVATION_PASSWORD_BYTES_SIZE_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_APDU_P1_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_DATA_BYTES_SIZE_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_DATA_WITH_HD_PATH_BYTES_SIZE_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_HD_INDEX_BYTES_SIZE_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_INITIAL_VECTOR_BYTES_SIZE_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_KEY_CHUNK_BYTES_SIZE_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_KEY_INDEX_BYTES_SIZE_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_KEY_MAC_BYTES_SIZE_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_KEY_SIZE_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_PIN_BYTES_SIZE_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_RECOVERY_DATA_MAC_BYTES_SIZE_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_RECOVER_DATA_PORTION_SIZE_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_SAULT_BYTES_SIZE_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_START_POSITION_BYTES_SIZE_INCORRECT;
import static com.tonnfccard.smartcard.CommonConstants.NEGATIVE_LE;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import java.security.KeyStore;
import java.util.Random;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class TonWalletAppletApduCommandsTest {

    private final static ByteArrayUtil BYTE_ARRAY_HELPER = ByteArrayUtil.getInstance();
    private Random random = new Random();

    @Before
    public void prepareKeystore(){
        try{
            byte[] key = new byte[32];
            final SecretKey hmacSha256Key = new SecretKeySpec(key, 0, key.length, KeyProperties.KEY_ALGORITHM_HMAC_SHA256);
            final KeyStore keyStore = KeyStore.getInstance(ANDROID_KEYSTORE);
            String keyAlias = HmacHelper.HMAC_KEY_ALIAS + "222333";
            keyStore.load(null);
            if (keyStore.containsAlias(keyAlias)) keyStore.deleteEntry(keyAlias);
            keyStore.setEntry(keyAlias,
                    new KeyStore.SecretKeyEntry(hmacSha256Key),
                    new KeyProtection.Builder(KeyProperties.PURPOSE_SIGN)
                            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                            .build());
            HMAC_HELPER.setCurrentSerialNumber("222333");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testGetVerifyPasswordAPDUIncorrectInputData() {
        try {
            getVerifyPasswordAPDU(null, new byte[IV_SIZE]);
            fail();
        }
        catch (Exception e) {
            assertEquals(e.getMessage(), ERROR_MSG_ACTIVATION_PASSWORD_BYTES_SIZE_INCORRECT);
        }

        try {
            getVerifyPasswordAPDU(new byte[PASSWORD_SIZE - 1], new byte[IV_SIZE]);
            fail();
        }
        catch (Exception e) {
            assertEquals(e.getMessage(), ERROR_MSG_ACTIVATION_PASSWORD_BYTES_SIZE_INCORRECT);
        }

        try {
            getVerifyPasswordAPDU(new byte[PASSWORD_SIZE + 1], new byte[IV_SIZE]);
            fail();
        }
        catch (Exception e) {
            assertEquals(e.getMessage(), ERROR_MSG_ACTIVATION_PASSWORD_BYTES_SIZE_INCORRECT);
        }

        try {
            getVerifyPasswordAPDU(new byte[PASSWORD_SIZE], null);
            fail();
        }
        catch (Exception e) {
            assertEquals(e.getMessage(), ERROR_MSG_INITIAL_VECTOR_BYTES_SIZE_INCORRECT);
        }

        try {
            getVerifyPasswordAPDU(new byte[PASSWORD_SIZE], new byte[IV_SIZE - 1]);
            fail();
        }
        catch (Exception e) {
            assertEquals(e.getMessage(), ERROR_MSG_INITIAL_VECTOR_BYTES_SIZE_INCORRECT);
        }

        try {
            getVerifyPasswordAPDU(new byte[PASSWORD_SIZE], new byte[IV_SIZE + 1]);
            fail();
        }
        catch (Exception e) {
            assertEquals(e.getMessage(), ERROR_MSG_INITIAL_VECTOR_BYTES_SIZE_INCORRECT);
        }
    }

    @Test
    public void testGetVerifyPasswordAPDU() {
        try {
            CAPDU capdu = getVerifyPasswordAPDU(new byte[PASSWORD_SIZE], new byte[IV_SIZE]);
            byte[] data = BYTE_ARRAY_HELPER.bConcat(new byte[PASSWORD_SIZE], new byte[IV_SIZE]);
            checkAPDU(capdu, INS_VERIFY_PASSWORD, P1, P2, data, NEGATIVE_LE);
        }
        catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testGetVerifyPinAPDU() {
        try {
            CAPDU capdu = getVerifyPinAPDU(DEFAULT_PIN, new byte[SAULT_LENGTH]);
            byte[] data = prepareApduData(BYTE_ARRAY_HELPER.bConcat(DEFAULT_PIN, new byte[SAULT_LENGTH]));
            checkAPDU(capdu, INS_VERIFY_PIN, P1, P2, data, NEGATIVE_LE);
        }
        catch (Exception e) {
            fail();
        }
    }


    @Test
    public void testGetVerifyPinAPDUIncorrectInputData() {
        try {
            getVerifyPinAPDU(null, new byte[SAULT_LENGTH]);
            fail();
        }
        catch (Exception e) {
            assertEquals(e.getMessage(), ERROR_MSG_PIN_BYTES_SIZE_INCORRECT);
        }

        try {
            getVerifyPinAPDU(new byte[PIN_SIZE - 1], new byte[SAULT_LENGTH]);
            fail();
        }
        catch (Exception e) {
            assertEquals(e.getMessage(), ERROR_MSG_PIN_BYTES_SIZE_INCORRECT);
        }

        try {
            getVerifyPinAPDU(new byte[PIN_SIZE + 1], new byte[SAULT_LENGTH]);
            fail();
        }
        catch (Exception e) {
            assertEquals(e.getMessage(), ERROR_MSG_PIN_BYTES_SIZE_INCORRECT);
        }

        try {
            getVerifyPinAPDU(new byte[PIN_SIZE], null);
            fail();
        }
        catch (Exception e) {
            assertEquals(e.getMessage(), ERROR_MSG_SAULT_BYTES_SIZE_INCORRECT);
        }

        try {
            getVerifyPinAPDU(new byte[PIN_SIZE], new byte[SAULT_LENGTH - 1]);
            fail();
        }
        catch (Exception e) {
            assertEquals(e.getMessage(), ERROR_MSG_SAULT_BYTES_SIZE_INCORRECT);
        }

        try {
            getVerifyPinAPDU(new byte[PIN_SIZE], new byte[SAULT_LENGTH + 1]);
            fail();
        }
        catch (Exception e) {
            assertEquals(e.getMessage(), ERROR_MSG_SAULT_BYTES_SIZE_INCORRECT);
        }
    }



    @Test
    public void testGetSignShortMessageWithDefaultPathAPDU() {
        try {
            CAPDU capdu = getSignShortMessageWithDefaultPathAPDU(new byte[1], new byte[SAULT_LENGTH]);
            byte[] data = prepareApduData(BYTE_ARRAY_HELPER.bConcat(new byte[]{0x00, 0x01}, new byte[1], new byte[SAULT_LENGTH]));
            checkAPDU(capdu, INS_SIGN_SHORT_MESSAGE_WITH_DEFAULT_PATH, P1, P2, data, SIG_LEN);
        }
        catch (Exception e) {
            fail();
        }
    }


    @Test
    public void testGetSignShortMessageWithDefaultPathAPDUIncorrectInputData() {
        try {
            getSignShortMessageWithDefaultPathAPDU(null,  new byte[SAULT_LENGTH]);
            fail();
        }
        catch (Exception e) {
            assertEquals(e.getMessage(), ERROR_MSG_DATA_BYTES_SIZE_INCORRECT);
        }

        try {
            getSignShortMessageWithDefaultPathAPDU(new byte[0], new byte[SAULT_LENGTH]);
            fail();
        }
        catch (Exception e) {
            assertEquals(e.getMessage(), ERROR_MSG_DATA_BYTES_SIZE_INCORRECT);
        }

        try {
            getSignShortMessageWithDefaultPathAPDU(new byte[DATA_FOR_SIGNING_MAX_SIZE + 1], new byte[SAULT_LENGTH]);
            fail();
        }
        catch (Exception e) {
            assertEquals(e.getMessage(), ERROR_MSG_DATA_BYTES_SIZE_INCORRECT);
        }

        try {
            getSignShortMessageWithDefaultPathAPDU(new byte[DATA_FOR_SIGNING_MAX_SIZE], null);
            fail();
        }
        catch (Exception e) {
            assertEquals(e.getMessage(), ERROR_MSG_SAULT_BYTES_SIZE_INCORRECT);
        }

        try {
            getSignShortMessageWithDefaultPathAPDU(new byte[DATA_FOR_SIGNING_MAX_SIZE], new byte[SAULT_LENGTH - 1]);
            fail();
        }
        catch (Exception e) {
            assertEquals(e.getMessage(), ERROR_MSG_SAULT_BYTES_SIZE_INCORRECT);
        }

        try {
            getSignShortMessageWithDefaultPathAPDU(new byte[DATA_FOR_SIGNING_MAX_SIZE], new byte[SAULT_LENGTH + 1]);
            fail();
        }
        catch (Exception e) {
            assertEquals(e.getMessage(), ERROR_MSG_SAULT_BYTES_SIZE_INCORRECT);
        }
    }


    @Test
    public void testGetSignShortMessageAPDU() {
        try {
            CAPDU capdu = getSignShortMessageAPDU(new byte[1], new byte[2], new byte[SAULT_LENGTH]);
            byte[] data = prepareApduData(BYTE_ARRAY_HELPER.bConcat(new byte[]{0x00, 0x01}, new byte[1], new byte[]{0x02}, new byte[2], new byte[SAULT_LENGTH]));
            checkAPDU(capdu, INS_SIGN_SHORT_MESSAGE, P1, P2, data, SIG_LEN);
        }
        catch (Exception e) {
            fail();
        }
    }


    @Test
    public void testGetSignShortMessageAPDUIncorrectInputData() {
        try {
            getSignShortMessageAPDU(null,  new byte[2], new byte[SAULT_LENGTH]);
            fail();
        }
        catch (Exception e) {
            assertEquals(e.getMessage(), ERROR_MSG_DATA_WITH_HD_PATH_BYTES_SIZE_INCORRECT);
        }

        try {
            getSignShortMessageAPDU(new byte[0], new byte[2], new byte[SAULT_LENGTH]);
            fail();
        }
        catch (Exception e) {
            assertEquals(e.getMessage(), ERROR_MSG_DATA_WITH_HD_PATH_BYTES_SIZE_INCORRECT);
        }

        try {
            getSignShortMessageAPDU(new byte[DATA_FOR_SIGNING_MAX_SIZE_FOR_CASE_WITH_PATH + 1], new byte[2], new byte[SAULT_LENGTH]);
            fail();
        }
        catch (Exception e) {
            assertEquals(e.getMessage(), ERROR_MSG_DATA_WITH_HD_PATH_BYTES_SIZE_INCORRECT);
        }

        try {
            getSignShortMessageAPDU(new byte[DATA_FOR_SIGNING_MAX_SIZE_FOR_CASE_WITH_PATH], new byte[2], null);
            fail();
        }
        catch (Exception e) {
            assertEquals(e.getMessage(), ERROR_MSG_SAULT_BYTES_SIZE_INCORRECT);
        }

        try {
            getSignShortMessageAPDU(new byte[DATA_FOR_SIGNING_MAX_SIZE_FOR_CASE_WITH_PATH], new byte[2], new byte[SAULT_LENGTH - 1]);
            fail();
        }
        catch (Exception e) {
            assertEquals(e.getMessage(), ERROR_MSG_SAULT_BYTES_SIZE_INCORRECT);
        }

        try {
            getSignShortMessageAPDU(new byte[DATA_FOR_SIGNING_MAX_SIZE_FOR_CASE_WITH_PATH], new byte[2], new byte[SAULT_LENGTH + 1]);
            fail();
        }
        catch (Exception e) {
            assertEquals(e.getMessage(), ERROR_MSG_SAULT_BYTES_SIZE_INCORRECT);
        }

        try {
            getSignShortMessageAPDU(new byte[DATA_FOR_SIGNING_MAX_SIZE_FOR_CASE_WITH_PATH], null, new byte[SAULT_LENGTH]);
            fail();
        }
        catch (Exception e) {
            assertEquals(e.getMessage(), ERROR_MSG_HD_INDEX_BYTES_SIZE_INCORRECT);
        }

        try {
            getSignShortMessageAPDU(new byte[DATA_FOR_SIGNING_MAX_SIZE_FOR_CASE_WITH_PATH], new byte[0], new byte[SAULT_LENGTH]);
            fail();
        }
        catch (Exception e) {
            assertEquals(e.getMessage(), ERROR_MSG_HD_INDEX_BYTES_SIZE_INCORRECT);
        }

        try {
            getSignShortMessageAPDU(new byte[DATA_FOR_SIGNING_MAX_SIZE_FOR_CASE_WITH_PATH], new byte[MAX_HD_INDEX_SIZE + 1], new byte[SAULT_LENGTH]);
            fail();
        }
        catch (Exception e) {
            assertEquals(e.getMessage(), ERROR_MSG_HD_INDEX_BYTES_SIZE_INCORRECT);
        }
    }

    @Test
    public void testGetPublicKeyAPDU() {
        try {
            CAPDU capdu = getPublicKeyAPDU(new byte[MAX_HD_INDEX_SIZE]);
            checkAPDU(capdu, INS_GET_PUBLIC_KEY, P1, P2, new byte[MAX_HD_INDEX_SIZE], PUBLIC_KEY_LEN);
        }
        catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testGetPublicKeyAPDUIncorrectInputData() {
        try {
            getPublicKeyAPDU(null);
            fail();
        }
        catch (Exception e) {
            assertEquals(e.getMessage(), ERROR_MSG_HD_INDEX_BYTES_SIZE_INCORRECT);
        }

        try {
            getPublicKeyAPDU(new byte[0]);
            fail();
        }
        catch (Exception e) {
            assertEquals(e.getMessage(), ERROR_MSG_HD_INDEX_BYTES_SIZE_INCORRECT);
        }

        try {
            getPublicKeyAPDU(new byte[MAX_HD_INDEX_SIZE + 1]);
            fail();
        }
        catch (Exception e) {
            assertEquals(e.getMessage(), ERROR_MSG_HD_INDEX_BYTES_SIZE_INCORRECT);
        }
    }


    @Test
    public void testGetAddRecoveryDataPartAPDU() {
        try {
            for (byte p1 = 0 ; p1 < 2; p1++) {
                byte[] data = new byte[DATA_RECOVERY_PORTION_MAX_SIZE];
                random.nextBytes(data);
                checkAPDU(getAddRecoveryDataPartAPDU(p1, data), INS_ADD_RECOVERY_DATA_PART, p1, P2, data, NEGATIVE_LE);
            }
            byte[] data = new byte[HMAC_SHA_SIG_SIZE];
            random.nextBytes(data);
            checkAPDU(getAddRecoveryDataPartAPDU((byte) 0x02, data), INS_ADD_RECOVERY_DATA_PART, (byte) 0x02, P2, data, NEGATIVE_LE);
        }
        catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testGetAddRecoveryDataPartAPDUIncorrectInputData() {
        try {
            getAddRecoveryDataPartAPDU((byte) -1, null);
            fail();
        }
        catch (Exception e) {
            assertEquals(e.getMessage(), ERROR_MSG_APDU_P1_INCORRECT);
        }

        try {
            getAddRecoveryDataPartAPDU((byte) 3, null);
            fail();
        }
        catch (Exception e) {
            assertEquals(e.getMessage(), ERROR_MSG_APDU_P1_INCORRECT);
        }


        try {
            getAddRecoveryDataPartAPDU(P1, null);
            fail();
        }
        catch (Exception e) {
            assertEquals(e.getMessage(), ERROR_MSG_RECOVER_DATA_PORTION_SIZE_INCORRECT);
        }

        try {
            getAddRecoveryDataPartAPDU(P1, new byte[0]);
            fail();
        }
        catch (Exception e) {
            assertEquals(e.getMessage(), ERROR_MSG_RECOVER_DATA_PORTION_SIZE_INCORRECT);
        }

        try {
            getAddRecoveryDataPartAPDU(P1, new byte[DATA_RECOVERY_PORTION_MAX_SIZE + 1]);
            fail();
        }
        catch (Exception e) {
            assertEquals(e.getMessage(), ERROR_MSG_RECOVER_DATA_PORTION_SIZE_INCORRECT);
        }

        try {
            getAddRecoveryDataPartAPDU((byte) 0x02, null);
            fail();
        }
        catch (Exception e) {
            assertEquals(e.getMessage(), ERROR_MSG_RECOVERY_DATA_MAC_BYTES_SIZE_INCORRECT);
        }

        try {
            getAddRecoveryDataPartAPDU((byte) 0x02, new byte[HMAC_SHA_SIG_SIZE + 1]);
            fail();
        }
        catch (Exception e) {
            assertEquals(e.getMessage(), ERROR_MSG_RECOVERY_DATA_MAC_BYTES_SIZE_INCORRECT);
        }

    }


    @Test
    public void testGetGetRecoveryDataPartAPDU() {
        try {
            CAPDU capdu = getGetRecoveryDataPartAPDU(new byte[2], (byte) DATA_PORTION_MAX_SIZE);
            checkAPDU(capdu, INS_GET_RECOVERY_DATA_PART, P1, P2, new byte[2], DATA_PORTION_MAX_SIZE);
        }
        catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testGetGetRecoveryDataAPDUIncorrectInputData() {
        try {
            getGetRecoveryDataPartAPDU(null, (byte) DATA_PORTION_MAX_SIZE);
            fail();
        }
        catch (Exception e) {
            assertEquals(e.getMessage(), ERROR_MSG_START_POSITION_BYTES_SIZE_INCORRECT);
        }

        try {
            getGetRecoveryDataPartAPDU(new byte[1], (byte) DATA_PORTION_MAX_SIZE);
            fail();
        }
        catch (Exception e) {
            assertEquals(e.getMessage(), ERROR_MSG_START_POSITION_BYTES_SIZE_INCORRECT);
        }

        try {
            getGetRecoveryDataPartAPDU(new byte[3], (byte) DATA_PORTION_MAX_SIZE);
            fail();
        }
        catch (Exception e) {
            assertEquals(e.getMessage(), ERROR_MSG_START_POSITION_BYTES_SIZE_INCORRECT);
        }

    }

    @Test
    public void testGetResetKeyChainAPDU() {
        try {
            byte[] sault = new byte[SAULT_LENGTH];
            random.nextBytes(sault);
            byte[] data = BYTE_ARRAY_HELPER.bConcat(sault, HMAC_HELPER.computeMac(sault));
            checkAPDU(getResetKeyChainAPDU(sault), INS_RESET_KEYCHAIN, P1, P2, data, NEGATIVE_LE);
        }
        catch (Exception e) {
            fail();
        }
    }


    @Test
    public void testGetResetKeyChainAPDUIncorrectInputData() {
        try {
            getResetKeyChainAPDU(null);
            fail();
        }
        catch (Exception e) {
            assertEquals(e.getMessage(), ERROR_MSG_SAULT_BYTES_SIZE_INCORRECT);
        }
        try {
            getResetKeyChainAPDU(new byte[SAULT_LENGTH + 1]);
            fail();
        }
        catch (Exception e) {
            assertEquals(e.getMessage(), ERROR_MSG_SAULT_BYTES_SIZE_INCORRECT);
        }
        try {
            getResetKeyChainAPDU(new byte[SAULT_LENGTH - 1]);
            fail();
        }
        catch (Exception e) {
            assertEquals(e.getMessage(), ERROR_MSG_SAULT_BYTES_SIZE_INCORRECT);
        }
    }

    @Test
    public void testGetNumberOfKeysAPDU() {
        try {
            byte[] sault = new byte[SAULT_LENGTH];
            random.nextBytes(sault);
            byte[] data = BYTE_ARRAY_HELPER.bConcat(sault, HMAC_HELPER.computeMac(sault));
            checkAPDU(getNumberOfKeysAPDU(sault), INS_GET_NUMBER_OF_KEYS, P1, P2, data, GET_NUMBER_OF_KEYS_LE);
        }
        catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testGetNumberOfKeysAPDUIncorrectInputData() {
        try {
            getNumberOfKeysAPDU(null);
            fail();
        }
        catch (Exception e) {
            assertEquals(e.getMessage(), ERROR_MSG_SAULT_BYTES_SIZE_INCORRECT);
        }
        try {
            getNumberOfKeysAPDU(new byte[SAULT_LENGTH + 1]);
            fail();
        }
        catch (Exception e) {
            assertEquals(e.getMessage(), ERROR_MSG_SAULT_BYTES_SIZE_INCORRECT);
        }
        try {
            getNumberOfKeysAPDU(new byte[SAULT_LENGTH - 1]);
            fail();
        }
        catch (Exception e) {
            assertEquals(e.getMessage(), ERROR_MSG_SAULT_BYTES_SIZE_INCORRECT);
        }
    }


    @Test
    public void testGetGetOccupiedSizeAPDU() {
        try {
            byte[] sault = new byte[SAULT_LENGTH];
            random.nextBytes(sault);
            byte[] data = BYTE_ARRAY_HELPER.bConcat(sault, HMAC_HELPER.computeMac(sault));
            checkAPDU(getGetOccupiedSizeAPDU(sault), INS_GET_OCCUPIED_STORAGE_SIZE, P1, P2, data, GET_OCCUPIED_SIZE_LE);
        }
        catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testGetGetOccupiedSizeAPDUIncorrectInputData() {
        try {
            getGetOccupiedSizeAPDU(null);
            fail();
        }
        catch (Exception e) {
            assertEquals(e.getMessage(), ERROR_MSG_SAULT_BYTES_SIZE_INCORRECT);
        }
        try {
            getGetOccupiedSizeAPDU(new byte[SAULT_LENGTH + 1]);
            fail();
        }
        catch (Exception e) {
            assertEquals(e.getMessage(), ERROR_MSG_SAULT_BYTES_SIZE_INCORRECT);
        }
        try {
            getGetOccupiedSizeAPDU(new byte[SAULT_LENGTH - 1]);
            fail();
        }
        catch (Exception e) {
            assertEquals(e.getMessage(), ERROR_MSG_SAULT_BYTES_SIZE_INCORRECT);
        }
    }

    @Test
    public void testGetGetFreeSizeAPDU() {
        try {
            byte[] sault = new byte[SAULT_LENGTH];
            random.nextBytes(sault);
            byte[] data = BYTE_ARRAY_HELPER.bConcat(sault, HMAC_HELPER.computeMac(sault));
            checkAPDU(getGetFreeSizeAPDU(sault), INS_GET_FREE_STORAGE_SIZE, P1, P2, data, GET_FREE_SIZE_LE);
        }
        catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testGetGetFreeSizeAPDUIncorrectInputData() {
        try {
            getGetFreeSizeAPDU(null);
            fail();
        }
        catch (Exception e) {
            assertEquals(e.getMessage(), ERROR_MSG_SAULT_BYTES_SIZE_INCORRECT);
        }
        try {
            getGetFreeSizeAPDU(new byte[SAULT_LENGTH + 1]);
            fail();
        }
        catch (Exception e) {
            assertEquals(e.getMessage(), ERROR_MSG_SAULT_BYTES_SIZE_INCORRECT);
        }
        try {
            getGetFreeSizeAPDU(new byte[SAULT_LENGTH - 1]);
            fail();
        }
        catch (Exception e) {
            assertEquals(e.getMessage(), ERROR_MSG_SAULT_BYTES_SIZE_INCORRECT);
        }
    }


    @Test
    public void testGetCheckKeyHmacConsistencyAPDU() {
        try {
            byte[] sault = new byte[SAULT_LENGTH];
            random.nextBytes(sault);
            byte[] mac = new byte[HMAC_SHA_SIG_SIZE];
            random.nextBytes(mac);
            byte[] data = prepareApduData(BYTE_ARRAY_HELPER.bConcat(mac, sault));
            checkAPDU(getCheckKeyHmacConsistencyAPDU(mac, sault), INS_CHECK_KEY_HMAC_CONSISTENCY, P1, P2, data, NEGATIVE_LE);
        }
        catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testGetCheckKeyHmacConsistencyAPDUIncorrectInputData() {
        try {
            getCheckKeyHmacConsistencyAPDU(new byte[HMAC_SHA_SIG_SIZE], null);
            fail();
        }
        catch (Exception e) {
            assertEquals(e.getMessage(), ERROR_MSG_SAULT_BYTES_SIZE_INCORRECT);
        }
        try {
            getCheckKeyHmacConsistencyAPDU(new byte[HMAC_SHA_SIG_SIZE], new byte[SAULT_LENGTH - 1]);
            fail();
        }
        catch (Exception e) {
            assertEquals(e.getMessage(), ERROR_MSG_SAULT_BYTES_SIZE_INCORRECT);
        }
        try {
            getCheckKeyHmacConsistencyAPDU(new byte[HMAC_SHA_SIG_SIZE], new byte[SAULT_LENGTH + 1]);
            fail();
        }
        catch (Exception e) {
            assertEquals(e.getMessage(), ERROR_MSG_SAULT_BYTES_SIZE_INCORRECT);
        }

        try {
            getCheckKeyHmacConsistencyAPDU( null, new byte[SAULT_LENGTH]);
            fail();
        }
        catch (Exception e) {
            assertEquals(e.getMessage(), ERROR_MSG_KEY_MAC_BYTES_SIZE_INCORRECT);
        }
        try {
            getCheckKeyHmacConsistencyAPDU(new byte[HMAC_SHA_SIG_SIZE - 1], new byte[SAULT_LENGTH]);
            fail();
        }
        catch (Exception e) {
            assertEquals(e.getMessage(), ERROR_MSG_KEY_MAC_BYTES_SIZE_INCORRECT);
        }
        try {
            getCheckKeyHmacConsistencyAPDU(new byte[HMAC_SHA_SIG_SIZE + 1], new byte[SAULT_LENGTH]);
            fail();
        }
        catch (Exception e) {
            assertEquals(e.getMessage(), ERROR_MSG_KEY_MAC_BYTES_SIZE_INCORRECT);
        }
    }


    @Test
    public void testGetGetIndexAndLenOfKeyInKeyChainAPDU() {
        try {
            byte[] sault = new byte[SAULT_LENGTH];
            random.nextBytes(sault);
            byte[] mac = new byte[HMAC_SHA_SIG_SIZE];
            random.nextBytes(mac);
            byte[] data = prepareApduData(BYTE_ARRAY_HELPER.bConcat(mac, sault));
            checkAPDU(getGetIndexAndLenOfKeyInKeyChainAPDU(mac, sault), INS_GET_KEY_INDEX_IN_STORAGE_AND_LEN, P1, P2, data, GET_KEY_INDEX_IN_STORAGE_AND_LEN_LE);
        }
        catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testGetGetIndexAndLenOfKeyInKeyChainAPDUIncorrectInputData() {
        try {
            getGetIndexAndLenOfKeyInKeyChainAPDU(new byte[HMAC_SHA_SIG_SIZE], null);
            fail();
        }
        catch (Exception e) {
            assertEquals(e.getMessage(), ERROR_MSG_SAULT_BYTES_SIZE_INCORRECT);
        }
        try {
            getGetIndexAndLenOfKeyInKeyChainAPDU(new byte[HMAC_SHA_SIG_SIZE], new byte[SAULT_LENGTH - 1]);
            fail();
        }
        catch (Exception e) {
            assertEquals(e.getMessage(), ERROR_MSG_SAULT_BYTES_SIZE_INCORRECT);
        }
        try {
            getGetIndexAndLenOfKeyInKeyChainAPDU(new byte[HMAC_SHA_SIG_SIZE], new byte[SAULT_LENGTH + 1]);
            fail();
        }
        catch (Exception e) {
            assertEquals(e.getMessage(), ERROR_MSG_SAULT_BYTES_SIZE_INCORRECT);
        }

        try {
            getGetIndexAndLenOfKeyInKeyChainAPDU( null, new byte[SAULT_LENGTH]);
            fail();
        }
        catch (Exception e) {
            assertEquals(e.getMessage(), ERROR_MSG_KEY_MAC_BYTES_SIZE_INCORRECT);
        }
        try {
            getGetIndexAndLenOfKeyInKeyChainAPDU(new byte[HMAC_SHA_SIG_SIZE - 1], new byte[SAULT_LENGTH]);
            fail();
        }
        catch (Exception e) {
            assertEquals(e.getMessage(), ERROR_MSG_KEY_MAC_BYTES_SIZE_INCORRECT);
        }
        try {
            getGetIndexAndLenOfKeyInKeyChainAPDU(new byte[HMAC_SHA_SIG_SIZE + 1], new byte[SAULT_LENGTH]);
            fail();
        }
        catch (Exception e) {
            assertEquals(e.getMessage(), ERROR_MSG_KEY_MAC_BYTES_SIZE_INCORRECT);
        }
    }


    @Test
    public void testGetInitiateChangeOfKeyAPDU() {
        try {
            byte[] sault = new byte[SAULT_LENGTH];
            random.nextBytes(sault);
            byte[] keyIndex = new byte[KEYCHAIN_KEY_INDEX_LEN];
            random.nextBytes(keyIndex);
            byte[] data = prepareApduData(BYTE_ARRAY_HELPER.bConcat(keyIndex, sault));
            checkAPDU(getInitiateChangeOfKeyAPDU(keyIndex, sault), INS_INITIATE_CHANGE_OF_KEY, P1, P2, data, NEGATIVE_LE);
        }
        catch (Exception e) {
            fail();
        }
    }


    @Test
    public void testGetInitiateChangeOfKeyAPDUIncorrectInputData() {
        try {
            getInitiateChangeOfKeyAPDU(new byte[KEYCHAIN_KEY_INDEX_LEN], null);
            fail();
        }
        catch (Exception e) {
            assertEquals(e.getMessage(), ERROR_MSG_SAULT_BYTES_SIZE_INCORRECT);
        }
        try {
            getInitiateChangeOfKeyAPDU(new byte[KEYCHAIN_KEY_INDEX_LEN], new byte[SAULT_LENGTH - 1]);
            fail();
        }
        catch (Exception e) {
            assertEquals(e.getMessage(), ERROR_MSG_SAULT_BYTES_SIZE_INCORRECT);
        }
        try {
            getInitiateChangeOfKeyAPDU(new byte[KEYCHAIN_KEY_INDEX_LEN], new byte[SAULT_LENGTH + 1]);
            fail();
        }
        catch (Exception e) {
            assertEquals(e.getMessage(), ERROR_MSG_SAULT_BYTES_SIZE_INCORRECT);
        }
        try {
            getInitiateChangeOfKeyAPDU( null, new byte[SAULT_LENGTH]);
            fail();
        }
        catch (Exception e) {
            assertEquals(e.getMessage(), ERROR_MSG_KEY_INDEX_BYTES_SIZE_INCORRECT);
        }
        try {
            getInitiateChangeOfKeyAPDU(new byte[KEYCHAIN_KEY_INDEX_LEN - 1], new byte[SAULT_LENGTH]);
            fail();
        }
        catch (Exception e) {
            assertEquals(e.getMessage(), ERROR_MSG_KEY_INDEX_BYTES_SIZE_INCORRECT);
        }
        try {
            getInitiateChangeOfKeyAPDU(new byte[KEYCHAIN_KEY_INDEX_LEN+ 1], new byte[SAULT_LENGTH]);
            fail();
        }
        catch (Exception e) {
            assertEquals(e.getMessage(), ERROR_MSG_KEY_INDEX_BYTES_SIZE_INCORRECT);
        }
    }


    @Test
    public void testGetInitiateDeleteOfKeyAPDU() {
        try {
            byte[] sault = new byte[SAULT_LENGTH];
            random.nextBytes(sault);
            byte[] keyIndex = new byte[KEYCHAIN_KEY_INDEX_LEN];
            random.nextBytes(keyIndex);
            byte[] data = prepareApduData(BYTE_ARRAY_HELPER.bConcat(keyIndex, sault));
            checkAPDU(getInitiateDeleteOfKeyAPDU(keyIndex, sault), INS_INITIATE_DELETE_KEY, P1, P2, data, INITIATE_DELETE_KEY_LE);
        }
        catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testGetInitiateDeleteOfKeyAPDUIncorrectInputData() {
        try {
            getInitiateDeleteOfKeyAPDU(new byte[KEYCHAIN_KEY_INDEX_LEN], null);
            fail();
        }
        catch (Exception e) {
            assertEquals(e.getMessage(), ERROR_MSG_SAULT_BYTES_SIZE_INCORRECT);
        }
        try {
            getInitiateDeleteOfKeyAPDU(new byte[KEYCHAIN_KEY_INDEX_LEN], new byte[SAULT_LENGTH - 1]);
            fail();
        }
        catch (Exception e) {
            assertEquals(e.getMessage(), ERROR_MSG_SAULT_BYTES_SIZE_INCORRECT);
        }
        try {
            getInitiateDeleteOfKeyAPDU(new byte[KEYCHAIN_KEY_INDEX_LEN], new byte[SAULT_LENGTH + 1]);
            fail();
        }
        catch (Exception e) {
            assertEquals(e.getMessage(), ERROR_MSG_SAULT_BYTES_SIZE_INCORRECT);
        }
        try {
            getInitiateDeleteOfKeyAPDU( null, new byte[SAULT_LENGTH]);
            fail();
        }
        catch (Exception e) {
            assertEquals(e.getMessage(), ERROR_MSG_KEY_INDEX_BYTES_SIZE_INCORRECT);
        }
        try {
            getInitiateDeleteOfKeyAPDU(new byte[KEYCHAIN_KEY_INDEX_LEN - 1], new byte[SAULT_LENGTH]);
            fail();
        }
        catch (Exception e) {
            assertEquals(e.getMessage(), ERROR_MSG_KEY_INDEX_BYTES_SIZE_INCORRECT);
        }
        try {
            getInitiateDeleteOfKeyAPDU(new byte[KEYCHAIN_KEY_INDEX_LEN+ 1], new byte[SAULT_LENGTH]);
            fail();
        }
        catch (Exception e) {
            assertEquals(e.getMessage(), ERROR_MSG_KEY_INDEX_BYTES_SIZE_INCORRECT);
        }
    }

    @Test
    public void testGetDeleteKeyChunkNumOfPacketsAPDU() {
        try {
            byte[] sault = new byte[SAULT_LENGTH];
            random.nextBytes(sault);
            byte[] data = BYTE_ARRAY_HELPER.bConcat(sault, HMAC_HELPER.computeMac(sault));
            checkAPDU(getDeleteKeyChunkNumOfPacketsAPDU(sault), INS_GET_DELETE_KEY_CHUNK_NUM_OF_PACKETS, P1, P2, data, GET_DELETE_KEY_CHUNK_NUM_OF_PACKETS_LE);
        }
        catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testGetDeleteKeyChunkNumOfPacketsAPDUIncorrectInputData() {
        try {
            getDeleteKeyChunkNumOfPacketsAPDU(null);
            fail();
        }
        catch (Exception e) {
            assertEquals(e.getMessage(), ERROR_MSG_SAULT_BYTES_SIZE_INCORRECT);
        }
        try {
            getDeleteKeyChunkNumOfPacketsAPDU(new byte[SAULT_LENGTH + 1]);
            fail();
        }
        catch (Exception e) {
            assertEquals(e.getMessage(), ERROR_MSG_SAULT_BYTES_SIZE_INCORRECT);
        }
        try {
            getDeleteKeyChunkNumOfPacketsAPDU(new byte[SAULT_LENGTH - 1]);
            fail();
        }
        catch (Exception e) {
            assertEquals(e.getMessage(), ERROR_MSG_SAULT_BYTES_SIZE_INCORRECT);
        }
    }

    @Test
    public void testGetDeleteKeyRecordNumOfPacketsAPDU() {
        try {
            byte[] sault = new byte[SAULT_LENGTH];
            random.nextBytes(sault);
            byte[] data = BYTE_ARRAY_HELPER.bConcat(sault, HMAC_HELPER.computeMac(sault));
            checkAPDU(getDeleteKeyRecordNumOfPacketsAPDU(sault), INS_GET_DELETE_KEY_RECORD_NUM_OF_PACKETS, P1, P2, data, GET_DELETE_KEY_RECORD_NUM_OF_PACKETS_LE);
        }
        catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testGetDeleteKeyRecordNumOfPacketsAPDUIncorrectInputData() {
        try {
            getDeleteKeyRecordNumOfPacketsAPDU(null);
            fail();
        }
        catch (Exception e) {
            assertEquals(e.getMessage(), ERROR_MSG_SAULT_BYTES_SIZE_INCORRECT);
        }
        try {
            getDeleteKeyRecordNumOfPacketsAPDU(new byte[SAULT_LENGTH + 1]);
            fail();
        }
        catch (Exception e) {
            assertEquals(e.getMessage(), ERROR_MSG_SAULT_BYTES_SIZE_INCORRECT);
        }
        try {
            getDeleteKeyRecordNumOfPacketsAPDU(new byte[SAULT_LENGTH - 1]);
            fail();
        }
        catch (Exception e) {
            assertEquals(e.getMessage(), ERROR_MSG_SAULT_BYTES_SIZE_INCORRECT);
        }
    }

    @Test
    public void testGetDeleteKeyRecordAPDU() {
        try {
            byte[] sault = new byte[SAULT_LENGTH];
            random.nextBytes(sault);
            byte[] data = BYTE_ARRAY_HELPER.bConcat(sault, HMAC_HELPER.computeMac(sault));
            checkAPDU(getDeleteKeyRecordAPDU(sault), INS_DELETE_KEY_RECORD, P1, P2, data, DELETE_KEY_RECORD_LE);
        }
        catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testGetDeleteKeyRecordAPDUIncorrectInputData() {
        try {
            getDeleteKeyRecordAPDU(null);
            fail();
        }
        catch (Exception e) {
            assertEquals(e.getMessage(), ERROR_MSG_SAULT_BYTES_SIZE_INCORRECT);
        }
        try {
            getDeleteKeyRecordAPDU(new byte[SAULT_LENGTH + 1]);
            fail();
        }
        catch (Exception e) {
            assertEquals(e.getMessage(), ERROR_MSG_SAULT_BYTES_SIZE_INCORRECT);
        }
        try {
            getDeleteKeyRecordAPDU(new byte[SAULT_LENGTH - 1]);
            fail();
        }
        catch (Exception e) {
            assertEquals(e.getMessage(), ERROR_MSG_SAULT_BYTES_SIZE_INCORRECT);
        }
    }


    @Test
    public void testGetDeleteKeyChunkAPDU() {
        try {
            byte[] sault = new byte[SAULT_LENGTH];
            random.nextBytes(sault);
            byte[] data = BYTE_ARRAY_HELPER.bConcat(sault, HMAC_HELPER.computeMac(sault));
            checkAPDU(getDeleteKeyChunkAPDU(sault), INS_DELETE_KEY_CHUNK, P1, P2, data, DELETE_KEY_CHUNK_LE);
        }
        catch (Exception e) {
            fail();
        }
    }


    @Test
    public void testGetDeleteKeyChunkAPDUIncorrectInputData() {
        try {
            getDeleteKeyChunkAPDU(null);
            fail();
        }
        catch (Exception e) {
            assertEquals(e.getMessage(), ERROR_MSG_SAULT_BYTES_SIZE_INCORRECT);
        }
        try {
            getDeleteKeyChunkAPDU(new byte[SAULT_LENGTH + 1]);
            fail();
        }
        catch (Exception e) {
            assertEquals(e.getMessage(), ERROR_MSG_SAULT_BYTES_SIZE_INCORRECT);
        }
        try {
            getDeleteKeyChunkAPDU(new byte[SAULT_LENGTH - 1]);
            fail();
        }
        catch (Exception e) {
            assertEquals(e.getMessage(), ERROR_MSG_SAULT_BYTES_SIZE_INCORRECT);
        }
    }


    @Test
    public void testGetGetHmacAPDU() {
        try {
            byte[] sault = new byte[SAULT_LENGTH];
            random.nextBytes(sault);
            byte[] keyIndex = new byte[KEYCHAIN_KEY_INDEX_LEN];
            random.nextBytes(keyIndex);
            byte[] data = prepareApduData(BYTE_ARRAY_HELPER.bConcat(keyIndex, sault));
            checkAPDU(getGetHmacAPDU(keyIndex, sault), INS_GET_HMAC, P1, P2, data, GET_HMAC_LE);
        }
        catch (Exception e) {
            fail();
        }
    }


    @Test
    public void testGetGetHmacAPDUIncorrectInputData() {
        try {
            getGetHmacAPDU(new byte[KEYCHAIN_KEY_INDEX_LEN], null);
            fail();
        }
        catch (Exception e) {
            assertEquals(e.getMessage(), ERROR_MSG_SAULT_BYTES_SIZE_INCORRECT);
        }
        try {
            getGetHmacAPDU(new byte[KEYCHAIN_KEY_INDEX_LEN], new byte[SAULT_LENGTH - 1]);
            fail();
        }
        catch (Exception e) {
            assertEquals(e.getMessage(), ERROR_MSG_SAULT_BYTES_SIZE_INCORRECT);
        }
        try {
            getGetHmacAPDU(new byte[KEYCHAIN_KEY_INDEX_LEN], new byte[SAULT_LENGTH + 1]);
            fail();
        }
        catch (Exception e) {
            assertEquals(e.getMessage(), ERROR_MSG_SAULT_BYTES_SIZE_INCORRECT);
        }
        try {
            getGetHmacAPDU( null, new byte[SAULT_LENGTH]);
            fail();
        }
        catch (Exception e) {
            assertEquals(e.getMessage(), ERROR_MSG_KEY_INDEX_BYTES_SIZE_INCORRECT);
        }
        try {
            getGetHmacAPDU(new byte[KEYCHAIN_KEY_INDEX_LEN - 1], new byte[SAULT_LENGTH]);
            fail();
        }
        catch (Exception e) {
            assertEquals(e.getMessage(), ERROR_MSG_KEY_INDEX_BYTES_SIZE_INCORRECT);
        }
        try {
            getGetHmacAPDU(new byte[KEYCHAIN_KEY_INDEX_LEN + 1], new byte[SAULT_LENGTH]);
            fail();
        }
        catch (Exception e) {
            assertEquals(e.getMessage(), ERROR_MSG_KEY_INDEX_BYTES_SIZE_INCORRECT);
        }
    }

    @Test
    public void testGetKeyChunkAPDU() {
        try {
            byte[] sault = new byte[SAULT_LENGTH];
            random.nextBytes(sault);
            byte[] keyIndex = new byte[KEYCHAIN_KEY_INDEX_LEN];
            random.nextBytes(keyIndex);
            short startPos = 0;
            byte[] data = prepareApduData(BYTE_ARRAY_HELPER.bConcat(keyIndex, new byte[]{(byte) (startPos >> 8), (byte) (startPos)}, sault));
            checkAPDU(getGetKeyChunkAPDU(keyIndex, startPos, sault, (byte) DATA_PORTION_MAX_SIZE), INS_GET_KEY_CHUNK, P1, P2, data, DATA_PORTION_MAX_SIZE);
        }
        catch (Exception e) {
            fail();
        }
    }


    @Test
    public void testGetKeyChunkAPDUIncorrectInputData() {
        short startPos = 0;
        try {
            getGetKeyChunkAPDU(new byte[KEYCHAIN_KEY_INDEX_LEN], startPos,null, (byte) DATA_PORTION_MAX_SIZE);
            fail();
        }
        catch (Exception e) {
            assertEquals(e.getMessage(), ERROR_MSG_SAULT_BYTES_SIZE_INCORRECT);
        }
        try {
            getGetKeyChunkAPDU(new byte[KEYCHAIN_KEY_INDEX_LEN], startPos, new byte[SAULT_LENGTH - 1], (byte) DATA_PORTION_MAX_SIZE);
            fail();
        }
        catch (Exception e) {
            assertEquals(e.getMessage(), ERROR_MSG_SAULT_BYTES_SIZE_INCORRECT);
        }
        try {
            getGetKeyChunkAPDU(new byte[KEYCHAIN_KEY_INDEX_LEN], startPos, new byte[SAULT_LENGTH + 1], (byte) DATA_PORTION_MAX_SIZE);
            fail();
        }
        catch (Exception e) {
            assertEquals(e.getMessage(), ERROR_MSG_SAULT_BYTES_SIZE_INCORRECT);
        }
        try {
            getGetKeyChunkAPDU( null, startPos, new byte[SAULT_LENGTH], (byte) DATA_PORTION_MAX_SIZE);
            fail();
        }
        catch (Exception e) {
            assertEquals(e.getMessage(), ERROR_MSG_KEY_INDEX_BYTES_SIZE_INCORRECT);
        }
        try {
            getGetKeyChunkAPDU(new byte[KEYCHAIN_KEY_INDEX_LEN - 1], startPos, new byte[SAULT_LENGTH], (byte) DATA_PORTION_MAX_SIZE);
            fail();
        }
        catch (Exception e) {
            assertEquals(e.getMessage(), ERROR_MSG_KEY_INDEX_BYTES_SIZE_INCORRECT);
        }
        try {
            getGetKeyChunkAPDU(new byte[KEYCHAIN_KEY_INDEX_LEN + 1], startPos, new byte[SAULT_LENGTH], (byte) DATA_PORTION_MAX_SIZE);
            fail();
        }
        catch (Exception e) {
            assertEquals(e.getMessage(), ERROR_MSG_KEY_INDEX_BYTES_SIZE_INCORRECT);
        }
    }

    @Test
    public void testGetSendKeyChunkAPDU() {
        try {
            byte[] instructions = new byte[]{INS_ADD_KEY_CHUNK, INS_CHANGE_KEY_CHUNK};
            for (int i = 0; i < instructions.length; i++) {
                for (byte p1 = 0 ; p1 < 2; p1++) {
                    byte[] keyChunk = new byte[DATA_PORTION_MAX_SIZE];
                    random.nextBytes(keyChunk);
                    byte[] sault = new byte[SAULT_LENGTH];
                    random.nextBytes(sault);
                    byte[] data = prepareApduData(BYTE_ARRAY_HELPER.bConcat(new byte[]{(byte) keyChunk .length}, keyChunk , sault));
                    checkAPDU(getSendKeyChunkAPDU(instructions[i], p1, keyChunk, sault), instructions[i], p1, P2, data, NEGATIVE_LE);
                }
                byte[] mac = new byte[HMAC_SHA_SIG_SIZE];
                random.nextBytes(mac);
                byte[] sault = new byte[SAULT_LENGTH];
                random.nextBytes(sault);
                byte[] data = prepareApduData(BYTE_ARRAY_HELPER.bConcat(mac, sault));
                checkAPDU(getSendKeyChunkAPDU(instructions[i], (byte) 0x02, mac, sault), instructions[i], (byte) 0x02, P2, data, SEND_CHUNK_LE);
            }
        }
        catch (Exception e) {
            fail();
        }
    }


    @Test
    public void testGetSendKeyChunkAPDUIncorrectInputData() {
        byte[] keyChunk = new byte[DATA_PORTION_MAX_SIZE];
        byte[] sault = new byte[SAULT_LENGTH];
        byte[] instructions = new byte[]{INS_ADD_KEY_CHUNK, INS_CHANGE_KEY_CHUNK};
        for (int i = 0; i < instructions.length; i++) {
            try {
                getSendKeyChunkAPDU(instructions[i], (byte) -1, keyChunk, sault);
                fail();
            }
            catch (Exception e) {
                assertEquals(e.getMessage(), ERROR_MSG_APDU_P1_INCORRECT);
            }
            try {
                getSendKeyChunkAPDU(instructions[i], (byte) 3, keyChunk, sault);
                fail();
            }
            catch (Exception e) {
                assertEquals(e.getMessage(), ERROR_MSG_APDU_P1_INCORRECT);
            }
            try {
                getSendKeyChunkAPDU(instructions[i], P1, null, sault);
                fail();
            }
            catch (Exception e) {
                assertEquals(e.getMessage(), ERROR_MSG_KEY_CHUNK_BYTES_SIZE_INCORRECT);
            }
            try {
                getSendKeyChunkAPDU(instructions[i], P1, new byte[0], sault);
                fail();
            }
            catch (Exception e) {
                assertEquals(e.getMessage(), ERROR_MSG_KEY_CHUNK_BYTES_SIZE_INCORRECT);
            }
            try {
                getSendKeyChunkAPDU(instructions[i], P1, new byte[DATA_PORTION_MAX_SIZE + 1], sault);
                fail();
            }
            catch (Exception e) {
                assertEquals(e.getMessage(), ERROR_MSG_KEY_CHUNK_BYTES_SIZE_INCORRECT);
            }
            try {
                getSendKeyChunkAPDU(instructions[i], (byte) 0x02, null, sault);
                fail();
            }
            catch (Exception e) {
                assertEquals(e.getMessage(), ERROR_MSG_KEY_MAC_BYTES_SIZE_INCORRECT);
            }
            try {
                getSendKeyChunkAPDU(instructions[i], (byte) 0x02, new byte[HMAC_SHA_SIG_SIZE - 1], sault);
                fail();
            }
            catch (Exception e) {
                assertEquals(e.getMessage(), ERROR_MSG_KEY_MAC_BYTES_SIZE_INCORRECT);
            }
            try {
                getSendKeyChunkAPDU(instructions[i], (byte) 0x02, new byte[HMAC_SHA_SIG_SIZE + 1], sault);
                fail();
            }
            catch (Exception e) {
                assertEquals(e.getMessage(), ERROR_MSG_KEY_MAC_BYTES_SIZE_INCORRECT);
            }
            try {
                getSendKeyChunkAPDU(instructions[i], P1, new byte[DATA_PORTION_MAX_SIZE], null);
                fail();
            }
            catch (Exception e) {
                assertEquals(e.getMessage(), ERROR_MSG_SAULT_BYTES_SIZE_INCORRECT);
            }
            try {
                getSendKeyChunkAPDU(instructions[i], P1, new byte[DATA_PORTION_MAX_SIZE], new byte[SAULT_LENGTH - 1]);
                fail();
            }
            catch (Exception e) {
                assertEquals(e.getMessage(), ERROR_MSG_SAULT_BYTES_SIZE_INCORRECT);
            }
            try {
                getSendKeyChunkAPDU(instructions[i], P1, new byte[DATA_PORTION_MAX_SIZE], new byte[SAULT_LENGTH + 1]);
                fail();
            }
            catch (Exception e) {
                assertEquals(e.getMessage(), ERROR_MSG_SAULT_BYTES_SIZE_INCORRECT);
            }
        }
    }

    @Test
    public void testGetCheckAvailableVolForNewKeyAPDU() {
        try {
            byte[] sault = new byte[SAULT_LENGTH];
            random.nextBytes(sault);
            byte[] data = prepareApduData(BYTE_ARRAY_HELPER.bConcat(new byte[]{(byte) (MAX_KEY_SIZE_IN_KEYCHAIN >> 8), (byte) (MAX_KEY_SIZE_IN_KEYCHAIN)}, sault));
            checkAPDU(getCheckAvailableVolForNewKeyAPDU(MAX_KEY_SIZE_IN_KEYCHAIN, sault), INS_CHECK_AVAILABLE_VOL_FOR_NEW_KEY, P1, P2, data, NEGATIVE_LE);
        }
        catch (Exception e) {
            fail();
        }
    }


    @Test
    public void testGetCheckAvailableVolForNewKeyAPDUIncorrectInputData() {
        try {
            getCheckAvailableVolForNewKeyAPDU(MAX_KEY_SIZE_IN_KEYCHAIN,null);
            fail();
        }
        catch (Exception e) {
            assertEquals(e.getMessage(), ERROR_MSG_SAULT_BYTES_SIZE_INCORRECT);
        }
        try {
            getCheckAvailableVolForNewKeyAPDU(MAX_KEY_SIZE_IN_KEYCHAIN, new byte[SAULT_LENGTH + 1]);
            fail();
        }
        catch (Exception e) {
            assertEquals(e.getMessage(), ERROR_MSG_SAULT_BYTES_SIZE_INCORRECT);
        }
        try {
            getCheckAvailableVolForNewKeyAPDU(MAX_KEY_SIZE_IN_KEYCHAIN, new byte[SAULT_LENGTH - 1]);
            fail();
        }
        catch (Exception e) {
            assertEquals(e.getMessage(), ERROR_MSG_SAULT_BYTES_SIZE_INCORRECT);
        }
        try {
            getCheckAvailableVolForNewKeyAPDU((short) -1, new byte[SAULT_LENGTH]);
            fail();
        }
        catch (Exception e) {
            assertEquals(e.getMessage(), ERROR_MSG_KEY_SIZE_INCORRECT);
        }
        try {
            getCheckAvailableVolForNewKeyAPDU((short) (MAX_KEY_SIZE_IN_KEYCHAIN + 1), new byte[SAULT_LENGTH]);
            fail();
        }
        catch (Exception e) {
            assertEquals(e.getMessage(), ERROR_MSG_KEY_SIZE_INCORRECT);
        }
    }

    private void checkAPDU(CAPDU capdu, byte ins, byte p1, byte p2, byte[] data, int le) {
        assertEquals(capdu.getCla(), WALLET_APPLET_CLA);
        assertEquals(capdu.getIns(), ins);
        assertEquals(capdu.getP1(), p1);
        assertEquals(capdu.getP2(), p2);
        assertArrayEquals(capdu.getData(), data);
        assertEquals(capdu.getLe(), le);
    }








}