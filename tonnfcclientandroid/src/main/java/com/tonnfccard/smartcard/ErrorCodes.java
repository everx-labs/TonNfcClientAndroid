package com.tonnfccard.smartcard;

import com.tonnfccard.smartcard.wrappers.RAPDU;
import com.tonnfccard.utils.ByteArrayHelper;

import java.util.HashMap;
import java.util.Map;

public class ErrorCodes {
    private static final ByteArrayHelper BYTE_ARRAY_HELPER = ByteArrayHelper.getInstance();
    private static Map<Short, String> codeToMsg = new HashMap<>();

    /** Javacard standard status words */
    public final static short SW_SUCCESS  = (short) 0x9000;
    public final static short SW_WRONG_LENGTH  = (short) 0x6700;
    public final static short SW_APPLET_SELECT_FAILED   = (short) 0x6999;
    public final static short SW_RESPONSE_BYTES_REMAINING   = (short) 0x6100;
    public final static short SW_CLA_NOT_SUPPORTED   = (short) 0x6E00;
    public final static short SW_COMMAND_CHAINING_NOT_SUPPORTED   = (short) 0x6884;
    public final static short SW_COMMAND_NOT_ALLOWED   = (short) 0x6986;
    public final static short SW_CONDITIONS_OF_USE_NOT_SATISFIED   = (short) 0x6985;
    public final static short SW_CORRECT_EXPECTED_LENGTH   = (short) 0x6C00;
    public final static short SW_DATA_INVALID   = (short) 0x6984;
    public final static short SW_NOT_ENOUGH_MEMORY_SPACE_IN_FILE   = (short) 0x6A84;
    public final static short SW_FILE_INVALID   = (short) 0x6983;
    public final static short SW_FILE_NOT_FOUND  = (short) 0x6A82;
    public final static short SW_FUNCTION_NOT_SUPPORTED  = (short) 0x6A81;
    public final static short SW_INCORRECT_P1_P2  = (short) 0x6A86;
    public final static short SW_INS_NOT_SUPPORTED  = (short) 0x6D00;
    public final static short SW_LAST_COMMAND_IN_CHAIN_EXPECTED  = (short) 0x6883;
    public final static short SW_LOGICAL_CHANNEL_NOT_SUPPORTED  = (short) 0x6881;
    public final static short SW_RECORD_NOT_FOUND  = (short) 0x6883;
    public final static short SW_SECURE_MESSAGING_NOT_SUPPORTED  = (short) 0x6882;
    public final static short SW_SECURITY_CONDITION_NOT_SATISFIED  = (short) 0x6982;
    public final static short SW_COMMAND_ABORTED = (short) 0x6F00;
    public final static short SW_WRONG_DATA = (short) 0x6A80;


    /** TonWalletApplet status words*/
    // Common errors
    public final static short SW_INTERNAL_BUFFER_IS_NULL_OR_TOO_SMALL  = (short) 0x4F00;
    public final static short SW_PERSONALIZATION_NOT_FINISHED  = (short) 0x4F01;
    public final static short SW_INCORRECT_OFFSET = (short) 0x4F02;
    public final static short SW_INCORRECT_PAYLOAD = (short) 0x4F03;

    // Password authentication errors
    public final static short SW_INCORRECT_PASSWORD_FOR_CARD_AUTHENICATION   = (short) 0x5F00;
    public final static short SW_INCORRECT_PASSWORD_CARD_IS_BLOCKED   = (short) 0x5F01;

    // Signature errors
    public final static short SW_SET_COIN_TYPE_FAILED = (short) 0x6F01;
    public final static short SW_SET_CURVE_FAILED = (short) 0x6F02;
    public final static short SW_GET_COIN_PUB_DATA_FAILED = (short) 0x6F03;
    public final static short SW_SIGN_DATA_FAILED = (short) 0x6F04;

    // Pin verification errors (for Ton wallet applet)
    public final static short SW_INCORRECT_PIN = (short) 0x6F07;
    public final static short SW_PIN_TRIES_EXPIRED = (short) 0x6F08;

    // Pin verification errors (for Coin manager)
    public final static short SW_INCORRECT_PIN_COIN_MANAGER = (short) 0x9B01;
    public final static short SW_UPDATE_PIN_ERROR = (short) 0x9B02;


    public final static short SW_LOAD_SEED_ERROR = (short) 0x9B03;


    // Key chain errors
    public final static short SW_INCORRECT_KEY_INDEX  = (short) 0x7F00;
    public final static short SW_INCORRECT_KEY_CHUNK_START_OR_LEN  = (short) 0x7F01;
    public final static short SW_INCORRECT_KEY_CHUNK_LEN  = (short) 0x7F02;
    public final static short SW_NOT_ENOUGH_SPACE = (short) 0x7F03;
    public final static short SW_KEY_SIZE_UNKNOWN  = (short) 0x7F04;
    public final static short SW_KEY_LEN_INCORRECT  = (short) 0x7F05;
    public final static short SW_HMAC_EXISTS   = (short) 0x7F06;
    public final static short SW_INCORRECT_KEY_INDEX_TO_CHANGE  = (short) 0x7F07;
    public final static short SW_MAX_KEYS_NUMBER_EXCEEDED  = (short) 0x7F08;
    public final static short SW_DELETE_KEY_CHUNK_IS_NOT_FINISHED  = (short) 0x7F09;


    // Hmac errors
    public final static short SW_INCORRECT_SAULT   = (short) 0x8F01;
    public final static short SW_DATA_INTEGRITY_CORRUPTED  = (short) 0x8F02;
    public final static short SW_INCORRECT_APDU_HMAC  = (short) 0x8F03;
    public final static short SW_HMAC_VERIFICATION_TRIES_EXPIRED   = (short) 0x8F04;

    // Recovery errors
    public final static short SW_RECOVERY_DATA_TOO_LONG = (short) 0x6F09;
    public final static short SW_INCORRECT_START_POS_OR_LE = (short) 0x6F0A;
    public final static short SW_INTEGRITY_OF_RECOVERY_DATA_CORRUPTED = (short) 0x6F0B;
    public final static short SW_RECOVERY_DATA_ALREADY_EXISTS = (short) 0x6F0C;
    public final static short SW_RECOVERY_DATA_IS_NOT_SET = (short) 0x6F0D;

    // Serial number errors
    public final static short SW_SERIAL_NUMBER_IS_NOT_SET    = (short) 0xA001;
    public final static short SW_SERIAL_NUMBER_INCORRECT_FORMAT = (short) 0xA002;



    static {
        addError(SW_SUCCESS, "No error.                                                                                                                  ");
        addError(SW_APPLET_SELECT_FAILED, "Applet select failed.                                                                                                                  ");
        addError(SW_RESPONSE_BYTES_REMAINING, "Response bytes remaining.                                                                                                                  ");
        addError(SW_CLA_NOT_SUPPORTED, "CLA value not supported.                                                                                                                  ");
        addError(SW_COMMAND_CHAINING_NOT_SUPPORTED, "Command chaining not supported.                                                                                                                  ");
        addError(SW_COMMAND_NOT_ALLOWED, "Command not allowed (no current EF).                                                                                                                  ");
        addError(SW_CONDITIONS_OF_USE_NOT_SATISFIED, "Conditions of use not satisﬁed.                                                                                                                  ");
        addError(SW_CORRECT_EXPECTED_LENGTH, "Correct Expected Length (Le).                                                                                                                    ");
        addError(SW_DATA_INVALID, "Data invalid.                                                                                                                    ");
        addError(SW_NOT_ENOUGH_MEMORY_SPACE_IN_FILE, "Not enough memory space in the ﬁle.                                                                                                                    ");
        addError(SW_FILE_INVALID, "File invalid.                                                                                                                    ");
        addError(SW_FILE_NOT_FOUND, "File not found.                                                                                                                    ");
        addError(SW_FUNCTION_NOT_SUPPORTED, "Function not supported.                                                                                                                    ");
        addError(SW_INCORRECT_P1_P2, "Incorrect parameters (P1,P2).                                                                                                                    ");
        addError(SW_INS_NOT_SUPPORTED, "INS value not supported.                                                                                                                     ");
        addError(SW_LAST_COMMAND_IN_CHAIN_EXPECTED, "Last command in chain expected.                                                                                                                     ");
        addError(SW_LOGICAL_CHANNEL_NOT_SUPPORTED, "Card does not support the operation on the speciﬁed logical channel.                                                                                   ");
        addError(SW_RECORD_NOT_FOUND, "Record not found.                                                                                   ");
        addError(SW_SECURE_MESSAGING_NOT_SUPPORTED, "Card does not support secure messaging.                                                                                   ");
        addError(SW_SECURITY_CONDITION_NOT_SATISFIED, "Security condition not satisﬁed.                                                                                   ");
        addError(SW_COMMAND_ABORTED, "Command aborted, No precise diagnosis.                                                                                   ");
        addError(SW_WRONG_DATA, "Wrong data.                                                                                   ");
        addError(SW_WRONG_LENGTH, "Wrong length.                                                                                   ");

        addError(SW_INTERNAL_BUFFER_IS_NULL_OR_TOO_SMALL, "Internal buffer is null or too small.                                                                                   ");
        addError(SW_PERSONALIZATION_NOT_FINISHED, "Personalization is not finished.                                                                                   ");
        addError(SW_INCORRECT_OFFSET, "Internal error: incorrect offset.                                                                                   ");
        addError(SW_INCORRECT_PAYLOAD, "Internal error: incorrect payload value.                                                                                   ");

        addError(SW_INCORRECT_PASSWORD_FOR_CARD_AUTHENICATION, "Incorrect password for card authentication.                                                                                 ");
        addError(SW_INCORRECT_PASSWORD_CARD_IS_BLOCKED , "Incorrect password, card is locked.                                                                                 ");

        addError(SW_SET_COIN_TYPE_FAILED, "Set coin type failed.                                                                                 ");
        addError(SW_SET_CURVE_FAILED, "Set curve failed.                                                                                 ");
        addError(SW_GET_COIN_PUB_DATA_FAILED, "Get coin pub data failed.                                                                                 ");
        addError(SW_SIGN_DATA_FAILED, "Sign data failed.                                                                                 ");

        addError(SW_INCORRECT_PIN, "Incorrect PIN (from Ton wallet applet).                                                                                ");
        addError(SW_INCORRECT_PIN_COIN_MANAGER, "Incorrect PIN (from Coin manager).                                                                                 ");
        addError(SW_UPDATE_PIN_ERROR, "Update PIN error (for CHANGE_PIN) or wallet status not support to export (for GENERATE SEED).                                                                                 ");
        addError(SW_PIN_TRIES_EXPIRED, "PIN tries expired.                                                                                 ");

        addError(SW_LOAD_SEED_ERROR, "Load seed error.                                                                                 ");

        addError(SW_INCORRECT_KEY_INDEX , "Incorrect key index.                                                                                 ");
        addError(SW_INCORRECT_KEY_CHUNK_START_OR_LEN, "Incorrect key chunk start or length.                                                                                 ");
        addError(SW_INCORRECT_KEY_CHUNK_LEN, "Incorrect key chunk length.                                                                                 ");
        addError(SW_NOT_ENOUGH_SPACE, "Not enough space.                                                                                 ");
        addError(SW_KEY_SIZE_UNKNOWN, "Key size unknown.                                                                                 ");
        addError(SW_KEY_LEN_INCORRECT, "Key length incorrect.                                                                                 ");
        addError(SW_HMAC_EXISTS, "Hmac exists already.                                                                                 ");
        addError(SW_INCORRECT_KEY_INDEX_TO_CHANGE, "Incorrect key index to change.                                                                                 ");
        addError(SW_MAX_KEYS_NUMBER_EXCEEDED, "Max number of keys (1023) is exceeded.                                                                                 ");
        addError(SW_DELETE_KEY_CHUNK_IS_NOT_FINISHED, "Delete key chunk is not finished.                                                                                 ");

        addError(SW_INCORRECT_SAULT, "Incorrect sault.                                                                                 ");
        addError(SW_DATA_INTEGRITY_CORRUPTED, "Data integrity corrupted.                                                                                 ");
        addError(SW_INCORRECT_APDU_HMAC, "Incorrect apdu hmac. ");
        addError(SW_HMAC_VERIFICATION_TRIES_EXPIRED, "Apdu Hmac verification tries expired.                                                                                 ");

        addError(SW_RECOVERY_DATA_TOO_LONG, "Too big length of recovery data                                                                                 ");
        addError(SW_INTEGRITY_OF_RECOVERY_DATA_CORRUPTED, "Hash of recovery data is incorrect                                                                                 ");
        addError(SW_INCORRECT_START_POS_OR_LE, "Incorrect start or length of recovery data piece in internal buffer                                                                                 ");
        addError(SW_RECOVERY_DATA_ALREADY_EXISTS, "Recovery data already exists                                                                                 ");
        addError(SW_RECOVERY_DATA_IS_NOT_SET, "Recovery data does not exist                                                                                 ");

        addError(SW_SERIAL_NUMBER_IS_NOT_SET, "Serial number does not exist. You must set it.                                                                                 ");
        addError(SW_SERIAL_NUMBER_INCORRECT_FORMAT, "Serial number must be a hex string of format 0X0Y0Z ..., where X,Y,Z (etc) are > =0 and <=9                                                                                  ");

    }

    private static void addError(Short sw, String errMsg) {
        codeToMsg.put(sw, errMsg.trim());
    }

    public static String getMsg(RAPDU apduAnswer) throws Exception {
        byte[] swBytes = {apduAnswer.getSW1(), apduAnswer.getSW2()};
        int sw = BYTE_ARRAY_HELPER.makeShort(swBytes, (short)0);
        return codeToMsg.get(sw);
    }

    public static String getMsg(String apduAnswer) throws Exception {
        int sw = BYTE_ARRAY_HELPER.makeShort(BYTE_ARRAY_HELPER.bytes(apduAnswer.trim()), (short) 0);
        return codeToMsg.get((short) sw);
    }

    public static String getMsg(short sw) {
    return codeToMsg.get((short) sw);
  }

}
