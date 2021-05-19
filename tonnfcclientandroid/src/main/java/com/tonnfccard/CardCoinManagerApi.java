package com.tonnfccard;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentManager;

import com.tonnfccard.callback.NfcCallback;
import com.tonnfccard.nfc.NfcApduRunner;
import com.tonnfccard.smartcard.ApduRunner;
import com.tonnfccard.smartcard.RAPDU;
import com.tonnfccard.smartcard.CAPDU;

import java.util.concurrent.TimeUnit;

import javax.xml.transform.Result;

import static androidx.test.internal.runner.junit4.statement.UiThreadStatement.runOnUiThread;
import static androidx.test.internal.runner.junit4.statement.UiThreadStatement.shouldRunOnUiThread;
import static com.tonnfccard.TonWalletConstants.DONE_MSG;
import static com.tonnfccard.TonWalletConstants.GENERATED_MSG;
import static com.tonnfccard.TonWalletConstants.MAX_PIN_TRIES;
import static com.tonnfccard.TonWalletConstants.NOT_GENERATED_MSG;
import static com.tonnfccard.TonWalletConstants.PIN_SIZE;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_DEVICE_LABEL_LEN_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_DEVICE_LABEL_NOT_HEX;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_GET_APPLET_LIST_RESPONSE_LEN_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_GET_AVAILABLE_MEMORY_RESPONSE_LEN_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_GET_CSN_RESPONSE_LEN_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_GET_DEVICE_LABEL_RESPONSE_LEN_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_GET_PIN_TLT_OR_RTL_RESPONSE_LEN_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_GET_PIN_TLT_OR_RTL_RESPONSE_VAL_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_GET_ROOT_KEY_STATUS_RESPONSE_LEN_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_GET_SE_VERSION_RESPONSE_LEN_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_PIN_FORMAT_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_PIN_LEN_INCORRECT;
import static com.tonnfccard.smartcard.CoinManagerApduCommands.*;
import static com.tonnfccard.smartcard.CoinManagerApduCommands.getChangePinAPDU;
import static com.tonnfccard.smartcard.CoinManagerApduCommands.getGenerateSeedAPDU;
import static com.tonnfccard.smartcard.CoinManagerApduCommands.getSetDeviceLabelAPDU;

/**
 * Class containing functions-wrappers for card operations belonging to CoinManager entity.
 *
 * Here there are some utility functions: setDeviceLabel, getDeviceLabel, getSeVersion (SE = secure element),
 * getCsn (CSN = SecureElement id), getAvailableMemory, getAppsList. We do not use them now.
 *
 * There are important functions related to ed25519 seed maintaining : getRootKeyStatus (show if seed is generated),
 * resetWallet (reset seed and PIN), generateSeed.
 *
 * And also there are functions related to PIN protecting ed25519 signature: getMaxPinTries, getRemainingPinTries, changePin.
 */

public final class CardCoinManagerApi extends TonWalletApi {
  private static final String TAG = "CardCoinManagerNfcApi";


  public CardCoinManagerApi(Context activity, NfcApduRunner apduRunner) {
    super(activity, apduRunner);
  }

  /**
   * @param deviceLabel
   * @param callback
   * This function is used to set the device label. Now we do not use this device label stored in CoinManager.
   */

  public void setDeviceLabel(final String deviceLabel, final NfcCallback callback, Boolean... showDialog) {
    new Thread(new Runnable() {
      public void run() {
        try {
          String json = setDeviceLabelAndGetJson(deviceLabel, showDialog);
          resolveJson(json, callback);
          Log.d(TAG, "setDeviceLabel response : " + json);
        } catch (Exception e) {
            EXCEPTION_HELPER.handleException(e, callback, TAG);
        }
      }
    }).start();
  }

  /**
   *
   * @param deviceLabel
   * @return
   * @throws Exception
   * This function is used to set the device label. Now we do not use this device label stored in CoinManager.
   */

  public String setDeviceLabelAndGetJson(final String deviceLabel, Boolean... showDialog) throws Exception {
    try {
      //long start = System.currentTimeMillis();
      if (!STR_HELPER.isHexString(deviceLabel))
        throw new IllegalArgumentException(ERROR_MSG_DEVICE_LABEL_NOT_HEX);
      if (deviceLabel.length() != 2 * LABEL_LENGTH)
        throw new IllegalArgumentException(ERROR_MSG_DEVICE_LABEL_LEN_INCORRECT);
      boolean showDialogFlag = showDialog.length > 0 ? showDialog[0] : true;
      if (showDialogFlag) openInvitationDialog();
      apduRunner.sendCoinManagerAppletAPDU(getSetDeviceLabelAPDU(BYTE_ARR_HELPER.bytes(deviceLabel)));
      String json = JSON_HELPER.createResponseJson(DONE_MSG);
      if (showDialogFlag) closeInvitationDialog();
      //long end = System.currentTimeMillis();
      //Log.d("TAG", "!!Time = " + String.valueOf(end - start) );
      return json;
    }
    catch (Exception e) {
      closeInvitationDialogWithFail();
      throw new Exception(EXCEPTION_HELPER.makeFinalErrMsg(e), e);
    }
  }

  /**
   * @param callback
   * This function is used to get device label. Now we do not use this device label stored in CoinManager.
   */
  public void getDeviceLabel(final NfcCallback callback, Boolean... showDialog) {
    new Thread(new Runnable() {
      public void run() {
        try {
          String json = getDeviceLabelAndGetJson(showDialog);
          resolveJson(json, callback);
          Log.d(TAG, "getDeviceLabel response : " + json);
        } catch (Exception e) {
            EXCEPTION_HELPER.handleException(e, callback, TAG);
        }
      }
    }).start();
  }

  /**
   * @return
   * @throws Exception
   * This function is used to get device label. Now we do not use this device label stored in CoinManager.
   */
  public String getDeviceLabelAndGetJson(Boolean... showDialog) throws Exception {
    try {
      //long start = System.currentTimeMillis();
      boolean showDialogFlag = showDialog.length > 0 ? showDialog[0] : true;
      if (showDialogFlag) openInvitationDialog();
      RAPDU rapdu = apduRunner.sendCoinManagerAppletAPDU(GET_DEVICE_LABEL_APDU);
      if (rapdu == null || rapdu.getData() == null || rapdu.getData().length != LABEL_LENGTH)
        throw new Exception(ERROR_MSG_GET_DEVICE_LABEL_RESPONSE_LEN_INCORRECT);
      String response = BYTE_ARR_HELPER.hex(rapdu.getData());
      String json = JSON_HELPER.createResponseJson(response);
      if (showDialogFlag) closeInvitationDialog();
      //long end = System.currentTimeMillis();
      //Log.d("TAG", "!!Time = " + String.valueOf(end - start) );
      return json;
    }
    catch (Exception e) {
      closeInvitationDialogWithFail();
      throw new Exception(EXCEPTION_HELPER.makeFinalErrMsg(e), e);
    }
  }

  /**
   * @param callback
   * This function is used to get SE (secure element) version.
   */
  public void getSeVersion(final NfcCallback callback, Boolean... showDialog) {
    new Thread(new Runnable() {
      public void run() {
        try {
          String json = getSeVersionAndGetJson(showDialog);
          resolveJson(json, callback);
          Log.d(TAG, "getSeVersion response : " + json);
        } catch (Exception e) {
            EXCEPTION_HELPER.handleException(e, callback, TAG);
        }
      }
    }).start();
  }

  /**
   * @return
   * @throws Exception
   * This function is used to get SE (secure element) version.
   */
  public String getSeVersionAndGetJson(Boolean... showDialog) throws Exception {
    try {
      //long start = System.currentTimeMillis();
      boolean showDialogFlag = showDialog.length > 0 ? showDialog[0] : true;
      if (showDialogFlag) openInvitationDialog();
      String json = executeCoinManagerOperationAndSendHex(GET_SE_VERSION_APDU, ERROR_MSG_GET_SE_VERSION_RESPONSE_LEN_INCORRECT);
      if (showDialogFlag) closeInvitationDialog();
      //long end = System.currentTimeMillis();
      //Log.d("TAG", "!!Time = " + String.valueOf(end - start) );
      return json;
    }
    catch (Exception e) {
      closeInvitationDialogWithFail();
      throw new Exception(EXCEPTION_HELPER.makeFinalErrMsg(e), e);
    }
  }

  /**
   * @param callback
   * This function is used to get CSN (SEID)
   */
  public void getCsn(final NfcCallback callback, Boolean... showDialog) {
    new Thread(new Runnable() {
      public void run() {
        try {
          String json = getCsnAndGetJson(showDialog);
          resolveJson(json, callback);
          Log.d(TAG, "getCsn (CSN = SecureElement id) response : " + json);
        } catch (Exception e) {
            EXCEPTION_HELPER.handleException(e, callback, TAG);
        }
      }
    }).start();
  }

  /**
   * @return
   * @throws Exception
   * This function is used to get CSN (SEID)
   */
  public String getCsnAndGetJson(Boolean... showDialog) throws Exception {
    try {
      boolean showDialogFlag = showDialog.length > 0 ? showDialog[0] : true;
      if (showDialogFlag) openInvitationDialog();
      // long start = System.currentTimeMillis();
      String json = executeCoinManagerOperationAndSendHex(GET_CSN_APDU, ERROR_MSG_GET_CSN_RESPONSE_LEN_INCORRECT);
      if (showDialogFlag) closeInvitationDialog();
      //  long end = System.currentTimeMillis();
      // Log.d("TAG", "!!Time = " + String.valueOf(end - start) );
      return json;
    }
    catch (Exception e) {
      closeInvitationDialogWithFail();
      throw new Exception(EXCEPTION_HELPER.makeFinalErrMsg(e), e);
    }
  }

  /**
   * @param callback
   * This function is used to get retry maximum times of PIN.
   */
  public void getMaxPinTries(final NfcCallback callback, Boolean... showDialog) {
    new Thread(new Runnable() {
      public void run() {
        try {
          String json = getMaxPinTriesAndGetJson(showDialog);
          resolveJson(json, callback);
          Log.d(TAG, "getMaxPinTries response : " + json);
        } catch (Exception e) {
            EXCEPTION_HELPER.handleException(e, callback, TAG);
        }
      }
    }).start();
  }

  /**
   * @return
   * @throws Exception
   * This function is used to get retry maximum times of PIN.
   */
  public String getMaxPinTriesAndGetJson(Boolean... showDialog) throws Exception {
    try {
      boolean showDialogFlag = showDialog.length > 0 ? showDialog[0] : true;
      if (showDialogFlag) openInvitationDialog();
      //long start = System.currentTimeMillis();
      String json = getPinTries(GET_PIN_TLT_APDU);
      //long end = System.currentTimeMillis();
      if (showDialogFlag) closeInvitationDialog();
      //Log.d("TAG", "!!Time = " + String.valueOf(end - start) );
      return json;
    }
    catch (Exception e) {
      closeInvitationDialogWithFail();
      throw new Exception(EXCEPTION_HELPER.makeFinalErrMsg(e), e);
    }
  }

  /**
   * @param callback
   * This function is used to get remaining retry times of PIN.
   */
  public void getRemainingPinTries(final NfcCallback callback, Boolean... showDialog) {
    new Thread(new Runnable() {
      public void run() {
        try {
          String json = getRemainingPinTriesAndGetJson(showDialog);
          resolveJson(json, callback);
          Log.d(TAG, "getRemainingPinTries response : " + json);
        } catch (Exception e) {
            EXCEPTION_HELPER.handleException(e, callback, TAG);
        }
      }
    }).start();
  }

  /**
   * @return
   * @throws Exception
   * This function is used to get remaining retry times of PIN.
   */
  public String getRemainingPinTriesAndGetJson(Boolean... showDialog) throws Exception {
    try {
      //long start = System.currentTimeMillis();
      boolean showDialogFlag = showDialog.length > 0 ? showDialog[0] : true;
      if (showDialogFlag) openInvitationDialog();
      String json = getPinTries(GET_PIN_RTL_APDU);
      if (showDialogFlag) closeInvitationDialog();
      //long end = System.currentTimeMillis();
      //Log.d("TAG", "!!Time = " + String.valueOf(end - start) );
      return json;
    }
    catch (Exception e) {
      closeInvitationDialogWithFail();
      throw new Exception(EXCEPTION_HELPER.makeFinalErrMsg(e), e);
    }
  }

  /**
   * @param callback
   * This function is used to get the status of seed for ed25519: is it generated or not.
   */
  public void getRootKeyStatus(final NfcCallback callback, Boolean... showDialog) {
    new Thread(new Runnable() {
      public void run() {
        try {
          String json = getRootKeyStatusAndGetJson(showDialog);
          resolveJson(json, callback);
          Log.d(TAG, "getRootKeyStatus response : " + json);
        } catch (Exception e) {
            EXCEPTION_HELPER.handleException(e, callback, TAG);
        }
      }
    }).start();
  }

  /**
   * @return
   * @throws Exception
   * This function is used to get the status of seed for ed25519: is it generated or not.
   */
  public String getRootKeyStatusAndGetJson(Boolean... showDialog) throws Exception {
    try {
      //long start = System.currentTimeMillis();
      boolean showDialogFlag = showDialog.length > 0 ? showDialog[0] : true;
      if (showDialogFlag) openInvitationDialog();
      RAPDU rapdu = apduRunner.sendCoinManagerAppletAPDU(GET_ROOT_KEY_STATUS_APDU);
      if (rapdu == null || rapdu.getData() == null || rapdu.getData().length == 0)
        throw new Exception(ERROR_MSG_GET_ROOT_KEY_STATUS_RESPONSE_LEN_INCORRECT);
      String response = BYTE_ARR_HELPER.hex(rapdu.getData()).equals(POSITIVE_ROOT_KEY_STATUS) ? GENERATED_MSG : NOT_GENERATED_MSG;
      String json = JSON_HELPER.createResponseJson(response);
      if (showDialogFlag) closeInvitationDialog();
      //long end = System.currentTimeMillis();
      //Log.d("TAG", "!!Time = " + String.valueOf(end - start) );
      return json;
    }
    catch (Exception e) {
      closeInvitationDialogWithFail();
      throw new Exception(EXCEPTION_HELPER.makeFinalErrMsg(e), e);
    }
  }

  /**
   * @param callback
   * This function is used to reset the wallet state to the initial state. After resetting the wallet, the default PIN value would be 5555. The remaining retry for the PIN will be reset to MAX (default is 10). The seed for ed25519 will be erased.
   * And after its calling any card operation (except of CoinManager stuff) will fail with 6F02 error.
   * TON Labs wallet applet does not work without seed.
   */
  public void resetWallet(final NfcCallback callback, Boolean... showDialog) {
    new Thread(new Runnable() {
      public void run() {
        try {
          String json = resetWalletAndGetJson(showDialog);
          resolveJson(json, callback);
          Log.d(TAG, "resetWallet response : " + json);
        } catch (Exception e) {
            EXCEPTION_HELPER.handleException(e, callback, TAG);
        }
      }
    }).start();
  }

  /**
   * @return
   * @throws Exception
   * This function is used to reset the wallet state to the initial state. After resetting the wallet, the default PIN value would be 5555.
   * The remaining retry for the PIN will be reset to MAX (default is 10). The seed for ed25519 will be erased. And after its calling any card operation (except of CoinManager stuff) will fail with 6F02 error.
   * TON Labs wallet applet does not work without seed.
   */
  public String resetWalletAndGetJson(Boolean... showDialog) throws Exception {
    try {
     // long start = System.currentTimeMillis();
      boolean showDialogFlag = showDialog.length > 0 ? showDialog[0] : true;
      if (showDialogFlag) openInvitationDialog();
      apduRunner.sendCoinManagerAppletAPDU(RESET_WALLET_APDU);
      String json = JSON_HELPER.createResponseJson(DONE_MSG);
      if (showDialogFlag) closeInvitationDialog();
     // long end = System.currentTimeMillis();
     // Log.d("TAG", "!!Time = " + String.valueOf(end - start) );
      return json;
    }
    catch (Exception e) {
      closeInvitationDialogWithFail();
      throw new Exception(EXCEPTION_HELPER.makeFinalErrMsg(e), e);
    }
  }

  /**
   * @param callback
   * This function is used to obtain the amount of memory of the specified type that is available to the applet.
   * Note that implementation-dependent memory overhead structures may also use the same memory pool.
   */
  public void getAvailableMemory(final NfcCallback callback, Boolean... showDialog) {
    new Thread(new Runnable() {
      public void run() {
        try {
          String json = getAvailableMemoryAndGetJson(showDialog);
          resolveJson(json, callback);
          Log.d(TAG, "getAvailableMemory response : " + json);
        } catch (Exception e) {
            EXCEPTION_HELPER.handleException(e, callback, TAG);
        }
      }
    }).start();
  }

  /**
   * @return
   * @throws Exception
   * This function is used to obtain the amount of memory of the specified type that is available to the applet.
   * Note that implementation-dependent memory overhead structures may also use the same memory pool.
   */
  public String getAvailableMemoryAndGetJson(Boolean... showDialog) throws Exception {
    try {
      // long start = System.currentTimeMillis();
      boolean showDialogFlag = showDialog.length > 0 ? showDialog[0] : true;
      if (showDialogFlag) openInvitationDialog();
      String json = executeCoinManagerOperationAndSendHex(GET_AVAILABLE_MEMORY_APDU, ERROR_MSG_GET_AVAILABLE_MEMORY_RESPONSE_LEN_INCORRECT);
      if (showDialogFlag) closeInvitationDialog();
      // long end = System.currentTimeMillis();
      //Log.d("TAG", "!!Time = " + String.valueOf(end - start) );
      return json;
    }
    catch (Exception e) {
      closeInvitationDialogWithFail();
      throw new Exception(EXCEPTION_HELPER.makeFinalErrMsg(e), e);
    }
  }

  /**
   * @param callback
   * This function is used to get application list. It returns list of applets AIDs that were installed onto card.
   */
  public void getAppsList(final NfcCallback callback, Boolean... showDialog) {
    new Thread(new Runnable() {
      public void run() {
        try {
          String json = getAppsListAndGetJson(showDialog);
          resolveJson(json, callback);
          Log.d(TAG, "getAppsList response : " + json);
        } catch (Exception e) {
            EXCEPTION_HELPER.handleException(e, callback, TAG);
        }
      }
    }).start();
  }

  /**
   * @return
   * @throws Exception
   * This function is used to get application list. It returns list of applets AIDs that were installed onto card.
   */
  public String getAppsListAndGetJson(Boolean... showDialog) throws Exception {
    try {
      //long start = System.currentTimeMillis();
      boolean showDialogFlag = showDialog.length > 0 ? showDialog[0] : true;
      if (showDialogFlag) openInvitationDialog();
      String json = executeCoinManagerOperationAndSendHex(GET_APPLET_LIST_APDU, ERROR_MSG_GET_APPLET_LIST_RESPONSE_LEN_INCORRECT);
      if (showDialogFlag) closeInvitationDialog();
      //long end = System.currentTimeMillis();
      // Log.d("TAG", "!!Time = " + String.valueOf(end - start) );
      return json;
    }
    catch (Exception e) {
      closeInvitationDialogWithFail();
      throw new Exception(EXCEPTION_HELPER.makeFinalErrMsg(e), e);
    }
  }

  /**
   * @param pin
   * @param callback
   * This function is used to generate the seed for ed25519 with RNG.
   */
  public void generateSeed(final String pin, final NfcCallback callback, Boolean... showDialog) {
    new Thread(new Runnable() {
      public void run() {
        try {
          String json = generateSeedAndGetJson(pin, showDialog);
          resolveJson(json, callback);
          Log.d(TAG, "generateSeed response : " + json);
        } catch (Exception e) {
            EXCEPTION_HELPER.handleException(e, callback, TAG);
        }
      }
    }).start();
  }

  /**
   * @param pin
   * @return
   * @throws Exception
   * This function is used to generate the seed for ed25519 with RNG.
   */
  public String generateSeedAndGetJson(final String pin, Boolean... showDialog) throws Exception {
    try {
      //long start = System.currentTimeMillis();
      if (!STR_HELPER.isNumericString(pin))
        throw new IllegalArgumentException(ERROR_MSG_PIN_FORMAT_INCORRECT);
      if (pin.length() != PIN_SIZE)
        throw new IllegalArgumentException(ERROR_MSG_PIN_LEN_INCORRECT);
      boolean showDialogFlag = showDialog.length > 0 ? showDialog[0] : true;
      if (showDialogFlag) openInvitationDialog();
      apduRunner.sendCoinManagerAppletAPDU(getGenerateSeedAPDU(BYTE_ARR_HELPER.bytes(STR_HELPER.pinToHex(pin))));
      String json = JSON_HELPER.createResponseJson(DONE_MSG);
      if (showDialogFlag) closeInvitationDialog();
      //long end = System.currentTimeMillis();
     // Log.d("TAG", "!!Time = " + String.valueOf(end - start) );
      return json;
    }
    catch (Exception e) {
      closeInvitationDialogWithFail();
      throw new Exception(EXCEPTION_HELPER.makeFinalErrMsg(e), e);
    }
  }

  /**
   * @param oldPin
   * @param newPin
   * @param callback
   * This function is used to change device PIN.
   */
  public void changePin(final String oldPin, final String newPin, final NfcCallback callback, Boolean... showDialog) {
    new Thread(new Runnable() {
      public void run() {
        try {
          String json =  changePinAndGetJson(oldPin, newPin,  showDialog);
          resolveJson(json, callback);
          Log.d(TAG, "changePin response : " + json);
        } catch (Exception e) {
            EXCEPTION_HELPER.handleException(e, callback, TAG);
        }
      }
    }).start();
  }

  /**
   * @param oldPin
   * @param newPin
   * @return
   * @throws Exception
   * This function is used to change device PIN.
   */
  public String changePinAndGetJson(final String oldPin, final String newPin, Boolean... showDialog) throws Exception {
    try {
      //long start = System.currentTimeMillis();
      if (!STR_HELPER.isNumericString(newPin) || !STR_HELPER.isNumericString(oldPin))
        throw new Exception(ERROR_MSG_PIN_FORMAT_INCORRECT);
      if (oldPin.length() != PIN_SIZE || newPin.length() != PIN_SIZE)
        throw new Exception(ERROR_MSG_PIN_LEN_INCORRECT);
      boolean showDialogFlag = showDialog.length > 0 ? showDialog[0] : true;
      if (showDialogFlag) openInvitationDialog();
      apduRunner.sendCoinManagerAppletAPDU(getChangePinAPDU(BYTE_ARR_HELPER.bytes(STR_HELPER.pinToHex(oldPin)), BYTE_ARR_HELPER.bytes(STR_HELPER.pinToHex(newPin))));
      String json = JSON_HELPER.createResponseJson(DONE_MSG);
      if (showDialogFlag) closeInvitationDialog();
      //long end = System.currentTimeMillis();
      //Log.d("TAG", "!!Time = " + String.valueOf(end - start) );
      return json;
    }
    catch (Exception e) {
      closeInvitationDialogWithFail();
      throw new Exception(EXCEPTION_HELPER.makeFinalErrMsg(e), e);
    }
  }

  private String executeCoinManagerOperationAndSendHex(CAPDU apdu, String errMsg) throws Exception {
    try {
      RAPDU rapdu = apduRunner.sendCoinManagerAppletAPDU(apdu);
      if (rapdu == null || rapdu.getData() == null || rapdu.getData().length == 0)
        throw new Exception(errMsg);
      String response = BYTE_ARR_HELPER.hex(rapdu.getData());
      return JSON_HELPER.createResponseJson(response);
    }
    catch (Exception e) {
      throw new Exception(EXCEPTION_HELPER.makeFinalErrMsg(e), e);
    }
  }

  private String getPinTries(CAPDU apdu) throws Exception {
    try {
      RAPDU rapdu = apduRunner.sendCoinManagerAppletAPDU(apdu);
      if (rapdu == null || rapdu.getData() == null || rapdu.getData().length == 0)
        throw new Exception(ERROR_MSG_GET_PIN_TLT_OR_RTL_RESPONSE_LEN_INCORRECT);
      byte res = rapdu.getData()[0];
      if (res < 0 || res > MAX_PIN_TRIES) {
        throw new Exception(ERROR_MSG_GET_PIN_TLT_OR_RTL_RESPONSE_VAL_INCORRECT);
      }
      return JSON_HELPER.createResponseJson(Byte.toString(res));
    }
    catch (Exception e) {
      throw new Exception(EXCEPTION_HELPER.makeFinalErrMsg(e), e);
    }
  }
}
