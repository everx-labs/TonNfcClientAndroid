package com.tonnfccard.smartcard;

import com.tonnfccard.utils.ByteArrayHelper;

import java.util.Arrays;

import static com.tonnfccard.api.utils.ResponsesConstants.ERROR_MSG_STATE_RESPONSE_INCORRECT;

public enum TonWalletAppletStates {

  INSTALLED ((byte) 0x07, "TonWalletApplet is invalid (is not personalized)"),
  PERSONALIZED ((byte) 0x17, "TonWalletApplet is personalized."),
  WAITE_AUTHORIZATION_MODE ((byte) 0x27, "TonWalletApplet waits two-factor authorization."),
  DELETE_KEY_FROM_KEYCHAIN_MODE ((byte) 0x37, "TonWalletApplet is personalized and waits finishing key deleting from keychain."),
  BLOCKED_MODE ((byte) 0x47, "TonWalletApplet is blocked.");

  private Byte value;
  private String description;

  private static final ByteArrayHelper BYTE_ARRAY_HELPER = ByteArrayHelper.getInstance();

  TonWalletAppletStates(Byte value, String descr) {
    this.value = value;
    this.description = descr;
  }

  public String getDescription() {
    return description;
  }

  public Byte getValue() {
    return value;
  }

  @Override
  public String toString() {
    return "State{" +
      "value='" + value + '\'' +
      "description='" + description + '\'' +
      '}';
  }

  public static TonWalletAppletStates findByStateValue(Byte value) throws Exception{
    return Arrays.stream(TonWalletAppletStates.values()).filter(v ->
      v.getValue().equals(value)).findFirst().orElseThrow(() ->
      new Exception(ERROR_MSG_STATE_RESPONSE_INCORRECT + BYTE_ARRAY_HELPER.hex(value)));
  }
}
