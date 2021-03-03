package com.tonnfccard.helpers;

import android.util.Log;

import androidx.annotation.RestrictTo;

import com.tonnfccard.callback.NfcCallback;

import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_EXCEPTION_OBJECT_IS_NULL;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_NFC_CALLBACK_IS_NULL;

@RestrictTo(RestrictTo.Scope.LIBRARY)
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
    String finalErrMsg = ( e == null  || e.getMessage() == null) ? ERROR_MSG_EXCEPTION_OBJECT_IS_NULL : e.getMessage();

    if (callback != null){
     // finalErrMsg = makeErrMsg(e);
      callback.getReject().reject(finalErrMsg);
    }
    else {
      finalErrMsg = ERROR_MSG_NFC_CALLBACK_IS_NULL;
    }
    Log.e(tag, "Error happened : " + finalErrMsg);
  }

  public  String makeErrMsg(Exception e) {
    String errMsg = ( e == null  || e.getMessage() == null) ? ERROR_MSG_EXCEPTION_OBJECT_IS_NULL : e.getMessage();
    return errMsg.startsWith("{") ? errMsg : JSON_HELPER.createErrorJson(errMsg);  // check if errMsg is in JSON format already
  }
}
