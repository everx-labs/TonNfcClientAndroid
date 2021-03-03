package com.tonnfccard.callback;

@FunctionalInterface
public interface NfcRejecter {
  void reject(String errorMsg);
}
