Here there is full list list of functions provided by TonNfcClientAndroid library to make different requests to the card. These functions are naturally divided into several groups. There are five groups and respectively five classes providing an API for you: CardActivationApi,  CardCryptoApi,  CardKeyChainApi, RecoveryDataApi , CardCoinManagerApi. And there is an ancestor TonWalletApi for the first four of them (except of CardCoinManagerApi). It contains some common functions.

### Common functions

* getTonAppletState()

	This function returns state of TON Wallet applet.
	
	Exemplary responses:
	{"message":"TonWalletApplet waits two-factor authorization.","status":"ok"}
