This specification describes all TON Wallet applet modes and APDU commands available in each mode. It provides working examples of APDU commands scripts.

TON Wallet applet may be in one of the following states (modes):

- Personalization (APP_INSTALLED = 0x07);
- Waiting for authorization (APP_WAITE_AUTHORIZATION_MODE = 0x27);
- Main working mode (APP_PERSONALIZED = 0x17);
- Blocked mode (APP_BLOCKED_MODE = 0x47);
- Delete key from keychain mode (APP_DELETE_KEY_FROM_KEYCHAIN_MODE = (byte) 0x37).

## APDU notations

We are using the following notation:

CLA = APDU command class

INS = APDU command type

P1= first param of APDU

P2= second param of APDU

LC = length of input data for APDU command

LE = length of response data array for APDU command.

| means concatenation.

Each APDU command field (CLA, INS, P1, P2, Lc and Le)  has a size = 1 byte except of Data.

APDU command may have one of the following format:

CLA | INS | P1 | P2 

CLA | INS | P1 | P2 | LC | Data

CLA | INS | P1 | P2 | LC | Data | LE

CLA | INS | P1 | P2 | LE

## APP_INSTALLED state/Personalization

After applet loading and installation on the card it will be in mode APP_INSTALLED. It will wait for personalization. Personalization will be done at factory. The following APDU commands will be available.

**Note**: After personalization is done applet state is switched into APP_WAITE_AUTHORIZATION_MODE. And this transition is irreversible. So the end user will not ever get the card with applet in this state.

- **SET_SERIAL_NUMBER**

    ***APDU input params:***

    CLA: 0xB0

    INS: 0x96

    P1: 0x00

    P2: 0x00

    LC: 0x18

    Data: Bytes of serial number

    ***APDU response status msg:***

    9000 — success

    *Incorrect APDU data errors:*

    6700 (Wrong length) — Length of APDU Data field ≠ 24.

    A002 — Each input byte of SN must contain value ≥ 0 and ≤ 9.

- **SET_ENCRYPTED_PASSWORD_FOR_CARD_AUTHENTICATION**

    ***APDU input params:***

    CLA: 0xB0

    INS: 0x91

    P1: 0x00

    P2: 0x00

    LC: 0x80

    Data: Bytes of encrypted activation password

    ***APDU response status msg:***

    9000 — success

    *Incorrect APDU data errors:*

    6700 (Wrong length) — Length of APDU Data field ≠ 128.

- **SET_ENCRYPTED_COMMON_SECRET**

    ***APDU input params:***

    CLA: 0xB0

    INS: 0x94

    P1: 0x00

    P2: 0x00

    LC: 0x20

    Data: Bytes of encrypted common secret

    ***APDU response status msg:***

    9000 — success

    *Incorrect APDU data errors:*

    6700 (Wrong length) — Length of APDU Data field ≠ 32.

- **GET_HASH_OF_ENCRYPTED_PASSWORD**

    ***APDU input params:***

    CLA: 0xB0

    INS: 0x93

    P1: 0x00

    P2: 0x00

    LE: 0x20

    ***APDU response status msg:***

    9000 — success

    *Incorrect APDU data errors:*

    6700 (Wrong length) — LE ≠ 32.

    ***APDU response data:*** 

    32 bytes of SHA256(encrypted activation password)

- **GET_HASH_OF_COMMON_SECRET**

    ***APDU input params:***

    CLA: 0xB0

    INS: 0x95

    P1: 0x00

    P2: 0x00

    LE: 0x20

    ***APDU response status msg:***

    9000 — success

    ***APDU response data:*** 

    32 bytes of SHA256(common secret)

- **FINISH_PERS**

    This command finishes personalization and changes the state of applet. APP_WAITE_AUTHORIZATION_MODE state will be switched on .

    *Precondition:*  SET_PASSWORD_FOR_CARD_AUTHENTICATION and SET_COMMON_SECRE**T** should be called before, otherwise exception will be thrown.

    ***APDU input params:***

    CLA: 0xB0

    INS: 0x90

    P1: 0x00

    P2: 0x00

    ***APDU response status msg:***

    9000 — success

    *Protocols violation errors:*

    4F01 — personalization is not finished: encrypted activation password or common secret were not set successfully by corresponding APDU commands.

- **GET_APP_INFO**

    This command returns applet state.

    ***APDU input params:***

    CLA: 0xB0

    INS: 0xC1

    P1: 0x00

    P2: 0x00

    LE: 0x01

    ***APDU response status msg:***

    9000 — success

    *Incorrect APDU data errors:*

    6700 (Wrong length) — LE ≠ 1.

    ***APDU response data:*** 

    1 byte (Applet state = 0x07, 0x17, 0x27, 0x37 or 0x47)

- **INS_GET_SERIAL_NUMBER**

    This command returns card serial number bytes.

    *APDU input params:*

    CLA: 0xB0

    INS: 0x80

    P1: 0xC2

    P2: 0x00

    LE: 0x18

    *APDU response status msg:*

    9000 — success

    *Incorrect APDU data errors:*

    6700 (Wrong length) — LE ≠ 24.

    *Protocols violation errors:*

    A001 — Serial number is not set.

    *APDU response data:* 

    24 bytes digital byte array containing serial number
    
    - **GET_HASH_OF_ENCRYPTED_COMMON_SECRET**

    ***APDU input params:***

    CLA: 0xB0

    INS: 0x95

    P1: 0x00

    P2: 0x00

    LE: 0x20

    ***APDU response status msg:***

    9000 — success

    *Incorrect APDU data errors:*

    6700 (Wrong length) — LE ≠ 32.

    ***APDU response data:*** 

    32 bytes of SHA256(common secret)
    
## APP_WAITE_AUTHORIZATION_MODE state/Applet authorization

After finishing the production applet will be in APP_WAITE_AUTHORIZATION_MODE. After getting the device the end user should complete the procedure of two-factor authorization to make applet working. For this he must know unencrypted activation password.

The following APDU commands will be available here.   

- **VERIFY_PASSWORD**

    ***APDU input params:***

    CLA: 0xB0

    INS: 0x92

    P1: 0x00

    P2: 0x00

    LC: 0x90

    Data: 128 bytes of unencrypted activation password | 16 bytes of IV for AES128 CBC

    ***APDU response status msg:***

    9000 — success, in this case applet state  APP_PERSONALIZED is set up and key for HMAC signature is produced based on common secret and sha256(unencrypted activation password ).

    *Incorrect APDU data errors:*

    6700 (Wrong length) — Length of input APDU Data field  ≠ 144.

    *Unauthorized access errors*

    5f00 — Incorrect password for card authentication.

    5f01 — Incorrect password, card is locked. This error is thrown after 20 successive fails to verify password. Before throwing this error applet state  APP_BLOCKED_MODE is set up.
