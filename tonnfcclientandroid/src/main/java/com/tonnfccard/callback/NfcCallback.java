package com.tonnfccard.callback;

/**
 *  Callback entity to grab the resulting data from the card/error message
 *  The main purpose of creation was to make more uniform and convenient way of working with React native (RN) library for cards
 *  In RN lib we override resolve and reject using standard Promises of RN bridge
 */
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
