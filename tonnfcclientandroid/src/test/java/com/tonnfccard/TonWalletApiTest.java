package com.tonnfccard;

import android.nfc.NfcAdapter;
import android.nfc.tech.IsoDep;

import com.tonnfccard.nfc.NfcApduRunner;

import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static com.tonnfccard.nfc.NfcApduRunner.TIME_OUT;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TonWalletApiTest {
    protected IsoDep prepareTagMock() throws Exception {
        IsoDep tag = mock(IsoDep.class);
        when(tag.isConnected()).thenReturn(false);
        Mockito.doNothing().when(tag).connect();
        Mockito.doNothing().when(tag).setTimeout(TIME_OUT);
        return tag;
    }

    protected NfcApduRunner prepareNfcApduRunnerMock(NfcApduRunner nfcApduRunner) {
        NfcAdapter nfcAdapterMock = mock(NfcAdapter.class);
        when(nfcAdapterMock.isEnabled()).thenReturn(true);
        NfcApduRunner nfcApduRunnerMock = Mockito.spy(nfcApduRunner);
        nfcApduRunnerMock.setNfcAdapter(nfcAdapterMock);
        return nfcApduRunnerMock;
    }

    protected void mockNfcAdapter(NfcApduRunner nfcApduRunner, boolean adapterEnabled) {
        NfcAdapter nfcAdapterMock = mock(NfcAdapter.class);
        when(nfcAdapterMock.isEnabled())
                .thenReturn(adapterEnabled);
        nfcApduRunner.setNfcAdapter(nfcAdapterMock);
    }

    protected void mockNfcAdapterToBeNull(NfcApduRunner nfcApduRunner) {
        MockedStatic<NfcAdapter> nfcAdapterMockedStatic = Mockito.mockStatic(NfcAdapter.class);
        nfcAdapterMockedStatic
                .when(() -> NfcAdapter.getDefaultAdapter(any()))
                .thenReturn(null);
        nfcApduRunner.setNfcAdapter(null);
    }
}