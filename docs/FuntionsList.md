# Full functions list

Here there is full functions list provided by TonNfcClientAndroid library to make different requests to NFC TON Labs Security cards. 

In [readme](https://github.com/tonlabs/TonNfcClientAndroid/blob/master/README.md) (see section about NfcCallback) we said that for each card operation there was a pair of functions. One of them puts result/error message into callback, the second does not. For example there is getSerialNumberAndGetJson() function returning json string and getSerialNumber(NfcCallback callback) returning void and putting the same json into callback. They do the same work. So for short we just give the full list of functions omitting "AndGetJson" suffix and argument NfcCallback callback. We provide only essential information about input data format requirements and possible responses.

## NFC related functions

Here there are functions to check/change the state of your NFC hardware.  In TonNfcClientAndroid library there is a class NfcApi for this.

- **checkIfNfcSupported()**

    Check if your Android device has NFC hardware. 

    *Responses:*

    {"message":"true","status":"ok"}

    {"message":"false","status":"ok"}

- **checkIfNfcEnabled()**

    Check if NFC option is turned on for your Android device.

    *Responses:*

    {"message":"true","status":"ok"}

    {"message":"false","status":"ok"}

- **openNfcSettings()**

    Open "Settings" panel to mantain NFC option.

    *Response:*

    {"message":"done","status":"ok"}
    
## CoinManager functions

Here there are functions to call APDU commands of CoinManager. CoinManager is an additional software integrated into NFC TON Labs Security card. It is responsible for maintaining ed25519 seed, related PIN and it provides some other auxiliary operations.  In TonNfcClientAndroid library there is a class CardCoinManagerApi providing all CoinManager functions.

- **setDeviceLabel(String deviceLabel)**

    This function is used to set the device label. Now we do not use this device label stored in Coin Manager.

    *Arguments requirements:*

    1) label — hex string of length 64, example: '005815A3942073A6ADC70C035780FDD09DF09AFEEA4173B92FE559C34DCA0550'

    *Response:*

    {"message":"done","status":"ok"}

- **getDeviceLabel()**

    This function is used to get device label. Now we do not use this device label stored in Coin Manager.

    *Exemplary response:*

    {"message":"005815A3942073A6ADC70C035780FDD09DF09AFEEA4173B92FE559C34DCA0550","status":"ok"}

- **getSeVersion()**

    This function is used to get SE (secure element) version. 

    *Response:*

    {"message":"1008","status":"ok"}

- **getCsn()**

    This function is used to get CSN (SEID).

    *Exemplary response:*

    {"message":"11223344556677881122334455667788","status":"ok"}

- **getMaxPinTries()**

    This function is used to get retry maximum times of PIN. 

    *Response:*

    {"message":"10","status":"ok"}

- **getRemainingPinTries()**

    This function is used to get remaining retry times of PIN.

    *Exemplary response:*

    {"message":"10","status":"ok"}

- **getRootKeyStatus()**

    This function is used to get the status of seed for ed25519: is it generated or not.

    *Response:*

    a) If seed is present: {"message":"generated","status":"ok"}

    b) If seed is not present: {"message":"not generated","status":"ok"}

- **resetWallet()**

    This function is used to reset the wallet state to the initial state. After resetting the wallet, the default PIN value would be 5555. The remaining retry for the PIN will be reset to MAX (default is 10). The seed for ed25519 will be erased. And after its calling any card operation (except of CoinManager stuff) will fail with 6F02 error. TON Labs wallet applet does not work without seed.

    *Response:*

    {"message":"done","status":"ok"}

- **getAvailableMemory()**

    This function is used to obtain the amount of memory of the specified type that is available to the applet. Note that implementation-dependent memory overhead structures may also use the same memory pool.

- **getAppsList()**

    This function is used to get application list. It returns list of applets AIDs that were installed onto card.

    *Exemplary response:*

    {"message":"4F0D31313232333334343535363600","status":"ok"}

    Note: Here 313132323333343435353636 is AID of our TON Labs wallet applet

- **generateSeed(String pin)**

    This function is used to generate the seed for ed25519 with RNG.

    *Arguments requirements:*

    pin — numeric string of length 4, example: '5555'

    By the way 5555 is a default PIN for all cards. 

    *Response:*

    If seed does not exist then: {"message":"done","status":"ok"}

    If seed already exists and you call generateSeed then it will throw a error.

- **changePin(String oldPin, String newPin)**

    This function is used to change device PIN.

    *Arguments requirements:*

    oldPin — numeric string of length 4, example: '5555'

    newPin — numeric string of length 4, example: '6666'

    *Response:*

    {"message":"done","status":"ok"}

## Common functions

- getTonAppletState()

    This function returns state of TON Wallet applet.

    *Exemplary responses:*

    {"message":"TonWalletApplet waits two-factor authorization.","status":"ok"}

    {"message":"TonWalletApplet is personalized.","status":"ok"}

- getSerialNumber()

    This function returns serial number that was saved into TON Wallet applet during personalization step by Feitian. It must be identical to SN printed on the card.

    *Exemplary response:*

    {"message":"504394802433901126813236","status":"ok"}

- getSault()

- selectKeyForHmac(serialNumber)

    Manually select new active card.

    *Arguments requirements:*

    serialNumber — numeric string of length 24, example: "50439480243390112681323"

    *Response:*

    {"message":"done","status":"ok"}

- createKeyForHmac(authenticationPassword, commonSecret, serialNumber)

    If you reinstalled app and lost HMAC SHA256 symmetric key for the card from your Android keystore/iOS keychain, then create the key for your card using this function.

    *Arguments requirements:*

     authenticationPassword — hex string of length 256, example: "4A0FD62FFC3249A45ED369BD9B9CB340829179E94B8BE546FB19A1BC67C9411BC5DC85B5E38F96689B921A64DEF1A3B6F4D2F5C7D2B0BD7CCE420DBD281BA1CC82EE0B233820EB5CFE505B7201903ABB12959B251A5A8525B2515F57ACDE30905E70C2A375D5C0EC10A5EA6E264206395BF163969632398FA4A88D359FEA21D9"

    commonSecret — hex string of length 64, example: "9CEE28E284487EEB8FA6CE7C101C1184BB368F0CCAD057C9D89F7EC3307E72BA"

    serialNumber — numeric string of length 24, example: "50439480243390112681323"

    *Note 1:* Use here activation data tuple  (authenticationPassword, commonSecret) that is correct for your card, i.e. corresponds to your serialNumber.

    *Note 2:* If the key for your card already exists in keystore it will not throw a error. It will just delete and recreate the key for you.

    *Response:*

    {"message":"done","status":"ok"}

- getCurrentSerialNumber()

    Get serial number of currently active key (card). In fact this is a serialNumber of the card with which your app communicated last time.

    *Exemplary response:*

    {"message":"504394802433901126813236","status":"ok"}

- getAllSerialNumbers()

    Get the list of card serial numbers for wich we have keys in Android keystore/iOS keychain.

    *Exemplary response:*

    {"serial_number_field":["504394802433901126813236", "455324585319848551839771"],"status":"ok"}

- isKeyForHmacExist(serialNumber)

    Check if key for given serialNumber lives in Android keystore/iOS keychain.

    *Arguments requirements:*

    serialNumber — numeric string of length 24, example: "50439480243390112681323"

    *Exemplary response:*

    {"message":"true","status":"ok"}

- deleteKeyForHmac(serialNumber)

    Delete key for given serialNumber from Android keystore/iOS keychain.

    *Arguments requirements:*

    serialNumber — numeric string of length 24, example: "50439480243390112681323"

    *Response:*

    {"message":"done","status":"ok"}

- isNfcSupported()

    Check if your smartphone/iPhone has NFC hardware. 

    *Responses:*

    {"message":"true","status":"ok"}

    {"message":"false","status":"ok"}

- isNfcEnabled()

    Check if your  NFC option is turned on for your smartphone.

    *Responses:*

    {"message":"true","status":"ok"}

    {"message":"false","status":"ok"}

- openNfcSettings()

    Open "Settings" panel to mantain NFC option.

- disconnectCard()

    Interrupt  NFC connection.
    
    
## Functions available during card activation step    

When user gets NFC TON Labs security card  at the first time, the applet on the card is in a special state. It waits for user authentication. And the main functionality of applet is blocked for now. At this point you may call all functions from previous section. 

And also some special functions are available in CardActivationApi. They are necessary to complete card activation (see Card activation section).

- turnOnWallet(newPin, authenticationPassword, commonSecret, initialVector)

    This function makes TON Wallet applet activation. After its succesfull call applet will be in working personalized state (so getTonAppletState() will return {"message":"TonWalletApplet is personalized.","status":"ok"}).

    *Arguments requirements:*

    newPin — numeric string of length 4, example: '6666'

     authenticationPassword — hex string of length 256, example: "4A0FD62FFC3249A45ED369BD9B9CB340829179E94B8BE546FB19A1BC67C9411BC5DC85B5E38F96689B921A64DEF1A3B6F4D2F5C7D2B0BD7CCE420DBD281BA1CC82EE0B233820EB5CFE505B7201903ABB12959B251A5A8525B2515F57ACDE30905E70C2A375D5C0EC10A5EA6E264206395BF163969632398FA4A88D359FEA21D9"

    commonSecret — hex string of length 64, example: "9CEE28E284487EEB8FA6CE7C101C1184BB368F0CCAD057C9D89F7EC3307E72BA"

    initialVector — hex string of length 32, example: "E439F75C6FC516F1C4725E825164216C"

    Note: Use here activation data tuple  (authenticationPassword, commonSecret, initialVector) that is correct for your card, i.e. corresponds to your serialNumber.

    *Response:*

    {"message":"done","status":"ok"}

- getHashOfEncryptedCommonSecret()

    Return SHA256 hash of encrypted common secret.

    *Exemplary response:*

    {"message":"EFBF24AC1563B34ADB0FFE0B0A53659E72E26765704C109C95346EEAA1D4BEAF","status":"ok"}

- getHashOfEncryptedPassword()

    Return SHA256 hash of encrypted password.

    *Exemplary responses:*

    {"message":"26D4B03C0C0E168DC33E48BBCEB457C21364658C9D487341827BBFFB4D8B38F3","status":"ok"}
    
    
## Functions that work for personalized Ton Wallet applet    
	
**Functions related to the stuff for recovery service:**

- getRecoveryDataLen()

    Read actual recovery data length.

    *Exemplary response:* 

    {"message":"7","status":"ok"}

- getRecoveryData()

    Read  recovery data from TON Wallet applet.

    *Exemplary response:* 

    {"message":"00112233445566","status":"ok"}

- addRecoveryData(recoveryData)

    Save recovery data into applet. 

    *Arguments requirements:*

    recoveryData — hex string of even length ≥ 2 and ≤ 2048

    *Response:*

    {"message":"done","status":"ok"}

- isRecoveryDataSet()

    Return 'true'/'false' if recovery data exists/does not exist.

    *Response:*

    1) If we added recovery data, then: {"message":"true","status":"ok"}

    2) If we did not add recovery data, then: {"message":"false","status":"ok"}

- resetRecoveryData()

    Clear recovery data.

    *Response:*

    {"message":"done","status":"ok"}	
    
    **Functions related to ed25519 signature:**

- getPublicKeyForDefaultPath()

    Return public key for HD path m/44'/396'/0'/0'/0'

    *Exemplary response:*

    {"message":"B81F0E0E07316DAB6C320ECC6BF3DBA48A70101C5251CC31B1D8F831B36E9F2A","status":"ok"}

- verifyPin(pin)

    Make pin verification.

    *Arguments requirements:*

    pin — numeric string of length 4, example: '5555'

    *Response:*

    {"message":"done","status":"ok"}

- signForDefaultHdPath(dataForSigning, pin)

    Make pin verification and data signing by key for HD path m/44'/396'/0'/0'/0'.

    *Arguments requirements:*

    pin — numeric string of length 4, example: '5555'

    data — hex string of even length ≥ 2 and ≤ 378.

    *Exemplary response:*

    {"message":"2D6A2749DD5AF5BB356220BFA06A0C624D5814438F37983322BBAD762EFB4759CFA927E6735B7CD556196894F3CE077ADDD6B49447B8B325ADC494B82DC8B605","status":"ok"}

- sign(dataForSigning, hdIndex, pin)

    Make pin verification and data signing by key for HD path m/44'/396'/0'/0'/hdIndex'.

    *Arguments requirements:*

    pin — numeric string of length 4, example: '5555'

    hdIndex — numeric string of length > 0 and ≤ 10.

    data — hex string of even length ≥ 2 and ≤ 356.

    *Exemplary response:*

    {"message":"13FB836213B12BBD41209273F81BCDCF7C226947B18128F73E9A6E96C84B30C3288E51C622C045488981B6544D02D0940DE54D68A0A78BC2A5F9523B8757B904","status":"ok"}

- getPublicKey(hdIndex)

    Return public key for HD path m/44'/396'/0'/0'/hdIndex'.

    *Arguments requirements:*

    hdIndex — numeric string of length > 0 and ≤ 10.

    *Exemplary response:*

    {"message":"B81F0E0E07316DAB6C320ECC6BF3DBA48A70101C5251CC31B1D8F831B36E9F2A","status":"ok"}
    
    **Functions related to Card Keychain:**
    
    Inside TON Wallet applet we implemented small flexible independent keychain. It allows to store some user's keys and secrets.

- resetKeyChain()

    Clear keychain, i.e. remove all stored keys.

    *Response:*

    {"message":"done","status":"ok"}

- getKeyChainDataAboutAllKeys()

    Return list of pairs (keyHmac, keyLength)  in json format.

    *Exemplary response:*

- getKeyChainInfo()

    Return json characterizing the state of keychain. 

    *Exemplary response:*

    {"numberOfKeys":0,"occupiedSize":0,"freeSize":32767,"status":"ok"}

- getNumberOfKeys()

    Return number of keys in card keychain.

    *Exemplary response:*

    {"message":"1","status":"ok"}

- getOccupiedStorageSize()

    Return the volume of occupied size in card keychain (in bytes).

    *Exemplary response:*

    {"message":"0","status":"ok"}

- getFreeStorageSize()

    Return the volume of free size in card keychain (in bytes).

    *Exemplary response:*

    {"message":"32767","status":"ok"}

- getKeyFromKeyChain(keyHmac)

    Read key from card keychain based on its hmac.

    *Arguments requirements:*

    keyHmac — hex string of length 64.

    *Exemplary response:*

    {"message":"001122334455","status":"ok"}

- addKeyIntoKeyChain(newKey)

    Save new key into card keychain.

    *Arguments requirements:*

    neyKey — hex string of even length ≥ 2 and ≤ 16384.

    *Response:*

    {"message":"EFBF24AC1563B34ADB0FFE0B0A53659E72E26765704C109C95346EEAA1D4BEAF","status":"ok"}

    where "message" contains hmac of newKey.

- deleteKeyFromKeyChain(keyHmac)

    Delete key from card keychain based on its hmac.

    *Arguments requirements:*

    keyHmac — hex string of length 64.

    *Exemplary response:*

    {"message":"5","status":"ok"}

    where "message" field contains the number of remaining keys

- finishDeleteKeyFromKeyChainAfterInterruption()

    Finish the process of deleting key from card keychain. It may be necessary if previous DELETE operation was occassionally interrupted.

    *Exemplary response:*

    {"message":"5","status":"ok"}

    where "message" field contains the number of remaining keys

- changeKeyInKeyChain(newKey, oldKeyHmac)

    Replace existing key by  new key. The length of new key must be equal to length of old key.

    *Arguments requirements:*

    newKey — hex string of even length ≥ 2 and ≤ 16384. 

    oldKeyHmac — hex string of length 64.

    *Response:*

    {"message":"EFBF24AC1563B34ADB0FFE0B0A53659E72E26765704C109C95346EEAA1D4BEAF","status":"ok"}

    where "message" contains hmac of newKey.

- getIndexAndLenOfKeyInKeyChain(keyHmac)

    Read index (inside internal applet storage) and length of key by its hmac.

    *Arguments requirements:*

    keyHmac — hex string of length 64.

- checkAvailableVolForNewKey(keySize)

    Check if there is enough free volume in card keychain to add new key of length = keySize

    *Arguments requirements:*

    keySize — numeric string representing short value > 0 and ≤ 8192.

    *Response:*

    {"message":"done","status":"ok"}

- checkKeyHmacConsistency(keyHmac)

    *Response:*

    {"message":"done","status":"ok"}

- getHmac(index)

    Get hmac of key in card keychain by its index. 

    *Arguments requirements:*

    index — numerical string storing a number ≥ 0 and ≤1023.

    *Exemplary response:*

    {"message":"EFBF24AC1563B34ADB0FFE0B0A53659E72E26765704C109C95346EEAA1D4BEAF","status":"ok"}

- getDeleteKeyRecordNumOfPackets

- getDeleteKeyChunkNumOfPackets
