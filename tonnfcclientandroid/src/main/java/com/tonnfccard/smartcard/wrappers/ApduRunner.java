package com.tonnfccard.smartcard.wrappers;

import android.util.Log;

import com.tonnfccard.api.utils.JsonHelper;
import com.tonnfccard.smartcard.TonWalletAppletStates;
import com.tonnfccard.smartcard.apdu.ApduHelper;
import com.tonnfccard.utils.ByteArrayHelper;

import java.util.List;

import static com.tonnfccard.api.utils.ResponsesConstants.ERROR_MSG_APDU_EMPTY;
import static com.tonnfccard.api.utils.ResponsesConstants.ERROR_MSG_APDU_NOT_SUPPORTED;
import static com.tonnfccard.api.utils.ResponsesConstants.ERROR_MSG_STATE_RESPONSE_LEN_INCORRECT;
import static com.tonnfccard.smartcard.ErrorCodes.getMsg;
import static com.tonnfccard.smartcard.TonWalletAppletConstants.getStateByIns;
import static com.tonnfccard.smartcard.apdu.CoinManagerApduCommands.SELECT_COIN_MANAGER_APDU;
import static com.tonnfccard.smartcard.apdu.TonWalletAppletApduCommands.GET_APPLET_STATE_APDU_LIST;
import static com.tonnfccard.smartcard.apdu.TonWalletAppletApduCommands.INS_GET_APP_INFO;
import static com.tonnfccard.smartcard.apdu.TonWalletAppletApduCommands.getTonWalletAppletApduCommandName;

public abstract class ApduRunner {
  private static final String TAG = "ApduRunner";
  private static final JsonHelper JSON_HELPER = JsonHelper.getInstance();
  private static final ApduHelper APDU_HELPER = ApduHelper.getInstance();
  private static final ByteArrayHelper BYTE_ARRAY_HELPER = ByteArrayHelper.getInstance();

  public ApduRunner() {
  }

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

  public RAPDU sendCoinManagerAppletAPDU(CAPDU commandAPDU) throws Exception {
    sendAPDU(SELECT_COIN_MANAGER_APDU);
    return sendAPDU(commandAPDU);
  }

  public RAPDU sendTonWalletAppletAPDU(CAPDU commandAPDU) throws Exception {
    RAPDU response = sendAPDUList(GET_APPLET_STATE_APDU_LIST);
    if (response.getData().length != 0x01) throw new Exception(ERROR_MSG_STATE_RESPONSE_LEN_INCORRECT);
    if (commandAPDU.getIns() == INS_GET_APP_INFO) return response;
    TonWalletAppletStates appletState = TonWalletAppletStates.findByStateValue(response.getData()[0]);
    byte ins = commandAPDU.getIns();
    if (!getStateByIns(ins).contains(appletState)) {
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

    String swFormatted = BYTE_ARRAY_HELPER.hex(new byte[]{rapdu.getSW1()})
      + BYTE_ARRAY_HELPER.hex(new byte[]{rapdu.getSW2()});

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
