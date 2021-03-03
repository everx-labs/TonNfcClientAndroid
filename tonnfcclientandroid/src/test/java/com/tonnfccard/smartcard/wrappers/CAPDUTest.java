package com.tonnfccard.smartcard.wrappers;

import com.tonnfccard.helpers.StringHelper;
import com.tonnfccard.smartcard.CAPDU;
import com.tonnfccard.utils.ByteArrayUtil;

import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class CAPDUTest {

    private static final ByteArrayUtil BYTE_ARRAY_HELPER = ByteArrayUtil.getInstance();
    private static final StringHelper STR_HELPER = StringHelper.getInstance();

    @Test
    public void CAPDUTest1() {
        int cla = 0x01;
        int ins = 0x02;
        int p1 = 0x00;
        int p2 = 0x03;
        CAPDU capdu = new CAPDU(cla, ins, p1, p2);
        assertArrayEquals(new byte[]{(byte) cla, (byte) ins, (byte) p1, (byte) p2}, capdu.getBytes());
        assertEquals(capdu.getCla(), (byte) cla);
        assertEquals(capdu.getIns(), (byte) ins);
        assertEquals(capdu.getP1(), (byte) p1);
        assertEquals(capdu.getP2(), (byte) p2);
        assertEquals(capdu.getLc(), 0x00);
        assertEquals(capdu.getData().length, 0x00);
        assertEquals(capdu.getLe(), -1);
        assertEquals(capdu.toString(), "01020003");
    }

    @Test
    public void CAPDUTest2() {
        int cla = 0x21;
        int ins = 0x32;
        int p1 = 0x01;
        int p2 = 0x02;
        for (int le = 0x00; le <= 0xFF; le++) {
            CAPDU capdu = new CAPDU(cla, ins, p1, p2, le);
            assertArrayEquals(new byte[]{(byte) cla, (byte) ins, (byte) p1, (byte) p2, (byte) le}, capdu.getBytes());
            assertEquals(capdu.getCla(), (byte) cla);
            assertEquals(capdu.getIns(), (byte) ins);
            assertEquals(capdu.getP1(), (byte) p1);
            assertEquals(capdu.getP2(), (byte) p2);
            assertEquals(capdu.getLc(), 0x00);
            assertEquals(capdu.getData().length, 0x00);
            assertEquals(capdu.getLe(), le);
            assertEquals(capdu.toString(), "21320102" + BYTE_ARRAY_HELPER.hex((byte) le));
        }
    }

    @Test
    public void CAPDUTest3() {
        int cla = 0xB0;
        int ins = 0xA1;
        int p1 = 0x0A;
        int p2 = 0x0B;
        Random random = new Random();
        for (int len = 0x01; len <= 0xFF; len++) {
            byte[] dataField = new byte[len];
            random.nextBytes(dataField);
            CAPDU capdu = new CAPDU(cla, ins, p1, p2, dataField);
            assertArrayEquals(BYTE_ARRAY_HELPER.bConcat(new byte[]{(byte) cla, (byte) ins, (byte) p1, (byte) p2, (byte) dataField.length}, dataField), capdu.getBytes());
            assertEquals(capdu.getCla(), (byte) cla);
            assertEquals(capdu.getIns(), (byte) ins);
            assertEquals(capdu.getP1(), (byte) p1);
            assertEquals(capdu.getP2(), (byte) p2);
            assertEquals(capdu.getLc(), dataField.length);
            assertArrayEquals(capdu.getData(), dataField);
            assertEquals(capdu.getLe(), -1);
            assertEquals(capdu.toString(), "B0A10A0B" + BYTE_ARRAY_HELPER.hex((byte) dataField.length) + BYTE_ARRAY_HELPER.hex(dataField));
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void CAPDUTest4() {
        int cla = 0xB0;
        int ins = 0xA1;
        int p1 = 0x0A;
        int p2 = 0x0B;
        byte[] dataField = new byte[0];
        new CAPDU(cla, ins, p1, p2, dataField);
    }

    @Test(expected = IllegalArgumentException.class)
    public void CAPDUTest5() {
        int cla = 0xB0;
        int ins = 0xA1;
        int p1 = 0x0A;
        int p2 = 0x0B;
        new CAPDU(cla, ins, p1, p2, null);
    }

    @Test
    public void CAPDUTest6() {
        int cla = 0xB0;
        int ins = 0xA1;
        int p1 = 0x0A;
        int p2 = 0x0B;
        Random random = new Random();
        for (int le = 0x00; le <= 0xFF; le++) {
            for (int len = 0x01; len <= 0xFF; len++) {
                byte[] dataField = new byte[len];
                random.nextBytes(dataField);
                CAPDU capdu = new CAPDU(cla, ins, p1, p2, dataField, le);
                assertArrayEquals(BYTE_ARRAY_HELPER.bConcat(new byte[]{(byte) cla, (byte) ins, (byte) p1, (byte) p2, (byte) dataField.length}, dataField, new byte[]{(byte) le}), capdu.getBytes());
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
    public void CAPDUTest7() {
        int cla = 0xB0;
        int ins = 0xA1;
        int p1 = 0x0A;
        int p2 = 0x0B;
        byte[] dataField = new byte[0];
        for (int le = 0x00; le <= 0xFF; le++) {
            try {
                new CAPDU(cla, ins, p1, p2, dataField, 0xFF);
                fail();
            } catch (IllegalArgumentException e) {
            }
        }
    }

    @Test
    public void CAPDUTest8() {
        int cla = 0xB0;
        int ins = 0xA1;
        int p1 = 0x0A;
        int p2 = 0x0B;
        for (int le = 0x00; le <= 0xFF; le++) {
            try {
                new CAPDU(cla, ins, p1, p2, null, 0xFF);
                fail();
            } catch (IllegalArgumentException e) {
            }
        }
    }

}