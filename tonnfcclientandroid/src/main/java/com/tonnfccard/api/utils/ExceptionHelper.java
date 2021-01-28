package com.tonnfccard.api.utils;

import android.util.Log;

import com.tonnfccard.api.callback.NfcCallback;

import static com.tonnfccard.api.utils.ResponsesConstants.ERROR_MSG_EXCEPTION_OBJECT_IS_NULL;
import static com.tonnfccard.api.utils.ResponsesConstants.ERROR_MSG_NFC_CALLBACK_IS_NULL;

public class ExceptionHelper {
  private static ExceptionHelper instance;

  public static ExceptionHelper getInstance(){
    if (instance == null) {
      instance = new ExceptionHelper();
    }
    return instance;
  }

  private ExceptionHelper(){}

  public static final JsonHelper JSON_HELPER = JsonHelper.getInstance();

  public void handleException(Exception e, NfcCallback callback, String tag) {
    String finalErrMsg;
    if (callback != null){
      String errMsg = ( e == null  || e.getMessage() == null) ? ERROR_MSG_EXCEPTION_OBJECT_IS_NULL : e.getMessage();
      finalErrMsg = errMsg.startsWith("{") ? errMsg : JSON_HELPER.createErrorJson(errMsg);  // check if errMsg is in JSON format already
      callback.getReject().reject(finalErrMsg);
    }
    else {
      finalErrMsg = ERROR_MSG_NFC_CALLBACK_IS_NULL;
    }
    Log.e(tag, "Error happened : " + finalErrMsg);
  }
}
