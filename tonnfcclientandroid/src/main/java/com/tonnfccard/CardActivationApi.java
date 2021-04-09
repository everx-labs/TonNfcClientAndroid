package com.tonnfccard;

import android.content.Context;
import android.util.Log;

import com.tonnfccard.callback.NfcCallback;
import com.tonnfccard.nfc.NfcApduRunner;
import com.tonnfccard.smartcard.TonWalletAppletStates;
import com.tonnfccard.smartcard.ApduRunner;
import com.tonnfccard.smartcard.RAPDU;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import static com.tonnfccard.TonWalletConstants.*;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_APPLET_DOES_NOT_WAIT_AUTHORIZATION;
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
  public void turnOnWallet(final String newPin, final String password, final String commonSecret, final String initialVector, final NfcCallback callback) {
    new Thread(new Runnable() {
      public void run() {
        try {
          String json = turnOnWalletAndGetJson(newPin, password, commonSecret, initialVector);
          resolveJson(json, callback);
          Log.d(TAG, "turnOnWallet response : " + json);
        } catch (Exception e) {

          EXCEPTION_HELPER.handleException(e, callback, TAG);
        }
      }
    }).start();
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
      long start = System.currentTimeMillis();
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
      TonWalletAppletStates state = turnOnWallet(BYTE_ARR_HELPER.bytes(STR_HELPER.pinToHex(newPin)), BYTE_ARR_HELPER.bytes(password), BYTE_ARR_HELPER.bytes(commonSecret), BYTE_ARR_HELPER.bytes(initialVector));
      String json = JSON_HELPER.createResponseJson(state.getDescription());
      long end = System.currentTimeMillis();
      Log.d("TAG", "!!Time = " + String.valueOf(end - start) );
      return json;
    }
    catch (Exception e) {
      throw new Exception(EXCEPTION_HELPER.makeFinalErrMsg(e), e);
    }
  }

  /**
   * @param callback
   * Return SHA256 hash of encrypted common secret.
   */
  public void getHashOfEncryptedCommonSecret(final NfcCallback callback) {
    new Thread(new Runnable() {
      public void run() {
        try {
          String json = getHashOfEncryptedCommonSecretAndGetJson();
          resolveJson(json, callback);
          Log.d(TAG, "getHashOfCommonSecret response : " + json);
        } catch (Exception e) {
          EXCEPTION_HELPER.handleException(e, callback, TAG);
        }
      }
    }).start();
  }

  /**
   * @return
   * @throws Exception
   * Return SHA256 hash of encrypted common secret.
   */
  public String getHashOfEncryptedCommonSecretAndGetJson() throws Exception {
    try {
      long start = System.currentTimeMillis();
      String response = BYTE_ARR_HELPER.hex(selectTonWalletAppletAndGetHashOfEncryptedCommonSecret().getData());
      String json = JSON_HELPER.createResponseJson(response);
      long end = System.currentTimeMillis();
      Log.d("TAG", "!!Time = " + String.valueOf(end - start) );
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
  public void getHashOfEncryptedPassword(final NfcCallback callback) {
    new Thread(new Runnable() {
      public void run() {
        try {
          String json = getHashOfEncryptedPasswordAndGetJson();
          resolveJson(json, callback);
          Log.d(TAG, "getHashOfEncryptedPassword response : " + json);
        } catch (Exception e) {
          EXCEPTION_HELPER.handleException(e, callback, TAG);
        }
      }
    }).start();
  }

  /**
   * @return
   * @throws Exception
   * Return SHA256 hash of encrypted password.
   */
  public String getHashOfEncryptedPasswordAndGetJson() throws Exception {
    try {
      long start = System.currentTimeMillis();
      String response = BYTE_ARR_HELPER.hex(selectTonWalletAppletAndGetHashOfEncryptedPassword().getData());
      String json = JSON_HELPER.createResponseJson(response);
      long end = System.currentTimeMillis();
      Log.d("TAG", "!!Time = " + String.valueOf(end - start) );
      return json;
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
    if (appletState != TonWalletAppletStates.WAITE_AUTHORIZATION_MODE)
      throw new Exception(ERROR_MSG_APPLET_DOES_NOT_WAIT_AUTHORIZATION + appletState.getDescription());
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
