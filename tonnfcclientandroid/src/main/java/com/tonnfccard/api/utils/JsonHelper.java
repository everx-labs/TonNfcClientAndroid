package com.tonnfccard.api.utils;

import com.tonnfccard.smartcard.ErrorCodes;
import com.tonnfccard.smartcard.apdu.ApduHelper;
import com.tonnfccard.smartcard.wrappers.CAPDU;

import org.json.JSONException;
import org.json.JSONObject;

import static com.tonnfccard.api.utils.ResponsesConstants.ANDROID_INTERNAL_ERROR_TYPE_ID;
import static com.tonnfccard.api.utils.ResponsesConstants.CARD_ERROR_TYPE_ID;
import static com.tonnfccard.api.utils.ResponsesConstants.FAIL_STATUS;
import static com.tonnfccard.api.utils.ResponsesConstants.SUCCESS_STATUS;
import static com.tonnfccard.api.utils.ResponsesConstants.getErrorCode;
import static com.tonnfccard.api.utils.ResponsesConstants.getErrorTypeMsg;

public class JsonHelper {
  private static final String TAG = "JsonHelper";
  private static final String MALFORMED_JSON_MSG = "Malformed data for json.";
  public static final String STATUS_FIELD = "status";
  public static final String ERROR_CODE_FIELD = "errorCode";
  public static final String ERROR_TYPE_FIELD = "errorType";
  public static final String ERROR_TYPE_ID_FIELD = "errorTypeId";
  public static final String MESSAGE_FIELD = "message";
  public static final String CARD_INSTRUCTION_FIELD = "cardInstruction";
  public static final String APDU_FIELD = "apdu";

  private static final StringHelper STRING_HELPER = StringHelper.getInstance();
  private static final ApduHelper APDU_HELPER = ApduHelper.getInstance();

  private static JsonHelper instance;

  public static JsonHelper getInstance(){
    if (instance == null) {
      instance = new JsonHelper();
    }
    return instance;
  }

  private JsonHelper(){}

  public String createResponseJson(String msg)  {
    if (msg == null) return MALFORMED_JSON_MSG;
    JSONObject jObjectData = new JSONObject();
    try {
      jObjectData.put(MESSAGE_FIELD, msg);
      jObjectData.put(STATUS_FIELD, SUCCESS_STATUS);
    }
    catch (JSONException e) {
      return e.getMessage();
    }
    return  jObjectData.toString();
  }

  public String createErrorJsonForCardException(String sw, CAPDU capdu)  {
    if (!STRING_HELPER.isHexString(sw) || sw.length() != 4) {
      return MALFORMED_JSON_MSG;
    }
    JSONObject jObjectData = new JSONObject();
    try {
      String msg = ErrorCodes.getMsg(sw);
      if (msg != null) jObjectData.put(MESSAGE_FIELD, ErrorCodes.getMsg(sw));
      jObjectData.put(STATUS_FIELD, FAIL_STATUS);
      jObjectData.put(ERROR_TYPE_ID_FIELD, CARD_ERROR_TYPE_ID);
      jObjectData.put(ERROR_TYPE_FIELD , getErrorTypeMsg(CARD_ERROR_TYPE_ID));
      jObjectData.put(ERROR_CODE_FIELD, sw);

      String apduName = APDU_HELPER.getApduCommandName(capdu);
      if (apduName != null)
        jObjectData.put(CARD_INSTRUCTION_FIELD, apduName);

      jObjectData.put(APDU_FIELD, capdu.getFormattedApdu());
    }
    catch (Exception e) {
      return e.getMessage();
    }
    return  jObjectData.toString();
  }

  public String createErrorJson(String msg)  {
    if (msg == null) return MALFORMED_JSON_MSG;
    JSONObject jObjectData = new JSONObject();
    try {
      jObjectData.put(MESSAGE_FIELD, msg);
      jObjectData.put(STATUS_FIELD, FAIL_STATUS);

      String errCode = getErrorCode(msg);
      String errTypeId = errCode == null ? ANDROID_INTERNAL_ERROR_TYPE_ID : errCode.substring(0, 1);

      jObjectData.put(ERROR_TYPE_ID_FIELD , errTypeId);

      String errTypeMsg = getErrorTypeMsg(errTypeId);
      jObjectData.put(ERROR_TYPE_FIELD, errTypeMsg);

      if (errCode != null)
        jObjectData.put(ERROR_CODE_FIELD, errCode);
    }
    catch (Exception e) {
      return e.getMessage();
    }
    return jObjectData.toString();
  }



}
