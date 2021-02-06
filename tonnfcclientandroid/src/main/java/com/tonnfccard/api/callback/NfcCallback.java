package com.tonnfccard.api.callback;

import android.util.Log;

public class NfcCallback {
  private NfcResolver resolve;
  private NfcRejecter reject;

  public NfcCallback(NfcResolver resolve, NfcRejecter reject) {
    set(resolve, reject);
  }

  public void set(NfcResolver resolve, NfcRejecter reject) {
    this.resolve = resolve;
    this.reject = reject;
  }

  public NfcResolver getResolve() {
    return resolve;
  }

  public NfcRejecter getReject() {
    return reject;
  }

}
