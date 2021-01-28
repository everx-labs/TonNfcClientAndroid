package com.tonnfccard.api.utils;

import static com.tonnfccard.api.utils.ResponsesConstants.errorMsgToErrorCodeMap;

public class ErrorListGenerator {
  public static void main(String[] args) {
    JsonHelper jsonHelper = JsonHelper.getInstance();
    for(String errMsg : errorMsgToErrorCodeMap.keySet()) {
      String json = jsonHelper.createErrorJson(errMsg);
      System.out.println(json);
    }
  }
}
