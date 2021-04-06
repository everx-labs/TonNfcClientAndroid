package com.tonnfccard;

import android.nfc.NfcAdapter;
import android.nfc.tech.IsoDep;

import com.tonnfccard.helpers.HmacHelper;
import com.tonnfccard.helpers.StringHelper;
import com.tonnfccard.nfc.NfcApduRunner;
import com.tonnfccard.smartcard.ErrorCodes;
import com.tonnfccard.smartcard.RAPDU;
import com.tonnfccard.utils.ByteArrayUtil;

import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.security.KeyStore;
import java.util.Random;

import static com.tonnfccard.TonWalletConstants.SAULT_LENGTH;
import static com.tonnfccard.TonWalletConstants.SHA_HASH_SIZE;
import static com.tonnfccard.nfc.NfcApduRunner.TIME_OUT;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.GET_APP_INFO_APDU;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.GET_SAULT_APDU;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.GET_SERIAL_NUMBER_APDU;
import static com.tonnfccard.smartcard.TonWalletAppletApduCommands.SELECT_TON_WALLET_APPLET_APDU;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class NfcMockHelper {
    public static final Random random = new Random();
    public static final ByteArrayUtil BYTE_ARRAY_HELPER = ByteArrayUtil.getInstance();
    public static final StringHelper STRING_HELPER = StringHelper.getInstance();
    public static boolean androidKeyStoreIsMocked = false;
    public static final byte[] SW_SUCCESS = BYTE_ARRAY_HELPER.bytes(ErrorCodes.SW_SUCCESS);
    public  static final String SN = "050004030904080002040303090001010206080103020306";

    public static IsoDep prepareTagMock() throws Exception {
        IsoDep tag = mock(IsoDep.class);
        when(tag.isConnected()).thenReturn(false);
        Mockito.doNothing().when(tag).connect();
        Mockito.doNothing().when(tag).setTimeout(TIME_OUT);
        return tag;
    }

    public static IsoDep prepareAdvancedTagMock(byte[] sault, byte state) throws Exception {
        IsoDep tag = prepareTagMock();
        when(tag.transceive(SELECT_TON_WALLET_APPLET_APDU.getBytes())).thenReturn(SW_SUCCESS);
        when(tag.transceive(GET_SERIAL_NUMBER_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bConcat(BYTE_ARRAY_HELPER.bytes(SN), SW_SUCCESS));
        when(tag.transceive(GET_SAULT_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bConcat(sault, SW_SUCCESS));
        when(tag.transceive(GET_APP_INFO_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bConcat(new byte[]{state}, SW_SUCCESS));
        return tag;
    }

    public static IsoDep prepareAdvancedTagMock(byte[] sault) throws Exception {
        IsoDep tag = prepareTagMock();
        when(tag.transceive(SELECT_TON_WALLET_APPLET_APDU.getBytes())).thenReturn(SW_SUCCESS);
        when(tag.transceive(GET_SERIAL_NUMBER_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bConcat(BYTE_ARRAY_HELPER.bytes(SN), SW_SUCCESS));
        when(tag.transceive(GET_SAULT_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bConcat(sault, SW_SUCCESS));
        return tag;
    }

    public static IsoDep prepareSimpleTagMock(byte state) throws Exception {
        IsoDep tag = prepareTagMock();
        when(tag.transceive(SELECT_TON_WALLET_APPLET_APDU.getBytes())).thenReturn(SW_SUCCESS);
        when(tag.transceive(GET_APP_INFO_APDU.getBytes())).thenReturn(BYTE_ARRAY_HELPER.bConcat(new byte[]{state}, SW_SUCCESS));
        return tag;
    }

    public static NfcApduRunner prepareNfcApduRunnerMock(NfcApduRunner nfcApduRunner) {
        NfcAdapter nfcAdapterMock = mock(NfcAdapter.class);
        when(nfcAdapterMock.isEnabled()).thenReturn(true);
        NfcApduRunner nfcApduRunnerMock = Mockito.spy(nfcApduRunner);
        nfcApduRunnerMock.setNfcAdapter(nfcAdapterMock);
        return nfcApduRunnerMock;
    }

    public static void mockNfcAdapter(NfcApduRunner nfcApduRunner, boolean adapterEnabled) {
        NfcAdapter nfcAdapterMock = mock(NfcAdapter.class);
        when(nfcAdapterMock.isEnabled())
                .thenReturn(adapterEnabled);
        nfcApduRunner.setNfcAdapter(nfcAdapterMock);
    }

    public static void mockNfcAdapterToBeNull(NfcApduRunner nfcApduRunner) {
        MockedStatic<NfcAdapter> nfcAdapterMockedStatic = Mockito.mockStatic(NfcAdapter.class);
        nfcAdapterMockedStatic
                .when(() -> NfcAdapter.getDefaultAdapter(any()))
                .thenReturn(null);
        nfcApduRunner.setNfcAdapter(null);
    }

    public static HmacHelper prepareHmacHelperMock(HmacHelper hmacHelper) throws Exception{
        HmacHelper hmacHelperMock = Mockito.spy(hmacHelper);
        byte[] mac = BYTE_ARRAY_HELPER.bytes(STRING_HELPER.randomHexString(SHA_HASH_SIZE * 2));
        Mockito.doReturn(mac).when(hmacHelperMock).computeMac(any());
        System.out.println(BYTE_ARRAY_HELPER.hex(hmacHelperMock.computeMac(new byte[1])));
        return hmacHelperMock;
    }

    public static void mockAndroidKeyStore() throws Exception{
        if (androidKeyStoreIsMocked) return;
        KeyStore keyStoreMock = mock(KeyStore.class);
        MockedStatic<KeyStore> keyStoreMockStatic = Mockito.mockStatic(KeyStore.class);
        keyStoreMockStatic
                .when(() -> KeyStore.getInstance(any()))
                .thenReturn(keyStoreMock);
        Mockito.doNothing().when(keyStoreMock).load(any());
        Mockito.doNothing().when(keyStoreMock).setEntry(any(), any(), any());
        Mockito.doReturn(true).when(keyStoreMock).containsAlias(any());
        androidKeyStoreIsMocked = true;
    }

    public static byte[] createSault() {
        byte[] sault = new byte[SAULT_LENGTH];
        random.nextBytes(sault);
        return sault;
    }
}
