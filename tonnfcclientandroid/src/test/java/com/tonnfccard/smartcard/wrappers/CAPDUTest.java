package com.tonnfccard.smartcard.wrappers;

import com.tonnfccard.smartcard.CAPDU;
import com.tonnfccard.utils.ByteArrayUtil;
import org.junit.Test;
import java.util.Random;

import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_APDU_DATA_FIELD_LEN_INCORRECT;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class CAPDUTest {

    private static final ByteArrayUtil BYTE_ARRAY_HELPER = ByteArrayUtil.getInstance();

    @Test
    public void CAPDUTest1() {
        byte cla = 0x01;
        byte ins = 0x02;
        byte p1 = 0x00;
        byte p2 = 0x03;
        CAPDU capdu = new CAPDU(cla, ins, p1, p2);
        assertArrayEquals(new byte[]{cla, ins, p1, p2}, capdu.getBytes());
        assertEquals(capdu.getCla(),  cla);
        assertEquals(capdu.getIns(),  ins);
        assertEquals(capdu.getP1(),  p1);
        assertEquals(capdu.getP2(),  p2);
        assertEquals(capdu.getLc(), 0x00);
        assertEquals(capdu.getData().length, 0x00);
        assertEquals(capdu.getLe(), -1);
        assertEquals(capdu.toString(), "01020003");
    }

    @Test
    public void CAPDUTest2() {
        byte cla = 0x21;
        byte ins = 0x32;
        byte p1 = 0x01;
        byte p2 = 0x02;
        for (int le = 0x00; le <= 0xFF; le++) {
            CAPDU capdu = new CAPDU(cla, ins, p1, p2, (byte) le);
            assertArrayEquals(new byte[]{cla, ins, p1, p2, (byte) le}, capdu.getBytes());
            assertEquals(capdu.getCla(), cla);
            assertEquals(capdu.getIns(), ins);
            assertEquals(capdu.getP1(), p1);
            assertEquals(capdu.getP2(), p2);
            assertEquals(capdu.getLc(), 0x00);
            assertEquals(capdu.getData().length, 0x00);
            assertEquals(capdu.getLe(), le);
            assertEquals(capdu.toString(), "21320102" + BYTE_ARRAY_HELPER.hex((byte) le));
        }
    }

    @Test
    public void CAPDUTest3() {
        byte cla = (byte) 0xB0;
        byte ins = (byte) 0xA1;
        byte p1 = 0x0A;
        byte p2 = 0x0B;
        Random random = new Random();
        for (int len = 0x01; len <= 0xFF; len++) {
            byte[] dataField = new byte[len];
            random.nextBytes(dataField);
            CAPDU capdu = new CAPDU(cla, ins, p1, p2, dataField);
            assertArrayEquals(BYTE_ARRAY_HELPER.bConcat(new byte[]{cla, ins,  p1,  p2, (byte) dataField.length}, dataField), capdu.getBytes());
            assertEquals(capdu.getCla(), cla);
            assertEquals(capdu.getIns(), ins);
            assertEquals(capdu.getP1(), p1);
            assertEquals(capdu.getP2(), p2);
            assertEquals(capdu.getLc(), dataField.length);
            assertArrayEquals(capdu.getData(), dataField);
            assertEquals(capdu.getLe(), -1);
            assertEquals(capdu.toString(), "B0A10A0B" + BYTE_ARRAY_HELPER.hex((byte) dataField.length) + BYTE_ARRAY_HELPER.hex(dataField));
        }
    }

    @Test
    public void CAPDUTest4() {
        byte cla = (byte) 0xB0;
        byte ins = (byte) 0xA1;
        byte p1 = 0x0A;
        byte p2 = 0x0B;

        try {
            byte[] dataField = new byte[0];
            new CAPDU(cla, ins, p1, p2, dataField);
            fail();
        }
        catch (IllegalArgumentException e) {
            assertEquals(e.getMessage(), ERROR_MSG_APDU_DATA_FIELD_LEN_INCORRECT);
        }

        try {
            byte[] dataField = new byte[256];
            new CAPDU(cla, ins, p1, p2, dataField);
            fail();
        }
        catch (IllegalArgumentException e) {
            assertEquals(e.getMessage(), ERROR_MSG_APDU_DATA_FIELD_LEN_INCORRECT);
        }

        try {
            new CAPDU(cla, ins, p1, p2, null);
            fail();
        }
        catch (IllegalArgumentException e) {
            assertEquals(e.getMessage(), ERROR_MSG_APDU_DATA_FIELD_LEN_INCORRECT);
        }
    }

    @Test
    public void CAPDUTest5() {
        byte cla = (byte) 0xB0;
        byte ins = (byte) 0xA1;
        byte p1 = 0x0A;
        byte p2 = 0x0B;
        Random random = new Random();
        for (int le = 0x00; le <= 0xFF; le++) {
            for (int len = 0x01; len <= 0xFF; len++) {
                byte[] dataField = new byte[len];
                random.nextBytes(dataField);
                CAPDU capdu = new CAPDU(cla, ins, p1, p2, dataField, (byte)le);
                assertArrayEquals(BYTE_ARRAY_HELPER.bConcat(new byte[]{cla, ins, p1, p2, (byte) dataField.length}, dataField, new byte[]{(byte) le}), capdu.getBytes());
                assertEquals(capdu.getCla(), (byte) cla);
                assertEquals(capdu.getIns(), (byte) ins);
                assertEquals(capdu.getP1(), (byte) p1);
                assertEquals(capdu.getP2(), (byte) p2);
                assertEquals(capdu.getLc(), dataField.length);
                assertArrayEquals(capdu.getData(), dataField);
                assertEquals(capdu.getLe(), le);
                assertEquals(capdu.toString(), "B0A10A0B" + BYTE_ARRAY_HELPER.hex((byte) dataField.length) + BYTE_ARRAY_HELPER.hex(dataField) + BYTE_ARRAY_HELPER.hex((byte) le));
            }
        }
    }

    @Test
    public void CAPDUTest6() {
        byte cla = (byte) 0xB0;
        byte ins = (byte) 0xA1;
        byte p1 = 0x0A;
        byte p2 = 0x0B;
        byte[] dataField = new byte[0];
        for (int le = 0x00; le <= 0xFF; le++) {
            try {
                new CAPDU(cla, ins, p1, p2, dataField, (byte) 0xFF);
                fail();
            } catch (IllegalArgumentException e) {
                assertEquals(e.getMessage(), ERROR_MSG_APDU_DATA_FIELD_LEN_INCORRECT);
            }
        }
    }

    @Test
    public void CAPDUTest8() {
        byte cla = (byte) 0xB0;
        byte ins = (byte) 0xA1;
        byte p1 = 0x0A;
        byte p2 = 0x0B;
        for (int le = 0x00; le <= 0xFF; le++) {
            try {
                new CAPDU(cla, ins, p1, p2, null, (byte) 0xFF);
                fail();
            } catch (IllegalArgumentException e) {
                assertEquals(e.getMessage(), ERROR_MSG_APDU_DATA_FIELD_LEN_INCORRECT);
            }
        }
    }

}