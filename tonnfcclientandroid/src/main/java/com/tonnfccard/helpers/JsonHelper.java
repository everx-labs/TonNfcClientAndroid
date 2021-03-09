package com.tonnfccard.helpers;

import androidx.annotation.RestrictTo;

import com.tonnfccard.smartcard.CAPDU;
import com.tonnfccard.smartcard.ErrorCodes;
import com.tonnfccard.smartcard.ApduHelper;

import org.json.JSONException;
import org.json.JSONObject;

import static com.tonnfccard.helpers.ResponsesConstants.*;
import static com.tonnfccard.TonWalletConstants.*;

@RestrictTo(RestrictTo.Scope.LIBRARY)
public class JsonHelper {
  private static final String TAG = "JsonHelper";

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

  public String createResponseJson(String msg) throws JSONException {
    if (msg == null) throw new IllegalArgumentException(ERROR_MSG_MALFORMED_JSON_MSG);
    JSONObject jObjectData = new JSONObject();
    jObjectData.put(MESSAGE_FIELD, msg);
    jObjectData.put(STATUS_FIELD, SUCCESS_STATUS);
    return jObjectData.toString();
  }

  public String createErrorJsonForCardException(String sw, CAPDU capdu) throws JSONException  {
    if (!STRING_HELPER.isHexString(sw) || sw.length() != 4) throw new IllegalArgumentException(ERROR_MSG_MALFORMED_SW_FOR_JSON);
    if (capdu == null) throw new IllegalArgumentException(ERROR_MSG_CAPDU_IS_NULL);
    JSONObject jObjectData = new JSONObject();
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
    return  jObjectData.toString();
  }

  public String createErrorJson(String msg) throws JSONException {
    if (msg == null) throw new IllegalArgumentException(ERROR_MSG_MALFORMED_JSON_MSG);
    JSONObject jObjectData = new JSONObject();
    jObjectData.put(MESSAGE_FIELD, msg);
    jObjectData.put(STATUS_FIELD, FAIL_STATUS);
    String errCode = getErrorCode(msg);
    String errTypeId = errCode == null ? ANDROID_INTERNAL_ERROR_TYPE_ID : errCode.substring(0, 1);
    jObjectData.put(ERROR_TYPE_ID_FIELD , errTypeId);
    String errTypeMsg = getErrorTypeMsg(errTypeId);
    jObjectData.put(ERROR_TYPE_FIELD, errTypeMsg);
    if (errCode != null)
      jObjectData.put(ERROR_CODE_FIELD, errCode);
    return jObjectData.toString();
  }



}
