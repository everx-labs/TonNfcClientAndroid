In the case of any error TonNfcClientAndroid functions throws an exception. The exception usually contains inside a error message wrapped into json of special format. You can work with exception object directly or you can get error message json into your callback. Below there is a full list of json error messages that TonNfcClientAndroid can potentially throw into the caller.

## CARD_ERRORS

Here there are errors produced by NFC card (TON Labs wallet applet itself). So Android code just catches it and throws a error message (or puts it into callback). Below there are exemplary jsons. Their fields have the following meanings:

+ *code* — error status word (SW) produced by the card (applet)

+ *cardInstruction* — title of APDU command that failed

+ *errorTypeId* — id of error type ( it will always be zero here)

+ *errorType* — description of error type 

+ *message* — contains error message corresponding to code thrown by the card.

+ *apdu* — full text of failed APDU command in hex format

In below list field "cardInstruction" always equals to  GET_APP_INFO (just as example). But really in this field you may meet any other card instruction (APDU).

```json
{
"errorType": "Applet fail: card operation error",
"errorTypeId": "0",
"code": "6C00",
"message": "Correct Expected Length (Le).",
"cardInstruction": "GET_APP_INFO",
"apdu": "B0 C1 00 00 ",
"status": "fail"
}

{
"errorType": "Applet fail: card operation error",
"errorTypeId": "0",
"code": "6100",
"message": "Response bytes remaining.",
"cardInstruction": "GET_APP_INFO",
"apdu": "B0 C1 00 00 ",
"status": "fail"
}

{
"errorType": "Applet fail: card operation error",
"errorTypeId": "0",
"code": "6E00",
"message": "CLA value not supported.",
"cardInstruction": "GET_APP_INFO",
"apdu": "B0 C1 00 00 ",
"status": "fail"
}

{
"errorType": "Applet fail: card operation error",
"errorTypeId": "0",
"code": "6D00",
"message": "INS value not supported.",
"cardInstruction": "GET_APP_INFO",
"apdu": "B0 C1 00 00 ",
"status": "fail"
}

{
"errorType": "Applet fail: card operation error",
"errorTypeId": "0",
"code": "6F00",
"message": "Command aborted, No precise diagnosis.",
"cardInstruction": "GET_APP_INFO",
"apdu": "B0 C1 00 00 ",
"status": "fail"
}

{
"errorType": "Applet fail: card operation error",
"errorTypeId": "0",
"code": "7F00",
"message": "Incorrect key index.",
"cardInstruction": "GET_APP_INFO",
"apdu": "B0 C1 00 00 ",
"status": "fail"
}

{
"errorType": "Applet fail: card operation error",
"errorTypeId": "0",
"code": "6A80",
"message": "Wrong data.",
"cardInstruction": "GET_APP_INFO",
"apdu": "B0 C1 00 00 ",
"status": "fail"
}

{
"errorType": "Applet fail: card operation error",
"errorTypeId": "0",
"code": "6700",
"message": "Wrong length.",
"cardInstruction": "GET_APP_INFO",
"apdu": "B0 C1 00 00 ",
"status": "fail"
}

{
"errorType": "Applet fail: card operation error",
"errorTypeId": "0",
"code": "4F00",
"message": "Internal buffer is null or too small.",
"cardInstruction": "GET_APP_INFO",
"apdu": "B0 C1 00 00 ",
"status": "fail"
}

{
"errorType": "Applet fail: card operation error",
"errorTypeId": "0",
"code": "5F00",
"message": "Incorrect password for card authentication.",
"cardInstruction": "GET_APP_INFO",
"apdu": "B0 C1 00 00 ",
"status": "fail"
}

{
"errorType": "Applet fail: card operation error",
"errorTypeId": "0",
"code": "6A81",
"message": "Function not supported.",
"cardInstruction": "GET_APP_INFO",
"apdu": "B0 C1 00 00 ",
"status": "fail"
}

{
"errorType": "Applet fail: card operation error",
"errorTypeId": "0",
"code": "6881",
"message": "Card does not support the operation on the speciﬁed logical channel.",
"cardInstruction": "GET_APP_INFO",
"apdu": "B0 C1 00 00 ",
"status": "fail"
}

{
"errorType": "Applet fail: card operation error",
"errorTypeId": "0",
"code": "4F01",
"message": "Personalization is not finished.",
"cardInstruction": "GET_APP_INFO",
"apdu": "B0 C1 00 00 ",
"status": "fail"
}

{
"errorType": "Applet fail: card operation error",
"errorTypeId": "0",
"code": "5F01",
"message": "Incorrect password, card is locked.",
"cardInstruction": "GET_APP_INFO",
"apdu": "B0 C1 00 00 ",
"status": "fail"
}

{
"errorType": "Applet fail: card operation error",
"errorTypeId": "0",
"code": "6F01",
"message": "Set coin type failed.",
"cardInstruction": "GET_APP_INFO",
"apdu": "B0 C1 00 00 ",
"status": "fail"
}

{
"errorType": "Applet fail: card operation error",
"errorTypeId": "0",
"code": "7F01",
"message": "Incorrect key chunk start or length.",
"cardInstruction": "GET_APP_INFO",
"apdu": "B0 C1 00 00 ",
"status": "fail"
}

{
"errorType": "Applet fail: card operation error",
"errorTypeId": "0",
"code": "6A82",
"message": "File not found.",
"cardInstruction": "GET_APP_INFO",
"apdu": "B0 C1 00 00 ",
"status": "fail"
}

{
"errorType": "Applet fail: card operation error",
"errorTypeId": "0",
"code": "6882",
"message": "Card does not support secure messaging.",
"cardInstruction": "GET_APP_INFO",
"apdu": "B0 C1 00 00 ",
"status": "fail"
}

{
"errorType": "Applet fail: card operation error",
"errorTypeId": "0",
"code": "6982",
"message": "Security condition not satisﬁed.",
"cardInstruction": "GET_APP_INFO",
"apdu": "B0 C1 00 00 ",
"status": "fail"
}

{
"errorType": "Applet fail: card operation error",
"errorTypeId": "0",
"code": "4F02",
"message": "Internal error: incorrect offset.",
"cardInstruction": "GET_APP_INFO",
"apdu": "B0 C1 00 00 ",
"status": "fail"
}

{
"errorType": "Applet fail: card operation error",
"errorTypeId": "0",
"code": "6F02",
"message": "Set curve failed.",
"cardInstruction": "GET_APP_INFO",
"apdu": "B0 C1 00 00 ",
"status": "fail"
}

{
"errorType": "Applet fail: card operation error",
"errorTypeId": "0",
"code": "7F02",
"message": "Incorrect key chunk length.",
"cardInstruction": "GET_APP_INFO",
"apdu": "B0 C1 00 00 ",
"status": "fail"
}

{
"errorType": "Applet fail: card operation error",
"errorTypeId": "0",
"code": "6983",
"message": "File invalid.",
"cardInstruction": "GET_APP_INFO",
"apdu": "B0 C1 00 00 ",
"status": "fail"
}

{
"errorType": "Applet fail: card operation error",
"errorTypeId": "0",
"code": "6883",
"message": "Record not found.",
"cardInstruction": "GET_APP_INFO",
"apdu": "B0 C1 00 00 ",
"status": "fail"
}

{
"errorType": "Applet fail: card operation error",
"errorTypeId": "0",
"code": "4F03",
"message": "Internal error: incorrect payload value.",
"cardInstruction": "GET_APP_INFO",
"apdu": "B0 C1 00 00 ",
"status": "fail"
}

{
"errorType": "Applet fail: card operation error",
"errorTypeId": "0",
"code": "6F03",
"message": "Get coin pub data failed.",
"cardInstruction": "GET_APP_INFO",
"apdu": "B0 C1 00 00 ",
"status": "fail"
}

{
"errorType": "Applet fail: card operation error",
"errorTypeId": "0",
"code": "7F03",
"message": "Not enough space.",
"cardInstruction": "GET_APP_INFO",
"apdu": "B0 C1 00 00 ",
"status": "fail"
}

{
"errorType": "Applet fail: card operation error",
"errorTypeId": "0",
"code": "6884",
"message": "Command chaining not supported.",
"cardInstruction": "GET_APP_INFO",
"apdu": "B0 C1 00 00 ",
"status": "fail"
}

{
"errorType": "Applet fail: card operation error",
"errorTypeId": "0",
"code": "6984",
"message": "Data invalid.",
"cardInstruction": "GET_APP_INFO",
"apdu": "B0 C1 00 00 ",
"status": "fail"
}

{
"errorType": "Applet fail: card operation error",
"errorTypeId": "0",
"code": "6A84",
"message": "Not enough memory space in the ﬁle.",
"cardInstruction": "GET_APP_INFO",
"apdu": "B0 C1 00 00 ",
"status": "fail"
}

{
"errorType": "Applet fail: card operation error",
"errorTypeId": "0",
"code": "6F04",
"message": "Sign data failed.",
"cardInstruction": "GET_APP_INFO",
"apdu": "B0 C1 00 00 ",
"status": "fail"
}

{
"errorType": "Applet fail: card operation error",
"errorTypeId": "0",
"code": "7F04",
"message": "Key size unknown.",
"cardInstruction": "GET_APP_INFO",
"apdu": "B0 C1 00 00 ",
"status": "fail"
}

{
"errorType": "Applet fail: card operation error",
"errorTypeId": "0",
"code": "6985",
"message": "Conditions of use not satisﬁed.",
"cardInstruction": "GET_APP_INFO",
"apdu": "B0 C1 00 00 ",
"status": "fail"
}

{
"errorType": "Applet fail: card operation error",
"errorTypeId": "0",
"code": "7F05",
"message": "Key length incorrect.",
"cardInstruction": "GET_APP_INFO",
"apdu": "B0 C1 00 00 ",
"status": "fail"
}

{
"errorType": "Applet fail: card operation error",
"errorTypeId": "0",
"code": "6986",
"message": "Command not allowed (no current EF).",
"cardInstruction": "GET_APP_INFO",
"apdu": "B0 C1 00 00 ",
"status": "fail"
}

{
"errorType": "Applet fail: card operation error",
"errorTypeId": "0",
"code": "6A86",
"message": "Incorrect parameters (P1,P2).",
"cardInstruction": "GET_APP_INFO",
"apdu": "B0 C1 00 00 ",
"status": "fail"
}

{
"errorType": "Applet fail: card operation error",
"errorTypeId": "0",
"code": "7F06",
"message": "Hmac exists already.",
"cardInstruction": "GET_APP_INFO",
"apdu": "B0 C1 00 00 ",
"status": "fail"
}

{
"errorType": "Applet fail: card operation error",
"errorTypeId": "0",
"code": "6F07",
"message": "Incorrect PIN (from Ton wallet applet).",
"cardInstruction": "GET_APP_INFO",
"apdu": "B0 C1 00 00 ",
"status": "fail"
}

{
"errorType": "Applet fail: card operation error",
"errorTypeId": "0",
"code": "7F07",
"message": "Incorrect key index to change.",
"cardInstruction": "GET_APP_INFO",
"apdu": "B0 C1 00 00 ",
"status": "fail"
}

{
"errorType": "Applet fail: card operation error",
"errorTypeId": "0",
"code": "6F08",
"message": "PIN tries expired.",
"cardInstruction": "GET_APP_INFO",
"apdu": "B0 C1 00 00 ",
"status": "fail"
}

{
"errorType": "Applet fail: card operation error",
"errorTypeId": "0",
"code": "7F08",
"message": "Max number of keys (1023) is exceeded.",
"cardInstruction": "GET_APP_INFO",
"apdu": "B0 C1 00 00 ",
"status": "fail"
}

{
"errorType": "Applet fail: card operation error",
"errorTypeId": "0",
"code": "7F09",
"message": "Delete key chunk is not finished.",
"cardInstruction": "GET_APP_INFO",
"apdu": "B0 C1 00 00 ",
"status": "fail"
}

{
"errorType": "Applet fail: card operation error",
"errorTypeId": "0",
"code": "6F09",
"message": "Too big length of recovery data",
"cardInstruction": "GET_APP_INFO",
"apdu": "B0 C1 00 00 ",
"status": "fail"
}

{
"errorType": "Applet fail: card operation error",
"errorTypeId": "0",
"code": "6F0A",
"message": "Incorrect start or length of recovery data piece in internal buffer",
"cardInstruction": "GET_APP_INFO",
"apdu": "B0 C1 00 00 ",
"status": "fail"
}

{
"errorType": "Applet fail: card operation error",
"errorTypeId": "0",
"code": "6F0B",
"message": "Hash of recovery data is incorrect",
"cardInstruction": "GET_APP_INFO",
"apdu": "B0 C1 00 00 ",
"status": "fail"
}

{
"errorType": "Applet fail: card operation error",
"errorTypeId": "0",
"code": "6F0C",
"message": "Recovery data already exists",
"cardInstruction": "GET_APP_INFO",
"apdu": "B0 C1 00 00 ",
"status": "fail"
}

{
"errorType": "Applet fail: card operation error",
"errorTypeId": "0",
"code": "6F0D",
"message": "Recovery data does not exist",
"cardInstruction": "GET_APP_INFO",
"apdu": "B0 C1 00 00 ",
"status": "fail"
}

{
"errorType": "Applet fail: card operation error",
"errorTypeId": "0",
"code": "6999",
"message": "Applet select failed.",
"cardInstruction": "GET_APP_INFO",
"apdu": "B0 C1 00 00 ",
"status": "fail"
}

{
"errorType": "Applet fail: card operation error",
"errorTypeId": "0",
"code": "8F04",
"message": "Apdu Hmac verification tries expired.",
"cardInstruction": "GET_APP_INFO",
"apdu": "B0 C1 00 00 ",
"status": "fail"
}
{
"errorType": "Applet fail: card operation error",
"errorTypeId": "0",
"code": "9B03",
"message": "Load seed error.",
"cardInstruction": "GET_APP_INFO",
"apdu": "B0 C1 00 00 ",
"status": "fail"
}

{
"errorType": "Applet fail: card operation error",
"errorTypeId": "0",
"code": "8F03",
"message": "Incorrect apdu hmac.",
"cardInstruction": "GET_APP_INFO",
"apdu": "B0 C1 00 00 ",
"status": "fail"
}

{
"errorType": "Applet fail: card operation error",
"errorTypeId": "0",
"code": "6F0D",
"message": "Recovery data does not exist",
"cardInstruction": "GET_APP_INFO",
"apdu": "B0 C1 00 00 ",
"status": "fail"
}

{
"errorType": "Applet fail: card operation error",
"errorTypeId": "0",
"code": "6999",
"message": "Applet select failed.",
"cardInstruction": "GET_APP_INFO",
"apdu": "B0 C1 00 00 ",
"status": "fail"
}

{
"errorType": "Applet fail: card operation error",
"errorTypeId": "0",
"code": "8F04",
"message": "Apdu Hmac verification tries expired.",
"cardInstruction": "GET_APP_INFO",
"apdu": "B0 C1 00 00 ",
"status": "fail"
}

{
"errorType": "Applet fail: card operation error",
"errorTypeId": "0",
"code": "9B03",
"message": "Load seed error.",
"cardInstruction": "GET_APP_INFO",
"apdu": "B0 C1 00 00 ",
"status": "fail"
}
{
"errorType": "Applet fail: card operation error",
"errorTypeId": "0",
"code": "8F03",
"message": "Incorrect apdu hmac.",
"cardInstruction": "GET_APP_INFO",
"apdu": "B0 C1 00 00 ",
"status": "fail"
}

{
"errorType": "Applet fail: card operation error",
"errorTypeId": "0",
"code": "A001",
"message": "Serial number does not exist. You must set it.",
"cardInstruction": "GET_APP_INFO",
"apdu": "B0 C1 00 00 ",
"status": "fail"
}
```

## ANDROID_INTERNAL_ERRORS

Here there are some internal errors that may happen inside Android code. It means that something wrong happened and there is a bug in a library. Please report to the team if you would meet it. Normally they must not be met.

```json
{
  "code": "90000",
  "errorType": "Android code fail: internal error",
  "errorTypeId": "9",
  "message": "Apdu command is null",
  "status": "fail"
}
{
  "code": "90001",
  "errorType": "Android code fail: internal error",
  "errorTypeId": "9",
  "message": "Data field in APDU must have length > 0 and <= 255 bytes.",
  "status": "fail"
}
{
  "code": "90002",
  "errorType": "Android code fail: internal error",
  "errorTypeId": "9",
  "message": "APDU response bytes are incorrect. It must contain at least 2 bytes of status word (SW) from the card.",
  "status": "fail"
}
{
  "code": "90003",
  "errorType": "Android code fail: internal error",
  "errorTypeId": "9",
  "message": "APDU response is incorrect. Response from the card can not contain > 255 bytes.",
  "status": "fail"
}
{
  "code": "90004",
  "errorType": "Android code fail: internal error",
  "errorTypeId": "9",
  "message": "Pin byte array must have length 4.",
  "status": "fail"
}
{
  "code": "90005",
  "errorType": "Android code fail: internal error",
  "errorTypeId": "9",
  "message": "Device label byte array must have length 32.",
  "status": "fail"
}
{
  "code": "90006",
  "errorType": "Android code fail: internal error",
  "errorTypeId": "9",
  "message": "Activation password byte array must have length 128.",
  "status": "fail"
}
{
  "code": "90007",
  "errorType": "Android code fail: internal error",
  "errorTypeId": "9",
  "message": "Initial vector byte array must have length 16.",
  "status": "fail"
}
{
  "code": "90008",
  "errorType": "Android code fail: internal error",
  "errorTypeId": "9",
  "message": "Data for signing byte array must have length > 0 and <= 189.",
  "status": "fail"
}
{
  "code": "90009",
  "errorType": "Android code fail: internal error",
  "errorTypeId": "9",
  "message": "Data for signing byte array must have length > 0 and <= 178.",
  "status": "fail"
}
{
  "code": "90010",
  "errorType": "Android code fail: internal error",
  "errorTypeId": "9",
  "message": "APDU parameter P2 must take value from {0, 1, 2}.",
  "status": "fail"
}
{
  "code": "90011",
  "errorType": "Android code fail: internal error",
  "errorTypeId": "9",
  "message": "Key (from keyChain) chunk byte array must have length > 0 and <= 128.",
  "status": "fail"
}
{
  "code": "90012",
  "errorType": "Android code fail: internal error",
  "errorTypeId": "9",
  "message": "Key (from keyChain) mac byte array must have length 32.",
  "status": "fail"
}
{
  "code": "90013",
  "errorType": "Android code fail: internal error",
  "errorTypeId": "9",
  "message": "Sault byte array must have length 32.",
  "status": "fail"
}
{
  "code": "90014",
  "errorType": "Android code fail: internal error",
  "errorTypeId": "9",
  "message": "hdIndex byte array must have length > 0 and <= 10.",
  "status": "fail"
}
{
  "code": "90015",
  "errorType": "Android code fail: internal error",
  "errorTypeId": "9",
  "message": "Key (from keyChain) index byte array must have length = 2.",
  "status": "fail"
}
{
  "code": "90016",
  "errorType": "Android code fail: internal error",
  "errorTypeId": "9",
  "message": "Length is not a positive value",
  "status": "fail"
}
{
  "code": "90017",
  "errorType": "Android code fail: internal error",
  "errorTypeId": "9",
  "message": "Intent is null.",
  "status": "fail"
}
{
  "code": "90018",
  "errorType": "Android code fail: internal error",
  "errorTypeId": "9",
  "message": "Source array is null",
  "status": "fail"
}
{
  "code": "90019",
  "errorType": "Android code fail: internal error",
  "errorTypeId": "9",
  "message": "Destination array is null",
  "status": "fail"
}
{
  "code": "90020",
  "errorType": "Android code fail: internal error",
  "errorTypeId": "9",
  "message": "All array elements must be decimal digits >= 0 and < 9",
  "status": "fail"
}
{
  "code": "90021",
  "errorType": "Android code fail: internal error",
  "errorTypeId": "9",
  "message": "Can not convert empty array (or null) into digital string.",
  "status": "fail"
}
{
  "code": "90022",
  "errorType": "Android code fail: internal error",
  "errorTypeId": "9",
  "message": "Nfc callback is null.",
  "status": "fail"
}
{
  "code": "90023",
  "errorType": "Android code fail: internal error",
  "errorTypeId": "9",
  "message": "Exception object is null.",
  "status": "fail"
}
{
  "code": "90024",
  "errorType": "Android code fail: internal error",
  "errorTypeId": "9",
  "message": "Count of is less than 0.",
  "status": "fail"
}
{
  "code": "90025",
  "errorType": "Android code fail: internal error",
  "errorTypeId": "9",
  "message": "Count is greater than length of source array (",
  "status": "fail"
}
{
  "code": "90026",
  "errorType": "Android code fail: internal error",
  "errorTypeId": "9",
  "message": "From is less than 0",
  "status": "fail"
}
{
  "code": "90027",
  "errorType": "Android code fail: internal error",
  "errorTypeId": "9",
  "message": "Offset is out of bound (",
  "status": "fail"
}
{
  "code": "90028",
  "errorType": "Android code fail: internal error",
  "errorTypeId": "9",
  "message": "End index is out of bound (",
  "status": "fail"
}
{
  "code": "90029",
  "errorType": "Android code fail: internal error",
  "errorTypeId": "9",
  "message": "All arrays arg are null.",
  "status": "fail"
}
{
  "code": "90030",
  "errorType": "Android code fail: internal error",
  "errorTypeId": "9",
  "message": "String is null.",
  "status": "fail"
}
{
  "code": "90031",
  "errorType": "Android code fail: internal error",
  "errorTypeId": "9",
  "message": "Source string is not correct hex: ",
  "status": "fail"
}
{
  "code": "90032",
  "errorType": "Android code fail: internal error",
  "errorTypeId": "9",
  "message": "Incorrect offset in source array.",
  "status": "fail"
}
{
  "code": "90033",
  "errorType": "Android code fail: internal error",
  "errorTypeId": "9",
  "message": "Incorrect offset in dest array.",
  "status": "fail"
}
{
  "code": "90034",
  "errorType": "Android code fail: internal error",
  "errorTypeId": "9",
  "message": "Source array must have length >= 2.",
  "status": "fail"
}
{
  "code": "90035",
  "errorType": "Android code fail: internal error",
  "errorTypeId": "9",
  "message": "First array is null",
  "status": "fail"
}
{
  "code": "90036",
  "errorType": "Android code fail: internal error",
  "errorTypeId": "9",
  "message": "Second array is null",
  "status": "fail"
}
{
  "code": "90037",
  "errorType": "Android code fail: internal error",
  "errorTypeId": "9",
  "message": "Context is null",
  "status": "fail"
}
{
  "code": "90038",
  "errorType": "Android code fail: internal error",
  "errorTypeId": "9",
  "message": "Error message is null.",
  "status": "fail"
}
{
  "code": "90039",
  "errorType": "Android code fail: internal error",
  "errorTypeId": "9",
  "message": "Key for HMAC-SHA256 algorithm is null.",
  "status": "fail"
}
{
  "code": "90040",
  "errorType": "Android code fail: internal error",
  "errorTypeId": "9",
  "message": "Key bytes for HMAC-SHA256 must have length >= 32.",
  "status": "fail"
}
{
  "code": "90041",
  "errorType": "Android code fail: internal error",
  "errorTypeId": "9",
  "message": "Data to sign by HMAC-SHA256 algorithm is null.",
  "status": "fail"
}
{
  "code": "90042",
  "errorType": "Android code fail: internal error",
  "errorTypeId": "9",
  "message": "Current serial number is null.",
  "status": "fail"
}
{
  "code": "90043",
  "errorType": "Android code fail: internal error",
  "errorTypeId": "9",
  "message": "Not an instance of a SecretKeyEntry",
  "status": "fail"
}
{
  "code": "90044",
  "errorType": "Android code fail: internal error",
  "errorTypeId": "9",
  "message": "Malformed data for json (message is null).",
  "status": "fail"
}
{
  "code": "90045",
  "errorType": "Android code fail: internal error",
  "errorTypeId": "9",
  "message": "Malformed SW for json.",
  "status": "fail"
}
{
  "code": "90046",
  "errorType": "Android code fail: internal error",
  "errorTypeId": "9",
  "message": "CAPDU is null.",
  "status": "fail"
}
{
  "code": "90047",
  "errorType": "Android code fail: internal error",
  "errorTypeId": "9",
  "message": "String is not in ascii.",
  "status": "fail"
}
{
  "code": "90048",
  "errorType": "Android code fail: internal error",
  "errorTypeId": "9",
  "message": "APDU data field is null.",
  "status": "fail"
}
{
  "code": "90049",
  "errorType": "Android code fail: internal error",
  "errorTypeId": "9",
  "message": "Recovery data portion must have length > 0 and <= 250.",
  "status": "fail"
}
{
  "code": "90050",
  "errorType": "Android code fail: internal error",
  "errorTypeId": "9",
  "message": "Mac (byte array) of recovery data portion must have length 32.",
  "status": "fail"
}
{
  "code": "90051",
  "errorType": "Android code fail: internal error",
  "errorTypeId": "9",
  "message": "Start position byte array must have length = 2.",
  "status": "fail"
}
{
  "code": "90052",
  "errorType": "Android code fail: internal error",
  "errorTypeId": "9",
  "message": "APDU response is null.",
  "status": "fail"
}
```
## NFC_INTERRUPTION_ERRORS

{
  "code": "20000",
  "errorType": "Native code fail: NFC connection interruption",
  "errorTypeId": "2",
  "message": "Nfc connection was interrupted by user.",
  "status": "fail"
}

## ANDROID_NFC_ERRORS

Here there is a list of any troubles with NFC hardware and connection. 

```json
{
  "code": "220000",
  "errorType": "Android code fail: NFC error",
  "errorTypeId": "22",
  "message": "Nfc connection establishing error.",
  "status": "fail"
}
{
  "code": "220001",
  "errorType": "Android code fail: NFC error",
  "errorTypeId": "22",
  "message": "Nfc is disabled.",
  "status": "fail"
}
{
  "code": "220002",
  "errorType": "Android code fail: NFC error",
  "errorTypeId": "22",
  "message": "Nfc hardware is not found for this smartphone.",
  "status": "fail"
}
{
  "code": "220003",
  "errorType": "Android code fail: NFC error",
  "errorTypeId": "22",
  "message": "Nfc tag is not found.",
  "status": "fail"
}
{
  "code": "220004",
  "errorType": "Android code fail: NFC error",
  "errorTypeId": "22",
  "message": "Error happened during NFC tag disconnection.",
  "status": "fail"
}
{
  "code": "220005",
  "errorType": "Android code fail: NFC error",
  "errorTypeId": "22",
  "message": "Data transfer via NFC failed. Probably NFC connection was lost.",
  "status": "fail"
}
{
  "code": "220006",
  "errorType": "Android code fail: NFC error",
  "errorTypeId": "22",
  "message": "Response from the card is too short. It must contain at least 2 bytes.",
  "status": "fail"
}
```

## INPUT_DATA_FORMAT_ERRORS

Any trouble with input data passing into TonNfcClientAndroid API functions. For example, encryptedPassword argument for turnOnWallet function must be a hex string of length 256 (and inside it is transformed into byte array of length 128). If you would send string of another length you will get error.

```json
{
  "errorType": "Native code fail: incorrect format of input data",
  "errorTypeId": "3",
  "code": "30000",
  "message": "Activation password is a hex string of length 256.",
  "status": "fail"
}
{
  "errorType": "Native code fail: incorrect format of input data",
  "errorTypeId": "3",
  "code": "30001",
  "message": "Common secret is a hex string of length 64.",
  "status": "fail"
}
{
  "errorType": "Native code fail: incorrect format of input data",
  "errorTypeId": "3",
  "code": "30002",
  "message": "Initial vector is a hex string of length 32.",
  "status": "fail"
}
{
  "errorType": "Native code fail: incorrect format of input data",
  "errorTypeId": "3",
  "code": "30003",
  "message": "Activation password is not a valid hex string.",
  "status": "fail"
}
{
  "errorType": "Native code fail: incorrect format of input data",
  "errorTypeId": "3",
  "code": "30004",
  "message": "Common secret is not a valid hex string.",
  "status": "fail"
}
{
  "errorType": "Native code fail: incorrect format of input data",
  "errorTypeId": "3",
  "code": "30005",
  "message": "Initial vector is not a valid hex string.",
  "status": "fail"
}
{
  "errorType": "Native code fail: incorrect format of input data",
  "errorTypeId": "3",
  "code": "30006",
  "message": "Pin must be a numeric string of length 4.",
  "status": "fail"
}
{
  "errorType": "Native code fail: incorrect format of input data",
  "errorTypeId": "3",
  "code": "30007",
  "message": "Pin is not a valid numeric string.",
  "status": "fail"
}
{
  "errorType": "Native code fail: incorrect format of input data",
  "errorTypeId": "3",
  "code": "30008",
  "message": "Data for signing is not a valid hex .",
  "status": "fail"
}
{
  "errorType": "Native code fail: incorrect format of input data",
  "errorTypeId": "3",
  "code": "30009",
  "message": "Data for signing must be a nonempty hex string of even length > 0 and <= 378.",
  "status": "fail"
}
{
  "errorType": "Native code fail: incorrect format of input data",
  "errorTypeId": "3",
  "code": "30010",
  "message": "Data for signing must be a nonempty hex string of even length > 0 and <= 356.",
  "status": "fail"
}
{
  "errorType": "Native code fail: incorrect format of input data",
  "errorTypeId": "3",
  "code": "30011",
  "message": "Recovery data is not a valid hex string.",
  "status": "fail"
}
{
  "errorType": "Native code fail: incorrect format of input data",
  "errorTypeId": "3",
  "code": "30012",
  "message": "Recovery data is a hex string of length > 0 and <= 4096.",
  "status": "fail"
}
{
  "errorType": "Native code fail: incorrect format of input data",
  "errorTypeId": "3",
  "code": "30013",
  "message": "Hd index must be a numeric string of length > 0 and <= 10.",
  "status": "fail"
}
{
  "errorType": "Native code fail: incorrect format of input data",
  "errorTypeId": "3",
  "code": "30014",
  "message": "Hd index is not a valid numeric string.",
  "status": "fail"
}
{
  "errorType": "Native code fail: incorrect format of input data",
  "errorTypeId": "3",
  "code": "30015",
  "message": "Device label must be a hex string of length 64.",
  "status": "fail"
}
{
  "errorType": "Native code fail: incorrect format of input data",
  "errorTypeId": "3",
  "code": "30016",
  "message": "Device label is not a valid hex string.",
  "status": "fail"
}
{
  "errorType": "Native code fail: incorrect format of input data",
  "errorTypeId": "3",
  "code": "30017",
  "message": "Key hmac is a hex string of length 64.",
  "status": "fail"
}
{
  "errorType": "Native code fail: incorrect format of input data",
  "errorTypeId": "3",
  "code": "30018",
  "message": "Key hmac is not a valid hex string.",
  "status": "fail"
}
{
  "errorType": "Native code fail: incorrect format of input data",
  "errorTypeId": "3",
  "code": "30019",
  "message": "Key is not a valid hex string.",
  "status": "fail"
}
{
  "errorType": "Native code fail: incorrect format of input data",
  "errorTypeId": "3",
  "code": "30020",
  "message": "Key is a hex string of length > 0 and <= 16384.",
  "status": "fail"
}
{
  "errorType": "Native code fail: incorrect format of input data",
  "errorTypeId": "3",
  "code": "30021",
  "message": "Key size must be > 0 and <= 8192.",
  "status": "fail"
}
{
  "errorType": "Native code fail: incorrect format of input data",
  "errorTypeId": "3",
  "code": "30022",
  "message": "Length of new key must be equal to length of old key ",
  "status": "fail"
}
{
  "errorType": "Native code fail: incorrect format of input data",
  "errorTypeId": "3",
  "code": "30023",
  "message": "Key index is a numeric string representing integer >= 0 and <= 1022.",
  "status": "fail"
}
{
  "errorType": "Native code fail: incorrect format of input data",
  "errorTypeId": "3",
  "code": "30024",
  "message": "Key index is not a valid numeric string.",
  "status": "fail"
}
{
  "errorType": "Native code fail: incorrect format of input data",
  "errorTypeId": "3",
  "code": "30025",
  "message": "Serial number is a numeric string of length 24.",
  "status": "fail"
}
{
  "errorType": "Native code fail: incorrect format of input data",
  "errorTypeId": "3",
  "code": "30026",
  "message": "Serial number is not a valid numeric string.",
  "status": "fail"
}
```

## CARD_RESPONSE_DATA_ERRORS

This sublist of errors is about additional checking of data that comes into Android from the card. We check all responses from card: i.e. their formats, lengthes, ranges in some cases. Normally errors of such type must not happen. Please report if you would get one of them.

```json
{
  "code": "40000",
  "errorType": "Native code fail: incorrect response from card",
  "errorTypeId": "4",
  "message": "Sault response from card must have length 32. Current length is ",
  "status": "fail"
}
{
  "code": "40001",
  "errorType": "Native code fail: incorrect response from card",
  "errorTypeId": "4",
  "message": "Applet state response from card must have length 1.",
  "status": "fail"
}
{
  "code": "40002",
  "errorType": "Native code fail: incorrect response from card",
  "errorTypeId": "4",
  "message": "Unknown applet state = ",
  "status": "fail"
}
{
  "code": "40003",
  "errorType": "Native code fail: incorrect response from card",
  "errorTypeId": "4",
  "message": "Recovery data hash must have length 32.",
  "status": "fail"
}
{
  "code": "40004",
  "errorType": "Native code fail: incorrect response from card",
  "errorTypeId": "4",
  "message": "Recovery data length byte array must have length 2.",
  "status": "fail"
}
{
  "code": "40005",
  "errorType": "Native code fail: incorrect response from card",
  "errorTypeId": "4",
  "message": "Recovery data length must be > 0 and <= 2048.",
  "status": "fail"
}
{
  "code": "40006",
  "errorType": "Native code fail: incorrect response from card",
  "errorTypeId": "4",
  "message": "Response from IS_RECOVERY_DATA_SET card operation must have length 1.",
  "status": "fail"
}
{
  "code": "40007",
  "errorType": "Native code fail: incorrect response from card",
  "errorTypeId": "4",
  "message": "Recovery data portion must have length = ",
  "status": "fail"
}
{
  "code": "40008",
  "errorType": "Native code fail: incorrect response from card",
  "errorTypeId": "4",
  "message": "Hash of encrypted password must have length 32.",
  "status": "fail"
}
{
  "code": "40009",
  "errorType": "Native code fail: incorrect response from card",
  "errorTypeId": "4",
  "message": "Hash of encrypted common secret must have length 32.",
  "status": "fail"
}
{
  "code": "40010",
  "errorType": "Native code fail: incorrect response from card",
  "errorTypeId": "4",
  "message": "Card two-factor authentication failed: Hash of encrypted common secret is invalid.",
  "status": "fail"
}
{
  "code": "40011",
  "errorType": "Native code fail: incorrect response from card",
  "errorTypeId": "4",
  "message": "Card two-factor authentication failed: Hash of encrypted password is invalid.",
  "status": "fail"
}
{
  "code": "40012",
  "errorType": "Native code fail: incorrect response from card",
  "errorTypeId": "4",
  "message": "Signature must have length 64.",
  "status": "fail"
}
{
  "code": "40013",
  "errorType": "Native code fail: incorrect response from card",
  "errorTypeId": "4",
  "message": "Public key must have length 32.",
  "status": "fail"
}
{
  "code": "40014",
  "errorType": "Native code fail: incorrect response from card",
  "errorTypeId": "4",
  "message": "Response from GET_NUMBER_OF_KEYS card operation must have length 2.",
  "status": "fail"
}
{
  "code": "40015",
  "errorType": "Native code fail: incorrect response from card",
  "errorTypeId": "4",
  "message": "Number of keys in keychain must be >= 0 and <= 1023.",
  "status": "fail"
}
{
  "code": "40016",
  "errorType": "Native code fail: incorrect response from card",
  "errorTypeId": "4",
  "message": "Response from GET_OCCUPIED_SIZE card operation must have length 2.",
  "status": "fail"
}
{
  "code": "40017",
  "errorType": "Native code fail: incorrect response from card",
  "errorTypeId": "4",
  "message": "Response from GET_FREE_SIZE_RESPONSE card operation must have length 2.",
  "status": "fail"
}
{
  "code": "40018",
  "errorType": "Native code fail: incorrect response from card",
  "errorTypeId": "4",
  "message": "Occupied size in keychain can not be negative.",
  "status": "fail"
}
{
  "code": "40019",
  "errorType": "Native code fail: incorrect response from card",
  "errorTypeId": "4",
  "message": "Free size in keychain can not be negative.",
  "status": "fail"
}
{
  "code": "40020",
  "errorType": "Native code fail: incorrect response from card",
  "errorTypeId": "4",
  "message": "Response from GET_KEY_INDEX_IN_STORAGE_AND_LEN card operation must have length 4.",
  "status": "fail"
}
{
  "code": "40021",
  "errorType": "Native code fail: incorrect response from card",
  "errorTypeId": "4",
  "message": "Key index must be >= 0 and <= 1022.",
  "status": "fail"
}
{
  "code": "40022",
  "errorType": "Native code fail: incorrect response from card",
  "errorTypeId": "4",
  "message": "Key length (in keychain) must be > 0 and <= 8192.",
  "status": "fail"
}
{
  "code": "40023",
  "errorType": "Native code fail: incorrect response from card",
  "errorTypeId": "4",
  "message": "Response from DELETE_KEY_CHUNK card operation must have length 1.",
  "status": "fail"
}
{
  "code": "40024",
  "errorType": "Native code fail: incorrect response from card",
  "errorTypeId": "4",
  "message": "Response from DELETE_KEY_CHUNK card operation must have value 0 or 1.",
  "status": "fail"
}
{
  "code": "40025",
  "errorType": "Native code fail: incorrect response from card",
  "errorTypeId": "4",
  "message": "Response from DELETE_KEY_RECORD card operation must have length 1.",
  "status": "fail"
}
{
  "code": "40026",
  "errorType": "Native code fail: incorrect response from card",
  "errorTypeId": "4",
  "message": "Response from DELETE_KEY_RECORD card operation must have value 0 or 1.",
  "status": "fail"
}
{
  "code": "40027",
  "errorType": "Native code fail: incorrect response from card",
  "errorTypeId": "4",
  "message": "Response from GET_DELETE_KEY_CHUNK_NUM_OF_PACKETS card operation must have length 2.",
  "status": "fail"
}
{
  "code": "40028",
  "errorType": "Native code fail: incorrect response from card",
  "errorTypeId": "4",
  "message": "Response from GET_DELETE_KEY_CHUNK_NUM_OF_PACKETS card operation can not be negative.",
  "status": "fail"
}
{
  "code": "40029",
  "errorType": "Native code fail: incorrect response from card",
  "errorTypeId": "4",
  "message": "Response from GET_DELETE_KEY_RECORD card operation must have length 2.",
  "status": "fail"
}
{
  "code": "40030",
  "errorType": "Native code fail: incorrect response from card",
  "errorTypeId": "4",
  "message": "Response from GET_DELETE_KEY_RECORD_NUM_OF_PACKETS card operation can not be negative.",
  "status": "fail"
}
{
  "code": "40031",
  "errorType": "Native code fail: incorrect response from card",
  "errorTypeId": "4",
  "message": "After ADD_KEY card operation number of keys must be increased by 1.",
  "status": "fail"
}
{
  "code": "40032",
  "errorType": "Native code fail: incorrect response from card",
  "errorTypeId": "4",
  "message": "After CHANGE_KEY card operation number of keys must not be changed.",
  "status": "fail"
}
{
  "code": "40033",
  "errorType": "Native code fail: incorrect response from card",
  "errorTypeId": "4",
  "message": "Response from SEND_CHUNK card operation must have length 2.",
  "status": "fail"
}
{
  "code": "40034",
  "errorType": "Native code fail: incorrect response from card",
  "errorTypeId": "4",
  "message": "Hash of key (from keychain) must have length 32.",
  "status": "fail"
}
{
  "code": "40035",
  "errorType": "Native code fail: incorrect response from card",
  "errorTypeId": "4",
  "message": "Response from INITIATE_DELETE_KEY card operation must have length 2.",
  "status": "fail"
}
{
  "code": "40036",
  "errorType": "Native code fail: incorrect response from card",
  "errorTypeId": "4",
  "message": "Key data portion must have length = ",
  "status": "fail"
}
{
  "code": "40037",
  "errorType": "Native code fail: incorrect response from card",
  "errorTypeId": "4",
  "message": "Serial number must have length 24.",
  "status": "fail"
}
{
  "code": "40038",
  "errorType": "Native code fail: incorrect response from card",
  "errorTypeId": "4",
  "message": "Response from GET_PIN_TLT (GET_PIN_RTL) must have length > 0.",
  "status": "fail"
}
{
  "code": "40039",
  "errorType": "Native code fail: incorrect response from card",
  "errorTypeId": "4",
  "message": "Response from GET_PIN_TLT (GET_PIN_RTL) must have value >= 0 and <= 10.",
  "status": "fail"
}
{
  "code": "40040",
  "errorType": "Native code fail: incorrect response from card",
  "errorTypeId": "4",
  "message": "Response from GET_ROOT_KEY_STATUS must have length > 0.",
  "status": "fail"
}
{
  "code": "40041",
  "errorType": "Native code fail: incorrect response from card",
  "errorTypeId": "4",
  "message": "Response from GET_DEVICE_LABEL_APDU must have length = 32.",
  "status": "fail"
}
{
  "code": "40042",
  "errorType": "Native code fail: incorrect response from card",
  "errorTypeId": "4",
  "message": "Response from GET_CSN_VERSION must have length > 0.",
  "status": "fail"
}
{
  "code": "40043",
  "errorType": "Native code fail: incorrect response from card",
  "errorTypeId": "4",
  "message": "Response from GET_SE_VERSION_APDU must have length > 0.",
  "status": "fail"
}
{
  "code": "40044",
  "errorType": "Native code fail: incorrect response from card",
  "errorTypeId": "4",
  "message": "Response from GET_AVAILABLE_MEMORY must have length > 0.",
  "status": "fail"
}
{
  "code": "40045",
  "errorType": "Native code fail: incorrect response from card",
  "errorTypeId": "4",
  "message": "Response from GET_APPLET_LIST must have length > 0.",
  "status": "fail"
}
```

## IMPROPER_APPLET_STATE_ERROR

Before sending some APDU command into applet Android code usually checks applet state. If in current applet state this APDU is not supported, then Android code throws a error and does not even try to send this APDU into applet (But If it would send it then card will produce _6D00_ error).

```json
{
"errorType": "Native code fail: improper applet state",
"errorTypeId": "5",
"code": "50000",
"message": "APDU command is not supported",
"status": "fail"
}

{
"errorType": "Native code fail: improper applet state",
"errorTypeId": "5",
"code": "50001",
"message": "Applet must be in mode that waits authentication. Now it is: ",
"status": "fail"
}

{
"errorType": "Native code fail: improper applet state",
"errorTypeId": "5",
"code": "50002",
"message": "Applet must be in personalized mode. Now it is: ",
"status": "fail"
}

{
"errorType": "Native code fail: improper applet state",
"errorTypeId": "5",
"code": "50003",
"message": "Applet must be in mode for deleting key. Now it is ",
"status": "fail"
}
```

## ANDROID_HMAC_KEY_ERROR

Here there is a list of possible errors that can happen during work with hmac keys living in Android Keystore.

```json
{
  "code": "80000",
  "errorType": "Native code (Android) fail: hmac key issue",
  "errorTypeId": "8",
  "message": "Key for hmac signing for specified serial number does not exist.",
  "status": "fail"
}
{
  "code": "80001",
  "errorType": "Native code (Android) fail: hmac key issue",
  "errorTypeId": "8",
  "message": "Current serial number is not set. Can not select key for hmac.",
  "status": "fail"
}
```

## WRONG_CARD_ERROR

```json
{
"errorType": "Native code fail: wrong card",
"errorTypeId": "7",
"code": "70000",
"message": "You try to use security card with incorrect serial number.",
"status": "fail"
}
```
