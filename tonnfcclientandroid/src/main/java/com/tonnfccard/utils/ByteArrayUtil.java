package com.tonnfccard.utils;

import com.tonnfccard.helpers.StringHelper;

import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_ARRAYS_ARE_NULL;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_COUNT_IS_LESS_THEN_ZERO;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_COUNT_IS_TOO_BIG;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_DEST_ARRAY_IS_NULL;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_END_INDEX_OUT_OF_BOUND;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_FIRST_ARRAY_IS_NULL;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_FROM_IS_LESS_THEN_ZERO;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_FROM_IS_OUT_OF_BOUND;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_LENGTH_IS_NOT_POSITIVE;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_OFFSET_IS_NOT_CORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_SECOND_ARRAY_IS_NULL;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_SOURCE_ARRAY_IS_NULL;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_SOURCE_ARRAY_LENGTH_LESS_THAN_TWO;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_STRING_IS_NOT_CORRECT_HEX;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_STRING_IS_NULL;

public class ByteArrayUtil {

  private final char[] HEX_ARRAY = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

  private static StringHelper stringHelper = StringHelper.getInstance();

  private static ByteArrayUtil instance;

  public static ByteArrayUtil getInstance() {
    if (instance == null) {
      instance = new ByteArrayUtil();
    }
    return instance;
  }

  private ByteArrayUtil(){}

  /**
   * Take count of bytes of array from the left side
   */
  public  byte[] bLeft(byte[] src, int count)  {
    if (src == null) throw new IllegalArgumentException(ERROR_MSG_SOURCE_ARRAY_IS_NULL);
    if (count < 0) throw new IllegalArgumentException(ERROR_MSG_COUNT_IS_LESS_THEN_ZERO);
    if (count > src.length) throw new IllegalArgumentException(ERROR_MSG_COUNT_IS_TOO_BIG + count + " > " + src.length + ")");
    byte[] result = new byte[count];
    System.arraycopy(src, 0, result, 0, count);
    return result;
  }

  /**
   * Take count of bytes of array from the right side
   */
  public byte[] bRight(byte[] src, int count)  {
    if (src == null) throw new IllegalArgumentException(ERROR_MSG_SOURCE_ARRAY_IS_NULL);
    if (count < 0) throw new IllegalArgumentException(ERROR_MSG_COUNT_IS_LESS_THEN_ZERO);
    if (count > src.length) throw new IllegalArgumentException(ERROR_MSG_COUNT_IS_TOO_BIG + count + " > " + src.length + ")");
    byte[] result = new byte[count];
    System.arraycopy(src, src.length - count, result, 0, count);
    return result;
  }


  /**
   * Take a part from array
   */
  public byte[] bSub(byte[] src, int from, int count)  {
    if (src == null) throw new IllegalArgumentException(ERROR_MSG_SOURCE_ARRAY_IS_NULL);
    if (count < 0) throw new IllegalArgumentException(ERROR_MSG_COUNT_IS_LESS_THEN_ZERO);
    if (from < 0) throw new IllegalArgumentException(ERROR_MSG_FROM_IS_LESS_THEN_ZERO);
    if (from >= src.length)
      throw new IllegalArgumentException(ERROR_MSG_FROM_IS_OUT_OF_BOUND + from + ">=" + (src.length) + ")");
    if (from + count > src.length)
      throw new IllegalArgumentException(ERROR_MSG_END_INDEX_OUT_OF_BOUND + (from + count) + ">" + (src.length) + ")");
    byte[] result = new byte[count];
    System.arraycopy(src, from, result, 0, count);
    return result;
  }

  public byte[] bSub(byte[] src, int from)  {
    return bSub(src, from, src.length - from);
  }

  /**
   * Arrays concatenation
   */

  public byte[] bConcat(byte[]... arrays) {
    if (arrays == null) throw new IllegalArgumentException(ERROR_MSG_ARRAYS_ARE_NULL);
    int totalLen = 0;
    for (byte[] array : arrays) {
      if (array != null)
        totalLen += array.length;
    }

    if (totalLen > 0) {
      int counter = 0;
      byte[] result = new byte[totalLen];
      for (byte[] array : arrays) {
        if (array != null) {
          int len = array.length;
          System.arraycopy(array, 0, result, counter, len);
          counter += array.length;
        }
      }
      return result;
    }
    else return null;
  }

  /**
   * Array copy
   */
  public byte[] bCopy(byte[] src)  {
    if (src == null) throw new IllegalArgumentException(ERROR_MSG_SOURCE_ARRAY_IS_NULL);
    byte[] result = new byte[src.length];
    System.arraycopy(src, 0, result, 0, src.length);
    return result;
  }

  /**
   * Convert byte to hex-string
   */
  public String hex(byte b)  {
    return hex(new byte[]{b});
  }

  /**
   * Convert integer to hex-string representation
   */
  public String hex(int i) {
    if (i <= 0xFF) return String.format("%02X", i);
    else if (i <= 0xFFFF) return String.format("%04X", i);
    else if (i <= 0xFFFFFF) return String.format("%06X", i);
    else return String.format("%08X", i);
  }

  /**
   * Convert byte array to hex-string
   */
  public String hex(byte[] src)  {
    if (src == null) throw new IllegalArgumentException(ERROR_MSG_SOURCE_ARRAY_IS_NULL);
   // if (src.length == 0) throw new IllegalArgumentException(ERROR_MSG_SOURCE_ARRAY_IS_EMPTY);
    char[] hexChars = new char[2 * src.length];
    int v;
    for (int j = 0; j < src.length; j++) {
      v = src[j] & 0xFF;
      hexChars[j * 2] = HEX_ARRAY[v >>> 4];
      hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
    }
    return new String(hexChars);
  }

  /**
   * Convert hex-string to byte array
   */
  public byte[] bytes(String s)  {
    if (s == null) throw new IllegalArgumentException(ERROR_MSG_STRING_IS_NULL);
    if (!stringHelper.isHexString(s))
      throw new IllegalArgumentException(ERROR_MSG_STRING_IS_NOT_CORRECT_HEX + s + "'");
    int len = s.length();
    byte[] data = new byte[len / 2];
    for (int i = 0; i < len; i += 2) {
      data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
        + Character.digit(s.charAt(i + 1), 16));
    }
    return data;
  }

  /**
   * Make byte array (1-byte length) from one byte
   */
  public byte[] bytes(byte b) {
    return new byte[]{b};
  }

  /**
   * Make byte array from integer
   */
  public byte[] bytes(int i) {
    if (i <= 0xFF) return new byte[]{(byte) i};
    else if (i <= 0xFFFF) return new byte[]{(byte) (i >> 8), (byte) i};
    else if (i <= 0xFFFFFF) return new byte[]{(byte) (i >> 16), (byte) (i >> 8), (byte) i};
    else return new byte[]{(byte) (i >> 24), (byte) (i >> 16), (byte) (i >> 8), (byte) i};
  }

  /**
   * Compare two byte arrays
   */
  public boolean bEquals(byte[] first, byte[] second)  {
    if (first == null) throw new IllegalArgumentException(ERROR_MSG_FIRST_ARRAY_IS_NULL);
    if (second == null) throw new IllegalArgumentException(ERROR_MSG_SECOND_ARRAY_IS_NULL);
    if (first.length != second.length) return false;
    for (int i = 0; i < first.length; i++) {
      byte b1 = first[i];
      byte b2 = second[i];
      if (b1 != b2) return false;
    }
    return true;
  }

  public void arrayCopy(byte[] src, int srcOff, byte[] dest, int destOff, int length)  {
    if (src == null) throw new IllegalArgumentException(ERROR_MSG_SOURCE_ARRAY_IS_NULL);
    if (dest == null) throw new IllegalArgumentException(ERROR_MSG_DEST_ARRAY_IS_NULL);
    if (length <= 0) throw new IllegalArgumentException(ERROR_MSG_LENGTH_IS_NOT_POSITIVE);
    if (srcOff < 0 || destOff < 0 ||  src.length < (srcOff + length) || dest.length < (destOff + length))
      throw new IllegalArgumentException(ERROR_MSG_OFFSET_IS_NOT_CORRECT);
    System.arraycopy(src, srcOff, dest, destOff, length);
  }

  public int makeShort(byte[] src, int srcOff) {
    if (src == null) throw new IllegalArgumentException(ERROR_MSG_SOURCE_ARRAY_IS_NULL);
    if (src.length < 2) throw new IllegalArgumentException(ERROR_MSG_SOURCE_ARRAY_LENGTH_LESS_THAN_TWO);
    if (srcOff < 0 || src.length < (srcOff + 2)) throw new IllegalArgumentException(ERROR_MSG_OFFSET_IS_NOT_CORRECT);
    int b0 = src[srcOff] & 0xFF;
    int b1 = src[srcOff + 1] & 0xFF;
    return (b0 << 8) + b1;
  }

  public void setShort(byte[] src, short srcOff, short sValue)  {
    if (src == null) throw new IllegalArgumentException(ERROR_MSG_SOURCE_ARRAY_IS_NULL);
    if (src.length < 2) throw new IllegalArgumentException(ERROR_MSG_SOURCE_ARRAY_LENGTH_LESS_THAN_TWO);
    if (srcOff < 0 || src.length < (srcOff + 2)) throw new IllegalArgumentException(ERROR_MSG_OFFSET_IS_NOT_CORRECT);
    src[srcOff] = (byte) (sValue >> 8);
    src[srcOff + 1] = (byte) (sValue);
  }

}
