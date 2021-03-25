package com.tonnfccard.utils;

import static com.tonnfccard.helpers.ResponsesConstants.*;
import static org.junit.Assert.*;
import org.junit.Test;

import java.util.Random;

public class ByteArrayUtilTest {
    private final static ByteArrayUtil BYTE_ARRAY_HELPER = ByteArrayUtil.getInstance();
    private Random random = new Random();

    @Test
    public void setShortTestIncorrectInput() {
        try {
            BYTE_ARRAY_HELPER.setShort(null, 0, (short) 0);
            fail();
        }
        catch (IllegalArgumentException e) {
            assertEquals(e.getMessage(), ERROR_MSG_SOURCE_ARRAY_IS_NULL);
        }

        for (int i = 0 ; i < 2 ; i++) {
            try {
                BYTE_ARRAY_HELPER.setShort(new byte[i], 0, (short) 0);
                fail();
            } catch (IllegalArgumentException e) {
                assertEquals(e.getMessage(), ERROR_MSG_SOURCE_ARRAY_LENGTH_LESS_THAN_TWO);
            }
        }

        try {
            BYTE_ARRAY_HELPER.setShort(new byte[2], 1, (short) 0);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals(e.getMessage(), ERROR_MSG_SOURCE_OFFSET_IS_NOT_CORRECT);
        }
    }


    @Test
    public void setShortTest() {
        try {
            byte[] src = new byte[2];
            short val = (short) 0x9000;
            BYTE_ARRAY_HELPER.setShort(src, 0, val);
            assertEquals(src[1], 0x00);
            assertEquals(src[0], (byte) 0x90);

            src = new byte[10];
            val = (short) 0xFFFE;
            BYTE_ARRAY_HELPER.setShort(src, 8, val);
            assertEquals(src[9], (byte) 0xFE);
            assertEquals(src[8], (byte) 0xFF);
        } catch (IllegalArgumentException e) {
            fail();
        }
    }


    @Test
    public void makeShortTestIncorrectInput() {
        try {
            BYTE_ARRAY_HELPER.makeShort(null, 0);
            fail();
        }
        catch (IllegalArgumentException e) {
            assertEquals(e.getMessage(), ERROR_MSG_SOURCE_ARRAY_IS_NULL);
        }

        for (int i = 0 ; i < 2 ; i++) {
            try {
                BYTE_ARRAY_HELPER.makeShort(new byte[i], 0);
                fail();
            } catch (IllegalArgumentException e) {
                assertEquals(e.getMessage(), ERROR_MSG_SOURCE_ARRAY_LENGTH_LESS_THAN_TWO);
            }
        }

        try {
            BYTE_ARRAY_HELPER.makeShort(new byte[2], 1);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals(e.getMessage(), ERROR_MSG_SOURCE_OFFSET_IS_NOT_CORRECT);
        }
    }

    @Test
    public void makeShortTest() {
        try {
            byte[] src = new byte[2];
            src[0] = (byte) 0x90;
            src[1] = (byte) 0x01;
            short val = (short) 0x9001;
            short res = BYTE_ARRAY_HELPER.makeShort(src, 0);
            assertEquals(val, res);

            src = new byte[20];
            src[11] = (byte) 0x7F;
            src[12] = (byte) 0xFF;
            res = BYTE_ARRAY_HELPER.makeShort(src, 11);
            assertEquals(Short.MAX_VALUE, res);

            src = new byte[100];
            src[52] = (byte) 0x80;
            src[53] = (byte) 0x00;
            res = BYTE_ARRAY_HELPER.makeShort(src, 52);
            assertEquals((short)0x8000, res);

            src = new byte[1000];
            src[200] = (byte) 0xFF;
            src[201] = (byte) 0xFF;
            res = BYTE_ARRAY_HELPER.makeShort(src, 200);
            assertEquals(-1, res);
        } catch (IllegalArgumentException e) {
            fail();
        }
    }

    @Test
    public void makeSetShortTest() {
        try {
            byte[] src = new byte[2];
            short val = (short) 0x9000;
            BYTE_ARRAY_HELPER.setShort(src, 0, val);
            short res = BYTE_ARRAY_HELPER.makeShort(src, 0);
            assertEquals(val, res);

            src = new byte[10000];
            val = (short) 0xFFEA;
            BYTE_ARRAY_HELPER.setShort(src, 5023, val);
            res = BYTE_ARRAY_HELPER.makeShort(src, 5023);
            assertEquals(val, res);


            src = new byte[2000];
            val = (short) 0x00A1;
            BYTE_ARRAY_HELPER.setShort(src, 1998, val);
            res = BYTE_ARRAY_HELPER.makeShort(src, 1998);
            assertEquals(val, res);
        } catch (IllegalArgumentException e) {
            fail();
        }
    }

    @Test
    public void arrayCopyIncorrectInputTest() {
        try {
            BYTE_ARRAY_HELPER.arrayCopy(null, 0, new  byte[1], 0, 1);
            fail();
        }
        catch (IllegalArgumentException e) {
            assertEquals(e.getMessage(), ERROR_MSG_SOURCE_ARRAY_IS_NULL);
        }

        try {
            BYTE_ARRAY_HELPER.arrayCopy(new  byte[1], 0, null, 0, 1);
            fail();
        }
        catch (IllegalArgumentException e) {
            assertEquals(e.getMessage(), ERROR_MSG_DEST_ARRAY_IS_NULL);
        }

        for (int i = -2 ; i <= 0 ; i++) {
            try {
                BYTE_ARRAY_HELPER.arrayCopy(new byte[1], 0, new byte[1], 0, i);
                fail();
            } catch (IllegalArgumentException e) {
                assertEquals(e.getMessage(), ERROR_MSG_LENGTH_IS_NOT_POSITIVE);
            }
        }

        for (int i = -3 ; i < 0 ; i++) {
            try {
                BYTE_ARRAY_HELPER.arrayCopy(new byte[1], i, new byte[1], 0, 1);
                fail();
            } catch (IllegalArgumentException e) {
                assertEquals(e.getMessage(), ERROR_MSG_SOURCE_OFFSET_IS_NOT_CORRECT);
            }
        }

        for (int i = -3 ; i < 0 ; i++) {
            try {
                BYTE_ARRAY_HELPER.arrayCopy(new byte[1], 0, new byte[1], i, 1);
                fail();
            } catch (IllegalArgumentException e) {
                assertEquals(e.getMessage(), ERROR_MSG_DEST_OFFSET_IS_NOT_CORRECT);
            }
        }

        try {
            BYTE_ARRAY_HELPER.arrayCopy(new byte[10], 8, new byte[7], 0, 5);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals(e.getMessage(), ERROR_MSG_SOURCE_OFFSET_IS_NOT_CORRECT);
        }

        try {
            BYTE_ARRAY_HELPER.arrayCopy(new byte[10], 2, new byte[7], 4, 5);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals(e.getMessage(), ERROR_MSG_DEST_OFFSET_IS_NOT_CORRECT);
        }
    }


    @Test
    public void arrayCopyTest() {
        try {
            byte[] src = new byte[10];
            random.nextBytes(src);
            byte[] dest = new byte[6];
            random.nextBytes(dest);
            int srcOff = 3;
            int destOff = 1;
            int len = 5;
            byte oldVal = dest[0];
            BYTE_ARRAY_HELPER.arrayCopy(src, srcOff, dest, destOff, len);
            assertEquals(oldVal, dest[0]);
            for(int i = 0; i < len ; i++ ) {
                assertEquals(src[i + srcOff], dest[i + destOff]);
            }

        } catch (IllegalArgumentException e) {
            fail();
        }
    }

    @Test
    public void arrayCopyTest2() {
        try {
            byte[] src = new byte[100];
            random.nextBytes(src);
            byte[] dest = new byte[100];
            random.nextBytes(dest);
            int srcOff = 0;
            int destOff = 0;
            int len = 100;
            BYTE_ARRAY_HELPER.arrayCopy(src, srcOff, dest, destOff, len);
            for(int i = 0; i < len ; i++ ) {
                assertEquals(src[i + srcOff], dest[i + destOff]);
            }
        } catch (IllegalArgumentException e) {
            fail();
        }
    }

    @Test
    public void arrayCopyTest3() {
        try {
            byte[] src = new byte[5];
            random.nextBytes(src);
            byte[] dest = new byte[5];
            random.nextBytes(dest);
            int srcOff = 0;
            int destOff = 0;
            int len = 4;
            byte oldVal = dest[dest.length - 1];
            BYTE_ARRAY_HELPER.arrayCopy(src, srcOff, dest, destOff, len);
            assertEquals(oldVal, dest[dest.length - 1]);
            for(int i = 0; i < len ; i++ ) {
                assertEquals(src[i + srcOff], dest[i + destOff]);
            }
        } catch (IllegalArgumentException e) {
            fail();
        }
    }

    @Test
    public void bEqualsIncorrectInputTest() {
        try {
            BYTE_ARRAY_HELPER.bEquals(null, new byte[0]);
            fail();
        }
        catch (IllegalArgumentException e) {
            assertEquals(e.getMessage(), ERROR_MSG_FIRST_ARRAY_IS_NULL);
        }

        try {
            BYTE_ARRAY_HELPER.bEquals(new byte[0], null);
            fail();
        }
        catch (IllegalArgumentException e) {
            assertEquals(e.getMessage(), ERROR_MSG_SECOND_ARRAY_IS_NULL);
        }
    }


    @Test
    public void bEqualsTest() {
        try {
            assertTrue(BYTE_ARRAY_HELPER.bEquals(new byte[0], new byte[0]));
            assertFalse(BYTE_ARRAY_HELPER.bEquals(new byte[2], new byte[3]));

            byte[] first = new byte[5];
            random.nextBytes(first);
            byte[] second = new byte[5];
            random.nextBytes(second);
            assertFalse(BYTE_ARRAY_HELPER.bEquals(first, second));

            BYTE_ARRAY_HELPER.arrayCopy(first, 0, second, 0, 5);
            assertTrue(BYTE_ARRAY_HELPER.bEquals(first, second));

        } catch (IllegalArgumentException e) {
            fail();
        }
    }

    @Test
    public void testEquals() {
        assertTrue(BYTE_ARRAY_HELPER.bEquals(new byte[]{0x42, (byte) 0xAD}, new byte[]{0x42, (byte) 0xAD}));
    }

    @Test
    public void testNotEquals() {
        assertFalse(BYTE_ARRAY_HELPER.bEquals(new byte[]{0x42, (byte) 0xAD}, new byte[]{0x42}));
        assertFalse(BYTE_ARRAY_HELPER.bEquals(new byte[]{}, new byte[]{0x42}));
        assertFalse(BYTE_ARRAY_HELPER.bEquals(new byte[]{0x42, (byte) 0xAD}, new byte[]{0x42}));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEqualsException3() {
        BYTE_ARRAY_HELPER.bEquals(null, null);
    }

    @Test
    public void bytesForByteTest() {
        for (byte b = Byte.MIN_VALUE; b < Byte.MAX_VALUE; b++){
            byte[] bytes = BYTE_ARRAY_HELPER.bytes(b);
            assertEquals(bytes.length, 1);
            assertEquals(bytes[0], b);
        }
    }

    @Test
    public void bytesForIntTest() {
        for (int b = 0x00; b <= 0xFF; b++){
            byte[] bytes = BYTE_ARRAY_HELPER.bytes(b);
            assertEquals(bytes.length, 1);
            assertEquals(bytes[0], (byte) b);
        }
    }

    @Test
    public void bytesForIntTest2() {
        assertArrayEquals(new byte[]{(byte) 0x01, (byte) 0x00}, BYTE_ARRAY_HELPER.bytes(0x100));
        assertArrayEquals(new byte[]{(byte) 0x7F, (byte) 0xFF}, BYTE_ARRAY_HELPER.bytes(0x7FFF));
        assertArrayEquals(new byte[]{(byte) 0x80, (byte) 0x00}, BYTE_ARRAY_HELPER.bytes(0x8000));
        assertArrayEquals(new byte[]{(byte) 0xAF, (byte) 0xCD}, BYTE_ARRAY_HELPER.bytes(0xAFCD));
        assertArrayEquals(new byte[]{(byte) 0xFF, (byte) 0xFF}, BYTE_ARRAY_HELPER.bytes(0xFFFF));
        assertArrayEquals(new byte[]{(byte) 0x02, (byte) 0x00, (byte) 0x00}, BYTE_ARRAY_HELPER.bytes(0x20000));
        assertArrayEquals(new byte[]{(byte) 0x7F, (byte) 0xFF, (byte) 0xFF}, BYTE_ARRAY_HELPER.bytes(0x7fffff));
        assertArrayEquals(new byte[]{(byte) 0x80, (byte) 0x00, (byte) 0x00}, BYTE_ARRAY_HELPER.bytes(0x800000));
        assertArrayEquals(new byte[]{(byte) 0xFA, (byte) 0xCE, (byte) 0x8D}, BYTE_ARRAY_HELPER.bytes(0xFACE8D));
        assertArrayEquals(new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF}, BYTE_ARRAY_HELPER.bytes(0xffffff));
        assertArrayEquals(new byte[]{(byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00}, BYTE_ARRAY_HELPER.bytes(0x1000000));
        assertArrayEquals(new byte[]{(byte) 0x0F, (byte) 0x00, (byte) 0x00, (byte) 0x00}, BYTE_ARRAY_HELPER.bytes(0xf000000));
        assertArrayEquals(new byte[]{(byte) 0x10, (byte) 0x00, (byte) 0x00, (byte) 0x00}, BYTE_ARRAY_HELPER.bytes(0x10000000));
        assertArrayEquals(new byte[]{(byte) 0x7F, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF}, BYTE_ARRAY_HELPER.bytes(0x7FFFFFFF));
        assertArrayEquals(new byte[]{(byte) 0x80, (byte) 0x00, (byte) 0x00, (byte) 0x00}, BYTE_ARRAY_HELPER.bytes(0x80000000));
        assertArrayEquals(new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF}, BYTE_ARRAY_HELPER.bytes(0xffffffff));
    }


    @Test
    public void bytesForStringIncorrectInputTest() {
        try {
            String s= null;
            BYTE_ARRAY_HELPER.bytes(s);
            fail();
        }
        catch (IllegalArgumentException e) {
            assertEquals(e.getMessage(), ERROR_MSG_STRING_IS_NULL);
        }

        String[] s = new String[]{"", "123", "01234", "12as"};
        for(int i = 0; i < s.length; i++) {
            try {
                BYTE_ARRAY_HELPER.bytes(s[i]);
                fail();
            }
            catch (IllegalArgumentException e) {
                assertEquals(e.getMessage(), ERROR_MSG_STRING_IS_NOT_CORRECT_HEX + s[i]);
            }
        }
    }

    @Test
    public void bytesForStringTest() {
        assertArrayEquals(new byte[]{(byte) 0xFA, (byte) 0xCE, (byte) 0x8D}, BYTE_ARRAY_HELPER.bytes("FACE8D"));
        assertArrayEquals(new byte[]{(byte) 0xFA, (byte) 0xCE, (byte) 0x8D}, BYTE_ARRAY_HELPER.bytes("FacE8D"));
        assertArrayEquals(new byte[]{(byte) 0xFF, (byte) 0xAA, (byte) 0xCC}, BYTE_ARRAY_HELPER.bytes("FFAACC"));
        assertArrayEquals(new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01}, BYTE_ARRAY_HELPER.bytes("0000000001"));
        assertArrayEquals(new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff}, BYTE_ARRAY_HELPER.bytes("ffffffffffffff"));
        assertArrayEquals(new byte[]{(byte) 0xBE}, BYTE_ARRAY_HELPER.bytes(0xBE));
    }

    @Test
    public void testHex() {
       assertEquals("42AD", BYTE_ARRAY_HELPER.hex(new byte[]{0x42, (byte) 0xAD}));
       assertEquals("", BYTE_ARRAY_HELPER.hex(new byte[0]));
       assertEquals("0A01", BYTE_ARRAY_HELPER.hex(new byte[]{0x0A, (byte) 0x01}));
       assertEquals("FFFFFF", BYTE_ARRAY_HELPER.hex(new byte[]{(byte)0xff, (byte) 0xff, (byte) 0xff}));
       assertEquals("00000000", BYTE_ARRAY_HELPER.hex(new byte[]{0x00, 0x00, 0x00, 0x00}));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testHexEx1() {
        BYTE_ARRAY_HELPER.hex(null);
    }

    @Test
    public void testHexForInt() {
        assertEquals("9000", BYTE_ARRAY_HELPER.hex((short)0x9000));
        assertEquals("00", BYTE_ARRAY_HELPER.hex(0x00));
        assertEquals("0F", BYTE_ARRAY_HELPER.hex(0x0F));
        assertEquals("FF", BYTE_ARRAY_HELPER.hex(0xFF));
        assertEquals("0100", BYTE_ARRAY_HELPER.hex(0x100));
        assertEquals("6543", BYTE_ARRAY_HELPER.hex(0x6543));
        assertEquals("FFFF", BYTE_ARRAY_HELPER.hex(0xffff));
        assertEquals("9000", BYTE_ARRAY_HELPER.hex(0x9000));
        assertEquals("010000", BYTE_ARRAY_HELPER.hex(0x10000));
        assertEquals("FFFFFE", BYTE_ARRAY_HELPER.hex(0xfffffe));
        assertEquals("FFFFFF", BYTE_ARRAY_HELPER.hex(0xfffffF));
        assertEquals("01000000", BYTE_ARRAY_HELPER.hex(0x1000000));
        assertEquals("7FFFFFFF", BYTE_ARRAY_HELPER.hex(Integer.MAX_VALUE));
        assertEquals("80000000", BYTE_ARRAY_HELPER.hex(Integer.MIN_VALUE));
        assertEquals("FFFFFFFF", BYTE_ARRAY_HELPER.hex(-1));
    }

    @Test
    public void testHexForByte() {
        assertEquals("00", BYTE_ARRAY_HELPER.hex((byte) 0x00));
        assertEquals("0F", BYTE_ARRAY_HELPER.hex((byte) 0x0F));
        assertEquals("C3", BYTE_ARRAY_HELPER.hex((byte) 0xc3));
        assertEquals("FF", BYTE_ARRAY_HELPER.hex((byte) -1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBCopyIncorrectInput() {
        BYTE_ARRAY_HELPER.bCopy(null);
    }

    @Test
    public void testBCopy() {
        byte[] arr = BYTE_ARRAY_HELPER.bCopy(new byte[0]);
        assertEquals(arr.length, 0);
        byte[] src = new byte[100];
        random.nextBytes(src);
        arr = BYTE_ARRAY_HELPER.bCopy(src);
        assertArrayEquals(src, arr);
    }

    @Test
    public void testBConcatIncorrectInput() {
        try {
            BYTE_ARRAY_HELPER.bConcat(null);
            fail();
        }
        catch (IllegalArgumentException e) {
            assertEquals(e.getMessage(), ERROR_MSG_ARRAYS_ARE_NULL);
        }

        try {
            BYTE_ARRAY_HELPER.bConcat(null, null, null);
            fail();
        }
        catch (IllegalArgumentException e) {
            assertEquals(e.getMessage(), ERROR_MSG_ARRAYS_ARE_NULL);
        }

    }

    @Test
    public void testBConcat() {
        assertArrayEquals(new byte[]{0x02}, BYTE_ARRAY_HELPER.bConcat(new byte[]{0x02}));
        assertArrayEquals(new byte[]{0x0A, 0x01, 0x01, 0x0F, (byte) 0xFF, 0x02, 0x05, 0x0F, (byte) 0xC1},
                BYTE_ARRAY_HELPER.bConcat(new byte[]{0x0A}, new byte[]{0x01, 0x01, 0x0F, (byte) 0xFF}, new byte[]{0x02, 0x05, 0x0F, (byte) 0xC1}));
        assertArrayEquals(new byte[]{0x02, 0x0A, 0x0C}, BYTE_ARRAY_HELPER.bConcat(new byte[0], new byte[]{0x02, 0x0A}, new byte[]{0x0C}));
        assertArrayEquals(new byte[]{0x0E}, BYTE_ARRAY_HELPER.bConcat(new byte[0], null, new byte[]{0x0E}));
        assertArrayEquals(new byte[0], BYTE_ARRAY_HELPER.bConcat(new byte[0], null));
        assertArrayEquals(new byte[0], BYTE_ARRAY_HELPER.bConcat(new byte[0], null, new byte[0], new byte[0]));
        assertArrayEquals(new byte[]{0x0E, 0x02, 0x0A, 0x0C}, BYTE_ARRAY_HELPER.bConcat(new byte[0], null, new byte[]{0x0E}, new byte[]{0x02, 0x0A, 0x0C}, null));
    }

    @Test
    public void testBSubIncorrectInput() {
        try {
            BYTE_ARRAY_HELPER.bSub(null, 0, 1);
            fail();
        }
        catch (IllegalArgumentException e) {
            assertEquals(e.getMessage(), ERROR_MSG_SOURCE_ARRAY_IS_NULL);
        }

        try {
            BYTE_ARRAY_HELPER.bSub(new byte[6], -1, 0);
            fail();
        }
        catch (IllegalArgumentException e) {
            assertEquals(e.getMessage(), ERROR_MSG_FROM_IS_LESS_THEN_ZERO);
        }

        try {
            BYTE_ARRAY_HELPER.bSub(new byte[6], 0, -1);
            fail();
        }
        catch (IllegalArgumentException e) {
            assertEquals(e.getMessage(), ERROR_MSG_COUNT_IS_LESS_THEN_ZERO);
        }

        try {
            BYTE_ARRAY_HELPER.bSub(new byte[6], 6, 1);
            fail();
        }
        catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains(ERROR_MSG_OFFSET_IS_OUT_OF_BOUND));
        }

        try {
            BYTE_ARRAY_HELPER.bSub(new byte[6], 5, 2);
            fail();
        }
        catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains(ERROR_MSG_END_INDEX_OUT_OF_BOUND));
        }
    }

    @Test
    public void testSub() {
        assertArrayEquals(new byte[]{0x02, 0x03}, BYTE_ARRAY_HELPER.bSub(new byte[]{0x01, 0x02, 0x03, 0x04}, 1, 2));
        assertArrayEquals(new byte[]{0x04}, BYTE_ARRAY_HELPER.bSub(new byte[]{0x01, 0x02, 0x03, 0x04}, 3, 1));
        assertArrayEquals(new byte[]{0x1}, BYTE_ARRAY_HELPER.bSub(new byte[]{0x01, 0x02, 0x03, 0x04}, 0, 1));
        assertArrayEquals(new byte[]{},BYTE_ARRAY_HELPER.bSub(new byte[]{0x01, 0x02, 0x03, 0x04}, 1, 0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSubEx1() {
        BYTE_ARRAY_HELPER.bSub(null, 1, 2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSubEx2() {
        BYTE_ARRAY_HELPER.bSub(new byte[]{}, -1, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSubEx3() {
        BYTE_ARRAY_HELPER.bSub(new byte[]{0x01}, 0, 2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSubEx4() {
        BYTE_ARRAY_HELPER.bSub(new byte[]{0x01}, 0, -1);
    }

    @Test
    public void testBLeft() {
        assertArrayEquals(new byte[]{0x42}, BYTE_ARRAY_HELPER.bLeft(new byte[]{0x42, 0x13}, 1));
        assertArrayEquals(new byte[0], BYTE_ARRAY_HELPER.bLeft(new byte[]{0x42, 0x13}, 0));
        assertArrayEquals(new byte[]{0x42, 0x13, 0x12}, BYTE_ARRAY_HELPER.bLeft(new byte[]{0x42, 0x13, 0x12, 0x55, 0x77}, 3));
    }

    @Test
    public void testBLeftIncorrectInput() {
        try {
            BYTE_ARRAY_HELPER.bLeft(null, 1);
            fail();
        }
        catch (IllegalArgumentException e) {
            assertEquals(e.getMessage(), ERROR_MSG_SOURCE_ARRAY_IS_NULL);
        }
        try {
            BYTE_ARRAY_HELPER.bLeft(new byte[]{0x42}, -1);
            fail();
        }
        catch (IllegalArgumentException e) {
            assertEquals(e.getMessage(), ERROR_MSG_COUNT_IS_LESS_THEN_ZERO);
        }
        try {
            BYTE_ARRAY_HELPER.bLeft(new byte[]{0x42}, 2);
            fail();
        }
        catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains(ERROR_MSG_COUNT_IS_TOO_BIG));
        }
    }


    @Test
    public void testRight() {
        assertArrayEquals(new byte[0], BYTE_ARRAY_HELPER.bRight(new byte[]{0x42, 0x13}, 0));
        assertArrayEquals(new byte[]{0x13}, BYTE_ARRAY_HELPER.bRight(new byte[]{0x42, 0x13}, 1));
        assertArrayEquals(new byte[]{0x42, 0x13}, BYTE_ARRAY_HELPER.bRight(new byte[]{0x42, 0x13}, 2));
    }

    @Test
    public void testBRightIncorrectInput() {
        try {
            BYTE_ARRAY_HELPER.bRight(null, 1);
            fail();
        }
        catch (IllegalArgumentException e) {
            assertEquals(e.getMessage(), ERROR_MSG_SOURCE_ARRAY_IS_NULL);
        }
        try {
            BYTE_ARRAY_HELPER.bRight(new byte[]{0x42}, -1);
            fail();
        }
        catch (IllegalArgumentException e) {
            assertEquals(e.getMessage(), ERROR_MSG_COUNT_IS_LESS_THEN_ZERO);
        }
        try {
            BYTE_ARRAY_HELPER.bRight(new byte[]{0x42}, 2);
            fail();
        }
        catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains(ERROR_MSG_COUNT_IS_TOO_BIG));
        }
    }

}