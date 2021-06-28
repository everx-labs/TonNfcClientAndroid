package com.tonnfccard;

import android.content.Context;
import android.util.Log;

import com.tonnfccard.callback.NfcCallback;
import com.tonnfccard.helpers.CardApiInterface;
import com.tonnfccard.nfc.NfcApduRunner;
import com.tonnfccard.smartcard.ApduRunner;
import com.tonnfccard.smartcard.RAPDU;
import com.tonnfccard.utils.ByteArrayUtil;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.tonnfccard.TonWalletConstants.*;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_CARD_HAVE_INCORRECT_SN;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_DATA_FOR_SIGNING_LEN_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_DATA_FOR_SIGNING_NOT_HEX;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_DATA_FOR_SIGNING_WITH_PATH_LEN_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_HD_INDEX_FORMAT_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_HD_INDEX_LEN_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_PIN_FORMAT_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_PIN_LEN_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_PUBLIC_KEY_RESPONSE_LEN_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_SERIAL_NUMBER_LEN_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_SERIAL_NUMBER_NOT_NUMERIC;
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

  public CardCryptoApi(NfcApduRunner apduRunner) {
    super(apduRunner);
  }

  /**
   * @param pin - security card PIN code
   * @param callback
   * Make pin verification and put result of verification into callback.
   */
  private final CardApiInterface<List<String>> verifyPin = list -> this.verifyPinAndGetJson(list.get(0));

  public void verifyPin(final String pin, final NfcCallback callback, Boolean... showDialog) {
    boolean showDialogFlag = showDialog.length > 0 ? showDialog[0] : false;
    CardTask cardTask = new CardTask(this, callback,  Collections.singletonList(pin), verifyPin, showDialogFlag);
    cardTask.execute();
  }

  /**
   * @param pin - security card PIN code
   * @return
   * @throws Exception
   * Make pin verification.
   */
  public String verifyPinAndGetJson(final String pin) throws Exception {
    try {
      //long start = System.currentTimeMillis();
      checkPin(pin);
      verifyPin(BYTE_ARR_HELPER.bytes(STR_HELPER.pinToHex(pin)));
      String json = JSON_HELPER.createResponseJson(DONE_MSG);
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
   * Put public key for HD path m/44'/396'/0'/0'/0' into callback
   */
  private final CardApiInterface<List<String>> getPublicKeyForDefaultPath = list -> this.getPublicKeyForDefaultPathAndGetJson();

  public void getPublicKeyForDefaultPath(final NfcCallback callback, Boolean... showDialog) {
    boolean showDialogFlag = showDialog.length > 0 ? showDialog[0] : false;
    CardTask cardTask = new CardTask(this, callback,  Collections.emptyList(), getPublicKeyForDefaultPath, showDialogFlag);
    cardTask.execute();
  }

  /**
   * @return
   * @throws Exception
   * Return public key for HD path m/44'/396'/0'/0'/0'
   */
  public String getPublicKeyForDefaultPathAndGetJson() throws Exception {
    try {
      return getPublicKeyForDefaultPathAndGetJsonNotProcessException();
    }
    catch (Exception e) {
      throw new Exception(EXCEPTION_HELPER.makeFinalErrMsg(e), e);
    }
  }

  /**
   * @param serialNumber
   * @param callback
   * Put public key for HD path m/44'/396'/0'/0'/0' into callback if serialNumber == serialNumberFomCard
   */
  private final CardApiInterface<List<String>> checkSerialNumberAndGetPublicKeyForDefaultPath = list -> this.checkSerialNumberAndGetPublicKeyForDefaultPathAndGetJson(list.get(0));

  public void checkSerialNumberAndGetPublicKeyForDefaultPath(final String serialNumber, final NfcCallback callback, Boolean... showDialog) {
    boolean showDialogFlag = showDialog.length > 0 ? showDialog[0] : false;
    CardTask cardTask = new CardTask(this, callback,  Collections.singletonList(serialNumber), checkSerialNumberAndGetPublicKeyForDefaultPath, showDialogFlag);
    cardTask.execute();
  }

  /**
   * @param serialNumber
   * @return
   * @throws Exception
   * Return public key for HD path m/44'/396'/0'/0'/0' if serialNumber == serialNumberFomCard
   */
  public String checkSerialNumberAndGetPublicKeyForDefaultPathAndGetJson(final String serialNumber) throws Exception {
    try {
      checkCardSerialNumber(serialNumber);
      return getPublicKeyForDefaultPathAndGetJsonNotProcessException();
    }
    catch (Exception e) {
      throw new Exception(EXCEPTION_HELPER.makeFinalErrMsg(e), e);
    }
  }


  /**
   * @param hdIndex
   * @param callback
   * Put public key for HD path m/44'/396'/0'/0'/hdIndex' into callback.
   */
  private final CardApiInterface<List<String>> getPublicKey = list -> this.getPublicKeyAndGetJson(list.get(0));

  public void getPublicKey(final String hdIndex, final NfcCallback callback, Boolean... showDialog) {
    boolean showDialogFlag = showDialog.length > 0 ? showDialog[0] : false;
    CardTask cardTask = new CardTask(this, callback,  Collections.singletonList(hdIndex), getPublicKey, showDialogFlag);
    cardTask.execute();
  }

  /**
   * @param hdIndex
   * @return
   * @throws Exception
   * Return public key for HD path m/44'/396'/0'/0'/hdIndex'.
   */
  public String getPublicKeyAndGetJson(final String hdIndex) throws Exception {
    try {
      return getPublicKeyAndGetJsonNotProcessException(hdIndex);
    }
    catch (Exception e) {
      throw new Exception(EXCEPTION_HELPER.makeFinalErrMsg(e), e);
    }
  }

  /**
   * @param serialNumber
   * @param hdIndex
   * @param callback
   * Put public key for HD path m/44'/396'/0'/0'/hdIndex' into callback.
   */
  private final CardApiInterface<List<String>> checkSerialNumberAndGetPublicKey = list -> this.checkSerialNumberAndGetPublicKeyAndGetJson(list.get(0), list.get(1));

  public void checkSerialNumberAndGetPublicKey(final String serialNumber, final String hdIndex, final NfcCallback callback, Boolean... showDialog) {
    boolean showDialogFlag = showDialog.length > 0 ? showDialog[0] : false;
    CardTask cardTask = new CardTask(this, callback,  Arrays.asList(serialNumber, hdIndex), checkSerialNumberAndGetPublicKey, showDialogFlag);
    cardTask.execute();
  }

  /**
   * @param serialNumber
   * @param hdIndex
   * @return
   * @throws Exception
   * Return public key for HD path m/44'/396'/0'/0'/hdIndex' if serialNumber == serialNumberFomCard
   */
  public String checkSerialNumberAndGetPublicKeyAndGetJson(final String serialNumber, final String hdIndex) throws Exception {
    try {
      checkCardSerialNumber(serialNumber);
      return getPublicKeyAndGetJsonNotProcessException(hdIndex);
    }
    catch (Exception e) {
      throw new Exception(EXCEPTION_HELPER.makeFinalErrMsg(e), e);
    }
  }

  /**
   * @param dataForSigning
   * @param callback
   * Make data signing by key for HD path m/44'/396'/0'/0'/0' and put signature into callback. Prior to call this function you must call verifyPin.
   */
  private final CardApiInterface<List<String>> signForDefaultHdPath = list -> this.signForDefaultHdPathAndGetJson(list.get(0));

  public void signForDefaultHdPath(final String dataForSigning, final NfcCallback callback, Boolean... showDialog) {
    boolean showDialogFlag = showDialog.length > 0 ? showDialog[0] : false;
    CardTask cardTask = new CardTask(this, callback,  Collections.singletonList(dataForSigning), signForDefaultHdPath, showDialogFlag);
    cardTask.execute();
  }

  /**
   * @param dataForSigning
   * @return
   * @throws Exception
   * Make data signing by key for HD path m/44'/396'/0'/0'/0'. Prior to call this function you must call verifyPin.
   */
  public String signForDefaultHdPathAndGetJson(final String dataForSigning) throws Exception {
    try {
      return signForDefaultHdPathAndGetJsonNotProcessException(dataForSigning);
    }
    catch (Exception e) {
      throw new Exception(EXCEPTION_HELPER.makeFinalErrMsg(e), e);
    }
  }

  /**
   * @param serialNumber
   * @param dataForSigning
   * @param callback
   * Make data signing by key for HD path m/44'/396'/0'/0'/0' and put signature into callback if serialNumber == serialNumberFomCard. Prior to call this function you must call verifyPin.
   */
  private final CardApiInterface<List<String>> checkSerialNumberAndSignForDefaultHdPath = list -> this.checkSerialNumberAndSignForDefaultHdPathAndGetJson(list.get(0), list.get(1));

  public void checkSerialNumberAndSignForDefaultHdPath(final String serialNumber, final String dataForSigning, final NfcCallback callback, Boolean... showDialog) {
    boolean showDialogFlag = showDialog.length > 0 ? showDialog[0] : false;
    CardTask cardTask = new CardTask(this, callback,  Arrays.asList(serialNumber, dataForSigning), checkSerialNumberAndSignForDefaultHdPath, showDialogFlag);
    cardTask.execute();
  }

  /**
   * @param serialNumber
   * @param dataForSigning
   * @return
   * @throws Exception
   * Make data signing by key for HD path m/44'/396'/0'/0'/0' if serialNumber == serialNumberFomCard. Prior to call this function you must call verifyPin.
   */
  public String checkSerialNumberAndSignForDefaultHdPathAndGetJson(final String serialNumber, final String dataForSigning) throws Exception {
    try {
      checkCardSerialNumber(serialNumber);
      return signForDefaultHdPathAndGetJsonNotProcessException(dataForSigning);
    }
    catch (Exception e) {
      throw new Exception(EXCEPTION_HELPER.makeFinalErrMsg(e), e);
    }
  }


  /**
   * @param dataForSigning
   * @param hdIndex
   * @param callback
   * Make data signing by key for HD path m/44'/396'/0'/0'/hdIndex' and put into callback. Prior to call this function you must call verifyPin.
   */
  private final CardApiInterface<List<String>> sign = list -> this.signAndGetJson(list.get(0), list.get(1));

  public void sign(final String dataForSigning, final String hdIndex, final NfcCallback callback, Boolean... showDialog) {
    boolean showDialogFlag = showDialog.length > 0 ? showDialog[0] : false;
    CardTask cardTask = new CardTask(this, callback, Arrays.asList(dataForSigning, hdIndex), sign, showDialogFlag);
    cardTask.execute();
  }

  /**
   * @param dataForSigning
   * @param hdIndex
   * @return
   * @throws Exception
   * Make data signing by key for HD path m/44'/396'/0'/0'/hdIndex'. Prior to call this function you must call verifyPin.
   */
  public String signAndGetJson(final String dataForSigning, final String hdIndex) throws Exception {
    try {
      return signAndGetJsonNotProcessException(dataForSigning, hdIndex);
    }
    catch (Exception e) {
      throw new Exception(EXCEPTION_HELPER.makeFinalErrMsg(e), e);
    }
  }

  /**
   * @param serialNumber
   * @param dataForSigning
   * @param hdIndex
   * @param callback
   * Make data signing by key for HD path m/44'/396'/0'/0'/hdIndex' and put into callback if serialNumber == serialNumberFomCard. Prior to call this function you must call verifyPin.
   */
  private final CardApiInterface<List<String>> checkSerialNumberAndSign = list -> this.checkSerialNumberAndSignAndGetJson(list.get(0), list.get(1), list.get(2));

  public void checkSerialNumberAndSign(final String serialNumber, final String dataForSigning, final String hdIndex, final NfcCallback callback, Boolean... showDialog) {
    boolean showDialogFlag = showDialog.length > 0 ? showDialog[0] : false;
    CardTask cardTask = new CardTask(this, callback, Arrays.asList(serialNumber, dataForSigning, hdIndex), checkSerialNumberAndSign, showDialogFlag);
    cardTask.execute();
  }

  /**
   * @param serialNumber
   * @param dataForSigning
   * @param hdIndex
   * @return
   * @throws Exception
   * Make data signing by key for HD path m/44'/396'/0'/0'/hdIndex' if serialNumber == serialNumberFomCard. Prior to call this function you must call verifyPin.
   */
  public String checkSerialNumberAndSignAndGetJson(final String serialNumber, final String dataForSigning, final String hdIndex) throws Exception {
    try {
      checkCardSerialNumber(serialNumber);
      return signAndGetJsonNotProcessException(dataForSigning, hdIndex);
    }
    catch (Exception e) {
      throw new Exception(EXCEPTION_HELPER.makeFinalErrMsg(e), e);
    }
  }

  /**
   * @param dataForSigning
   * @param pin
   * @param callback
   * Make pin verification and data signing by key for HD path m/44'/396'/0'/0'/0'. Put signature to callback. Prior to call this function you must call verifyPin.
   */

  private final CardApiInterface<List<String>> verifyPinAndSignForDefaultHdPath = list -> this.verifyPinAndSignForDefaultHdPathAndGetJson(list.get(0), list.get(1));

  public void verifyPinAndSignForDefaultHdPath(final String dataForSigning, final String pin, final NfcCallback callback, Boolean... showDialog) {
    boolean showDialogFlag = showDialog.length > 0 ? showDialog[0] : false;
    CardTask cardTask = new CardTask(this, callback, Arrays.asList(dataForSigning, pin), verifyPinAndSignForDefaultHdPath, showDialogFlag);
    cardTask.execute();
  }

  /**
   * @param dataForSigning
   * @param pin
   * @return
   * @throws Exception
   * Make pin verification and data signing by key for HD path m/44'/396'/0'/0'/0'. Prior to call this function you must call verifyPin.
   */
  public String verifyPinAndSignForDefaultHdPathAndGetJson(final String dataForSigning, final String pin) throws Exception {
    try {
      return verifyPinAndSignForDefaultHdPathAndGetJsonNotProcessException(dataForSigning, pin);
    }
    catch (Exception e) {
      throw new Exception(EXCEPTION_HELPER.makeFinalErrMsg(e), e);
    }
  }

  /**
   * @param serialNumber
   * @param dataForSigning
   * @param pin
   * @param callback
   * Make pin verification and data signing by key for HD path m/44'/396'/0'/0'/0' if serialNumber == serialNumberFomCard. Prior to call this function you must call verifyPin.
   */

  private final CardApiInterface<List<String>> checkSerialNumberAndVerifyPinAndSignForDefaultHdPath = list -> this.checkSerialNumberAndVerifyPinAndSignForDefaultHdPathAndGetJson(list.get(0), list.get(1), list.get(2));

  public void checkSerialNumberAndVerifyPinAndSignForDefaultHdPath(final String serialNumber, final String dataForSigning, final String pin, final NfcCallback callback, Boolean... showDialog) {
    boolean showDialogFlag = showDialog.length > 0 ? showDialog[0] : false;
    CardTask cardTask = new CardTask(this, callback, Arrays.asList(serialNumber, dataForSigning, pin), checkSerialNumberAndVerifyPinAndSignForDefaultHdPath, showDialogFlag);
    cardTask.execute();
  }

  /**
   * @param serialNumber
   * @param dataForSigning
   * @param pin
   * @return
   * @throws Exception
   * Make pin verification and data signing by key for HD path m/44'/396'/0'/0'/0'  if serialNumber == serialNumberFomCard. Prior to call this function you must call verifyPin.
   */
  public String checkSerialNumberAndVerifyPinAndSignForDefaultHdPathAndGetJson(final String serialNumber, final String dataForSigning, final String pin) throws Exception {
    try {
      checkCardSerialNumber(serialNumber);
      return verifyPinAndSignForDefaultHdPathAndGetJsonNotProcessException(dataForSigning, pin);
    }
    catch (Exception e) {
      throw new Exception(EXCEPTION_HELPER.makeFinalErrMsg(e), e);
    }
  }

  /**
   * @param dataForSigning
   * @param hdIndex
   * @param pin
   * @param callback
   * Make pin verification and data signing by key for HD path m/44'/396'/0'/0'/hdIndex'. Put signature into callback.
   */
  private final CardApiInterface<List<String>> verifyPinAndSign = list -> this.verifyPinAndSignAndGetJson(list.get(0), list.get(1), list.get(2));

  public void verifyPinAndSign(final String dataForSigning, final String hdIndex, final String pin, final NfcCallback callback, Boolean... showDialog) {
    boolean showDialogFlag = showDialog.length > 0 ? showDialog[0] : false;
    CardTask cardTask = new CardTask(this, callback, Arrays.asList(dataForSigning, hdIndex, pin), verifyPinAndSign, showDialogFlag);
    cardTask.execute();
  }

  /**
   * @param dataForSigning
   * @param hdIndex
   * @param pin
   * @return
   * @throws Exception
   * Make pin verification and data signing by key for HD path m/44'/396'/0'/0'/hdIndex'.
   */
  public String verifyPinAndSignAndGetJson(final String dataForSigning, final String hdIndex, final String pin) throws Exception {
    try {
      return verifyPinAndSignAndGetJsonNotProcessException(dataForSigning, hdIndex, pin);
    }
    catch (Exception e) {
      throw new Exception(EXCEPTION_HELPER.makeFinalErrMsg(e), e);
    }
  }

  /**
   * @param serialNumber
   * @param dataForSigning
   * @param hdIndex
   * @param pin
   * @param callback
   * Make pin verification and data signing by key for HD path m/44'/396'/0'/0'/hdIndex' if serialNumber == serialNumberFomCard. Put signature into callback.
   */
  private final CardApiInterface<List<String>> checkSerialNumberAndVerifyPinAndSign = list -> this.checkSerialNumberAndVerifyPinAndSignAndGetJson(list.get(0), list.get(1), list.get(2), list.get(3));

  public void checkSerialNumberAndVerifyPinAndSign(final String serialNumber, final String dataForSigning, final String hdIndex, final String pin, final NfcCallback callback, Boolean... showDialog) {
    boolean showDialogFlag = showDialog.length > 0 ? showDialog[0] : false;
    CardTask cardTask = new CardTask(this, callback, Arrays.asList(serialNumber, dataForSigning, hdIndex, pin), checkSerialNumberAndVerifyPinAndSign, showDialogFlag);
    cardTask.execute();
  }

  /**
   * @param serialNumber
   * @param dataForSigning
   * @param hdIndex
   * @param pin
   * @return
   * @throws Exception
   * Make pin verification and data signing by key for HD path m/44'/396'/0'/0'/hdIndex' if serialNumber == serialNumberFomCard.
   */
  public String checkSerialNumberAndVerifyPinAndSignAndGetJson(final String serialNumber, final String dataForSigning, final String hdIndex, final String pin) throws Exception {
    try {
      checkCardSerialNumber(serialNumber);
      return verifyPinAndSignAndGetJsonNotProcessException(dataForSigning, hdIndex, pin);
    }
    catch (Exception e) {
      throw new Exception(EXCEPTION_HELPER.makeFinalErrMsg(e), e);
    }
  }

  private String getPublicKeyForDefaultPathAndGetJsonNotProcessException() throws Exception {
    //long start = System.currentTimeMillis();
    String response = BYTE_ARR_HELPER.hex(getPublicKeyForDefaultPath().getData());
    String json = JSON_HELPER.createResponseJson(response);
    //long end = System.currentTimeMillis();
    //Log.d("TAG", "!!Time = " + String.valueOf(end - start) );
    return json;
  }

  private String getPublicKeyAndGetJsonNotProcessException(final String hdIndex) throws Exception {
    //long start = System.currentTimeMillis();
    checkHdIndex(hdIndex);
    String response = BYTE_ARR_HELPER.hex(getPublicKey(BYTE_ARR_HELPER.bytes(STR_HELPER.asciiToHex(hdIndex))).getData());
    String json = JSON_HELPER.createResponseJson(response);;
    //long end = System.currentTimeMillis();
    //Log.d("TAG", "!!Time = " + String.valueOf(end - start) );
    return json;
  }

  private String signForDefaultHdPathAndGetJsonNotProcessException(final String dataForSigning) throws Exception {
    //long start = System.currentTimeMillis();
    checkDataForSigning(dataForSigning);
    String response = BYTE_ARR_HELPER.hex(signForDefaultPath(BYTE_ARR_HELPER.bytes(dataForSigning)).getData());
    String json = JSON_HELPER.createResponseJson(response);
    //long end = System.currentTimeMillis();
    //Log.d("TAG", "!!Time = " + String.valueOf(end - start) );
    return json;
  }

  private String signAndGetJsonNotProcessException(final String dataForSigning, final String hdIndex) throws Exception {
    checkHdIndex(hdIndex);
    checkDataForSigningWithPath(dataForSigning);
    String response = BYTE_ARR_HELPER.hex(sign(BYTE_ARR_HELPER.bytes(dataForSigning), BYTE_ARR_HELPER.bytes(STR_HELPER.asciiToHex(hdIndex))).getData());
    String json = JSON_HELPER.createResponseJson(response);
    //long end = System.currentTimeMillis();
    //Log.d("TAG", "!!Time = " + String.valueOf(end - start) );
    return json;
  }

  private String verifyPinAndSignForDefaultHdPathAndGetJsonNotProcessException(final String dataForSigning, final String pin) throws Exception {
    //long start = System.currentTimeMillis();
    checkPin(pin);
    checkDataForSigning(dataForSigning);
    String response = BYTE_ARR_HELPER.hex(verifyPinAndSignForDefaultHdPath(BYTE_ARR_HELPER.bytes(dataForSigning), BYTE_ARR_HELPER.bytes(STR_HELPER.pinToHex(pin))).getData());
    String json = JSON_HELPER.createResponseJson(response);
    //long end = System.currentTimeMillis();
    //Log.d("TAG", "!!Time = " + String.valueOf(end - start) );
    return json;
  }

  public String verifyPinAndSignAndGetJsonNotProcessException(final String dataForSigning, final String hdIndex, final String pin) throws Exception {
    //long start = System.currentTimeMillis();
    checkPin(pin);
    checkHdIndex(hdIndex);
    checkDataForSigningWithPath(dataForSigning);
    String response = BYTE_ARR_HELPER.hex(verifyPinAndSign(BYTE_ARR_HELPER.bytes(dataForSigning), BYTE_ARR_HELPER.bytes(STR_HELPER.asciiToHex(hdIndex)), BYTE_ARR_HELPER.bytes(STR_HELPER.pinToHex(pin))).getData());
    String json = JSON_HELPER.createResponseJson(response);
    //long end = System.currentTimeMillis();
    //Log.d("TAG", "!!Time = " + String.valueOf(end - start) );
    return json;
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

  private void checkCardSerialNumber(final String serialNumber) throws Exception {
    if (!STR_HELPER.isNumericString(serialNumber))
      throw new Exception(ERROR_MSG_SERIAL_NUMBER_NOT_NUMERIC);
    if (serialNumber.length() != SERIAL_NUMBER_SIZE)
      throw new Exception(ERROR_MSG_SERIAL_NUMBER_LEN_INCORRECT);
    String serialNumberFromCard = STR_HELPER.makeDigitalString(getSerialNumber());
    if (!serialNumber.equals(serialNumberFromCard))
      throw new Exception(ERROR_MSG_CARD_HAVE_INCORRECT_SN);
  }

  private void checkPin(final String pin) throws Exception {
    if (!STR_HELPER.isNumericString(pin))
      throw new Exception(ERROR_MSG_PIN_FORMAT_INCORRECT);
    if (pin.length() != PIN_SIZE)
      throw new Exception(ERROR_MSG_PIN_LEN_INCORRECT);
  }

  private void checkHdIndex(final String hdIndex) throws Exception {
    if (!STR_HELPER.isNumericString(hdIndex))
      throw new Exception(ERROR_MSG_HD_INDEX_FORMAT_INCORRECT);
    if (hdIndex.length() == 0 || hdIndex.length() > MAX_HD_INDEX_SIZE)
      throw new Exception(ERROR_MSG_HD_INDEX_LEN_INCORRECT);
  }

  private void checkDataForSigning(final String dataForSigning) throws Exception {
    if (!STR_HELPER.isHexString(dataForSigning))
      throw new Exception(ERROR_MSG_DATA_FOR_SIGNING_NOT_HEX);
    if (dataForSigning.length() > (2 * DATA_FOR_SIGNING_MAX_SIZE))
      throw new Exception(ERROR_MSG_DATA_FOR_SIGNING_LEN_INCORRECT);
  }

  private void checkDataForSigningWithPath(final String dataForSigning) throws Exception {
    if (!STR_HELPER.isHexString(dataForSigning))
      throw new Exception(ERROR_MSG_DATA_FOR_SIGNING_NOT_HEX);
    if (dataForSigning.length() > (2 * DATA_FOR_SIGNING_MAX_SIZE_FOR_CASE_WITH_PATH))
      throw new Exception(ERROR_MSG_DATA_FOR_SIGNING_WITH_PATH_LEN_INCORRECT);
  }

}
