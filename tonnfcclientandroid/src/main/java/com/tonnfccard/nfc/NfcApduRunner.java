package com.tonnfccard.nfc;

import android.content.Context;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.util.Log;

import androidx.annotation.RestrictTo;

import com.tonnfccard.smartcard.ApduRunner;
import com.tonnfccard.smartcard.CAPDU;
import com.tonnfccard.smartcard.RAPDU;
import com.tonnfccard.utils.ByteArrayUtil;

import java.io.IOException;

import static android.nfc.NfcAdapter.EXTRA_TAG;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_BAD_RESPONSE;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_APDU_EMPTY;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_APDU_RESPONSE_TOO_LONG;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_INTENT_EMPTY;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_NFC_CONNECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_NFC_DISABLED;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_NFC_DISCONNECT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_NO_CONTEXT;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_NO_NFC;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_MSG_NO_TAG;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_NFC_CARD_WAS_NOT_CONNECTED;
import static com.tonnfccard.helpers.ResponsesConstants.ERROR_TRANSCEIVE;
import static com.tonnfccard.smartcard.RAPDU.MAX_LENGTH;

//@RestrictTo(RestrictTo.Scope.LIBRARY)

/**
 * This class is responsible for connecting NFC cards (tags), transmitting APDU commands via NFC and getting responses from the card.
 */
public class NfcApduRunner extends ApduRunner {
  public static final int TIME_OUT = 60000;
  public static final int NUMBER_OF_RETRIES_ATTEMPTS = 10;
  public static final int RETRY_TIME_OUT = 3000;
  private static NfcApduRunner nfcApduRunner;
  private static Context apiContext;

  private NfcAdapter nfcAdapter;
  private IsoDep nfcTag = null;
  private CAPDU lastSentAPDU = null;

  private NfcApduRunner(Context context) {
    super();
    apiContext = context;
  }

  public CAPDU getLastSentAPDU() {
    return lastSentAPDU;
  }


  public synchronized static NfcApduRunner getInstance(Context context) throws Exception{
    if (context == null) throw new Exception(ERROR_MSG_NO_CONTEXT);
    if (nfcApduRunner == null || apiContext == null) nfcApduRunner = new NfcApduRunner(context);
    return nfcApduRunner;
  }

  @RestrictTo(RestrictTo.Scope.TESTS)
  public void setCardTag(IsoDep nfcTag) {
    this.nfcTag = nfcTag;
  }

  @RestrictTo(RestrictTo.Scope.TESTS)
  public void setNfcAdapter(NfcAdapter nfcAdapter){
    this.nfcAdapter = nfcAdapter;
  }

  public boolean setCardTag(Intent intent) throws Exception{
    if (intent == null) throw new Exception(ERROR_MSG_INTENT_EMPTY);
    Tag tag = intent.getParcelableExtra(EXTRA_TAG);
    if (tag == null) return false;
    nfcTag = IsoDep.get(tag);
    return true;
  }

  public void connect() throws Exception {
    if (nfcAdapter == null) {
      nfcAdapter = NfcAdapter.getDefaultAdapter(apiContext);
    }
    if (nfcAdapter == null) {
      throw new Exception(ERROR_MSG_NO_NFC);
    } else if (!nfcAdapter.isEnabled()) {
      throw new Exception(ERROR_MSG_NFC_DISABLED);
    } else if (nfcTag == null) {
      throw new Exception(ERROR_MSG_NO_TAG);
    }
    try {
      if (!nfcTag.isConnected()) {
        nfcTag.connect();
        nfcTag.setTimeout(TIME_OUT);
      }
    } catch (Exception e) {
        throw new Exception(ERROR_MSG_NFC_CONNECT);
    }
  }

  @Override
  public void disconnectCard() throws Exception {
    System.out.println("I AM HERE");
    if (nfcTag == null) {
      throw new Exception(ERROR_MSG_NO_TAG);
    }
    System.out.println("I AM HERE1");
    try {
      System.out.println("I AM HERE2");
      nfcTag.close();
      System.out.println("I AM HERE3" + Thread.currentThread().getName());
    } catch (Exception e) {
      System.out.println("I AM HERE4");
        throw new Exception(ERROR_MSG_NFC_DISCONNECT + ", more details: " + e.getMessage());
    }
    System.out.println("I AM HERE35");
  }

  @Override
  public RAPDU transmitCommand(CAPDU commandAPDU) throws  Exception {
    Log.d("TAG","-1");
    RAPDU res = null;
    for (int i = 0 ; i < NUMBER_OF_RETRIES_ATTEMPTS ; i++) {
      Log.d("TAG", String.valueOf(i));
      try {
        if (nfcTag == null) {
          throw new Exception(ERROR_MSG_NO_TAG);
        }
        if (commandAPDU == null) {
          throw new Exception(ERROR_MSG_APDU_EMPTY);
        }
        res = new RAPDU(transceive(commandAPDU.getBytes()));
        lastSentAPDU = commandAPDU;
        return res;
      } catch (Exception e) {
        Thread.sleep(RETRY_TIME_OUT);
      }
    }
    throw new Exception(ERROR_NFC_CARD_WAS_NOT_CONNECTED);
  }

  private byte[] transceive(byte[] apduCommandBytes) throws Exception {
    connect();
    nfcTag.setTimeout(TIME_OUT);
    byte[] response;
    try {
     // long start = System.currentTimeMillis();
      response = nfcTag.transceive(apduCommandBytes);
      //long end = System.currentTimeMillis();
      //Log.d("TAG", "APDU = " + ByteArrayUtil.getInstance().hex(apduCommandBytes));
      //Log.d("TAG", "Time = " + String.valueOf(end - start) );
    } catch (Exception e) {
        throw new Exception(ERROR_TRANSCEIVE + ", More details: " + e.getMessage());
    }
    if (response == null || response.length <= 1) {
      throw new Exception(ERROR_BAD_RESPONSE);
    }
    if (response.length > MAX_LENGTH ) throw new IllegalArgumentException(ERROR_MSG_APDU_RESPONSE_TOO_LONG);
    return response;
  }
}
