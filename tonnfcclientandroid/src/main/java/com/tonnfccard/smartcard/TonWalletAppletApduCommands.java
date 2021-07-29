package com.tonnfccard.smartcard;

import androidx.annotation.RestrictTo;

import com.tonnfccard.helpers.HmacHelper;
import com.tonnfccard.utils.ByteArrayUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import static com.tonnfccard.smartcard.CommonConstants.LE;
import static com.tonnfccard.TonWalletConstants.*;
import static com.tonnfccard.smartcard.CommonConstants.SELECT_CLA;
import static com.tonnfccard.smartcard.CommonConstants.SELECT_INS;
import static com.tonnfccard.smartcard.CommonConstants.SELECT_P1;
import static com.tonnfccard.smartcard.CommonConstants.SELECT_P2;

/**
 * Here there are all objects representing APDU commands of TonWalletApplet
 */

@RestrictTo(RestrictTo.Scope.LIBRARY)
public class TonWalletAppletApduCommands {
  private static final ByteArrayUtil BYTE_ARRAY_HELPER = ByteArrayUtil.getInstance();
  private static HmacHelper HMAC_HELPER = HmacHelper.getInstance();

  public static void setHmacHelper(HmacHelper hmacHelper) {
    HMAC_HELPER = hmacHelper;
  }

  public static final byte[] TON_WALLET_APPLET_AID = {0x31, 0x31, 0x32, 0x32, 0x33, 0x33, 0x34, 0x34, 0x35, 0x35, 0x36, 0x36}; //"31313232333334343535363600";

  // code of CLA byte in the command APDU header
  public final static byte WALLET_APPLET_CLA = (byte) 0xB0;

  public final static byte P1 = (byte) 0x00;
  public final static byte P2 = (byte) 0x00;

  private static Map<Byte, String> tonWalletAppletCommandsNames = new HashMap<>();

  //Personalization
  public static final byte INS_FINISH_PERS = (byte)0x90;
  public static final byte INS_SET_ENCRYPTED_PASSWORD_FOR_CARD_AUTHENTICATION = (byte)0x91;
  public static final byte INS_SET_ENCRYPTED_COMMON_SECRET = (byte)0x94;
  public static final byte INS_SET_SERIAL_NUMBER  = (byte)0x96;

  //Waite for authentication mode
  public static final byte INS_VERIFY_PASSWORD = (byte)0x92;
  public static final byte INS_GET_HASH_OF_ENCRYPTED_PASSWORD = (byte)0x93;
  public static final byte INS_GET_HASH_OF_ENCRYPTED_COMMON_SECRET = (byte)0x95;


  // Main mode
  public static final byte INS_GET_PUBLIC_KEY = (byte)0xA0;
  public static final byte INS_GET_PUBLIC_KEY_WITH_DEFAULT_HD_PATH  = (byte)0xA7;

  public static final byte INS_VERIFY_PIN = (byte)0xA2;

  public static final byte INS_SIGN_SHORT_MESSAGE = (byte)0xA3;
  public static final byte INS_SIGN_SHORT_MESSAGE_WITH_DEFAULT_PATH = (byte)0xA5;

  public static final byte INS_GET_APP_INFO = (byte)0xC1;
  public static final byte INS_GET_SERIAL_NUMBER  = (byte)0xC2;

  public static final byte INS_CHECK_KEY_HMAC_CONSISTENCY = (byte)0xB0;
  public static final byte INS_GET_KEY_INDEX_IN_STORAGE_AND_LEN = (byte)0xB1;
  public static final byte INS_GET_KEY_CHUNK = (byte)0xB2;
  public static final byte INS_CHECK_AVAILABLE_VOL_FOR_NEW_KEY = (byte)0xB3;
  public static final byte INS_ADD_KEY_CHUNK = (byte)0xB4;
  public static final byte INS_INITIATE_CHANGE_OF_KEY = (byte)0xB5;
  public static final byte INS_CHANGE_KEY_CHUNK = (byte)0xB6;
  public static final byte INS_INITIATE_DELETE_KEY = (byte)0xB7;
  public static final byte INS_GET_NUMBER_OF_KEYS = (byte)0xB8; /**/
  public static final byte INS_GET_FREE_STORAGE_SIZE = (byte)0xB9;
  public static final byte INS_GET_OCCUPIED_STORAGE_SIZE = (byte)0xBA;
  public static final byte INS_GET_HMAC = (byte)0xBB;
  public static final byte INS_RESET_KEYCHAIN = (byte)0xBC;
  public static final byte INS_GET_SAULT = (byte)0xBD;

  public static final byte INS_DELETE_KEY_CHUNK = (byte)0xBE;
  public static final byte INS_DELETE_KEY_RECORD = (byte)0xBF;
  public static final byte INS_GET_DELETE_KEY_CHUNK_NUM_OF_PACKETS =  (byte)0xE1;
  public static final byte INS_GET_DELETE_KEY_RECORD_NUM_OF_PACKETS =  (byte)0xE2;

  public static final byte INS_ADD_RECOVERY_DATA_PART = (byte)0xD1;
  public static final byte INS_GET_RECOVERY_DATA_PART = (byte)0xD2;
  public static final byte INS_GET_RECOVERY_DATA_HASH = (byte)0xD3;
  public static final byte INS_GET_RECOVERY_DATA_LEN = (byte)0xD4;
  public static final byte INS_RESET_RECOVERY_DATA = (byte)0xD5;
  public static final byte INS_IS_RECOVERY_DATA_SET = (byte)0xD6;

  public static final byte GET_APP_INFO_LE = 0x01;
  public static final byte GET_NUMBER_OF_KEYS_LE = 0x02;
  public static final byte GET_KEY_INDEX_IN_STORAGE_AND_LEN_LE = 0x04;
  public static final byte INITIATE_DELETE_KEY_LE = 0x02;
  public static final byte GET_FREE_SIZE_LE = 0x02;
  public static final byte GET_OCCUPIED_SIZE_LE  = 0x02;
  public static final byte SEND_CHUNK_LE  = 0x02;
  public static final byte DELETE_KEY_CHUNK_LE  = 0x01;
  public static final byte DELETE_KEY_RECORD_LE  = 0x01;
  public static final byte GET_HMAC_LE  = 0x22;
  public static final byte GET_DELETE_KEY_CHUNK_NUM_OF_PACKETS_LE = 0x02;
  public static final byte GET_DELETE_KEY_RECORD_NUM_OF_PACKETS_LE = 0x02;
  public static final byte GET_SERIAL_NUMBER_LE =  (byte) 0x18;
  public static final byte GET_RECOVERY_DATA_LEN_LE =  0x02;
  public static final byte IS_RECOVERY_DATA_SET_LE =  0x01;

  /**
   *This command selects TonWalletApplet to start communicating with applet.
   */
  public final static CAPDU SELECT_TON_WALLET_APPLET_APDU = new CAPDU(SELECT_CLA, SELECT_INS, SELECT_P1, SELECT_P2, TON_WALLET_APPLET_AID, LE); // 00 A4 04 00 0C 31 31 32 32 33 33 34 34 35 35 36 36 00

  /**
   * GET_APP_INFO
   *
   * CLA: 0xB0
   * INS: 0xC1
   * P1: 0x00
   * P2: 0x00
   * LE: 0x01
   *
   * This command returns applet state. Available in any applet state. Applet state = 0x07, 0x17, 0x27, 0x37 or 0x47
   */
  public final static CAPDU GET_APP_INFO_APDU = new CAPDU(WALLET_APPLET_CLA, INS_GET_APP_INFO, P1, P2, GET_APP_INFO_LE);

  /**
   * GET_HASH_OF_ENCRYPTED_PASSWORD
   *
   * CLA: 0xB0
   * INS: 0x93
   * P1: 0x00
   * P2: 0x00
   * LE: 0x20
   *
   * This command returns SHA256 hash of encrypted (by AES) activation password. Available only in WAITE_AUTHENTICATION_MODE state of applet.
   */
  public final static CAPDU GET_HASH_OF_ENCRYPTED_PASSWORD_APDU = new CAPDU(WALLET_APPLET_CLA, INS_GET_HASH_OF_ENCRYPTED_PASSWORD, P1, P2, SHA_HASH_SIZE);

  /**
   * GET_HASH_OF_COMMON_SECRET
   *
   * CLA: 0xB0
   * INS: 0x95
   * P1: 0x00
   * P2: 0x00
   * LE: 0x20
   *
   * This command returns SHA256 hash of encrypted (by AES) activation common secret. Available only in WAITE_AUTHENTICATION_MODE state of applet.
   */
  public final static CAPDU GET_HASH_OF_ENCRYPTED_COMMON_SECRET_APDU = new CAPDU(WALLET_APPLET_CLA, INS_GET_HASH_OF_ENCRYPTED_COMMON_SECRET, P1, P2, SHA_HASH_SIZE);

  /**
   * GET_PUBLIC_KEY_WITH_DEFAULT_HD_PATH
   *
   * CLA: 0xB0
   * INS: 0xA7
   * P1: 0x00
   * P2: 0x00
   * LE: 0x20
   *
   * This function retrieves ED25519 public key from CoinManager for fixed bip44 HD path m/44'/396'/0'/0'/0
   */
  public final static CAPDU GET_PUB_KEY_WITH_DEFAULT_PATH_APDU = new CAPDU(WALLET_APPLET_CLA, INS_GET_PUBLIC_KEY_WITH_DEFAULT_HD_PATH, P1, P2, PUBLIC_KEY_LEN);

  /**
   * GET_SAULT
   *
   * CLA: 0xB0
   * INS: 0xBD
   * P1: 0x00
   * P2: 0x00
   * LE: 0x20
   *
   * The command outputs random 32-bytes sault produced by card. This sault must be used  by the host to generate HMAC.
   * In the end of its work it calls generateNewSault. So each call of GET_SAULT should produce new random looking sault
   * Available in applet states PERSONALIZED and DELETE_KEY_FROM_KEYCHAIN_MODE.
   */
  public final static CAPDU GET_SAULT_APDU = new CAPDU(WALLET_APPLET_CLA, INS_GET_SAULT, P1, P2, SAULT_LENGTH);

  /**
   * GET_RECOVERY_DATA_HASH
   *
   * CLA: 0xB0
   * INS: 0xD3
   * P1: 0x00
   * P2: 0x00
   * LE: 0x20
   *
   * This function returns SHA256 hash of encrypted binary blob saved during registration in Recovery service. This is necessary to control the integrity.
   * Available in applet states PERSONALIZED and DELETE_KEY_FROM_KEYCHAIN_MODE.
   */
  public final static CAPDU GET_RECOVERY_DATA_HASH_APDU = new CAPDU(WALLET_APPLET_CLA, INS_GET_RECOVERY_DATA_HASH, P1, P2, SHA_HASH_SIZE);

  /**
   * GET_RECOVERY_DATA_LEN
   *
   * CLA: 0xB0
   * INS: 0xD4
   * P1: 0x00
   * P2: 0x00
   * LE:  0x02
   *
   * This function returns real length of recovery data  saved in applet's internal buffer.
   * Available in applet states PERSONALIZED and DELETE_KEY_FROM_KEYCHAIN_MODE.
   */
  public final static CAPDU GET_RECOVERY_DATA_LEN_APDU = new CAPDU(WALLET_APPLET_CLA, INS_GET_RECOVERY_DATA_LEN, P1, P2, GET_RECOVERY_DATA_LEN_LE);

  /**
   * IS_RECOVERY_DATA_SET
   *
   * CLA: 0xB0
   * INS: 0xD6
   * P1: 0x00
   * P2: 0x00
   * LE: 0x01
   *
   * Returns 0x01 if recovery data is set, 0x00 if not.
   * Available in applet states PERSONALIZED and DELETE_KEY_FROM_KEYCHAIN_MODE.
   */
  public final static CAPDU IS_RECOVERY_DATA_SET_APDU = new CAPDU(WALLET_APPLET_CLA, INS_IS_RECOVERY_DATA_SET, P1, P2, IS_RECOVERY_DATA_SET_LE);

  /**
   * RESET_RECOVERY_DATA
   *
   * CLA: 0xB0
   * INS: 0xD5
   * P1: 0x00
   * P2: 0x00
   *
   * This function reset recovery data, internal buffer is filled by zeros, internal variable realRecoveryDataLen is set to 0, internal flag  isRecoveryDataSet is set to false.
   * Available in applet states PERSONALIZED and DELETE_KEY_FROM_KEYCHAIN_MODE
   */
  public final static CAPDU RESET_RECOVERY_DATA_APDU = new CAPDU(WALLET_APPLET_CLA, INS_RESET_RECOVERY_DATA, P1, P2);

  /**
   * GET_SERIAL_NUMBER
   *
   * CLA: 0xB0
   * INS: 0x80
   * P1: 0xC2
   * P2: 0x00
   * LE: 0x18
   *
   * This command returns card serial number. Available in any applet state.
   */
  public final static CAPDU GET_SERIAL_NUMBER_APDU = new CAPDU(WALLET_APPLET_CLA, INS_GET_SERIAL_NUMBER, P1, P2, GET_SERIAL_NUMBER_LE);

  public static final List<CAPDU> GET_APPLET_STATE_APDU_LIST = Arrays.asList(SELECT_TON_WALLET_APPLET_APDU, GET_APP_INFO_APDU);

  public final static String FINISH_PERS_APDU_NAME = "FINISH_PERS";
  public final static String SET_ENCRYPTED_PASSWORD_FOR_CARD_AUTHENTICATION_APDU_NAME = "SET_ENCRYPTED_PASSWORD_FOR_CARD_AUTHENTICATION";
  public final static String SET_ENCRYPTED_COMMON_SECRET_APDU_NAME = "SET_ENCRYPTED_COMMON_SECRET";
  public final static String VERIFY_PASSWORD_APDU_NAME = "VERIFY_PASSWORD";
  public final static String GET_HASH_OF_ENCRYPTED_PASSWORD_APDU_NAME = "GET_HASH_OF_ENCRYPTED_PASSWORD";
  public final static String GET_HASH_OF_ENCRYPTED_COMMON_SECRET_APDU_NAME = "GET_HASH_OF_ENCRYPTED_COMMON_SECRET";
  public final static String VERIFY_PIN_APDU_NAME = "VERIFY_PIN";
  public final static String GET_PUBLIC_KEY_APDU_NAME = "GET_PUBLIC_KEY";
  public final static String GET_PUBLIC_KEY_WITH_DEFAULT_HD_PATH_APDU_NAME = "GET_PUBLIC_KEY_WITH_DEFAULT_HD_PATH";
  public final static String SIGN_SHORT_MESSAGE_APDU_NAME = "SIGN_SHORT_MESSAGE";
  public final static String SIGN_SHORT_MESSAGE_WITH_DEFAULT_PATH_APDU_NAME = "SIGN_SHORT_MESSAGE_WITH_DEFAULT_PATH";
  public final static String GET_APP_INFO_APDU_NAME = "GET_APP_INFO";
  public final static String GET_KEY_INDEX_IN_STORAGE_AND_LEN_APDU_NAME = "GET_KEY_INDEX_IN_STORAGE_AND_LEN";
  public final static String GET_KEY_CHUNK_APDU_NAME = "GET_KEY_CHUNK";
  public final static String CHECK_AVAILABLE_VOL_FOR_NEW_KEY_APDU_NAME = "CHECK_AVAILABLE_VOL_FOR_NEW_KEY";
  public final static String ADD_KEY_CHUNK_APDU_NAME = "ADD_KEY_CHUNK";
  public final static String INITIATE_CHANGE_OF_KEY_APDU_NAME = "INITIATE_CHANGE_OF_KEY";
  public final static String CHANGE_KEY_CHUNK_APDU_NAME = "CHANGE_KEY_CHUNK";
  public final static String DELETE_KEY_CHUNK_APDU_NAME = "DELETE_KEY_CHUNK";
  public final static String GET_DELETE_KEY_CHUNK_NUM_OF_PACKETS_APDU_NAME = "GET_DELETE_KEY_CHUNK_NUM_OF_PACKETS";
  public final static String GET_DELETE_KEY_RECORD_NUM_OF_PACKETS_APDU_NAME = "GET_DELETE_KEY_RECORD_NUM_OF_PACKETS";
  public final static String INITIATE_DELETE_KEY_APDU_NAME = "INITIATE_DELETE_KEY";
  public final static String DELETE_KEY_RECORD_APDU_NAME = "DELETE_KEY_RECORD";
  public final static String GET_NUMBER_OF_KEYS_APDU_NAME = "GET_NUMBER_OF_KEYS";
  public final static String GET_FREE_STORAGE_APDU_NAME = "GET_FREE_STORAGE";
  public final static String GET_OCCUPIED_STORAGE_APDU_NAME = "GET_OCCUPIED_STORAGE";
  public final static String GET_HMAC_APDU_NAME = "GET_HMAC";
  public final static String RESET_KEYCHAIN_APDU_NAME = "RESET_KEYCHAIN";
  public final static String GET_SAULT_APDU_NAME = "GET_SAULT";
  public final static String CHECK_KEY_HMAC_CONSISTENCY_APDU_NAME = "CHECK_KEY_HMAC_CONSISTENCY";
  public final static String GET_SERIAL_NUMBER_APDU_NAME = "GET_SERIAL_NUMBER";
  public final static String SET_SERIAL_NUMBER_APDU_NAME = "SET_SERIAL_NUMBER";
  public final static String GET_RECOVERY_DATA_HASH_APDU_NAME = "GET_RECOVERY_DATA_HASH";
  public final static String GET_RECOVERY_DATA_LEN_APDU_NAME = "GET_RECOVERY_DATA_LEN";
  public final static String GET_GET_RECOVERY_DATA_PART_APDU_NAME = "GET_RECOVERY_DATA_PART";
  public final static String IS_RECOVERY_DATA_SET_APDU_NAME = "IS_RECOVERY_DATA_SET";
  public final static String RESET_RECOVERY_DATA_APDU_NAME = "RESET_RECOVERY_DATA";
  public final static String ADD_RECOVERY_DATA_PART_APDU_NAME = "ADD_RECOVERY_DATA_PART";

  static {
    addCommand(INS_FINISH_PERS, FINISH_PERS_APDU_NAME);
    addCommand(INS_SET_ENCRYPTED_PASSWORD_FOR_CARD_AUTHENTICATION, SET_ENCRYPTED_PASSWORD_FOR_CARD_AUTHENTICATION_APDU_NAME);
    addCommand(INS_SET_ENCRYPTED_COMMON_SECRET, SET_ENCRYPTED_COMMON_SECRET_APDU_NAME);
    addCommand(INS_VERIFY_PASSWORD, VERIFY_PASSWORD_APDU_NAME);
    addCommand(INS_GET_HASH_OF_ENCRYPTED_PASSWORD, GET_HASH_OF_ENCRYPTED_PASSWORD_APDU_NAME);
    addCommand(INS_GET_HASH_OF_ENCRYPTED_COMMON_SECRET, GET_HASH_OF_ENCRYPTED_COMMON_SECRET_APDU_NAME);
    addCommand(INS_VERIFY_PIN, VERIFY_PIN_APDU_NAME);
    addCommand(INS_GET_PUBLIC_KEY, GET_PUBLIC_KEY_APDU_NAME);
    addCommand(INS_GET_PUBLIC_KEY_WITH_DEFAULT_HD_PATH, GET_PUBLIC_KEY_WITH_DEFAULT_HD_PATH_APDU_NAME);
    addCommand(INS_SIGN_SHORT_MESSAGE, SIGN_SHORT_MESSAGE_APDU_NAME);
    addCommand(INS_SIGN_SHORT_MESSAGE_WITH_DEFAULT_PATH, SIGN_SHORT_MESSAGE_WITH_DEFAULT_PATH_APDU_NAME);
    addCommand(INS_GET_APP_INFO, GET_APP_INFO_APDU_NAME);
    addCommand(INS_GET_KEY_INDEX_IN_STORAGE_AND_LEN, GET_KEY_INDEX_IN_STORAGE_AND_LEN_APDU_NAME);
    addCommand(INS_GET_KEY_CHUNK, GET_KEY_CHUNK_APDU_NAME);
    addCommand(INS_CHECK_AVAILABLE_VOL_FOR_NEW_KEY, CHECK_AVAILABLE_VOL_FOR_NEW_KEY_APDU_NAME);
    addCommand(INS_ADD_KEY_CHUNK, ADD_KEY_CHUNK_APDU_NAME);
    addCommand(INS_INITIATE_CHANGE_OF_KEY, INITIATE_CHANGE_OF_KEY_APDU_NAME );
    addCommand(INS_CHANGE_KEY_CHUNK, CHANGE_KEY_CHUNK_APDU_NAME);
    addCommand(INS_DELETE_KEY_CHUNK, DELETE_KEY_CHUNK_APDU_NAME);
    addCommand(INS_GET_DELETE_KEY_CHUNK_NUM_OF_PACKETS, GET_DELETE_KEY_CHUNK_NUM_OF_PACKETS_APDU_NAME);
    addCommand(INS_GET_DELETE_KEY_RECORD_NUM_OF_PACKETS, GET_DELETE_KEY_RECORD_NUM_OF_PACKETS_APDU_NAME);
    addCommand(INS_INITIATE_DELETE_KEY, INITIATE_DELETE_KEY_APDU_NAME);
    addCommand(INS_DELETE_KEY_RECORD, DELETE_KEY_RECORD_APDU_NAME);
    addCommand(INS_GET_NUMBER_OF_KEYS, GET_NUMBER_OF_KEYS_APDU_NAME);
    addCommand(INS_GET_FREE_STORAGE_SIZE, GET_FREE_STORAGE_APDU_NAME);
    addCommand(INS_GET_OCCUPIED_STORAGE_SIZE, GET_OCCUPIED_STORAGE_APDU_NAME);
    addCommand(INS_GET_HMAC, GET_HMAC_APDU_NAME);
    addCommand(INS_RESET_KEYCHAIN, RESET_KEYCHAIN_APDU_NAME);
    addCommand(INS_GET_SAULT, GET_SAULT_APDU_NAME);
    addCommand(INS_CHECK_KEY_HMAC_CONSISTENCY, CHECK_KEY_HMAC_CONSISTENCY_APDU_NAME);
    addCommand(INS_GET_SERIAL_NUMBER, GET_SERIAL_NUMBER_APDU_NAME);
    addCommand(INS_SET_SERIAL_NUMBER, SET_SERIAL_NUMBER_APDU_NAME);
    addCommand(INS_GET_RECOVERY_DATA_HASH, GET_RECOVERY_DATA_HASH_APDU_NAME);
    addCommand(INS_GET_RECOVERY_DATA_LEN, GET_RECOVERY_DATA_LEN_APDU_NAME);
    addCommand(INS_GET_RECOVERY_DATA_PART, GET_GET_RECOVERY_DATA_PART_APDU_NAME);
    addCommand(INS_RESET_RECOVERY_DATA, RESET_RECOVERY_DATA_APDU_NAME);
    addCommand(INS_IS_RECOVERY_DATA_SET, IS_RECOVERY_DATA_SET_APDU_NAME);
    addCommand(INS_ADD_RECOVERY_DATA_PART, ADD_RECOVERY_DATA_PART_APDU_NAME);
  }

  public static String getTonWalletAppletApduCommandName(byte ins) {
    return  tonWalletAppletCommandsNames.get(ins);
  }

  private static void addCommand(byte ins, String name) {
    tonWalletAppletCommandsNames.put(ins, name.trim());
  }

 /*public static CAPDU setSerialNumberAPDU(byte[] data) {
    return new CAPDU(WALLET_APPLET_CLA, INS_SET_SERIAL_NUMBER,
      (byte) 0x00, (byte) 0x00,
      data);
  }*/

  /**
   VERIFY_PASSWORD

   CLA: 0xB0
   INS: 0x92
   P1: 0x00
   P2: 0x00
   LC: 0x90
   Data: 128 bytes of unencrypted activation password | 16 bytes of IV for AES128 CBC

   This function is available only in WAITE_AUTHENTICATION_MODE state of applet.
   It makes activation password verification and in the case of success it changes the state of applet: WAITE_AUTHENTICATION_MODE -> PERSONALIZED.
   After 20 unsuccessful attempts to verify password this functions changes the state of applet: WAITE_AUTHENTICATION_MODE -> BLOCKED_MODE. In this case applet is blocked.
   This is irreversible and card becomes useless.
   */
  public static CAPDU getVerifyPasswordAPDU(byte[] passwordBytes, byte[] initialVector)  {
    if (passwordBytes == null || passwordBytes.length != PASSWORD_SIZE)
      throw new IllegalArgumentException(ERROR_MSG_ACTIVATION_PASSWORD_BYTES_SIZE_INCORRECT);
    if (initialVector == null || initialVector.length != IV_SIZE)
      throw new IllegalArgumentException(ERROR_MSG_INITIAL_VECTOR_BYTES_SIZE_INCORRECT);
    byte[] data = BYTE_ARRAY_HELPER.bConcat(passwordBytes, initialVector);
    return new CAPDU(WALLET_APPLET_CLA, INS_VERIFY_PASSWORD, P1, P2, data);
  }

  /**
   VERIFY_PIN

   CLA: 0xB0
   INS: 0xA2
   P1: 0x00
   P2: 0x00
   LC: 0x44
   Data: 4 bytes of PIN | 32 bytes of sault | 32 bytes of mac

   This function verifies ascii encoded PIN bytes sent in the data field. After 10 fails of PIN verification internal seed will be blocked.
   Keys produced from it will not be available for signing transactions. And then RESET WALLET and GENERATE SEED APDU commands of CoinManager must be called.
   It will regenerate seed and reset PIN.The default card PIN is 5555 now. This version of applet was written for NFC card.
   It uses special mode PIN_MODE_FROM_API for entering PIN. PIN  bytes are  given to applet as plain text.
   If PIN code = 5555 plain PIN bytes array must look like {0x35, 0x35 0x35, 0x35}.
   Available in applet states PERSONALIZED and DELETE_KEY_FROM_KEYCHAIN_MODE.
   Precondition:  GET_SAULT should be called before to get new sault from card.
   */
  public static CAPDU getVerifyPinAPDU(byte[] pinBytes, byte[] sault) throws Exception {
    if (pinBytes == null || pinBytes.length != PIN_SIZE)
      throw new IllegalArgumentException(ERROR_MSG_PIN_BYTES_SIZE_INCORRECT);
    checkSault(sault);
    byte[] data = prepareApduData(BYTE_ARRAY_HELPER.bConcat(pinBytes, sault));
    return new CAPDU(WALLET_APPLET_CLA, INS_VERIFY_PIN, P1, P2, data);
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

   This function signs the message from apdu buffer by ED25519 for default bip44 HD path
   m/44'/396'/0'/0'/0'.
   Available in applet states PERSONALIZED and DELETE_KEY_FROM_KEYCHAIN_MODE.
   Precondition:  1) GET_SAULT should be called before to get new sault from card. 2) VERIFY_PIN should be called before.
   */
  public static CAPDU getSignShortMessageWithDefaultPathAPDU(byte[] dataForSigning, byte[] sault) throws Exception {
    if (dataForSigning == null || dataForSigning.length == 0 || dataForSigning.length > DATA_FOR_SIGNING_MAX_SIZE)
      throw new IllegalArgumentException(ERROR_MSG_DATA_BYTES_SIZE_INCORRECT);
    checkSault(sault);
    byte[] data = prepareApduData(BYTE_ARRAY_HELPER.bConcat(new byte[]{0x00, (byte) (dataForSigning.length)}, dataForSigning, sault));
    return new CAPDU(WALLET_APPLET_CLA, INS_SIGN_SHORT_MESSAGE_WITH_DEFAULT_PATH, P1, P2, data, SIG_LEN);
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

   This function signs the message from apdu buffer by ED25519 for default bip44 HD path
   m/44'/396'/0'/0'/ind'. There is 0 <= ind <= 2^31 - 1. ind must be represented as decimal number and each decimal place should be transformed into Ascii encoding.
   Example: ind = 171 ⇒ {0x31, 0x37, 0x31}
   Available in applet states PERSONALIZED and DELETE_KEY_FROM_KEYCHAIN_MODE.
   Precondition:  1)* GET_SAULT should be called before to get new sault from card. 2) VERIFY_PIN should be called before.
   */
  public static CAPDU getSignShortMessageAPDU(byte[] dataForSigning, byte[] hdIndex, byte[] sault) throws Exception {
    if (dataForSigning == null || dataForSigning.length == 0 || dataForSigning.length > DATA_FOR_SIGNING_MAX_SIZE_FOR_CASE_WITH_PATH)
      throw new IllegalArgumentException(ERROR_MSG_DATA_WITH_HD_PATH_BYTES_SIZE_INCORRECT);
    checkSault(sault);
    checkHdIndex(hdIndex);
    byte[] data = prepareApduData(BYTE_ARRAY_HELPER.bConcat(new byte[]{0x00, (byte) (dataForSigning.length)}, dataForSigning, new byte[]{(byte) hdIndex.length}, hdIndex, sault));
    return new CAPDU(WALLET_APPLET_CLA, INS_SIGN_SHORT_MESSAGE, P1, P2, data, SIG_LEN);
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

   This function retrieves ED25519 public key from CoinManager for bip44 HD path
   m/44'/396'/0'/0'/ind'. There is 0 <= ind <= 2^31 - 1. ind must be represented as decimal number and each decimal place should be transformed into Ascii encoding.
   Example: ind = 171 ⇒ {0x31, 0x37, 0x31}
   Available in applet states PERSONALIZED and DELETE_KEY_FROM_KEYCHAIN_MODE.
   */
  public static CAPDU getPublicKeyAPDU(byte[] ind) {
    checkHdIndex(ind);
    return new CAPDU(WALLET_APPLET_CLA, INS_GET_PUBLIC_KEY, P1, P2,
      ind,
      PUBLIC_KEY_LEN);
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
   *
   * This function receives encrypted byte array containing data for recovery service. Now it is multisignature wallet address, surf public key(32 bytes),
   * card common secret (32 bytes) and authentication password (128 bytes) (and this stuff is wrapped in json). Just in case in applet for now we reserved 2048 bytes for string recovery data.
   * It is probably bigger volume than required just now.
   * As usually the APDU command can be used to put no more than 256 bytes into applet at once. It is just a limitation of APDU protocol. 256 bytes is a max byte array length that we can send(request) into the card.
   * So if recover data will extended then ADD_RECOVERY_DATA_PART should be called multiple times sequentially.
   * Last call of ADD_RECOVERY_DATA_PART must contain SHA256 hash of all recovery data. The card inside will compute hash of received data and it will compare te computed hash and hash received from the host.
   * If they are identical then internal flag isRecoveryDataSet is set to true. Otherwise the card resets all internal buffers, sets  isRecoveryDataSet = false and thrwos exception.
   * Available in applet states PERSONALIZED and DELETE_KEY_FROM_KEYCHAIN_MODE.
   */
  public static CAPDU getAddRecoveryDataPartAPDU(byte p1, byte[] data) {
    if (p1 < 0 || p1 > 2) throw new IllegalArgumentException(ERROR_MSG_APDU_P1_INCORRECT);
    if (p1 <= 1 && (data == null || data.length == 0 || data.length > DATA_RECOVERY_PORTION_MAX_SIZE))
      throw new IllegalArgumentException(ERROR_MSG_RECOVER_DATA_PORTION_SIZE_INCORRECT);
    if (p1 == 2 && (data == null || data.length != HMAC_SHA_SIG_SIZE))
      throw new IllegalArgumentException(ERROR_MSG_RECOVERY_DATA_MAC_BYTES_SIZE_INCORRECT);
    return new CAPDU(WALLET_APPLET_CLA, INS_ADD_RECOVERY_DATA_PART,
      p1, (byte) 0x00,
      data);
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
   *
   * This function returns encrypted binary blob saved during registration in Recovery service. This APDU command shouldn't be protected with HMAC or PIN.
   * If length of recovery data > 256 bytes then this apdu command must be called multiple times.
   * Since as usually the APDU command can be used to transmit no more than 256 bytes from applet into host at once. It is just a limitation of APDU protocol.
   * 256 bytes is a max byte array length that we can request from the card.
   * Available in applet states PERSONALIZED and DELETE_KEY_FROM_KEYCHAIN_MODE.
   */
  public static CAPDU getGetRecoveryDataPartAPDU(byte[] startPositionBytes, byte le) {
    if (startPositionBytes == null || startPositionBytes.length != 2)
      throw new IllegalArgumentException(ERROR_MSG_START_POSITION_BYTES_SIZE_INCORRECT);
    return new CAPDU(WALLET_APPLET_CLA, INS_GET_RECOVERY_DATA_PART,
      (byte) 0x00, (byte) 0x00,
      startPositionBytes,
      le);
  }

  /**
   RESET_KEYCHAIN

   CLA: 0xB0
   INS: 0xBC
   P1: 0x00
   P2: 0x00
   LC: 0x40
   Data: sault (32 bytes) | mac (32 bytes)

   Clears all internal buffers and counters. So after it keychain is clear. In the end it always switches applet state into APP_PERSONALIZED.
   Precondition:  GET_SAULT should be called before to get new sault from card.
   Available in applet states PERSONALIZED and DELETE_KEY_FROM_KEYCHAIN_MODE.
   */
  public static CAPDU getResetKeyChainAPDU(byte[] sault) throws Exception {
    byte[] data = prepareSaultBasedApduData(sault);
    return new CAPDU(WALLET_APPLET_CLA, INS_RESET_KEYCHAIN, P1, P2, data);
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

   Outputs the number of keys that are stored by KeyChain at the present moment.
   Precondition:  GET_SAULT should be called before to get new sault from card.
   Available in applet states PERSONALIZED and DELETE_KEY_FROM_KEYCHAIN_MODE.
   */
  public static CAPDU getNumberOfKeysAPDU(byte[] sault) throws Exception {
    byte[] data = prepareSaultBasedApduData(sault);
    return new CAPDU(WALLET_APPLET_CLA, INS_GET_NUMBER_OF_KEYS, P1, P2,
      data, GET_NUMBER_OF_KEYS_LE);
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

   Outputs the volume of occupied size (in bytes) in KeyChain.
   Precondition:  GET_SAULT should be called before to get new sault from card.
   Available in applet states PERSONALIZED and DELETE_KEY_FROM_KEYCHAIN_MODE.
   */
  public static CAPDU getGetOccupiedSizeAPDU(byte[] sault) throws Exception {
    byte[] data = prepareSaultBasedApduData(sault);
    return new CAPDU(
      WALLET_APPLET_CLA,
      INS_GET_OCCUPIED_STORAGE_SIZE, P1, P2,
      data, GET_OCCUPIED_SIZE_LE);
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

   Outputs the volume of free size in keyStore.
   Precondition:  GET_SAULT should be called before to get new sault from card.
   Available in applet states PERSONALIZED and DELETE_KEY_FROM_KEYCHAIN_MODE.
   */
  public static CAPDU getGetFreeSizeAPDU(byte[] sault) throws Exception {
    byte[] data = prepareSaultBasedApduData(sault);
    return new CAPDU(
      WALLET_APPLET_CLA,
      INS_GET_FREE_STORAGE_SIZE, P1, P2,
      data, GET_FREE_SIZE_LE);
  }

  /***
   CHECK_AVAILABLE_VOL_FOR_NEW_KEY

   CLA: 0xB0
   INS: 0xB3
   P1: 0x00
   P2: 0x00
   LC: 0x42
   Data: length of new key (2 bytes) | sault (32 bytes) | mac (32 bytes)

   Gets from host the size of new key that user wants to add. It checks free space. And if it's enough it saves this value into internal applet variable.
   Otherwise it will throw an exception. This command always should be called before adding new key.
   Precondition:  GET_SAULT should be called before to get new sault from card.
   Available in applet state PERSONALIZED.
   */
  public static CAPDU getCheckAvailableVolForNewKeyAPDU(short keySize, byte[] sault) throws Exception {
    if (keySize <= 0 || keySize > MAX_KEY_SIZE_IN_KEYCHAIN)
      throw new IllegalArgumentException(ERROR_MSG_KEY_SIZE_INCORRECT);
    checkSault(sault);
    byte[] data = prepareApduData(BYTE_ARRAY_HELPER.bConcat(new byte[]{(byte) (keySize >> 8), (byte) (keySize)}, sault));
    return new CAPDU(
      WALLET_APPLET_CLA,
      INS_CHECK_AVAILABLE_VOL_FOR_NEW_KEY, P1, P2,
      data);
  }

  /***
   CHECK_KEY_HMAC_CONSISTENCY

   CLA: 0xB0
   INS: 0xB0
   P1: 0x00
   P2: 0x00
   LC: 0x60
   Data: keyMac (32 bytes) | sault (32 bytes) | mac (32 bytes)

   Gets mac of key and checks that *mac(key bytes in keyStore)* coincides with this mac.
   Precondition:  GET_SAULT should be called before to get new sault from card.
   Available in applet states PERSONALIZED and DELETE_KEY_FROM_KEYCHAIN_MODE.
   */
  public static CAPDU getCheckKeyHmacConsistencyAPDU(byte[] keyMac, byte[] sault) throws Exception {
    checkHmac(keyMac);
    checkSault(sault);
    byte[] data = prepareApduData(BYTE_ARRAY_HELPER.bConcat(keyMac, sault));
    return new CAPDU(WALLET_APPLET_CLA, INS_CHECK_KEY_HMAC_CONSISTENCY, P1, P2, data);
  }

  /***
   INITIATE_CHANGE_OF_KEY

   CLA: 0xB0
   INS: 0xB5
   P1: 0x00
   P2: 0x00
   LC: 0x42
   Data: index of key (2 bytes) | sault (32 bytes) | mac (32 bytes)

   Gets from the host  the real index of key to be changed and stores this index into internal applet variable.
   Precondition:  GET_SAULT should be called before to get new sault from card.
   Available in applet state PERSONALIZED.
   */
  public static CAPDU getInitiateChangeOfKeyAPDU(byte[] keyIndex, byte[] sault) throws Exception {
    checkKeyChainKeyIndex(keyIndex);
    checkSault(sault);
    byte[] data = prepareApduData(BYTE_ARRAY_HELPER.bConcat(new byte[]{keyIndex[0], keyIndex[1]}, sault));
    return new CAPDU(
      WALLET_APPLET_CLA,
      INS_INITIATE_CHANGE_OF_KEY, P1, P2,
      data);
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

   Gets hmac of the key from host. It outputs its real index in keyOffsets array and key length.
   Precondition:  GET_SAULT should be called before to get new sault from card.
   Available in applet states PERSONALIZED and DELETE_KEY_FROM_KEYCHAIN_MODE.
   */
  public static CAPDU getGetIndexAndLenOfKeyInKeyChainAPDU(byte[] keyMac, byte[] sault) throws Exception {
    checkHmac(keyMac);
    checkSault(sault);
    byte[] data = prepareApduData(BYTE_ARRAY_HELPER.bConcat(keyMac, sault));
    return new CAPDU(
      WALLET_APPLET_CLA,
      INS_GET_KEY_INDEX_IN_STORAGE_AND_LEN, P1, P2,
      data,
      GET_KEY_INDEX_IN_STORAGE_AND_LEN_LE);
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

   Gets from host the real index of key in keyOffsets array and stores this index into internal applet variable.
   Outputs length of key to be deleted. In the case of success in the end it changes the state of applet on DELETE_KEY_FROM_KEYCHAIN_MODE.
   Precondition:  1) GET_SAULT should be called before to get new sault from card. 2) call GET_KEY_INDEX_IN_STORAGE_AND_LEN to get key index.
   Available in applet state PERSONALIZED.
   */
  public static CAPDU getInitiateDeleteOfKeyAPDU(byte[] keyIndex, byte[] sault) throws Exception {
    checkKeyChainKeyIndex(keyIndex);
    checkSault(sault);
    byte[] data = prepareApduData(BYTE_ARRAY_HELPER.bConcat(new byte[]{keyIndex[0], keyIndex[1]}, sault));
    return new CAPDU(
      WALLET_APPLET_CLA,
      INS_INITIATE_DELETE_KEY, P1, P2,
      data,
      INITIATE_DELETE_KEY_LE);
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
   *
   * Outputs total number of iterations that is necessary to remove key chunk from keychain.
   * Precondition:  GET_SAULT should be called before to get new sault from card.
   * Available in applet state DELETE_KEY_FROM_KEYCHAIN_MODE.
   */
  public static CAPDU getDeleteKeyChunkNumOfPacketsAPDU(byte[] sault) throws Exception {
    byte[] data = prepareSaultBasedApduData(sault);
    return new CAPDU(
      WALLET_APPLET_CLA,
      INS_GET_DELETE_KEY_CHUNK_NUM_OF_PACKETS, P1, P2,
      data, GET_DELETE_KEY_CHUNK_NUM_OF_PACKETS_LE);
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
   *
   * Outputs total number of iterations that is necessary to remove key record from keychain.
   * Precondition:  GET_SAULT should be called before to get new sault from card.
   * Available in applet state DELETE_KEY_FROM_KEYCHAIN_MODE.
   */
  public static CAPDU getDeleteKeyRecordNumOfPacketsAPDU(byte[] sault) throws Exception {
    byte[] data = prepareSaultBasedApduData(sault);
    return new CAPDU(
      WALLET_APPLET_CLA,
      INS_GET_DELETE_KEY_RECORD_NUM_OF_PACKETS, P1, P2,
      data, GET_DELETE_KEY_RECORD_NUM_OF_PACKETS_LE);
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

   Deletes the portion of key bytes in internal buffer. Max size of portion = 128 bytes.
   The command should be called (totalOccupied size - offsetOfNextKey) / 128  times + 1 times for tail having length  (totalOccupied size - offsetOfNextKey) % 128.
   The response contains status byte. If status == 0 then we should continue calling DELETE_LEY_CHUNK. Else if status == 1 then process is finished.
   Such splitting into multiple calls was implemented only because we wanted to use javacard transaction for deleting to control data integrity in keychain.
   But our device has small internal buffer for transactions and can not handle big transactions.
   Precondition: 1) GET_SAULT should be called before to get new sault from card.  2) INITIATE_DELETE_KEY should be called before to set the index of key to be deleted.
   Available in applet state DELETE_KEY_FROM_KEYCHAIN_MODE.
   */
  public static CAPDU getDeleteKeyChunkAPDU(byte[] sault) throws Exception {
    byte[] data = prepareSaultBasedApduData(sault);
    return new CAPDU(
      WALLET_APPLET_CLA,
      INS_DELETE_KEY_CHUNK, P1, P2,
      data,
      DELETE_KEY_CHUNK_LE);
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

   Processes the portion of elements in internal buffer storing record about keys. Max size of portion = 12.
   The command should be called (numberOfStoredKeys - indOfKeyToDelete - 1) / 12  times + 1 times for tail having length  (numberOfStoredKeys - indOfKeyToDelete - 1) % 12.
   The response contains status byte. If status == 0 then we should continue calling DELETE_LEY_RECORD. Else if status == 1 then process is finished.
   In the end when status is set to 1 and delete operation is finished applet state is changed on APP_PERSONALIZED.
   And again one may conduct new add, change or delete operation in keychain,
   Such splitting into multiple calls was implemented only because we wanted to use javacard transaction for deleting to control data integrity in keychain.
   But our device has small internal buffer for transactions and can not handle big transactions.
   Precondition: 1) GET_SAULT should be called before to get new sault from card.  2) DELETE_KEY _CHUNK should be called before to clear keyStore array
   (it should be called until it will return response byte = 1).
   Available in applet state DELETE_KEY_FROM_KEYCHAIN_MODE.
   */
  public static CAPDU getDeleteKeyRecordAPDU(byte[] sault) throws Exception {
    byte[] data = prepareSaultBasedApduData(sault);
    return new CAPDU(
      WALLET_APPLET_CLA,
      INS_DELETE_KEY_RECORD, P1, P2,
      data,
      DELETE_KEY_RECORD_LE);
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

   Gets the index of key (≥ 0 and  < numberOfStoredKeys) and outputs its hmac.
   Precondition:  GET_SAULT should be called before to get new sault from card.
   Available in applet states PERSONALIZED and DELETE_KEY_FROM_KEYCHAIN_MODE.
   */
  public static CAPDU getGetHmacAPDU(byte[] keyIndex, byte[] sault) throws Exception {
    checkKeyChainKeyIndex(keyIndex);
    checkSault(sault);
    byte[] data = prepareApduData(BYTE_ARRAY_HELPER.bConcat(keyIndex, sault));
    return new CAPDU(
      WALLET_APPLET_CLA,
      INS_GET_HMAC, P1, P2,
      data,
      GET_HMAC_LE);
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

   Gets from host  the real index of key in keyOffsets array and relative start position from which key bytes should be read (at first time startPos = 0).
   Applet calculates real offset inside keyStore based on its data and startPos and outputs key chunk. Max size of key chunk is 255 bytes.
   So if key size > 255 bytes GET_KEY_CHUNK will be called multiple times. After getting all data host should verify that hmac of received data is correct and we did not lose any packet.
   Precondition:  1) GET_SAULT should be called before to get new sault from card. 2)  call GET_KEY_INDEX_IN_STORAGE_AND_LEN.
   Available in applet states PERSONALIZED and DELETE_KEY_FROM_KEYCHAIN_MODE.
   */
  public static CAPDU getGetKeyChunkAPDU(byte[] keyIndex, short startPos, byte[] sault, byte le) throws Exception {
    checkKeyChainKeyIndex(keyIndex);
    checkSault(sault);
    byte[] data = prepareApduData(BYTE_ARRAY_HELPER.bConcat(keyIndex, new byte[]{(byte) (startPos >> 8), (byte) (startPos)}, sault));
    return new CAPDU(
      WALLET_APPLET_CLA,
      INS_GET_KEY_CHUNK, P1, P2,
      data,
      le);
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

   Gets keychunk from the host and adds it into the end of keyStore array. Max size of key chunk is 128 bytes.
   So if total key size > 128 bytes ADD_KEY_CHUNK will be called multiple times. After all key data is transmitted we call ADD_KEY_CHUNK once again, its input data is Hmac of the key.
   Applet checks that hmac of received sequence equals to this hmac got from host. So we did not lose any packet and key is not replaced by adversary.
   And also  ADD_KEY_CHUNK  checks that size of new key equals to size set previously by command CHECK_AVAILABLE_VOL_FOR_NEW_KEY.
   If verification is ok all ket data is added into keyMacs, keyOffsets and keyLebs buffers, keys counter is incremented  and since this moment the key is registered and can be requested from the card.
   Precondition:  1) GET_SAULT should be called before to get new sault from card. 2) call CHECK_AVAILABLE_VOL_FOR_NEW_KEY.
   Then ADD_KEY_CHUNK is called multiple times.
   Available in applet states PERSONALIZED.
   */
  public static CAPDU getAddKeyChunkAPDU(byte p1, byte[] keyChunkOrMacBytes, byte[] sault) throws Exception {
    return getSendKeyChunkAPDU(INS_ADD_KEY_CHUNK, p1, keyChunkOrMacBytes, sault);
  }

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

   Gets keychunk bytes from the host and puts it into keyStore array in place of old key bytes. Max size of key chunk is 128 bytes.
   So if total key size > 128 bytes CHANGE_KEY_CHUNK will be called multiple times.  We control that lengthes of old key and new key are the same.
   After all new key data is transmitted we call CHANGE_KEY_CHUNK once again, its input data is Hmac of new key and it is verified by applet.
   If it is ok then corresponding data about key  is changed in keyOffsets, keyMacs and keyLens buffers.
   Precondition:  1) GET_SAULT should be called before to get new sault from card. 2)call  GET_KEY_INDEX_IN_STORAGE_AND_LEN , INIATE_CHANGE_OF_KEY.
   Then call CHANGE_KEY_CHUNK multiple times.
   Available in applet states PERSONALIZED.
   */
  public static CAPDU getChangeKeyChunkAPDU(byte p1, byte[] keyChunkOrMacBytes, byte[] sault) throws Exception {
    return getSendKeyChunkAPDU(INS_CHANGE_KEY_CHUNK, p1, keyChunkOrMacBytes, sault);
  }

  public static CAPDU getSendKeyChunkAPDU(byte ins, byte p1, byte[] keyChunkOrMacBytes, byte[] sault) throws Exception {
    checkSault(sault);
    if (p1 < 0 || p1 > 2)
      throw new IllegalArgumentException(ERROR_MSG_APDU_P1_INCORRECT);
    if (p1 < 2 && (keyChunkOrMacBytes == null || keyChunkOrMacBytes.length == 0 || keyChunkOrMacBytes.length > DATA_PORTION_MAX_SIZE))
      throw new IllegalArgumentException(ERROR_MSG_KEY_CHUNK_BYTES_SIZE_INCORRECT);
    if (p1 == 2 && (keyChunkOrMacBytes == null || keyChunkOrMacBytes.length != HMAC_SHA_SIG_SIZE))
      throw new IllegalArgumentException(ERROR_MSG_KEY_MAC_BYTES_SIZE_INCORRECT);
    byte[] data = (p1 == 2) ? prepareApduData(BYTE_ARRAY_HELPER.bConcat(keyChunkOrMacBytes, sault))
      : prepareApduData(BYTE_ARRAY_HELPER.bConcat(new byte[]{(byte) keyChunkOrMacBytes.length}, keyChunkOrMacBytes, sault));
    return (p1 == 2) ? new CAPDU(WALLET_APPLET_CLA, ins, p1, P2, data, SEND_CHUNK_LE) :
      new CAPDU(WALLET_APPLET_CLA, ins, p1, P2, data);
  }

  public static byte[] prepareSaultBasedApduData(byte[] sault) throws Exception {
    checkSault(sault);
    return BYTE_ARRAY_HELPER.bConcat(sault, HMAC_HELPER.computeMac(sault));
  }

  public static byte[] prepareApduData(byte[] dataChunk) throws Exception {
    return BYTE_ARRAY_HELPER.bConcat(dataChunk, HMAC_HELPER.computeMac(dataChunk));
  }

  private static void checkHmac(byte[] hmac) {
    if (hmac == null || hmac.length != HMAC_SHA_SIG_SIZE)
      throw new IllegalArgumentException(ERROR_MSG_KEY_MAC_BYTES_SIZE_INCORRECT);
  }

  private static void checkSault(byte[] sault) {
    if (sault == null || sault.length != SAULT_LENGTH)
      throw new IllegalArgumentException(ERROR_MSG_SAULT_BYTES_SIZE_INCORRECT);
  }

  private static void checkHdIndex(byte[] ind) {
    if (ind == null || ind.length == 0 || ind.length > MAX_HD_INDEX_SIZE)
      throw new IllegalArgumentException(ERROR_MSG_HD_INDEX_BYTES_SIZE_INCORRECT);
  }

  private static void checkKeyChainKeyIndex(byte[] ind) {
    if (ind == null || ind.length != KEYCHAIN_KEY_INDEX_LEN)
      throw new IllegalArgumentException(ERROR_MSG_KEY_INDEX_BYTES_SIZE_INCORRECT);
  }
}
