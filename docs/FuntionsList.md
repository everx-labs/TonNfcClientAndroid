# Full functions list

Here there is full functions list provided by TonNfcClientAndroid library to make different requests to NFC TON Labs Security cards. 

In [readme](https://github.com/tonlabs/TonNfcClientAndroid/blob/master/README.md) (see section about NfcCallback) we said that for each card operation there was a pair of functions. One of them puts result/error message into callback. The second just returns the result/ throws an exception. For example, there is getSerialNumberAndGetJson() function returning json string and getSerialNumber(NfcCallback callback, Boolean... showDialog) returning void and putting the same json into callback. They do the same work.

**Note:** All functions working with NfcCallback create their separate thread for card operation via AsyncTask. Whereas functions returning json string do not create their own thread. So if one does not want to block UI, then they should be called in new thread.

The majority of functions below has the last input parameter 'Boolean... showDialog'. It indicates if one wants to show invitation dialog for NFC card connection. If this parameter is not specified at all, the dialog will not be shown by default.

## NFC related functions

Here there are functions to check/change the state of your NFC hardware.  In TonNfcClientAndroid library there is a class NfcApi for this.

- **void checkIfNfcSupported(final NfcCallback callback),** <br/>
  **String checkIfNfcSupportedAndGetJson()**

    Check if your Android device has NFC hardware. 

    *Responses:*
            
        {"message":"true","status":"ok"}
        {"message":"false","status":"ok"}

- **void checkIfNfcEnabled(final NfcCallback callback),** <br/>
  **String checkIfNfcEnabledAndGetJson(),** 

    Check if NFC option is turned on for your Android device.

    *Responses:*

        {"message":"true","status":"ok"}
        {"message":"false","status":"ok"}

- **void openNfcSettings(final NfcCallback callback),** <br/>
  **String openNfcSettingsAndGetJson()**

    Open "Settings" panel to mantain NFC option.

    *Response:*

        {"message":"done","status":"ok"}
    
## CoinManager functions

Here there are functions to call APDU commands of CoinManager. CoinManager is an additional software integrated into NFC TON Labs Security card. It is responsible for maintaining ed25519 seed, related PIN and it provides some auxiliary operations.  In TonNfcClientAndroid library there is a class CardCoinManagerApi providing all CoinManager functions.

- **void setDeviceLabel(final String deviceLabel, final NfcCallback callback, Boolean... showDialog),** <br/>
  **String setDeviceLabelAndGetJson(final String deviceLabel, Boolean... showDialog)**

    This function is used to set the device label. Now we do not use this device label stored in CoinManager.

    *Arguments requirements:*

        deviceLabel — hex string of length 64, example: '005815A3942073A6ADC70C035780FDD09DF09AFEEA4173B92FE559C34DCA0550'

    *Response:*

        {"message":"done","status":"ok"}

- **void getDeviceLabel(final NfcCallback callback, Boolean... showDialog),** <br/>
  **String getDeviceLabelAndGetJson(Boolean... showDialog)**

    This function is used to get device label. Now we do not use this device label stored in CoinManager.

    *Exemplary response:*

        {"message":"005815A3942073A6ADC70C035780FDD09DF09AFEEA4173B92FE559C34DCA0550","status":"ok"}

- **void getSeVersion(final NfcCallback callback, Boolean... showDialog),** <br/>
  **String getSeVersionAndGetJson(Boolean... showDialog)**

    This function is used to get SE (secure element) version. 

    *Response:*

        {"message":"1008","status":"ok"}

- **void getCsn(final NfcCallback callback, Boolean... showDialog),** <br/>
  **String getCsnAndGetJson(Boolean... showDialog)**

    This function is used to get CSN (SEID).

    *Exemplary response:*

        {"message":"11223344556677881122334455667788","status":"ok"}

- **void getMaxPinTries(final NfcCallback callback, Boolean... showDialog),** <br/>
  **String getMaxPinTriesAndGetJson(Boolean... showDialog)**

    This function is used to get retry maximum times of PIN. 

    *Response:*

        {"message":"10","status":"ok"}

- **void getRemainingPinTries(final NfcCallback callback, Boolean... showDialog),** <br/>
  **String getRemainingPinTriesAndGetJson(Boolean... showDialog)**

    This function is used to get remaining retry times of PIN.

    *Exemplary response:*

        {"message":"10","status":"ok"}

- **void getRootKeyStatus(final NfcCallback callback, Boolean... showDialog),** <br/>
  **String getRootKeyStatusAndGetJson(Boolean... showDialog)**

    This function is used to get the status of seed for ed25519: is it generated or not.

    *Response:*

        a) If seed is present: {"message":"generated","status":"ok"}
        b) If seed is not present: {"message":"not generated","status":"ok"}

- **void resetWallet(final NfcCallback callback, Boolean... showDialog),** <br/>
  **String resetWalletsAndGetJson(Boolean... showDialog)**

    This function is used to reset the wallet state to the initial state. After resetting the wallet, the default PIN value would be 5555. The remaining retry for the PIN will be reset to MAX (default is 10). The seed for ed25519 will be erased. And after its calling any card operation (except of CoinManager stuff) will fail with 6F02 error. TON Labs wallet applet does not work without seed.

    *Response:*

        {"message":"done","status":"ok"}

- **void getAvailableMemory(final NfcCallback callback, Boolean... showDialog),** <br/>
  **String getAvailableMemoryAndGetJson(Boolean... showDialog)**

    This function is used to obtain the amount of memory of the specified type that is available to the applet. Note that implementation-dependent memory overhead structures may also use the same memory pool.

- **void getAppsList(final NfcCallback callback, Boolean... showDialog),** <br/>
  **String getAppsListAndGetJson(Boolean... showDialog)**

    This function is used to get application list. It returns list of applets AIDs that were installed onto card.

    *Exemplary response:*

        {"message":"4F0D31313232333334343535363600","status":"ok"}

    _Note:_ Here 313132323333343435353636 is AID of our TON Labs wallet applet

- **void generateSeed(final String pin, final NfcCallback callback, Boolean... showDialog),** <br/>
  **String generateSeedAndGetJson(final String pin, Boolean... showDialog)**

    This function is used to generate the seed for ed25519 with RNG.

    *Arguments requirements:*

        pin — numeric string of length 4, example: '5555'

    _Note:_ By the way 5555 is a default PIN for all cards. 

    *Response:*

        If seed does not exist then: {"message":"done","status":"ok"}
        If seed already exists and you call generateSeed then it will throw a error.

- **void changePin(final String oldPin, final String newPin, final NfcCallback callback, Boolean... showDialog),** <br/>
  **String changePinAndGetJson(final String oldPin, final String newPin, Boolean... showDialog)**

    This function is used to change device PIN.

    *Arguments requirements:*

        oldPin — numeric string of length 4, example: '5555'
        newPin — numeric string of length 4, example: '6666'

    *Response:*

        {"message":"done","status":"ok"}

## Functions to work with TON Labs wallet applet

TON Labs wallet applet is software developed by TON Labs team and integrated into NFC TON Labs Security card. It provides main card functionality. It takes seed for ed25519 signature from CoinManager entity.

These functions are naturally divided into four groups. And there are respectively four classes in TonNfcClientAndroid library providing an API: CardActivationApi,  CardCryptoApi,  CardKeyChainApi, RecoveryDataApi. And there is a superclass TonWalletApi containing some common functions and functions to maintain keys for HMAC SHA256 signature (see section _Protection against MITM_ in readme).

### TonWalletApi functions

#### Common functions

- **void getTonAppletState(final NfcCallback callback, Boolean... showDialog),** <br/>
  **String getTonAppletStateAndGetJson(Boolean... showDialog)**

    This function returns state of TON Labs wallet applet.

    *Exemplary responses:*

        {"message":"TonWalletApplet waits two-factor authentication.","status":"ok"}
        {"message":"TonWalletApplet is personalized.","status":"ok"}

    _Note:_ Full list of applet states you may find in readme.

- **void getSerialNumber(final NfcCallback callback, Boolean... showDialog),** <br/>
  **String getSerialNumberAndGetJson(Boolean... showDialog)**

    This function returns serial number (SN). It must be identical to SN printed on the card.

    *Exemplary response:*

        {"message":"504394802433901126813236","status":"ok"}

- **void getSault(final NfcCallback callback, Boolean... showDialog),** <br/>
  **String getSaultAndGetJson(Boolean... showDialog)**

    This function returns fresh 32 bytes sault generated by the card. 

    *Exemplary response:*

        {"message":"B81F0E0E07316DAB6C320ECC6BF3DBA48A70101C5251CC31B1D8F831B36E9F2A","status":"ok"}

- **void disconnectCard(final NfcCallback callback),** <br/>
  **String disconnectCardAndGetJson()**

    Breaks NFC connection. 

    *Response:*

        {"message":"done","status":"ok"}

#### Functions to mantain keys for HMAC SHA256 

- **void selectKeyForHmac(final String serialNumber, final NfcCallback callback),** <br/>
  **String selectKeyForHmacAndGetJson(final String serialNumber)**

    Manually select new active card (it selects the serial number and correspondingly choose the appropriate key HMAC SHA256 from Android Keystore).

    *Arguments requirements:*

        serialNumber — numeric string of length 24, example: "50439480243390112681323"

    *Response:*

        {"message":"done","status":"ok"}

- **void createKeyForHmac(final String authenticationPassword, final String commonSecret, final String serialNumber, final NfcCallback callback),** <br/>
  **String createKeyForHmacAndGetJson(final String authenticationPassword, final String commonSecret, final String serialNumber)**

    If you reinstalled app and lost HMAC SHA256 symmetric key for the card from your Android keystore, then create the key for your card using this function.

    *Arguments requirements:*

        authenticationPassword — hex string of length 256, 
        example: "4A0FD62FFC3249A45ED369BD9B9CB340829179E94B8BE546FB19A1BC67C9411BC5DC85B5E38F96689B921A64DEF1A3B6F4D2F5C7D2B0BD7CCE420DBD281BA1CC82EE0B233820EB5CFE505B7201903ABB12959B251A5A8525B2515F57ACDE30905E70C2A375D5C0EC10A5EA6E264206395BF163969632398FA4A88D359FEA21D9"

        commonSecret — hex string of length 64, example: "9CEE28E284487EEB8FA6CE7C101C1184BB368F0CCAD057C9D89F7EC3307E72BA"

        serialNumber — numeric string of length 24, example: "50439480243390112681323"

    *Note 1:* Use here activation data tuple  (authenticationPassword, commonSecret) that is correct for your card, i.e. corresponds to your serialNumber.

    *Note 2:* If the key for your card already exists in keystore it will not throw a error. It will just delete and recreate the key for you.

    *Response:*

        {"message":"done","status":"ok"}

- **void getCurrentSerialNumber(final NfcCallback callback),** <br/>
  **String getCurrentSerialNumberAndGetJson()** 

    Get serial number of currently active key (card). In fact this is a serialNumber of the card with which your app communicated last time.

    *Exemplary response:*

        {"message":"504394802433901126813236","status":"ok"}

- **void getAllSerialNumbers(final NfcCallback callback),** <br/>
  **String getAllSerialNumbersAndGetJson()** 

    Get the list of card serial numbers for which we have keys in Android keystore.

    *Exemplary response:*

        {"message":["504394802433901126813236", "455324585319848551839771"],"status":"ok"}

- **void isKeyForHmacExist(final String serialNumber, final NfcCallback callback),** <br/>
  **String isKeyForHmacExistAndGetJson(final String serialNumber)**

    Check if key for given serialNumber exists in Android keystore.

    *Arguments requirements:*

        serialNumber — numeric string of length 24, example: "50439480243390112681323"

    *Exemplary response:*

        {"message":"true","status":"ok"}

- **void deleteKeyForHmac(final String serialNumber, final NfcCallback callback),** <br/>
  **String deleteKeyForHmacAndGetJson(final String serialNumber)**

    Delete key for given serialNumber from Android keystore.

    *Arguments requirements:*

        serialNumber — numeric string of length 24, example: "50439480243390112681323"

    *Response:*

        {"message":"done","status":"ok"}

### CardActivationApi functions

When user gets NFC TON Labs security card at the first time, the applet on the card is in a special state. It waits for user authentication. And the main functionality of applet is blocked for now. At this point you may call all functions from previous subsections. And also some special functions are available in CardActivationApi. They are necessary to complete card activation. 

- **void turnOnWallet(final String newPin, final String password, final String commonSecret, final String initialVector, final NfcCallback callback, Boolean... showDialog),** <br/>
  **String turnOnWalletAndGetJson(final String newPin, final String password, final String commonSecret, final String initialVector, Boolean... showDialog)**

    This function makes TON Labs wallet applet activation. After its succesfull call applet will be in working personalized state (so getTonAppletState() will return {"message":"TonWalletApplet is personalized.","status":"ok"}). At the begining of its work it reset seed and pin and generate new seed.

    *Arguments requirements:*

        newPin — numeric string of length 4, example: '7777'

        password — hex string of length 256, 
        example: "4A0FD62FFC3249A45ED369BD9B9CB340829179E94B8BE546FB19A1BC67C9411BC5DC85B5E38F96689B921A64DEF1A3B6F4D2F5C7D2B0BD7CCE420DBD281BA1CC82EE0B233820EB5CFE505B7201903ABB12959B251A5A8525B2515F57ACDE30905E70C2A375D5C0EC10A5EA6E264206395BF163969632398FA4A88D359FEA21D9"

        commonSecret — hex string of length 64, example: "9CEE28E284487EEB8FA6CE7C101C1184BB368F0CCAD057C9D89F7EC3307E72BA"

        initialVector — hex string of length 32, example: "E439F75C6FC516F1C4725E825164216C"

    _Note:_ Use here activation data tuple  (authenticationPassword, commonSecret, initialVector) that is correct for your card, i.e. corresponds to your serialNumber.

    *Response:*

        {"message":"TonWalletApplet is personalized.","status":"ok"}
        
- **void turnOnWallet(final String password, final String commonSecret, final String initialVector, final NfcCallback callback, Boolean... showDialog),** <br/>
  **String turnOnWalletAndGetJson(final String password, final String commonSecret, final String initialVector, Boolean... showDialog)**

    This function makes TON Labs wallet applet activation. After its succesfull call applet will be in working personalized state (so getTonAppletState() will return {"message":"TonWalletApplet is personalized.","status":"ok"}). It uses default PIN '5555'. At the begining of its work it reset seed and pin and generate new seed.

    *Arguments requirements:*

        password — hex string of length 256, 
        example: "4A0FD62FFC3249A45ED369BD9B9CB340829179E94B8BE546FB19A1BC67C9411BC5DC85B5E38F96689B921A64DEF1A3B6F4D2F5C7D2B0BD7CCE420DBD281BA1CC82EE0B233820EB5CFE505B7201903ABB12959B251A5A8525B2515F57ACDE30905E70C2A375D5C0EC10A5EA6E264206395BF163969632398FA4A88D359FEA21D9"

        commonSecret — hex string of length 64, example: "9CEE28E284487EEB8FA6CE7C101C1184BB368F0CCAD057C9D89F7EC3307E72BA"

        initialVector — hex string of length 32, example: "E439F75C6FC516F1C4725E825164216C"

    _Note:_ Use here activation data tuple  (authenticationPassword, commonSecret, initialVector) that is correct for your card, i.e. corresponds to your serialNumber.

    *Response:*

        {"message":"TonWalletApplet is personalized.","status":"ok"}

- **void getHashOfEncryptedCommonSecret(final NfcCallback callback, Boolean... showDialog),** <br/>
  **String getHashOfEncryptedCommonSecretAndGetJson(Boolean... showDialog)**

    Return SHA256 hash of encrypted common secret.

    *Exemplary response:*

        {"message":"EFBF24AC1563B34ADB0FFE0B0A53659E72E26765704C109C95346EEAA1D4BEAF","status":"ok"}

- **void getHashOfEncryptedPassword(final NfcCallback callback, Boolean... showDialog),** <br/>
  **String getHashOfEncryptedPasswordAndGetJson(Boolean... showDialog)**

    Return SHA256 hash of encrypted password.

    *Exemplary responses:*

        {"message":"26D4B03C0C0E168DC33E48BBCEB457C21364658C9D487341827BBFFB4D8B38F3","status":"ok"}
        
- **void getHashes(final NfcCallback callback, Boolean... showDialog),** <br/>
  **String getHashesAndGetJson(Boolean... showDialog)**

    Return SHA256 hash of encrypted password, hash of encrypted common secret, serial number.

    *Exemplary responses:*

        {"ecsHash":"26D4B03C0C0E168DC33E48BBCEB457C21364658C9D487341827BBFFB4D8B38F3","epHash":"EFBF24AC1563B34ADB0FFE0B0A53659E72E26765704C109C95346EEAA1D4BEAF", "serialNumber":"929526125066377952749605", "status":"ok"}

- **void generateSeedAndGetHashes(final NfcCallback callback, Boolean... showDialog),** <br/>
  **String generateSeedAndGetHashesAndGetJson(Boolean... showDialog)**

    Generate seed if it's absent and then return return SHA256 hash of encrypted password, hash encrypted common secret, serial number.

    *Exemplary responses:*

        {"ecsHash":"26D4B03C0C0E168DC33E48BBCEB457C21364658C9D487341827BBFFB4D8B38F3","epHash":"EFBF24AC1563B34ADB0FFE0B0A53659E72E26765704C109C95346EEAA1D4BEAF", "serialNumber":"929526125066377952749605", "status":"ok"}


### CardCryptoApi functions

Here there are functions related to ed25519 signature.

- **void getPublicKeyForDefaultPath(final NfcCallback callback, Boolean... showDialog),** <br/>
  **String getPublicKeyForDefaultPathAndGetJson(Boolean... showDialog)**

    Return public key for HD path m/44'/396'/0'/0'/0'.

    *Exemplary response:*

        {"message":"B81F0E0E07316DAB6C320ECC6BF3DBA48A70101C5251CC31B1D8F831B36E9F2A","status":"ok"}
        
- **public void checkSerialNumberAndGetPublicKeyForDefaultPath(final String serialNumber, final NfcCallback callback, Boolean... showDialog),** <br/>
  **public String checkSerialNumberAndGetPublicKeyForDefaultPathAndGetJson(final String serialNumber)**
  
    Read serial number of currently connected security card and compare it with serialNumber argument. If they are identical then return public key for HD path m/44'/396'/0'/0'/0'. Else reject the card.
  
    *Arguments requirements:*

        serialNumber — numeric string of length 24, example: "50439480243390112681323".
 
    *Exemplary response:*

        {"message":"B81F0E0E07316DAB6C320ECC6BF3DBA48A70101C5251CC31B1D8F831B36E9F2A","status":"ok"}

- **void verifyPin(final String pin, final NfcCallback callback, Boolean... showDialog),** <br/>
  **String verifyPinAndGetJson(final String pin, Boolean... showDialog)**

    Make pin verification.

    *Arguments requirements:*

        pin — numeric string of length 4, example: '5555'

    *Response:*

        {"message":"done","status":"ok"}

- **void signForDefaultHdPath(final String dataForSigning, final NfcCallback callback, Boolean... showDialog),** <br/>
  **String signForDefaultHdPathAndGetJson(final String dataForSigning, Boolean... showDialog)**

    Make data signing by key for HD path m/44'/396'/0'/0'/0'. Prior to call this function you must call verifyPin.

    *Arguments requirements:*

        dataForSigning — hex string of even length ≥ 2 and ≤ 378.

    *Exemplary response:*

        {"message":"2D6A2749DD5AF5BB356220BFA06A0C624D5814438F37983322BBAD762EFB4759CFA927E6735B7CD556196894F3CE077ADDD6B49447B8B325ADC494B82DC8B605","status":"ok"}
        
- **public void checkSerialNumberAndSignForDefaultHdPath(final String serialNumber, final String dataForSigning, final NfcCallback callback, Boolean... showDialog),** <br/>
  **public String checkSerialNumberAndSignForDefaultHdPathAndGetJson(final String serialNumber, final String dataForSigning)**
  
    Read serial number of currently connected security card and compare it with serialNumber argument. If they are identical then make data signing by key for HD path m/44'/396'/0'/0'/0'. Else reject the card. Prior to call this function you must call verifyPin. 
  
    *Arguments requirements:*

        serialNumber — numeric string of length 24, example: "50439480243390112681323".
        
        dataForSigning — hex string of even length ≥ 2 and ≤ 378.
       
    *Exemplary response:*

        {"message":"2D6A2749DD5AF5BB356220BFA06A0C624D5814438F37983322BBAD762EFB4759CFA927E6735B7CD556196894F3CE077ADDD6B49447B8B325ADC494B82DC8B605","status":"ok"}

- **void sign(final String dataForSigning, final String hdIndex, final NfcCallback callback, Boolean... showDialog),** <br/>
  **String signAndGetJson(final String dataForSigning, final String hdIndex, Boolean... showDialog)**

    Make data signing by key for HD path m/44'/396'/0'/0'/hdIndex'. Prior to call this function you must call verifyPin.

    *Arguments requirements:*

       hdIndex — numeric string of length > 0 and ≤ 10.

       dataForSigning — hex string of even length ≥ 2 and ≤ 356.

    *Exemplary response:*

        {"message":"13FB836213B12BBD41209273F81BCDCF7C226947B18128F73E9A6E96C84B30C3288E51C622C045488981B6544D02D0940DE54D68A0A78BC2A5F9523B8757B904","status":"ok"}
        
- **public void checkSerialNumberAndSign(final String serialNumber, final String dataForSigning, final String hdIndex, final NfcCallback callback, Boolean... showDialog),** <br/>
  **public String checkSerialNumberAndSignAndGetJson(final String serialNumber, final String dataForSigning, final String hdIndex)**
  
    Read serial number of currently connected security card and compare it with serialNumber argument. If they are identical then make data signing by key for HD path m/44'/396'/0'/0'/hdIndex'. Else reject the card. Prior to call this function you must call verifyPin.
  
   *Arguments requirements:*

       serialNumber — numeric string of length 24, example: "50439480243390112681323".
       
       hdIndex — numeric string of length > 0 and ≤ 10.

       dataForSigning — hex string of even length ≥ 2 and ≤ 356.
       
    *Exemplary response:*

     {"message":"13FB836213B12BBD41209273F81BCDCF7C226947B18128F73E9A6E96C84B30C3288E51C622C045488981B6544D02D0940DE54D68A0A78BC2A5F9523B8757B904","status":"ok"}   

- **void getPublicKey(final String hdIndex, final NfcCallback callback, Boolean... showDialog),** <br/>
  **String getPublicKeyAndGetJson(final String hdIndex, Boolean... showDialog)** 

    Return public key for HD path m/44'/396'/0'/0'/hdIndex'.

    *Arguments requirements:*

        hdIndex — numeric string of length > 0 and ≤ 10.

    *Exemplary response:*

        {"message":"B81F0E0E07316DAB6C320ECC6BF3DBA48A70101C5251CC31B1D8F831B36E9F2A","status":"ok"}
        
- **public void checkSerialNumberAndGetPublicKey(final String serialNumber, final String hdIndex, final NfcCallback callback, Boolean... showDialog),**  <br/>
  **public String checkSerialNumberAndGetPublicKeyAndGetJson(final String serialNumber, final String hdIndex)**
  
    Read serial number of currently connected security card and compare it with serialNumber argument. If they are identical then return public key for HD path m/44'/396'/0'/0'/hdIndex'. Else reject the card.
  
    *Arguments requirements:*

        serialNumber — numeric string of length 24, example: "50439480243390112681323".
        
        hdIndex — numeric string of length > 0 and ≤ 10.
 
- **void verifyPinAndSignForDefaultHdPath(final String dataForSigning, final String pin, final NfcCallback callback, Boolean... showDialog),** <br/>
  **String verifyPinAndSignForDefaultHdPathAndGetJson(final String dataForSigning, final String pin, Boolean... showDialog)**

    Make pin verification and data signing by key for HD path m/44'/396'/0'/0'/0'. 

    *Arguments requirements:*

        pin — numeric string of length 4, example: '5555'

        dataForSigning — hex string of even length ≥ 2 and ≤ 378.

    *Exemplary response:*

        {"message":"2D6A2749DD5AF5BB356220BFA06A0C624D5814438F37983322BBAD762EFB4759CFA927E6735B7CD556196894F3CE077ADDD6B49447B8B325ADC494B82DC8B605","status":"ok"}
        
- **public void checkSerialNumberAndVerifyPinAndSignForDefaultHdPath(final String serialNumber, final String dataForSigning, final String pin, final NfcCallback callback, Boolean... showDialog),** <br/>
  **public String checkSerialNumberAndVerifyPinAndSignForDefaultHdPathAndGetJson(final String serialNumber, final String dataForSigning, final String pin)**  
  
    Read serial number of currently connected security card and compare it with serialNumber argument. If they are identical then make  pin verification and data signing by key for HD path m/44'/396'/0'/0'/0'. Else reject the card.
    
    *Arguments requirements:*
    
        serialNumber — numeric string of length 24, example: "50439480243390112681323".

        pin — numeric string of length 4, example: '5555'

        dataForSigning — hex string of even length ≥ 2 and ≤ 378.
        
    *Exemplary response:*

        {"message":"2D6A2749DD5AF5BB356220BFA06A0C624D5814438F37983322BBAD762EFB4759CFA927E6735B7CD556196894F3CE077ADDD6B49447B8B325ADC494B82DC8B605","status":"ok"}     

- **void verifyPinAndSign(final String dataForSigning, final String hdIndex, final String pin, final NfcCallback callback, Boolean... showDialog),** <br/>
  **String verifyPinAndSignAndGetJson(final String dataForSigning, final String hdIndex, final String pin, Boolean... showDialog)**

    Make pin verification and data signing by key for HD path m/44'/396'/0'/0'/hdIndex'.

    *Arguments requirements:*

        pin — numeric string of length 4, example: '5555'

        hdIndex — numeric string of length > 0 and ≤ 10.

        dataForSigning — hex string of even length ≥ 2 and ≤ 356.

    *Exemplary response:*

        {"message":"13FB836213B12BBD41209273F81BCDCF7C226947B18128F73E9A6E96C84B30C3288E51C622C045488981B6544D02D0940DE54D68A0A78BC2A5F9523B8757B904","status":"ok"}
        
- **public void checkSerialNumberAndVerifyPinAndSign(final String serialNumber, final String dataForSigning, final String hdIndex, final String pin, final NfcCallback callback, Boolean... showDialog),** <br/>
**public String checkSerialNumberAndVerifyPinAndSignAndGetJson(final String serialNumber, final String dataForSigning, final String hdIndex, final String pin)**

   Read serial number of currently connected security card and compare it with serialNumber argument. If they are identical then make pin verification and data signing by key for HD path m/44'/396'/0'/0'/hdIndex'. Else reject the card.
   
   *Arguments requirements:*
   
        serialNumber — numeric string of length 24, example: "50439480243390112681323".

        pin — numeric string of length 4, example: '5555'.

        hdIndex — numeric string of length > 0 and ≤ 10.

        dataForSigning — hex string of even length ≥ 2 and ≤ 356.
        
   *Exemplary response:*

        {"message":"13FB836213B12BBD41209273F81BCDCF7C226947B18128F73E9A6E96C84B30C3288E51C622C045488981B6544D02D0940DE54D68A0A78BC2A5F9523B8757B904","status":"ok"}      

### RecoveryDataApi functions

- **void getRecoveryDataLen(final NfcCallback callback, Boolean... showDialog),** <br/>
  **String getRecoveryDataLenAndGetJson(Boolean... showDialog)**

    Read actual recovery data length.

    *Exemplary response:* 

        {"message":"7","status":"ok"}

- **void getRecoveryDataHash(final NfcCallback callback, Boolean... showDialog),** <br/>
  **String getRecoveryDataHashAndGetJson(Boolean... showDialog)**

    Read recovery data SHA256 hash.

    *Exemplary response:* 

        {"message":"B81F0E0E07316DAB6C320ECC6BF3DBA48A70101C5251CC31B1D8F831B36E9F2A","status":"ok"}

- **void getRecoveryData(final NfcCallback callback, Boolean... showDialog),** <br/>
  **String getRecoveryDataAndGetJson(Boolean... showDialog)**

    Read  recovery data from TON Wallet applet.

    *Exemplary response:* 

        {"message":"00112233445566","status":"ok"}

- **void addRecoveryData(final String recoveryData, final NfcCallback callback, Boolean... showDialog),** <br/>
  **String addRecoveryDataAndGetJson(final String recoveryData, Boolean... showDialog)**

    Save recovery data into applet. 

    *Arguments requirements:*

        recoveryData — hex string of even length ≥ 2 and ≤ 4096.

    *Response:*

        {"message":"done","status":"ok"}

- **void isRecoveryDataSet(final NfcCallback callback, Boolean... showDialog),** <br/>
  **String isRecoveryDataSetAndGetJson(Boolean... showDialog)**

    Return 'true'/'false' if recovery data exists/does not exist.

    *Response:*

        1) If we added recovery data, then: {"message":"true","status":"ok"}
        2) If we did not add recovery data, then: {"message":"false","status":"ok"}

- **void resetRecoveryData(final NfcCallback callback, Boolean... showDialog),** <br/>
  **String resetRecoveryDataAndGetJson(Boolean... showDialog)**

    Clear recovery data.

    *Response:*

        {"message":"done","status":"ok"}

### CardKeyChainApi functions

- **void resetKeyChain(final NfcCallback callback, Boolean... showDialog),** <br/>
  **String resetKeyChainAndGetJson(Boolean... showDialog)**

    Clear keychain, i.e. remove all stored keys.

    *Response:*

        {"message":"done","status":"ok"}

- **void getKeyChainDataAboutAllKeys(final NfcCallback callback, Boolean... showDialog),** <br/>
  **String getKeyChainDataAboutAllKeysAndGetJson(Boolean... showDialog)**

    Return list of pairs (keyHmac, keyLength)  in json format.

    *Exemplary response:*

        {"message":[{"hmac":"D7E0DFB66A2F72AAD7D66D897C805D307EE1F1CB8077D3B8CF1A942D6A5AC2FF","length":"6"},{"hmac":"D31D1D600F8E5B5951275B9C6DED079011FD852ABB62C14A2EECA2E6924452C0","length":"3"}],"status":"ok"}

- **void getKeyChainInfo(final NfcCallback callback, Boolean... showDialog),** <br/>
  **String getKeyChainInfoAndGetJson(Boolean... showDialog)**

    Return json characterizing the state of keychain. 

    *Exemplary response:*

        {"numberOfKeys":0,"occupiedSize":0,"freeSize":32767,"status":"ok"}

- **void getNumberOfKeys(final NfcCallback callback, Boolean... showDialog),** <br/>
  **String getNumberOfKeysAndGetJson(Boolean... showDialog)**

    Return number of keys in card keychain.

    *Exemplary response:*

        {"message":"1","status":"ok"}

- **void getOccupiedStorageSize(final NfcCallback callback, Boolean... showDialog),** <br/>
  **String getOccupiedStorageSizeAndGetJson(Boolean... showDialog)**

    Return the volume of occupied size in card keychain (in bytes).

    *Exemplary response:*

        {"message":"0","status":"ok"}

- **void getFreeStorageSize(final NfcCallback callback, Boolean... showDialog),** <br/>
  **String getFreeStorageSizeAndGetJson(Boolean... showDialog)**

    Return the volume of free size in card keychain (in bytes).

    *Exemplary response:*

        {"message":"32767","status":"ok"}

- **void getKeyFromKeyChain(final String keyHmac, final NfcCallback callback, Boolean... showDialog),** <br/>
  **String getKeyFromKeyChainAndGetJson(final String keyHmac, Boolean... showDialog)**

    Read key from card keychain based on its hmac.

    *Arguments requirements:*

        keyHmac — hex string of length 64.

    *Exemplary response:*

        {"message":"001122334455","status":"ok"}

- **void addKeyIntoKeyChain(final String newKey, final NfcCallback callback, Boolean... showDialog),** <br/>
  **String addKeyIntoKeyChainAndGetJson(final String newKey, Boolean... showDialog)**

    Save new key into card keychain.

    *Arguments requirements:*

        newKey — hex string of even length ≥ 2 and ≤ 16384.

    *Response:*

        {"message":"EFBF24AC1563B34ADB0FFE0B0A53659E72E26765704C109C95346EEAA1D4BEAF","status":"ok"}

    where "message" contains hmac of newKey.

- **void deleteKeyFromKeyChain(final String keyHmac, final NfcCallback callback, Boolean... showDialog),** <br/>
  **String deleteKeyFromKeyChainAndGetJson(final String keyHmac, Boolean... showDialog)**

    Delete key from card keychain based on its hmac.

    *Arguments requirements:*

        keyHmac — hex string of length 64.

    *Exemplary response:*

        {"message":"5","status":"ok"}

    where "message" field contains the number of remaining keys

- **void finishDeleteKeyFromKeyChainAfterInterruption(final NfcCallback callback, Boolean... showDialog),** <br/>
  **String finishDeleteKeyFromKeyChainAfterInterruptionAndGetJson(Boolean... showDialog)**

    Finish the process of deleting key from card keychain. It may be necessary if previous DELETE operation was occassionally interrupted (like card disconnection).

    *Exemplary response:*

        {"message":"5","status":"ok"}

    where "message" field contains the number of remaining keys

- **void changeKeyInKeyChain(final String newKey, final String oldKeyHmac, final NfcCallback callback, Boolean... showDialog),** <br/>
  **String changeKeyInKeyChainAndGetJson(final String newKey, final String oldKeyHmac, Boolean... showDialog)**

    Replace existing key by new key. The length of new key must be equal to length of old key.

    *Arguments requirements:*

        newKey — hex string of even length ≥ 2 and ≤ 16384. 

        oldKeyHmac — hex string of length 64.

    *Response:*

        {"message":"EFBF24AC1563B34ADB0FFE0B0A53659E72E26765704C109C95346EEAA1D4BEAF","status":"ok"}

    where "message" contains hmac of newKey.

- **void getIndexAndLenOfKeyInKeyChain(final String keyHmac, final NfcCallback callback, Boolean... showDialog),** <br/>
  **String getIndexAndLenOfKeyInKeyChainAndGetJson(final String keyHmac, Boolean... showDialog)**

    Read index (inside internal applet storage) and length of key by its hmac.

    *Arguments requirements:*

        keyHmac — hex string of length 64.

    *Exemplary response:*

        {"message":"{\"index\":1,\"length\":3}","status":"ok"}

- **void checkAvailableVolForNewKey(final Short keySize, final NfcCallback callback, Boolean... showDialog),** <br/>
  **String checkAvailableVolForNewKeyAndGetJson(final Short keySize, Boolean... showDialog)**

    Check if there is enough free volume in card keychain to add new key of length = keySize. If there is no enough space then it throws an exception

    *Arguments requirements:*

        keySize — numeric string representing short value > 0 and ≤ 8192.

    *Response:*

        {"message":"done","status":"ok"}

- **void checkKeyHmacConsistency(final String keyHmac, final NfcCallback callback, Boolean... showDialog),** <br/>
  **String checkKeyHmacConsistencyAndGetJson(final String keyHmac, Boolean... showDialog)**

    Checks if card's keychain stores a key with such keyHmac and if this hmac really corresponds to the key.

    *Response:*

        {"message":"done","status":"ok"}

- **void getHmac(final String index, final NfcCallback callback, Boolean... showDialog),** <br/>
  **String getHmacAndGetJson(final String index, Boolean... showDialog)**

    Get hmac of key in card keychain by its index. 

    *Arguments requirements:*

        index — digital string storing an integer ≥ 0 and ≤1023.

    *Exemplary response:*

        {"message":"EFBF24AC1563B34ADB0FFE0B0A53659E72E26765704C109C95346EEAA1D4BEAF","status":"ok"}

- **void getDeleteKeyRecordNumOfPackets(final NfcCallback callback, Boolean... showDialog),** <br/>
  **String getDeleteKeyRecordNumOfPacketsAndGetJson(Boolean... showDialog)**

    Returns the number of keys records packets that must be deleted to finish deleting of key.

    *Exemplary response:*

        {"message":"2","status":"ok"}

- **void getDeleteKeyChunkNumOfPackets(final NfcCallback callback, Boolean... showDialog),** <br/>
  **String getDeleteKeyChunkNumOfPacketsAndGetJson(Boolean... showDialog)**

    Returns the number of keys chunks packets that must be deleted to finish deleting of key.

    *Exemplary response:*

        {"message":"5","status":"ok"}
