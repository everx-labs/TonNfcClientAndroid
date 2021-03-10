package com.tonnfccard.smartcard;

import androidx.annotation.RestrictTo;

import com.tonnfccard.utils.ByteArrayUtil;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.tonnfccard.TonWalletConstants.*;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_STATE_RESPONSE_INCORRECT;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.INS_ADD_KEY_CHUNK;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.INS_ADD_RECOVERY_DATA_PART;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.INS_CHANGE_KEY_CHUNK;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.INS_CHECK_AVAILABLE_VOL_FOR_NEW_KEY;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.INS_CHECK_KEY_HMAC_CONSISTENCY;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.INS_DELETE_KEY_CHUNK;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.INS_DELETE_KEY_RECORD;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.INS_FINISH_PERS;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.INS_GET_APP_INFO;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.INS_GET_DELETE_KEY_CHUNK_NUM_OF_PACKETS;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.INS_GET_DELETE_KEY_RECORD_NUM_OF_PACKETS;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.INS_GET_FREE_STORAGE_SIZE;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.INS_GET_HASH_OF_ENCRYPTED_COMMON_SECRET;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.INS_GET_HASH_OF_ENCRYPTED_PASSWORD;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.INS_GET_HMAC;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.INS_GET_KEY_CHUNK;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.INS_GET_KEY_INDEX_IN_STORAGE_AND_LEN;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.INS_GET_NUMBER_OF_KEYS;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.INS_GET_OCCUPIED_STORAGE_SIZE;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.INS_GET_PUBLIC_KEY;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.INS_GET_PUBLIC_KEY_WITH_DEFAULT_HD_PATH;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.INS_GET_RECOVERY_DATA_HASH;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.INS_GET_RECOVERY_DATA_LEN;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.INS_GET_RECOVERY_DATA_PART;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.INS_GET_SAULT;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.INS_GET_SERIAL_NUMBER;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.INS_INITIATE_CHANGE_OF_KEY;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.INS_INITIATE_DELETE_KEY;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.INS_IS_RECOVERY_DATA_SET;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.INS_RESET_KEYCHAIN;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.INS_RESET_RECOVERY_DATA;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.INS_SET_ENCRYPTED_COMMON_SECRET;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.INS_SET_ENCRYPTED_PASSWORD_FOR_CARD_AUTHENTICATION;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.INS_SET_SERIAL_NUMBER;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.INS_SIGN_SHORT_MESSAGE;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.INS_SIGN_SHORT_MESSAGE_WITH_DEFAULT_PATH;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.INS_VERIFY_PASSWORD;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.INS_VERIFY_PIN;


@RestrictTo(RestrictTo.Scope.LIBRARY)
public enum TonWalletAppletStates {

  INSTALLED ((byte) 0x07, INSTALLED_STATE_MSG),
  PERSONALIZED ((byte) 0x17, PERSONALIZED_STATE_MSG),
  WAITE_AUTHORIZATION_MODE ((byte) 0x27, WAITE_AUTHORIZATION_MSG),
  DELETE_KEY_FROM_KEYCHAIN_MODE ((byte) 0x37, DELETE_KEY_FROM_KEYCHAIN_MSG),
  BLOCKED_MODE ((byte) 0x47, BLOCKED_MSG);

  public static final List<TonWalletAppletStates> ALL_APPLET_STATES = Arrays.asList(TonWalletAppletStates.values());
  public static final List<TonWalletAppletStates> INSTALLED_STATE = Arrays.asList(TonWalletAppletStates.INSTALLED);
  public static final List<TonWalletAppletStates> PERSONALIZED_STATE = Arrays.asList(TonWalletAppletStates.PERSONALIZED);
  public static final List<TonWalletAppletStates> WAITE_AUTHORIZATION_STATE = Arrays.asList(TonWalletAppletStates.WAITE_AUTHORIZATION_MODE);
  public static final List<TonWalletAppletStates> PERSONALIZED_AND_DELETE_STATE = Arrays.asList(TonWalletAppletStates.PERSONALIZED, TonWalletAppletStates.DELETE_KEY_FROM_KEYCHAIN_MODE);


  //private static Map<Byte, String> tonWalletAppletStates = new HashMap<>();
  private static Map<Byte, List<TonWalletAppletStates>> tonWalletAppletCommandStateMapping = new HashMap<>();



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

  private Byte value;
  private String description;

  private static final ByteArrayUtil BYTE_ARRAY_HELPER = ByteArrayUtil.getInstance();

  TonWalletAppletStates(Byte value, String descr) {
    this.value = value;
    this.description = descr;
  }

  public String getDescription() {
    return description;
  }

  public Byte getValue() {
    return value;
  }

  @Override
  public String toString() {
    return "State{" +
      "value='" + value + '\'' +
      "description='" + description + '\'' +
      '}';
  }

  public static TonWalletAppletStates findByStateValue(Byte value) throws Exception{
    return Arrays.stream(TonWalletAppletStates.values()).filter(v ->
      v.getValue().equals(value)).findFirst().orElseThrow(() ->
      new Exception(ERROR_MSG_STATE_RESPONSE_INCORRECT + BYTE_ARRAY_HELPER.hex(value)));
  }
}
