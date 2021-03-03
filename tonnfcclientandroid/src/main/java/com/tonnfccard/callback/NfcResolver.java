package com.tonnfccard.callback;

@FunctionalInterface
public interface NfcResolver {
  void resolve(Object value);
}
