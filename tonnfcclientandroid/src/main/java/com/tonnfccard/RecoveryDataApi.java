package com.tonnfccard;

import android.content.Context;
import android.util.Log;

import com.tonnfccard.callback.NfcCallback;
import com.tonnfccard.helpers.CardApiInterface;
import com.tonnfccard.nfc.NfcApduRunner;
import com.tonnfccard.smartcard.ApduRunner;
import com.tonnfccard.smartcard.RAPDU;

import java.util.Collections;
import java.util.List;

import static com.tonnfccard.TonWalletConstants.*;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_IS_RECOVERY_DATA_SET_RESPONSE_LEN_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_RECOVERY_DATA_HASH_RESPONSE_LEN_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_RECOVERY_DATA_LENGTH_RESPONSE_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_RECOVERY_DATA_LENGTH_RESPONSE_LEN_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_RECOVERY_DATA_LEN_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_RECOVERY_DATA_NOT_HEX;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_RECOVERY_DATA_PORTION_INCORRECT_LEN;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.GET_RECOVERY_DATA_HASH_APDU;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.GET_RECOVERY_DATA_LEN_APDU;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.IS_RECOVERY_DATA_SET_APDU;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.RESET_RECOVERY_DATA_APDU;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.getAddRecoveryDataPartAPDU;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.getGetRecoveryDataPartAPDU;

/**
 * Class containing functions-wrappers for card operations related to recovery data service: resetRecoveryData, getRecoveryDataHash (SHA256), getRecoveryDataLen,
 * isRecoveryDataSet, addRecoveryData, getRecoveryData.
 * Use them to add/get/check state of recovery data on security card.
 */

public final class RecoveryDataApi extends TonWalletApi {
  private static final String TAG = "RecoveryDataApi";

  public RecoveryDataApi(Context activity,  NfcApduRunner apduRunner) {
    super(activity, apduRunner);
  }

  public RecoveryDataApi(NfcApduRunner apduRunner) {
    super(apduRunner);
  }

  /**
   * @param callback
   * Read actual recovery data length.
   */
  private final CardApiInterface<List<String>> resetRecoveryData = list -> this.resetRecoveryDataAndGetJson();

  public void resetRecoveryData(final NfcCallback callback, Boolean... showDialog) {
    boolean showDialogFlag = showDialog.length > 0 ? showDialog[0] : false;
    CardTask cardTask = new CardTask(this, callback,  Collections.emptyList(), resetRecoveryData, showDialogFlag);
    cardTask.execute();
  }

  /**
   * @return
   * @throws Exception
   * Read actual recovery data length.
   */
  public String resetRecoveryDataAndGetJson() throws Exception {
    try {
      //long start = System.currentTimeMillis();
      resetRecoveryData();
      String json = JSON_HELPER.createResponseJson(DONE_MSG);
      //long end = System.currentTimeMillis();
      //Log.d("TAG", "!!Time = " + String.valueOf(end - start) );
      return  json;
    }
    catch (Exception e) {
      throw new Exception(EXCEPTION_HELPER.makeFinalErrMsg(e), e);
    }
  }

  private RAPDU resetRecoveryData() throws Exception {
    return apduRunner.sendTonWalletAppletAPDU(RESET_RECOVERY_DATA_APDU);
  }

  /**
   * @param callback
   * Read recovery data SHA256 hash.
   */
  private final CardApiInterface<List<String>> getRecoveryDataHash = list -> this.getRecoveryDataHashAndGetJson();

  public void getRecoveryDataHash(final NfcCallback callback, Boolean... showDialog) {
    boolean showDialogFlag = showDialog.length > 0 ? showDialog[0] : false;
    CardTask cardTask = new CardTask(this, callback,  Collections.emptyList(), getRecoveryDataHash, showDialogFlag);
    cardTask.execute();
  }

  /**
   * @return
   * @throws Exception
   * Read recovery data SHA256 hash.
   */
  public String getRecoveryDataHashAndGetJson() throws Exception {
    try {
      //long start = System.currentTimeMillis();
      String response = BYTE_ARR_HELPER.hex(getRecoveryDataHash().getData());
      String json = JSON_HELPER.createResponseJson(response);
      //long end = System.currentTimeMillis();
      //Log.d("TAG", "!!Time = " + String.valueOf(end - start) );
      return json;
    }
    catch (Exception e) {
      throw new Exception(EXCEPTION_HELPER.makeFinalErrMsg(e), e);
    }
  }

  private RAPDU getRecoveryDataHash() throws Exception {
    RAPDU rapdu = apduRunner.sendTonWalletAppletAPDU(GET_RECOVERY_DATA_HASH_APDU);
    if (rapdu == null || rapdu.getData() == null || rapdu.getData().length != SHA_HASH_SIZE) throw new Exception(ERROR_MSG_RECOVERY_DATA_HASH_RESPONSE_LEN_INCORRECT);
    return rapdu;
  }

  /**
   * @param callback
   * Read actual recovery data length.
   */
  private final CardApiInterface<List<String>> getRecoveryDataLen = list -> this.getRecoveryDataLenAndGetJson();

  public void getRecoveryDataLen(final NfcCallback callback, Boolean... showDialog) {
    boolean showDialogFlag = showDialog.length > 0 ? showDialog[0] : false;
    CardTask cardTask = new CardTask(this, callback,  Collections.emptyList(), getRecoveryDataLen, showDialogFlag);
    cardTask.execute();
  }

  /**
   * @return
   * @throws Exception
   * Read actual recovery data length.
   */
  public String getRecoveryDataLenAndGetJson() throws Exception {
    try {
      //long start = System.currentTimeMillis();
      String response = Integer.valueOf(getRecoveryDataLen()).toString();
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
   * @return
   * @throws Exception
   * Read actual recovery data length.
   */
  private int getRecoveryDataLen() throws Exception {
    RAPDU rapdu = apduRunner.sendTonWalletAppletAPDU(GET_RECOVERY_DATA_LEN_APDU);
    if (rapdu == null || rapdu.getData() == null || rapdu.getData().length != 0x02)
      throw new Exception(ERROR_MSG_RECOVERY_DATA_LENGTH_RESPONSE_LEN_INCORRECT);
    short len = BYTE_ARR_HELPER.makeShort(rapdu.getData(), 0);
    if (len <= 0 || len > RECOVERY_DATA_MAX_SIZE)
      throw new Exception(ERROR_MSG_RECOVERY_DATA_LENGTH_RESPONSE_INCORRECT);
    return len;
  }

  /**
   * @param callback
   * Return 'true'/'false' if recovery data exists/does not exist.
   */
  private final CardApiInterface<List<String>> isRecoveryDataSet = list -> this.isRecoveryDataSetAndGetJson();

  public void isRecoveryDataSet(final NfcCallback callback, Boolean... showDialog) {
    boolean showDialogFlag = showDialog.length > 0 ? showDialog[0] : false;
    CardTask cardTask = new CardTask(this, callback,  Collections.emptyList(), isRecoveryDataSet, showDialogFlag);
    cardTask.execute();
  }

  /**
   * @return
   * @throws Exception
   * Return 'true'/'false' if recovery data exists/does not exist.
   */
  public String isRecoveryDataSetAndGetJson() throws Exception {
    try {
      //long start = System.currentTimeMillis();
      String response = isRecoveryDataSet().getData()[0] == 0 ? FALSE_MSG : TRUE_MSG;
      String json = JSON_HELPER.createResponseJson(response);
      //long end = System.currentTimeMillis();
      //Log.d("TAG", "!!Time = " + String.valueOf(end - start) );
      return json;
    }
    catch (Exception e) {
      throw new Exception(EXCEPTION_HELPER.makeFinalErrMsg(e), e);
    }
  }

  private RAPDU isRecoveryDataSet() throws Exception {
    RAPDU rapdu = apduRunner.sendTonWalletAppletAPDU(IS_RECOVERY_DATA_SET_APDU);
    if (rapdu == null || rapdu.getData() == null || rapdu.getData().length != 0x01) throw new Exception(ERROR_IS_RECOVERY_DATA_SET_RESPONSE_LEN_INCORRECT);
    return rapdu;
  }

  /**
   * @param recoveryData
   * @param callback
   * Save recovery data into applet.
   */
  private final CardApiInterface<List<String>> addRecoveryData = list -> this.addRecoveryDataAndGetJson(list.get(0));

  public void addRecoveryData(final String recoveryData, final NfcCallback callback, Boolean... showDialog) {
    boolean showDialogFlag = showDialog.length > 0 ? showDialog[0] : false;
    CardTask cardTask = new CardTask(this, callback,  Collections.singletonList(recoveryData), addRecoveryData, showDialogFlag);
    cardTask.execute();
  }

  /**
   * @param recoveryData
   * @return
   * @throws Exception
   * Save recovery data into applet.
   */
  public String addRecoveryDataAndGetJson(final String recoveryData) throws Exception {
    try {
      //long start = System.currentTimeMillis();
      if (!STR_HELPER.isHexString(recoveryData))
        throw new Exception(ERROR_MSG_RECOVERY_DATA_NOT_HEX);
      if (recoveryData.length() > 2 * RECOVERY_DATA_MAX_SIZE)
        throw new Exception(ERROR_MSG_RECOVERY_DATA_LEN_INCORRECT);
      addRecoveryData(BYTE_ARR_HELPER.bytes(recoveryData));
      String json = JSON_HELPER.createResponseJson(DONE_MSG);
     // long end = System.currentTimeMillis();
      //Log.d("TAG", "!!Time = " + String.valueOf(end - start) );
      return json;
    }
    catch (Exception e) {
      throw new Exception(EXCEPTION_HELPER.makeFinalErrMsg(e), e);
    }
  }

  /**
   * @param recoveryData
   * @throws Exception
   * Save recovery data into applet.
   */
  private void addRecoveryData(byte[] recoveryData) throws Exception {
    int numberOfPackets = recoveryData.length / DATA_RECOVERY_PORTION_MAX_SIZE;
    for (int i = 0; i < numberOfPackets; i++) {
      System.out.println("packet#" + i);
      byte[] chunk = BYTE_ARR_HELPER.bSub(recoveryData, i * DATA_RECOVERY_PORTION_MAX_SIZE, DATA_RECOVERY_PORTION_MAX_SIZE);
      byte p1 = i == 0 ? (byte) 0x00 : (byte) 0x01;
      apduRunner.sendTonWalletAppletAPDU(getAddRecoveryDataPartAPDU(p1, chunk));
    }

    int tailLen = recoveryData.length % DATA_RECOVERY_PORTION_MAX_SIZE;

    if (tailLen > 0) {
      System.out.println("tail#");
      byte[] chunk = BYTE_ARR_HELPER.bSub(recoveryData, numberOfPackets * DATA_RECOVERY_PORTION_MAX_SIZE, tailLen);
      byte p1 = numberOfPackets == 0 ? (byte) 0x00 : (byte) 0x01;
      apduRunner.sendTonWalletAppletAPDU(getAddRecoveryDataPartAPDU(p1, chunk));
    }

    byte[] hash = digest.digest(recoveryData);
    apduRunner.sendTonWalletAppletAPDU(getAddRecoveryDataPartAPDU((byte) 0x02, hash));
  }

  /**
   * @param callback
   * Read recovery data from TON Wallet applet.
   */
  private final CardApiInterface<List<String>> getRecoveryData = list -> this.getRecoveryDataAndGetJson();

  public void getRecoveryData(final NfcCallback callback, Boolean... showDialog) {
    boolean showDialogFlag = showDialog.length > 0 ? showDialog[0] : false;
    CardTask cardTask = new CardTask(this, callback,  Collections.emptyList(), getRecoveryData, showDialogFlag);
    cardTask.execute();
  }

  /**
   * @return
   * @throws Exception
   * Read recovery data from TON Wallet applet.
   */
  public String getRecoveryDataAndGetJson() throws Exception {
    try {
      //long start = System.currentTimeMillis();
      String response = BYTE_ARR_HELPER.hex(getRecoveryData());
      String json = JSON_HELPER.createResponseJson(response);
      //long end = System.currentTimeMillis();
      //Log.d("TAG", "!!Time = " + String.valueOf(end - start) );
      return json;
    }
    catch (Exception e) {
      e.printStackTrace();
      throw new Exception(EXCEPTION_HELPER.makeFinalErrMsg(e), e);
    }
  }

  private byte[] getRecoveryData() throws Exception {
    int len = getRecoveryDataLen();
    byte[] recoveryData = new byte[len];
    int numberOfPackets = len / DATA_RECOVERY_PORTION_MAX_SIZE;
    short startPos = 0;
    Log.d(TAG, "numberOfPackets = " + numberOfPackets);
    System.out.println("00");
    for (int i = 0; i < numberOfPackets; i++) {
      Log.d(TAG, "packet " + i);
      byte[] dataChunk = new byte[]{(byte) (startPos >> 8), (byte) (startPos)};
      RAPDU rapdu = apduRunner.sendTonWalletAppletAPDU(getGetRecoveryDataPartAPDU(dataChunk, (byte) DATA_RECOVERY_PORTION_MAX_SIZE));
      if (rapdu == null || rapdu.getData() == null || rapdu.getData().length != DATA_RECOVERY_PORTION_MAX_SIZE) throw new Exception(ERROR_RECOVERY_DATA_PORTION_INCORRECT_LEN + DATA_RECOVERY_PORTION_MAX_SIZE);
      byte[] res = rapdu.getData();
      BYTE_ARR_HELPER.arrayCopy(res, 0, recoveryData, startPos, DATA_RECOVERY_PORTION_MAX_SIZE);
      startPos += DATA_RECOVERY_PORTION_MAX_SIZE;
    }
    int tailLen = len % DATA_RECOVERY_PORTION_MAX_SIZE;
    if (tailLen > 0) {
      byte[] dataChunk = new byte[]{(byte) (startPos >> 8), (byte) (startPos)};
      RAPDU rapdu = apduRunner.sendTonWalletAppletAPDU(getGetRecoveryDataPartAPDU(dataChunk, (byte) tailLen));
      if (rapdu == null || rapdu.getData() == null || rapdu.getData().length != tailLen) throw new Exception(ERROR_RECOVERY_DATA_PORTION_INCORRECT_LEN + tailLen);
      byte[] res = rapdu.getData();
      BYTE_ARR_HELPER.arrayCopy(res, 0, recoveryData, startPos, tailLen);
    }
    return recoveryData;
  }

}

