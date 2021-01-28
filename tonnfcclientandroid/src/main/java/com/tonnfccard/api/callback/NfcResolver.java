package com.tonnfccard.api.callback;

@FunctionalInterface
public interface NfcResolver {
  void resolve(Object value);
}
