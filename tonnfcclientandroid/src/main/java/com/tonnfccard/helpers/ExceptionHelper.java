package com.tonnfccard.helpers;

import android.util.Log;

import androidx.annotation.RestrictTo;

import com.tonnfccard.callback.NfcCallback;

import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_ERR_MSG_IS_NULL;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_EXCEPTION_OBJECT_IS_NULL;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_NFC_CALLBACK_IS_NULL;

/**
 * We wrap all error messages into json of special formats.
 * This class is responsible for wrapping of unwrapped error messages
 * and also responsible for putting error messages into callbacks.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class ExceptionHelper {
    private static ExceptionHelper instance;

    public static ExceptionHelper getInstance() {
        if (instance == null) {
            instance = new ExceptionHelper();
        }
        return instance;
    }

    private ExceptionHelper() {
    }

    public static final JsonHelper JSON_HELPER = JsonHelper.getInstance();

    //Putting any error message produced by the library into callback
    //If callback is null then just print error message into Logcat
    //Enforce wrapping of error message into json
    public void handleException(Exception e, NfcCallback callback, String tag) {
        String finalErrMsg;
        if (callback == null) {
            finalErrMsg = makeFinalErrMsg(ERROR_MSG_NFC_CALLBACK_IS_NULL);
        } else {
            finalErrMsg = makeFinalErrMsg(e);
            callback.getReject().reject(finalErrMsg);
        }
        Log.e(tag, "Error happened : " + finalErrMsg);
    }

    // At this point we get exception with error messages that can be wrapped/unwrapped into json.
    // They are wrapped already if it's a error thrown by card applet (so ApduRunner makes wrapping).
    // Other error messages (not related to applet directly) are wrapped into json here.
    public String makeFinalErrMsg(Exception e) {
        String errMsg = e == null ? ERROR_MSG_EXCEPTION_OBJECT_IS_NULL : e.getMessage();
        return makeFinalErrMsg(errMsg);
    }

    // At this point we get error messages that can be wrapped/unwrapped into json.
    // They are wrapped already if it's a error thrown by card applet (so ApduRunner makes wrapping).
    // Other error messages (not related to applet directly) are wrapped into json here.
    public String makeFinalErrMsg(String errMsg) {
        try {
            if (errMsg == null) errMsg = ERROR_MSG_ERR_MSG_IS_NULL;
            return errMsg.startsWith("{") ? errMsg : JSON_HELPER.createErrorJson(errMsg);  // check if errMsg is in JSON format already
        }
        catch (Exception e) {
            return e.getMessage();
        }
    }
}
