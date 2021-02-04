package com.tonnfccard.smartcard;

import com.tonnfccard.utils.ByteArrayHelper;

import java.util.Arrays;

import static com.tonnfccard.api.utils.ResponsesConstants.ERROR_MSG_STATE_RESPONSE_INCORRECT;
import static com.tonnfccard.smartcard.TonWalletAppletConstants.BLOCKED_MSG;
import static com.tonnfccard.smartcard.TonWalletAppletConstants.DELETE_KEY_FROM_KEYCHAIN_MSG;
import static com.tonnfccard.smartcard.TonWalletAppletConstants.INSTALLED_STATE_MSG;
import static com.tonnfccard.smartcard.TonWalletAppletConstants.PERSONALIZED_STATE_MSG;
import static com.tonnfccard.smartcard.TonWalletAppletConstants.WAITE_AUTHORIZATION_MSG;

public enum TonWalletAppletStates {

  INSTALLED ((byte) 0x07, INSTALLED_STATE_MSG),
  PERSONALIZED ((byte) 0x17, PERSONALIZED_STATE_MSG),
  WAITE_AUTHORIZATION_MODE ((byte) 0x27, WAITE_AUTHORIZATION_MSG),
  DELETE_KEY_FROM_KEYCHAIN_MODE ((byte) 0x37, DELETE_KEY_FROM_KEYCHAIN_MSG),
  BLOCKED_MODE ((byte) 0x47, BLOCKED_MSG);

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
