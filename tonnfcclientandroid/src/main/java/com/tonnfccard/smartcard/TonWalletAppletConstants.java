package com.tonnfccard.smartcard;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.tonnfccard.smartcard.apdu.TonWalletAppletApduCommands.INS_ADD_KEY_CHUNK;
import static com.tonnfccard.smartcard.apdu.TonWalletAppletApduCommands.INS_ADD_RECOVERY_DATA_PART;
import static com.tonnfccard.smartcard.apdu.TonWalletAppletApduCommands.INS_CHANGE_KEY_CHUNK;
import static com.tonnfccard.smartcard.apdu.TonWalletAppletApduCommands.INS_CHECK_AVAILABLE_VOL_FOR_NEW_KEY;
import static com.tonnfccard.smartcard.apdu.TonWalletAppletApduCommands.INS_CHECK_KEY_HMAC_CONSISTENCY;
import static com.tonnfccard.smartcard.apdu.TonWalletAppletApduCommands.INS_DELETE_KEY_CHUNK;
import static com.tonnfccard.smartcard.apdu.TonWalletAppletApduCommands.INS_DELETE_KEY_RECORD;
import static com.tonnfccard.smartcard.apdu.TonWalletAppletApduCommands.INS_FINISH_PERS;
import static com.tonnfccard.smartcard.apdu.TonWalletAppletApduCommands.INS_GET_APP_INFO;
import static com.tonnfccard.smartcard.apdu.TonWalletAppletApduCommands.INS_GET_DELETE_KEY_CHUNK_NUM_OF_PACKETS;
import static com.tonnfccard.smartcard.apdu.TonWalletAppletApduCommands.INS_GET_DELETE_KEY_RECORD_NUM_OF_PACKETS;
import static com.tonnfccard.smartcard.apdu.TonWalletAppletApduCommands.INS_GET_FREE_STORAGE_SIZE;
import static com.tonnfccard.smartcard.apdu.TonWalletAppletApduCommands.INS_GET_HASH_OF_ENCRYPTED_COMMON_SECRET;
import static com.tonnfccard.smartcard.apdu.TonWalletAppletApduCommands.INS_GET_HASH_OF_ENCRYPTED_PASSWORD;
import static com.tonnfccard.smartcard.apdu.TonWalletAppletApduCommands.INS_GET_HMAC;
import static com.tonnfccard.smartcard.apdu.TonWalletAppletApduCommands.INS_GET_KEY_CHUNK;
import static com.tonnfccard.smartcard.apdu.TonWalletAppletApduCommands.INS_GET_KEY_INDEX_IN_STORAGE_AND_LEN;
import static com.tonnfccard.smartcard.apdu.TonWalletAppletApduCommands.INS_GET_NUMBER_OF_KEYS;
import static com.tonnfccard.smartcard.apdu.TonWalletAppletApduCommands.INS_GET_OCCUPIED_STORAGE_SIZE;
import static com.tonnfccard.smartcard.apdu.TonWalletAppletApduCommands.INS_GET_PUBLIC_KEY;
import static com.tonnfccard.smartcard.apdu.TonWalletAppletApduCommands.INS_GET_PUBLIC_KEY_WITH_DEFAULT_HD_PATH;
import static com.tonnfccard.smartcard.apdu.TonWalletAppletApduCommands.INS_GET_RECOVERY_DATA_HASH;
import static com.tonnfccard.smartcard.apdu.TonWalletAppletApduCommands.INS_GET_RECOVERY_DATA_LEN;
import static com.tonnfccard.smartcard.apdu.TonWalletAppletApduCommands.INS_GET_RECOVERY_DATA_PART;
import static com.tonnfccard.smartcard.apdu.TonWalletAppletApduCommands.INS_GET_SAULT;
import static com.tonnfccard.smartcard.apdu.TonWalletAppletApduCommands.INS_GET_SERIAL_NUMBER;
import static com.tonnfccard.smartcard.apdu.TonWalletAppletApduCommands.INS_INITIATE_CHANGE_OF_KEY;
import static com.tonnfccard.smartcard.apdu.TonWalletAppletApduCommands.INS_INITIATE_DELETE_KEY;
import static com.tonnfccard.smartcard.apdu.TonWalletAppletApduCommands.INS_IS_RECOVERY_DATA_SET;
import static com.tonnfccard.smartcard.apdu.TonWalletAppletApduCommands.INS_RESET_KEYCHAIN;
import static com.tonnfccard.smartcard.apdu.TonWalletAppletApduCommands.INS_RESET_RECOVERY_DATA;
import static com.tonnfccard.smartcard.apdu.TonWalletAppletApduCommands.INS_SET_ENCRYPTED_COMMON_SECRET;
import static com.tonnfccard.smartcard.apdu.TonWalletAppletApduCommands.INS_SET_ENCRYPTED_PASSWORD_FOR_CARD_AUTHENTICATION;
import static com.tonnfccard.smartcard.apdu.TonWalletAppletApduCommands.INS_SET_SERIAL_NUMBER;
import static com.tonnfccard.smartcard.apdu.TonWalletAppletApduCommands.INS_SIGN_SHORT_MESSAGE;
import static com.tonnfccard.smartcard.apdu.TonWalletAppletApduCommands.INS_SIGN_SHORT_MESSAGE_WITH_DEFAULT_PATH;
import static com.tonnfccard.smartcard.apdu.TonWalletAppletApduCommands.INS_VERIFY_PASSWORD;
import static com.tonnfccard.smartcard.apdu.TonWalletAppletApduCommands.INS_VERIFY_PIN;

public class TonWalletAppletConstants {

    /****************************************
     * States *
     ****************************************
     */

    public static final String INSTALLED_STATE_MSG =  "TonWalletApplet is invalid (is not personalized)";
    public static final String PERSONALIZED_STATE_MSG = "TonWalletApplet is personalized.";
    public static final String WAITE_AUTHORIZATION_MSG =  "TonWalletApplet waits two-factor authorization.";
    public static final String DELETE_KEY_FROM_KEYCHAIN_MSG = "TonWalletApplet is personalized and waits finishing key deleting from keychain.";
    public static final String BLOCKED_MSG = "TonWalletApplet is blocked.";

    public static final List<TonWalletAppletStates> ALL_APPLET_STATES = Arrays.asList(TonWalletAppletStates.values());
    public static final List<TonWalletAppletStates> INSTALLED_STATE = Arrays.asList(TonWalletAppletStates.INSTALLED);
    public static final List<TonWalletAppletStates> PERSONALIZED_STATE = Arrays.asList(TonWalletAppletStates.PERSONALIZED);
    public static final List<TonWalletAppletStates> WAITE_AUTHORIZATION_STATE = Arrays.asList(TonWalletAppletStates.WAITE_AUTHORIZATION_MODE);
    public static final List<TonWalletAppletStates> PERSONALIZED_AND_DELETE_STATE = Arrays.asList(TonWalletAppletStates.PERSONALIZED, TonWalletAppletStates.DELETE_KEY_FROM_KEYCHAIN_MODE);


    //private static Map<Byte, String> tonWalletAppletStates = new HashMap<>();
    private static Map<Byte, List<TonWalletAppletStates>> tonWalletAppletCommandStateMapping = new HashMap<>();

    public static final short DATA_PORTION_MAX_SIZE = 128;

    public static final byte PUBLIC_KEY_LEN = 32;
    public static final byte TRANSACTION_HASH_SIZE = 32;
    public static final byte SIG_LEN = 0x40;

    public static final byte[] DEFAULT_PIN = new byte[]{0x35, 0x35, 0x35, 0x35};
    public static final byte PIN_SIZE = (byte) 4;
    public final static byte MAX_PIN_TRIES = (byte) 10;

    public static final short DATA_FOR_SIGNING_MAX_SIZE = (short) 189;
    public final static short APDU_DATA_MAX_SIZE = (short) 255;

    public static final short DATA_FOR_SIGNING_MAX_SIZE_FOR_CASE_WITH_PATH = (short) 178;

    public final static byte SHA_HASH_SIZE = (short) 32;

    public static short PASSWORD_SIZE = 128;

    public static short IV_SIZE = 16;

    public static final byte SAULT_LENGTH = (short) 32;

    public final static short HMAC_SHA_SIG_SIZE = (short) 32;

    public static final short MAX_NUMBER_OF_KEYS_IN_KEYCHAIN = 1023;

    public static final short MAX_KEY_SIZE_IN_KEYCHAIN = 8192;

    public static final short KEY_CHAIN_SIZE = 32767;

    public final static short MAX_IND_SIZE = (short) 10;

    public static final short COMMON_SECRET_SIZE = 32;

    public static final short MAX_HMAC_FAIL_TRIES = 20;

    public static final short KEYCHAIN_KEY_INDEX_LEN = 2;

    public static final short DATA_RECOVERY_PORTION_MAX_SIZE = 250;

    public static final short RECOVERY_DATA_MAX_SIZE = 2048;

    public final static short SERIAL_NUMBER_SIZE = (short) 24;

    public final static String DEFAULT_SERIAL_NUMBER = "504394802433901126813236";

    public final static String EMPTY_SERIAL_NUMBER = "empty";

    static {
        addAppletCommandStatePair(INS_FINISH_PERS, INSTALLED_STATE);
        addAppletCommandStatePair(INS_SET_ENCRYPTED_PASSWORD_FOR_CARD_AUTHENTICATION, INSTALLED_STATE);
        addAppletCommandStatePair(INS_SET_ENCRYPTED_COMMON_SECRET, INSTALLED_STATE);
        addAppletCommandStatePair(INS_SET_SERIAL_NUMBER, INSTALLED_STATE);

        addAppletCommandStatePair(INS_VERIFY_PASSWORD, WAITE_AUTHORIZATION_STATE);
        addAppletCommandStatePair(INS_GET_HASH_OF_ENCRYPTED_COMMON_SECRET, WAITE_AUTHORIZATION_STATE);
        addAppletCommandStatePair(INS_GET_HASH_OF_ENCRYPTED_PASSWORD, WAITE_AUTHORIZATION_STATE);

        addAppletCommandStatePair(INS_VERIFY_PIN, PERSONALIZED_AND_DELETE_STATE);
        addAppletCommandStatePair(INS_GET_PUBLIC_KEY, PERSONALIZED_AND_DELETE_STATE);
        addAppletCommandStatePair(INS_GET_PUBLIC_KEY_WITH_DEFAULT_HD_PATH, PERSONALIZED_AND_DELETE_STATE);
        addAppletCommandStatePair(INS_SIGN_SHORT_MESSAGE, PERSONALIZED_AND_DELETE_STATE);
        addAppletCommandStatePair(INS_SIGN_SHORT_MESSAGE_WITH_DEFAULT_PATH, PERSONALIZED_AND_DELETE_STATE);
        addAppletCommandStatePair(INS_GET_KEY_INDEX_IN_STORAGE_AND_LEN, PERSONALIZED_AND_DELETE_STATE);
        addAppletCommandStatePair(INS_GET_KEY_CHUNK, PERSONALIZED_AND_DELETE_STATE);
        addAppletCommandStatePair(INS_DELETE_KEY_CHUNK, PERSONALIZED_AND_DELETE_STATE);
        addAppletCommandStatePair(INS_DELETE_KEY_RECORD, PERSONALIZED_AND_DELETE_STATE);
        addAppletCommandStatePair(INS_GET_DELETE_KEY_CHUNK_NUM_OF_PACKETS, PERSONALIZED_AND_DELETE_STATE);
        addAppletCommandStatePair(INS_GET_DELETE_KEY_RECORD_NUM_OF_PACKETS, PERSONALIZED_AND_DELETE_STATE);
        addAppletCommandStatePair(INS_INITIATE_DELETE_KEY, PERSONALIZED_AND_DELETE_STATE);
        addAppletCommandStatePair(INS_GET_NUMBER_OF_KEYS, PERSONALIZED_AND_DELETE_STATE);
        addAppletCommandStatePair(INS_GET_FREE_STORAGE_SIZE, PERSONALIZED_AND_DELETE_STATE);
        addAppletCommandStatePair(INS_GET_OCCUPIED_STORAGE_SIZE, PERSONALIZED_AND_DELETE_STATE);
        addAppletCommandStatePair(INS_GET_HMAC, PERSONALIZED_AND_DELETE_STATE);
        addAppletCommandStatePair(INS_RESET_KEYCHAIN, PERSONALIZED_AND_DELETE_STATE);
        addAppletCommandStatePair(INS_GET_SAULT, PERSONALIZED_AND_DELETE_STATE);
        addAppletCommandStatePair(INS_CHECK_KEY_HMAC_CONSISTENCY, PERSONALIZED_AND_DELETE_STATE);

        addAppletCommandStatePair(INS_CHECK_AVAILABLE_VOL_FOR_NEW_KEY, PERSONALIZED_STATE);
        addAppletCommandStatePair(INS_ADD_KEY_CHUNK, PERSONALIZED_STATE);
        addAppletCommandStatePair(INS_INITIATE_CHANGE_OF_KEY, PERSONALIZED_STATE);
        addAppletCommandStatePair(INS_CHANGE_KEY_CHUNK, PERSONALIZED_STATE);
        addAppletCommandStatePair(INS_GET_APP_INFO, ALL_APPLET_STATES);
        addAppletCommandStatePair(INS_GET_SERIAL_NUMBER, ALL_APPLET_STATES);

        addAppletCommandStatePair(INS_RESET_RECOVERY_DATA, PERSONALIZED_AND_DELETE_STATE);
        addAppletCommandStatePair(INS_ADD_RECOVERY_DATA_PART, PERSONALIZED_AND_DELETE_STATE);
        addAppletCommandStatePair(INS_GET_RECOVERY_DATA_PART, PERSONALIZED_AND_DELETE_STATE);
        addAppletCommandStatePair(INS_GET_RECOVERY_DATA_HASH, PERSONALIZED_AND_DELETE_STATE);
        addAppletCommandStatePair(INS_GET_RECOVERY_DATA_LEN, PERSONALIZED_AND_DELETE_STATE);
        addAppletCommandStatePair(INS_IS_RECOVERY_DATA_SET, PERSONALIZED_AND_DELETE_STATE);
    }

    private static void addAppletCommandStatePair(Byte ins, List<TonWalletAppletStates> states) {
        tonWalletAppletCommandStateMapping.put(ins, states);
    }

    public static List<TonWalletAppletStates> getStateByIns(Byte ins) {
        return  tonWalletAppletCommandStateMapping.get(ins);
    }



}
