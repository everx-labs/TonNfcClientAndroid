package com.tonnfccard;

import android.content.Context;
import android.util.Log;

import com.tonnfccard.callback.NfcCallback;
import com.tonnfccard.helpers.CardApiInterface;
import com.tonnfccard.nfc.NfcApduRunner;
import com.tonnfccard.smartcard.TonWalletAppletStates;
import com.tonnfccard.smartcard.ApduRunner;
import com.tonnfccard.smartcard.RAPDU;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.tonnfccard.TonWalletConstants.*;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_KEY_DATA_PORTION_INCORRECT_LEN;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_NUM_OF_KEYS_INCORRECT_AFTER_ADD;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_NUM_OF_KEYS_INCORRECT_AFTER_CHANGE;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_APPLET_DOES_NOT_WAIT_TO_DELETE_KEY;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_APPLET_IS_NOT_PERSONALIZED;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_DELETE_KEY_CHUNK_RESPONSE_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_DELETE_KEY_CHUNK_RESPONSE_LEN_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_DELETE_KEY_RECORD_RESPONSE_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_DELETE_KEY_RECORD_RESPONSE_LEN_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_FREE_SIZE_RESPONSE_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_GET_DELETE_KEY_CHUNK_NUM_OF_PACKETS_RESPONSE_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_GET_DELETE_KEY_CHUNK_NUM_OF_PACKETS_RESPONSE_LEN_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_GET_DELETE_KEY_RECORD_NUM_OF_PACKETS_RESPONSE_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_GET_DELETE_KEY_RECORD_NUM_OF_PACKETS_RESPONSE_LEN_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_GET_FREE_SIZE_RESPONSE_LEN_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_GET_HMAC_RESPONSE_LEN_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_GET_KEY_INDEX_IN_STORAGE_AND_LEN_RESPONSE_LEN_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_GET_NUMBER_OF_KEYS_RESPONSE_LEN_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_GET_OCCUPIED_SIZE_RESPONSE_LEN_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_INITIATE_DELETE_KEY_RESPONSE_LEN_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_KEY_HMAC_LEN_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_KEY_HMAC_NOT_HEX;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_KEY_INDEX_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_KEY_INDEX_STRING_NOT_NUMERIC;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_KEY_INDEX_VALUE_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_KEY_LENGTH_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_KEY_LEN_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_KEY_NOT_HEX;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_KEY_SIZE_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_NEW_KEY_LEN_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_NUMBER_OF_KEYS_RESPONSE_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_OCCUPIED_SIZE_RESPONSE_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_SEND_CHUNK_RESPONSE_LEN_INCORRECT;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.DELETE_KEY_CHUNK_LE;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.DELETE_KEY_RECORD_LE;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.GET_DELETE_KEY_CHUNK_NUM_OF_PACKETS_LE;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.GET_DELETE_KEY_RECORD_NUM_OF_PACKETS_LE;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.GET_FREE_SIZE_LE;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.GET_KEY_INDEX_IN_STORAGE_AND_LEN_LE;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.GET_NUMBER_OF_KEYS_LE;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.GET_OCCUPIED_SIZE_LE;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.INITIATE_DELETE_KEY_LE;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.INS_ADD_KEY_CHUNK;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.INS_CHANGE_KEY_CHUNK;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.SEND_CHUNK_LE;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.getCheckAvailableVolForNewKeyAPDU;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.getCheckKeyHmacConsistencyAPDU;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.getDeleteKeyChunkAPDU;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.getDeleteKeyChunkNumOfPacketsAPDU;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.getDeleteKeyRecordAPDU;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.getDeleteKeyRecordNumOfPacketsAPDU;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.getGetFreeSizeAPDU;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.getGetHmacAPDU;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.getGetIndexAndLenOfKeyInKeyChainAPDU;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.getGetKeyChunkAPDU;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.getGetOccupiedSizeAPDU;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.getInitiateChangeOfKeyAPDU;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.getInitiateDeleteOfKeyAPDU;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.getNumberOfKeysAPDU;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.getResetKeyChainAPDU;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.getSendKeyChunkAPDU;


/**
 * Class containing functions-wrappers for card operations related to card keychain.
 *
 * The most important functions: resetKeyChain, getKeyChainInfo, deleteKeyFromKeyChain, finishDeleteKeyFromKeyChainAfterInterruption,
 * getKeyFromKeyChain, addKeyIntoKeyChain, changeKeyInKeyChain, getKeyChainDataAboutAllKeys.
 *
 * Auxiliary functions: getNumberOfKeys, checkKeyHmacConsistency, checkAvailableVolForNewKey, getIndexAndLenOfKeyInKeyChain,
 * getDeleteKeyRecordNumOfPackets, getDeleteKeyChunkNumOfPackets, getOccupiedStorageSize, getFreeStorageSize, getHmac.
 */

public final class CardKeyChainApi extends TonWalletApi {

  public static final String TAG = "CardKeyChainNfcApi";

  public CardKeyChainApi(Context activity, NfcApduRunner apduRunner) {
    super(activity, apduRunner);
  }

  public CardKeyChainApi(NfcApduRunner apduRunner) {
    super(apduRunner);
  }

  /**
   * @param callback
   * Clear keychain, i.e. remove all stored keys.
   */
  private final CardApiInterface<List<String>> resetKeyChain = list -> this.resetKeyChainAndGetJson();

  public void resetKeyChain(final NfcCallback callback, Boolean... showDialog) {
    boolean showDialogFlag = showDialog.length > 0 ? showDialog[0] : false;
    CardTask cardTask = new CardTask(this, callback,  Collections.emptyList(), resetKeyChain, showDialogFlag);
    cardTask.execute();
  }


  /**
   * @return
   * @throws Exception
   * Clear keychain, i.e. remove all stored keys.
   */
  public String resetKeyChainAndGetJson() throws Exception {
    try {
      //long start = System.currentTimeMillis();
      resetKeyChain();
      String json =  JSON_HELPER.createResponseJson(DONE_MSG);
      //long end = System.currentTimeMillis();
      //Log.d("TAG", "!!Time = " + String.valueOf(end - start) );
      return json;
    }
    catch (Exception e) {
      throw new Exception(EXCEPTION_HELPER.makeFinalErrMsg(e), e);
    }
  }

  /**
   * @param callback
   * Return list of pairs (keyHmac, keyLength) in json format.
   */
  private final CardApiInterface<List<String>> getKeyChainInfo = list -> this.getKeyChainInfoAndGetJson();

  public void getKeyChainInfo(final NfcCallback callback, Boolean... showDialog) {
    boolean showDialogFlag = showDialog.length > 0 ? showDialog[0] : false;
    CardTask cardTask = new CardTask(this, callback,  Collections.emptyList(), getKeyChainInfo, showDialogFlag);
    cardTask.execute();
  }

  /**
   * @return
   * @throws Exception
   * Return list of pairs (keyHmac, keyLength) in json format.
   */
  public String getKeyChainInfoAndGetJson() throws Exception {
    try {
      //long start = System.currentTimeMillis();
      int numOfKeys = getNumberOfKeys();
      int occupiedStorageSize = getOccupiedStorageSize();
      int freeStorageSize = getFreeStorageSize();
      JSONObject jsonResponse = new JSONObject();
      jsonResponse.put(NUMBER_OF_KEYS_FIELD, numOfKeys);
      jsonResponse.put(OCCUPIED_SIZE_FIELD, occupiedStorageSize);
      jsonResponse.put(FREE_SIZE_FIELD, freeStorageSize);
      jsonResponse.put(STATUS_FIELD, SUCCESS_STATUS);
      //long end = System.currentTimeMillis();
      //Log.d("TAG", "!!Time = " + String.valueOf(end - start) );
      return jsonResponse.toString();
    }
    catch (Exception e) {
      throw new Exception(EXCEPTION_HELPER.makeFinalErrMsg(e), e);
    }
  }

  /**
   * @param callback
   * Return number of keys in card keychain.
   */
  private final CardApiInterface<List<String>> getNumberOfKeys = list -> this.getNumberOfKeysAndGetJson();

  public void getNumberOfKeys(final NfcCallback callback, Boolean... showDialog) {
    boolean showDialogFlag = showDialog.length > 0 ? showDialog[0] : false;
    CardTask cardTask = new CardTask(this, callback,  Collections.emptyList(), getNumberOfKeys, showDialogFlag);
    cardTask.execute();
  }

  /**
   * @return
   * @throws Exception
   * Return number of keys in card keychain.
   */
  public String getNumberOfKeysAndGetJson() throws Exception {
    try {
      //long start = System.currentTimeMillis();
      int numOfKeys = getNumberOfKeys();
      String json = JSON_HELPER.createResponseJson(Integer.valueOf(numOfKeys).toString());
      //long end = System.currentTimeMillis();
      //Log.d("TAG", "!!Time = " + String.valueOf(end - start) );
      return json;
    }
    catch (Exception e) {
      throw new Exception(EXCEPTION_HELPER.makeFinalErrMsg(e), e);
    }
  }

  /**
   * @param keyHmac
   * @param callback
   * Checks if card's keychain stores a key with such keyHmac and if this hmac really corresponds to the key.
   */
  private final CardApiInterface<List<String>> checkKeyHmacConsistency = list -> this.checkKeyHmacConsistencyAndGetJson(list.get(0));

  public void checkKeyHmacConsistency(final String keyHmac,final NfcCallback callback, Boolean... showDialog) {
    boolean showDialogFlag = showDialog.length > 0 ? showDialog[0] : false;
    CardTask cardTask = new CardTask(this, callback,  Collections.singletonList(keyHmac), checkKeyHmacConsistency, showDialogFlag);
    cardTask.execute();
  }

  /**
   * @param keyHmac
   * @return
   * @throws Exception
   * Checks if card's keychain stores a key with such keyHmac and if this hmac really corresponds to the key.
   */
  public String checkKeyHmacConsistencyAndGetJson(final String keyHmac) throws Exception {
    try {
      //long start = System.currentTimeMillis();
      if (!STR_HELPER.isHexString(keyHmac))
        throw new Exception(ERROR_MSG_KEY_HMAC_NOT_HEX);
      if (keyHmac.length() != 2 * HMAC_SHA_SIG_SIZE)
        throw new Exception(ERROR_MSG_KEY_HMAC_LEN_INCORRECT);
      checkKeyHmacConsistency(BYTE_ARR_HELPER.bytes(keyHmac));
      String json = JSON_HELPER.createResponseJson(DONE_MSG);
      //long end = System.currentTimeMillis();
      //Log.d("TAG", "!!Time = " + String.valueOf(end - start) );
      return json;
    }
    catch (Exception e) {
      throw new Exception(EXCEPTION_HELPER.makeFinalErrMsg(e), e);
    }
  }

  /**
   * @param keySize
   * @param callback
   * Check if there is enough free volume in card keychain to add new key of length = keySize.
   * If there is no enough space then it throws an exception
   */
  private final CardApiInterface<List<String>> checkAvailableVolForNewKey = list -> this.checkAvailableVolForNewKeyAndGetJson(Short.parseShort(list.get(0)));

  public void checkAvailableVolForNewKey(final Short keySize, final NfcCallback callback, Boolean... showDialog) {
    boolean showDialogFlag = showDialog.length > 0 ? showDialog[0] : false;
    CardTask cardTask = new CardTask(this, callback,  Collections.singletonList(String.valueOf(keySize)), checkAvailableVolForNewKey, showDialogFlag);
    cardTask.execute();
  }

  /**
   * @param keySize
   * @return
   * @throws Exception
   * Check if there is enough free volume in card keychain to add new key of length = keySize.
   * If there is no enough space then it throws an exception
   */
  public String checkAvailableVolForNewKeyAndGetJson(final Short keySize) throws Exception {
    try {
      //long start = System.currentTimeMillis();
      if (keySize <= 0 || keySize > MAX_KEY_SIZE_IN_KEYCHAIN)
        throw new Exception(ERROR_MSG_KEY_SIZE_INCORRECT);
      checkAvailableVolForNewKey(keySize);
      String json = JSON_HELPER.createResponseJson(DONE_MSG);
      //long end = System.currentTimeMillis();
      //Log.d("TAG", "!!Time = " + String.valueOf(end - start) );
      return json;
    }
    catch (Exception e) {
      throw new Exception(EXCEPTION_HELPER.makeFinalErrMsg(e), e);
    }
  }

  /**
   * @param keyHmac
   * @param callback
   * Read index (inside internal applet storage) and length of key by its hmac
   */
  private final CardApiInterface<List<String>> getIndexAndLenOfKeyInKeyChain = list -> this.getIndexAndLenOfKeyInKeyChainAndGetJson(list.get(0));

  public void getIndexAndLenOfKeyInKeyChain(final String keyHmac, final NfcCallback callback, Boolean... showDialog) {
    boolean showDialogFlag = showDialog.length > 0 ? showDialog[0] : false;
    CardTask cardTask = new CardTask(this, callback,  Collections.singletonList(keyHmac), getIndexAndLenOfKeyInKeyChain, showDialogFlag);
    cardTask.execute();
  }

  /**
   * @param keyHmac
   * @return
   * @throws Exception
   * Read index (inside internal applet storage) and length of key by its hmac
   */
  public String getIndexAndLenOfKeyInKeyChainAndGetJson(final String keyHmac)  throws Exception {
    try {
      //long start = System.currentTimeMillis();
      if (!STR_HELPER.isHexString(keyHmac))
        throw new Exception(ERROR_MSG_KEY_HMAC_NOT_HEX);
      if (keyHmac.length() != 2 * HMAC_SHA_SIG_SIZE)
        throw new Exception(ERROR_MSG_KEY_HMAC_LEN_INCORRECT);
      String response = getIndexAndLenOfKeyInKeyChain(BYTE_ARR_HELPER.bytes(keyHmac)).toString();
      String json = JSON_HELPER.createResponseJson(response);
      //long end = System.currentTimeMillis();
      //Log.d("TAG", "!!Time = " + String.valueOf(end - start) );
      return json;
    }
    catch (Exception e) {
      throw new Exception(EXCEPTION_HELPER.makeFinalErrMsg(e), e);
    }
  }

  /**
   * @param callback
   * Returns the number of keys records packets that must be deleted to finish deleting of key.
   */
  private final CardApiInterface<List<String>> getDeleteKeyRecordNumOfPackets = list -> this.getDeleteKeyRecordNumOfPacketsAndGetJson();

  public void getDeleteKeyRecordNumOfPackets(final NfcCallback callback, Boolean... showDialog) {
    boolean showDialogFlag = showDialog.length > 0 ? showDialog[0] : false;
    CardTask cardTask = new CardTask(this, callback,  Collections.emptyList(), getDeleteKeyRecordNumOfPackets, showDialogFlag);
    cardTask.execute();
  }

  /**
   * @return
   * @throws Exception
   * Returns the number of keys records packets that must be deleted to finish deleting of key.
   */
  public String getDeleteKeyRecordNumOfPacketsAndGetJson()  throws Exception {
    try {
      //long start = System.currentTimeMillis();
      String numOfPackets = Integer.valueOf(getDeleteKeyRecordNumOfPackets()).toString();
      String json = JSON_HELPER.createResponseJson(numOfPackets);
      //long end = System.currentTimeMillis();
      //Log.d("TAG", "!!Time = " + String.valueOf(end - start) );
      return json;
    }
    catch (Exception e) {
      throw new Exception(EXCEPTION_HELPER.makeFinalErrMsg(e), e);
    }
  }

  /**
   * @param callback
   * Returns the number of keys chunks packets that must be deleted to finish deleting of key.
   */
  private final CardApiInterface<List<String>> getDeleteKeyChunkNumOfPackets = list -> this.getDeleteKeyChunkNumOfPacketsAndGetJson();

  public void getDeleteKeyChunkNumOfPackets(final NfcCallback callback, Boolean... showDialog) {
    boolean showDialogFlag = showDialog.length > 0 ? showDialog[0] : false;
    CardTask cardTask = new CardTask(this, callback,  Collections.emptyList(), getDeleteKeyChunkNumOfPackets, showDialogFlag);
    cardTask.execute();
  }

  /**
   * @return
   * @throws Exception
   * Returns the number of keys chunks packets that must be deleted to finish deleting of key.
   */
  public String getDeleteKeyChunkNumOfPacketsAndGetJson() throws Exception {
    try {
      //long start = System.currentTimeMillis();
      String numOfPackets = Integer.valueOf(getDeleteKeyChunkNumOfPackets()).toString();
      String json =  JSON_HELPER.createResponseJson(numOfPackets);
      //long end = System.currentTimeMillis();
      //Log.d("TAG", "!!Time = " + String.valueOf(end - start) );
      return json;
    }
    catch (Exception e) {
      throw new Exception(EXCEPTION_HELPER.makeFinalErrMsg(e), e);
    }
  }

  /**
   * @param keyHmac
   * @param callback
   * Delete key from card keychain based on its hmac.
   */
  private final CardApiInterface<List<String>> deleteKeyFromKeyChain = list -> this.deleteKeyFromKeyChainAndGetJson(list.get(0));

  public void deleteKeyFromKeyChain(final String keyHmac, final NfcCallback callback, Boolean... showDialog) {
    boolean showDialogFlag = showDialog.length > 0 ? showDialog[0] : false;
    CardTask cardTask = new CardTask(this, callback,  Collections.singletonList(keyHmac), deleteKeyFromKeyChain, showDialogFlag);
    cardTask.execute();
  }

  /**
   * @param keyHmac
   * @return
   * @throws Exception
   * Delete key from card keychain based on its hmac.
   */
  public String deleteKeyFromKeyChainAndGetJson(final String keyHmac) throws Exception {
    try {
      //long start = System.currentTimeMillis();
      if (!STR_HELPER.isHexString(keyHmac))
        throw new Exception(ERROR_MSG_KEY_HMAC_NOT_HEX);
      if (keyHmac.length() != 2 * HMAC_SHA_SIG_SIZE)
        throw new Exception(ERROR_MSG_KEY_HMAC_LEN_INCORRECT);
      int numOfKeys = deleteKeyFromKeyChain(BYTE_ARR_HELPER.bytes(keyHmac));
      String json =  JSON_HELPER.createResponseJson(Integer.valueOf(numOfKeys).toString());
      //long end = System.currentTimeMillis();
      //Log.d("TAG", "!!Time = " + String.valueOf(end - start) );
      return json;
    }
    catch (Exception e) {
      throw new Exception(EXCEPTION_HELPER.makeFinalErrMsg(e), e);
    }
  }

  /**
   * @param callback
   * Finish the process of deleting key from card keychain.
   * It may be necessary if previous DELETE operation was occassionally interrupted (like card disconnection).
   */
  private final CardApiInterface<List<String>> finishDeleteKeyFromKeyChainAfterInterruption = list -> this.finishDeleteKeyFromKeyChainAfterInterruptionAndGetJson();

  public void finishDeleteKeyFromKeyChainAfterInterruption(final NfcCallback callback, Boolean... showDialog) {
    boolean showDialogFlag = showDialog.length > 0 ? showDialog[0] : false;
    CardTask cardTask = new CardTask(this, callback,  Collections.emptyList(), finishDeleteKeyFromKeyChainAfterInterruption, showDialogFlag);
    cardTask.execute();
  }

  /**
   * @return
   * @throws Exception
   * Finish the process of deleting key from card keychain.
   * It may be necessary if previous DELETE operation was occassionally interrupted (like card disconnection).
   */
  public String finishDeleteKeyFromKeyChainAfterInterruptionAndGetJson() throws Exception {
    try {
      //long start = System.currentTimeMillis();
      int numOfKeys = finishDeleteKeyFromKeyChainAfterInterruption();
      String json =  JSON_HELPER.createResponseJson(Integer.valueOf(numOfKeys).toString());
      //long end = System.currentTimeMillis();
      //Log.d("TAG", "!!Time = " + String.valueOf(end - start) );
      return json;
    }
    catch (Exception e) {
      throw new Exception(EXCEPTION_HELPER.makeFinalErrMsg(e), e);
    }
  }

  /**
   * @param callback
   * Return the volume of occupied size in card keychain (in bytes).
   */
  private final CardApiInterface<List<String>> getOccupiedStorageSize = list -> this.getOccupiedStorageSizeAndGetJson();

  public void getOccupiedStorageSize(final NfcCallback callback, Boolean... showDialog) {
    boolean showDialogFlag = showDialog.length > 0 ? showDialog[0] : false;
    CardTask cardTask = new CardTask(this, callback,  Collections.emptyList(), getOccupiedStorageSize, showDialogFlag);
    cardTask.execute();
  }

  /**
   * @return
   * @throws Exception
   * Return the volume of occupied size in card keychain (in bytes).
   */
  public String getOccupiedStorageSizeAndGetJson() throws Exception {
    try {
      //long start = System.currentTimeMillis();
      String size = Integer.valueOf(getOccupiedStorageSize()).toString();
      String json =  JSON_HELPER.createResponseJson(size);
      //long end = System.currentTimeMillis();
      //Log.d("TAG", "!!Time = " + String.valueOf(end - start) );
      return json;
    }
    catch (Exception e) {
      throw new Exception(EXCEPTION_HELPER.makeFinalErrMsg(e), e);
    }
  }

  /**
   * @param callback
   * Return the volume of free size in card keychain (in bytes).
   */
  private final CardApiInterface<List<String>> getFreeStorageSize = list -> this.getFreeStorageSizeAndGetJson();

  public void getFreeStorageSize(final NfcCallback callback, Boolean... showDialog) {
    boolean showDialogFlag = showDialog.length > 0 ? showDialog[0] : false;
    CardTask cardTask = new CardTask(this, callback,  Collections.emptyList(), getFreeStorageSize, showDialogFlag);
    cardTask.execute();
  }

  /**
   * @return
   * @throws Exception
   * Return the volume of free size in card keychain (in bytes).
   */
  public String getFreeStorageSizeAndGetJson() throws Exception {
    try {
      //long start = System.currentTimeMillis();
      String size = Integer.valueOf(getFreeStorageSize()).toString();
      String json =   JSON_HELPER.createResponseJson(size);
      //long end = System.currentTimeMillis();
      //Log.d("TAG", "!!Time = " + String.valueOf(end - start) );
      return json;
    }
    catch (Exception e) {
      throw new Exception(EXCEPTION_HELPER.makeFinalErrMsg(e), e);
    }
  }

  /**
   * @param keyHmac
   * @param callback
   * Read key from card keychain based on its hmac.
   */
  private final CardApiInterface<List<String>> getKeyFromKeyChain = list -> this.getKeyFromKeyChainAndGetJson(list.get(0));

  public void getKeyFromKeyChain(final String keyHmac, final NfcCallback callback, Boolean... showDialog) {
    boolean showDialogFlag = showDialog.length > 0 ? showDialog[0] : false;
    CardTask cardTask = new CardTask(this, callback,  Collections.singletonList(keyHmac), getKeyFromKeyChain, showDialogFlag);
    cardTask.execute();
  }

  /**
   * @param keyHmac
   * @return
   * @throws Exception
   * Read key from card keychain based on its hmac.
   */
  public String getKeyFromKeyChainAndGetJson(final String keyHmac) throws Exception {
    try {
      //long start = System.currentTimeMillis();
      if (!STR_HELPER.isHexString(keyHmac))
        throw new Exception(ERROR_MSG_KEY_HMAC_NOT_HEX);
      if (keyHmac.length() != 2 * HMAC_SHA_SIG_SIZE)
        throw new Exception(ERROR_MSG_KEY_HMAC_LEN_INCORRECT);
      String key = BYTE_ARR_HELPER.hex(getKeyFromKeyChain(BYTE_ARR_HELPER.bytes(keyHmac)));
      String json =   JSON_HELPER.createResponseJson(key);
      //long end = System.currentTimeMillis();
      //Log.d("TAG", "!!Time = " + String.valueOf(end - start) );
      return json;
    }
    catch (Exception e) {
      throw new Exception(EXCEPTION_HELPER.makeFinalErrMsg(e), e);
    }
  }

  /**
   * @param newKey
   * @param callback
   * Save new key into card keychain.
   */
  private final CardApiInterface<List<String>> addKeyIntoKeyChain = list -> this.addKeyIntoKeyChainAndGetJson(list.get(0));

  public void addKeyIntoKeyChain(final String newKey, final NfcCallback callback, Boolean... showDialog) {
    boolean showDialogFlag = showDialog.length > 0 ? showDialog[0] : false;
    CardTask cardTask = new CardTask(this, callback,  Collections.singletonList(newKey), addKeyIntoKeyChain, showDialogFlag);
    cardTask.execute();
  }

  /**
   * @param newKey
   * @return
   * @throws Exception
   * Save new key into card keychain.
   */
  public String addKeyIntoKeyChainAndGetJson(final String newKey) throws Exception {
    try {
      //long start = System.currentTimeMillis();
      if (!STR_HELPER.isHexString(newKey))
        throw new Exception(ERROR_MSG_KEY_NOT_HEX);
      if (newKey.length() > 2 * MAX_KEY_SIZE_IN_KEYCHAIN)
        throw new Exception(ERROR_MSG_KEY_LEN_INCORRECT);
      String keyHmac = addKeyIntoKeyChain(BYTE_ARR_HELPER.bytes(newKey));
      String json =   JSON_HELPER.createResponseJson(keyHmac);
      //long end = System.currentTimeMillis();
      //Log.d("TAG", "!!Time = " + String.valueOf(end - start) );
      return json;
    }
    catch (Exception e) {
      throw new Exception(EXCEPTION_HELPER.makeFinalErrMsg(e), e);
    }
  }

  /**
   * @param newKey
   * @param oldKeyHMac
   * @param callback
   * Replace existing key by new key. The length of new key must be equal to length of old key.
   */
  private final CardApiInterface<List<String>> changeKeyInKeyChain = list -> this.changeKeyInKeyChainAndGetJson(list.get(0), list.get(1));

  public void changeKeyInKeyChain(final String newKey, final String oldKeyHMac, final NfcCallback callback, Boolean... showDialog) {
    boolean showDialogFlag = showDialog.length > 0 ? showDialog[0] : false;
    CardTask cardTask = new CardTask(this, callback, Arrays.asList(newKey, oldKeyHMac), changeKeyInKeyChain, showDialogFlag);
    cardTask.execute();
  }

  /**
   * @param newKey
   * @param oldKeyHMac
   * @return
   * @throws Exception
   * Replace existing key by new key. The length of new key must be equal to length of old key.
   */
  public String changeKeyInKeyChainAndGetJson(final String newKey, final String oldKeyHMac) throws Exception {
    try {
      //long start = System.currentTimeMillis();
      if (!STR_HELPER.isHexString(newKey))
        throw new Exception(ERROR_MSG_KEY_NOT_HEX);
      if (newKey.length() > 2 * MAX_KEY_SIZE_IN_KEYCHAIN)
        throw new Exception(ERROR_MSG_KEY_LEN_INCORRECT);
      if (!STR_HELPER.isHexString(oldKeyHMac))
        throw new Exception(ERROR_MSG_KEY_HMAC_NOT_HEX);
      if (oldKeyHMac.length() != 2 * HMAC_SHA_SIG_SIZE)
        throw new Exception(ERROR_MSG_KEY_HMAC_LEN_INCORRECT);
      String newKeyHmac = changeKeyInKeyChain(BYTE_ARR_HELPER.bytes(newKey), BYTE_ARR_HELPER.bytes(oldKeyHMac));
      String json = JSON_HELPER.createResponseJson(newKeyHmac);
      //long end = System.currentTimeMillis();
      //Log.d("TAG", "!!Time = " + String.valueOf(end - start) );
      return json;
    }
    catch (Exception e) {
      throw new Exception(EXCEPTION_HELPER.makeFinalErrMsg(e), e);
    }
  }

  /**
   * @param callback
   * Return list of pairs (keyHmac, keyLength) in json format.
   */
  private final CardApiInterface<List<String>> getKeyChainDataAboutAllKeys = list -> this.getKeyChainDataAboutAllKeysAndGetJson();

  public void getKeyChainDataAboutAllKeys(final NfcCallback callback, Boolean... showDialog) {
    boolean showDialogFlag = showDialog.length > 0 ? showDialog[0] : false;
    CardTask cardTask = new CardTask(this, callback,  Collections.emptyList(), getKeyChainDataAboutAllKeys, showDialogFlag);
    cardTask.execute();
  }

  /**
   * @return
   * @throws Exception
   * Return list of pairs (keyHmac, keyLength) in json format.
   */
  public String getKeyChainDataAboutAllKeysAndGetJson() throws Exception {
    try {
      //long start = System.currentTimeMillis();
      Map<String, Short> map = getAllHmacsOfKeysFromCard();
      JSONObject allKeysObj = new JSONObject();
      JSONArray jArray = new JSONArray();
      for (final String hmac : map.keySet()) {
        JSONObject jObject = new JSONObject();
        jObject.put(KEY_HMAC_FIELD, hmac);
        jObject.put(KEY_LENGTH_FIELD, map.get(hmac).toString());
        jArray.put(jObject);
      }
      allKeysObj.put(MESSAGE_FIELD, jArray);
      allKeysObj.put(STATUS_FIELD, SUCCESS_STATUS);
      //long end = System.currentTimeMillis();
      //Log.d("TAG", "!!Time = " + String.valueOf(end - start) );
      return allKeysObj.toString();
    }
    catch (Exception e) {
      throw new Exception(EXCEPTION_HELPER.makeFinalErrMsg(e), e);
    }
  }

  /**
   * @param index
   * @param callback
   * Get hmac of key in card keychain by its index.
   */
  private final CardApiInterface<List<String>> getHmac = list -> this.getHmacAndGetJson(list.get(0));

  public void getHmac(final String index, final NfcCallback callback, Boolean... showDialog) {
    boolean showDialogFlag = showDialog.length > 0 ? showDialog[0] : false;
    CardTask cardTask = new CardTask(this, callback,  Collections.singletonList(index), getHmac, showDialogFlag);
    cardTask.execute();
  }

  /**
   * @param index
   * @return
   * @throws Exception
   * Get hmac of key in card keychain by its index.
   */
  public String getHmacAndGetJson(final String index) throws Exception {
    try {
      //long start = System.currentTimeMillis();
      if (!STR_HELPER.isNumericString(index))
        throw new Exception(ERROR_MSG_KEY_INDEX_STRING_NOT_NUMERIC);
      short ind = parseIndex(index);
      if (ind < 0 || ind > MAX_NUMBER_OF_KEYS_IN_KEYCHAIN - 1)
        throw new Exception(ERROR_MSG_KEY_INDEX_VALUE_INCORRECT);
      byte[] indBytes = new byte[2];
      BYTE_ARR_HELPER.setShort(indBytes, 0, ind);
      byte[] data = getHmac(indBytes);
      JSONObject jObject = new JSONObject();
      byte[] mac = BYTE_ARR_HELPER.bSub(data, 0, HMAC_SHA_SIG_SIZE);
      short len = BYTE_ARR_HELPER.makeShort(data, HMAC_SHA_SIG_SIZE);
      jObject.put(KEY_HMAC_FIELD, BYTE_ARR_HELPER.hex(mac));
      jObject.put(KEY_LENGTH_FIELD, len);
      jObject.put(STATUS_FIELD, SUCCESS_STATUS);
      //long end = System.currentTimeMillis();
      //Log.d("TAG", "!!Time = " + String.valueOf(end - start) );
      return jObject.toString();
    }
    catch (Exception e) {
      throw new Exception(EXCEPTION_HELPER.makeFinalErrMsg(e), e);
    }
  }

  private short parseIndex(String index) throws Exception{
    try {
      return Short.parseShort(index);
    }
    catch (NumberFormatException e) {
      throw new Exception(ERROR_MSG_KEY_INDEX_VALUE_INCORRECT);
    }
  }

  private RAPDU resetKeyChain() throws Exception {
    reselectKeyForHmac();
    byte[] sault = getSaultBytes();
    return apduRunner.sendAPDU(getResetKeyChainAPDU(sault));
  }

  private int getNumberOfKeys() throws Exception {
    reselectKeyForHmac();
    byte[] sault = getSaultBytes();
    RAPDU rapdu = apduRunner.sendAPDU(getNumberOfKeysAPDU(sault));
    if (rapdu == null || rapdu.getData() == null || rapdu.getData().length != GET_NUMBER_OF_KEYS_LE)
      throw new Exception(ERROR_MSG_GET_NUMBER_OF_KEYS_RESPONSE_LEN_INCORRECT);
    byte[] response = rapdu.getData();
    short numOfKeys = BYTE_ARR_HELPER.makeShort(response, 0);
    if (numOfKeys < 0 || numOfKeys > MAX_NUMBER_OF_KEYS_IN_KEYCHAIN)
      throw new Exception(ERROR_MSG_NUMBER_OF_KEYS_RESPONSE_INCORRECT);
    return numOfKeys;
  }

  private void checkKeyHmacConsistency(byte[] keyHmac) throws Exception {
    reselectKeyForHmac();
    byte[] sault = getSaultBytes();
    apduRunner.sendAPDU(getCheckKeyHmacConsistencyAPDU(keyHmac, sault));
  }

  private void checkAvailableVolForNewKey(short keySize) throws Exception {
    TonWalletAppletStates appletState = getTonAppletState();
    if (appletState != TonWalletAppletStates.PERSONALIZED)
      throw new Exception(ERROR_MSG_APPLET_IS_NOT_PERSONALIZED + appletState.getDescription() + ".");
    reselectKeyForHmac();
    byte[] sault = getSaultBytes();
    apduRunner.sendAPDU(getCheckAvailableVolForNewKeyAPDU(keySize, sault));
  }

  private void initiateChangeOfKey(byte[] index) throws Exception {
    TonWalletAppletStates appletState = getTonAppletState();
    if (appletState != TonWalletAppletStates.PERSONALIZED)
      throw new Exception(ERROR_MSG_APPLET_IS_NOT_PERSONALIZED + appletState.getDescription() + ".");
    reselectKeyForHmac();
    byte[] sault = getSaultBytes();
    apduRunner.sendAPDU(getInitiateChangeOfKeyAPDU(index, sault));
  }

  private JSONObject getIndexAndLenOfKeyInKeyChain(byte[] keyHmac) throws Exception {
    reselectKeyForHmac();
    byte[] sault = getSaultBytes();
    RAPDU rapdu =  apduRunner.sendAPDU(getGetIndexAndLenOfKeyInKeyChainAPDU(keyHmac, sault));
    if (rapdu == null || rapdu.getData() == null || rapdu.getData().length != GET_KEY_INDEX_IN_STORAGE_AND_LEN_LE)
      throw new Exception(ERROR_MSG_GET_KEY_INDEX_IN_STORAGE_AND_LEN_RESPONSE_LEN_INCORRECT);
    byte[] response = rapdu.getData();
    short index = BYTE_ARR_HELPER.makeShort(response, 0);
    if (index < 0 || index > MAX_NUMBER_OF_KEYS_IN_KEYCHAIN - 1)
      throw new Exception(ERROR_MSG_KEY_INDEX_INCORRECT);
    short len = BYTE_ARR_HELPER.makeShort(response, 2);
    if (len <= 0 || len > MAX_KEY_SIZE_IN_KEYCHAIN)
      throw new Exception(ERROR_MSG_KEY_LENGTH_INCORRECT);
    JSONObject jsonResponse = new JSONObject();
    jsonResponse.put(KEY_INDEX_FIELD, index);
    jsonResponse.put(KEY_LENGTH_FIELD, len);
    return jsonResponse;
  }

  //todo: probably should check here the number of keys before operation, check the speed
  private int deleteKeyFromKeyChain(byte[] macBytes) throws Exception {
    JSONObject jsonObject = getIndexAndLenOfKeyInKeyChain(macBytes);
    byte[] ind = new byte[2];
    short indVal = (short) jsonObject.getInt(KEY_INDEX_FIELD);
    BYTE_ARR_HELPER.setShort(ind, 0, indVal);
    initiateDeleteOfKey(ind);

    int deleteKeyChunkIsDone = 0;
    while (deleteKeyChunkIsDone == 0) {
      deleteKeyChunkIsDone = deleteKeyChunk();
    }
    int deleteKeyRecordIsDone = 0;
    while (deleteKeyRecordIsDone == 0) {
      deleteKeyRecordIsDone = deleteKeyRecord();
    }
    return getNumberOfKeys();
  }

  private int finishDeleteKeyFromKeyChainAfterInterruption() throws Exception {
    TonWalletAppletStates appletState = getTonAppletState();
    if (appletState != TonWalletAppletStates.DELETE_KEY_FROM_KEYCHAIN_MODE)
      throw new Exception(ERROR_MSG_APPLET_DOES_NOT_WAIT_TO_DELETE_KEY + appletState.getDescription() + ".");

    reselectKeyForHmac();

    int deleteKeyChunkIsDone = 0;
    while (deleteKeyChunkIsDone == 0) {
      deleteKeyChunkIsDone = deleteKeyChunk();
    }
    int deleteKeyRecordIsDone = 0;
    while (deleteKeyRecordIsDone == 0) {
      deleteKeyRecordIsDone = deleteKeyRecord();
    }
    return getNumberOfKeys();
  }

  private void initiateDeleteOfKey(byte[] index) throws Exception {
    TonWalletAppletStates appletState = getTonAppletState();
    if (appletState !=  TonWalletAppletStates.PERSONALIZED)
      throw new Exception(ERROR_MSG_APPLET_IS_NOT_PERSONALIZED + appletState.getDescription() + ".");
    reselectKeyForHmac();
    byte[] sault = getSaultBytes();
    RAPDU rapdu = apduRunner.sendAPDU(getInitiateDeleteOfKeyAPDU(index, sault));
    if (rapdu == null || rapdu.getData() == null || rapdu.getData().length != INITIATE_DELETE_KEY_LE)
      throw new Exception(ERROR_MSG_INITIATE_DELETE_KEY_RESPONSE_LEN_INCORRECT);
    rapdu.getData();
  }

  private int deleteKeyChunk() throws Exception {
    byte[] sault = getSaultBytes();
    RAPDU rapdu = apduRunner.sendAPDU(getDeleteKeyChunkAPDU(sault));
    if (rapdu == null || rapdu.getData() == null || rapdu.getData().length != DELETE_KEY_CHUNK_LE)
      throw new Exception(ERROR_MSG_DELETE_KEY_CHUNK_RESPONSE_LEN_INCORRECT);
    byte[] response = rapdu.getData();
    int status = response[0];
    if (status < 0 || status > 2)
      throw new Exception(ERROR_MSG_DELETE_KEY_CHUNK_RESPONSE_INCORRECT);
    return status;
  }

  private int deleteKeyRecord() throws Exception {
    byte[] sault = getSaultBytes();
    RAPDU rapdu = apduRunner.sendAPDU(getDeleteKeyRecordAPDU(sault));
    if (rapdu == null || rapdu.getData() == null || rapdu.getData().length != DELETE_KEY_RECORD_LE)
      throw new Exception(ERROR_MSG_DELETE_KEY_RECORD_RESPONSE_LEN_INCORRECT);
    byte[] response = rapdu.getData();
    int status = response[0];
    if (status < 0 || status > 2)
      throw new Exception(ERROR_MSG_DELETE_KEY_RECORD_RESPONSE_INCORRECT);
    return status;
  }

  private int getDeleteKeyChunkNumOfPackets() throws Exception {
    TonWalletAppletStates appletState = getTonAppletState();
    if (appletState != TonWalletAppletStates.DELETE_KEY_FROM_KEYCHAIN_MODE)
      throw new Exception(ERROR_MSG_APPLET_DOES_NOT_WAIT_TO_DELETE_KEY + appletState.getDescription() + ".");
    reselectKeyForHmac();
    byte[] sault = getSaultBytes();
    RAPDU rapdu = apduRunner.sendAPDU(getDeleteKeyChunkNumOfPacketsAPDU(sault));
    if (rapdu == null || rapdu.getData() == null || rapdu.getData().length  != GET_DELETE_KEY_CHUNK_NUM_OF_PACKETS_LE)
      throw new Exception(ERROR_MSG_GET_DELETE_KEY_CHUNK_NUM_OF_PACKETS_RESPONSE_LEN_INCORRECT);
    byte[] response = rapdu.getData();
    short num = BYTE_ARR_HELPER.makeShort(response, 0);
    if (num < 0)
      throw new Exception(ERROR_MSG_GET_DELETE_KEY_CHUNK_NUM_OF_PACKETS_RESPONSE_INCORRECT);
    return num;
  }

  private int getDeleteKeyRecordNumOfPackets() throws Exception {
    TonWalletAppletStates appletState = getTonAppletState();
    if (appletState != TonWalletAppletStates.DELETE_KEY_FROM_KEYCHAIN_MODE)
      throw new Exception(ERROR_MSG_APPLET_DOES_NOT_WAIT_TO_DELETE_KEY + appletState.getDescription() + ".");
    reselectKeyForHmac();
    byte[] sault = getSaultBytes();
    RAPDU rapdu = apduRunner.sendAPDU(getDeleteKeyRecordNumOfPacketsAPDU(sault));
    if (rapdu == null || rapdu.getData() == null || rapdu.getData().length != GET_DELETE_KEY_RECORD_NUM_OF_PACKETS_LE)
      throw new Exception(ERROR_MSG_GET_DELETE_KEY_RECORD_NUM_OF_PACKETS_RESPONSE_LEN_INCORRECT);
    byte[] response = rapdu.getData();
    short num = BYTE_ARR_HELPER.makeShort(response, 0);
    if (num < 0)
      throw new Exception(ERROR_MSG_GET_DELETE_KEY_RECORD_NUM_OF_PACKETS_RESPONSE_INCORRECT);
    return num;
  }

  private int getOccupiedStorageSize() throws Exception {
    reselectKeyForHmac();
    byte[] sault = getSaultBytes();
    RAPDU rapdu = apduRunner.sendAPDU(getGetOccupiedSizeAPDU(sault));
    if (rapdu == null || rapdu.getData() == null || rapdu.getData().length != GET_OCCUPIED_SIZE_LE)
      throw new Exception(ERROR_MSG_GET_OCCUPIED_SIZE_RESPONSE_LEN_INCORRECT);
    byte[] response = rapdu.getData();
    short size = BYTE_ARR_HELPER.makeShort(response, 0);
    if (size < 0)
      throw new Exception(ERROR_MSG_OCCUPIED_SIZE_RESPONSE_INCORRECT);
    return size;
  }

  private int getFreeStorageSize() throws Exception {
    reselectKeyForHmac();
    byte[] sault = getSaultBytes();
    RAPDU rapdu = apduRunner.sendAPDU(getGetFreeSizeAPDU(sault));
    if (rapdu == null || rapdu.getData() == null || rapdu.getData().length != GET_FREE_SIZE_LE)
      throw new Exception(ERROR_MSG_GET_FREE_SIZE_RESPONSE_LEN_INCORRECT);
    byte[] response = rapdu.getData();
    short size = BYTE_ARR_HELPER.makeShort(response, 0);
    if (size < 0)
      throw new Exception(ERROR_MSG_FREE_SIZE_RESPONSE_INCORRECT);
    return size;
  }

  private byte[] getHmac(byte[] ind) throws Exception {
    reselectKeyForHmac();
    byte[] sault = getSaultBytes();
    RAPDU rapdu =  apduRunner.sendAPDU(getGetHmacAPDU(ind, sault));
    if (rapdu == null || rapdu.getData() == null || rapdu.getData().length != (HMAC_SHA_SIG_SIZE + 2))
      throw new Exception(ERROR_MSG_GET_HMAC_RESPONSE_LEN_INCORRECT);
    return rapdu.getData();
  }

  private byte[] getKeyFromKeyChain(byte[] macBytes) throws Exception {
    JSONObject jsonObject = getIndexAndLenOfKeyInKeyChain(macBytes);
    int keyLen = jsonObject.getInt(KEY_LENGTH_FIELD);
    byte[] ind = new byte[2];
    short indVal = (short) jsonObject.getInt(KEY_INDEX_FIELD);
    BYTE_ARR_HELPER.setShort(ind, 0, indVal);
    return getKeyFromKeyChain(keyLen, ind);
  }

  private byte[] getKeyFromKeyChain(int keyLen, byte[] ind) throws Exception {
    byte[] key = new byte[keyLen];
    byte[] sault;
    int numberOfPackets = keyLen / DATA_PORTION_MAX_SIZE;
    short startPos = 0;
    for (int i = 0; i < numberOfPackets; i++) {
      sault = getSaultBytes();
      RAPDU rapdu = apduRunner.sendAPDU(getGetKeyChunkAPDU(ind, startPos, sault, (byte) DATA_PORTION_MAX_SIZE));
      if (rapdu == null || rapdu.getData() == null || rapdu.getData().length != DATA_PORTION_MAX_SIZE) throw new Exception(ERROR_KEY_DATA_PORTION_INCORRECT_LEN + DATA_PORTION_MAX_SIZE);
      byte[] res = rapdu.getData();
      BYTE_ARR_HELPER.arrayCopy(res, 0, key, startPos, DATA_PORTION_MAX_SIZE);
      startPos += DATA_PORTION_MAX_SIZE;
    }
    int tailLen = keyLen % DATA_PORTION_MAX_SIZE;
    if (tailLen > 0) {
      sault = getSaultBytes();
      RAPDU rapdu = apduRunner.sendAPDU(getGetKeyChunkAPDU(ind, startPos, sault, (byte) tailLen));
      if (rapdu == null || rapdu.getData() == null || rapdu.getData().length != tailLen) throw new Exception(ERROR_KEY_DATA_PORTION_INCORRECT_LEN + tailLen);
      byte[] res = rapdu.getData();
      BYTE_ARR_HELPER.arrayCopy(res, 0, key, startPos, tailLen);
    }
    return key;
  }

  private void addKey(byte[] keyBytes) throws Exception {
    int oldNumOfKeys = getNumberOfKeys();
    int newNumberOfKeys = sendKey(keyBytes, INS_ADD_KEY_CHUNK);
    if (newNumberOfKeys != (oldNumOfKeys + 1))
      throw new Exception(ERROR_MSG_NUM_OF_KEYS_INCORRECT_AFTER_ADD);
  }

  private void changeKey(byte[] keyBytes) throws Exception {
    int oldNumOfKeys = getNumberOfKeys();
    int newNumberOfKeys = sendKey(keyBytes, INS_CHANGE_KEY_CHUNK);
    if (oldNumOfKeys != newNumberOfKeys)
      throw new Exception(ERROR_MSG_NUM_OF_KEYS_INCORRECT_AFTER_CHANGE);
  }

  private int sendKey(byte[] keyBytes, byte ins) throws Exception {
    int numberOfPackets = keyBytes.length / DATA_PORTION_MAX_SIZE;
    byte[] keyChunk, sault;
    for (int i = 0; i < numberOfPackets; i++) {
      sault = getSaultBytes();
      keyChunk = BYTE_ARR_HELPER.bSub(keyBytes, i * DATA_PORTION_MAX_SIZE, DATA_PORTION_MAX_SIZE);
      apduRunner.sendAPDU(getSendKeyChunkAPDU(ins, i == 0 ? (byte) 0x00 : (byte) 0x01, keyChunk, sault));
    }

    int tailLen = keyBytes.length % DATA_PORTION_MAX_SIZE;
    if (tailLen > 0) {
      sault = getSaultBytes();
      keyChunk = BYTE_ARR_HELPER.bSub(keyBytes, numberOfPackets * DATA_PORTION_MAX_SIZE, tailLen);
      apduRunner.sendAPDU(getSendKeyChunkAPDU(ins, numberOfPackets == 0 ? (byte) 0x00 : (byte) 0x01, keyChunk, sault));
    }

    byte[] mac = HMAC_HELPER.computeMac(keyBytes);
    sault = getSaultBytes();
    RAPDU rapdu = apduRunner.sendAPDU(getSendKeyChunkAPDU(ins, (byte) 0x02, mac, sault));

    if (rapdu == null || rapdu.getData() == null || rapdu.getData().length != SEND_CHUNK_LE)
      throw new Exception(ERROR_MSG_SEND_CHUNK_RESPONSE_LEN_INCORRECT);
    byte[] response = rapdu.getData();
    short numOfKeys = BYTE_ARR_HELPER.makeShort(response, 0);
    if (numOfKeys <= 0 || numOfKeys > MAX_NUMBER_OF_KEYS_IN_KEYCHAIN)
      throw new Exception(ERROR_MSG_NUMBER_OF_KEYS_RESPONSE_INCORRECT);
    return numOfKeys;
  }

  private String addKeyIntoKeyChain(byte[] keyBytes) throws Exception {
    checkAvailableVolForNewKey((short) keyBytes.length);
    addKey(keyBytes);
    return BYTE_ARR_HELPER.hex(HMAC_HELPER.computeMac(keyBytes));
  }

  private String changeKeyInKeyChain(byte[] newKeyBytes, byte[] macBytesOfOldKey) throws Exception {
    JSONObject jsonObject = getIndexAndLenOfKeyInKeyChain(macBytesOfOldKey);
    int keyLen = jsonObject.getInt(KEY_LENGTH_FIELD);
    if (keyLen != newKeyBytes.length)
      throw new IllegalArgumentException(ERROR_MSG_NEW_KEY_LEN_INCORRECT + keyLen + ".");
    byte[] ind = new byte[2];
    short indVal = (short) jsonObject.getInt(KEY_INDEX_FIELD);
    BYTE_ARR_HELPER.setShort(ind, 0, indVal);
    initiateChangeOfKey(ind);
    changeKey(newKeyBytes);
    return BYTE_ARR_HELPER.hex(HMAC_HELPER.computeMac(newKeyBytes));
  }

  private Map<String, Short> getAllHmacsOfKeysFromCard() throws Exception {
    Map<String, Short> hmacs = new LinkedHashMap<>();
    int numOfKeys = getNumberOfKeys();
    byte[] ind = new byte[2];
    for (short i = 0; i < numOfKeys; i++) {
      BYTE_ARR_HELPER.setShort(ind, 0, i);
      byte[] data = getHmac(ind);
      byte[] mac = BYTE_ARR_HELPER.bSub(data, 0, HMAC_SHA_SIG_SIZE);
      short len = BYTE_ARR_HELPER.makeShort(data, HMAC_SHA_SIG_SIZE);
      hmacs.put(BYTE_ARR_HELPER.hex(mac), len);
    }
    return hmacs;
  }

}
