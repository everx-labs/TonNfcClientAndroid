# Release Notes

All notable changes to this project will be documented in this file.

## [1.0] – 2021-01-28

### New

- Added all necessary functionality to communicate with TON NFC security smart cards. And also we prepared a simple example of using the library.

## [2.0] – 2021-01-29

### New

- Added documentation.

## [3.0] – 2021-02-07

### New

- Extended readme, updated functions list doc.
- Added more code samples into exemplary app.
- Added unit tests

### Fixed

- Fixed some bugs around ByteArrayHelper.

## [3.0.1] – 2021-03-12

### New

- Added setCardTag function into TonWalletApi.
- Refactoring: added more verification of functions arguments and IllegalArgumentException throwing; added more Android internal error messages; rework for RADU and CAPDU wrappers; added more constants.
- Reorganized packages structure and added annotation @RestrictTo(RestrictTo.Scope.LIBRARY) for classes that should not be available from the outside. So now public API of the library includes: classes with ending 'Api' and also ByteArrayUtil, TonWalletConstants, NfcApduRunner, CAPDU, RAPDU.
- Added more unit tests and comments, fixes for docs.

### Fixed

- Fixed bugs in ByteArrayUtil.

## [3.1.1] – 2021-04-21

### New

- Added turnOnWallet without PIN argument.
- Added getHashes function.
- Added more unit tests + some code refactoring.

### Fixed 

- "errorCode field -> code field" in jsons representing errors.

## [3.2.0] – 2021-05-20

### New

- Added invitation dialogs for NFC card connection.

## [3.2.1] – 2021-05-20

### Fixed

- Rework invitation dialogs using async tasks to make it stable for integration into react native .
