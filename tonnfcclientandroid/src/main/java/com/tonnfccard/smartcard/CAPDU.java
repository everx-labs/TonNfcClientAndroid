package com.tonnfccard.smartcard;


import androidx.annotation.RestrictTo;

import com.tonnfccard.utils.ByteArrayUtil;

import java.util.Arrays;

import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_APDU_DATA_FIELD_LEN_INCORRECT;

/**
 * Class wrapper to represent APDU command that is sent to smart card
 * We are using the following notation:
 * <p>
 * CLA = APDU command class
 * INS = APDU command type
 * P1= first param of APDU
 * P2= second param of APDU
 * LC = length of input data for APDU command
 * LE = length of response data array for APDU command.
 * <p>
 * <p>
 * Each APDU command field (CLA, INS, P1, P2, Lc and Le)  has a size = 1 byte except of Data.
 * APDU command may have one of the following format:
 * CLA | INS | P1 | P2
 * CLA | INS | P1 | P2 | LC | Data
 * CLA | INS | P1 | P2 | LC | Data | LE
 * CLA | INS | P1 | P2 | LE
 * <p>
 * Note:
 * 1) LE = 0 usually means that applet must return all the data that it has
 * 2) LE = -1 we use for the case when really LE is absent and we do not wait for response from the card.
 */

public class CAPDU {
    public final static int MAX_DATA_LEN = 255;
    public final static int HEADER_LENGTH = 4;
    private final static ByteArrayUtil BYTE_ARRAY_HELPER = ByteArrayUtil.getInstance();
    private final static byte[] EMPTY_DATA = new byte[0];

    private byte[] bytes;

    public CAPDU(byte cla, byte ins, byte p1, byte p2) {
        this.bytes = BYTE_ARRAY_HELPER.bConcat(new byte[]{cla, ins, p1, p2});
    }

    public CAPDU(byte cla, byte ins, byte p1, byte p2, byte le) {
        this.bytes = BYTE_ARRAY_HELPER.bConcat(new byte[]{cla, ins, p1, p2, le});
    }

    public CAPDU(byte cla, byte ins, byte p1, byte p2, byte[] dataField) {
        checkDataField(dataField);
        //if (dataField == null) return;
        this.bytes = BYTE_ARRAY_HELPER.bConcat(new byte[]{cla, ins, p1, p2, (byte) dataField.length}, dataField);
    }

    public CAPDU(byte cla, byte ins, byte p1, byte p2, byte[] dataField, byte le) {
        checkDataField(dataField);
        this.bytes = BYTE_ARRAY_HELPER.bConcat(new byte[]{cla, ins, p1, p2, (byte) dataField.length}, dataField, new byte[]{le});
    }

    public byte getCla() {
        return bytes[0];
    }

    public byte getIns() {
        return bytes[1];
    }

    public byte getP1() {
        return bytes[2];
    }

    public byte getP2() {
        return bytes[3];
    }

    public int getLc() {
        if (bytes.length <= HEADER_LENGTH + 1) return 0;
        return (0xFF & bytes[4]);
    }

    public int getLe() {
        if (bytes.length <= HEADER_LENGTH ||
                (getLc() != 0 && bytes.length == HEADER_LENGTH + 1 + getLc())) return -1;
        return (0xFF & bytes[bytes.length - 1]);
    }

    public byte[] getData() {
        if (getLc() > 0)
            return BYTE_ARRAY_HELPER.bSub(bytes, HEADER_LENGTH + 1, getLc());
        return EMPTY_DATA;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public StringBuilder getFormattedApdu() {
        StringBuilder apduFormated = new StringBuilder().append(BYTE_ARRAY_HELPER.hex(getCla())).append(" ")
                .append(BYTE_ARRAY_HELPER.hex(getIns())).append(" ")
                .append(BYTE_ARRAY_HELPER.hex(getP1())).append(" ")
                .append(BYTE_ARRAY_HELPER.hex(getP2())).append(" ");

        if (getLc() > 0) {
            apduFormated.append(BYTE_ARRAY_HELPER.hex(getLc())).append(" ")
                    .append(BYTE_ARRAY_HELPER.hex(getData())).append(" ");
        }

        if (getLe() != -1) {
            apduFormated.append(BYTE_ARRAY_HELPER.hex(getLe())).append(" ");
        }
        return apduFormated;
    }

    @Override
    public String toString() {
        return BYTE_ARRAY_HELPER.hex(getBytes());
    }

    private static void checkDataField(byte[] dataField) {
        if (dataField == null || dataField.length == 0 || dataField.length > MAX_DATA_LEN) {
            throw new IllegalArgumentException(ERROR_MSG_APDU_DATA_FIELD_LEN_INCORRECT);
        }
    }

    @Override
    public boolean equals(Object o) {
        // If the object is compared with itself then return true
        if (o == this) {
            return true;
        }
        /* Check if o is an instance of Complex or not
          "null instanceof [type]" also returns false */
        if (!(o instanceof CAPDU)) {
            return false;
        }
        CAPDU capdu = (CAPDU) o;
        return Arrays.equals(bytes, ((CAPDU) o).bytes);
    }
}
