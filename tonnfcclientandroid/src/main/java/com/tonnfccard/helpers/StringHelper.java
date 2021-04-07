package com.tonnfccard.helpers;

import androidx.annotation.RestrictTo;

import java.util.Random;
import java.util.regex.Pattern;

import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_ARRAY_ELEMENTS_ARE_NOT_DIGITS;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_ARRAY_TO_MAKE_DIGITAL_STR_MUST_NOT_BE_EMPTY;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_PIN_FORMAT_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_PIN_LEN_INCORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_STRING_IS_NOT_ASCII;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_STRING_IS_NULL;
import static com.tonnfccard.TonWalletConstants.PIN_SIZE;

/**
 * Some auxiliary functions to work with strings
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class StringHelper {
  private final Pattern PATTERN_HEX = Pattern.compile("[0-9a-fA-F]+");
  private final Pattern PATTERN_NUMERIC = Pattern.compile("[0-9]+");
  private final char[] HEX_DIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
  private final char[] DECIMAL_DIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};

  private Random random = new Random();

  private static StringHelper instance;

  public static StringHelper getInstance(){
    if (instance == null) {
      instance = new StringHelper();
    }
    return instance;
  }

  private StringHelper(){}

  // Generate random hex string
  public String randomHexString(int size) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < size; i++) {
      sb.append(HEX_DIGITS[random.nextInt(HEX_DIGITS.length)]);
    }
    return sb.toString();
  }

  public String randomDigitalString(int size) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < size; i++) {
      sb.append(DECIMAL_DIGITS[random.nextInt(DECIMAL_DIGITS.length)]);
    }
    return sb.toString();
  }

  // Check if string is in hex format
  public boolean isHexString(String str) {
    if (str == null || str.length() == 0 || str.length() % 2 != 0) return false;
    return PATTERN_HEX.matcher(str).matches();
  }

  // Check if string contains only digits in range [0,9]
  public boolean isNumericString(String str) {
    if (str == null) return false;
    return PATTERN_NUMERIC.matcher(str).matches();
  }

  // Byte array containing elements in range [0,9] is converted into digital string
  public String makeDigitalString(byte[] byteArray) {
    if (byteArray == null || byteArray.length == 0) throw new IllegalArgumentException(ERROR_MSG_ARRAY_TO_MAKE_DIGITAL_STR_MUST_NOT_BE_EMPTY);
    StringBuilder stringBuilder = new StringBuilder();
    for (int i = 0; i < byteArray.length; i++) {
      if (byteArray[i] < 0 || byteArray[i] > 9) throw new IllegalArgumentException(ERROR_MSG_ARRAY_ELEMENTS_ARE_NOT_DIGITS);
      stringBuilder.append(Integer.valueOf(byteArray[i]));
    }
    return stringBuilder.toString();
  }

  public String asciiToHex(String asciiStr) {
    if (asciiStr == null) throw new IllegalArgumentException(ERROR_MSG_STRING_IS_NULL);
    if (!isASCII(asciiStr)) throw new IllegalArgumentException(ERROR_MSG_STRING_IS_NOT_ASCII);
    char[] chars = asciiStr.toCharArray();
    StringBuilder hex = new StringBuilder();
    for (char ch : chars) {
      //hex.append(Integer.toHexString((int) ch));
      hex.append(String.format("%02X", (int) ch));
    }
    return hex.toString();
  }

  public String pinToHex(String pin) {
    if (!isNumericString(pin))
      throw new IllegalArgumentException(ERROR_MSG_PIN_FORMAT_INCORRECT);
    if (pin.length() != PIN_SIZE)
      throw new IllegalArgumentException(ERROR_MSG_PIN_LEN_INCORRECT);
    return asciiToHex(pin);
   /* char[] chars = pin.toCharArray();
    StringBuilder hex = new StringBuilder();
    for (char ch : chars) {
      hex.append("3").append(ch);
    }
    return hex.toString();*/
  }


  private static boolean isASCII(String s) {
    for (int i = 0; i < s.length(); i++)
      if (s.charAt(i) > 127)
        return false;
    return true;
  }


}
