package com.tonnfccard.api.utils;

import java.util.Random;
import java.util.regex.Pattern;

import static com.tonnfccard.api.utils.ResponsesConstants.ERROR_MSG_ARRAY_ELEMENTS_ARE_NOT_DIGITS;
import static com.tonnfccard.api.utils.ResponsesConstants.ERROR_MSG_ARRAY_TO_MAKE_DIGITAL_STR_MUST_NOT_BE_EMPTY;
import static com.tonnfccard.api.utils.ResponsesConstants.ERROR_MSG_PIN_FORMAT_INCORRECT;
import static com.tonnfccard.api.utils.ResponsesConstants.ERROR_MSG_PIN_LEN_INCORRECT;
import static com.tonnfccard.api.utils.ResponsesConstants.ERROR_MSG_STRING_IS_NULL;
import static com.tonnfccard.smartcard.TonWalletAppletConstants.PIN_SIZE;

public class StringHelper {
  private final Pattern PATTERN_HEX = Pattern.compile("[0-9a-fA-F]+");
  private final Pattern PATTERN_NUMERIC = Pattern.compile("[0-9]+");
  private final char[] HEX_DIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

  private Random random = new Random();

  private static StringHelper instance;

  public static StringHelper getInstance(){
    if (instance == null) {
      instance = new StringHelper();
    }
    return instance;
  }

  private StringHelper(){}

  public String randomHexString(int size) {
    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < size; i++) {
      sb.append(HEX_DIGITS[random.nextInt(HEX_DIGITS.length)]);
    }
    return sb.toString();
  }

  public boolean isHexString(String str) {
    if (str == null || str.length() == 0 || str.length() % 2 != 0) return false;
    return PATTERN_HEX.matcher(str).matches();
  }

  public boolean isNumericString(String str) {
    if (str == null) return false;
    return PATTERN_NUMERIC.matcher(str).matches();
  }

  public String makeDigitalString(byte[] byteArray) throws Exception {
    if (byteArray == null || byteArray.length == 0) throw new Exception(ERROR_MSG_ARRAY_TO_MAKE_DIGITAL_STR_MUST_NOT_BE_EMPTY);
    StringBuilder stringBuilder = new StringBuilder();
    for (int i = 0; i < byteArray.length; i++) {
      if (byteArray[i] < 0 || byteArray[i] > 9) throw new Exception(ERROR_MSG_ARRAY_ELEMENTS_ARE_NOT_DIGITS);
      stringBuilder.append(Integer.valueOf(byteArray[i]));
    }
    return stringBuilder.toString();
  }

  public String asciiToHex(String asciiStr) throws Exception {
    if (asciiStr == null) throw new Exception(ERROR_MSG_STRING_IS_NULL);
    char[] chars = asciiStr.toCharArray();
    StringBuilder hex = new StringBuilder();
    for (char ch : chars) {
      hex.append(Integer.toHexString((int) ch));
    }
    return hex.toString();
  }

  public  String pinToHex(String pin) throws Exception {
    if (!isNumericString(pin))
      throw new Exception(ERROR_MSG_PIN_FORMAT_INCORRECT);
    if (pin.length() != PIN_SIZE)
      throw new Exception(ERROR_MSG_PIN_LEN_INCORRECT);
    char[] chars = pin.toCharArray();
    StringBuilder hex = new StringBuilder();
    for (char ch : chars) {
      hex.append("3").append(ch);
    }
    return hex.toString();
  }


}
