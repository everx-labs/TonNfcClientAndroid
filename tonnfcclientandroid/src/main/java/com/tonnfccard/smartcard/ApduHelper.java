package com.tonnfccard.smartcard;

import androidx.annotation.RestrictTo;
import com.tonnfccard.utils.ByteArrayUtil;

import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_CAPDU_IS_NULL;
import static com.tonnfccard.smartcard.CoinManagerApduCommands.COIN_MANAGER_CLA;
import static com.tonnfccard.smartcard.CoinManagerApduCommands.getCoinManagerApduCommandName;
import static com.tonnfccard.smartcard.CommonConstants.SELECT_CLA;
import static com.tonnfccard.smartcard.CommonConstants.SELECT_COIN_MANAGER_APDU_NAME;
import static com.tonnfccard.smartcard.CommonConstants.SELECT_INS;
import static com.tonnfccard.smartcard.CommonConstants.SELECT_P1;
import static com.tonnfccard.smartcard.CommonConstants.SELECT_P2;
import static com.tonnfccard.smartcard.CommonConstants.SELECT_TON_WALLET_APPLET_APDU_NAME;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.WALLET_APPLET_CLA;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.getTonWalletAppletApduCommandName;

/**
 *  This utility class is to get the name of any APDU command.
 */

@RestrictTo(RestrictTo.Scope.LIBRARY)
public class ApduHelper {

  private static final ByteArrayUtil BYTE_ARRAY_HELPER = ByteArrayUtil.getInstance();

  private static ApduHelper instance;

  public static ApduHelper getInstance() {
    if (instance == null) {
      instance = new ApduHelper();
    }
    return instance;
  }

  private ApduHelper() {}

  //Get the name of any APDU command, return null if command is not recognized
  public String getApduCommandName(CAPDU commandAPDU) {
    if (commandAPDU == null) throw new IllegalArgumentException(ERROR_MSG_CAPDU_IS_NULL);
    if (isSelectAPDU(commandAPDU)) {
      if (commandAPDU.getData().length == 0)
        return SELECT_COIN_MANAGER_APDU_NAME;
      else return SELECT_TON_WALLET_APPLET_APDU_NAME;
    }
    if (commandAPDU.getCla() == WALLET_APPLET_CLA && getTonWalletAppletApduCommandName(commandAPDU.getIns()) != null)
      return getTonWalletAppletApduCommandName(commandAPDU.getIns());
    if (commandAPDU.getCla() == COIN_MANAGER_CLA)
      return getCoinManagerApduCommandName(BYTE_ARRAY_HELPER.hex(commandAPDU.getData()));
    return null;
  }

  private boolean isSelectAPDU(CAPDU commandAPDU) {
    return commandAPDU.getCla() == SELECT_CLA && commandAPDU.getIns() == SELECT_INS &&  commandAPDU.getP1() == SELECT_P1 &&  commandAPDU.getP2() ==  SELECT_P2;
  }
}
