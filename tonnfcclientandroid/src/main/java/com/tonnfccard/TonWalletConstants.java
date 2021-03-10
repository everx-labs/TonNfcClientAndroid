package com.tonnfccard;

public final class TonWalletConstants {
    public static final String SUCCESS_STATUS = "ok";
    public static final String FAIL_STATUS = "fail";
    public static final String DONE_MSG = "done";
    public static final String FALSE_MSG = "false";
    public static final String TRUE_MSG = "true";
    public static final String GENERATED_MSG = "generated";
    public static final String NOT_GENERATED_MSG = "not generated";
    public static final String HMAC_KEYS_DOES_NOT_FOUND_MSG = "HMAC-SHA256 keys are not found in Android keystore.";

    public static final String INSTALLED_STATE_MSG =  "TonWalletApplet is invalid (is not personalized)";
    public static final String PERSONALIZED_STATE_MSG = "TonWalletApplet is personalized.";
    public static final String WAITE_AUTHORIZATION_MSG =  "TonWalletApplet waits two-factor authorization.";
    public static final String DELETE_KEY_FROM_KEYCHAIN_MSG = "TonWalletApplet is personalized and waits finishing key deleting from keychain.";
    public static final String BLOCKED_MSG = "TonWalletApplet is blocked.";

    public static final short DATA_PORTION_MAX_SIZE = 128;

    public final static String DEFAULT_SERIAL_NUMBER = "504394802433901126813236";
    public final static String EMPTY_SERIAL_NUMBER = "empty";
    public static final byte PUBLIC_KEY_LEN = 32;
    public static final byte TRANSACTION_HASH_SIZE = 32;
    public static final byte SIG_LEN = 0x40;
    public static final byte[] DEFAULT_PIN = new byte[]{0x35, 0x35, 0x35, 0x35};
    public static final byte PIN_SIZE = (byte) 4;
    public final static byte MAX_PIN_TRIES = (byte) 10;
    public static final short DATA_FOR_SIGNING_MAX_SIZE = (short) 189;
    public static final short DATA_FOR_SIGNING_MAX_SIZE_FOR_CASE_WITH_PATH = (short) 178;
    public final static byte SHA_HASH_SIZE = (short) 32;
    public static short PASSWORD_SIZE = 128;
    public static short IV_SIZE = 16;
    public static final byte SAULT_LENGTH = (short) 32;
    public final static short HMAC_SHA_SIG_SIZE = (short) 32;
    public static final short MAX_NUMBER_OF_KEYS_IN_KEYCHAIN = 1023;
    public static final short MAX_KEY_SIZE_IN_KEYCHAIN = 8192;
    public static final short KEY_CHAIN_SIZE = 32767;
    public final static short MAX_HD_INDEX_SIZE = (short) 10;
    public static final short COMMON_SECRET_SIZE = 32;
    public static final short MAX_HMAC_FAIL_TRIES = 20;
    public static final short KEYCHAIN_KEY_INDEX_LEN = 2;
    public static final short DATA_RECOVERY_PORTION_MAX_SIZE = 250;
    public static final short RECOVERY_DATA_MAX_SIZE = 2048;
    public final static short SERIAL_NUMBER_SIZE = (short) 24;

    public static final String STATUS_FIELD = "status";
    public static final String ERROR_CODE_FIELD = "errorCode";
    public static final String ERROR_TYPE_FIELD = "errorType";
    public static final String ERROR_TYPE_ID_FIELD = "errorTypeId";
    public static final String MESSAGE_FIELD = "message";
    public static final String CARD_INSTRUCTION_FIELD = "cardInstruction";
    public static final String APDU_FIELD = "apdu";

}
