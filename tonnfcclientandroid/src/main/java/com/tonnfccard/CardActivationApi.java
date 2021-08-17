package com.tonnfccard;

import android.content.Context;

import com.tonnfccard.callback.NfcCallback;
import com.tonnfccard.helpers.CardApiInterface;
import com.tonnfccard.nfc.NfcApduRunner;
import com.tonnfccard.smartcard.TonWalletAppletStates;
import com.tonnfccard.smartcard.RAPDU;

import org.json.JSONObject;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import static com.tonnfccard.TonWalletConstants.*;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_APPLET_DOES_NOT_WAIT_AUTHENTICATION;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_APPLET_IS_NOT_PERSONALIZED;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_COMMON_SECRET_LEN_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_COMMON_SECRET_NOT_HEX;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_HASH_OF_ENCRYPTED_COMMON_SECRET_RESPONSE_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_HASH_OF_ENCRYPTED_COMMON_SECRET_RESPONSE_LEN_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_HASH_OF_ENCRYPTED_PASSWORD_RESPONSE_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_HASH_OF_ENCRYPTED_PASSWORD_RESPONSE_LEN_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_INITIAL_VECTOR_LEN_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_INITIAL_VECTOR_NOT_HEX;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_PASSWORD_LEN_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_PASSWORD_NOT_HEX;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_PIN_FORMAT_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_PIN_LEN_INCORRECT;
import static com.tonnfccard.smartcard.CoinManagerApduCommands.GET_ROOT_KEY_STATUS_APDU;
import static com.tonnfccard.smartcard.CoinManagerApduCommands.POSITIVE_ROOT_KEY_STATUS;
import static com.tonnfccard.smartcard.CoinManagerApduCommands.RESET_WALLET_APDU;
import static com.tonnfccard.smartcard.CoinManagerApduCommands.getChangePinAPDU;
import static com.tonnfccard.smartcard.CoinManagerApduCommands.getGenerateSeedAPDU;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.GET_HASH_OF_ENCRYPTED_COMMON_SECRET_APDU;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.GET_HASH_OF_ENCRYPTED_PASSWORD_APDU;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.getVerifyPasswordAPDU;


/**
 * Class containing functions-wrappers for card operations related to card activation process: turnOnWallet, getHashOfEncryptedCommonSecret, getHashOfEncryptedPassword.
 * Use them to make security card activation.
 */

public final class CardActivationApi extends TonWalletApi {
  private static final String TAG = "CardActivationNfcApi";


  public CardActivationApi(NfcApduRunner apduRunner) {
    super(apduRunner);
  }

  public CardActivationApi(Context activity, NfcApduRunner apduRunner) {
    super(activity, apduRunner);
  }

  /**
   * @param newPin
   * @param password
   * @param commonSecret
   * @param initialVector
   * @param callback
   * This function makes TON Labs wallet applet activation.
   * Use here activation data tuple (password, commonSecret, initialVector) that is correct for your card, i.e. corresponds to your serialNumber.
   */
  private final CardApiInterface<List<String>> turnOnWallet = list ->  this.turnOnWalletAndGetJson(list.get(0), list.get(1), list.get(2), list.get(3));

  public void turnOnWallet(final String newPin, final String password, final String commonSecret, final String initialVector, final NfcCallback callback, Boolean... showDialog) {
    boolean showDialogFlag = showDialog.length > 0 ? showDialog[0] : false;
    CardTask cardTask = new CardTask(this, callback, Arrays.asList(newPin, password, commonSecret, initialVector), turnOnWallet, showDialogFlag);
    cardTask.execute();
  }

  /**
   *
   * @param newPin
   * @param password
   * @param commonSecret
   * @param initialVector
   * @return
   * @throws Exception
   * This function makes TON Labs wallet applet activation.
   * Use here activation data tuple (password, commonSecret, initialVector) that is correct for your card, i.e. corresponds to your serialNumber.
   *
   */

  public String turnOnWalletAndGetJson(final String newPin, final String password, final String commonSecret, final String initialVector) throws Exception {
    try {
      //long start = System.currentTimeMillis();
      if (!STR_HELPER.isHexString(password))
        throw new Exception(ERROR_MSG_PASSWORD_NOT_HEX);
      if (password.length() != 2 * PASSWORD_SIZE)
        throw new Exception(ERROR_MSG_PASSWORD_LEN_INCORRECT);
      if (!STR_HELPER.isNumericString(newPin))
        throw new Exception(ERROR_MSG_PIN_FORMAT_INCORRECT);
      if (newPin.length() != PIN_SIZE)
        throw new Exception(ERROR_MSG_PIN_LEN_INCORRECT);
      if (!STR_HELPER.isHexString(commonSecret))
        throw new Exception(ERROR_MSG_COMMON_SECRET_NOT_HEX);
      if (commonSecret.length() != 2 * COMMON_SECRET_SIZE)
        throw new Exception(ERROR_MSG_COMMON_SECRET_LEN_INCORRECT);
      if (!STR_HELPER.isHexString(initialVector))
        throw new Exception(ERROR_MSG_INITIAL_VECTOR_NOT_HEX);
      if (initialVector.length() != 2 * IV_SIZE)
        throw new Exception(ERROR_MSG_INITIAL_VECTOR_LEN_INCORRECT);
      TonWalletAppletStates appletState = turnOnWallet(BYTE_ARR_HELPER.bytes(STR_HELPER.pinToHex(newPin)), BYTE_ARR_HELPER.bytes(password), BYTE_ARR_HELPER.bytes(commonSecret), BYTE_ARR_HELPER.bytes(initialVector));
      if (appletState != TonWalletAppletStates.PERSONALIZED)
        throw new Exception(ERROR_MSG_APPLET_IS_NOT_PERSONALIZED + appletState.getDescription());
      String json = JSON_HELPER.createResponseJson(appletState.getDescription());
      //long end = System.currentTimeMillis();
      //Log.d("TAG", "!!Time = " + String.valueOf(end - start) );
      return json;
    }
    catch (Exception e) {
      throw new Exception(EXCEPTION_HELPER.makeFinalErrMsg(e), e);
    }
  }

  /**
   * @param password
   * @param commonSecret
   * @param initialVector
   * @param callback
   * This function makes TON Labs wallet applet activation for default PIN 5555.
   * Use here activation data tuple (password, commonSecret, initialVector) that is correct for your card, i.e. corresponds to your serialNumber.
   */
  private final CardApiInterface<List<String>> turnOnWalletWithoutPin = list ->  this.turnOnWalletAndGetJson(list.get(0), list.get(1), list.get(2));

  public void turnOnWallet(final String password, final String commonSecret, final String initialVector, final NfcCallback callback, Boolean... showDialog) {
    boolean showDialogFlag = showDialog.length > 0 ? showDialog[0] : false;
    CardTask cardTask = new CardTask(this, callback, Arrays.asList(password, commonSecret, initialVector), turnOnWalletWithoutPin, showDialogFlag);
    cardTask.execute();
  }

  /**
   *
   * @param password
   * @param commonSecret
   * @param initialVector
   * @return
   * @throws Exception
   * This function makes TON Labs wallet applet activation for default PIN 5555.
   * Use here activation data tuple (password, commonSecret, initialVector) that is correct for your card, i.e. corresponds to your serialNumber.
   *
   */
  public String turnOnWalletAndGetJson(final String password, final String commonSecret, final String initialVector) throws Exception {
    return turnOnWalletAndGetJson(DEFAULT_PIN_STR, password, commonSecret, initialVector);
  }

  /**
   * @param callback
   * Return SHA256 hash of encrypted common secret.
   */
  private final CardApiInterface<List<String>> getHashOfEncryptedCommonSecret = list -> this.getHashOfEncryptedCommonSecretAndGetJson();

  public void getHashOfEncryptedCommonSecret(final NfcCallback callback, Boolean... showDialog) {
    boolean showDialogFlag = showDialog.length > 0 ? showDialog[0] : false;
    CardTask cardTask = new CardTask(this, callback,  Collections.emptyList(), getHashOfEncryptedCommonSecret, showDialogFlag);
    cardTask.execute();
  }

  /**
   * @return
   * @throws Exception
   * Return SHA256 hash of encrypted common secret.
   */
  public String getHashOfEncryptedCommonSecretAndGetJson() throws Exception {
    try {
      //long start = System.currentTimeMillis();
      String response = BYTE_ARR_HELPER.hex(selectTonWalletAppletAndGetHashOfEncryptedCommonSecret().getData());
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
   * Return SHA256 hash of encrypted password.
   */
  private final CardApiInterface<List<String>> getHashOfEncryptedPassword = list -> this.getHashOfEncryptedPasswordAndGetJson();

  public void getHashOfEncryptedPassword(final NfcCallback callback, Boolean... showDialog) {
    boolean showDialogFlag = showDialog.length > 0 ? showDialog[0] : false;
    CardTask cardTask = new CardTask(this, callback,  Collections.emptyList(), getHashOfEncryptedPassword, showDialogFlag);
    cardTask.execute();
  }

  /**
   * @return
   * @throws Exception
   * Return SHA256 hash of encrypted password.
   */
  public String getHashOfEncryptedPasswordAndGetJson() throws Exception {
    try {
      //long start = System.currentTimeMillis();
      String response = BYTE_ARR_HELPER.hex(selectTonWalletAppletAndGetHashOfEncryptedPassword().getData());
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
   * Return SHA256 hashes of encrypted password and encrypted common secret.
   */
  private final CardApiInterface<List<String>> getHashes = list -> this.getHashesAndGetJson();

  public void getHashes(final NfcCallback callback, Boolean... showDialog) {
    boolean showDialogFlag = showDialog.length > 0 ? showDialog[0] : false;
    CardTask cardTask = new CardTask(this, callback,  Collections.emptyList(), getHashes, showDialogFlag);
    cardTask.execute();
  }

  /**
   * @return
   * @throws Exception
   * Return SHA256 hashes of encrypted password and encrypted common secret.
   */
  public String getHashesAndGetJson() throws Exception {
    try {
      //long start = System.currentTimeMillis();
      String ecsHash = BYTE_ARR_HELPER.hex(selectTonWalletAppletAndGetHashOfEncryptedCommonSecret().getData());
      String epHash = BYTE_ARR_HELPER.hex(getHashOfEncryptedPassword().getData());
      String sn = STR_HELPER.makeDigitalString(getSerialNumber());
      JSONObject jsonResponse = new JSONObject();
      jsonResponse.put(ECS_HASH_FIELD, ecsHash);
      jsonResponse.put(EP_HASH_FIELD, epHash);
      jsonResponse.put(SN_FIELD, sn);
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
   * Generate seed and return SHA256 hashes of encrypted password and encrypted common secret.
   */
  private final CardApiInterface<List<String>> generateSeedAndGetHashes = list -> this.generateSeedAndGetHashesAndGetJson();

  public void generateSeedAndGetHashes(final NfcCallback callback, Boolean... showDialog) {
    boolean showDialogFlag = showDialog.length > 0 ? showDialog[0] : false;
    CardTask cardTask = new CardTask(this, callback,  Collections.emptyList(), generateSeedAndGetHashes, showDialogFlag);
    cardTask.execute();
  }

  /**
   * @return
   * @throws Exception
   * Generate seed and return SHA256 hashes of encrypted password and encrypted common secret.
   */
  public String generateSeedAndGetHashesAndGetJson() throws Exception {
    try {
      //long start = System.currentTimeMillis();
      String response = BYTE_ARR_HELPER.hex(apduRunner.sendCoinManagerAppletAPDU(GET_ROOT_KEY_STATUS_APDU).getData()).equals(POSITIVE_ROOT_KEY_STATUS) ? GENERATED_MSG : NOT_GENERATED_MSG;
      if (response.equals(NOT_GENERATED_MSG)) {
        apduRunner.sendAPDU(RESET_WALLET_APDU); //to reset pin just in case
        apduRunner.sendAPDU(getGenerateSeedAPDU(DEFAULT_PIN));
      }
      TonWalletAppletStates appletState = getTonAppletState();
      if (appletState != TonWalletAppletStates.WAITE_AUTHENTICATION_MODE)
        throw new Exception(ERROR_MSG_APPLET_DOES_NOT_WAIT_AUTHENTICATION + appletState.getDescription());
      String ecsHash = BYTE_ARR_HELPER.hex(selectTonWalletAppletAndGetHashOfEncryptedCommonSecret().getData());
      String epHash = BYTE_ARR_HELPER.hex(getHashOfEncryptedPassword().getData());
      String sn = STR_HELPER.makeDigitalString(getSerialNumber());
      JSONObject jsonResponse = new JSONObject();
      jsonResponse.put(ECS_HASH_FIELD, ecsHash);
      jsonResponse.put(EP_HASH_FIELD, epHash);
      jsonResponse.put(SN_FIELD, sn);
      jsonResponse.put(STATUS_FIELD, SUCCESS_STATUS);
      //long end = System.currentTimeMillis();
      //Log.d("TAG", "!!Time = " + String.valueOf(end - start) );
      return jsonResponse.toString();
    }
    catch (Exception e) {
      throw new Exception(EXCEPTION_HELPER.makeFinalErrMsg(e), e);
    }
  }




  private RAPDU getHashOfEncryptedCommonSecret() throws Exception {
    RAPDU rapdu = apduRunner.sendAPDU(GET_HASH_OF_ENCRYPTED_COMMON_SECRET_APDU);
    if (rapdu == null || rapdu.getData() == null || rapdu.getData().length != SHA_HASH_SIZE) throw new Exception(ERROR_MSG_HASH_OF_ENCRYPTED_COMMON_SECRET_RESPONSE_LEN_INCORRECT);
    return rapdu;
  }

  private RAPDU getHashOfEncryptedPassword() throws Exception {
    RAPDU rapdu = apduRunner.sendAPDU(GET_HASH_OF_ENCRYPTED_PASSWORD_APDU);
    if (rapdu == null || rapdu.getData() == null || rapdu.getData().length != SHA_HASH_SIZE) throw new Exception(ERROR_MSG_HASH_OF_ENCRYPTED_PASSWORD_RESPONSE_LEN_INCORRECT);
    return rapdu;
  }

  private RAPDU selectTonWalletAppletAndGetHashOfEncryptedCommonSecret() throws Exception {
    RAPDU rapdu = apduRunner.sendTonWalletAppletAPDU(GET_HASH_OF_ENCRYPTED_COMMON_SECRET_APDU);
    if (rapdu == null || rapdu.getData() == null || rapdu.getData().length != SHA_HASH_SIZE) throw new Exception(ERROR_MSG_HASH_OF_ENCRYPTED_COMMON_SECRET_RESPONSE_LEN_INCORRECT);
    return rapdu;
  }

  private RAPDU selectTonWalletAppletAndGetHashOfEncryptedPassword() throws Exception {
    RAPDU rapdu =  apduRunner.sendTonWalletAppletAPDU(GET_HASH_OF_ENCRYPTED_PASSWORD_APDU);
    if (rapdu == null || rapdu.getData() == null || rapdu.getData().length != SHA_HASH_SIZE) throw new Exception(ERROR_MSG_HASH_OF_ENCRYPTED_PASSWORD_RESPONSE_LEN_INCORRECT);
    return rapdu;
  }


  private TonWalletAppletStates turnOnWallet(byte[] newPinBytes, byte[] password, byte[] commonSecret, byte[] initialVector) throws Exception {
    apduRunner.sendCoinManagerAppletAPDU(RESET_WALLET_APDU);
    apduRunner.sendAPDU(getGenerateSeedAPDU(DEFAULT_PIN));
    TonWalletAppletStates appletState = getTonAppletState();
    if (appletState != TonWalletAppletStates.WAITE_AUTHENTICATION_MODE)
      throw new Exception(ERROR_MSG_APPLET_DOES_NOT_WAIT_AUTHENTICATION + appletState.getDescription());
    boolean status = verifyHashOfEncryptedCommonSecret(password, commonSecret, initialVector);
    if (!status)
      throw new Exception(ERROR_MSG_HASH_OF_ENCRYPTED_COMMON_SECRET_RESPONSE_INCORRECT);
    status = verifyHashOfEncryptedPassword(password,initialVector);
    if (!status)
      throw new Exception(ERROR_MSG_HASH_OF_ENCRYPTED_PASSWORD_RESPONSE_INCORRECT);
    verifyPassword(password, initialVector);
    apduRunner.sendCoinManagerAppletAPDU(getChangePinAPDU(DEFAULT_PIN, newPinBytes));
    String serialNumber = STR_HELPER.makeDigitalString(getSerialNumber());
    createKeyForHmac(password, commonSecret, serialNumber);
    return getTonAppletState();
  }

  private boolean selectTonWalletAppletAndAndVerifyHashOfCommonSecret(byte[] commonSecret) throws Exception {
    String hashFromCard = BYTE_ARR_HELPER.hex(selectTonWalletAppletAndGetHashOfEncryptedCommonSecret().getData());
    String commonSecretHash = BYTE_ARR_HELPER.hex(digest.digest(commonSecret));
    return hashFromCard.equals(commonSecretHash);
  }

  private boolean verifyHashOfEncryptedCommonSecret(byte[] password, byte[] commonSecret, byte[] initialVector) throws Exception {
    String hashFromCard = BYTE_ARR_HELPER.hex(getHashOfEncryptedCommonSecret().getData());
    IvParameterSpec ivSpec = new IvParameterSpec(initialVector);
    byte[] passwordHash = digest.digest(password);
    SecretKeySpec skeySpec = new SecretKeySpec(BYTE_ARR_HELPER.bSub(passwordHash, 0, 16), "AES");
    Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
    cipher.init(Cipher.ENCRYPT_MODE, skeySpec, ivSpec);
    byte[] encryptedCommonSecret = cipher.doFinal(commonSecret);
    String encryptedCommonSecretHash = BYTE_ARR_HELPER.hex(digest.digest(encryptedCommonSecret));
    return hashFromCard.equals(encryptedCommonSecretHash);
  }

  private boolean verifyHashOfEncryptedPassword(byte[] password, byte[] initialVector) throws Exception {
    String hashFromCard = BYTE_ARR_HELPER.hex(getHashOfEncryptedPassword().getData());
    IvParameterSpec ivSpec = new IvParameterSpec(initialVector);
    byte[] passwordHash = digest.digest(password);
    SecretKeySpec skeySpec = new SecretKeySpec(BYTE_ARR_HELPER.bSub(passwordHash, 0, 16), "AES");
    Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
    cipher.init(Cipher.ENCRYPT_MODE, skeySpec, ivSpec);
    byte[] encryptedPassword = cipher.doFinal(password);
    String encryptedPasswordHash = BYTE_ARR_HELPER.hex(digest.digest(encryptedPassword));
    return hashFromCard.equals(encryptedPasswordHash);
  }

  private RAPDU verifyPassword(byte[] password, byte[] initialVector) throws Exception {
    return apduRunner.sendAPDU(getVerifyPasswordAPDU(password, initialVector));
  }

  private RAPDU selectTonWalletAppletAndVerifyPassword(byte[] password, byte[] initialVector) throws Exception {
    return apduRunner.sendTonWalletAppletAPDU(getVerifyPasswordAPDU(password, initialVector));
  }
}
