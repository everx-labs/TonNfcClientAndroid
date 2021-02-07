package com.tonnfccard.smartcard.apdu;

import com.tonnfccard.smartcard.cryptoUtils.HmacHelper;
import com.tonnfccard.smartcard.wrappers.CAPDU;
import com.tonnfccard.utils.ByteArrayHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.tonnfccard.api.utils.ResponsesConstants.ERROR_MSG_ACTIVATION_PASSWORD_BYTES_SIZE_INCORRECT;
import static com.tonnfccard.api.utils.ResponsesConstants.ERROR_MSG_APDU_P1_INCORRECT;
import static com.tonnfccard.api.utils.ResponsesConstants.ERROR_MSG_DATA_BYTES_SIZE_INCORRECT;
import static com.tonnfccard.api.utils.ResponsesConstants.ERROR_MSG_DATA_WITH_HD_PATH_BYTES_SIZE_INCORRECT;
import static com.tonnfccard.api.utils.ResponsesConstants.ERROR_MSG_HD_INDEX_BYTES_SIZE_INCORRECT;
import static com.tonnfccard.api.utils.ResponsesConstants.ERROR_MSG_INITIAL_VECTOR_BYTES_SIZE_INCORRECT;
import static com.tonnfccard.api.utils.ResponsesConstants.ERROR_MSG_KEY_CHUNK_BYTES_SIZE_INCORRECT;
import static com.tonnfccard.api.utils.ResponsesConstants.ERROR_MSG_KEY_INDEX_BYTES_SIZE_INCORRECT;
import static com.tonnfccard.api.utils.ResponsesConstants.ERROR_MSG_KEY_MAC_BYTES_SIZE_INCORRECT;
import static com.tonnfccard.api.utils.ResponsesConstants.ERROR_MSG_PIN_BYTES_SIZE_INCORRECT;
import static com.tonnfccard.api.utils.ResponsesConstants.ERROR_MSG_SAULT_BYTES_SIZE_INCORRECT;
import static com.tonnfccard.smartcard.TonWalletAppletConstants.DATA_FOR_SIGNING_MAX_SIZE;
import static com.tonnfccard.smartcard.TonWalletAppletConstants.DATA_FOR_SIGNING_MAX_SIZE_FOR_CASE_WITH_PATH;
import static com.tonnfccard.smartcard.TonWalletAppletConstants.DATA_PORTION_MAX_SIZE;
import static com.tonnfccard.smartcard.TonWalletAppletConstants.HMAC_SHA_SIG_SIZE;
import static com.tonnfccard.smartcard.TonWalletAppletConstants.IV_SIZE;
import static com.tonnfccard.smartcard.TonWalletAppletConstants.KEYCHAIN_KEY_INDEX_LEN;
import static com.tonnfccard.smartcard.TonWalletAppletConstants.MAX_IND_SIZE;
import static com.tonnfccard.smartcard.TonWalletAppletConstants.PASSWORD_SIZE;
import static com.tonnfccard.smartcard.TonWalletAppletConstants.PIN_SIZE;
import static com.tonnfccard.smartcard.TonWalletAppletConstants.PUBLIC_KEY_LEN;
import static com.tonnfccard.smartcard.TonWalletAppletConstants.SAULT_LENGTH;
import static com.tonnfccard.smartcard.TonWalletAppletConstants.SHA_HASH_SIZE;
import static com.tonnfccard.smartcard.TonWalletAppletConstants.SIG_LEN;
import static com.tonnfccard.smartcard.apdu.Constants.LE;
import static com.tonnfccard.smartcard.apdu.Constants.SELECT_CLA;
import static com.tonnfccard.smartcard.apdu.Constants.SELECT_INS;
import static com.tonnfccard.smartcard.apdu.Constants.SELECT_P1;
import static com.tonnfccard.smartcard.apdu.Constants.SELECT_P2;

public class TonWalletAppletApduCommands {
  private static final ByteArrayHelper BYTE_ARRAY_HELPER = ByteArrayHelper.getInstance();
  private static HmacHelper HMAC_HELPER = HmacHelper.getInstance();

  public static void setHmacHelper(HmacHelper hmacHelper) {
    HMAC_HELPER = hmacHelper;
  }

  public static final byte[] TON_WALLET_APPLET_AID = {0x31, 0x31, 0x32, 0x32, 0x33, 0x33, 0x34, 0x34, 0x35, 0x35, 0x36, 0x36}; //"31313232333334343535363600";

  // code of CLA byte in the command APDU header
  public final static byte WALLET_APPLET_CLA = (byte) 0xB0;

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
  public static final byte GET_SERIAL_NUMBER_LE =  (byte)0x18;

  //00 A4 04 00 0C 31 31 32 32 33 33 34 34 35 35 36 36 00
  public final static CAPDU SELECT_TON_WALLET_APPLET_APDU = new CAPDU(SELECT_CLA, SELECT_INS, SELECT_P1, SELECT_P2, TON_WALLET_APPLET_AID, LE);
  public final static CAPDU GET_APP_INFO_APDU = new CAPDU(WALLET_APPLET_CLA, INS_GET_APP_INFO, 0x00, 0x00, GET_APP_INFO_LE);
  public final static CAPDU GET_HASH_OF_ENCRYPTED_PASSWORD_APDU = new CAPDU(WALLET_APPLET_CLA, INS_GET_HASH_OF_ENCRYPTED_PASSWORD, 0x00, 0x00, SHA_HASH_SIZE);
  public final static CAPDU GET_HASH_OF_ENCRYPTED_COMMON_SECRET_APDU = new CAPDU(WALLET_APPLET_CLA, INS_GET_HASH_OF_ENCRYPTED_COMMON_SECRET, 0x00, 0x00, SHA_HASH_SIZE);
  public final static CAPDU GET_PUB_KEY_WITH_DEFAULT_PATH_APDU = new CAPDU(WALLET_APPLET_CLA, INS_GET_PUBLIC_KEY_WITH_DEFAULT_HD_PATH, 0x00, 0x0, PUBLIC_KEY_LEN);
  public final static CAPDU GET_SAULT_APDU = new CAPDU(WALLET_APPLET_CLA, INS_GET_SAULT, 0x00, 0x0, SAULT_LENGTH);
  public final static CAPDU GET_RECOVERY_DATA_HASH_APDU = new CAPDU(WALLET_APPLET_CLA, INS_GET_RECOVERY_DATA_HASH, 0x00, 0x00, SHA_HASH_SIZE);
  public final static CAPDU GET_RECOVERY_DATA_LEN_APDU = new CAPDU(WALLET_APPLET_CLA, INS_GET_RECOVERY_DATA_LEN, 0x00, 0x00, 0x02);
  public final static CAPDU IS_RECOVERY_DATA_SET_APDU = new CAPDU(WALLET_APPLET_CLA, INS_IS_RECOVERY_DATA_SET, 0x00, 0x00, 0x01);
  public final static CAPDU RESET_RECOVERY_DATA_APDU = new CAPDU(WALLET_APPLET_CLA, INS_RESET_RECOVERY_DATA, 0x00, 0x00);
  public final static CAPDU GET_SERIAL_NUMBER_APDU = new CAPDU(WALLET_APPLET_CLA, INS_GET_SERIAL_NUMBER, 0x00, 0x00, GET_SERIAL_NUMBER_LE);

  public static final List<CAPDU> GET_APPLET_STATE_APDU_LIST = new ArrayList<>();

  static {
    GET_APPLET_STATE_APDU_LIST.add(SELECT_TON_WALLET_APPLET_APDU);
    GET_APPLET_STATE_APDU_LIST.add(GET_APP_INFO_APDU);

    addCommand(INS_FINISH_PERS, "FINISH_PERS");
    addCommand(INS_SET_ENCRYPTED_PASSWORD_FOR_CARD_AUTHENTICATION, "SET_ENCRYPTED_PASSWORD_FOR_CARD_AUTHENTICATION");
    addCommand(INS_SET_ENCRYPTED_COMMON_SECRET, "SET_ENCRYPTED_COMMON_SECRET");
    addCommand(INS_VERIFY_PASSWORD, "VERIFY_PASSWORD");
    addCommand(INS_GET_HASH_OF_ENCRYPTED_PASSWORD, "GET_HASH_OF_ENCRYPTED_PASSWORD");
    addCommand(INS_GET_HASH_OF_ENCRYPTED_COMMON_SECRET, "GET_HASH_OF_ENCRYPTED_COMMON_SECRET");
    addCommand(INS_VERIFY_PIN, "VERIFY_PIN");
    addCommand(INS_GET_PUBLIC_KEY, "GET_PUBLIC_KEY");
    addCommand(INS_GET_PUBLIC_KEY_WITH_DEFAULT_HD_PATH, "GET_PUBLIC_KEY_WITH_DEFAULT_HD_PATH");
    addCommand(INS_SIGN_SHORT_MESSAGE, "SIGN_SHORT_MESSAGE");
    addCommand(INS_SIGN_SHORT_MESSAGE_WITH_DEFAULT_PATH, "SIGN_SHORT_MESSAGE_WITH_DEFAULT_PATH");
    addCommand(INS_GET_APP_INFO, "GET_APP_INFO");
    addCommand(INS_GET_KEY_INDEX_IN_STORAGE_AND_LEN, "GET_KEY_INDEX_IN_STORAGE_AND_LEN");
    addCommand(INS_GET_KEY_CHUNK, "GET_KEY_CHUNK");
    addCommand(INS_CHECK_AVAILABLE_VOL_FOR_NEW_KEY, "CHECK_AVAILABLE_VOL_FOR_NEW_KEY");
    addCommand(INS_ADD_KEY_CHUNK, "ADD_KEY_CHUNK");
    addCommand(INS_INITIATE_CHANGE_OF_KEY, "INITIATE_CHANGE_OF_KEY");
    addCommand(INS_CHANGE_KEY_CHUNK, "CHANGE_KEY_CHUNK");
    addCommand(INS_DELETE_KEY_CHUNK, "DELETE_KEY_CHUNK");
    addCommand(INS_GET_DELETE_KEY_CHUNK_NUM_OF_PACKETS, "GET_DELETE_KEY_CHUNK_NUM_OF_PACKETS");
    addCommand(INS_GET_DELETE_KEY_RECORD_NUM_OF_PACKETS, "GET_DELETE_KEY_RECORD_NUM_OF_PACKETS");
    addCommand(INS_INITIATE_DELETE_KEY, "INITIATE_DELETE_KEY");
    addCommand(INS_DELETE_KEY_RECORD, "DELETE_KEY_RECORD");
    addCommand(INS_GET_NUMBER_OF_KEYS, "GET_NUMBER_OF_KEYS");
    addCommand(INS_GET_FREE_STORAGE_SIZE, "GET_FREE_STORAGE_SIZE");
    addCommand(INS_GET_OCCUPIED_STORAGE_SIZE, "GET_OCCUPIED_STORAGE_SIZE");
    addCommand(INS_GET_HMAC, "GET_HMAC");
    addCommand(INS_RESET_KEYCHAIN, "RESET_KEYCHAIN");
    addCommand(INS_GET_SAULT, "GET_SAULT");
    addCommand(INS_CHECK_KEY_HMAC_CONSISTENCY, "CHECK_KEY_HMAC_CONSISTENCY");
    addCommand(INS_GET_SERIAL_NUMBER, "GET_SERIAL_NUMBER");
    addCommand(INS_SET_SERIAL_NUMBER, "SET_SERIAL_NUMBER");
    addCommand(INS_GET_RECOVERY_DATA_HASH, "GET_RECOVERY_DATA_HASH");
    addCommand(INS_GET_RECOVERY_DATA_LEN, "GET_RECOVERY_DATA_LEN");
    addCommand(INS_GET_RECOVERY_DATA_PART, "GET_RECOVERY_DATA_PART");
    addCommand(INS_RESET_RECOVERY_DATA, "RESET_RECOVERY_DATA");
    addCommand(INS_IS_RECOVERY_DATA_SET, "IS_RECOVERY_DATA_SET");
    addCommand(INS_ADD_RECOVERY_DATA_PART, "ADD_RECOVERY_DATA_PART");
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

  public static CAPDU getVerifyPasswordAPDU(byte[] passwordBytes, byte[] initialVector) throws Exception {
    if (passwordBytes == null || passwordBytes.length != PASSWORD_SIZE)
      throw new IllegalArgumentException(ERROR_MSG_ACTIVATION_PASSWORD_BYTES_SIZE_INCORRECT);
    if (initialVector == null || initialVector.length != IV_SIZE)
      throw new IllegalArgumentException(ERROR_MSG_INITIAL_VECTOR_BYTES_SIZE_INCORRECT);
    byte[] data = BYTE_ARRAY_HELPER.bConcat(passwordBytes, initialVector);
    return new CAPDU(WALLET_APPLET_CLA, INS_VERIFY_PASSWORD, 0x00, 0x00, data);
  }

  public static CAPDU getVerifyPinAPDU(byte[] pinBytes, byte[] sault) throws Exception {
    if (pinBytes == null || pinBytes.length != PIN_SIZE)
      throw new IllegalArgumentException(ERROR_MSG_PIN_BYTES_SIZE_INCORRECT);
    checkSault(sault);
    byte[] data = prepareApduData(BYTE_ARRAY_HELPER.bConcat(pinBytes, sault));
    return new CAPDU(WALLET_APPLET_CLA, INS_VERIFY_PIN, 0x00, 0x00, data);
  }

  public static CAPDU getSignShortMessageWithDefaultPathAPDU(byte[] dataForSigning, byte[] sault) throws Exception {
    if (dataForSigning == null || dataForSigning.length == 0 || dataForSigning.length > DATA_FOR_SIGNING_MAX_SIZE)
      throw new IllegalArgumentException(ERROR_MSG_DATA_BYTES_SIZE_INCORRECT);
    checkSault(sault);
    byte[] data = prepareApduData(BYTE_ARRAY_HELPER.bConcat(new byte[]{0x00, (byte) (dataForSigning.length)}, dataForSigning, sault));
    return new CAPDU(WALLET_APPLET_CLA, INS_SIGN_SHORT_MESSAGE_WITH_DEFAULT_PATH, 0x00, 0x00, data, SIG_LEN);
  }

  public static CAPDU getSignShortMessageAPDU(byte[] dataForSigning, byte[] ind, byte[] sault) throws Exception {
    if (dataForSigning == null || dataForSigning.length == 0 || dataForSigning.length > DATA_FOR_SIGNING_MAX_SIZE_FOR_CASE_WITH_PATH)
      throw new IllegalArgumentException(ERROR_MSG_DATA_WITH_HD_PATH_BYTES_SIZE_INCORRECT);
    checkSault(sault);
    checkHdIndex(ind);
    byte[] data = prepareApduData(BYTE_ARRAY_HELPER.bConcat(new byte[]{0x00, (byte) (dataForSigning.length)}, dataForSigning, new byte[]{(byte) ind.length}, ind, sault));
    return new CAPDU(WALLET_APPLET_CLA, INS_SIGN_SHORT_MESSAGE, 0x00, 0x00, data, SIG_LEN);
  }

  public static CAPDU getPublicKeyAPDU(byte[] ind) {
    checkHdIndex(ind);
    return new CAPDU(WALLET_APPLET_CLA, INS_GET_PUBLIC_KEY,
      0x00, 0x00,
      ind,
      PUBLIC_KEY_LEN);
  }

  public static CAPDU addRecoveryDataPartAPDU(byte p1, byte[] data) {
    return new CAPDU(WALLET_APPLET_CLA, INS_ADD_RECOVERY_DATA_PART,
      p1, (byte) 0x00,
      data);
  }

  public static CAPDU getRecoveryDataPartAPDU(byte[] startPositionBytes, byte le) {
    return new CAPDU(WALLET_APPLET_CLA, INS_GET_RECOVERY_DATA_PART,
      (byte) 0x00, (byte) 0x00,
      startPositionBytes,
      le);
  }

  public static CAPDU getResetKeyChainAPDU(byte[] sault) throws Exception {
    byte[] data = prepareSaultBasedApduData(sault);
    return new CAPDU(WALLET_APPLET_CLA, INS_RESET_KEYCHAIN, 0x00, 0x00, data);
  }

  public static CAPDU getNumberOfKeysAPDU(byte[] sault) throws Exception {
    byte[] data = prepareSaultBasedApduData(sault);
    return new CAPDU(WALLET_APPLET_CLA, INS_GET_NUMBER_OF_KEYS, 0x00, 0x00,
      data, GET_NUMBER_OF_KEYS_LE);
  }

  public static CAPDU getGetOccupiedSizeAPDU(byte[] sault) throws Exception {
    byte[] data = prepareSaultBasedApduData(sault);
    return new CAPDU(
      WALLET_APPLET_CLA,
      INS_GET_OCCUPIED_STORAGE_SIZE,
      0x00, 0x00,
      data, GET_OCCUPIED_SIZE_LE);
  }

  public static CAPDU getGetFreeSizeAPDU(byte[] sault) throws Exception {
    byte[] data = prepareSaultBasedApduData(sault);
    return new CAPDU(
      WALLET_APPLET_CLA,
      INS_GET_FREE_STORAGE_SIZE,
      0x00, 0x00,
      data, GET_FREE_SIZE_LE);
  }

  public static CAPDU getCheckAvailableVolForNewKeyAPDU(short keySize, byte[] sault) throws Exception {
    checkSault(sault);
    byte[] data = prepareApduData(BYTE_ARRAY_HELPER.bConcat(new byte[]{(byte) (keySize >> 8), (byte) (keySize)}, sault));
    return new CAPDU(
      WALLET_APPLET_CLA,
      INS_CHECK_AVAILABLE_VOL_FOR_NEW_KEY,
      0x00, 0x00,
      data);
  }

  public static CAPDU getCheckKeyHmacConsistencyAPDU(byte[] keyHmac, byte[] sault) throws Exception {
    checkHmac(keyHmac);
    checkSault(sault);
    byte[] data = prepareApduData(BYTE_ARRAY_HELPER.bConcat(keyHmac, sault));
    return new CAPDU(WALLET_APPLET_CLA, INS_CHECK_KEY_HMAC_CONSISTENCY, 0x00, 0x00, data);
  }

  public static CAPDU getInitiateChangeOfKeyAPDU(byte[] index, byte[] sault) throws Exception {
    checkKeyChainKeyIndex(index);
    checkSault(sault);
    byte[] data = prepareApduData(BYTE_ARRAY_HELPER.bConcat(new byte[]{index[0], index[1]}, sault));
    return new CAPDU(
      WALLET_APPLET_CLA,
      INS_INITIATE_CHANGE_OF_KEY,
      0x00, 0x00,
      data);
  }

  public static CAPDU getGetIndexAndLenOfKeyInKeyChainAPDU(byte[] keyHmac, byte[] sault) throws Exception {
    checkHmac(keyHmac);
    checkSault(sault);
    byte[] data = prepareApduData(BYTE_ARRAY_HELPER.bConcat(keyHmac, sault));
    return new CAPDU(
      WALLET_APPLET_CLA,
      INS_GET_KEY_INDEX_IN_STORAGE_AND_LEN,
      0x00, 0x00,
      data,
      GET_KEY_INDEX_IN_STORAGE_AND_LEN_LE);
  }

  public static CAPDU getInitiateDeleteOfKeyAPDU(byte[] index, byte[] sault) throws Exception {
    checkKeyChainKeyIndex(index);
    checkSault(sault);
    byte[] data = prepareApduData(BYTE_ARRAY_HELPER.bConcat(new byte[]{index[0], index[1]}, sault));
    return new CAPDU(
      WALLET_APPLET_CLA,
      INS_INITIATE_DELETE_KEY,
      0x00, 0x00,
      data,
      INITIATE_DELETE_KEY_LE);
  }

  public static CAPDU getDeleteKeyChunkNumOfPacketsAPDU(byte[] sault) throws Exception {
    byte[] data = prepareSaultBasedApduData(sault);
    return new CAPDU(
      WALLET_APPLET_CLA,
      INS_GET_DELETE_KEY_CHUNK_NUM_OF_PACKETS,
      0x00, 0x00,
      data, GET_DELETE_KEY_CHUNK_NUM_OF_PACKETS_LE);
  }

  public static CAPDU getDeleteKeyRecordNumOfPacketsAPDU(byte[] sault) throws Exception {
    byte[] data = prepareSaultBasedApduData(sault);
    return new CAPDU(
      WALLET_APPLET_CLA,
      INS_GET_DELETE_KEY_RECORD_NUM_OF_PACKETS,
      0x00, 0x00,
      data, GET_DELETE_KEY_RECORD_NUM_OF_PACKETS_LE);
  }

  public static CAPDU getDeleteKeyChunkAPDU(byte[] sault) throws Exception {
    byte[] data = prepareSaultBasedApduData(sault);
    return new CAPDU(
      WALLET_APPLET_CLA,
      INS_DELETE_KEY_CHUNK,
      0x00, 0x00,
      data,
      DELETE_KEY_CHUNK_LE);
  }

  public static CAPDU getDeleteKeyRecordAPDU(byte[] sault) throws Exception {
    byte[] data = prepareSaultBasedApduData(sault);
    return new CAPDU(
      WALLET_APPLET_CLA,
      INS_DELETE_KEY_RECORD,
      0x00, 0x00,
      data,
      DELETE_KEY_RECORD_LE);
  }

  public static CAPDU getGetHmacAPDU(byte[] index, byte[] sault) throws Exception {
    checkKeyChainKeyIndex(index);
    checkSault(sault);
    byte[] data = prepareApduData(BYTE_ARRAY_HELPER.bConcat(index, sault));
    return new CAPDU(
      WALLET_APPLET_CLA,
      INS_GET_HMAC,
      0x00, 0x00,
      data,
      GET_HMAC_LE);
  }

  public static CAPDU getGetKeyChunkAPDU(byte[] index, short startPos, byte[] sault, byte le) throws Exception {
    checkKeyChainKeyIndex(index);
    checkSault(sault);
    byte[] data = prepareApduData(BYTE_ARRAY_HELPER.bConcat(index, new byte[]{(byte) (startPos >> 8), (byte) (startPos)}, sault));
    return new CAPDU(
      WALLET_APPLET_CLA,
      INS_GET_KEY_CHUNK,
      0x00, 0x00,
      data,
      le);
  }

  public static CAPDU getAddKeyChunkAPDU(byte p1, byte[] keyChunkOrMacBytes, byte[] sault) throws Exception {
    return getSendKeyChunkAPDU(INS_ADD_KEY_CHUNK, p1, keyChunkOrMacBytes, sault);
  }

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
    return (p1 == 2) ? new CAPDU(WALLET_APPLET_CLA, ins, p1, 0x00, data, SEND_CHUNK_LE) :
      new CAPDU(WALLET_APPLET_CLA, ins, p1, 0x00, data);
  }

  private static byte[] prepareSaultBasedApduData(byte[] sault) throws Exception {
    checkSault(sault);
    return BYTE_ARRAY_HELPER.bConcat(sault, HMAC_HELPER.computeMac(sault));
  }

  private static byte[] prepareApduData(byte[] dataChunk) throws Exception {
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
    if (ind == null || ind.length == 0 || ind.length > MAX_IND_SIZE)
      throw new IllegalArgumentException(ERROR_MSG_HD_INDEX_BYTES_SIZE_INCORRECT);
  }

  private static void checkKeyChainKeyIndex(byte[] ind) {
    if (ind == null || ind.length != KEYCHAIN_KEY_INDEX_LEN)
      throw new IllegalArgumentException(ERROR_MSG_KEY_INDEX_BYTES_SIZE_INCORRECT);
  }
}
