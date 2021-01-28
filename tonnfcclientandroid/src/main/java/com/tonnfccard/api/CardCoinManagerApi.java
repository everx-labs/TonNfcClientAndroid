package com.tonnfccard.api;

import android.content.Context;
import android.util.Log;

import com.tonnfccard.api.callback.NfcCallback;
import com.tonnfccard.smartcard.wrappers.ApduRunner;
import com.tonnfccard.smartcard.wrappers.CAPDU;
import com.tonnfccard.smartcard.wrappers.RAPDU;

import static com.tonnfccard.api.utils.ResponsesConstants.DONE_MSG;
import static com.tonnfccard.api.utils.ResponsesConstants.ERROR_MSG_DEVICE_LABEL_LEN_INCORRECT;
import static com.tonnfccard.api.utils.ResponsesConstants.ERROR_MSG_DEVICE_LABEL_NOT_HEX;
import static com.tonnfccard.api.utils.ResponsesConstants.ERROR_MSG_GET_APPLET_LIST_RESPONSE_LEN_INCORRECT;
import static com.tonnfccard.api.utils.ResponsesConstants.ERROR_MSG_GET_AVAILABLE_MEMORY_RESPONSE_LEN_INCORRECT;
import static com.tonnfccard.api.utils.ResponsesConstants.ERROR_MSG_GET_CSN_RESPONSE_LEN_INCORRECT;
import static com.tonnfccard.api.utils.ResponsesConstants.ERROR_MSG_GET_DEVICE_LABEL_RESPONSE_LEN_INCORRECT;
import static com.tonnfccard.api.utils.ResponsesConstants.ERROR_MSG_GET_PIN_TLT_OR_RTL_RESPONSE_LEN_INCORRECT;
import static com.tonnfccard.api.utils.ResponsesConstants.ERROR_MSG_GET_PIN_TLT_OR_RTL_RESPONSE_VAL_INCORRECT;
import static com.tonnfccard.api.utils.ResponsesConstants.ERROR_MSG_GET_ROOT_KEY_STATUS_RESPONSE_LEN_INCORRECT;
import static com.tonnfccard.api.utils.ResponsesConstants.ERROR_MSG_GET_SE_VERSION_RESPONSE_LEN_INCORRECT;
import static com.tonnfccard.api.utils.ResponsesConstants.ERROR_MSG_PIN_FORMAT_INCORRECT;
import static com.tonnfccard.api.utils.ResponsesConstants.ERROR_MSG_PIN_LEN_INCORRECT;
import static com.tonnfccard.api.utils.ResponsesConstants.GENERATED_MSG;
import static com.tonnfccard.api.utils.ResponsesConstants.NOT_GENERATED_MSG;
import static com.tonnfccard.smartcard.TonWalletAppletConstants.MAX_PIN_TRIES;
import static com.tonnfccard.smartcard.TonWalletAppletConstants.PIN_SIZE;
import static com.tonnfccard.smartcard.apdu.CoinManagerApduCommands.GET_APPLET_LIST_APDU;
import static com.tonnfccard.smartcard.apdu.CoinManagerApduCommands.GET_AVAILABLE_MEMORY_APDU;
import static com.tonnfccard.smartcard.apdu.CoinManagerApduCommands.GET_CSN;
import static com.tonnfccard.smartcard.apdu.CoinManagerApduCommands.GET_DEVICE_LABEL;
import static com.tonnfccard.smartcard.apdu.CoinManagerApduCommands.GET_PIN_RTL_APDU;
import static com.tonnfccard.smartcard.apdu.CoinManagerApduCommands.GET_PIN_TLT_APDU;
import static com.tonnfccard.smartcard.apdu.CoinManagerApduCommands.GET_ROOT_KEY_STATUS_APDU;
import static com.tonnfccard.smartcard.apdu.CoinManagerApduCommands.GET_SE_VERSION;
import static com.tonnfccard.smartcard.apdu.CoinManagerApduCommands.LABEL_LENGTH;
import static com.tonnfccard.smartcard.apdu.CoinManagerApduCommands.POSITIVE_ROOT_KEY_STATUS;
import static com.tonnfccard.smartcard.apdu.CoinManagerApduCommands.RESET_WALLET_APDU;
import static com.tonnfccard.smartcard.apdu.CoinManagerApduCommands.getChangePinAPDU;
import static com.tonnfccard.smartcard.apdu.CoinManagerApduCommands.getGenerateSeedAPDU;
import static com.tonnfccard.smartcard.apdu.CoinManagerApduCommands.getSetDeviceLabelAPDU;

public class CardCoinManagerApi extends TonWalletApi {
  private static final String TAG = "CardCoinManagerNfcApi";

  public CardCoinManagerApi(Context activity, ApduRunner apduRunner) {
    super(activity, apduRunner);
  }

  public void setDeviceLabel(final String deviceLabel, final NfcCallback callback) {
    new Thread(new Runnable() {
      public void run() {
        try {
          String json = setDeviceLabelAndGetJson(deviceLabel);
          resolveJson(json, callback);
          Log.d(TAG, "setDeviceLabel response : " + json);
        } catch (Exception e) {
          EXCEPTION_HELPER.handleException(e, callback, TAG);
        }
      }
    }).start();
  }

  public String setDeviceLabelAndGetJson(String deviceLabel) throws Exception {
    if (!STR_HELPER.isHexString(deviceLabel))
      throw new Exception(ERROR_MSG_DEVICE_LABEL_NOT_HEX);
    if (deviceLabel.length() != 2 * LABEL_LENGTH)
      throw new Exception(ERROR_MSG_DEVICE_LABEL_LEN_INCORRECT);
    apduRunner.sendCoinManagerAppletAPDU(getSetDeviceLabelAPDU(BYTE_ARR_HELPER.bytes(deviceLabel)));
    return JSON_HELPER.createResponseJson(DONE_MSG);
  }

  public void getDeviceLabel(final NfcCallback callback) {
    new Thread(new Runnable() {
      public void run() {
        try {
          String json = getDeviceLabelAndGetJson();
          resolveJson(json, callback);
          Log.d(TAG, "getDeviceLabel response : " + json);
        } catch (Exception e) {
          EXCEPTION_HELPER.handleException(e, callback, TAG);
        }
      }
    }).start();
  }

  public String getDeviceLabelAndGetJson() throws Exception {
    RAPDU rapdu = apduRunner.sendCoinManagerAppletAPDU(GET_DEVICE_LABEL);
    if (rapdu == null || rapdu.getData() == null || rapdu.getData().length != LABEL_LENGTH) throw new Exception(ERROR_MSG_GET_DEVICE_LABEL_RESPONSE_LEN_INCORRECT);
    String response = BYTE_ARR_HELPER.hex(rapdu.getData());
    return JSON_HELPER.createResponseJson(response);
  }

  public void getSeVersion(final NfcCallback callback) {
    new Thread(new Runnable() {
      public void run() {
        try {
          String json = getSeVersionAndGetJson();
          resolveJson(json, callback);
          Log.d(TAG, "getSeVersion response : " + json);
        } catch (Exception e) {
          EXCEPTION_HELPER.handleException(e, callback, TAG);
        }
      }
    }).start();
  }

  public String getSeVersionAndGetJson() throws Exception {
    return executeCoinManagerOperationAndSendHex(GET_SE_VERSION, ERROR_MSG_GET_SE_VERSION_RESPONSE_LEN_INCORRECT);
  }

  public void getCsn(final NfcCallback callback) {
    new Thread(new Runnable() {
      public void run() {
        try {
          String json = getCsnAndGetJson();
          resolveJson(json, callback);
          Log.d(TAG, "getCsn (CSN = SecureElement id) response : " + json);
        } catch (Exception e) {
          EXCEPTION_HELPER.handleException(e, callback, TAG);
        }
      }
    }).start();
  }

  public String getCsnAndGetJson() throws Exception {
    return executeCoinManagerOperationAndSendHex(GET_CSN, ERROR_MSG_GET_CSN_RESPONSE_LEN_INCORRECT);
  }

  public void getMaxPinTries(final NfcCallback callback) {
    new Thread(new Runnable() {
      public void run() {
        try {
          String json = getMaxPinTriesAndGetJson();
          resolveJson(json, callback);
          Log.d(TAG, "getMaxPinTries response : " + json);
        } catch (Exception e) {
          EXCEPTION_HELPER.handleException(e, callback, TAG);
        }
      }
    }).start();
  }

  public String getMaxPinTriesAndGetJson() throws Exception {
    return getPinTries(GET_PIN_TLT_APDU);
  }

  public void getRemainingPinTries(final NfcCallback callback) {
    new Thread(new Runnable() {
      public void run() {
        try {
          String json = getRemainingPinTriesAndGetJson();
          resolveJson(json, callback);
          Log.d(TAG, "getRemainingPinTries response : " + json);
        } catch (Exception e) {
          EXCEPTION_HELPER.handleException(e, callback, TAG);
        }
      }
    }).start();
  }

  public String getRemainingPinTriesAndGetJson() throws Exception {
    return getPinTries(GET_PIN_RTL_APDU);
  }

  public void getRootKeyStatus(final NfcCallback callback) {
    new Thread(new Runnable() {
      public void run() {
        try {
          String json = getRootKeyStatusAndGetJson();
          resolveJson(json, callback);
          Log.d(TAG, "getRootKeyStatus response : " + json);
        } catch (Exception e) {
          EXCEPTION_HELPER.handleException(e, callback, TAG);
        }
      }
    }).start();
  }

  public String getRootKeyStatusAndGetJson() throws Exception {
    RAPDU rapdu = apduRunner.sendCoinManagerAppletAPDU(GET_ROOT_KEY_STATUS_APDU);
    if (rapdu == null || rapdu.getData() == null || rapdu.getData().length == 0) throw new Exception(ERROR_MSG_GET_ROOT_KEY_STATUS_RESPONSE_LEN_INCORRECT);
    String response = BYTE_ARR_HELPER.hex(rapdu.getData()).equals(POSITIVE_ROOT_KEY_STATUS) ? GENERATED_MSG : NOT_GENERATED_MSG;
    return JSON_HELPER.createResponseJson(response);
  }

  public void resetWallet(final NfcCallback callback) {
    new Thread(new Runnable() {
      public void run() {
        try {
          String json = resetWalletAndGetJson();
          resolveJson(json, callback);
          Log.d(TAG, "resetWallet response : " + json);
        } catch (Exception e) {
          EXCEPTION_HELPER.handleException(e, callback, TAG);
        }
      }
    }).start();
  }

  public String resetWalletAndGetJson() throws Exception {
    apduRunner.sendCoinManagerAppletAPDU(RESET_WALLET_APDU);
    return JSON_HELPER.createResponseJson(DONE_MSG);
  }

  public void getAvailableMemory(final NfcCallback callback) {
    new Thread(new Runnable() {
      public void run() {
        try {
          String json = getAvailableMemoryAndGetJson();
          resolveJson(json, callback);
          Log.d(TAG, "getAvailableMemory response : " + json);
        } catch (Exception e) {
          EXCEPTION_HELPER.handleException(e, callback, TAG);
        }
      }
    }).start();
  }

  public String getAvailableMemoryAndGetJson() throws Exception {
    return executeCoinManagerOperationAndSendHex(GET_AVAILABLE_MEMORY_APDU, ERROR_MSG_GET_AVAILABLE_MEMORY_RESPONSE_LEN_INCORRECT);
  }

  public void getAppsList(final NfcCallback callback) {
    new Thread(new Runnable() {
      public void run() {
        try {
          String json = getAppsListAndGetJson();
          resolveJson(json, callback);
          Log.d(TAG, "getAppsList response : " + json);
        } catch (Exception e) {
          EXCEPTION_HELPER.handleException(e, callback, TAG);
        }
      }
    }).start();
  }

  public String getAppsListAndGetJson() throws Exception {
    return executeCoinManagerOperationAndSendHex(GET_APPLET_LIST_APDU, ERROR_MSG_GET_APPLET_LIST_RESPONSE_LEN_INCORRECT);
  }

  public void generateSeed(final String pin, final NfcCallback callback) {
    new Thread(new Runnable() {
      public void run() {
        try {
          String json = generateSeedAndGetJson(pin);
          resolveJson(json, callback);
          Log.d(TAG, "generateSeed response : " + json);
        } catch (Exception e) {
          EXCEPTION_HELPER.handleException(e, callback, TAG);
        }
      }
    }).start();
  }

  public String generateSeedAndGetJson(String pin) throws Exception {
    if (!STR_HELPER.isNumericString(pin))
      throw new Exception(ERROR_MSG_PIN_FORMAT_INCORRECT);
    if (pin.length() != PIN_SIZE)
      throw new Exception(ERROR_MSG_PIN_LEN_INCORRECT);
    apduRunner.sendCoinManagerAppletAPDU(getGenerateSeedAPDU(BYTE_ARR_HELPER.bytes(STR_HELPER.pinToHex(pin))));
    return JSON_HELPER.createResponseJson(DONE_MSG);
  }

  public void changePin(final String oldPin, final String newPin, final NfcCallback callback) {
    new Thread(new Runnable() {
      public void run() {
        try {
          String json =  changePinAndGetJson(oldPin, newPin);
          resolveJson(json, callback);
          Log.d(TAG, "changePin response : " + json);
        } catch (Exception e) {
          EXCEPTION_HELPER.handleException(e, callback, TAG);
        }
      }
    }).start();
  }

  public String changePinAndGetJson(final String oldPin, final String newPin) throws Exception {
    if (!STR_HELPER.isNumericString(newPin) || !STR_HELPER.isNumericString(oldPin))
      throw new Exception(ERROR_MSG_PIN_FORMAT_INCORRECT);
    if (oldPin.length() != PIN_SIZE || newPin.length() != PIN_SIZE)
      throw new Exception(ERROR_MSG_PIN_LEN_INCORRECT);
    apduRunner.sendCoinManagerAppletAPDU(getChangePinAPDU(BYTE_ARR_HELPER.bytes(STR_HELPER.pinToHex(oldPin)), BYTE_ARR_HELPER.bytes(STR_HELPER.pinToHex(newPin))));
    return JSON_HELPER.createResponseJson(DONE_MSG);
  }

  private String executeCoinManagerOperationAndSendHex(CAPDU apdu, String errMsg) throws Exception {
    RAPDU rapdu = apduRunner.sendCoinManagerAppletAPDU(apdu);
    if (rapdu == null || rapdu.getData() == null || rapdu.getData().length == 0) throw new Exception(errMsg);
    String response = BYTE_ARR_HELPER.hex(rapdu.getData());
    return JSON_HELPER.createResponseJson(response);
  }

  private String getPinTries(CAPDU apdu) throws Exception {
    RAPDU rapdu = apduRunner.sendCoinManagerAppletAPDU(apdu);
    if (rapdu == null || rapdu.getData() == null || rapdu.getData().length == 0) throw new Exception(ERROR_MSG_GET_PIN_TLT_OR_RTL_RESPONSE_LEN_INCORRECT);
    byte res = rapdu.getData()[0];
    if (res < 0 || res > MAX_PIN_TRIES) {
      throw new Exception(ERROR_MSG_GET_PIN_TLT_OR_RTL_RESPONSE_VAL_INCORRECT);
    }
    return JSON_HELPER.createResponseJson(Byte.toString(res));
  }
}
