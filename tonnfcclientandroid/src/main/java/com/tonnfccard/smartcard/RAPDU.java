package com.tonnfccard.smartcard;

import androidx.annotation.RestrictTo;

import com.tonnfccard.utils.ByteArrayUtil;

import java.util.Arrays;

import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_APDU_RESPONSE_TOO_LONG;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_SW_TOO_SHORT;

/**
 *  Class wrapper to represent the response from smart card.
 *  Response from the card has the following structure: DATA | SW1 | SW2
 *
 *  DATA - real data (byte array) returned by the card, for example: ed25519 signature
 *  SW1 - first byte of status word
 *  SW2 - second byte of status word
 *
 *  DATA field may not exist if we do not wait the data from applet, but we always must get SW1 | SW2
 *
 *  SW1 == 0x90 && SW2 == 0x00 means that card operation is done successfully
 *  If you get another SW then some error happened. Check ErrorCodes class for more information.
 */

public class RAPDU {
    public static final int MIN_LENGTH = 2;
    public static final int MAX_LENGTH = 257;
    private static final ByteArrayUtil BYTE_ARRAY_HELPER = ByteArrayUtil.getInstance();
    private final byte[] bytes;


    public RAPDU(String response) {
        this(BYTE_ARRAY_HELPER.bytes(response));
    }

    public RAPDU(byte[] bytes) {
        if (bytes == null || bytes.length < MIN_LENGTH) throw new IllegalArgumentException(ERROR_MSG_SW_TOO_SHORT);
        if (bytes.length > MAX_LENGTH ) throw new IllegalArgumentException(ERROR_MSG_APDU_RESPONSE_TOO_LONG);
        this.bytes = bytes;
    }

    public byte[] getData() {
        return BYTE_ARRAY_HELPER.bLeft(bytes, bytes.length - 2);
    }

    public byte[] getSW() {
        return new byte[]{bytes[bytes.length - 2], bytes[bytes.length - 1]};
    }

    public byte getSW1() {
        return bytes[bytes.length-2];
    }

    public byte getSW2() {
        return bytes[bytes.length-1];
    }

    public byte[] getBytes() {
        return bytes;
    }

    public static boolean isSuccess(RAPDU rapdu) {
        return rapdu.getSW1() == (byte)0x90 && rapdu.getSW2() == (byte)0x00;
    }

    @Override
    public String toString() {
        return BYTE_ARRAY_HELPER.hex(getSW()) +
                (getData()!=null && getData().length > 0
                        ? " '"+   BYTE_ARRAY_HELPER.hex(getData())+"'"
                        : "");
    }

    public String prepareSwFormatted() {
        return BYTE_ARRAY_HELPER.hex(new byte[]{getSW1()})
                + BYTE_ARRAY_HELPER.hex(new byte[]{getSW2()});
    }

    @Override
    public boolean equals(Object o) {
        // If the object is compared with itself then return true
        if (o == this) {
            return true;
        }
        /* Check if o is an instance of Complex or not
          "null instanceof [type]" also returns false */
        if (!(o instanceof RAPDU)) {
            return false;
        }
        RAPDU rapdu = (RAPDU) o;
        return Arrays.equals(bytes, ((RAPDU) o).bytes);
    }
}
