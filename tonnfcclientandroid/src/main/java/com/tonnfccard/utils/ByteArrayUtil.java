package com.tonnfccard.utils;

import com.tonnfccard.helpers.StringHelper;

import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_ARRAYS_ARE_NULL;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_COUNT_IS_LESS_THEN_ZERO;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_COUNT_IS_TOO_BIG;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_DEST_ARRAY_IS_NULL;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_DEST_OFFSET_IS_NOT_CORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_END_INDEX_OUT_OF_BOUND;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_FIRST_ARRAY_IS_NULL;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_FROM_IS_LESS_THEN_ZERO;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_OFFSET_IS_OUT_OF_BOUND;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_LENGTH_IS_NOT_POSITIVE;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_SECOND_ARRAY_IS_NULL;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_SOURCE_ARRAY_IS_NULL;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_SOURCE_ARRAY_LENGTH_LESS_THAN_TWO;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_SOURCE_OFFSET_IS_NOT_CORRECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_STRING_IS_NOT_CORRECT_HEX;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_STRING_IS_NULL;

/**
 * Utility class providing functions to handle byte arrays, hex representations of byte arrays and integer numbers
 */

public class ByteArrayUtil {

  private static StringHelper stringHelper = StringHelper.getInstance();

  private static ByteArrayUtil instance;

  public static ByteArrayUtil getInstance() {
    if (instance == null) {
      instance = new ByteArrayUtil();
    }
    return instance;
  }

  private ByteArrayUtil(){}

  //Take count of bytes of array from the left side
  public  byte[] bLeft(byte[] src, int count)  {
    if (src == null) throw new IllegalArgumentException(ERROR_MSG_SOURCE_ARRAY_IS_NULL);
    if (count < 0) throw new IllegalArgumentException(ERROR_MSG_COUNT_IS_LESS_THEN_ZERO);
    if (count > src.length) throw new IllegalArgumentException(ERROR_MSG_COUNT_IS_TOO_BIG + count + " > " + src.length + ")");
    byte[] result = new byte[count];
    System.arraycopy(src, 0, result, 0, count);
    return result;
  }

  //Take count of bytes of array from the right side
  public byte[] bRight(byte[] src, int count)  {
    if (src == null) throw new IllegalArgumentException(ERROR_MSG_SOURCE_ARRAY_IS_NULL);
    if (count < 0) throw new IllegalArgumentException(ERROR_MSG_COUNT_IS_LESS_THEN_ZERO);
    if (count > src.length) throw new IllegalArgumentException(ERROR_MSG_COUNT_IS_TOO_BIG + count + " > " + src.length + ")");
    byte[] result = new byte[count];
    System.arraycopy(src, src.length - count, result, 0, count);
    return result;
  }


  //Take a part from array of length count starting from offset
  public byte[] bSub(byte[] src, int offset, int count)  {
    if (src == null) throw new IllegalArgumentException(ERROR_MSG_SOURCE_ARRAY_IS_NULL);
    if (count < 0) throw new IllegalArgumentException(ERROR_MSG_COUNT_IS_LESS_THEN_ZERO);
    if (offset < 0) throw new IllegalArgumentException(ERROR_MSG_FROM_IS_LESS_THEN_ZERO);
    if (offset >= src.length)
      throw new IllegalArgumentException(ERROR_MSG_OFFSET_IS_OUT_OF_BOUND + offset + ">=" + (src.length) + ")");
    if (offset + count > src.length)
      throw new IllegalArgumentException(ERROR_MSG_END_INDEX_OUT_OF_BOUND + (offset + count) + ">" + (src.length) + ")");
    byte[] result = new byte[count];
    System.arraycopy(src, offset, result, 0, count);
    return result;
  }

  // Make concatenation of byte arrays. If each input is null then throws IllegalArgumentException.
  // If there is at list one non null array, then we concatenate and during concatenation null array are skipped.
  public byte[] bConcat(byte[]... arrays) {
    if (arrays == null) throw new IllegalArgumentException(ERROR_MSG_ARRAYS_ARE_NULL);
    boolean empty = true;
    for (int i=0; i<arrays.length; i++) {
      if (arrays[i] != null) {
        empty = false;
        break;
      }
    }
    if (empty) throw new IllegalArgumentException(ERROR_MSG_ARRAYS_ARE_NULL);
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
    else return new byte[0];
  }

  //Create a copy of array.
  //If array is null throws IllegalArgumentException
  public byte[] bCopy(byte[] src)  {
    if (src == null) throw new IllegalArgumentException(ERROR_MSG_SOURCE_ARRAY_IS_NULL);
    byte[] result = new byte[src.length];
    System.arraycopy(src, 0, result, 0, src.length);
    return result;
  }

  //Convert byte to hex string of length 2
  public String hex(byte b)  {
    return hex(new byte[]{b});
  }

  public String hex(short i) {
    if (i >= 0 && i <= 0xFF) return String.format("%02X", i);
    else  return String.format("%04X", i);
  }

  //Convert integer to hex string representation of even length
  public String hex(int i) {
    if (i >= 0 && i <= 0xFF) return String.format("%02X", i);
    else if (i >= 0 && i <= 0xFFFF) return String.format("%04X", i);
    else if (i >= 0 && i <= 0xFFFFFF) return String.format("%06X", i);
    else return String.format("%08X", i);
  }


  //Convert byte array to hex string of even length.
  //If array is null throws IllegalArgumentException.
  public String hex(byte[] src)  {
    if (src == null) throw new IllegalArgumentException(ERROR_MSG_SOURCE_ARRAY_IS_NULL);
   // if (src.length == 0) throw new IllegalArgumentException(ERROR_MSG_SOURCE_ARRAY_IS_EMPTY);
    StringBuilder stringBuilder = new StringBuilder();
    for (int j = 0; j < src.length; j++) {
      stringBuilder.append(String.format("%02X", src[j]));
    }
    return stringBuilder.toString();
  }

  //Convert hex non-empty string of even length to byte array.
  //If string is null, has length = 0 or odd length, or not in hex then throws IllegalArgumentException.
  public byte[] bytes(String s)  {
    if (s == null) throw new IllegalArgumentException(ERROR_MSG_STRING_IS_NULL);
    if (!stringHelper.isHexString(s))
      throw new IllegalArgumentException(ERROR_MSG_STRING_IS_NOT_CORRECT_HEX + s);
    int len = s.length();
    byte[] data = new byte[len / 2];
    for (int i = 0; i < len; i += 2) {
      data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
        + Character.digit(s.charAt(i + 1), 16));
    }
    return data;
  }

  //Make byte array (1-byte length) from one byte
  public byte[] bytes(byte b) {
    return new byte[]{b};
  }

  public byte[] bytes(short i) {
    if (i>= 0 && i <= 0xFF) return new byte[]{(byte) i};
    else  return new byte[]{(byte) (i >> 8), (byte) i};
  }

  //Make byte array from integer. Depending on integer value array has size 1, 2, 3 or 4 respectively.
  //Big endian order is used.
  //For example: 0x80000000 -> new byte[]{(byte) 0x80, (byte) 0x00, (byte) 0x00, (byte) 0x00}.
  public byte[] bytes(int i) {
    if (i>= 0 && i <= 0xFF) return new byte[]{(byte) i};
    else if (i>= 0 && i <= 0xFFFF) return new byte[]{(byte) (i >> 8), (byte) i};
    else if (i>= 0 && i <= 0xFFFFFF) return new byte[]{(byte) (i >> 16), (byte) (i >> 8), (byte) i};
    else return new byte[]{(byte) (i >> 24), (byte) (i >> 16), (byte) (i >> 8), (byte) i};
  }

  //Compare two byte arrays, if one of them is null then throws IllegalArgumentException.
  public boolean bEquals(byte[] first, byte[] second)  {
    if (first == null) throw new IllegalArgumentException(ERROR_MSG_FIRST_ARRAY_IS_NULL);
    if (second == null) throw new IllegalArgumentException(ERROR_MSG_SECOND_ARRAY_IS_NULL);
    if (first.length != second.length) return false;
    for (int i = 0; i < first.length; i++) {
      if (first[i] != second[i]) return false;
    }
    return true;
  }

  //Copy length elements of src array starting from srcOff into array dest starting from destOff.
  //If src or dest is null, length <= 0, srcOff < 0, destOff < 0 or (srcOff + length) > src.length,
  //(destOff + length)> dest.length then throws IllegalArgumentException.
  public void arrayCopy(byte[] src, int srcOff, byte[] dest, int destOff, int length)  {
    if (src == null) throw new IllegalArgumentException(ERROR_MSG_SOURCE_ARRAY_IS_NULL);
    if (dest == null) throw new IllegalArgumentException(ERROR_MSG_DEST_ARRAY_IS_NULL);
    if (length <= 0) throw new IllegalArgumentException(ERROR_MSG_LENGTH_IS_NOT_POSITIVE);
    if (srcOff < 0 || src.length < (srcOff + length) )
      throw new IllegalArgumentException(ERROR_MSG_SOURCE_OFFSET_IS_NOT_CORRECT);
    if (destOff < 0 ||   dest.length < (destOff + length))
      throw new IllegalArgumentException(ERROR_MSG_DEST_OFFSET_IS_NOT_CORRECT);
    System.arraycopy(src, srcOff, dest, destOff, length);
  }

  //Computes short value (2^8)*src[srcOff] + src[srcOff + 1]
  //src must not be null and srcOff + 2 <= src.length, otherwise IllegalArgumentException is thrown
  public short makeShort(byte[] src, int srcOff) {
    if (src == null) throw new IllegalArgumentException(ERROR_MSG_SOURCE_ARRAY_IS_NULL);
    if (src.length < 2) throw new IllegalArgumentException(ERROR_MSG_SOURCE_ARRAY_LENGTH_LESS_THAN_TWO);
    if (srcOff < 0 || src.length < (srcOff + 2)) throw new IllegalArgumentException(ERROR_MSG_SOURCE_OFFSET_IS_NOT_CORRECT);
    int b0 = src[srcOff] & 0xFF;
    int b1 = src[srcOff + 1] & 0xFF;
    return (short)((b0 << 8) + b1);
  }

  //Split sValue into two bytes and put into src starting from srcOff position.
  //Big endian is used.
  //1st byte of sValue goes into src[srcOff + 1], 2nd byte goes into src[srcOff]
  //src must not be null and srcOff + 2 <= src.length, otherwise IllegalArgumentException is thrown
  public void setShort(byte[] src, int srcOff, short sValue)  {
    if (src == null) throw new IllegalArgumentException(ERROR_MSG_SOURCE_ARRAY_IS_NULL);
    if (src.length < 2) throw new IllegalArgumentException(ERROR_MSG_SOURCE_ARRAY_LENGTH_LESS_THAN_TWO);
    if (srcOff < 0 || src.length < (srcOff + 2)) throw new IllegalArgumentException(ERROR_MSG_SOURCE_OFFSET_IS_NOT_CORRECT);
    src[srcOff] = (byte) (sValue >> 8);
    src[srcOff + 1] = (byte) (sValue);
  }
}
