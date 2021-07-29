package com.tonnfccard.nfc;

import android.content.Context;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.tech.IsoDep;
import android.os.Build;

import androidx.test.core.app.ApplicationProvider;

import com.tonnfccard.helpers.ResponsesConstants;
import com.tonnfccard.smartcard.CAPDU;
import com.tonnfccard.smartcard.RAPDU;
import com.tonnfccard.utils.ByteArrayUtil;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.internal.DoNotInstrument;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static com.tonnfccard.TonWalletConstants.BLOCKED_STATE;
import static com.tonnfccard.TonWalletConstants.INSTALLED_STATE;
import static com.tonnfccard.TonWalletConstants.PERSONALIZED_STATE;
import static com.tonnfccard.nfc.NfcApduRunner.TIME_OUT;
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

@RunWith(RobolectricTestRunner.class)
@Config(sdk = Build.VERSION_CODES.P)
@DoNotInstrument
public class NfcApduRunnerTest {

    private NfcApduRunner nfcApduRunner;

    private final NfcApduRunnerFunction<CAPDU> disconnect = capdu -> {nfcApduRunner.disconnectCard(); return new RAPDU("9000");}; // just a fake RAPDU for unification with other functions
    private final NfcApduRunnerFunction<CAPDU> connect = capdu -> {nfcApduRunner.connect(); return new RAPDU("9000");}; // just a fake RAPDU for unification with other functions
    private final NfcApduRunnerFunction<CAPDU> transmitCommand = capdu -> {return nfcApduRunner.transmitCommand(capdu);};
    private final NfcApduRunnerFunction<CAPDU> sendAPDU = capdu -> {return nfcApduRunner.sendAPDU(capdu);};
    private final NfcApduRunnerFunction<CAPDU> sendTonWalletAppletAPDU = capdu -> {return nfcApduRunner.sendTonWalletAppletAPDU(capdu);};
    private final NfcApduRunnerFunction<CAPDU> sendCoinManagerAppletAPDU = capdu -> {return nfcApduRunner.sendCoinManagerAppletAPDU(capdu);};

    private final List<NfcApduRunnerFunction<CAPDU>> nfcRunnerFunctions = Arrays.asList(disconnect, connect, transmitCommand, sendAPDU, sendTonWalletAppletAPDU, sendCoinManagerAppletAPDU);
    private final List<NfcApduRunnerFunction<CAPDU>> nfcRunnerFunctionsShort = Arrays.asList(connect, transmitCommand, sendAPDU, sendTonWalletAppletAPDU, sendCoinManagerAppletAPDU);
    private final List<NfcApduRunnerFunction<CAPDU>> nfcRunnerFunctionsShort2 = Arrays.asList(transmitCommand, sendAPDU, sendTonWalletAppletAPDU, sendCoinManagerAppletAPDU);
    private final List<NfcApduRunnerFunction<CAPDU>> nfcRunnerFunctionsShort3 = Arrays.asList(sendAPDU, sendTonWalletAppletAPDU, sendCoinManagerAppletAPDU);


    @Before
    public  void init() throws Exception {
        Context context = ApplicationProvider.getApplicationContext();
        nfcApduRunner = NfcApduRunner.getInstance(context);
        nfcApduRunner.setNumberOfRetries(1);
        nfcApduRunner.setRetryTimeOut(10);
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

    /** Test setCardTag **/

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

    /** Test disconnect close error **/

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

    /** Test null tag **/

    @Test
    public void testNoTag() {
        IsoDep isoDep = null;
        NfcAdapter nfcAdapterMock = mock(NfcAdapter.class);
        when(nfcAdapterMock.isEnabled())
                .thenReturn(true);
        nfcApduRunner.setCardTag(isoDep);
        nfcApduRunner.setNfcAdapter(nfcAdapterMock);
        for(NfcApduRunnerFunction<CAPDU> nfcFunc : nfcRunnerFunctions) {
            try {
                nfcFunc.accept(SELECT_TON_WALLET_APPLET_APDU);
                fail();
            } catch (Exception e) {
                System.out.println(e.getMessage());
                assertEquals(e.getMessage(), ResponsesConstants.ERROR_MSG_NO_TAG);
            }
        }
    }

    /** Test no NFC **/

    @Test
    public void testNoNfc() {
        for(NfcApduRunnerFunction<CAPDU> nfcFunc : nfcRunnerFunctionsShort) {
            try (MockedStatic<NfcAdapter> nfcAdapterMockedStatic = Mockito.mockStatic(NfcAdapter.class)) {
                IsoDep isoDep = mock(IsoDep.class);
                nfcAdapterMockedStatic
                        .when(() -> NfcAdapter.getDefaultAdapter(any()))
                        .thenReturn(null);
                nfcApduRunner.setNfcAdapter(null);
                nfcApduRunner.setCardTag(isoDep);
                nfcFunc.accept(SELECT_TON_WALLET_APPLET_APDU);
                fail();
            } catch (Exception e) {
                System.out.println(e.getMessage());
                assertEquals(e.getMessage(), ResponsesConstants.ERROR_MSG_NO_NFC_HARDWARE);
            }
        }
    }

    /** Test NFC disabled **/

    @Test
    public void testNfcDisabled()  {
        IsoDep isoDep = mock(IsoDep.class);
        NfcAdapter nfcAdapterMock = mock(NfcAdapter.class);
        when(nfcAdapterMock.isEnabled())
                .thenReturn(false);
        nfcApduRunner.setCardTag(isoDep);
        nfcApduRunner.setNfcAdapter(nfcAdapterMock);
        for(NfcApduRunnerFunction<CAPDU> nfcFunc : nfcRunnerFunctionsShort) {
            try {
               nfcFunc.accept(GET_SAULT_APDU);
                fail();
            } catch (Exception e) {
                System.out.println(e.getMessage());
                assertTrue(e.getMessage().contains(ResponsesConstants.ERROR_MSG_NFC_DISABLED));
            }
        }
    }

    /** Test NFC connect error **/

    @Test
    public void testNfcConnectError() throws Exception {
        IsoDep isoDep = mock(IsoDep.class);
        Mockito.doThrow(new IOException()).when(isoDep).connect();
        NfcAdapter nfcAdapterMock = mock(NfcAdapter.class);
        when(nfcAdapterMock.isEnabled())
                .thenReturn(true);
        nfcApduRunner.setCardTag(isoDep);
        nfcApduRunner.setNfcAdapter(nfcAdapterMock);
        for(NfcApduRunnerFunction<CAPDU> nfcFunc : nfcRunnerFunctionsShort) {
            try {
                nfcFunc.accept(GET_SAULT_APDU);
                fail();
            } catch (Exception e) {
                assertTrue(e.getMessage().contains(ResponsesConstants.ERROR_MSG_NFC_CONNECT));
            }
        }
    }

    /** Test null command **/

    @Test
    public void testNullApdu()  {
        IsoDep tag = mock(IsoDep.class);
        nfcApduRunner.setCardTag(tag);
        for(NfcApduRunnerFunction<CAPDU> nfcFunc : nfcRunnerFunctionsShort2) {
            try {
                nfcFunc.accept(null);
                fail();
            } catch (Exception e) {
                assertEquals(e.getMessage(), ResponsesConstants.ERROR_MSG_APDU_EMPTY);
            }
        }
    }

    /** Test transceive error **/

    @Test
    public void testTransceiveError() throws IOException {
        NfcAdapter nfcAdapterMock = mock(NfcAdapter.class);
        when(nfcAdapterMock.isEnabled())
                .thenReturn(true);
        nfcApduRunner.setNfcAdapter(nfcAdapterMock);
        IsoDep tag = mock(IsoDep.class);
        when(tag.isConnected()).thenReturn(false);
        Mockito.doNothing().when(tag).connect();
        Mockito.doNothing().when(tag).setTimeout(TIME_OUT);
        Mockito.doThrow(new IOException()).when(tag).transceive(any());
        nfcApduRunner.setCardTag(tag);
        for(NfcApduRunnerFunction<CAPDU> nfcFunc : nfcRunnerFunctionsShort2) {
            try {
                nfcFunc.accept(SELECT_TON_WALLET_APPLET_APDU);
                fail();
            } catch (Exception e) {
                System.out.println(e.getMessage());
                assertTrue(e.getMessage().contains(ResponsesConstants.ERROR_TRANSCEIVE));
            }
        }
    }

    /** Test too short response  **/

    @Test
    public void testTooShortResponseError() throws IOException {
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
            for(NfcApduRunnerFunction<CAPDU> nfcFunc : nfcRunnerFunctionsShort2) {
                try {
                    nfcFunc.accept(SELECT_TON_WALLET_APPLET_APDU);
                    fail();
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    assertEquals(e.getMessage(), ResponsesConstants.ERROR_BAD_RESPONSE);
                }
            }
        }
    }

    /** Test successfull transmit/send APDU **/

    @Test
    public void transmitAndSendTest() throws Exception {
        NfcAdapter nfcAdapterMock = mock(NfcAdapter.class);
        when(nfcAdapterMock.isEnabled())
                .thenReturn(true);
        nfcApduRunner.setNfcAdapter(nfcAdapterMock);
        for(NfcApduRunnerFunction<CAPDU> nfcFunc : nfcRunnerFunctionsShort2) {
            IsoDep tag = mock(IsoDep.class);
            when(tag.isConnected()).thenReturn(false);
            Mockito.doNothing().when(tag).connect();
            Mockito.doNothing().when(tag).setTimeout(TIME_OUT);
            when(tag.transceive(SELECT_COIN_MANAGER_APDU.getBytes())).thenReturn(new byte[]{(byte) 0x90, (byte) 0x00});
            when(tag.transceive(SELECT_TON_WALLET_APPLET_APDU.getBytes())).thenReturn(new byte[]{(byte) 0x90, (byte) 0x00});
            when(tag.transceive(GET_APP_INFO_APDU.getBytes())).thenReturn(new byte[]{PERSONALIZED_STATE, (byte) 0x90, (byte) 0x00});

            nfcApduRunner.setCardTag(tag);

            RAPDU rapdu = nfcFunc.accept(SELECT_TON_WALLET_APPLET_APDU);
            assertEquals(rapdu.getBytes().length, 2);
            assertEquals(rapdu.getData().length, 0);
            assertEquals(rapdu.getSW1(), (byte) 0x90);
            assertEquals(rapdu.getSW2(), (byte) 0x00);


            rapdu = nfcFunc.accept(GET_APP_INFO_APDU);
            assertEquals(rapdu.getBytes().length, 3);
            assertEquals(rapdu.getData().length, 1);
            assertEquals(rapdu.getData()[0], (byte) 0x17);
            assertEquals(rapdu.getSW1(), (byte) 0x90);
            assertEquals(rapdu.getSW2(), (byte) 0x00);

            when(tag.transceive(GET_RECOVERY_DATA_HASH_APDU.getBytes())).thenReturn(ByteArrayUtil.getInstance().bConcat(ByteArrayUtil.getInstance().bytes("aaaa1111aaaa11112222333322223333aaaa1111aaaa11112222333322223333"),
                    new byte[]{(byte) 0x90, (byte) 0x00}));
            nfcApduRunner.setCardTag(tag);
            rapdu = nfcFunc.accept(GET_RECOVERY_DATA_HASH_APDU);
            assertEquals(rapdu.getBytes().length, 34);
            assertEquals(rapdu.getData().length, 32);
            assertArrayEquals(rapdu.getData(), ByteArrayUtil.getInstance().bytes("aaaa1111aaaa11112222333322223333aaaa1111aaaa11112222333322223333"));
            assertEquals(rapdu.getSW1(), (byte) 0x90);
            assertEquals(rapdu.getSW2(), (byte) 0x00);
        }
    }

    /**Test bad SW**/

    @Test
    public void testForBadSw() throws Exception {
        NfcAdapter nfcAdapterMock = mock(NfcAdapter.class);
        when(nfcAdapterMock.isEnabled())
                .thenReturn(true);
        nfcApduRunner.setNfcAdapter(nfcAdapterMock);
        IsoDep tag = mock(IsoDep.class);
        when(tag.isConnected()).thenReturn(false);
        Mockito.doNothing().when(tag).connect();
        Mockito.doNothing().when(tag).setTimeout(TIME_OUT);
        when(tag.transceive(SELECT_COIN_MANAGER_APDU.getBytes())).thenReturn(new byte[]{(byte) 0x90, (byte) 0x00});
        when(tag.transceive(SELECT_TON_WALLET_APPLET_APDU.getBytes())).thenReturn(new byte[]{(byte) 0x90, (byte) 0x00});
        when(tag.transceive(GET_APP_INFO_APDU.getBytes())).thenReturn(new byte[]{PERSONALIZED_STATE, (byte) 0x90, (byte) 0x00});
        when(tag.transceive(GET_SAULT_APDU.getBytes())).thenReturn(new byte[]{(byte)0x6F, (byte)0x00}); // not 9000 sw
        nfcApduRunner.setCardTag(tag);
        for(NfcApduRunnerFunction<CAPDU> nfcFunc : nfcRunnerFunctionsShort3) {
            try {
                nfcFunc.accept(GET_SAULT_APDU);
                fail();
            } catch (Exception e) {
                System.out.println(e.getMessage());
                assertTrue(e.getMessage().contains("6F00"));
            }
        }
    }


    /**Test bad states**/

    @Test
    public void sendTonWalletAppletAPDUTestStates() throws Exception {
        NfcAdapter nfcAdapterMock = mock(NfcAdapter.class);
        when(nfcAdapterMock.isEnabled())
                .thenReturn(true);
        NfcApduRunner nfcApduRunner =  mock(NfcApduRunner.class);

        when(nfcApduRunner.sendAPDUList(GET_APPLET_STATE_APDU_LIST)).thenReturn(new RAPDU(new  byte[]{PERSONALIZED_STATE, (byte)0x90, (byte)0x00}));
        when(nfcApduRunner.sendTonWalletAppletAPDU(GET_HASH_OF_ENCRYPTED_COMMON_SECRET_APDU)).thenCallRealMethod();
        try {
            RAPDU rapdu = nfcApduRunner.sendTonWalletAppletAPDU(GET_HASH_OF_ENCRYPTED_COMMON_SECRET_APDU);
            fail();

        }
        catch (Exception e) {
            e.printStackTrace();
            assertTrue(e.getMessage().contains(ResponsesConstants.ERROR_MSG_APDU_NOT_SUPPORTED));
        }

        when(nfcApduRunner.sendAPDUList(GET_APPLET_STATE_APDU_LIST)).thenReturn(new RAPDU(new  byte[]{BLOCKED_STATE, (byte)0x90, (byte)0x00}));
        when(nfcApduRunner.sendTonWalletAppletAPDU(GET_PUB_KEY_WITH_DEFAULT_PATH_APDU)).thenCallRealMethod();
        try {
            RAPDU rapdu = nfcApduRunner.sendTonWalletAppletAPDU(GET_PUB_KEY_WITH_DEFAULT_PATH_APDU);
            fail();

        }
        catch (Exception e) {
            e.printStackTrace();
            assertTrue(e.getMessage().contains(ResponsesConstants.ERROR_MSG_APDU_NOT_SUPPORTED));
        }

        when(nfcApduRunner.sendAPDUList(GET_APPLET_STATE_APDU_LIST)).thenReturn(new RAPDU(new  byte[]{INSTALLED_STATE, (byte)0x90, (byte)0x00}));
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

