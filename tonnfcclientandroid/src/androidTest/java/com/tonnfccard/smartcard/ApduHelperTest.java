package com.tonnfccard.smartcard;

import android.security.keystore.KeyProperties;
import android.security.keystore.KeyProtection;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.tonnfccard.helpers.HmacHelper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.security.KeyStore;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import static com.tonnfccard.TonWalletConstants.DATA_RECOVERY_PORTION_MAX_SIZE;
import static com.tonnfccard.TonWalletConstants.DEFAULT_PIN;
import static com.tonnfccard.TonWalletConstants.HMAC_SHA_SIG_SIZE;
import static com.tonnfccard.TonWalletConstants.IV_SIZE;
import static com.tonnfccard.TonWalletConstants.PASSWORD_SIZE;
import static com.tonnfccard.TonWalletConstants.SAULT_LENGTH;
import static com.tonnfccard.helpers.HmacHelper.ANDROID_KEYSTORE;
import static com.tonnfccard.helpers.HmacHelperTest.HMAC_HELPER;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_CAPDU_IS_NULL;
import static com.tonnfccard.smartcard.CoinManagerApduCommands.CHANGE_PIN_APDU_NAME;
import static com.tonnfccard.smartcard.CoinManagerApduCommands.GENERATE_SEED_APDU_NAME;
import static com.tonnfccard.smartcard.CoinManagerApduCommands.GET_APPLET_LIST_APDU;
import static com.tonnfccard.smartcard.CoinManagerApduCommands.GET_APPLET_LIST_APDU_NAME;
import static com.tonnfccard.smartcard.CoinManagerApduCommands.GET_AVAILABLE_MEMORY_APDU;
import static com.tonnfccard.smartcard.CoinManagerApduCommands.GET_AVAILABLE_MEMORY_APDU_NAME;
import static com.tonnfccard.smartcard.CoinManagerApduCommands.GET_CSN_APDU;
import static com.tonnfccard.smartcard.CoinManagerApduCommands.GET_CSN_APDU_NAME;
import static com.tonnfccard.smartcard.CoinManagerApduCommands.GET_DEVICE_LABEL_APDU;
import static com.tonnfccard.smartcard.CoinManagerApduCommands.GET_DEVICE_LABEL_APDU_NAME;
import static com.tonnfccard.smartcard.CoinManagerApduCommands.GET_PIN_RTL_APDU;
import static com.tonnfccard.smartcard.CoinManagerApduCommands.GET_PIN_RTL_APDU_NAME;
import static com.tonnfccard.smartcard.CoinManagerApduCommands.GET_PIN_TLT_APDU;
import static com.tonnfccard.smartcard.CoinManagerApduCommands.GET_PIN_TLT_APDU_NAME;
import static com.tonnfccard.smartcard.CoinManagerApduCommands.GET_ROOT_KEY_STATUS_APDU;
import static com.tonnfccard.smartcard.CoinManagerApduCommands.GET_ROOT_KEY_STATUS_APDU_NAME;
import static com.tonnfccard.smartcard.CoinManagerApduCommands.GET_SE_VERSION_APDU;
import static com.tonnfccard.smartcard.CoinManagerApduCommands.GET_SE_VERSION_APDU_NAME;
import static com.tonnfccard.smartcard.CoinManagerApduCommands.LABEL_LENGTH;
import static com.tonnfccard.smartcard.CoinManagerApduCommands.RESET_WALLET_APDU;
import static com.tonnfccard.smartcard.CoinManagerApduCommands.RESET_WALLET_APDU_NAME;
import static com.tonnfccard.smartcard.CoinManagerApduCommands.SELECT_COIN_MANAGER_APDU;
import static com.tonnfccard.smartcard.CoinManagerApduCommands.SET_DEVICE_LABEL_APDU_NAME;
import static com.tonnfccard.smartcard.CoinManagerApduCommands.getChangePinAPDU;
import static com.tonnfccard.smartcard.CoinManagerApduCommands.getGenerateSeedAPDU;
import static com.tonnfccard.smartcard.CoinManagerApduCommands.getSetDeviceLabelAPDU;
import static com.tonnfccard.smartcard.CommonConstants.SELECT_COIN_MANAGER_APDU_NAME;
import static com.tonnfccard.smartcard.CommonConstants.SELECT_TON_WALLET_APPLET_APDU_NAME;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.ADD_RECOVERY_DATA_PART_APDU_NAME;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.GET_APP_INFO_APDU;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.GET_APP_INFO_APDU_NAME;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.GET_GET_RECOVERY_DATA_PART_APDU_NAME;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.GET_HASH_OF_ENCRYPTED_COMMON_SECRET_APDU;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.GET_HASH_OF_ENCRYPTED_COMMON_SECRET_APDU_NAME;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.GET_HASH_OF_ENCRYPTED_PASSWORD_APDU;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.GET_HASH_OF_ENCRYPTED_PASSWORD_APDU_NAME;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.GET_PUBLIC_KEY_APDU_NAME;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.GET_PUBLIC_KEY_WITH_DEFAULT_HD_PATH_APDU_NAME;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.GET_PUB_KEY_WITH_DEFAULT_PATH_APDU;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.GET_RECOVERY_DATA_HASH_APDU;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.GET_RECOVERY_DATA_HASH_APDU_NAME;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.GET_RECOVERY_DATA_LEN_APDU;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.GET_RECOVERY_DATA_LEN_APDU_NAME;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.GET_SAULT_APDU;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.GET_SAULT_APDU_NAME;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.GET_SERIAL_NUMBER_APDU;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.GET_SERIAL_NUMBER_APDU_NAME;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.IS_RECOVERY_DATA_SET_APDU;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.IS_RECOVERY_DATA_SET_APDU_NAME;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.RESET_RECOVERY_DATA_APDU;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.RESET_RECOVERY_DATA_APDU_NAME;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.SELECT_TON_WALLET_APPLET_APDU;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.SIGN_SHORT_MESSAGE_APDU_NAME;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.VERIFY_PASSWORD_APDU_NAME;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.VERIFY_PIN_APDU_NAME;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.*;
import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class ApduHelperTest {

    private static final ApduHelper APDU_HELPER = ApduHelper.getInstance();

    @Before
    public void prepareKeystore(){
       try{
           byte[] key = new byte[32];
           final SecretKey hmacSha256Key = new SecretKeySpec(key, 0, key.length, KeyProperties.KEY_ALGORITHM_HMAC_SHA256);
           final KeyStore keyStore = KeyStore.getInstance(ANDROID_KEYSTORE);
           String keyAlias = HmacHelper.HMAC_KEY_ALIAS + "77788899911";
           keyStore.load(null);
           if (keyStore.containsAlias(keyAlias)) keyStore.deleteEntry(keyAlias);
           keyStore.setEntry(keyAlias,
                   new KeyStore.SecretKeyEntry(hmacSha256Key),
                   new KeyProtection.Builder(KeyProperties.PURPOSE_SIGN)
                           .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                           .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                           .build());
           HMAC_HELPER.setCurrentSerialNumber("77788899911");
       }
       catch (Exception e) {
           e.printStackTrace();
       }
    }

    @Test
    public void getApduCommandNameTest() {
        try {
            String s = APDU_HELPER.getApduCommandName(null);
            fail();
        }
        catch (IllegalArgumentException e) {
            assertEquals(e.getMessage(), ERROR_MSG_CAPDU_IS_NULL);
        }
        catch (Exception e) {
            fail();
        }

        assertEquals(APDU_HELPER.getApduCommandName(SELECT_COIN_MANAGER_APDU), SELECT_COIN_MANAGER_APDU_NAME);
        assertEquals(APDU_HELPER.getApduCommandName(SELECT_TON_WALLET_APPLET_APDU), SELECT_TON_WALLET_APPLET_APDU_NAME);
        assertEquals(APDU_HELPER.getApduCommandName(GET_ROOT_KEY_STATUS_APDU), GET_ROOT_KEY_STATUS_APDU_NAME);
        assertEquals(APDU_HELPER.getApduCommandName(GET_PIN_RTL_APDU), GET_PIN_RTL_APDU_NAME);
        assertEquals(APDU_HELPER.getApduCommandName(GET_PIN_TLT_APDU), GET_PIN_TLT_APDU_NAME);
        assertEquals(APDU_HELPER.getApduCommandName(RESET_WALLET_APDU), RESET_WALLET_APDU_NAME);
        assertEquals(APDU_HELPER.getApduCommandName(GET_AVAILABLE_MEMORY_APDU), GET_AVAILABLE_MEMORY_APDU_NAME);
        assertEquals(APDU_HELPER.getApduCommandName(GET_APPLET_LIST_APDU), GET_APPLET_LIST_APDU_NAME);
        assertEquals(APDU_HELPER.getApduCommandName(GET_SE_VERSION_APDU), GET_SE_VERSION_APDU_NAME);
        assertEquals(APDU_HELPER.getApduCommandName(GET_CSN_APDU), GET_CSN_APDU_NAME);
        assertEquals(APDU_HELPER.getApduCommandName(GET_DEVICE_LABEL_APDU), GET_DEVICE_LABEL_APDU_NAME);
        assertEquals(APDU_HELPER.getApduCommandName(getChangePinAPDU(DEFAULT_PIN, DEFAULT_PIN)), CHANGE_PIN_APDU_NAME);
        assertEquals(APDU_HELPER.getApduCommandName(getGenerateSeedAPDU(DEFAULT_PIN)), GENERATE_SEED_APDU_NAME);
        assertEquals(APDU_HELPER.getApduCommandName(getSetDeviceLabelAPDU(new byte[LABEL_LENGTH])), SET_DEVICE_LABEL_APDU_NAME);

        assertEquals(APDU_HELPER.getApduCommandName(GET_APP_INFO_APDU), GET_APP_INFO_APDU_NAME);
        assertEquals(APDU_HELPER.getApduCommandName(GET_HASH_OF_ENCRYPTED_PASSWORD_APDU),  GET_HASH_OF_ENCRYPTED_PASSWORD_APDU_NAME);
        assertEquals(APDU_HELPER.getApduCommandName(GET_HASH_OF_ENCRYPTED_COMMON_SECRET_APDU), GET_HASH_OF_ENCRYPTED_COMMON_SECRET_APDU_NAME);
        assertEquals(APDU_HELPER.getApduCommandName(GET_PUB_KEY_WITH_DEFAULT_PATH_APDU), GET_PUBLIC_KEY_WITH_DEFAULT_HD_PATH_APDU_NAME);
        assertEquals(APDU_HELPER.getApduCommandName(GET_SAULT_APDU), GET_SAULT_APDU_NAME);
        assertEquals(APDU_HELPER.getApduCommandName(GET_RECOVERY_DATA_HASH_APDU), GET_RECOVERY_DATA_HASH_APDU_NAME);
        assertEquals(APDU_HELPER.getApduCommandName(GET_RECOVERY_DATA_LEN_APDU), GET_RECOVERY_DATA_LEN_APDU_NAME);
        assertEquals(APDU_HELPER.getApduCommandName(IS_RECOVERY_DATA_SET_APDU), IS_RECOVERY_DATA_SET_APDU_NAME);
        assertEquals(APDU_HELPER.getApduCommandName(RESET_RECOVERY_DATA_APDU), RESET_RECOVERY_DATA_APDU_NAME);
        assertEquals(APDU_HELPER.getApduCommandName(GET_SERIAL_NUMBER_APDU), GET_SERIAL_NUMBER_APDU_NAME);
        assertEquals(APDU_HELPER.getApduCommandName(getVerifyPasswordAPDU(new byte[PASSWORD_SIZE], new byte[IV_SIZE])), VERIFY_PASSWORD_APDU_NAME);

        byte[] mac = new byte[HMAC_SHA_SIG_SIZE];
        byte[] sault = new byte[SAULT_LENGTH];
        byte[] data = new byte[1];
        byte[] ind = new byte[1];
        byte[] pos = new byte[2];
        byte[] keyInd = new byte[2];
        try {
            assertEquals(APDU_HELPER.getApduCommandName(getVerifyPinAPDU(DEFAULT_PIN, sault)), VERIFY_PIN_APDU_NAME);
            assertEquals(APDU_HELPER.getApduCommandName(getSignShortMessageAPDU(data, ind, sault)), SIGN_SHORT_MESSAGE_APDU_NAME);
            assertEquals(APDU_HELPER.getApduCommandName(getPublicKeyAPDU(ind)), GET_PUBLIC_KEY_APDU_NAME);
            assertEquals(APDU_HELPER.getApduCommandName(getAddRecoveryDataPartAPDU((byte) 0, data)), ADD_RECOVERY_DATA_PART_APDU_NAME);
            assertEquals(APDU_HELPER.getApduCommandName(getGetRecoveryDataPartAPDU(pos, (byte) DATA_RECOVERY_PORTION_MAX_SIZE)), GET_GET_RECOVERY_DATA_PART_APDU_NAME);
            assertEquals(APDU_HELPER.getApduCommandName(getResetKeyChainAPDU(sault)), RESET_KEYCHAIN_APDU_NAME);
            assertEquals(APDU_HELPER.getApduCommandName(getNumberOfKeysAPDU(sault)), GET_NUMBER_OF_KEYS_APDU_NAME);
            assertEquals(APDU_HELPER.getApduCommandName(getGetOccupiedSizeAPDU(sault)), GET_OCCUPIED_STORAGE_APDU_NAME);
            assertEquals(APDU_HELPER.getApduCommandName(getGetFreeSizeAPDU(sault)), GET_FREE_STORAGE_APDU_NAME);
            assertEquals(APDU_HELPER.getApduCommandName(getCheckAvailableVolForNewKeyAPDU((short) 1, sault)), CHECK_AVAILABLE_VOL_FOR_NEW_KEY_APDU_NAME);
            assertEquals(APDU_HELPER.getApduCommandName(getCheckKeyHmacConsistencyAPDU(mac, sault)), CHECK_KEY_HMAC_CONSISTENCY_APDU_NAME);
            assertEquals(APDU_HELPER.getApduCommandName(getInitiateChangeOfKeyAPDU(keyInd, sault)), INITIATE_CHANGE_OF_KEY_APDU_NAME);
            assertEquals(APDU_HELPER.getApduCommandName(getGetIndexAndLenOfKeyInKeyChainAPDU(mac, sault)), GET_KEY_INDEX_IN_STORAGE_AND_LEN_APDU_NAME);
            assertEquals(APDU_HELPER.getApduCommandName(getInitiateDeleteOfKeyAPDU(keyInd, sault)), INITIATE_DELETE_KEY_APDU_NAME);
            assertEquals(APDU_HELPER.getApduCommandName(getDeleteKeyChunkNumOfPacketsAPDU(sault)), GET_DELETE_KEY_CHUNK_NUM_OF_PACKETS_APDU_NAME);
            assertEquals(APDU_HELPER.getApduCommandName(getDeleteKeyRecordNumOfPacketsAPDU(sault)), GET_DELETE_KEY_RECORD_NUM_OF_PACKETS_APDU_NAME);
            assertEquals(APDU_HELPER.getApduCommandName(getDeleteKeyChunkAPDU(sault)), DELETE_KEY_CHUNK_APDU_NAME);
            assertEquals(APDU_HELPER.getApduCommandName(getDeleteKeyRecordAPDU(sault)), DELETE_KEY_RECORD_APDU_NAME);
            assertEquals(APDU_HELPER.getApduCommandName(getGetHmacAPDU(keyInd, sault)), GET_HMAC_APDU_NAME);
            assertEquals(APDU_HELPER.getApduCommandName(getChangeKeyChunkAPDU((byte) 2, mac, sault)), CHANGE_KEY_CHUNK_APDU_NAME);
            assertEquals(APDU_HELPER.getApduCommandName(getAddKeyChunkAPDU((byte) 2, mac, sault)), ADD_KEY_CHUNK_APDU_NAME);
            assertEquals(APDU_HELPER.getApduCommandName(getGetKeyChunkAPDU(keyInd, (short) 0, sault, (byte) DATA_RECOVERY_PORTION_MAX_SIZE)), GET_KEY_CHUNK_APDU_NAME);
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            fail();
        }
    }

}