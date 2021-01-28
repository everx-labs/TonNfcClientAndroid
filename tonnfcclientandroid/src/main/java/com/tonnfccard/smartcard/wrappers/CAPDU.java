package com.tonnfccard.smartcard.wrappers;


import com.tonnfccard.utils.ByteArrayHelper;

import static com.tonnfccard.api.utils.ResponsesConstants.ERROR_MSG_APDU_DATA_FIELD_LEN_INCORRECT;

public class CAPDU {
    private static final ByteArrayHelper BYTE_ARRAY_HELPER = ByteArrayHelper.getInstance();

    private final static byte[] EMPTY_DATA = new byte[0];

    private final static int HEADER_LENGTH = 4;

    private byte[] bytes;

    public CAPDU(int cla, int ins, int p1, int p2) {
        this.bytes = BYTE_ARRAY_HELPER.bConcat(new byte[]{(byte)cla, (byte)ins, (byte)p1, (byte)p2});
    }

    public CAPDU(int cla, int ins, int p1, int p2, int le)  {
        this.bytes = BYTE_ARRAY_HELPER.bConcat(new byte[]{(byte)cla, (byte)ins, (byte)p1, (byte)p2, (byte) le});
    }

    public CAPDU(int cla, int ins, int p1, int p2, byte[] dataField)   {
       // checkDataField(dataField);
      if (dataField == null) return;
        this.bytes = BYTE_ARRAY_HELPER.bConcat(new byte[]{(byte)cla, (byte)ins, (byte)p1, (byte)p2, (byte) dataField.length}, dataField);
    }

    public CAPDU(int cla, int ins, int p1, int p2, byte[] dataField, int le)  {
      //  checkDataField(dataField);
        this.bytes = BYTE_ARRAY_HELPER.bConcat(new byte[]{(byte)cla, (byte)ins, (byte)p1, (byte)p2, (byte) dataField.length}, dataField, new byte[]{(byte) le});
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
        if(bytes.length <= HEADER_LENGTH + 1) return 0;
        return (0xFF & bytes[4]);
    }

    public int getLe() {
        if (bytes.length <= HEADER_LENGTH ||
                (getLc() != 0 && bytes.length == HEADER_LENGTH + 1 + getLc())) return -1;
        return (0xFF & bytes[bytes.length-1]);
    }

    public byte[] getData()  {
        if(getLc()> 0)
            return BYTE_ARRAY_HELPER.bSub(bytes, HEADER_LENGTH + 1, getLc());
        return EMPTY_DATA;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public StringBuilder getFormattedApdu()  {
      StringBuilder apduFormated = new StringBuilder().append(BYTE_ARRAY_HELPER.hex(getCla())).append(" ")
        .append(BYTE_ARRAY_HELPER.hex(getIns())).append(" ")
        .append(BYTE_ARRAY_HELPER.hex(getP1())).append(" ")
        .append(BYTE_ARRAY_HELPER.hex(getP2())).append(" ");

      if (getLc() > 0) {
        apduFormated.append(BYTE_ARRAY_HELPER.hex(getLc())).append(" ")
          .append(BYTE_ARRAY_HELPER.hex(getData())).append(" ");
      }

      if (getLe() == -1) {
        apduFormated.append(BYTE_ARRAY_HELPER.hex(getLe())).append(" ");
      }
      return apduFormated;
    }

    @Override
    public String toString()  {
      try {
        return BYTE_ARRAY_HELPER.hex(getBytes());
      }
      catch (Exception e) {
        return "";
      }
    }

    private static void checkDataField(byte[] dataField) {
        if (dataField == null || dataField.length == 0 || dataField.length > 255){
          throw new IllegalArgumentException(ERROR_MSG_APDU_DATA_FIELD_LEN_INCORRECT);
        }
    }
}
