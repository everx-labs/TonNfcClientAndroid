package com.tonnfccard;

import android.content.Context;
import android.content.Intent;
import android.security.keystore.KeyProperties;
import android.security.keystore.KeyProtection;
import android.util.Log;

import com.tonnfccard.callback.NfcCallback;
import com.tonnfccard.helpers.CardApiInterface;
import com.tonnfccard.helpers.ExceptionHelper;
import com.tonnfccard.helpers.JsonHelper;
import com.tonnfccard.helpers.StringHelper;
import com.tonnfccard.nfc.NfcApduRunner;
import com.tonnfccard.smartcard.TonWalletAppletStates;
import com.tonnfccard.helpers.HmacHelper;
import com.tonnfccard.smartcard.RAPDU;
import com.tonnfccard.utils.ByteArrayUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.security.KeyStore;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import static com.tonnfccard.TonWalletConstants.*;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_COMMON_SECRET_LEN_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_COMMON_SECRET_NOT_HEX;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_GET_SERIAL_NUMBER_RESPONSE_LEN_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_KEY_FOR_HMAC_DOES_NOT_EXIST_IN_ANDROID_KEYSTORE;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_PASSWORD_LEN_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_PASSWORD_NOT_HEX;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_SAULT_RESPONSE_LEN_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_SERIAL_NUMBER_LEN_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_SERIAL_NUMBER_NOT_NUMERIC;
import static com.tonnfccard.TonWalletConstants.EMPTY_SERIAL_NUMBER;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_STATE_RESPONSE_LEN_INCORRECT;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.GET_APP_INFO_APDU;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.GET_SAULT_APDU;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.GET_SERIAL_NUMBER_APDU;

/** Basic class containing functions-wrappers for some simple basic card operations: getSault, getTonAppletState, getSerialNumber.
 *  And also it contains all necessary functions to maintain keys living in Android keystore for HMAC-SHA256 signature.
 */
public class TonWalletApi {
  private static final String TAG = "TonWalletApi";
  protected static final StringHelper STR_HELPER = StringHelper.getInstance();
  protected static final JsonHelper JSON_HELPER = JsonHelper.getInstance();
  protected static final ByteArrayUtil BYTE_ARR_HELPER = ByteArrayUtil.getInstance();
  protected static final ExceptionHelper EXCEPTION_HELPER = ExceptionHelper.getInstance();

  protected static HmacHelper HMAC_HELPER = HmacHelper.getInstance();

  static MessageDigest digest;
  static String currentSerialNumber = EMPTY_SERIAL_NUMBER;

  public void setApduRunner(NfcApduRunner apduRunner) {
    this.apduRunner = apduRunner;
  }

  public static Context getActivity() {
    return activity;
  }

  public static void setActivity(Context activity) {
    TonWalletApi.activity = activity;
  }

  protected static Context activity;

  protected NfcApduRunner apduRunner;

  public NfcApduRunner getApduRunner() {
    return apduRunner;
  }

  static {
    try {
      digest = MessageDigest.getInstance("SHA-256");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  TonWalletApi(Context activity, NfcApduRunner apduRunner) {
    TonWalletApi.activity = activity;
    this.apduRunner = apduRunner;
  }

  TonWalletApi(NfcApduRunner apduRunner) {
    this.apduRunner = apduRunner;
  }

  public static void setHmacHelper(HmacHelper hmacHelper) {
    HMAC_HELPER = hmacHelper;
  }

  public boolean setCardTag(Intent intent) throws Exception {
    return apduRunner.setCardTag(intent);
  }

  /**
   * @param callback
   * Breaks NFC connection.
   */
  public void disconnectCard(final NfcCallback callback) {
    new Thread(new Runnable() {
      public void run() {
        try {
          String json = disconnectCardAndGetJson();
          resolveJson(json, callback);
          Log.d(TAG, "disconnectCard response : " + json);
        } catch (Exception e) {
            EXCEPTION_HELPER.handleException(e, callback, TAG);
        }
      }
    }).start();
  }

  /**
   * @return
   * @throws Exception
   * Breaks NFC connection.
   */
  public String disconnectCardAndGetJson()  throws Exception {
    try {
      apduRunner.disconnectCard();
      return JSON_HELPER.createResponseJson(DONE_MSG);
    }
    catch (Exception e) {
      throw new Exception(EXCEPTION_HELPER.makeFinalErrMsg(e), e);
    }
  }

  /**
   * @param callback
   * This function returns serial number (SN). It must be identical to SN printed on the card.
   */
  private final CardApiInterface<List<String>> getSerialNumber = list -> this.getSerialNumberAndGetJson();

  public void getSerialNumber(final NfcCallback callback, Boolean... showDialog) {
    boolean showDialogFlag = showDialog.length > 0 ? showDialog[0] : false;
    CardTask cardTask = new CardTask(this, callback,  Collections.emptyList(), getSerialNumber, showDialogFlag);
    cardTask.execute();
  }

  /**
   * @return
   * @throws Exception
   * This function returns serial number (SN). It must be identical to SN printed on the card.
   */
  public String getSerialNumberAndGetJson() throws Exception {
    try {
      //long start = System.currentTimeMillis();
      String response = STR_HELPER.makeDigitalString(getSerialNumber());
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
   * This function returns state of TON Labs wallet applet.
   */
  private final CardApiInterface<List<String>> getTonAppletState = list -> this.getTonAppletStateAndGetJson();

  public void getTonAppletState(final NfcCallback callback, Boolean... showDialog) {
    boolean showDialogFlag = showDialog.length > 0 ? showDialog[0] : false;
    CardTask cardTask = new CardTask(this, callback,  Collections.emptyList(), getTonAppletState, showDialogFlag);
    cardTask.execute();
  }

  /**
   * @return
   * @throws Exception
   * This function returns state of TON Labs wallet applet.
   */
  public String getTonAppletStateAndGetJson() throws Exception {
    try {
      //long start = System.currentTimeMillis();
      TonWalletAppletStates state = getTonAppletState();
      String json = JSON_HELPER.createResponseJson(state.getDescription());
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
   * This function returns fresh 32 bytes sault generated by the card.
   */
  private final CardApiInterface<List<String>> getSault = list -> this.getSaultAndGetJson();

  public void getSault(final NfcCallback callback, Boolean... showDialog) {
    boolean showDialogFlag = showDialog.length > 0 ? showDialog[0] : false;
    CardTask cardTask = new CardTask(this, callback,  Collections.emptyList(), getSault, showDialogFlag);
    cardTask.execute();
  }

  /**
   * @return
   * @throws Exception
   * This function returns fresh 32 bytes sault generated by the card.
   */
  public String getSaultAndGetJson() throws Exception {
    try {
      //long start = System.currentTimeMillis();
      String json = JSON_HELPER.createResponseJson(getSaultHex());
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
   * Get the list of card serial numbers for which we have keys in Android keystore.
   */
  public void getAllSerialNumbers(final NfcCallback callback) {
    new Thread(new Runnable() {
      public void run() {
        try {
          String json = getAllSerialNumbersAndGetJson();
          resolveJson(json, callback);
          Log.d(TAG, "getAllSerialNumbers response : " + json);
        } catch (Exception e) {
            EXCEPTION_HELPER.handleException(e, callback, TAG);
        }
      }
    }).start();
  }

  /**
   * @return
   * @throws Exception
   * Get the list of card serial numbers for which we have keys in Android keystore.
   */
  public String getAllSerialNumbersAndGetJson() throws Exception {
    try {
      List<String> allSerialNumbers = getAllSerialNumbers();
      if (allSerialNumbers.isEmpty()) {
        return JSON_HELPER.createResponseJson(HMAC_KEYS_ARE_NOT_FOUND_MSG);
      } else {
        JSONObject allAliasesObj = new JSONObject();
        JSONArray jArray = new JSONArray();
        for (final String sn : allSerialNumbers) {
          jArray.put(sn);
        }
        allAliasesObj.put(MESSAGE_FIELD, jArray);
        allAliasesObj.put(STATUS_FIELD, SUCCESS_STATUS);
        return allAliasesObj.toString();
      }
    }
    catch (Exception e) {
      throw new Exception(EXCEPTION_HELPER.makeFinalErrMsg(e), e);
    }
  }

  /**
   * @param serialNumber
   * @param callback
   * Delete key for given serialNumber from Android keystore.
   */
  public void deleteKeyForHmac(final String serialNumber, final NfcCallback callback) {
    new Thread(new Runnable() {
      public void run() {
        try {
          String json =  deleteKeyForHmacAndGetJson(serialNumber);
          resolveJson(json, callback);
          Log.d(TAG, "deleteKeyForHmac response : " + json);
        } catch (Exception e) {
            EXCEPTION_HELPER.handleException(e, callback, TAG);
        }
      }
    }).start();
  }

  /**
   * @param serialNumber
   * @return
   * @throws Exception
   * Delete key for given serialNumber from Android keystore.
   */
  public String deleteKeyForHmacAndGetJson(final String serialNumber)  throws Exception {
    try {
      if (!STR_HELPER.isNumericString(serialNumber))
        throw new Exception(ERROR_MSG_SERIAL_NUMBER_NOT_NUMERIC);
      if (serialNumber.length() != SERIAL_NUMBER_SIZE)
        throw new Exception(ERROR_MSG_SERIAL_NUMBER_LEN_INCORRECT);
      deleteKeyForHmac(serialNumber);
      return JSON_HELPER.createResponseJson(DONE_MSG);
    }
    catch (Exception e) {
      throw new Exception(EXCEPTION_HELPER.makeFinalErrMsg(e), e);
    }
  }

  /**
   * @param password
   * @param commonSecret
   * @param serialNumber
   * @param callback
   * If you reinstalled app and lost HMAC SHA256 symmetric key for the card from your Android keystore, then create the key for your card using this function.
   */
  public void createKeyForHmac(final String password, final String commonSecret, final String serialNumber, final NfcCallback callback) {
    new Thread(new Runnable() {
      public void run() {
        try {
          String json = createKeyForHmacAndGetJson(password, commonSecret, serialNumber);
          resolveJson(json, callback);
          Log.d(TAG, "createKeyForHmac response : " + json);
        } catch (Exception e) {
            EXCEPTION_HELPER.handleException(e, callback, TAG);
        }
      }
    }).start();
  }

  /**
   * @param password
   * @param commonSecret
   * @param serialNumber
   * @return
   * @throws Exception
   * If you reinstalled app and lost HMAC SHA256 symmetric key for the card from your Android keystore, then create the key for your card using this function.
   */
  public String createKeyForHmacAndGetJson(final String password, final String commonSecret, final String serialNumber) throws Exception {
    try {
      if (!STR_HELPER.isHexString(password))
        throw new Exception(ERROR_MSG_PASSWORD_NOT_HEX);
      if (password.length() != 2 * PASSWORD_SIZE)
        throw new Exception(ERROR_MSG_PASSWORD_LEN_INCORRECT);
      if (!STR_HELPER.isHexString(commonSecret))
        throw new Exception(ERROR_MSG_COMMON_SECRET_NOT_HEX);
      if (commonSecret.length() != 2 * COMMON_SECRET_SIZE)
        throw new Exception(ERROR_MSG_COMMON_SECRET_LEN_INCORRECT);
      if (!STR_HELPER.isNumericString(serialNumber))
        throw new Exception(ERROR_MSG_SERIAL_NUMBER_NOT_NUMERIC);
      if (serialNumber.length() != SERIAL_NUMBER_SIZE)
        throw new Exception(ERROR_MSG_SERIAL_NUMBER_LEN_INCORRECT);
      createKeyForHmac(BYTE_ARR_HELPER.bytes(password), BYTE_ARR_HELPER.bytes(commonSecret), serialNumber);
      return JSON_HELPER.createResponseJson(DONE_MSG);
    }
    catch (Exception e) {
      throw new Exception(EXCEPTION_HELPER.makeFinalErrMsg(e), e);
    }
  }

  /**
   * @param serialNumber
   * @param callback
   * Check if key for given serialNumber exists in Android keystore.
   */
  public void isKeyForHmacExist(final String serialNumber, final NfcCallback callback) {
    new Thread(new Runnable() {
      public void run() {
        try {
          String json =  isKeyForHmacExistAndGetJson(serialNumber);
          resolveJson(json, callback);
          Log.d(TAG, "isKeyForHmacExist response : " + json);
        } catch (Exception e) {
            EXCEPTION_HELPER.handleException(e, callback, TAG);
        }
      }
    }).start();
  }

  /**
   * @param serialNumber
   * @return
   * @throws Exception
   * Check if key for given serialNumber exists in Android keystore.
   */
  public String isKeyForHmacExistAndGetJson(final String serialNumber) throws Exception {
    try {
      if (!STR_HELPER.isNumericString(serialNumber))
        throw new Exception(ERROR_MSG_SERIAL_NUMBER_NOT_NUMERIC);
      if (serialNumber.length() != SERIAL_NUMBER_SIZE)
        throw new Exception(ERROR_MSG_SERIAL_NUMBER_LEN_INCORRECT);
      boolean res = isKeyForHmacExist(serialNumber);
      return JSON_HELPER.createResponseJson(res ? TRUE_MSG : FALSE_MSG);
    }
    catch (Exception e) {
      throw new Exception(EXCEPTION_HELPER.makeFinalErrMsg(e), e);
    }
  }

  /**
   * @param serialNumber
   * @param callback
   * Manually select new active card (it selects the serial number and correspondingly choose the appropriate key HMAC SHA256 from Android Keystore).
   */
  public void selectKeyForHmac(final String serialNumber, final NfcCallback callback) {
    new Thread(new Runnable() {
      public void run() {
        try {
          String json = selectKeyForHmacAndGetJson(serialNumber);
          resolveJson(json, callback);
          Log.d(TAG, "selectKeyForHmac response : " + json);
        } catch (Exception e) {
            EXCEPTION_HELPER.handleException(e, callback, TAG);
        }
      }
    }).start();
  }

  /**
   * @param serialNumber
   * @return
   * @throws Exception
   * Manually select new active card (it selects the serial number and correspondingly choose the appropriate key HMAC SHA256 from Android Keystore).
   */
  public String selectKeyForHmacAndGetJson(final String serialNumber) throws Exception {
    try {
      if (!STR_HELPER.isNumericString(serialNumber))
        throw new Exception(ERROR_MSG_SERIAL_NUMBER_NOT_NUMERIC);
      if (serialNumber.length() != SERIAL_NUMBER_SIZE)
        throw new Exception(ERROR_MSG_SERIAL_NUMBER_LEN_INCORRECT);
      selectKeyForHmac(serialNumber);
      return JSON_HELPER.createResponseJson(DONE_MSG);
    }
    catch (Exception e) {
      throw new Exception(EXCEPTION_HELPER.makeFinalErrMsg(e), e);
    }
  }

  /**
   * @param callback
   * Get serial number of currently active key (card). In fact this is a serialNumber of the card with which your app communicated last time.
   */
  public void getCurrentSerialNumber(final NfcCallback callback) {
    new Thread(new Runnable() {
      public void run() {
        try {
          String json = getCurrentSerialNumberAndGetJson();
          resolveJson(json, callback);
          Log.d(TAG, "getCurrentSerialNumber response : " + json);
        } catch (Exception e) {
            EXCEPTION_HELPER.handleException(e, callback, TAG);
        }
      }
    }).start();
  }

  /**
   * @return
   * @throws Exception
   * Get serial number of currently active key (card). In fact this is a serialNumber of the card with which your app communicated last time.
   */
  public String getCurrentSerialNumberAndGetJson() throws Exception {
    return JSON_HELPER.createResponseJson(currentSerialNumber);
  }

  public void setCurrentSerialNumber(final String currentSerialNumber) {
    TonWalletApi.currentSerialNumber = currentSerialNumber;
    HMAC_HELPER.setCurrentSerialNumber(currentSerialNumber);
  }

  void resolveJson(String json, NfcCallback callback){
    callback.getResolve().resolve(json);
    Log.d(TAG, "json = " + json);
  }

  void createKeyForHmac(byte[] password, byte[] commonSecret, String serialNumber) throws Exception {
    byte[] key = HMAC_HELPER.computeMac(digest.digest(password), commonSecret);
    final SecretKey hmacSha256Key = new SecretKeySpec(key, 0, key.length, KeyProperties.KEY_ALGORITHM_HMAC_SHA256);
    final KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
    String keyAlias = HmacHelper.HMAC_KEY_ALIAS + serialNumber;
    Log.d(TAG, "keyAlias = " + keyAlias);
    keyStore.load(null);
    if (keyStore.containsAlias(keyAlias)) keyStore.deleteEntry(keyAlias);
    keyStore.setEntry(keyAlias,
      new KeyStore.SecretKeyEntry(hmacSha256Key),
      new KeyProtection.Builder(KeyProperties.PURPOSE_SIGN)
        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
        .build());
    setCurrentSerialNumber(serialNumber);
  }

  void reselectKeyForHmac() throws Exception {
    String serialNumber = STR_HELPER.makeDigitalString(getSerialNumber());
    selectKeyForHmac(serialNumber);
  }

  private void deleteKeyForHmac(String serialNumber) throws Exception {
    final KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
    keyStore.load(null);
    String keyAlias = HmacHelper.HMAC_KEY_ALIAS + serialNumber;
    Log.d(TAG, "delete keyAlias  = " + keyAlias);
    if (keyStore.containsAlias(keyAlias))  {
      keyStore.deleteEntry(keyAlias);
      if (currentSerialNumber.equals(serialNumber)) {
        setCurrentSerialNumber(EMPTY_SERIAL_NUMBER);
      }
    }
    else throw new Exception(ERROR_MSG_KEY_FOR_HMAC_DOES_NOT_EXIST_IN_ANDROID_KEYSTORE);
  }

  private void selectKeyForHmac(String serialNumber) throws Exception {
    final KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
    keyStore.load(null);
    String keyAlias = HmacHelper.HMAC_KEY_ALIAS + serialNumber;
    if (keyStore.containsAlias(keyAlias))  {
      setCurrentSerialNumber(serialNumber);
    }
    else throw new Exception(ERROR_MSG_KEY_FOR_HMAC_DOES_NOT_EXIST_IN_ANDROID_KEYSTORE);
  }

  private boolean isKeyForHmacExist(String serialNumber) throws Exception {
    final KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
    keyStore.load(null);
    String keyAlias = HmacHelper.HMAC_KEY_ALIAS + serialNumber;
    return keyStore.containsAlias(keyAlias);
  }

  private List<String> getAllSerialNumbers() throws Exception {
    final KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
    keyStore.load(null);
    Enumeration<String> aliases = keyStore.aliases();
    List<String> serialNumbers = new ArrayList<>();
    while (aliases.hasMoreElements()) {
      String alias = aliases.nextElement();
      if (alias.startsWith(HmacHelper.HMAC_KEY_ALIAS))
        serialNumbers.add(alias.substring(HmacHelper.HMAC_KEY_ALIAS.length()));
    }
    return serialNumbers;
  }

  private String getSaultHex() throws Exception {
    return BYTE_ARR_HELPER.hex(selectTonWalletAppletAndGetSaultBytes());
  }

  TonWalletAppletStates getTonAppletState() throws Exception {
    RAPDU rapdu = apduRunner.sendTonWalletAppletAPDU(GET_APP_INFO_APDU);
    if (rapdu == null || rapdu.getData() == null || rapdu.getData().length != 0x01) throw new Exception(ERROR_MSG_STATE_RESPONSE_LEN_INCORRECT);
    return TonWalletAppletStates.findByStateValue(rapdu.getData()[0]);
  }

  byte[] getSerialNumber() throws Exception {
    RAPDU rapdu = apduRunner.sendTonWalletAppletAPDU(GET_SERIAL_NUMBER_APDU);
    if (rapdu == null || rapdu.getData() == null || rapdu.getData().length != SERIAL_NUMBER_SIZE) throw new Exception(ERROR_MSG_GET_SERIAL_NUMBER_RESPONSE_LEN_INCORRECT);
    return rapdu.getData();
  }

  byte[] getSaultBytes() throws Exception {
    RAPDU rapdu = getSault();
    if (rapdu == null || rapdu.getData() == null || rapdu.getData().length != SAULT_LENGTH) throw new Exception(ERROR_MSG_SAULT_RESPONSE_LEN_INCORRECT);
    return rapdu.getData();
  }

  private byte[] selectTonWalletAppletAndGetSaultBytes() throws Exception {
    RAPDU rapdu = selectTonWalletAppletAndGetSault();
    if (rapdu == null || rapdu.getData() == null || rapdu.getData().length != SAULT_LENGTH) throw new Exception(ERROR_MSG_SAULT_RESPONSE_LEN_INCORRECT);
    return rapdu.getData();
  }

  private RAPDU getSault() throws Exception {
    return apduRunner.sendAPDU(GET_SAULT_APDU);
  }

  private RAPDU selectTonWalletAppletAndGetSault() throws Exception {
    return apduRunner.sendTonWalletAppletAPDU(GET_SAULT_APDU);
  }
}
