package com.tonnfccard.smartcard;

class CommonConstants {
  final static byte SELECT_CLA = 0x00;
  final static byte SELECT_INS = (byte) 0xA4;
  final static byte SELECT_P1 = 0x04;
  final static byte SELECT_P2 = 0x00;
  final static byte LE = 0x00;
  final static byte NEGATIVE_LE = (byte) -1;
  final static String SELECT_COIN_MANAGER_APDU_NAME = "SELECT_COIN_MANAGER";
  final static String SELECT_TON_WALLET_APPLET_APDU_NAME = "SELECT_TON_WALLET_APPLET";
}
