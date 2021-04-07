package com.tonnfccard;

import android.content.Context;
import android.util.Log;

import com.tonnfccard.callback.NfcCallback;
import com.tonnfccard.nfc.NfcApduRunner;
import com.tonnfccard.smartcard.ApduRunner;
import com.tonnfccard.smartcard.RAPDU;

import static com.tonnfccard.TonWalletConstants.*;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_DATA_FOR_SIGNING_LEN_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_DATA_FOR_SIGNING_NOT_HEX;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_DATA_FOR_SIGNING_WITH_PATH_LEN_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_HD_INDEX_FORMAT_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_HD_INDEX_LEN_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_PIN_FORMAT_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_PIN_LEN_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_PUBLIC_KEY_RESPONSE_LEN_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_SIG_RESPONSE_LEN_INCORRECT;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.GET_PUB_KEY_WITH_DEFAULT_PATH_APDU;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.getPublicKeyAPDU;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.getSignShortMessageAPDU;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.getSignShortMessageWithDefaultPathAPDU;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.getVerifyPinAPDU;

/**
 * Class containing functions-wrappers for card operations related to ed25519 signature: verifyPin, getPublicKeyForDefaultPath, getPublicKey,
 * signForDefaultHdPath, sign, verifyPinAndSignForDefaultHdPath, verifyPinAndSign.
 *
 * Note 1: signForDefaultHdPath and verifyPinAndSignForDefaultHdPath do the same stuff. But you must call verifyPin before signForDefaultHdPath.
 * The same is true for sign and verifyPinAndSign.
 *
 * Note 2: signForDefaultHdPath, verifyPinAndSignForDefaultHdPat and getPublicKeyForDefaultPath work with bip44 HD path m/44'/396'/0'/0'/0'.
 */

public final class CardCryptoApi extends TonWalletApi {
  private static final String TAG = "CardCryptoNfcApi";

  public CardCryptoApi(Context activity, NfcApduRunner apduRunner) {
    super(activity, apduRunner);
  }

  public void verifyPin(final String pin, final NfcCallback callback) {
    new Thread(new Runnable() {
      public void run() {
        try {
          String json = verifyPinAndGetJson(pin);
          resolveJson(json, callback);
          Log.d(TAG, "verifyPin response : " + json);
        } catch (Exception e) {
          EXCEPTION_HELPER.handleException(e, callback, TAG);
        }
      }
    }).start();
  }

  public String verifyPinAndGetJson(String pin) throws Exception {
    try {
      if (!STR_HELPER.isNumericString(pin))
        throw new Exception(ERROR_MSG_PIN_FORMAT_INCORRECT);
      if (pin.length() != PIN_SIZE)
        throw new Exception(ERROR_MSG_PIN_LEN_INCORRECT);
      verifyPin(BYTE_ARR_HELPER.bytes(STR_HELPER.pinToHex(pin)));
      return JSON_HELPER.createResponseJson(DONE_MSG);
    }
    catch (Exception e) {
      throw new Exception(EXCEPTION_HELPER.makeFinalErrMsg(e), e);
    }
  }



  public void getPublicKeyForDefaultPath(final NfcCallback callback) {
    new Thread(new Runnable() {
      public void run() {
        try {
          String json = getPublicKeyForDefaultPathAndGetJson();
          resolveJson(json, callback);
          Log.d(TAG, "getPublicKeyForDefaultPath response : " + json);
        } catch (Exception e) {
          EXCEPTION_HELPER.handleException(e, callback, TAG);
        }
      }
    }).start();
  }

  public String getPublicKeyForDefaultPathAndGetJson() throws Exception {
    try {
      String response = BYTE_ARR_HELPER.hex(getPublicKeyForDefaultPath().getData());
      return JSON_HELPER.createResponseJson(response);
    }
    catch (Exception e) {
      throw new Exception(EXCEPTION_HELPER.makeFinalErrMsg(e), e);
    }
  }

  public void getPublicKey(final String index, final NfcCallback callback) {
    new Thread(new Runnable() {
      public void run() {
        try {
          String json = getPublicKeyAndGetJson(index);
          resolveJson(json, callback);
          Log.d(TAG, "getPublicKey response : " + json);
        } catch (Exception e) {
          EXCEPTION_HELPER.handleException(e, callback, TAG);
        }
      }
    }).start();
  }

  public String getPublicKeyAndGetJson(String index) throws Exception {
    try {
      if (!STR_HELPER.isNumericString(index))
        throw new Exception(ERROR_MSG_HD_INDEX_FORMAT_INCORRECT);
      if (index.length() == 0 || index.length() > MAX_HD_INDEX_SIZE)
        throw new Exception(ERROR_MSG_HD_INDEX_LEN_INCORRECT);
      String response = BYTE_ARR_HELPER.hex(getPublicKey(BYTE_ARR_HELPER.bytes(STR_HELPER.asciiToHex(index))).getData());
      return JSON_HELPER.createResponseJson(response);
    }
    catch (Exception e) {
      throw new Exception(EXCEPTION_HELPER.makeFinalErrMsg(e), e);
    }
  }

  public void signForDefaultHdPath(final String dataForSigning, final NfcCallback callback) {
    new Thread(new Runnable() {
      public void run() {
        try {
          String json = signForDefaultHdPathAndGetJson(dataForSigning);
          resolveJson(json, callback);
          Log.d(TAG, "signForDefaultHdPath response : " + json);
        } catch (Exception e) {
          EXCEPTION_HELPER.handleException(e, callback, TAG);
        }
      }
    }).start();
  }

  public String signForDefaultHdPathAndGetJson(final String dataForSigning) throws Exception {
    try {
      if (!STR_HELPER.isHexString(dataForSigning))
        throw new Exception(ERROR_MSG_DATA_FOR_SIGNING_NOT_HEX);
      if (dataForSigning.length() > (2 * DATA_FOR_SIGNING_MAX_SIZE))
        throw new Exception(ERROR_MSG_DATA_FOR_SIGNING_LEN_INCORRECT);
      String response = BYTE_ARR_HELPER.hex(signForDefaultPath(BYTE_ARR_HELPER.bytes(dataForSigning)).getData());
      return JSON_HELPER.createResponseJson(response);
    }
    catch (Exception e) {
      throw new Exception(EXCEPTION_HELPER.makeFinalErrMsg(e), e);
    }
  }

  public void sign(final String dataForSigning, final String index, final NfcCallback callback) {
    new Thread(new Runnable() {
      public void run() {
        try {
          String json = signAndGetJson(dataForSigning, index);
          resolveJson(json, callback);
          Log.d(TAG, "sign response : " + json);
        } catch (Exception e) {
          EXCEPTION_HELPER.handleException(e, callback, TAG);
        }
      }
    }).start();
  }

  public String signAndGetJson(final String dataForSigning, final String index) throws Exception {
    try {
      if (!STR_HELPER.isNumericString(index))
        throw new Exception(ERROR_MSG_HD_INDEX_FORMAT_INCORRECT);
      if (index.length() > MAX_HD_INDEX_SIZE)
        throw new Exception(ERROR_MSG_HD_INDEX_LEN_INCORRECT);
      if (!STR_HELPER.isHexString(dataForSigning))
        throw new Exception(ERROR_MSG_DATA_FOR_SIGNING_NOT_HEX);
      if (dataForSigning.length() > (2 * DATA_FOR_SIGNING_MAX_SIZE_FOR_CASE_WITH_PATH))
        throw new Exception(ERROR_MSG_DATA_FOR_SIGNING_WITH_PATH_LEN_INCORRECT);
      String response = BYTE_ARR_HELPER.hex(sign(BYTE_ARR_HELPER.bytes(dataForSigning), BYTE_ARR_HELPER.bytes(STR_HELPER.asciiToHex(index))).getData());
      return JSON_HELPER.createResponseJson(response);
    }
    catch (Exception e) {
      throw new Exception(EXCEPTION_HELPER.makeFinalErrMsg(e), e);
    }
  }

  public void verifyPinAndSignForDefaultHdPath(final String dataForSigning, final String pin, final NfcCallback callback) {
    new Thread(new Runnable() {
      public void run() {
        try {
          String json = verifyPinAndSignForDefaultHdPathAndGetJson(dataForSigning, pin);
          resolveJson(json, callback);
          Log.d(TAG, "verifyPinAndSignForDefaultHdPath response : " + json);
        } catch (Exception e) {
          EXCEPTION_HELPER.handleException(e, callback, TAG);
        }
      }
    }).start();
  }

  public String verifyPinAndSignForDefaultHdPathAndGetJson(final String dataForSigning, final String pin) throws Exception {
    try {
      if (!STR_HELPER.isNumericString(pin))
        throw new Exception(ERROR_MSG_PIN_FORMAT_INCORRECT);
      if (pin.length() != PIN_SIZE)
        throw new Exception(ERROR_MSG_PIN_LEN_INCORRECT);
      if (!STR_HELPER.isHexString(dataForSigning))
        throw new Exception(ERROR_MSG_DATA_FOR_SIGNING_NOT_HEX);
      if (dataForSigning.length() > (2 * DATA_FOR_SIGNING_MAX_SIZE))
        throw new Exception(ERROR_MSG_DATA_FOR_SIGNING_LEN_INCORRECT);
      String response = BYTE_ARR_HELPER.hex(verifyPinAndSignForDefaultHdPath(BYTE_ARR_HELPER.bytes(dataForSigning), BYTE_ARR_HELPER.bytes(STR_HELPER.pinToHex(pin))).getData());
      return JSON_HELPER.createResponseJson(response);
    }
    catch (Exception e) {
      throw new Exception(EXCEPTION_HELPER.makeFinalErrMsg(e), e);
    }
  }

  public void verifyPinAndSign(final String dataForSigning, final String index, final String pin, final NfcCallback callback) {
    new Thread(new Runnable() {
      public void run() {
        try {
          String json = verifyPinAndSignAndGetJson(dataForSigning, index, pin);
          resolveJson(json, callback);
          Log.d(TAG, "verifyPinAndSign response : " + json);
        } catch (Exception e) {
          EXCEPTION_HELPER.handleException(e, callback, TAG);
        }
      }
    }).start();
  }

  public String verifyPinAndSignAndGetJson(final String dataForSigning, final String index, final String pin) throws Exception {
    try {
      if (!STR_HELPER.isNumericString(pin))
        throw new Exception(ERROR_MSG_PIN_FORMAT_INCORRECT);
      if (pin.length() != PIN_SIZE)
        throw new Exception(ERROR_MSG_PIN_LEN_INCORRECT);
      if (!STR_HELPER.isNumericString(index))
        throw new Exception(ERROR_MSG_HD_INDEX_FORMAT_INCORRECT);
      if (index.length() > MAX_HD_INDEX_SIZE)
        throw new Exception(ERROR_MSG_HD_INDEX_LEN_INCORRECT);
      if (!STR_HELPER.isHexString(dataForSigning))
        throw new Exception(ERROR_MSG_DATA_FOR_SIGNING_NOT_HEX);
      if (dataForSigning.length() > (2 * DATA_FOR_SIGNING_MAX_SIZE_FOR_CASE_WITH_PATH))
        throw new Exception(ERROR_MSG_DATA_FOR_SIGNING_WITH_PATH_LEN_INCORRECT);
      String response = BYTE_ARR_HELPER.hex(verifyPinAndSign(BYTE_ARR_HELPER.bytes(dataForSigning), BYTE_ARR_HELPER.bytes(STR_HELPER.asciiToHex(index)), BYTE_ARR_HELPER.bytes(STR_HELPER.pinToHex(pin))).getData());
      return JSON_HELPER.createResponseJson(response);
    }
    catch (Exception e) {
      throw new Exception(EXCEPTION_HELPER.makeFinalErrMsg(e), e);
    }
  }

  private RAPDU getPublicKeyForDefaultPath() throws Exception {
    RAPDU rapdu = apduRunner.sendTonWalletAppletAPDU(GET_PUB_KEY_WITH_DEFAULT_PATH_APDU);
    if (rapdu == null || rapdu.getData() == null || rapdu.getData().length != PUBLIC_KEY_LEN) throw new Exception(ERROR_MSG_PUBLIC_KEY_RESPONSE_LEN_INCORRECT);
    return rapdu;
  }


  private RAPDU getPublicKey(byte[] indBytes) throws Exception {
    RAPDU rapdu = apduRunner.sendTonWalletAppletAPDU(getPublicKeyAPDU(indBytes));
    if (rapdu == null || rapdu.getData() == null || rapdu.getData().length != PUBLIC_KEY_LEN) throw new Exception(ERROR_MSG_PUBLIC_KEY_RESPONSE_LEN_INCORRECT);
    return rapdu;
  }

  private RAPDU verifyPin(byte[] pinBytes) throws Exception {
    reselectKeyForHmac();
    byte[] sault = getSaultBytes();
    return apduRunner.sendAPDU(getVerifyPinAPDU(pinBytes, sault));
  }

  private RAPDU verifyPinAndSignForDefaultHdPath(byte[] dataForSigning, byte[] pinBytes) throws Exception {
    reselectKeyForHmac();
    byte[] sault = getSaultBytes();
    apduRunner.sendAPDU(getVerifyPinAPDU(pinBytes, sault));
    sault = getSaultBytes();
    RAPDU rapdu = apduRunner.sendAPDU(getSignShortMessageWithDefaultPathAPDU(dataForSigning, sault));
    if (rapdu == null || rapdu.getData() == null || rapdu.getData().length != SIG_LEN) throw new Exception(ERROR_MSG_SIG_RESPONSE_LEN_INCORRECT);
    return rapdu;
  }

  private RAPDU signForDefaultPath(byte[] dataForSigning) throws Exception {
    reselectKeyForHmac();
    byte[] sault = getSaultBytes();
    RAPDU rapdu = apduRunner.sendAPDU(getSignShortMessageWithDefaultPathAPDU(dataForSigning, sault));
    if (rapdu == null || rapdu.getData() == null || rapdu.getData().length != SIG_LEN) throw new Exception(ERROR_MSG_SIG_RESPONSE_LEN_INCORRECT);
    return rapdu;
  }

  private RAPDU verifyPinAndSign(byte[] dataForSigning, byte[] ind, byte[] pinBytes) throws Exception {
    reselectKeyForHmac();
    byte[] sault = getSaultBytes();
    apduRunner.sendAPDU(getVerifyPinAPDU(pinBytes, sault));
    sault = getSaultBytes();
    RAPDU rapdu = apduRunner.sendAPDU(getSignShortMessageAPDU(dataForSigning, ind, sault));
    if (rapdu == null || rapdu.getData() == null || rapdu.getData().length != SIG_LEN) throw new Exception(ERROR_MSG_SIG_RESPONSE_LEN_INCORRECT);
    return rapdu;
  }

  private RAPDU sign(byte[] dataForSigning, byte[] ind) throws Exception {
    reselectKeyForHmac();
    byte[] sault = getSaultBytes();
    RAPDU rapdu = apduRunner.sendAPDU(getSignShortMessageAPDU(dataForSigning, ind, sault));
    if (rapdu == null || rapdu.getData() == null || rapdu.getData().length != SIG_LEN) throw new Exception(ERROR_MSG_SIG_RESPONSE_LEN_INCORRECT);
    return rapdu;
  }
}
