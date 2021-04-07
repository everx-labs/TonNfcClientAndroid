package com.tonnfccard.smartcard;

import android.security.keystore.KeyProperties;
import android.security.keystore.KeyProtection;

import androidx.test.ext.junit.runners.AndroidJUnit4;

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
import org.junit.runner.RunWith;

import java.lang.reflect.Array;
import java.security.KeyStore;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

@RunWith(AndroidJUnit4.class)
public class TonWalletAppletApduCommandsTest {

    private final static ByteArrayUtil BYTE_ARRAY_HELPER = ByteArrayUtil.getInstance();
    private final Random random = new Random();

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

    /**
     VERIFY_PASSWORD

     CLA: 0xB0
     INS: 0x92
     P1: 0x00
     P2: 0x00
     LC: 0x90
     Data: 128 bytes of unencrypted activation password | 16 bytes of IV for AES128 CBC

     */

    @Test
    public void testGetVerifyPasswordAPDUIncorrectInputData() {
        Arrays.asList(null, new byte[PASSWORD_SIZE - 1], new byte[PASSWORD_SIZE + 1]).forEach(badPassWord -> {
            try {
                getVerifyPasswordAPDU(badPassWord, new byte[IV_SIZE]);
                fail();
            }
            catch (Exception e) {
                assertEquals(e.getMessage(), ERROR_MSG_ACTIVATION_PASSWORD_BYTES_SIZE_INCORRECT);
            }
        });
        Arrays.asList(null, new byte[IV_SIZE - 1], new byte[IV_SIZE + 1]).forEach(badIv -> {
            try {
                getVerifyPasswordAPDU(new byte[PASSWORD_SIZE], badIv);
                fail();
            }
            catch (Exception e) {
                assertEquals(e.getMessage(), ERROR_MSG_INITIAL_VECTOR_BYTES_SIZE_INCORRECT);
            }
        });
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

    /**
     VERIFY_PIN

     CLA: 0xB0
     INS: 0xA2
     P1: 0x00
     P2: 0x00
     LC: 0x44
     Data: 4 bytes of PIN | 32 bytes of sault | 32 bytes of mac
     */

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
        Arrays.asList(null, new byte[PIN_SIZE - 1], new byte[PIN_SIZE + 1]).forEach(badPin -> {
            try {
                getVerifyPinAPDU(badPin, new byte[SAULT_LENGTH]);
                fail();
            }
            catch (Exception e) {
                assertEquals(e.getMessage(), ERROR_MSG_PIN_BYTES_SIZE_INCORRECT);
            }
        });
        Arrays.asList(null, new byte[SAULT_LENGTH - 1], new byte[SAULT_LENGTH + 1]).forEach(badSault -> {
            try {
                getVerifyPinAPDU(new byte[PIN_SIZE], badSault);
                fail();
            }
            catch (Exception e) {
                assertEquals(e.getMessage(), ERROR_MSG_SAULT_BYTES_SIZE_INCORRECT);
            }
        });
    }

    /**
     SIGN_SHORT_MESSAGE_WITH_DEFAULT_PATH

     CLA: 0xB0
     INS: 0xA5
     P1: 0x00
     P2: 0x00
     LC: APDU data length
     Data: messageLength (2bytes)| message | sault (32 bytes) | mac (32 bytes)
     LE: 0x40
     */

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
        Arrays.asList(null, new byte[0], new byte[DATA_FOR_SIGNING_MAX_SIZE + 1]).forEach(data -> {
            try {
                getSignShortMessageWithDefaultPathAPDU(data,  new byte[SAULT_LENGTH]);
                fail();
            }
            catch (Exception e) {
                assertEquals(e.getMessage(), ERROR_MSG_DATA_BYTES_SIZE_INCORRECT);
            }
        });
        Arrays.asList(null, new byte[SAULT_LENGTH - 1], new byte[SAULT_LENGTH + 1]).forEach(sault -> {
            try {
                getSignShortMessageWithDefaultPathAPDU(new byte[DATA_FOR_SIGNING_MAX_SIZE], sault);
                fail();
            }
            catch (Exception e) {
                assertEquals(e.getMessage(), ERROR_MSG_SAULT_BYTES_SIZE_INCORRECT);
            }
        });
    }

    /**
     SIGN_SHORT_MESSAGE

     CLA: 0xB0
     INS: 0xA3
     P1: 0x00
     P2: 0x00
     LC: APDU data length
     Data: messageLength (2bytes)| message | indLength (1 byte, > 0, <= 10) | ind | sault (32 bytes) | mac (32 bytes)
     LE: 0x40

     */

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
        Arrays.asList(null, new byte[0], new byte[DATA_FOR_SIGNING_MAX_SIZE_FOR_CASE_WITH_PATH + 1]).forEach(data -> {
            try {
                getSignShortMessageAPDU(data, new byte[2], new byte[SAULT_LENGTH]);
                fail();
            }
            catch (Exception e) {
                assertEquals(e.getMessage(), ERROR_MSG_DATA_WITH_HD_PATH_BYTES_SIZE_INCORRECT);
            }
        });
        Arrays.asList(null, new byte[SAULT_LENGTH - 1], new byte[SAULT_LENGTH + 1]).forEach(sault -> {
            try {
                getSignShortMessageAPDU(new byte[DATA_FOR_SIGNING_MAX_SIZE_FOR_CASE_WITH_PATH], new byte[2], sault);
                fail();
            }
            catch (Exception e) {
                assertEquals(e.getMessage(), ERROR_MSG_SAULT_BYTES_SIZE_INCORRECT);
            }
        });
        Arrays.asList(null, new byte[0], new byte[MAX_HD_INDEX_SIZE + 1]).forEach(hdIndex -> {
            try {
                getSignShortMessageAPDU(new byte[DATA_FOR_SIGNING_MAX_SIZE_FOR_CASE_WITH_PATH], hdIndex, new byte[SAULT_LENGTH]);
                fail();
            }
            catch (Exception e) {
                assertEquals(e.getMessage(), ERROR_MSG_HD_INDEX_BYTES_SIZE_INCORRECT);
            }
        });
    }

    /**
     GET_PUBLIC_KEY

     CLA: 0xB0
     INS: 0xA0
     P1: 0x00
     P2: 0x00
     LC: Number of decimal places in ind
     Data: Ascii encoding of ind decimal places
     LE: 0x20
     */

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
        Arrays.asList(null, new byte[0], new byte[MAX_HD_INDEX_SIZE + 1]).forEach(hdIndex -> {
            try {
                getPublicKeyAPDU(hdIndex);
                fail();
            }
            catch (Exception e) {
                assertEquals(e.getMessage(), ERROR_MSG_HD_INDEX_BYTES_SIZE_INCORRECT);
            }
        });
    }

    /**
     * ADD_RECOVERY_DATA_PART
     *
     * CLA: 0xB0
     * INS: 0xD1
     * P1: 0x00 (START_OF_TRANSMISSION), 0x01 or 0x02 (END_OF_TRANSMISSION)
     * P2: 0x00
     * LC: If (P1 ≠ 0x02) Length of recovery data piece else 0x20
     * Data: If (P1 ≠ 0x02) recovery data piece else SHA256(recovery data)
     */

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
        Arrays.asList((byte) -1, (byte) 3).forEach(p1 -> {
            try {
                getAddRecoveryDataPartAPDU((byte) -1, null);
                fail();
            }
            catch (Exception e) {
                assertEquals(e.getMessage(), ERROR_MSG_APDU_P1_INCORRECT);
            }
        });
        Arrays.asList(null, new byte[0], new byte[DATA_RECOVERY_PORTION_MAX_SIZE + 1]).forEach(portion -> {
            try {
                getAddRecoveryDataPartAPDU(P1, portion);
                fail();
            }
            catch (Exception e) {
                assertEquals(e.getMessage(), ERROR_MSG_RECOVER_DATA_PORTION_SIZE_INCORRECT);
            }
        });
        Arrays.asList(null, new byte[HMAC_SHA_SIG_SIZE + 1], new byte[HMAC_SHA_SIG_SIZE - 1]).forEach(mac -> {
            try {
                getAddRecoveryDataPartAPDU((byte) 0x02, null);
                fail();
            }
            catch (Exception e) {
                assertEquals(e.getMessage(), ERROR_MSG_RECOVERY_DATA_MAC_BYTES_SIZE_INCORRECT);
            }
        });
    }

    /**
     * GET_RECOVERY_DATA_PART
     *
     * CLA: 0xB0
     * INS: 0xD2
     * P1: 0x00
     * P2: 0x00
     * LC: 0x02
     * Data: startPosition of recovery data piece in internal buffer
     * LE: length of recovery data piece in internal buffer
     */

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
        Arrays.asList(null, new byte[1], new byte[3]).forEach(startPosBytes -> {
            try {
                getGetRecoveryDataPartAPDU(startPosBytes, (byte) DATA_PORTION_MAX_SIZE);
                fail();
            }
            catch (Exception e) {
                assertEquals(e.getMessage(), ERROR_MSG_START_POSITION_BYTES_SIZE_INCORRECT);
            }
        });
    }

    /**
     RESET_KEYCHAIN

     CLA: 0xB0
     INS: 0xBC
     P1: 0x00
     P2: 0x00
     LC: 0x40
     Data: sault (32 bytes) | mac (32 bytes)
     */

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
        Arrays.asList(null, new byte[SAULT_LENGTH + 1], new byte[SAULT_LENGTH - 1]).forEach(sault -> {
            try {
                getResetKeyChainAPDU(sault);
                fail();
            }
            catch (Exception e) {
                assertEquals(e.getMessage(), ERROR_MSG_SAULT_BYTES_SIZE_INCORRECT);
            }
        });
    }

    /**
     GET_NUMBER_OF_KEYS

     CLA: 0xB0
     INS: 0xB8
     P1: 0x00
     P2: 0x00
     LC: 0x40
     Data: sault (32 bytes) | mac (32 bytes)
     LE: 0x02
     */

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
        Arrays.asList(null, new byte[SAULT_LENGTH + 1], new byte[SAULT_LENGTH - 1]).forEach(sault -> {
            try {
                getNumberOfKeysAPDU(sault);
                fail();
            }
            catch (Exception e) {
                assertEquals(e.getMessage(), ERROR_MSG_SAULT_BYTES_SIZE_INCORRECT);
            }
        });
    }

    /***
     GET_OCCUPIED_STORAGE_SIZE

     CLA: 0xB0
     INS: 0xBA
     P1: 0x00
     P2: 0x00
     LC: 0x40
     Data: sault (32 bytes) | mac (32 bytes)
     LE: 0x02
     */


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
        Arrays.asList(null, new byte[SAULT_LENGTH + 1], new byte[SAULT_LENGTH - 1]).forEach(sault -> {
            try {
                getGetOccupiedSizeAPDU(sault);
                fail();
            }
            catch (Exception e) {
                assertEquals(e.getMessage(), ERROR_MSG_SAULT_BYTES_SIZE_INCORRECT);
            }
        });
    }

    /***
     GET_FREE_STORAGE_SIZE

     CLA: 0xB0
     INS: 0xB9
     P1: 0x00
     P2: 0x00
     LC: 0x40
     Data: sault (32 bytes) | mac (32 bytes)
     LE: 0x02
     */

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
        Arrays.asList(null, new byte[SAULT_LENGTH + 1], new byte[SAULT_LENGTH - 1]).forEach(sault -> {
            try {
                getGetFreeSizeAPDU(sault);
                fail();
            }
            catch (Exception e) {
                assertEquals(e.getMessage(), ERROR_MSG_SAULT_BYTES_SIZE_INCORRECT);
            }
        });
    }

    /***
     CHECK_KEY_HMAC_CONSISTENCY

     CLA: 0xB0
     INS: 0xB0
     P1: 0x00
     P2: 0x00
     LC: 0x60
     Data: keyMac (32 bytes) | sault (32 bytes) | mac (32 bytes)
     */


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
        Arrays.asList(null, new byte[SAULT_LENGTH + 1], new byte[SAULT_LENGTH - 1]).forEach(sault -> {
            try {
                getCheckKeyHmacConsistencyAPDU(new byte[HMAC_SHA_SIG_SIZE], sault);
                fail();
            }
            catch (Exception e) {
                assertEquals(e.getMessage(), ERROR_MSG_SAULT_BYTES_SIZE_INCORRECT);
            }
        });
        Arrays.asList(null, new byte[HMAC_SHA_SIG_SIZE + 1], new byte[HMAC_SHA_SIG_SIZE - 1]).forEach(mac -> {
            try {
                getCheckKeyHmacConsistencyAPDU( mac, new byte[SAULT_LENGTH]);
                fail();
            }
            catch (Exception e) {
                assertEquals(e.getMessage(), ERROR_MSG_KEY_MAC_BYTES_SIZE_INCORRECT);
            }
        });
    }

    /***
     GET_KEY_INDEX_IN_STORAGE_AND_LEN

     CLA: 0xB0
     INS: 0xB1
     P1: 0x00
     P2: 0x00
     LC: 0x60
     Data: hmac of key (32 bytes) | sault (32 bytes) | mac (32 bytes)
     LE: 0x04
     */


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
        Arrays.asList(null, new byte[SAULT_LENGTH + 1], new byte[SAULT_LENGTH - 1]).forEach(sault -> {
            try {
                getGetIndexAndLenOfKeyInKeyChainAPDU(new byte[HMAC_SHA_SIG_SIZE], sault);
                fail();
            }
            catch (Exception e) {
                assertEquals(e.getMessage(), ERROR_MSG_SAULT_BYTES_SIZE_INCORRECT);
            }
        });
        Arrays.asList(null, new byte[HMAC_SHA_SIG_SIZE + 1], new byte[HMAC_SHA_SIG_SIZE - 1]).forEach(mac -> {
            try {
                getGetIndexAndLenOfKeyInKeyChainAPDU( mac, new byte[SAULT_LENGTH]);
                fail();
            }
            catch (Exception e) {
                assertEquals(e.getMessage(), ERROR_MSG_KEY_MAC_BYTES_SIZE_INCORRECT);
            }
        });
    }

    /***
     INITIATE_CHANGE_OF_KEY

     CLA: 0xB0
     INS: 0xB5
     P1: 0x00
     P2: 0x00
     LC: 0x42
     Data: index of key (2 bytes) | sault (32 bytes) | mac (32 bytes)
     */


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
        Arrays.asList(null, new byte[SAULT_LENGTH + 1], new byte[SAULT_LENGTH - 1]).forEach(sault -> {
            try {
                getInitiateChangeOfKeyAPDU(new byte[KEYCHAIN_KEY_INDEX_LEN], sault);
                fail();
            }
            catch (Exception e) {
                assertEquals(e.getMessage(), ERROR_MSG_SAULT_BYTES_SIZE_INCORRECT);
            }
        });
        Arrays.asList(null, new byte[KEYCHAIN_KEY_INDEX_LEN + 1], new byte[KEYCHAIN_KEY_INDEX_LEN - 1]).forEach(index -> {
            try {
                getInitiateChangeOfKeyAPDU( index, new byte[SAULT_LENGTH]);
                fail();
            }
            catch (Exception e) {
                assertEquals(e.getMessage(), ERROR_MSG_KEY_INDEX_BYTES_SIZE_INCORRECT);
            }
        });
    }

    /***
     INITIATE_DELETE_KEY

     CLA: 0xB0
     INS: 0xB7
     P1: 0x00
     P2: 0x00
     LC: 0x42
     Data: key  index (2 bytes) | sault (32 bytes) | mac (32 bytes)
     LE: 0x02
     */


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
        Arrays.asList(null, new byte[SAULT_LENGTH + 1], new byte[SAULT_LENGTH - 1]).forEach(sault -> {
            try {
                getInitiateDeleteOfKeyAPDU(new byte[KEYCHAIN_KEY_INDEX_LEN], sault);
                fail();
            }
            catch (Exception e) {
                assertEquals(e.getMessage(), ERROR_MSG_SAULT_BYTES_SIZE_INCORRECT);
            }
        });
        Arrays.asList(null, new byte[KEYCHAIN_KEY_INDEX_LEN + 1], new byte[KEYCHAIN_KEY_INDEX_LEN - 1]).forEach(index -> {
            try {
                getInitiateDeleteOfKeyAPDU( index, new byte[SAULT_LENGTH]);
                fail();
            }
            catch (Exception e) {
                assertEquals(e.getMessage(), ERROR_MSG_KEY_INDEX_BYTES_SIZE_INCORRECT);
            }
        });
    }

    /**
     * GET_DELETE_KEY_CHUNK_NUM_OF_PACKETS
     *
     * CLA: 0xB0
     * INS: 0xE1
     * P1: 0x00
     * P2: 0x00
     * LC: 0x40
     * Data: sault (32 bytes) | mac (32 bytes)
     * LE: 0x02
     */

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
        Arrays.asList(null, new byte[SAULT_LENGTH + 1], new byte[SAULT_LENGTH - 1]).forEach(sault -> {
            try {
                getDeleteKeyChunkNumOfPacketsAPDU(sault);
                fail();
            }
            catch (Exception e) {
                assertEquals(e.getMessage(), ERROR_MSG_SAULT_BYTES_SIZE_INCORRECT);
            }
        });
    }

    /**
     * GET_DELETE_KEY_RECORD_NUM_OF_PACKETS
     *
     * CLA: 0xB0
     * INS: 0xE2
     * P1: 0x00
     * P2: 0x00
     * LC: 0x40
     * Data: sault (32 bytes) | mac (32 bytes)
     * LE: 0x02
     */

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
        Arrays.asList(null, new byte[SAULT_LENGTH + 1], new byte[SAULT_LENGTH - 1]).forEach(sault -> {
            try {
                getDeleteKeyRecordNumOfPacketsAPDU(sault);
                fail();
            }
            catch (Exception e) {
                assertEquals(e.getMessage(), ERROR_MSG_SAULT_BYTES_SIZE_INCORRECT);
            }
        });
    }

    /***
     DELETE_KEY_RECORD

     CLA: 0xB0
     INS: 0xBF
     P1: 0x00
     P2: 0x00
     LC: 0x40
     Data: sault (32 bytes) | mac (32 bytes)
     LE: 0x01
     */

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
        Arrays.asList(null, new byte[SAULT_LENGTH + 1], new byte[SAULT_LENGTH - 1]).forEach(sault -> {
            try {
                getDeleteKeyRecordAPDU(sault);
                fail();
            }
            catch (Exception e) {
                assertEquals(e.getMessage(), ERROR_MSG_SAULT_BYTES_SIZE_INCORRECT);
            }
        });
    }

    /***
     DELETE_KEY_CHUNK

     CLA: 0xB0
     INS: 0xBE
     P1: 0x00
     P2: 0x00
     LC: 0x40
     Data: sault (32 bytes) | mac (32 bytes)
     LE: 0x01
     */

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
        Arrays.asList(null, new byte[SAULT_LENGTH + 1], new byte[SAULT_LENGTH - 1]).forEach(sault -> {
            try {
                getDeleteKeyChunkAPDU(sault);
                fail();
            }
            catch (Exception e) {
                assertEquals(e.getMessage(), ERROR_MSG_SAULT_BYTES_SIZE_INCORRECT);
            }
        });
    }

    /***
     GET_HMAC

     CLA: 0xB0
     INS: 0xBB
     P1: 0x00
     P2: 0x00
     LC: 0x42
     Data: index of key (2 bytes) | sault (32 bytes) | mac (32 bytes)
     LE: 0x22
     */


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
        Arrays.asList(null, new byte[SAULT_LENGTH + 1], new byte[SAULT_LENGTH - 1]).forEach(sault -> {
            try {
                getGetHmacAPDU(new byte[KEYCHAIN_KEY_INDEX_LEN], sault);
                fail();
            }
            catch (Exception e) {
                assertEquals(e.getMessage(), ERROR_MSG_SAULT_BYTES_SIZE_INCORRECT);
            }
        });
        Arrays.asList(null, new byte[KEYCHAIN_KEY_INDEX_LEN + 1], new byte[KEYCHAIN_KEY_INDEX_LEN - 1]).forEach(index -> {
            try {
                getGetHmacAPDU( index, new byte[SAULT_LENGTH]);
                fail();
            }
            catch (Exception e) {
                assertEquals(e.getMessage(), ERROR_MSG_KEY_INDEX_BYTES_SIZE_INCORRECT);
            }
        });
    }

    /***
     GET_KEY_CHUNK

     CLA: 0xB0
     INS: 0xB2
     P1: 0x00
     P2: 0x00
     LC: 0x44
     Data: key  index (2 bytes) | startPos (2 bytes) | sault (32 bytes) | mac (32 bytes)
     LE: Key chunk length
     */

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
        Arrays.asList(null, new byte[SAULT_LENGTH + 1], new byte[SAULT_LENGTH - 1]).forEach(sault -> {
            try {
                getGetKeyChunkAPDU(new byte[KEYCHAIN_KEY_INDEX_LEN], startPos, sault, (byte) DATA_PORTION_MAX_SIZE);
                fail();
            }
            catch (Exception e) {
                assertEquals(e.getMessage(), ERROR_MSG_SAULT_BYTES_SIZE_INCORRECT);
            }
        });
        Arrays.asList(null, new byte[KEYCHAIN_KEY_INDEX_LEN + 1], new byte[KEYCHAIN_KEY_INDEX_LEN - 1]).forEach(index -> {
            try {
                getGetKeyChunkAPDU( index, startPos, new byte[SAULT_LENGTH], (byte) DATA_PORTION_MAX_SIZE);
                fail();
            }
            catch (Exception e) {
                assertEquals(e.getMessage(), ERROR_MSG_KEY_INDEX_BYTES_SIZE_INCORRECT);
            }
        });
    }

    /***
     ADD_KEY_CHUNK

     CLA: 0xB0
     INS: 0xB4
     P1: 0x00 (START_OF_TRANSMISSION), 0x01 or 0x02 (END_OF_TRANSMISSION)
     P2: 0x00
     LC:
     if (P1 = 0x00 OR  0x01): 0x01 +  length of key chunk + 0x40
     if (P1 = 0x02): 0x60

     Data:
     if (P1 = 0x00 OR  0x01): length of key chunk (1 byte) | key chunk | sault (32 bytes) | mac (32 bytes)
     if (P1 = 0x02): hmac of key (32 bytes) | sault (32 bytes) | mac (32 bytes)

     LE: if (P1 = 0x02): 0x02

     */

    /***
     CHANGE_KEY_CHUNK

     CLA: 0xB0
     INS: 0xB6
     P1: 0x00 (START_OF_TRANSMISSION), 0x01 or 0x02 (END_OF_TRANSMISSION)
     P2: 0x00
     LC:
     if (P1 = 0x00 OR  0x01): 0x01 +  length of key chunk + 0x40
     if (P1 = 0x02): 0x60

     Data:
     if (P1 = 0x00 OR  0x01): length of key chunk (1 byte) | key chunk | sault (32 bytes) | mac (32 bytes)
     if (P1 = 0x02): hmac of key (32 bytes) | sault (32 bytes) | mac (32 bytes)

     LE: if (P1 = 0x02): 0x02

     */

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
            for( byte p1 : Arrays.asList((byte) -1, (byte) 3)){
                try {
                    getSendKeyChunkAPDU(instructions[i], p1, keyChunk, sault);
                    fail();
                }
                catch (Exception e) {
                    assertEquals(e.getMessage(), ERROR_MSG_APDU_P1_INCORRECT);
                }
            };
            for( byte[] data : Arrays.asList(null, new byte[0], new byte[DATA_PORTION_MAX_SIZE + 1])) {
                try {
                    getSendKeyChunkAPDU(instructions[i], P1, data, sault);
                    fail();
                }
                catch (Exception e) {
                    assertEquals(e.getMessage(), ERROR_MSG_KEY_CHUNK_BYTES_SIZE_INCORRECT);
                }
            }
            for( byte[] mac : Arrays.asList(null, new byte[HMAC_SHA_SIG_SIZE + 1], new byte[HMAC_SHA_SIG_SIZE - 1])) {
                try {
                    getSendKeyChunkAPDU(instructions[i], (byte) 0x02, mac, sault);
                    fail();
                }
                catch (Exception e) {
                    assertEquals(e.getMessage(), ERROR_MSG_KEY_MAC_BYTES_SIZE_INCORRECT);
                }
            }
            for( byte[] badSault : Arrays.asList(null, new byte[SAULT_LENGTH + 1], new byte[SAULT_LENGTH - 1])) {
                try {
                    getSendKeyChunkAPDU(instructions[i], P1, new byte[DATA_PORTION_MAX_SIZE], badSault);
                    fail();
                }
                catch (Exception e) {
                    assertEquals(e.getMessage(), ERROR_MSG_SAULT_BYTES_SIZE_INCORRECT);
                }
            }
        }
    }

    /***
     CHECK_AVAILABLE_VOL_FOR_NEW_KEY

     CLA: 0xB0
     INS: 0xB3
     P1: 0x00
     P2: 0x00
     LC: 0x42
     Data: length of new key (2 bytes) | sault (32 bytes) | mac (32 bytes)
     */

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
        Arrays.asList(null, new byte[SAULT_LENGTH + 1], new byte[SAULT_LENGTH - 1]).forEach(sault -> {
            try {
                getCheckAvailableVolForNewKeyAPDU(MAX_KEY_SIZE_IN_KEYCHAIN, sault);
                fail();
            }
            catch (Exception e) {
                assertEquals(e.getMessage(), ERROR_MSG_SAULT_BYTES_SIZE_INCORRECT);
            }
        });
        Arrays.asList((short) -1, (short) (MAX_KEY_SIZE_IN_KEYCHAIN + 1)).forEach(size -> {
            try {
                getCheckAvailableVolForNewKeyAPDU(size, new byte[SAULT_LENGTH]);
                fail();
            }
            catch (Exception e) {
                assertEquals(e.getMessage(), ERROR_MSG_KEY_SIZE_INCORRECT);
            }
        });
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