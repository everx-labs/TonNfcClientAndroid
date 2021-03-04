package com.tonnfccard.callback;

/**
 *  Override it to grab error messages happened during card operation
 */
@FunctionalInterface
public interface NfcRejecter {
  void reject(String errorMsg);
}
