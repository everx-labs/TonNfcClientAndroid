Here there is full list list of functions provided by TonNfcClientAndroid library to make different requests to the card. These functions are naturally divided into several groups. There are five groups and respectively five classes providing an API for you: CardActivationApi,  CardCryptoApi,  CardKeyChainApi, RecoveryDataApi , CardCoinManagerApi. And there is an ancestor TonWalletApi for the first four of them (except of CardCoinManagerApi). It contains some common functions.

### Common functions

* getTonAppletState()

	This function returns state of TON Wallet applet.
	
	Exemplary responses:
	{"message":"TonWalletApplet waits two-factor authorization.","status":"ok"}
	
	
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
