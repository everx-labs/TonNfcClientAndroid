package com.tonnfccard.nfc;

import android.content.Context;
import android.nfc.tech.IsoDep;
import android.os.PowerManager;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.tonnfccard.helpers.ResponsesConstants;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.GET_SAULT_APDU;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class NfcApduRunnerTest {
    Context context  = ApplicationProvider.getApplicationContext();


    @Test
    public void testh()  {
        Context context  = ApplicationProvider.getApplicationContext();
    }

    @Test
    public void getInstanceTestNullAdapter() {
        try {
            NfcApduRunner.getInstance(null);
            fail();
        }
        catch (Exception e){
            assertEquals(e.getMessage(), ResponsesConstants.ERROR_MSG_NO_CONTEXT);
        }
    }

    @Test
    public void disconnectTestNoTag() throws Exception{
        NfcApduRunner nfcApduRunner = NfcApduRunner.getInstance(context);
        try {
            nfcApduRunner.disconnectCard();
            fail();
        }
        catch (Exception e){
            assertEquals(e.getMessage(), ResponsesConstants.ERROR_MSG_NO_TAG);
        }
    }

    @Test
    public void transmitCommandNoTag() throws Exception{
        NfcApduRunner nfcApduRunner = NfcApduRunner.getInstance(context);
        try {
            nfcApduRunner.transmitCommand(GET_SAULT_APDU);
            fail();
        }
        catch (Exception e){
            assertEquals(e.getMessage(), ResponsesConstants.ERROR_MSG_NO_TAG);
        }
    }

    @Test
    public void disconnectTest() throws Exception {
       /* NfcApduRunner nfcApduRunner = NfcApduRunner.getInstance(context);
        IsoDep tag = mock(IsoDep.class);
        Mockito.doThrow(new IOException()).when(tag).close();
        try {
            nfcApduRunner.disconnectCard();
            fail();
        }
        catch (Exception e){
            assertTrue(e.getMessage().contains(ResponsesConstants.ERROR_MSG_NFC_DISCONNECT));
        }*/
    }
}

  */
/*@Before
  public void setup() {
    PowerMockito.mockStatic(Log.class);
  }*//*


  @Test
  public void getInstanceTestNullAdapter() {
    try {
      ResponsesConstants.getInstance(null);
      fail();
    }
    catch (Exception e){
      assertEquals(e.getMessage(), ResponsesConstants.ERROR_MSG_NO_CONTEXT);
    }
  }

  @Test
  public void disconnectTestNoTag() {
    ResponsesConstants ResponsesConstants = ResponsesConstants.getInstance();
    IsoDep tag = null;
    ResponsesConstants.setCardTag(tag);
    try {
      ResponsesConstants.disconnectCard();
      fail();
    }
    catch (Exception e){
      assertEquals(e.getMessage(), ResponsesConstants.ERROR_MSG_NO_TAG);
    }
  }

  @Test
  public void disconnectTest() throws IOException {
    IsoDep tag = mock(IsoDep.class);

    Mockito.doThrow(new IOException()).when(tag).close();

    ResponsesConstants ResponsesConstants = ResponsesConstants.getInstance();
    ResponsesConstants.setCardTag(tag);
    try {
      ResponsesConstants.disconnectCard();
    }
    catch (Exception e){
     // e.printStackTrace();
      assertTrue(e.getMessage().contains(ResponsesConstants.ERROR_MSG_NFC_DISCONNECT));
      return;
    }
    fail();
  }

  @Test
  public void connectTestNoNfc() {
    mockStatic(NfcAdapter.class);
    when(NfcAdapter.getDefaultAdapter(any()))
      .thenReturn(null);
    ResponsesConstants ResponsesConstants = ResponsesConstants.getInstance();
    try {
      ResponsesConstants.connect();
      fail();
    }
    catch (Exception e){
      assertEquals(e.getMessage(), ResponsesConstants.ERROR_MSG_NO_NFC);
    }
  }

  @Test
  public void connectTestNfcDisabled() {
    NfcAdapter nfcAdapterMock = mock(NfcAdapter.class);
    when(nfcAdapterMock.isEnabled())
      .thenReturn(false);
    ResponsesConstants ResponsesConstants = ResponsesConstants.getInstance();
    ResponsesConstants.setNfcAdapter(nfcAdapterMock);
    try {
      ResponsesConstants.connect();
      fail();
    }
    catch (Exception e){
      assertEquals(e.getMessage(), ResponsesConstants.ERROR_MSG_NFC_DISABLED);
    }
  }

  @Test
  public void connectTestNoTag() {
    NfcAdapter nfcAdapterMock = mock(NfcAdapter.class);
    when(nfcAdapterMock.isEnabled())
      .thenReturn(true);
    ResponsesConstants ResponsesConstants = ResponsesConstants.getInstance();
    ResponsesConstants.setNfcAdapter(nfcAdapterMock);
    IsoDep tag = null;
    ResponsesConstants.setCardTag(tag);
    try {
      ResponsesConstants.connect();
      fail();
    }
    catch (Exception e){
      assertEquals(e.getMessage(), ResponsesConstants.ERROR_MSG_NO_TAG);
    }
  }

  @Test
  public void connectTestTagConnectError() throws IOException {
    NfcAdapter nfcAdapterMock = mock(NfcAdapter.class);
    when(nfcAdapterMock.isEnabled())
      .thenReturn(true);
    ResponsesConstants ResponsesConstants = ResponsesConstants.getInstance();
    ResponsesConstants.setNfcAdapter(nfcAdapterMock);

    IsoDep tag = mock(IsoDep.class);
    when(tag.isConnected()).thenReturn(false);
    Mockito.doThrow(new IOException()).when(tag).connect();
    ResponsesConstants.setCardTag(tag);
    try {
      ResponsesConstants.connect();
      fail();
    }
    catch (Exception e){
      assertEquals(e.getMessage(), ResponsesConstants.ERROR_MSG_NFC_CONNECT);
    }
  }

  @Test
  public void transmitCommandTestNull() {
    ResponsesConstants ResponsesConstants = ResponsesConstants.getInstance();
    try {
      ResponsesConstants.transmitCommand(null);
      fail();
    }
    catch (Exception e){
      assertEquals(e.getMessage(), ResponsesConstants.ERROR_APDU_EMPTY);
    }
  }

  @Test
  public void transmitCommandTestNoNfc() {
    mockStatic(NfcAdapter.class);
    when(NfcAdapter.getDefaultAdapter(any()))
      .thenReturn(null);
    ResponsesConstants ResponsesConstants = ResponsesConstants.getInstance();
    try {
      ResponsesConstants.transmitCommand(SELECT_TON_WALLET_APPLET_APDU);
      fail();
    }
    catch (Exception e){
      assertEquals(e.getMessage(), ResponsesConstants.ERROR_MSG_NO_NFC);
    }
  }

  @Test
  public void transmitCommandTestNfcDisabled() {
    NfcAdapter nfcAdapterMock = mock(NfcAdapter.class);
    when(nfcAdapterMock.isEnabled())
      .thenReturn(false);
    ResponsesConstants ResponsesConstants = ResponsesConstants.getInstance();
    ResponsesConstants.setNfcAdapter(nfcAdapterMock);
    try {
      ResponsesConstants.transmitCommand(SELECT_TON_WALLET_APPLET_APDU);
      fail();
    }
    catch (Exception e){
      assertEquals(e.getMessage(), ResponsesConstants.ERROR_MSG_NFC_DISABLED);
    }
  }

  @Test
  public void transmitCommandTestNoTag() {
    NfcAdapter nfcAdapterMock = mock(NfcAdapter.class);
    when(nfcAdapterMock.isEnabled())
      .thenReturn(true);
    ResponsesConstants ResponsesConstants = ResponsesConstants.getInstance();
    ResponsesConstants.setNfcAdapter(nfcAdapterMock);
    IsoDep tag = null;
    ResponsesConstants.setCardTag(tag);
    try {
      ResponsesConstants.transmitCommand(SELECT_TON_WALLET_APPLET_APDU);
      fail();
    }
    catch (Exception e){
      assertEquals(e.getMessage(), ResponsesConstants.ERROR_MSG_NO_TAG);
    }
  }

  @Test
  public void transmitCommandTestTagConnectError() throws IOException {
    NfcAdapter nfcAdapterMock = mock(NfcAdapter.class);
    when(nfcAdapterMock.isEnabled())
      .thenReturn(true);
    ResponsesConstants ResponsesConstants = ResponsesConstants.getInstance();
    ResponsesConstants.setNfcAdapter(nfcAdapterMock);

    IsoDep tag = mock(IsoDep.class);
    when(tag.isConnected()).thenReturn(false);
    Mockito.doThrow(new IOException()).when(tag).connect();
    ResponsesConstants.setCardTag(tag);
    try {
      ResponsesConstants.transmitCommand(SELECT_TON_WALLET_APPLET_APDU);
      fail();
    }
    catch (Exception e){
      assertEquals(e.getMessage(), ResponsesConstants.ERROR_MSG_NFC_CONNECT);
    }
  }

  @Test
  public void transmitCommandTestTagTransceiveError() throws IOException {
    NfcAdapter nfcAdapterMock = mock(NfcAdapter.class);
    when(nfcAdapterMock.isEnabled())
      .thenReturn(true);
    ResponsesConstants ResponsesConstants = ResponsesConstants.getInstance();
    ResponsesConstants.setNfcAdapter(nfcAdapterMock);

    IsoDep tag = mock(IsoDep.class);
    when(tag.isConnected()).thenReturn(false);
    Mockito.doNothing().when(tag).connect();
    Mockito.doNothing().when(tag).setTimeout(TIME_OUT);
    Mockito.doThrow(new IOException()).when(tag).transceive(SELECT_TON_WALLET_APPLET_APDU.getBytes());

    ResponsesConstants.setCardTag(tag);

    try {
      ResponsesConstants.transmitCommand(SELECT_TON_WALLET_APPLET_APDU);
      fail();
    }
    catch (Exception e) {
      assertTrue(e.getMessage().contains(ResponsesConstants.ERROR_TRANSCEIVE));
     // e.printStackTrace();
    }
  }

  @Test
  public void transmitCommandTestTooShortResponseError() throws IOException {
    NfcAdapter nfcAdapterMock = mock(NfcAdapter.class);
    when(nfcAdapterMock.isEnabled())
      .thenReturn(true);
    ResponsesConstants ResponsesConstants = ResponsesConstants.getInstance();
    ResponsesConstants.setNfcAdapter(nfcAdapterMock);

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

      ResponsesConstants.setCardTag(tag);

      try {
        ResponsesConstants.transmitCommand(SELECT_TON_WALLET_APPLET_APDU);
        fail();
      } catch (Exception e) {
        assertEquals(e.getMessage(), ResponsesConstants.ERROR_BAD_RESPONSE);
        e.printStackTrace();
      }
    }
  }

  @Test
  public void transmitCommandTest() throws Exception {
    NfcAdapter nfcAdapterMock = mock(NfcAdapter.class);
    when(nfcAdapterMock.isEnabled())
      .thenReturn(true);
    ResponsesConstants ResponsesConstants = ResponsesConstants.getInstance();
    ResponsesConstants.setNfcAdapter(nfcAdapterMock);

    IsoDep tag = mock(IsoDep.class);
    when(tag.isConnected()).thenReturn(false);
    Mockito.doNothing().when(tag).connect();
    Mockito.doNothing().when(tag).setTimeout(TIME_OUT);
    when(tag.transceive(SELECT_TON_WALLET_APPLET_APDU.getBytes())).thenReturn(new byte[]{(byte)0x90, (byte)0x00});
    ResponsesConstants.setCardTag(tag);

    RAPDU rapdu = ResponsesConstants.transmitCommand(SELECT_TON_WALLET_APPLET_APDU);
    assertEquals(rapdu.getBytes().length, 2);
    assertEquals(rapdu.getData().length, 0);
    assertEquals(rapdu.getSW1(), (byte)0x90);
    assertEquals(rapdu.getSW2(), (byte)0x00);


    when(tag.transceive(GET_APP_INFO_APDU.getBytes())).thenReturn(new byte[]{(byte)0x17, (byte)0x90, (byte)0x00});
    ResponsesConstants.setCardTag(tag);

    rapdu = ResponsesConstants.transmitCommand(GET_APP_INFO_APDU);
    assertEquals(rapdu.getBytes().length, 3);
    assertEquals(rapdu.getData().length, 1);
    assertEquals(rapdu.getData()[0], (byte)0x17);
    assertEquals(rapdu.getSW1(), (byte)0x90);
    assertEquals(rapdu.getSW2(), (byte)0x00);

    when(tag.transceive(GET_RECOVERY_DATA_HASH_APDU.getBytes())).thenReturn(ByteArrayHelper.bConcat(ByteArrayHelper.bytes("aaaa1111aaaa11112222333322223333aaaa1111aaaa11112222333322223333"),
      new byte[]{(byte)0x6F, (byte)0x00}));
    ResponsesConstants.setCardTag(tag);

    rapdu = ResponsesConstants.transmitCommand(GET_RECOVERY_DATA_HASH_APDU);
    assertEquals(rapdu.getBytes().length, 34);
    assertEquals(rapdu.getData().length, 32);
    assertArrayEquals(rapdu.getData(), ByteArrayHelper.bytes("aaaa1111aaaa11112222333322223333aaaa1111aaaa11112222333322223333"));
    assertEquals(rapdu.getSW1(), (byte)0x6F);
    assertEquals(rapdu.getSW2(), (byte)0x00);
  }

  @Test
  public void sendAPDUTestNull() {
    ResponsesConstants ResponsesConstants = ResponsesConstants.getInstance();
    try {
      ResponsesConstants.sendAPDU(null);
      fail();
    }
    catch (Exception e){
      assertEquals(e.getMessage(), ResponsesConstants.ERROR_APDU_EMPTY);
    }
  }

  @Test
  public void sendAPDUTestNoNfc() {
    mockStatic(NfcAdapter.class);
    when(NfcAdapter.getDefaultAdapter(any()))
      .thenReturn(null);
    ResponsesConstants ResponsesConstants = ResponsesConstants.getInstance();
    try {
      ResponsesConstants.sendAPDU(SELECT_TON_WALLET_APPLET_APDU);
      fail();
    }
    catch (Exception e){
      assertEquals(e.getMessage(), ResponsesConstants.ERROR_MSG_NO_NFC);
    }
  }

  @Test
  public void sendAPDUTestNfcDisabled() {
    NfcAdapter nfcAdapterMock = mock(NfcAdapter.class);
    when(nfcAdapterMock.isEnabled())
      .thenReturn(false);
    ResponsesConstants ResponsesConstants = ResponsesConstants.getInstance();
    ResponsesConstants.setNfcAdapter(nfcAdapterMock);
    try {
      ResponsesConstants.sendAPDU(SELECT_TON_WALLET_APPLET_APDU);
      fail();
    }
    catch (Exception e){
      assertEquals(e.getMessage(), ResponsesConstants.ERROR_MSG_NFC_DISABLED);
    }
  }

  @Test
  public void sendAPDUTestNoTag() {
    NfcAdapter nfcAdapterMock = mock(NfcAdapter.class);
    when(nfcAdapterMock.isEnabled())
      .thenReturn(true);
    ResponsesConstants ResponsesConstants = ResponsesConstants.getInstance();
    ResponsesConstants.setNfcAdapter(nfcAdapterMock);
    IsoDep tag = null;
    ResponsesConstants.setCardTag(tag);
    try {
      ResponsesConstants.sendAPDU(SELECT_TON_WALLET_APPLET_APDU);
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
    ResponsesConstants ResponsesConstants = ResponsesConstants.getInstance();
    ResponsesConstants.setNfcAdapter(nfcAdapterMock);

    IsoDep tag = mock(IsoDep.class);
    when(tag.isConnected()).thenReturn(false);
    Mockito.doThrow(new IOException()).when(tag).connect();
    ResponsesConstants.setCardTag(tag);
    try {
      ResponsesConstants.sendAPDU(SELECT_TON_WALLET_APPLET_APDU);
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
    ResponsesConstants ResponsesConstants = ResponsesConstants.getInstance();
    ResponsesConstants.setNfcAdapter(nfcAdapterMock);

    IsoDep tag = mock(IsoDep.class);
    when(tag.isConnected()).thenReturn(false);
    Mockito.doNothing().when(tag).connect();
    Mockito.doNothing().when(tag).setTimeout(TIME_OUT);
    Mockito.doThrow(new IOException()).when(tag).transceive(SELECT_TON_WALLET_APPLET_APDU.getBytes());

    ResponsesConstants.setCardTag(tag);

    try {
      ResponsesConstants.sendAPDU(SELECT_TON_WALLET_APPLET_APDU);
      fail();
    }
    catch (Exception e) {
      assertTrue(e.getMessage().contains(ResponsesConstants.ERROR_TRANSCEIVE));
      // e.printStackTrace();
    }
  }

  @Test
  public void sendAPDUTestTooShortResponseError() throws IOException {
    NfcAdapter nfcAdapterMock = mock(NfcAdapter.class);
    when(nfcAdapterMock.isEnabled())
      .thenReturn(true);
    ResponsesConstants ResponsesConstants = ResponsesConstants.getInstance();
    ResponsesConstants.setNfcAdapter(nfcAdapterMock);

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

      ResponsesConstants.setCardTag(tag);

      try {
        ResponsesConstants.sendAPDU(SELECT_TON_WALLET_APPLET_APDU);
        fail();
      } catch (Exception e) {
        assertEquals(e.getMessage(), ResponsesConstants.ERROR_BAD_RESPONSE);
        e.printStackTrace();
      }
    }
  }

  @Test
  public void sendAPDUTest() throws Exception {
    NfcAdapter nfcAdapterMock = mock(NfcAdapter.class);
    when(nfcAdapterMock.isEnabled())
      .thenReturn(true);
    ResponsesConstants ResponsesConstants = ResponsesConstants.getInstance();
    ResponsesConstants.setNfcAdapter(nfcAdapterMock);

    IsoDep tag = mock(IsoDep.class);
    when(tag.isConnected()).thenReturn(false);
    Mockito.doNothing().when(tag).connect();
    Mockito.doNothing().when(tag).setTimeout(TIME_OUT);
    when(tag.transceive(SELECT_TON_WALLET_APPLET_APDU.getBytes())).thenReturn(new byte[]{(byte)0x90, (byte)0x00});
    ResponsesConstants.setCardTag(tag);

    RAPDU rapdu = ResponsesConstants.sendAPDU(SELECT_TON_WALLET_APPLET_APDU);
    assertEquals(rapdu.getBytes().length, 2);
    assertEquals(rapdu.getData().length, 0);
    assertEquals(rapdu.getSW1(), (byte)0x90);
    assertEquals(rapdu.getSW2(), (byte)0x00);


    when(tag.transceive(GET_APP_INFO_APDU.getBytes())).thenReturn(new byte[]{(byte)0x17, (byte)0x90, (byte)0x00});
    ResponsesConstants.setCardTag(tag);

    rapdu = ResponsesConstants.sendAPDU(GET_APP_INFO_APDU);
    assertEquals(rapdu.getBytes().length, 3);
    assertEquals(rapdu.getData().length, 1);
    assertEquals(rapdu.getData()[0], (byte)0x17);
    assertEquals(rapdu.getSW1(), (byte)0x90);
    assertEquals(rapdu.getSW2(), (byte)0x00);

    when(tag.transceive(GET_RECOVERY_DATA_HASH_APDU.getBytes())).thenReturn(ByteArrayHelper.bConcat(ByteArrayHelper.bytes("aaaa1111aaaa11112222333322223333aaaa1111aaaa11112222333322223333aaaa1111aaaa11112222333322223333aaaa1111aaaa11112222333322223333"),
      new byte[]{(byte)0x90, (byte)0x00}));
    ResponsesConstants.setCardTag(tag);

    rapdu = ResponsesConstants.sendAPDU(GET_RECOVERY_DATA_HASH_APDU);
    assertEquals(rapdu.getBytes().length, 66);
    assertEquals(rapdu.getData().length, 64);
    assertArrayEquals(rapdu.getData(), ByteArrayHelper.bytes("aaaa1111aaaa11112222333322223333aaaa1111aaaa11112222333322223333aaaa1111aaaa11112222333322223333aaaa1111aaaa11112222333322223333"));
    assertEquals(rapdu.getSW1(), (byte)0x90);
    assertEquals(rapdu.getSW2(), (byte)0x00);
  }

  @Test
  public void sendAPDUTestForBadSw() throws Exception {
    NfcAdapter nfcAdapterMock = mock(NfcAdapter.class);
    when(nfcAdapterMock.isEnabled())
      .thenReturn(true);
    ResponsesConstants ResponsesConstants = ResponsesConstants.getInstance();
    ResponsesConstants.setNfcAdapter(nfcAdapterMock);

    IsoDep tag = mock(IsoDep.class);
    when(tag.isConnected()).thenReturn(false);
    Mockito.doNothing().when(tag).connect();
    Mockito.doNothing().when(tag).setTimeout(TIME_OUT);
    when(tag.transceive(SELECT_TON_WALLET_APPLET_APDU.getBytes())).thenReturn(new byte[]{(byte)0x6F, (byte)0x00}); // not 9000 sw
    ResponsesConstants.setCardTag(tag);

    try {
      RAPDU rapdu = ResponsesConstants.sendAPDU(SELECT_TON_WALLET_APPLET_APDU);
      fail();

    }
    catch (Exception e) {
      e.printStackTrace();
      assertTrue(e.getMessage().contains("6F00"));
    }
  }

  @Test
  public void  sendTonWalletAppletAPDUTest() throws Exception {
    NfcAdapter nfcAdapterMock = mock(NfcAdapter.class);
    when(nfcAdapterMock.isEnabled())
      .thenReturn(true);
    ResponsesConstants ResponsesConstants =  mock(ResponsesConstants.class);//ResponsesConstants.getInstance();

    when(ResponsesConstants.sendAPDUList(GET_APPLET_STATE_APDU_LIST)).thenReturn(new RAPDU(new  byte[]{APP_PERSONALIZED, (byte)0x90, (byte)0x00}));
    when(ResponsesConstants.sendTonWalletAppletAPDU(GET_HASH_OF_ENCRYPTED_COMMON_SECRET_APDU)).thenCallRealMethod();
    try {
      RAPDU rapdu = ResponsesConstants.sendTonWalletAppletAPDU(GET_HASH_OF_ENCRYPTED_COMMON_SECRET_APDU);
      fail();

    }
    catch (Exception e) {
      e.printStackTrace();
      assertTrue(e.getMessage().contains( " is not supported in state "));
    }

    when(ResponsesConstants.sendAPDUList(GET_APPLET_STATE_APDU_LIST)).thenReturn(new RAPDU(new  byte[]{APP_BLOCKED_MODE, (byte)0x90, (byte)0x00}));
    when(ResponsesConstants.sendTonWalletAppletAPDU(GET_PUB_KEY_WITH_DEFAULT_PATH_APDU)).thenCallRealMethod();
    try {
      RAPDU rapdu = ResponsesConstants.sendTonWalletAppletAPDU(GET_PUB_KEY_WITH_DEFAULT_PATH_APDU);
      fail();

    }
    catch (Exception e) {
      e.printStackTrace();
      assertTrue(e.getMessage().contains( " is not supported in state "));
    }

    when(ResponsesConstants.sendAPDUList(GET_APPLET_STATE_APDU_LIST)).thenReturn(new RAPDU(new  byte[]{APP_INSTALLED, (byte)0x90, (byte)0x00}));
    when(ResponsesConstants.sendTonWalletAppletAPDU(GET_HASH_OF_ENCRYPTED_PASSWORD_APDU)).thenCallRealMethod();
    try {
      RAPDU rapdu = ResponsesConstants.sendTonWalletAppletAPDU(GET_HASH_OF_ENCRYPTED_PASSWORD_APDU);
      fail();

    }
    catch (Exception e) {
      e.printStackTrace();
      assertTrue(e.getMessage().contains( " is not supported in state "));
    }
  }





}
*/