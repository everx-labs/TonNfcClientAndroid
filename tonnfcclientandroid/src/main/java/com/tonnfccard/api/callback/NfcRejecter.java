package com.tonnfccard.api.callback;

@FunctionalInterface
public interface NfcRejecter {
  void reject(String errorMsg);
}
