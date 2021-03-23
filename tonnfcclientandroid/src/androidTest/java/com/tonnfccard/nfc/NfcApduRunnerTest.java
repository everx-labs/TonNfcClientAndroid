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