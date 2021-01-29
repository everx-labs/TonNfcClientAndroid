In the case of any error functions of library throw an exception. The exception usually contains inside a error message that is packed into json of special format. You can work with exception object directly or you can get error message json into your callback.

Below there is a full list of json error messages that the library can potentially throw into outside world.

## CARD_ERRORS

Here there are errors produced by NFC card (Ton wallet applet itself). So Android code just catches it and throws a error message (or put it into callback). Below there are exemplary jsons in which field "cardInstruction" always equals to  GET_APP_INFO. In reality in this field you may meet any other card instruction (APDU).

{
"errorType": "Applet fail: card operation error",
"errorTypeId": "0",
"errorCode": "6C00",
"message": "Correct Expected Length (Le).",
"cardInstruction": "GET_APP_INFO",
"apdu": "B0 C1 00 00 ",
"status": "fail"
}

{
"errorType": "Applet fail: card operation error",
"errorTypeId": "0",
"errorCode": "6100",
"message": "Response bytes remaining.",
"cardInstruction": "GET_APP_INFO",
"apdu": "B0 C1 00 00 ",
"status": "fail"
}

{
"errorType": "Applet fail: card operation error",
"errorTypeId": "0",
"errorCode": "6E00",
"message": "CLA value not supported.",
"cardInstruction": "GET_APP_INFO",
"apdu": "B0 C1 00 00 ",
"status": "fail"
}

{
"errorType": "Applet fail: card operation error",
"errorTypeId": "0",
"errorCode": "6D00",
"message": "INS value not supported.",
"cardInstruction": "GET_APP_INFO",
"apdu": "B0 C1 00 00 ",
"status": "fail"
}

{
"errorType": "Applet fail: card operation error",
"errorTypeId": "0",
"errorCode": "6F00",
"message": "Command aborted, No precise diagnosis.",
"cardInstruction": "GET_APP_INFO",
"apdu": "B0 C1 00 00 ",
"status": "fail"
}

{
"errorType": "Applet fail: card operation error",
"errorTypeId": "0",
"errorCode": "7F00",
"message": "Incorrect key index.",
"cardInstruction": "GET_APP_INFO",
"apdu": "B0 C1 00 00 ",
"status": "fail"
}

{
"errorType": "Applet fail: card operation error",
"errorTypeId": "0",
"errorCode": "6A80",
"message": "Wrong data.",
"cardInstruction": "GET_APP_INFO",
"apdu": "B0 C1 00 00 ",
"status": "fail"
}

{
"errorType": "Applet fail: card operation error",
"errorTypeId": "0",
"errorCode": "6700",
"message": "Wrong length.",
"cardInstruction": "GET_APP_INFO",
"apdu": "B0 C1 00 00 ",
"status": "fail"
}

{
"errorType": "Applet fail: card operation error",
"errorTypeId": "0",
"errorCode": "4F00",
"message": "Internal buffer is null or too small.",
"cardInstruction": "GET_APP_INFO",
"apdu": "B0 C1 00 00 ",
"status": "fail"
}

{
"errorType": "Applet fail: card operation error",
"errorTypeId": "0",
"errorCode": "5F00",
"message": "Incorrect password for card authentication.",
"cardInstruction": "GET_APP_INFO",
"apdu": "B0 C1 00 00 ",
"status": "fail"
}

{
"errorType": "Applet fail: card operation error",
"errorTypeId": "0",
"errorCode": "6A81",
"message": "Function not supported.",
"cardInstruction": "GET_APP_INFO",
"apdu": "B0 C1 00 00 ",
"status": "fail"
}

{
"errorType": "Applet fail: card operation error",
"errorTypeId": "0",
"errorCode": "6881",
"message": "Card does not support the operation on the speciﬁed logical channel.",
"cardInstruction": "GET_APP_INFO",
"apdu": "B0 C1 00 00 ",
"status": "fail"
}

{
"errorType": "Applet fail: card operation error",
"errorTypeId": "0",
"errorCode": "4F01",
"message": "Personalization is not finished.",
"cardInstruction": "GET_APP_INFO",
"apdu": "B0 C1 00 00 ",
"status": "fail"
}

{
"errorType": "Applet fail: card operation error",
"errorTypeId": "0",
"errorCode": "5F01",
"message": "Incorrect password, card is locked.",
"cardInstruction": "GET_APP_INFO",
"apdu": "B0 C1 00 00 ",
"status": "fail"
}

{
"errorType": "Applet fail: card operation error",
"errorTypeId": "0",
"errorCode": "6F01",
"message": "Set coin type failed.",
"cardInstruction": "GET_APP_INFO",
"apdu": "B0 C1 00 00 ",
"status": "fail"
}

{
"errorType": "Applet fail: card operation error",
"errorTypeId": "0",
"errorCode": "7F01",
"message": "Incorrect key chunk start or length.",
"cardInstruction": "GET_APP_INFO",
"apdu": "B0 C1 00 00 ",
"status": "fail"
}

{
"errorType": "Applet fail: card operation error",
"errorTypeId": "0",
"errorCode": "6A82",
"message": "File not found.",
"cardInstruction": "GET_APP_INFO",
"apdu": "B0 C1 00 00 ",
"status": "fail"
}

{
"errorType": "Applet fail: card operation error",
"errorTypeId": "0",
"errorCode": "6882",
"message": "Card does not support secure messaging.",
"cardInstruction": "GET_APP_INFO",
"apdu": "B0 C1 00 00 ",
"status": "fail"
}

{
"errorType": "Applet fail: card operation error",
"errorTypeId": "0",
"errorCode": "6982",
"message": "Security condition not satisﬁed.",
"cardInstruction": "GET_APP_INFO",
"apdu": "B0 C1 00 00 ",
"status": "fail"
}

{
"errorType": "Applet fail: card operation error",
"errorTypeId": "0",
"errorCode": "4F02",
"message": "Internal error: incorrect offset.",
"cardInstruction": "GET_APP_INFO",
"apdu": "B0 C1 00 00 ",
"status": "fail"
}

{
"errorType": "Applet fail: card operation error",
"errorTypeId": "0",
"errorCode": "6F02",
"message": "Set curve failed.",
"cardInstruction": "GET_APP_INFO",
"apdu": "B0 C1 00 00 ",
"status": "fail"
}

{
"errorType": "Applet fail: card operation error",
"errorTypeId": "0",
"errorCode": "7F02",
"message": "Incorrect key chunk length.",
"cardInstruction": "GET_APP_INFO",
"apdu": "B0 C1 00 00 ",
"status": "fail"
}

{
"errorType": "Applet fail: card operation error",
"errorTypeId": "0",
"errorCode": "6983",
"message": "File invalid.",
"cardInstruction": "GET_APP_INFO",
"apdu": "B0 C1 00 00 ",
"status": "fail"
}

{
"errorType": "Applet fail: card operation error",
"errorTypeId": "0",
"errorCode": "6883",
"message": "Record not found.",
"cardInstruction": "GET_APP_INFO",
"apdu": "B0 C1 00 00 ",
"status": "fail"
}

{
"errorType": "Applet fail: card operation error",
"errorTypeId": "0",
"errorCode": "4F03",
"message": "Internal error: incorrect payload value.",
"cardInstruction": "GET_APP_INFO",
"apdu": "B0 C1 00 00 ",
"status": "fail"
}

{
"errorType": "Applet fail: card operation error",
"errorTypeId": "0",
"errorCode": "6F03",
"message": "Get coin pub data failed.",
"cardInstruction": "GET_APP_INFO",
"apdu": "B0 C1 00 00 ",
"status": "fail"
}

{
"errorType": "Applet fail: card operation error",
"errorTypeId": "0",
"errorCode": "7F03",
"message": "Not enough space.",
"cardInstruction": "GET_APP_INFO",
"apdu": "B0 C1 00 00 ",
"status": "fail"
}

{
"errorType": "Applet fail: card operation error",
"errorTypeId": "0",
"errorCode": "6884",
"message": "Command chaining not supported.",
"cardInstruction": "GET_APP_INFO",
"apdu": "B0 C1 00 00 ",
"status": "fail"
}

{
"errorType": "Applet fail: card operation error",
"errorTypeId": "0",
"errorCode": "6984",
"message": "Data invalid.",
"cardInstruction": "GET_APP_INFO",
"apdu": "B0 C1 00 00 ",
"status": "fail"
}

{
"errorType": "Applet fail: card operation error",
"errorTypeId": "0",
"errorCode": "6A84",
"message": "Not enough memory space in the ﬁle.",
"cardInstruction": "GET_APP_INFO",
"apdu": "B0 C1 00 00 ",
"status": "fail"
}

{
"errorType": "Applet fail: card operation error",
"errorTypeId": "0",
"errorCode": "6F04",
"message": "Sign data failed.",
"cardInstruction": "GET_APP_INFO",
"apdu": "B0 C1 00 00 ",
"status": "fail"
}

{
"errorType": "Applet fail: card operation error",
"errorTypeId": "0",
"errorCode": "7F04",
"message": "Key size unknown.",
"cardInstruction": "GET_APP_INFO",
"apdu": "B0 C1 00 00 ",
"status": "fail"
}

{
"errorType": "Applet fail: card operation error",
"errorTypeId": "0",
"errorCode": "6985",
"message": "Conditions of use not satisﬁed.",
"cardInstruction": "GET_APP_INFO",
"apdu": "B0 C1 00 00 ",
"status": "fail"
}

{
"errorType": "Applet fail: card operation error",
"errorTypeId": "0",
"errorCode": "7F05",
"message": "Key length incorrect.",
"cardInstruction": "GET_APP_INFO",
"apdu": "B0 C1 00 00 ",
"status": "fail"
}

{
"errorType": "Applet fail: card operation error",
"errorTypeId": "0",
"errorCode": "6986",
"message": "Command not allowed (no current EF).",
"cardInstruction": "GET_APP_INFO",
"apdu": "B0 C1 00 00 ",
"status": "fail"
}

{
"errorType": "Applet fail: card operation error",
"errorTypeId": "0",
"errorCode": "6A86",
"message": "Incorrect parameters (P1,P2).",
"cardInstruction": "GET_APP_INFO",
"apdu": "B0 C1 00 00 ",
"status": "fail"
}

{
"errorType": "Applet fail: card operation error",
"errorTypeId": "0",
"errorCode": "7F06",
"message": "Hmac exists already.",
"cardInstruction": "GET_APP_INFO",
"apdu": "B0 C1 00 00 ",
"status": "fail"
}

{
"errorType": "Applet fail: card operation error",
"errorTypeId": "0",
"errorCode": "6F07",
"message": "Incorrect PIN (from Ton wallet applet).",
"cardInstruction": "GET_APP_INFO",
"apdu": "B0 C1 00 00 ",
"status": "fail"
}

{
"errorType": "Applet fail: card operation error",
"errorTypeId": "0",
"errorCode": "7F07",
"message": "Incorrect key index to change.",
"cardInstruction": "GET_APP_INFO",
"apdu": "B0 C1 00 00 ",
"status": "fail"
}

{
"errorType": "Applet fail: card operation error",
"errorTypeId": "0",
"errorCode": "6F08",
"message": "PIN tries expired.",
"cardInstruction": "GET_APP_INFO",
"apdu": "B0 C1 00 00 ",
"status": "fail"
}

{
"errorType": "Applet fail: card operation error",
"errorTypeId": "0",
"errorCode": "7F08",
"message": "Max number of keys (1023) is exceeded.",
"cardInstruction": "GET_APP_INFO",
"apdu": "B0 C1 00 00 ",
"status": "fail"
}

{
"errorType": "Applet fail: card operation error",
"errorTypeId": "0",
"errorCode": "7F09",
"message": "Delete key chunk is not finished.",
"cardInstruction": "GET_APP_INFO",
"apdu": "B0 C1 00 00 ",
"status": "fail"
}

{
"errorType": "Applet fail: card operation error",
"errorTypeId": "0",
"errorCode": "6F09",
"message": "Too big length of recovery data",
"cardInstruction": "GET_APP_INFO",
"apdu": "B0 C1 00 00 ",
"status": "fail"
}

{
"errorType": "Applet fail: card operation error",
"errorTypeId": "0",
"errorCode": "6F0A",
"message": "Incorrect start or length of recovery data piece in internal buffer",
"cardInstruction": "GET_APP_INFO",
"apdu": "B0 C1 00 00 ",
"status": "fail"
}

{
"errorType": "Applet fail: card operation error",
"errorTypeId": "0",
"errorCode": "6F0B",
"message": "Hash of recovery data is incorrect",
"cardInstruction": "GET_APP_INFO",
"apdu": "B0 C1 00 00 ",
"status": "fail"
}

{
"errorType": "Applet fail: card operation error",
"errorTypeId": "0",
"errorCode": "6F0C",
"message": "Recovery data already exists",
"cardInstruction": "GET_APP_INFO",
"apdu": "B0 C1 00 00 ",
"status": "fail"
}

{
"errorType": "Applet fail: card operation error",
"errorTypeId": "0",
"errorCode": "6F0D",
"message": "Recovery data does not exist",
"cardInstruction": "GET_APP_INFO",
"apdu": "B0 C1 00 00 ",
"status": "fail"
}

{
"errorType": "Applet fail: card operation error",
"errorTypeId": "0",
"errorCode": "6999",
"message": "Applet select failed.",
"cardInstruction": "GET_APP_INFO",
"apdu": "B0 C1 00 00 ",
"status": "fail"
}

{
"errorType": "Applet fail: card operation error",
"errorTypeId": "0",
"errorCode": "8F04",
"message": "Apdu Hmac verification tries expired.",
"cardInstruction": "GET_APP_INFO",
"apdu": "B0 C1 00 00 ",
"status": "fail"
}
{
"errorType": "Applet fail: card operation error",
"errorTypeId": "0",
"errorCode": "9B03",
"message": "Load seed error.",
"cardInstruction": "GET_APP_INFO",
"apdu": "B0 C1 00 00 ",
"status": "fail"
}

{
"errorType": "Applet fail: card operation error",
"errorTypeId": "0",
"errorCode": "8F03",
"message": "Incorrect apdu hmac.",
"cardInstruction": "GET_APP_INFO",
"apdu": "B0 C1 00 00 ",
"status": "fail"
}

{
"errorType": "Applet fail: card operation error",
"errorTypeId": "0",
"errorCode": "6F0D",
"message": "Recovery data does not exist",
"cardInstruction": "GET_APP_INFO",
"apdu": "B0 C1 00 00 ",
"status": "fail"
}

{
"errorType": "Applet fail: card operation error",
"errorTypeId": "0",
"errorCode": "6999",
"message": "Applet select failed.",
"cardInstruction": "GET_APP_INFO",
"apdu": "B0 C1 00 00 ",
"status": "fail"
}

{
"errorType": "Applet fail: card operation error",
"errorTypeId": "0",
"errorCode": "8F04",
"message": "Apdu Hmac verification tries expired.",
"cardInstruction": "GET_APP_INFO",
"apdu": "B0 C1 00 00 ",
"status": "fail"
}

{
"errorType": "Applet fail: card operation error",
"errorTypeId": "0",
"errorCode": "9B03",
"message": "Load seed error.",
"cardInstruction": "GET_APP_INFO",
"apdu": "B0 C1 00 00 ",
"status": "fail"
}
{
"errorType": "Applet fail: card operation error",
"errorTypeId": "0",
"errorCode": "8F03",
"message": "Incorrect apdu hmac.",
"cardInstruction": "GET_APP_INFO",
"apdu": "B0 C1 00 00 ",
"status": "fail"
}

{
"errorType": "Applet fail: card operation error",
"errorTypeId": "0",
"errorCode": "A001",
"message": "Serial number does not exist. You must set it.",
"cardInstruction": "GET_APP_INFO",
"apdu": "B0 C1 00 00 ",
"status": "fail"
}

## ANDROID_INTERNAL_ERRORS

Here there are some internal errors that may happen inside Android code. It means that something wrong happened in library and there is a bug in a library. Please report to the team if you would meet it. Normally they must not be met.

{
"errorType": "Android code fail: internal error",
"errorTypeId": "1",
"errorCode": "10000",
"message": "Apdu command is null",
"status": "fail"
}

{
"errorType": "Android code fail: internal error",
"errorTypeId": "1",
"errorCode": "10001",
"message": "Data field in APDU must have length > 0 and <= 255 bytes.",
"status": "fail"
}

{
"errorType": "Android code fail: internal error",
"errorTypeId": "1",
"errorCode": "10002",
"message": "APDU response bytes are incorrect. It must contain at least 2 bytes of status word (SW) from the card.",
"status": "fail"
}

{
"errorType": "Android code fail: internal error",
"errorTypeId": "1",
"errorCode": "10003",
"message": "APDU response is incorrect. Response from the card can not contain > 255 bytes.",
"status": "fail"
}

{
"errorType": "Android code fail: internal error",
"errorTypeId": "1",
"errorCode": "10004",
"message": "Pin byte array must have length 4.",
"status": "fail"
}

{
"errorType": "Android code fail: internal error",
"errorTypeId": "1",
"errorCode": "10005",
"message": "Device label byte array must have length 32.",
"status": "fail"
}

{
"errorType": "Android code fail: internal error",
"errorTypeId": "1",
"errorCode": "10006",
"message": "Activation password byte array must have length 128.",
"status": "fail"
}

{
"errorType": "Android code fail: internal error",
"errorTypeId": "1",
"errorCode": "10007",
"message": "Initial vector byte array must have length 16.",
"status": "fail"
}

{
"errorType": "Android code fail: internal error",
"errorTypeId": "1",
"errorCode": "10008",
"message": "Data for signing byte array must have length > 0 and <= 189.",
"status": "fail"
}

{
"errorType": "Android code fail: internal error",
"errorTypeId": "1",
"errorCode": "10009",
"message": "Data for signing byte array must have length > 0 and <= 178.",
"status": "fail"
}

{
"errorType": "Android code fail: internal error",
"errorTypeId": "1",
"errorCode": "10010",
"message": "APDU parameter P2 must take value from {0, 1, 2}.",
"status": "fail"
}

{
"errorType": "Android code fail: internal error",
"errorTypeId": "1",
"errorCode": "10011",
"message": "Key (from keyChain) chunk byte array must have length > 0 and <= 128.",
"status": "fail"
}

{
"errorType": "Android code fail: internal error",
"errorTypeId": "1",
"errorCode": "10012",
"message": "Key (from keyChain) mac byte array must have length 32.",
"status": "fail"
}

{
"errorType": "Android code fail: internal error",
"errorTypeId": "1",
"errorCode": "10013",
"message": "Sault byte array must have length 32.",
"status": "fail"
}

{
"errorType": "Android code fail: internal error",
"errorTypeId": "1",
"errorCode": "10014",
"message": "hdIndex byte array must have length > 0 and <= 10.",
"status": "fail"
}

{
"errorType": "Android code fail: internal error",
"errorTypeId": "1",
"errorCode": "10015",
"message": "Key (from keyChain) index byte array must have length = 2.",
"status": "fail"
}

{
"errorType": "Android code fail: internal error",
"errorTypeId": "1",
"errorCode": "10016",
"message": "Length is not a positive value",
"status": "fail"
}

{
"errorType": "Android code fail: internal error",
"errorTypeId": "1",
"errorCode": "10017",
"message": "Intent is null.",
"status": "fail"
}

{
"errorType": "Android code fail: internal error",
"errorTypeId": "1",
"errorCode": "10018",
"message": "Source array is null",
"status": "fail"
}

{
"errorType": "Android code fail: internal error",
"errorTypeId": "1",
"errorCode": "10019",
"message": "Destination array is null",
"status": "fail"
}

{
"errorType": "Android code fail: internal error",
"errorTypeId": "1",
"errorCode": "10020",
"message": "All array elements must be decimal digits >= 0 and < 9",
"status": "fail"
}

{
"errorType": "Android code fail: internal error",
"errorTypeId": "1",
"errorCode": "10021",
"message": "Can not convert empty array (or null) into digital string.",
"status": "fail"
}

{
"errorType": "Android code fail: internal error",
"errorTypeId": "1",
"errorCode": "10022",
"message": "Nfc callback is null.",
"status": "fail"
}

{
"errorType": "Android code fail: internal error",
"errorTypeId": "1",
"errorCode": "10023",
"message": "Exception object or its message is null.",
"status": "fail"
}

{
"errorType": "Android code fail: internal error",
"errorTypeId": "1",
"errorCode": "10024",
"message": "Count of is less than 0.",
"status": "fail"
}

{
"errorType": "Android code fail: internal error",
"errorTypeId": "1",
"errorCode": "10025",
"message": "Count is greater than length of source array (",
"status": "fail"
}

{
"errorType": "Android code fail: internal error",
"errorTypeId": "1",
"errorCode": "10026",
"message": "From is less than 0",
"status": "fail"
}

{
"errorType": "Android code fail: internal error",
"errorTypeId": "1",
"errorCode": "10027",
"message": "From is out of bound (",
"status": "fail"
}

{
"errorType": "Android code fail: internal error",
"errorTypeId": "1",
"errorCode": "10028",
"message": "End index is out of bound (",
"status": "fail"
}

{
"errorType": "Android code fail: internal error",
"errorTypeId": "1",
"errorCode": "10029",
"message": "Arrays are null",
"status": "fail"
}

{
"errorType": "Android code fail: internal error",
"errorTypeId": "1",
"errorCode": "10030",
"message": "String is null",
"status": "fail"
}

{
"errorType": "Android code fail: internal error",
"errorTypeId": "1",
"errorCode": "10031",
"message": "Source string is not correct hex: '",
"status": "fail"
}

{
"errorType": "Android code fail: internal error",
"errorTypeId": "1",
"errorCode": "10032",
"message": "Incorrect offset in source array.",
"status": "fail"
}

{
"errorType": "Android code fail: internal error",
"errorTypeId": "1",
"errorCode": "10033",
"message": "Source array must have length >= 2.",
"status": "fail"
}

{
"errorType": "Android code fail: internal error",
"errorTypeId": "1",
"errorCode": "10034",
"message": "First array is null",
"status": "fail"
}

{
"errorType": "Android code fail: internal error",
"errorTypeId": "1",
"errorCode": "10035",
"message": "Second array is null",
"status": "fail"
}

## ANDROID_NFC_ERRORS

Here there is a list of any troubles with NFC hardware and connection. (one example: when you forgot to connect the card).

{
"errorType": "Android code fail: NFC error",
"errorTypeId": "2",
"errorCode": "20000",
"message": "Nfc connection establishing error.",
"status": "fail"
}

{
"errorType": "Android code fail: NFC error",
"errorTypeId": "2",
"errorCode": "20001",
"message": "Nfc is disabled.",
"status": "fail"
}

{
"errorType": "Android code fail: NFC error",
"errorTypeId": "2",
"errorCode": "20002",
"message": "Nfc hardware is not found for this smartphone.",
"status": "fail"
}

{
"errorType": "Android code fail: NFC error",
"errorTypeId": "2",
"errorCode": "20003",
"message": "Nfc tag is not found.",
"status": "fail"
}

{
"errorType": "Android code fail: NFC error",
"errorTypeId": "2",
"errorCode": "20004",
"message": "Error happened during NFC tag disconnection.",
"status": "fail"
}

{
"errorType": "Android code fail: NFC error",
"errorTypeId": "2",
"errorCode": "20005",
"message": "Context is null",
"status": "fail"
}

{
"errorType": "Android code fail: NFC error",
"errorTypeId": "2",
"errorCode": "20006",
"message": "Data transfer via NFC failed. Probably NFC connection was lost.",
"status": "fail"
}

{
"errorType": "Android code fail: NFC error",
"errorTypeId": "2",
"errorCode": "20007",
"message": "Response from the card is too short. It must contain at least 2 bytes.",
"status": "fail"
}

## INPUT_DATA_FORMAT_ERRORS

Any trouble with input data that you feed into library functions as arguments. For example encryptedPassword argument for turnOnWallet function now must be a hex string of length 256 (and inside it is transformed into byte array of length 128). If you would sent string of another length you will get error. 

{
"errorType": "Native code fail: incorrect format of input data",
"errorTypeId": "3",
"errorCode": "30000",
"message": "Activation password is a hex string of length 256.",
"status": "fail"
}

{
"errorType": "Native code fail: incorrect format of input data",
"errorTypeId": "3",
"errorCode": "30001",
"message": "Common secret is a hex string of length 64.",
"status": "fail"
}

{
"errorType": "Native code fail: incorrect format of input data",
"errorTypeId": "3",
"errorCode": "30002",
"message": "Initial vector is a hex string of length 32.",
"status": "fail"
}

{
"errorType": "Native code fail: incorrect format of input data",
"errorTypeId": "3",
"errorCode": "30003",
"message": "Activation password is not a valid hex string.",
"status": "fail"
}

{
"errorType": "Native code fail: incorrect format of input data",
"errorTypeId": "3",
"errorCode": "30004",
"message": "Common secret is not a valid hex string.",
"status": "fail"
}

{
"errorType": "Native code fail: incorrect format of input data",
"errorTypeId": "3",
"errorCode": "30005",
"message": "Initial vector is not a valid hex string.",
"status": "fail"
}

{
"errorType": "Native code fail: incorrect format of input data",
"errorTypeId": "3",
"errorCode": "30006",
"message": "Pin must be a numeric string of length 4.",
"status": "fail"
}

{
"errorType": "Native code fail: incorrect format of input data",
"errorTypeId": "3",
"errorCode": "30007",
"message": "Pin is not a valid numeric string.",
"status": "fail"
}

{
"errorType": "Native code fail: incorrect format of input data",
"errorTypeId": "3",
"errorCode": "30008",
"message": "Data for signing is not a valid hex .",
"status": "fail"
}

{
"errorType": "Native code fail: incorrect format of input data",
"errorTypeId": "3",
"errorCode": "30009",
"message": "Data for signing must be a nonempty hex string of even length > 0 and <= 378.",
"status": "fail"
}

{
"errorType": "Native code fail: incorrect format of input data",
"errorTypeId": "3",
"errorCode": "30010",
"message": "Data for signing must be a nonempty hex string of even length > 0 and <= 356.",
"status": "fail"
}

{
"errorType": "Native code fail: incorrect format of input data",
"errorTypeId": "3",
"errorCode": "30011",
"message": "Recovery data is not a valid hex string.",
"status": "fail"
}

{
"errorType": "Native code fail: incorrect format of input data",
"errorTypeId": "3",
"errorCode": "30012",
"message": "Recovery data is a hex string of length > 0 and <= 4096.",
"status": "fail"
}

{
"errorType": "Native code fail: incorrect format of input data",
"errorTypeId": "3",
"errorCode": "30013",
"message": "Hd index must be a numeric string of length > 0 and <= 10.",
"status": "fail"
}

{
"errorType": "Native code fail: incorrect format of input data",
"errorTypeId": "3",
"errorCode": "30014",
"message": "Hd index is not a valid numeric string.",
"status": "fail"
}

{
"errorType": "Native code fail: incorrect format of input data",
"errorTypeId": "3",
"errorCode": "30015",
"message": "Device label must be a hex string of length 64.",
"status": "fail"
}

{
"errorType": "Native code fail: incorrect format of input data",
"errorTypeId": "3",
"errorCode": "30016",
"message": "Device label is not a valid hex string.",
"status": "fail"
}

{
"errorType": "Native code fail: incorrect format of input data",
"errorTypeId": "3",
"errorCode": "30017",
"message": "Key hmac is a hex string of length 64.",
"status": "fail"
}

{
"errorType": "Native code fail: incorrect format of input data",
"errorTypeId": "3",
"errorCode": "30018",
"message": "Key hmac is not a valid hex string.",
"status": "fail"
}

{
"errorType": "Native code fail: incorrect format of input data",
"errorTypeId": "3",
"errorCode": "30019",
"message": "Key is not a valid hex string.",
"status": "fail"
}

{
"errorType": "Native code fail: incorrect format of input data",
"errorTypeId": "3",
"errorCode": "30020",
"message": "Key is a hex string of length > 0 and <= 16384.",
"status": "fail"
}

{
"errorType": "Native code fail: incorrect format of input data",
"errorTypeId": "3",
"errorCode": "30021",
"message": "Key size must be > 0 and <= 8192.",
"status": "fail"
}

{
"errorType": "Native code fail: incorrect format of input data",
"errorTypeId": "3",
"errorCode": "30022",
"message": "Length of new key must be equal to length of old key ",
"status": "fail"
}

{
"errorType": "Native code fail: incorrect format of input data",
"errorTypeId": "3",
"errorCode": "30023",
"message": "Key index is a numeric string representing integer >= 0 and <= 1022.",
"status": "fail"
}

{
"errorType": "Native code fail: incorrect format of input data",
"errorTypeId": "3",
"errorCode": "30024",
"message": "Key index is not a valid numeric string.",
"status": "fail"
}

{
"errorType": "Native code fail: incorrect format of input data",
"errorTypeId": "3",
"errorCode": "30025",
"message": "Serial number is a numeric string of length 24.",
"status": "fail"
}

{
"errorType": "Native code fail: incorrect format of input data",
"errorTypeId": "3",
"errorCode": "30026",
"message": "Serial number is not a valid numeric string.",
"status": "fail"
}


