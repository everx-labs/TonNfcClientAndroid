package com.tonnfccard.nfc;

import com.tonnfccard.smartcard.RAPDU;

@FunctionalInterface
public interface NfcApduRunnerFunction<T> {
    RAPDU accept(T t) throws Exception;
}
