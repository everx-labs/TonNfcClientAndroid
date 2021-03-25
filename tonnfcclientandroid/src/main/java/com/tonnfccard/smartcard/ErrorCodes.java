package com.tonnfccard.smartcard;

import androidx.annotation.RestrictTo;

import com.tonnfccard.utils.ByteArrayUtil;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_APDU_RESPONSE_IS_NULL;

/**
 * Here there are all status words that can be produced by applet
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class ErrorCodes {
    private static final ByteArrayUtil BYTE_ARRAY_HELPER = ByteArrayUtil.getInstance();
    private static Map<Short, String> codeToMsg = new LinkedHashMap<>();

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


    /** TonWalletApplet and CoinManager status words*/

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


    // Messages for status words
    public final static  String SW_SUCCESS_MSG =  "No error.";
    public final static  String SW_APPLET_SELECT_FAILED_MSG = "Applet select failed.";
    public final static  String SW_RESPONSE_BYTES_REMAINING_MSG = "Response bytes remaining.";
    public final static  String SW_CLA_NOT_SUPPORTED_MSG = "CLA value not supported.";
    public final static  String SW_COMMAND_CHAINING_NOT_SUPPORTED_MSG = "Command chaining not supported.";
    public final static  String SW_COMMAND_NOT_ALLOWED_MSG = "Command not allowed (no current EF).";
    public final static  String SW_CONDITIONS_OF_USE_NOT_SATISFIED_MSG = "Conditions of use not satisﬁed.";
    public final static  String SW_CORRECT_EXPECTED_LENGTH_MSG = "Correct Expected Length (Le).";
    public final static  String SW_DATA_INVALID_MSG = "Data invalid.";
    public final static  String SW_NOT_ENOUGH_MEMORY_SPACE_IN_FILE_MSG = "Not enough memory space in the ﬁle.";
    public final static  String SW_FILE_INVALID_MSG = "File invalid.";
    public final static  String SW_FILE_NOT_FOUND_MSG = "File not found.";
    public final static  String SW_FUNCTION_NOT_SUPPORTED_MSG = "Function not supported.";
    public final static  String SW_INCORRECT_P1_P2_MSG = "Incorrect parameters (P1,P2).";
    public final static  String SW_INS_NOT_SUPPORTED_MSG = "INS value not supported.";
    public final static  String SW_LAST_COMMAND_IN_CHAIN_EXPECTED_MSG = "Last command in chain expected.";
    public final static  String SW_LOGICAL_CHANNEL_NOT_SUPPORTED_MSG = "Card does not support the operation on the speciﬁed logical channel.";
    public final static  String SW_RECORD_NOT_FOUND_MSG = "Record not found.";
    public final static  String SW_SECURE_MESSAGING_NOT_SUPPORTED_MSG = "Card does not support secure messaging.";
    public final static  String SW_SECURITY_CONDITION_NOT_SATISFIED_MSG = "Security condition not satisﬁed.";
    public final static  String SW_COMMAND_ABORTED_MSG = "Command aborted, No precise diagnosis.";
    public final static  String SW_WRONG_DATA_MSG = "Wrong data.";
    public final static  String SW_WRONG_LENGTH_MSG = "Wrong length.";
    public final static  String SW_INTERNAL_BUFFER_IS_NULL_OR_TOO_SMALL_MSG = "Internal buffer is null or too small.";
    public final static  String SW_PERSONALIZATION_NOT_FINISHED_MSG = "Personalization is not finished.";
    public final static  String SW_INCORRECT_OFFSET_MSG = "Internal error: incorrect offset. ";
    public final static  String SW_INCORRECT_PAYLOAD_MSG = "Internal error: incorrect payload value.";
    public final static  String SW_INCORRECT_PASSWORD_FOR_CARD_AUTHENICATION_MSG = "Incorrect password for card authentication.";
    public final static  String SW_INCORRECT_PASSWORD_CARD_IS_BLOCKED_MSG = "Incorrect password, card is locked.";
    public final static  String SW_SET_COIN_TYPE_FAILED_MSG = "Set coin type failed.";
    public final static  String SW_SET_CURVE_FAILED_MSG = "Set curve failed.";
    public final static  String SW_GET_COIN_PUB_DATA_FAILED_MSG = "Get coin pub data failed.";
    public final static  String SW_SIGN_DATA_FAILED_MSG = "Sign data failed.";
    public final static  String SW_INCORRECT_PIN_MSG = "Incorrect PIN (from Ton wallet applet).";
    public final static  String SW_INCORRECT_PIN_COIN_MANAGER_MSG = "Incorrect PIN (from Coin manager).";
    public final static  String SW_UPDATE_PIN_ERROR_MSG = "Update PIN error (for CHANGE_PIN) or wallet status not support to export (for GENERATE SEED).";
    public final static  String SW_PIN_TRIES_EXPIRED_MSG = "PIN tries expired.";
    public final static  String SW_LOAD_SEED_ERROR_MSG = "Load seed error.";
    public final static  String SW_INCORRECT_KEY_INDEX_MSG = "Incorrect key index.";
    public final static  String SW_INCORRECT_KEY_CHUNK_START_OR_LEN_MSG = "Incorrect key chunk start or length.";
    public final static  String SW_INCORRECT_KEY_CHUNK_LEN_MSG = "Incorrect key chunk length.";
    public final static  String SW_NOT_ENOUGH_SPACE_MSG = "Not enough space.";
    public final static  String SW_KEY_SIZE_UNKNOWN_MSG = "Key size unknown.";
    public final static  String SW_KEY_LEN_INCORRECT_MSG = "Key length incorrect. ";
    public final static  String SW_HMAC_EXISTS_MSG = "Hmac exists already.";
    public final static  String SW_INCORRECT_KEY_INDEX_TO_CHANGE_MSG = "Incorrect key index to change";
    public final static  String SW_MAX_KEYS_NUMBER_EXCEEDED_MSG = "Max number of keys (1023) is exceeded.";
    public final static  String SW_DELETE_KEY_CHUNK_IS_NOT_FINISHED_MSG = "Delete key chunk is not finished.";
    public final static  String SW_INCORRECT_SAULT_MSG = "Incorrect sault.";
    public final static  String SW_DATA_INTEGRITY_CORRUPTED_MSG = "Data integrity corrupted.";
    public final static  String SW_INCORRECT_APDU_HMAC_MSG = "Incorrect apdu hmac.";
    public final static  String SW_HMAC_VERIFICATION_TRIES_EXPIRED_MSG = "Apdu Hmac verification tries expired.";
    public final static  String SW_RECOVERY_DATA_TOO_LONG_MSG = "Too big length of recovery data.";
    public final static  String SW_INTEGRITY_OF_RECOVERY_DATA_CORRUPTED_MSG = "Hash of recovery data is incorrect.";
    public final static  String SW_INCORRECT_START_POS_OR_LE_MSG = "Incorrect start or length of recovery data piece in internal buffer.";
    public final static  String SW_RECOVERY_DATA_ALREADY_EXISTS_MSG = "Recovery data already exists.";
    public final static  String SW_RECOVERY_DATA_IS_NOT_SET_MSG = "Recovery data does not exist.";
    public final static  String SW_SERIAL_NUMBER_IS_NOT_SET_MSG = "Serial number does not exist. You must set it.";
    public final static  String SW_SERIAL_NUMBER_INCORRECT_FORMAT_MSG = "Serial number must be a hex string of format 0X0Y0Z ..., where X,Y,Z (etc) are > =0 and <=9.";


    static {
        addError(SW_SUCCESS, SW_SUCCESS_MSG);
        addError(SW_APPLET_SELECT_FAILED, SW_APPLET_SELECT_FAILED_MSG );
        addError(SW_RESPONSE_BYTES_REMAINING, SW_RESPONSE_BYTES_REMAINING_MSG);
        addError(SW_CLA_NOT_SUPPORTED, SW_CLA_NOT_SUPPORTED_MSG);
        addError(SW_COMMAND_CHAINING_NOT_SUPPORTED, SW_COMMAND_CHAINING_NOT_SUPPORTED_MSG);
        addError(SW_COMMAND_NOT_ALLOWED,  SW_COMMAND_NOT_ALLOWED_MSG);
        addError(SW_CONDITIONS_OF_USE_NOT_SATISFIED, SW_CONDITIONS_OF_USE_NOT_SATISFIED_MSG);
        addError(SW_CORRECT_EXPECTED_LENGTH, SW_CORRECT_EXPECTED_LENGTH_MSG);
        addError(SW_DATA_INVALID, SW_DATA_INVALID_MSG);
        addError(SW_NOT_ENOUGH_MEMORY_SPACE_IN_FILE, SW_NOT_ENOUGH_MEMORY_SPACE_IN_FILE_MSG);
        addError(SW_FILE_INVALID, SW_FILE_INVALID_MSG);
        addError(SW_FILE_NOT_FOUND, SW_FILE_NOT_FOUND_MSG);
        addError(SW_FUNCTION_NOT_SUPPORTED, SW_FUNCTION_NOT_SUPPORTED_MSG);
        addError(SW_INCORRECT_P1_P2, SW_INCORRECT_P1_P2_MSG);
        addError(SW_INS_NOT_SUPPORTED, SW_INS_NOT_SUPPORTED_MSG);
        addError(SW_LAST_COMMAND_IN_CHAIN_EXPECTED, SW_LAST_COMMAND_IN_CHAIN_EXPECTED_MSG);
        addError(SW_LOGICAL_CHANNEL_NOT_SUPPORTED, SW_LOGICAL_CHANNEL_NOT_SUPPORTED_MSG);
        addError(SW_RECORD_NOT_FOUND, SW_RECORD_NOT_FOUND_MSG);
        addError(SW_SECURE_MESSAGING_NOT_SUPPORTED, SW_SECURE_MESSAGING_NOT_SUPPORTED_MSG);
        addError(SW_SECURITY_CONDITION_NOT_SATISFIED, SW_SECURITY_CONDITION_NOT_SATISFIED_MSG);
        addError(SW_COMMAND_ABORTED, SW_COMMAND_ABORTED_MSG);
        addError(SW_WRONG_DATA, SW_WRONG_DATA_MSG);
        addError(SW_WRONG_LENGTH, SW_WRONG_LENGTH_MSG);

        addError(SW_INTERNAL_BUFFER_IS_NULL_OR_TOO_SMALL, SW_INTERNAL_BUFFER_IS_NULL_OR_TOO_SMALL_MSG);
        addError(SW_PERSONALIZATION_NOT_FINISHED, SW_PERSONALIZATION_NOT_FINISHED_MSG);
        addError(SW_INCORRECT_OFFSET, SW_INCORRECT_OFFSET_MSG);
        addError(SW_INCORRECT_PAYLOAD, SW_INCORRECT_PAYLOAD_MSG);

        addError(SW_INCORRECT_PASSWORD_FOR_CARD_AUTHENICATION, SW_INCORRECT_PASSWORD_FOR_CARD_AUTHENICATION_MSG);
        addError(SW_INCORRECT_PASSWORD_CARD_IS_BLOCKED , SW_INCORRECT_PASSWORD_CARD_IS_BLOCKED_MSG);

        addError(SW_SET_COIN_TYPE_FAILED, SW_SET_COIN_TYPE_FAILED_MSG);
        addError(SW_SET_CURVE_FAILED, SW_SET_CURVE_FAILED_MSG);
        addError(SW_GET_COIN_PUB_DATA_FAILED, SW_GET_COIN_PUB_DATA_FAILED_MSG);
        addError(SW_SIGN_DATA_FAILED, SW_SIGN_DATA_FAILED_MSG);

        addError(SW_INCORRECT_PIN, SW_INCORRECT_PIN_MSG);
        addError(SW_INCORRECT_PIN_COIN_MANAGER, SW_INCORRECT_PIN_COIN_MANAGER_MSG);
        addError(SW_UPDATE_PIN_ERROR, SW_UPDATE_PIN_ERROR_MSG);
        addError(SW_PIN_TRIES_EXPIRED, SW_PIN_TRIES_EXPIRED_MSG);

        addError(SW_LOAD_SEED_ERROR, SW_LOAD_SEED_ERROR_MSG);

        addError(SW_INCORRECT_KEY_INDEX, SW_INCORRECT_KEY_INDEX_MSG);
        addError(SW_INCORRECT_KEY_CHUNK_START_OR_LEN, SW_INCORRECT_KEY_CHUNK_START_OR_LEN_MSG);
        addError(SW_INCORRECT_KEY_CHUNK_LEN, SW_INCORRECT_KEY_CHUNK_LEN_MSG);
        addError(SW_NOT_ENOUGH_SPACE, SW_NOT_ENOUGH_SPACE_MSG);
        addError(SW_KEY_SIZE_UNKNOWN, SW_KEY_SIZE_UNKNOWN_MSG);
        addError(SW_KEY_LEN_INCORRECT, SW_KEY_LEN_INCORRECT_MSG);
        addError(SW_HMAC_EXISTS, SW_HMAC_EXISTS_MSG);
        addError(SW_INCORRECT_KEY_INDEX_TO_CHANGE, SW_INCORRECT_KEY_INDEX_TO_CHANGE_MSG);
        addError(SW_MAX_KEYS_NUMBER_EXCEEDED, SW_MAX_KEYS_NUMBER_EXCEEDED_MSG);
        addError(SW_DELETE_KEY_CHUNK_IS_NOT_FINISHED, SW_DELETE_KEY_CHUNK_IS_NOT_FINISHED_MSG);

        addError(SW_INCORRECT_SAULT, SW_INCORRECT_SAULT_MSG);
        addError(SW_DATA_INTEGRITY_CORRUPTED, SW_DATA_INTEGRITY_CORRUPTED_MSG);
        addError(SW_INCORRECT_APDU_HMAC, SW_INCORRECT_APDU_HMAC_MSG);
        addError(SW_HMAC_VERIFICATION_TRIES_EXPIRED, SW_HMAC_VERIFICATION_TRIES_EXPIRED_MSG);

        addError(SW_RECOVERY_DATA_TOO_LONG, SW_RECOVERY_DATA_TOO_LONG_MSG);
        addError(SW_INTEGRITY_OF_RECOVERY_DATA_CORRUPTED, SW_INTEGRITY_OF_RECOVERY_DATA_CORRUPTED_MSG);
        addError(SW_INCORRECT_START_POS_OR_LE, SW_INCORRECT_START_POS_OR_LE_MSG);
        addError(SW_RECOVERY_DATA_ALREADY_EXISTS, SW_RECOVERY_DATA_ALREADY_EXISTS_MSG);
        addError(SW_RECOVERY_DATA_IS_NOT_SET, SW_RECOVERY_DATA_IS_NOT_SET_MSG);

        addError(SW_SERIAL_NUMBER_IS_NOT_SET, SW_SERIAL_NUMBER_IS_NOT_SET_MSG);
        addError(SW_SERIAL_NUMBER_INCORRECT_FORMAT, SW_SERIAL_NUMBER_INCORRECT_FORMAT_MSG);

    }

    private static void addError(Short sw, String errMsg) {
        codeToMsg.put(sw, errMsg.trim());
    }

    public static String getMsg(RAPDU apduResponse)  {
        if (apduResponse == null) throw new IllegalArgumentException(ERROR_MSG_APDU_RESPONSE_IS_NULL);
        byte[] swBytes = {apduResponse.getSW1(), apduResponse.getSW2()};
        short sw = BYTE_ARRAY_HELPER.makeShort(swBytes, (short)0);
        return codeToMsg.get(sw);
    }

    public static String getMsg(String apduResponse)  {
        if (apduResponse == null) throw new IllegalArgumentException(ERROR_MSG_APDU_RESPONSE_IS_NULL);
        short sw = BYTE_ARRAY_HELPER.makeShort(BYTE_ARRAY_HELPER.bytes(apduResponse.trim()), (short) 0);
        return codeToMsg.get(sw);
    }

    public static String getMsg(short sw) {
        return codeToMsg.get(sw);
    }

}
