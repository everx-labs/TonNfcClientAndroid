package com.tonnfccard.nfc;

import android.content.Context;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.os.Build;
import android.os.Bundle;

import androidx.test.core.app.ApplicationProvider;

import com.tonnfccard.helpers.ResponsesConstants;
import com.tonnfccard.smartcard.RAPDU;
import com.tonnfccard.utils.ByteArrayUtil;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.IOException;
import java.lang.reflect.Method;

import static com.tonnfccard.nfc.NfcApduRunner.TIME_OUT;
import static com.tonnfccard.smartcard.CoinManagerApduCommands.GET_ROOT_KEY_STATUS_APDU;
import static com.tonnfccard.smartcard.CoinManagerApduCommands.SELECT_COIN_MANAGER_APDU;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.GET_APPLET_STATE_APDU_LIST;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.GET_APP_INFO_APDU;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.GET_HASH_OF_ENCRYPTED_COMMON_SECRET_APDU;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.GET_HASH_OF_ENCRYPTED_PASSWORD_APDU;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.GET_PUB_KEY_WITH_DEFAULT_PATH_APDU;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.GET_RECOVERY_DATA_HASH_APDU;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.GET_SAULT_APDU;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.SELECT_TON_WALLET_APPLET_APDU;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


//TODO: It is good to add teests that would recreate Intent and IsoDep
//Class tagClass = Tag.class;
// Method createMockTagMethod = tagClass.getMethod("createMockTag", byte[].class, int[].class, Bundle[].class);
// see https://stackoverflow.com/questions/30841803/how-to-mock-a-android-nfc-tag-object-for-unit-testing
// TODO: For methods sendAPDU, sendTonWalletAppletAPDU, sendCoinManagerAppletAPDU there are a bunches of identical tests. Try to use lambdas to reduce the code

@RunWith(RobolectricTestRunner.class)
@Config(sdk = Build.VERSION_CODES.P)
public class NfcApduRunnerTest {

    private Context context;
    private NfcApduRunner nfcApduRunner;

    @Before
    public  void init() throws Exception {
        context = ApplicationProvider.getApplicationContext();
        nfcApduRunner = NfcApduRunner.getInstance(context);
    }

    @Test
    public void getInstanceTestNullContext() {
        try {
            NfcApduRunner.getInstance(null);
            fail();
        }
        catch (Exception e){
            assertEquals(e.getMessage(), ResponsesConstants.ERROR_MSG_NO_CONTEXT);
        }
    }

    /**Test setCardTag**/
    @Test
    public void setCardTagTestNullIntent()  {
        try {
            Intent intent = null;
            nfcApduRunner.setCardTag(intent);
            fail();
        }
        catch (Exception e){
            assertEquals(e.getMessage(), ResponsesConstants.ERROR_MSG_INTENT_EMPTY);
        }
    }

    @Test
    public void setCardTagTest() throws Exception {
        Intent intent = new Intent("android.nfc.action.ACTION_TECH_DISCOVERED");
        assertFalse(nfcApduRunner.setCardTag(intent));
    }

    /**Test connect**/

    @Test
    public void connectTestNoNfc() throws Exception {
        try (MockedStatic<NfcAdapter> nfcAdapterMockedStatic = Mockito.mockStatic(NfcAdapter.class)) {
            nfcAdapterMockedStatic
                    .when(() -> NfcAdapter.getDefaultAdapter(any()))
                    .thenReturn(null);
            nfcApduRunner.setNfcAdapter(null);
            nfcApduRunner.connect();
            fail();
        }
        catch (Exception e){
            assertEquals(e.getMessage(), ResponsesConstants.ERROR_MSG_NO_NFC);
        }
    }

    @Test
    public void connectTestNfcDisabled()  {
        try {
            NfcAdapter nfcAdapterMock = mock(NfcAdapter.class);
            when(nfcAdapterMock.isEnabled())
                    .thenReturn(false);
            nfcApduRunner.setNfcAdapter(nfcAdapterMock);
            nfcApduRunner.connect();
            fail();
        }
        catch (Exception e){
            assertEquals(e.getMessage(), ResponsesConstants.ERROR_MSG_NFC_DISABLED);
        }
    }

    @Test
    public void connectTestNoTag() throws Exception{
        IsoDep isoDep = null;
        NfcAdapter nfcAdapterMock = mock(NfcAdapter.class);
        when(nfcAdapterMock.isEnabled())
                .thenReturn(true);
        nfcApduRunner.setCardTag(isoDep);
        nfcApduRunner.setNfcAdapter(nfcAdapterMock);
        try {
            nfcApduRunner.connect();
            fail();
        }
        catch (Exception e){
            assertEquals(e.getMessage(), ResponsesConstants.ERROR_MSG_NO_TAG);
        }
    }

    @Test
    public void connectTestTagConnectError() throws Exception {
        IsoDep isoDep = mock(IsoDep.class);
        Mockito.doThrow(new IOException()).when(isoDep).connect();
        NfcAdapter nfcAdapterMock = mock(NfcAdapter.class);
        when(nfcAdapterMock.isEnabled())
                .thenReturn(true);
        nfcApduRunner.setCardTag(isoDep);
        nfcApduRunner.setNfcAdapter(nfcAdapterMock);
        try {
            nfcApduRunner.transmitCommand(GET_SAULT_APDU);
            fail();
        }
        catch (Exception e){
            assertTrue(e.getMessage().contains(ResponsesConstants.ERROR_MSG_NFC_CONNECT));
        }
    }


    /**Test disconnect**/

    @Test
    public void disconnectTestNoTag() {
        IsoDep isoDep = null;
        nfcApduRunner.setCardTag(isoDep);
        try {
            nfcApduRunner.disconnectCard();
            fail();
        }
        catch (Exception e){
            //System.out.println(e.getMessage());
            assertEquals(e.getMessage(), ResponsesConstants.ERROR_MSG_NO_TAG);
        }
    }

    @Test
    public void disconnectTest()  {
        try {
            IsoDep tag = mock(IsoDep.class);
            Mockito.doThrow(new IOException()).when(tag).close();
            nfcApduRunner.setCardTag(tag);
            nfcApduRunner.disconnectCard();
            fail();
        }
        catch (Exception e){
            assertTrue(e.getMessage().contains(ResponsesConstants.ERROR_MSG_NFC_DISCONNECT));
        }
    }

    /**Test transmitCommand**/

    @Test
    public void transmitCommandTestNull()  {
        IsoDep tag = mock(IsoDep.class);
        nfcApduRunner.setCardTag(tag);
        try {
            nfcApduRunner.transmitCommand(null);
            fail();
        }
        catch (Exception e){
            assertEquals(e.getMessage(), ResponsesConstants.ERROR_MSG_APDU_EMPTY);
        }
    }

    @Test
    public void transmitCommandNoTag() {
        IsoDep isoDep = null;
        nfcApduRunner.setCardTag(isoDep);
        try {
            nfcApduRunner.transmitCommand(GET_SAULT_APDU);
            fail();
        }
        catch (Exception e){
            assertEquals(e.getMessage(), ResponsesConstants.ERROR_MSG_NO_TAG);
        }
    }

    @Test
    public void transmitCommandTransceiveError() throws Exception {
        IsoDep isoDep = mock(IsoDep.class);
        when(isoDep.isConnected()).thenReturn(false);
        Mockito.doNothing().when(isoDep).connect();
        Mockito.doNothing().when(isoDep).setTimeout(TIME_OUT);
        Mockito.doThrow(new IOException()).when(isoDep).transceive(any());
        NfcAdapter nfcAdapterMock = mock(NfcAdapter.class);
        when(nfcAdapterMock.isEnabled())
                .thenReturn(true);
        nfcApduRunner.setCardTag(isoDep);
        nfcApduRunner.setNfcAdapter(nfcAdapterMock);
        try {
            nfcApduRunner.transmitCommand(GET_SAULT_APDU);
            fail();
        }
        catch (Exception e){
            assertTrue(e.getMessage().contains(ResponsesConstants.ERROR_TRANSCEIVE));
        }
    }

    @Test
    public void transmitCommandNoNfc() throws Exception {
        try (MockedStatic<NfcAdapter> nfcAdapterMockedStatic = Mockito.mockStatic(NfcAdapter.class)) {
            IsoDep isoDep = mock(IsoDep.class);
            nfcAdapterMockedStatic
                    .when(() -> NfcAdapter.getDefaultAdapter(any()))
                    .thenReturn(null);
            nfcApduRunner.setNfcAdapter(null);
            nfcApduRunner.setCardTag(isoDep);
            nfcApduRunner.transmitCommand(SELECT_TON_WALLET_APPLET_APDU);
            fail();
        }
        catch (Exception e){
            assertEquals(e.getMessage(), ResponsesConstants.ERROR_MSG_NO_NFC);
        }
    }

    @Test
    public void transmitCommandNfcDisabled() throws Exception {
        IsoDep isoDep = mock(IsoDep.class);
        NfcAdapter nfcAdapterMock = mock(NfcAdapter.class);
        when(nfcAdapterMock.isEnabled())
                .thenReturn(false);
        nfcApduRunner.setCardTag(isoDep);
        nfcApduRunner.setNfcAdapter(nfcAdapterMock);
        try {
            nfcApduRunner.transmitCommand(GET_SAULT_APDU);
            fail();
        }
        catch (Exception e){
            assertTrue(e.getMessage().contains(ResponsesConstants.ERROR_MSG_NFC_DISABLED));
        }
    }

    @Test
    public void transmitCommandConnectError() throws Exception {
        IsoDep isoDep = mock(IsoDep.class);
        Mockito.doThrow(new IOException()).when(isoDep).connect();
        NfcAdapter nfcAdapterMock = mock(NfcAdapter.class);
        when(nfcAdapterMock.isEnabled())
                .thenReturn(true);
        nfcApduRunner.setCardTag(isoDep);
        nfcApduRunner.setNfcAdapter(nfcAdapterMock);
        try {
            nfcApduRunner.transmitCommand(GET_SAULT_APDU);
            fail();
        }
        catch (Exception e){
            assertTrue(e.getMessage().contains(ResponsesConstants.ERROR_MSG_NFC_CONNECT));
        }
    }

    @Test
    public void transmitCommandTestTooShortResponseError() throws IOException {
        NfcAdapter nfcAdapterMock = mock(NfcAdapter.class);
        when(nfcAdapterMock.isEnabled())
                .thenReturn(true);
        nfcApduRunner.setNfcAdapter(nfcAdapterMock);
        for (int i = 0; i < 3 ; i++) {
            IsoDep tag = mock(IsoDep.class);
            when(tag.isConnected()).thenReturn(false);
            Mockito.doNothing().when(tag).connect();
            Mockito.doNothing().when(tag).setTimeout(TIME_OUT);

            if (i <  2) {
                when(tag.transceive(SELECT_TON_WALLET_APPLET_APDU.getBytes())).thenReturn(new byte[i]);
            }
            else {
                when(tag.transceive(SELECT_TON_WALLET_APPLET_APDU.getBytes())).thenReturn(null);
            }
            nfcApduRunner.setCardTag(tag);
            try {
                nfcApduRunner.transmitCommand(SELECT_TON_WALLET_APPLET_APDU);
                fail();
            } catch (Exception e) {
                assertEquals(e.getMessage(), ResponsesConstants.ERROR_BAD_RESPONSE);
            }
        }
    }

    @Test
    public void transmitCommandTest() throws Exception {
        NfcAdapter nfcAdapterMock = mock(NfcAdapter.class);
        when(nfcAdapterMock.isEnabled())
                .thenReturn(true);
        nfcApduRunner.setNfcAdapter(nfcAdapterMock);

        IsoDep tag = mock(IsoDep.class);
        when(tag.isConnected()).thenReturn(false);
        Mockito.doNothing().when(tag).connect();
        Mockito.doNothing().when(tag).setTimeout(TIME_OUT);
        when(tag.transceive(SELECT_TON_WALLET_APPLET_APDU.getBytes())).thenReturn(new byte[]{(byte)0x90, (byte)0x00});
        nfcApduRunner.setCardTag(tag);

        RAPDU rapdu = nfcApduRunner.transmitCommand(SELECT_TON_WALLET_APPLET_APDU);
        assertEquals(rapdu.getBytes().length, 2);
        assertEquals(rapdu.getData().length, 0);
        assertEquals(rapdu.getSW1(), (byte)0x90);
        assertEquals(rapdu.getSW2(), (byte)0x00);


        when(tag.transceive(GET_APP_INFO_APDU.getBytes())).thenReturn(new byte[]{(byte)0x17, (byte)0x90, (byte)0x00});
        nfcApduRunner.setCardTag(tag);

        rapdu = nfcApduRunner.transmitCommand(GET_APP_INFO_APDU);
        assertEquals(rapdu.getBytes().length, 3);
        assertEquals(rapdu.getData().length, 1);
        assertEquals(rapdu.getData()[0], (byte)0x17);
        assertEquals(rapdu.getSW1(), (byte)0x90);
        assertEquals(rapdu.getSW2(), (byte)0x00);

        when(tag.transceive(GET_RECOVERY_DATA_HASH_APDU.getBytes())).thenReturn(ByteArrayUtil.getInstance().bConcat(ByteArrayUtil.getInstance().bytes("aaaa1111aaaa11112222333322223333aaaa1111aaaa11112222333322223333"),
                new byte[]{(byte)0x6F, (byte)0x00}));
        nfcApduRunner.setCardTag(tag);
        rapdu = nfcApduRunner.transmitCommand(GET_RECOVERY_DATA_HASH_APDU);
        assertEquals(rapdu.getBytes().length, 34);
        assertEquals(rapdu.getData().length, 32);
        assertArrayEquals(rapdu.getData(), ByteArrayUtil.getInstance().bytes("aaaa1111aaaa11112222333322223333aaaa1111aaaa11112222333322223333"));
        assertEquals(rapdu.getSW1(), (byte)0x6F);
        assertEquals(rapdu.getSW2(), (byte)0x00);
    }

    /**Test sendAPDU**/

    @Test
    public void sendAPDUTestNull() {
        try {
            nfcApduRunner.sendAPDU(null);
            fail();
        }
        catch (Exception e){
            assertEquals(e.getMessage(), ResponsesConstants.ERROR_MSG_APDU_EMPTY);
        }
    }

    @Test
    public void sendAPDUTestNoNfc() {
        try (MockedStatic<NfcAdapter> nfcAdapterMockedStatic = Mockito.mockStatic(NfcAdapter.class)) {
            nfcAdapterMockedStatic
                    .when(() -> NfcAdapter.getDefaultAdapter(any()))
                    .thenReturn(null);
            IsoDep tag = mock(IsoDep.class);
            nfcApduRunner.setCardTag(tag);
            nfcApduRunner.setNfcAdapter(null);
            nfcApduRunner.sendAPDU(SELECT_TON_WALLET_APPLET_APDU);
            fail();
        }
        catch (Exception e){
            assertEquals(e.getMessage(), ResponsesConstants.ERROR_MSG_NO_NFC);
        }
    }

    @Test
    public void sendAPDUTestNfcDisabled() {
        IsoDep isoDep = mock(IsoDep.class);
        NfcAdapter nfcAdapterMock = mock(NfcAdapter.class);
        when(nfcAdapterMock.isEnabled())
                .thenReturn(false);
        nfcApduRunner.setCardTag(isoDep);
        nfcApduRunner.setNfcAdapter(nfcAdapterMock);
        try {
            nfcApduRunner.sendAPDU(SELECT_TON_WALLET_APPLET_APDU);
            fail();
        }
        catch (Exception e){
            assertEquals(e.getMessage(), ResponsesConstants.ERROR_MSG_NFC_DISABLED);
        }
    }

    @Test
    public void sendAPDUTestNoTag() {
        IsoDep tag = null;
        nfcApduRunner.setCardTag(tag);
        try {
            nfcApduRunner.sendAPDU(SELECT_TON_WALLET_APPLET_APDU);
            fail();
        }
        catch (Exception e){
            assertEquals(e.getMessage(), ResponsesConstants.ERROR_MSG_NO_TAG);
        }
    }

    @Test
    public void sendAPDUTestTagConnectError() throws IOException {
        NfcAdapter nfcAdapterMock = mock(NfcAdapter.class);
        when(nfcAdapterMock.isEnabled())
                .thenReturn(true);
        nfcApduRunner.setNfcAdapter(nfcAdapterMock);
        IsoDep tag = mock(IsoDep.class);
        when(tag.isConnected()).thenReturn(false);
        Mockito.doThrow(new IOException()).when(tag).connect();
        nfcApduRunner.setCardTag(tag);
        try {
            nfcApduRunner.sendAPDU(SELECT_TON_WALLET_APPLET_APDU);
            fail();
        }
        catch (Exception e){
            assertEquals(e.getMessage(), ResponsesConstants.ERROR_MSG_NFC_CONNECT);
        }
    }

    @Test
    public void sendAPDUTestTagTransceiveError() throws IOException {
        NfcAdapter nfcAdapterMock = mock(NfcAdapter.class);
        when(nfcAdapterMock.isEnabled())
                .thenReturn(true);
        nfcApduRunner.setNfcAdapter(nfcAdapterMock);
        IsoDep tag = mock(IsoDep.class);
        when(tag.isConnected()).thenReturn(false);
        Mockito.doNothing().when(tag).connect();
        Mockito.doNothing().when(tag).setTimeout(TIME_OUT);
        Mockito.doThrow(new IOException()).when(tag).transceive(SELECT_TON_WALLET_APPLET_APDU.getBytes());
        nfcApduRunner.setCardTag(tag);
        try {
            nfcApduRunner.sendAPDU(SELECT_TON_WALLET_APPLET_APDU);
            fail();
        }
        catch (Exception e) {
            assertTrue(e.getMessage().contains(ResponsesConstants.ERROR_TRANSCEIVE));
        }
    }

    @Test
    public void sendAPDUTestTooShortResponseError() throws IOException {
        NfcAdapter nfcAdapterMock = mock(NfcAdapter.class);
        when(nfcAdapterMock.isEnabled())
                .thenReturn(true);
        nfcApduRunner.setNfcAdapter(nfcAdapterMock);
        for (int i = 0; i < 3 ; i++) {
            IsoDep tag = mock(IsoDep.class);
            when(tag.isConnected()).thenReturn(false);
            Mockito.doNothing().when(tag).connect();
            Mockito.doNothing().when(tag).setTimeout(TIME_OUT);
            if (i <  2) {
                when(tag.transceive(SELECT_TON_WALLET_APPLET_APDU.getBytes())).thenReturn(new byte[i]);
            }
            else {
                when(tag.transceive(SELECT_TON_WALLET_APPLET_APDU.getBytes())).thenReturn(null);
            }
            nfcApduRunner.setCardTag(tag);
            try {
                nfcApduRunner.sendAPDU(SELECT_TON_WALLET_APPLET_APDU);
                fail();
            } catch (Exception e) {
                assertEquals(e.getMessage(), ResponsesConstants.ERROR_BAD_RESPONSE);
            }
        }
    }

    @Test
    public void sendAPDUTest() throws Exception {
        NfcAdapter nfcAdapterMock = mock(NfcAdapter.class);
        when(nfcAdapterMock.isEnabled())
                .thenReturn(true);
        nfcApduRunner.setNfcAdapter(nfcAdapterMock);

        IsoDep tag = mock(IsoDep.class);
        when(tag.isConnected()).thenReturn(false);
        Mockito.doNothing().when(tag).connect();
        Mockito.doNothing().when(tag).setTimeout(TIME_OUT);
        when(tag.transceive(SELECT_TON_WALLET_APPLET_APDU.getBytes())).thenReturn(new byte[]{(byte)0x90, (byte)0x00});
        nfcApduRunner.setCardTag(tag);

        RAPDU rapdu = nfcApduRunner.sendAPDU(SELECT_TON_WALLET_APPLET_APDU);
        assertEquals(rapdu.getBytes().length, 2);
        assertEquals(rapdu.getData().length, 0);
        assertEquals(rapdu.getSW1(), (byte)0x90);
        assertEquals(rapdu.getSW2(), (byte)0x00);


        when(tag.transceive(GET_APP_INFO_APDU.getBytes())).thenReturn(new byte[]{(byte)0x17, (byte)0x90, (byte)0x00});
        nfcApduRunner.setCardTag(tag);

        rapdu = nfcApduRunner.sendAPDU(GET_APP_INFO_APDU);
        assertEquals(rapdu.getBytes().length, 3);
        assertEquals(rapdu.getData().length, 1);
        assertEquals(rapdu.getData()[0], (byte)0x17);
        assertEquals(rapdu.getSW1(), (byte)0x90);
        assertEquals(rapdu.getSW2(), (byte)0x00);

        when(tag.transceive(GET_RECOVERY_DATA_HASH_APDU.getBytes())).thenReturn(ByteArrayUtil.getInstance().bConcat(ByteArrayUtil.getInstance().bytes("aaaa1111aaaa11112222333322223333aaaa1111aaaa11112222333322223333aaaa1111aaaa11112222333322223333aaaa1111aaaa11112222333322223333"),
                new byte[]{(byte)0x90, (byte)0x00}));
        nfcApduRunner.setCardTag(tag);

        rapdu = nfcApduRunner.sendAPDU(GET_RECOVERY_DATA_HASH_APDU);
        assertEquals(rapdu.getBytes().length, 66);
        assertEquals(rapdu.getData().length, 64);
        assertArrayEquals(rapdu.getData(), ByteArrayUtil.getInstance().bytes("aaaa1111aaaa11112222333322223333aaaa1111aaaa11112222333322223333aaaa1111aaaa11112222333322223333aaaa1111aaaa11112222333322223333"));
        assertEquals(rapdu.getSW1(), (byte)0x90);
        assertEquals(rapdu.getSW2(), (byte)0x00);
    }

    @Test
    public void sendAPDUTestForBadSw() throws Exception {
        NfcAdapter nfcAdapterMock = mock(NfcAdapter.class);
        when(nfcAdapterMock.isEnabled())
                .thenReturn(true);
        nfcApduRunner.setNfcAdapter(nfcAdapterMock);

        IsoDep tag = mock(IsoDep.class);
        when(tag.isConnected()).thenReturn(false);
        Mockito.doNothing().when(tag).connect();
        Mockito.doNothing().when(tag).setTimeout(TIME_OUT);
        when(tag.transceive(SELECT_TON_WALLET_APPLET_APDU.getBytes())).thenReturn(new byte[]{(byte)0x6F, (byte)0x00}); // not 9000 sw
        nfcApduRunner.setCardTag(tag);
        try {
            RAPDU rapdu = nfcApduRunner.sendAPDU(SELECT_TON_WALLET_APPLET_APDU);
            fail();
        }
        catch (Exception e) {
            assertTrue(e.getMessage().contains("6F00"));
        }
    }

    /**Test sendCoinManagerAppletAPDU**/

    @Test
    public void sendCoinManagerAppletAPDUTestNull() {
        try {
            nfcApduRunner.sendCoinManagerAppletAPDU(null);
            fail();
        }
        catch (Exception e){
            assertEquals(e.getMessage(), ResponsesConstants.ERROR_MSG_APDU_EMPTY);
        }
    }

    @Test
    public void sendCoinManagerAppletAPDUTestNoNfc() {
        try (MockedStatic<NfcAdapter> nfcAdapterMockedStatic = Mockito.mockStatic(NfcAdapter.class)) {
            nfcAdapterMockedStatic
                    .when(() -> NfcAdapter.getDefaultAdapter(any()))
                    .thenReturn(null);
            IsoDep tag = mock(IsoDep.class);
            nfcApduRunner.setCardTag(tag);
            nfcApduRunner.setNfcAdapter(null);
            nfcApduRunner.sendCoinManagerAppletAPDU(SELECT_COIN_MANAGER_APDU);
            fail();
        }
        catch (Exception e){
            assertEquals(e.getMessage(), ResponsesConstants.ERROR_MSG_NO_NFC);
        }
    }

    @Test
    public void sendCoinManagerAppletAPDUTestNfcDisabled() {
        IsoDep isoDep = mock(IsoDep.class);
        NfcAdapter nfcAdapterMock = mock(NfcAdapter.class);
        when(nfcAdapterMock.isEnabled())
                .thenReturn(false);
        nfcApduRunner.setCardTag(isoDep);
        nfcApduRunner.setNfcAdapter(nfcAdapterMock);
        try {
            nfcApduRunner.sendCoinManagerAppletAPDU(SELECT_COIN_MANAGER_APDU);
            fail();
        }
        catch (Exception e){
            assertEquals(e.getMessage(), ResponsesConstants.ERROR_MSG_NFC_DISABLED);
        }
    }

    @Test
    public void sendCoinManagerAppletAPDUTestNoTag() {
        IsoDep tag = null;
        nfcApduRunner.setCardTag(tag);
        try {
            nfcApduRunner.sendCoinManagerAppletAPDU(SELECT_COIN_MANAGER_APDU);
            fail();
        }
        catch (Exception e){
            assertEquals(e.getMessage(), ResponsesConstants.ERROR_MSG_NO_TAG);
        }
    }

    @Test
    public void sendCoinManagerAppletAPDUTestTagConnectError() throws IOException {
        NfcAdapter nfcAdapterMock = mock(NfcAdapter.class);
        when(nfcAdapterMock.isEnabled())
                .thenReturn(true);
        nfcApduRunner.setNfcAdapter(nfcAdapterMock);
        IsoDep tag = mock(IsoDep.class);
        when(tag.isConnected()).thenReturn(false);
        Mockito.doThrow(new IOException()).when(tag).connect();
        nfcApduRunner.setCardTag(tag);
        try {
            nfcApduRunner.sendCoinManagerAppletAPDU(SELECT_COIN_MANAGER_APDU);
            fail();
        }
        catch (Exception e){
            assertEquals(e.getMessage(), ResponsesConstants.ERROR_MSG_NFC_CONNECT);
        }
    }

    @Test
    public void sendCoinManagerAppletAPDUTestTagTransceiveError() throws IOException {
        NfcAdapter nfcAdapterMock = mock(NfcAdapter.class);
        when(nfcAdapterMock.isEnabled())
                .thenReturn(true);
        nfcApduRunner.setNfcAdapter(nfcAdapterMock);
        IsoDep tag = mock(IsoDep.class);
        when(tag.isConnected()).thenReturn(false);
        Mockito.doNothing().when(tag).connect();
        Mockito.doNothing().when(tag).setTimeout(TIME_OUT);
        Mockito.doThrow(new IOException()).when(tag).transceive(SELECT_COIN_MANAGER_APDU.getBytes());
        nfcApduRunner.setCardTag(tag);
        try {
            nfcApduRunner.sendCoinManagerAppletAPDU(SELECT_COIN_MANAGER_APDU);
            fail();
        }
        catch (Exception e) {
            assertTrue(e.getMessage().contains(ResponsesConstants.ERROR_TRANSCEIVE));
        }
    }

    @Test
    public void sendCoinManagerAppletAPDUTestTooShortResponseError() throws IOException {
        NfcAdapter nfcAdapterMock = mock(NfcAdapter.class);
        when(nfcAdapterMock.isEnabled())
                .thenReturn(true);
        nfcApduRunner.setNfcAdapter(nfcAdapterMock);
        for (int i = 0; i < 3 ; i++) {
            IsoDep tag = mock(IsoDep.class);
            when(tag.isConnected()).thenReturn(false);
            Mockito.doNothing().when(tag).connect();
            Mockito.doNothing().when(tag).setTimeout(TIME_OUT);
            if (i <  2) {
                when(tag.transceive(SELECT_TON_WALLET_APPLET_APDU.getBytes())).thenReturn(new byte[i]);
            }
            else {
                when(tag.transceive(SELECT_TON_WALLET_APPLET_APDU.getBytes())).thenReturn(null);
            }
            nfcApduRunner.setCardTag(tag);
            try {
                nfcApduRunner.sendCoinManagerAppletAPDU(SELECT_COIN_MANAGER_APDU);
                fail();
            } catch (Exception e) {
                assertEquals(e.getMessage(), ResponsesConstants.ERROR_BAD_RESPONSE);
            }
        }
    }

    @Test
    public void sendCoinManagerAppletAPDUTest() throws Exception {
        NfcAdapter nfcAdapterMock = mock(NfcAdapter.class);
        when(nfcAdapterMock.isEnabled())
                .thenReturn(true);
        nfcApduRunner.setNfcAdapter(nfcAdapterMock);

        IsoDep tag = mock(IsoDep.class);
        when(tag.isConnected()).thenReturn(false);
        Mockito.doNothing().when(tag).connect();
        Mockito.doNothing().when(tag).setTimeout(TIME_OUT);
        when(tag.transceive(SELECT_COIN_MANAGER_APDU.getBytes())).thenReturn(new byte[]{(byte)0x90, (byte)0x00});
        nfcApduRunner.setCardTag(tag);

        RAPDU rapdu = nfcApduRunner.sendCoinManagerAppletAPDU(SELECT_COIN_MANAGER_APDU);
        assertEquals(rapdu.getBytes().length, 2);
        assertEquals(rapdu.getData().length, 0);
        assertEquals(rapdu.getSW1(), (byte)0x90);
        assertEquals(rapdu.getSW2(), (byte)0x00);


        when(tag.transceive(GET_ROOT_KEY_STATUS_APDU.getBytes())).thenReturn(new byte[]{(byte)0x5A, (byte)0x90, (byte)0x00});
        nfcApduRunner.setCardTag(tag);

        rapdu = nfcApduRunner.sendCoinManagerAppletAPDU(GET_ROOT_KEY_STATUS_APDU);
        assertEquals(rapdu.getBytes().length, 3);
        assertEquals(rapdu.getData().length, 1);
        assertEquals(rapdu.getData()[0], (byte)0x5A);
        assertEquals(rapdu.getSW1(), (byte)0x90);
        assertEquals(rapdu.getSW2(), (byte)0x00);
    }

    @Test
    public void sendCoinManagerAppletAPDUTestForBadSw() throws Exception {
        NfcAdapter nfcAdapterMock = mock(NfcAdapter.class);
        when(nfcAdapterMock.isEnabled())
                .thenReturn(true);
        nfcApduRunner.setNfcAdapter(nfcAdapterMock);

        IsoDep tag = mock(IsoDep.class);
        when(tag.isConnected()).thenReturn(false);
        Mockito.doNothing().when(tag).connect();
        Mockito.doNothing().when(tag).setTimeout(TIME_OUT);
        when(tag.transceive(SELECT_COIN_MANAGER_APDU.getBytes())).thenReturn(new byte[]{(byte)0x6D, (byte)0x00}); // not 9000 sw
        nfcApduRunner.setCardTag(tag);
        try {
            RAPDU rapdu = nfcApduRunner.sendCoinManagerAppletAPDU(SELECT_TON_WALLET_APPLET_APDU);
            fail();
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
            assertTrue(e.getMessage().contains("6D00"));
        }
    }

    /**Test sendTonWalletAppletAPDU**/

    @Test
    public void sendTonWalletAppletAPDUTestNull() {
        try {
            nfcApduRunner.sendTonWalletAppletAPDU(null);
            fail();
        }
        catch (Exception e){
            assertEquals(e.getMessage(), ResponsesConstants.ERROR_MSG_APDU_EMPTY);
        }
    }

    @Test
    public void sendTonWalletAppletAPDUTestNoNfc() {
        try (MockedStatic<NfcAdapter> nfcAdapterMockedStatic = Mockito.mockStatic(NfcAdapter.class)) {
            nfcAdapterMockedStatic
                    .when(() -> NfcAdapter.getDefaultAdapter(any()))
                    .thenReturn(null);
            IsoDep tag = mock(IsoDep.class);
            nfcApduRunner.setCardTag(tag);
            nfcApduRunner.setNfcAdapter(null);
            nfcApduRunner.sendTonWalletAppletAPDU(SELECT_TON_WALLET_APPLET_APDU);
            fail();
        }
        catch (Exception e){
            assertEquals(e.getMessage(), ResponsesConstants.ERROR_MSG_NO_NFC);
        }
    }

    @Test
    public void sendTonWalletAppletAPDUTestNfcDisabled() {
        IsoDep isoDep = mock(IsoDep.class);
        NfcAdapter nfcAdapterMock = mock(NfcAdapter.class);
        when(nfcAdapterMock.isEnabled())
                .thenReturn(false);
        nfcApduRunner.setCardTag(isoDep);
        nfcApduRunner.setNfcAdapter(nfcAdapterMock);
        try {
            nfcApduRunner.sendTonWalletAppletAPDU(SELECT_TON_WALLET_APPLET_APDU);
            fail();
        }
        catch (Exception e){
            assertEquals(e.getMessage(), ResponsesConstants.ERROR_MSG_NFC_DISABLED);
        }
    }

    @Test
    public void sendTonWalletAppletAPDUTestNoTag() {
        IsoDep tag = null;
        nfcApduRunner.setCardTag(tag);
        try {
            nfcApduRunner.sendTonWalletAppletAPDU(SELECT_TON_WALLET_APPLET_APDU);
            fail();
        }
        catch (Exception e){
            assertEquals(e.getMessage(), ResponsesConstants.ERROR_MSG_NO_TAG);
        }
    }

    @Test
    public void sendTonWalletAppletAPDUTestTagConnectError() throws IOException {
        NfcAdapter nfcAdapterMock = mock(NfcAdapter.class);
        when(nfcAdapterMock.isEnabled())
                .thenReturn(true);
        nfcApduRunner.setNfcAdapter(nfcAdapterMock);
        IsoDep tag = mock(IsoDep.class);
        when(tag.isConnected()).thenReturn(false);
        Mockito.doThrow(new IOException()).when(tag).connect();
        nfcApduRunner.setCardTag(tag);
        try {
            nfcApduRunner.sendTonWalletAppletAPDU(SELECT_TON_WALLET_APPLET_APDU);
            fail();
        }
        catch (Exception e){
            assertEquals(e.getMessage(), ResponsesConstants.ERROR_MSG_NFC_CONNECT);
        }
    }

    @Test
    public void sendTonWalletAppletAPDUTestTagTransceiveError() throws IOException {
        NfcAdapter nfcAdapterMock = mock(NfcAdapter.class);
        when(nfcAdapterMock.isEnabled())
                .thenReturn(true);
        nfcApduRunner.setNfcAdapter(nfcAdapterMock);
        IsoDep tag = mock(IsoDep.class);
        when(tag.isConnected()).thenReturn(false);
        Mockito.doNothing().when(tag).connect();
        Mockito.doNothing().when(tag).setTimeout(TIME_OUT);
        Mockito.doThrow(new IOException()).when(tag).transceive(SELECT_TON_WALLET_APPLET_APDU.getBytes());
        nfcApduRunner.setCardTag(tag);
        try {
            nfcApduRunner.sendTonWalletAppletAPDU(SELECT_TON_WALLET_APPLET_APDU);
            fail();
        }
        catch (Exception e) {
            assertTrue(e.getMessage().contains(ResponsesConstants.ERROR_TRANSCEIVE));
        }
    }

    @Test
    public void sendTonWalletAppletAPDUTestTooShortResponseError() throws IOException {
        NfcAdapter nfcAdapterMock = mock(NfcAdapter.class);
        when(nfcAdapterMock.isEnabled())
                .thenReturn(true);
        nfcApduRunner.setNfcAdapter(nfcAdapterMock);
        for (int i = 0; i < 3 ; i++) {
            IsoDep tag = mock(IsoDep.class);
            when(tag.isConnected()).thenReturn(false);
            Mockito.doNothing().when(tag).connect();
            Mockito.doNothing().when(tag).setTimeout(TIME_OUT);
            if (i <  2) {
                when(tag.transceive(SELECT_TON_WALLET_APPLET_APDU.getBytes())).thenReturn(new byte[i]);
            }
            else {
                when(tag.transceive(SELECT_TON_WALLET_APPLET_APDU.getBytes())).thenReturn(null);
            }
            nfcApduRunner.setCardTag(tag);
            try {
                nfcApduRunner.sendTonWalletAppletAPDU(SELECT_TON_WALLET_APPLET_APDU);
                fail();
            } catch (Exception e) {
                assertEquals(e.getMessage(), ResponsesConstants.ERROR_BAD_RESPONSE);
            }
        }
    }

    @Test
    public void sendTonWalletAppletAPDUTest() throws Exception {
        NfcAdapter nfcAdapterMock = mock(NfcAdapter.class);
        when(nfcAdapterMock.isEnabled())
                .thenReturn(true);
        nfcApduRunner.setNfcAdapter(nfcAdapterMock);

        IsoDep tag = mock(IsoDep.class);
        when(tag.isConnected()).thenReturn(false);
        Mockito.doNothing().when(tag).connect();
        Mockito.doNothing().when(tag).setTimeout(TIME_OUT);
        when(tag.transceive(SELECT_TON_WALLET_APPLET_APDU.getBytes())).thenReturn(new byte[]{(byte)0x90, (byte)0x00});
        nfcApduRunner.setCardTag(tag);

        RAPDU rapdu = nfcApduRunner.sendAPDU(SELECT_TON_WALLET_APPLET_APDU);
        assertEquals(rapdu.getBytes().length, 2);
        assertEquals(rapdu.getData().length, 0);
        assertEquals(rapdu.getSW1(), (byte)0x90);
        assertEquals(rapdu.getSW2(), (byte)0x00);


        when(tag.transceive(GET_APP_INFO_APDU.getBytes())).thenReturn(new byte[]{(byte)0x17, (byte)0x90, (byte)0x00});
        nfcApduRunner.setCardTag(tag);

        rapdu = nfcApduRunner.sendAPDU(GET_APP_INFO_APDU);
        assertEquals(rapdu.getBytes().length, 3);
        assertEquals(rapdu.getData().length, 1);
        assertEquals(rapdu.getData()[0], (byte)0x17);
        assertEquals(rapdu.getSW1(), (byte)0x90);
        assertEquals(rapdu.getSW2(), (byte)0x00);

        when(tag.transceive(GET_RECOVERY_DATA_HASH_APDU.getBytes())).thenReturn(ByteArrayUtil.getInstance().bConcat(ByteArrayUtil.getInstance().bytes("aaaa1111aaaa11112222333322223333aaaa1111aaaa11112222333322223333aaaa1111aaaa11112222333322223333aaaa1111aaaa11112222333322223333"),
                new byte[]{(byte)0x90, (byte)0x00}));
        nfcApduRunner.setCardTag(tag);

        rapdu = nfcApduRunner.sendTonWalletAppletAPDU(GET_RECOVERY_DATA_HASH_APDU);
        assertEquals(rapdu.getBytes().length, 66);
        assertEquals(rapdu.getData().length, 64);
        assertArrayEquals(rapdu.getData(), ByteArrayUtil.getInstance().bytes("aaaa1111aaaa11112222333322223333aaaa1111aaaa11112222333322223333aaaa1111aaaa11112222333322223333aaaa1111aaaa11112222333322223333"));
        assertEquals(rapdu.getSW1(), (byte)0x90);
        assertEquals(rapdu.getSW2(), (byte)0x00);
    }

    @Test
    public void sendTonWalletAppletAPDUTestForBadSw() throws Exception {
        NfcAdapter nfcAdapterMock = mock(NfcAdapter.class);
        when(nfcAdapterMock.isEnabled())
                .thenReturn(true);
        nfcApduRunner.setNfcAdapter(nfcAdapterMock);

        IsoDep tag = mock(IsoDep.class);
        when(tag.isConnected()).thenReturn(false);
        Mockito.doNothing().when(tag).connect();
        Mockito.doNothing().when(tag).setTimeout(TIME_OUT);
        when(tag.transceive(SELECT_TON_WALLET_APPLET_APDU.getBytes())).thenReturn(new byte[]{(byte)0x6F, (byte)0x00}); // not 9000 sw
        nfcApduRunner.setCardTag(tag);
        try {
            RAPDU rapdu = nfcApduRunner.sendTonWalletAppletAPDU(SELECT_TON_WALLET_APPLET_APDU);
            fail();
        }
        catch (Exception e) {
            e.printStackTrace();
            assertTrue(e.getMessage().contains("6F00"));
        }
    }

    @Test
    public void sendTonWalletAppletAPDUTestStates() throws Exception {
        NfcAdapter nfcAdapterMock = mock(NfcAdapter.class);
        when(nfcAdapterMock.isEnabled())
                .thenReturn(true);
        NfcApduRunner nfcApduRunner =  mock(NfcApduRunner.class);

        when(nfcApduRunner.sendAPDUList(GET_APPLET_STATE_APDU_LIST)).thenReturn(new RAPDU(new  byte[]{(byte) 0x17, (byte)0x90, (byte)0x00}));
        when(nfcApduRunner.sendTonWalletAppletAPDU(GET_HASH_OF_ENCRYPTED_COMMON_SECRET_APDU)).thenCallRealMethod();
        try {
            RAPDU rapdu = nfcApduRunner.sendTonWalletAppletAPDU(GET_HASH_OF_ENCRYPTED_COMMON_SECRET_APDU);
            fail();

        }
        catch (Exception e) {
            e.printStackTrace();
            assertTrue(e.getMessage().contains(ResponsesConstants.ERROR_MSG_APDU_NOT_SUPPORTED));
        }

        when(nfcApduRunner.sendAPDUList(GET_APPLET_STATE_APDU_LIST)).thenReturn(new RAPDU(new  byte[]{(byte) 0x47, (byte)0x90, (byte)0x00}));
        when(nfcApduRunner.sendTonWalletAppletAPDU(GET_PUB_KEY_WITH_DEFAULT_PATH_APDU)).thenCallRealMethod();
        try {
            RAPDU rapdu = nfcApduRunner.sendTonWalletAppletAPDU(GET_PUB_KEY_WITH_DEFAULT_PATH_APDU);
            fail();

        }
        catch (Exception e) {
            e.printStackTrace();
            assertTrue(e.getMessage().contains(ResponsesConstants.ERROR_MSG_APDU_NOT_SUPPORTED));
        }

        when(nfcApduRunner.sendAPDUList(GET_APPLET_STATE_APDU_LIST)).thenReturn(new RAPDU(new  byte[]{(byte) 0x07, (byte)0x90, (byte)0x00}));
        when(nfcApduRunner.sendTonWalletAppletAPDU(GET_HASH_OF_ENCRYPTED_PASSWORD_APDU)).thenCallRealMethod();
        try {
            RAPDU rapdu = nfcApduRunner.sendTonWalletAppletAPDU(GET_HASH_OF_ENCRYPTED_PASSWORD_APDU);
            fail();

        }
        catch (Exception e) {
            e.printStackTrace();
            assertTrue(e.getMessage().contains(ResponsesConstants.ERROR_MSG_APDU_NOT_SUPPORTED));
        }
    }
}

