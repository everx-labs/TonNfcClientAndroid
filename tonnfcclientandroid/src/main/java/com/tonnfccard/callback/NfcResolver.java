package com.tonnfccard.callback;

/**
 *  Override it to grab the result of card operation
 */
@FunctionalInterface
public interface NfcResolver {
  void resolve(Object value);
}
