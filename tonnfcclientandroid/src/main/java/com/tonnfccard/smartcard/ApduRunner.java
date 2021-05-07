package com.tonnfccard.smartcard;

import android.content.Intent;
import android.util.Log;

import androidx.annotation.RestrictTo;

import com.tonnfccard.helpers.JsonHelper;
import com.tonnfccard.utils.ByteArrayUtil;

import java.util.List;

import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_APDU_EMPTY;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_APDU_NOT_SUPPORTED;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_STATE_RESPONSE_LEN_INCORRECT;
import static com.tonnfccard.smartcard.CommonConstants.SELECT_CLA;
import static com.tonnfccard.smartcard.CommonConstants.SELECT_INS;
import static com.tonnfccard.smartcard.ErrorCodes.getMsg;
import static com.tonnfccard.smartcard.CoinManagerApduCommands.SELECT_COIN_MANAGER_APDU;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.GET_APPLET_STATE_APDU_LIST;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.INS_GET_APP_INFO;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.SELECT_TON_WALLET_APPLET_APDU;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.getTonWalletAppletApduCommandName;


@RestrictTo(RestrictTo.Scope.LIBRARY)
public abstract class ApduRunner {
  private static final String TAG = "ApduRunner";
  private static final JsonHelper JSON_HELPER = JsonHelper.getInstance();
  private static final ApduHelper APDU_HELPER = ApduHelper.getInstance();
  private static final ByteArrayUtil BYTE_ARRAY_HELPER = ByteArrayUtil.getInstance();

  public ApduRunner() {
  }

  public abstract boolean setCardTag(Intent intent) throws Exception;

  public abstract void disconnectCard() throws Exception;

  public abstract RAPDU transmitCommand(CAPDU commandAPDU) throws Exception;

  public RAPDU sendAPDUList(List<CAPDU> apduList) throws Exception {
    System.out.println(apduList);
    RAPDU result = null;
    for (CAPDU apdu : apduList) {
      result = sendAPDU(apdu);
    }
    return result;
  }

  public RAPDU selectCoinManagerApplet() throws Exception {
    return sendAPDU(SELECT_COIN_MANAGER_APDU);
  }

  public RAPDU sendCoinManagerAppletAPDU(CAPDU commandAPDU) throws Exception {
    if (commandAPDU == null) throw new Exception(ERROR_MSG_APDU_EMPTY);
    sendAPDU(SELECT_COIN_MANAGER_APDU);
    return sendAPDU(commandAPDU);
  }

  public RAPDU sendTonWalletAppletAPDU(CAPDU commandAPDU) throws Exception {
    if (commandAPDU == null) throw new Exception(ERROR_MSG_APDU_EMPTY);
    if (commandAPDU.getCla() == SELECT_CLA && commandAPDU.getIns() == SELECT_INS) {
      return sendAPDU(SELECT_TON_WALLET_APPLET_APDU);
    }
    RAPDU response = sendAPDUList(GET_APPLET_STATE_APDU_LIST);
    if (response == null || response.getData() == null || response.getData().length != 0x01) throw new Exception(ERROR_MSG_STATE_RESPONSE_LEN_INCORRECT);
    if (commandAPDU.getIns() == INS_GET_APP_INFO ) return response;
    TonWalletAppletStates appletState = TonWalletAppletStates.findByStateValue(response.getData()[0]);
    byte ins = commandAPDU.getIns();
    if (!TonWalletAppletStates.getStateByIns(ins).contains(appletState)) {
      String errMsg = ERROR_MSG_APDU_NOT_SUPPORTED + " : " + getTonWalletAppletApduCommandName(ins) + " in " + appletState.getDescription();
      throw new Exception(errMsg);
    }
    return sendAPDU(commandAPDU);
  }

  public RAPDU sendAPDU(CAPDU commandAPDU) throws Exception {
    if (commandAPDU == null)
      throw new Exception(ERROR_MSG_APDU_EMPTY);
    Log.d(TAG, "===============================================================");
    Log.d(TAG, "===============================================================");
    StringBuilder apduFormated = commandAPDU.getFormattedApdu();

    Log.d(TAG, ">>> Send apdu  " + apduFormated);

    String apduName = APDU_HELPER.getApduCommandName(commandAPDU);
    if (apduName != null)
      Log.d(TAG, "(" + apduName + ")");
    RAPDU rapdu = transmitCommand(commandAPDU);

    StringBuilder msg = new StringBuilder();

    String swFormatted = rapdu.prepareSwFormatted();

    StringBuilder swMsg = new StringBuilder().append(swFormatted);

    if (getMsg(swFormatted) != null)
      swMsg.append(", ").append(getMsg(swFormatted));

    msg.append("SW1-SW2: " + swMsg);

    String rapduData = BYTE_ARRAY_HELPER.hex(rapdu.getData());

    if (rapduData.length() > 0)
      msg.append(", response data bytes: " + rapduData);

    Log.d(TAG, msg.toString());
    Log.d(TAG, "===============================================================");

    // Check result for success result

    if (rapdu.getSW1() != (byte) 0x90 || rapdu.getSW2() != (byte) 0x00) {
      String errMsg = JSON_HELPER.createErrorJsonForCardException(swFormatted, commandAPDU);
      throw new Exception(errMsg);
    }
    return rapdu;
  }

}
