#Recovery design service

## 1. Requirements for the Recovery service:

Recovery service should provide:

- creating a pair of keys in HSM for a surf user with an associated security card
- transparency of working with a key pair from the user's point of view
- access to the key pair only for authenticated users
- impossibility of matching key pairs from hsm with addresses of user wallets in the event of a service compromise
- this service is intended only for recovery without the functionality of signing transactions, etc.

## 2. Registration in the Recovery service

Registration in the Recovery service is offered to the user as a way to replace keys in the list of custodians in case of compromise of one of the keys, thus registration in the Recovery service is a logical continuation of the security card activation procedure.

On the mobile version of Surf, it might look like this:

When choosing to use the Recovery service, the user is prompted for an e-mail to receive a one-time password for authentication in the service and create a key pair.

After entering the e-mail, the following steps are performed:

**Surf**

1. Requests the public key of the SC service in the configuration contact **integrationConfig** {serviceId: 8}
2. Encrypts the user's email with the public key of the SC service, signs the received message with the private key of the Surf user and sends a request via the TSC contract (`requestRecoveryRegistration (bytes request)`) to generate a key pair in the Recovery service `enc (email | gen_pair_request ) | surf_pub_key` (The status becomes `**RECOVERY_REGISTRATION_STARTED = 3**`)
3. Asks the user for the `2FA code` from email, encrypts it with the public key of the SC service and sends the message`enc (2FA | surf_pub_key)`via the TSC contract (`send2FA (bytes code2FA)`) encrypting the message open (the status is becomes: `**RECOVERY_2FA_SENT = 2**`):
    - success - go to step 4 (receives from the TSC contract the message `event UpdatedRecoveryStatus (uint8 recoveryStatus, uint8 retriesLeft)` with the status `**RECOVERY_2FA_SUCCESS = 7**`)
    - failure - error `2FA code` (message from TCS-contract`event UpdatedRecoveryStatus (uint8 recoveryStatus, uint8 retriesLeft)`with status`**RECOVERY_2FA_CHECK_FAILED = 10**`and the number of remaining attempts), prompt the user to enter the code again (3 attempts ) then a request for a new code from `email` (after 9 unsuccessful attempts, it receives a message from the TCS contract`event BannedRecoveryTill (uint32 bannedTill)`(The status changes to `**RECOVERY_2FA_BANNED = 12**` )
4. Receives from the TSС-contract (from the external message `event FinishedRecoveryRegistrastion (bytes recoveryData)` or by calling `getRecoveryData ()`) (the status changes to `**RECOVERY_REGISRTATION_FINISHED = 4**`) **public key** from Recovery service to enable multisig in 2/3 mode and AES key
**Public key and AES key are encrypted with naclBox function**
5. Using the obtained AES-key, it forms the encrypted binary block `surf_pub_key | multisig_addr | card CS | card auth password`
6. Surf saves the encrypted binary block `surf_pub_key | multisig_addr | card CS | card auth password` **.**

**TSC-contract current version:**

1. Receives a message from Surf with a request to create a key pair (`requestRecoveryRegistration (bytes request)`). 
2. Forwards the message `enc (email | gen_pair_request) | surf_pub_key` to SC-service (`event RequestedRecoveryRegistration (bytes request, uint256 encryptionPublicKey)`)
3. Forwards the message `enc (2FA) | surf_pub_key` to SC-service (`send2FA (bytes code2FA)` ⇒ `event Sent2FA (bytes code2FA, uint256 encryptionPublicKey)`)
4. Receives from the SC-service the result of checking the 2FA code `updateRecoveryStatus (uint8 status)` ⇒ `event UpdatedRecoveryStatus (uint8 recoveryStatus, uint8 retriesLeft)`
In case of receiving the status `**RECOVERY_2FA_CHECK_FAILED = 10**` increases the attempts counter by 1:
    - on 3 and 6 attempts, go to step 4.
    - after 9 unsuccessful attempts, he is banned for a day. Sends the message `event BannedRecoveryTill (uint32 bannedTill)` to Surf (The status changes to **`RECOVERY_2FA_BANNED = 12`**)
5. Sends the message `enc (rec_surf_pub_key | export_recovery_aes_key)` (`finishRecoveryRegistration (bytes response)` ⇒ `event FinishedRecoveryRegistrastion (bytes recoveryData)`) from the SC service to Surf

**SC-service:**

1. Receiving a request for registration (`event RequestedRecoveryRegistration (bytes request, uint256 encryptionPublicKey)`) in the Recovery service, decrypts the message
2. Generates a `2FA-code` (6-digit string from the alphabet A-Za-z0-9), saves the match`surf_pub_key: 2FA` and sends a confirmation 2FA code to `email`.
3. Receives and decrypts a message from the TSC contract (`event Sent2FA (bytes code2FA, uint256 encryptionPublicKey)`), extracts the `2FA code`, matches`surf_pub_key: 2FA` 
    - success - calls in the TSC contract `updateRecoveryStatus (uint8 status)` with the status `**RECOVERY_2FA_SUCCESS = 7**`
    - failure - return of error of verification of `2FA-code` in the TSC-contract is called`updateRecoveryStatus (uint8 status)`with status`**RECOVERY_2FA_CHECK_FAILED = 10**`
4. Marks successful authentication and sends a request (createKeyPair ?????) to the recovery service to generate the key pair `enc (sha (email) | gen_keypair_req) | surf_pub_key`
5. Sends the response from the Recovery service `enc (rec_surf_pub_key | export_recovery_aes_key)` to the user via the TSC contract (`finishRecoveryRegistration (bytes response)` ⇒ `event FinishedRecoveryRegistrastion (bytes recoveryData)`)

**Recovery service:**

At the time of launching the service:

- Checks connection to HSM using config
- Calculates hash `argon2 (global_salt, global_salt2)`

Generates service keys in HSM itself using `argon2 (global_salt, global_salt2)` as identifier

1. Decrypts a message from an SC service
2. Computes `argon2 (sha256 (email), global_salt)`
3. Generates a key pair in HSM with the identifier `argon2 (sha256 (email), global_salt)`
4. Computes `argon2 (sha256 (email), global_salt2)`
5. Generates an exported AES-192 key in HSM with the identifier `argon2 (sha256 (email), global_salt2)`
6. Retrieves the public key from the pair (hereinafter `rec_surf_pub_key`)
7. Sends an encrypted `surf_pub_key` message`enc (rec_surf_pub_key | export_recovery_aes_key)`

## 3. Changing the list of custodians of multisig via Recovery service

If one of the multisig custodians is compromised, the user can update the list of custodians of his multisig using the Recovery service via Surf. To do this, the user performs the procedure corresponding to his situation

### 3.1 Only security card lost

**Surf:**

1. User initiates card loss in Surf
2. Surf calls the `submitUpdate` function of the multisig, passes the list of new custodians as an argument to`owners` (resets it to one custodian). An important point is the absence of a key from the Recovery service in this list, otherwise the Recovery service will not confirm such a transaction
3. Requests the public key of the SC service in the configuration contract **integrationConfig** {serviceId: 8}
4. Encrypts `email`,`multisig_addr` and `updateId` with the public key of the SC service, signs the received message with the private key of the Surf user and sends a request via the TSC contract (function`requestUpdateConfirmation (bytes request)`⇒`event RequestedUpdateConfirm (bytes request, uint256 encryptionPublicKey)`) to sign the transaction for updating the list of custodians of the multisig in the Recovery service`enc (email | multisig_addr | updateId | sign_restore_tx) | surf_pub_key` (The status becomes **`RECOVERY_UPDATE_REQUESTED = 5`**)
5. Asks the user for the `2FA-code` from email, encrypts it with the public key of the SC-service, sends a message via the TSC-contract (`send2FA (bytes code2FA)`), encrypting the message open (the status becomes:`**RECOVERY_2FA_SENT = 2**`):
    - success - go to step 6 (receives from the TSC contract the message `event UpdatedRecoveryStatus (uint8 recoveryStatus, uint8 retriesLeft)` with the status `**RECOVERY_2FA_SUCCESS = 7**`)
    - failure - error `2FA code` (message from TCS-contract`event UpdatedRecoveryStatus (uint8 recoveryStatus, uint8 retriesLeft)`with status`**RECOVERY_2FA_CHECK_FAILED = 10**`and the number of remaining attempts), prompt the user to enter the code again (3 attempts ) then a request for a new code from `email` (after 9 unsuccessful attempts, it receives a message from the TCS contract`event BannedRecoveryTill (uint32 bannedTill)`(The status changes to `**RECOVERY_2FA_BANNED = 12**`)
6. Receives a response about the results of `confirmUpdate` execution by the Recovery service from an external message from the TSC contract (`event FinishedUpdateConfirm (uint256 encryptionPublicKey)`) (the status changes to`**RECOVERY_UPDATE_CONFIRMED = 6**`)
7. Calls the multisig function `executeUpdate` using`surf_priv_key` for signature
8. Receives confirmation of successful update of the multisig custodian list
9. Notifies the client about the successful reset of the multisig and the deterioration of the level of protection of his wallet. Offers to order a new card.

**TSC-contract:**

1. Receives a message from Surf with a request to execute `confirmUpdate` (`requestUpdateConfirmation (bytes request)`)
2. Forwards the message from Surf to `enc (email | sign_restore_tx) | surf_pub_key` to SC-service (`event RequestedUpdateConfirm (bytes request, uint256 encryptionPublicKey)`) (The status becomes `**RECOVERY_UPDATE_REQUESTED = 5**`)
3. Forwards the message `enc (2FA) | surf_pub_key` to SC-service (`send2FA (bytes code2FA)` ⇒ `event Sent2FA (bytes code2FA, uint256 encryptionPublicKey)`)
4. Receives from the SC-service the result of checking the 2FA code `updateRecoveryStatus (status)` ⇒ `event UpdatedRecoveryStatus (uint8 recoveryStatus, uint8 retriesLeft)`
In case of receiving the status `**RECOVERY_2FA_CHECK_FAILED = 10**` increases the counter of attempts by 1:
    - on 3 and 6 attempts, go to step 2
    - after 9 unsuccessful attempts, he is banned for a day. Sends the message `event BannedRecoveryTill (uint32 bannedTill)` to Surf (The status changes to **`RECOVERY_2FA_BANNED = 12`**)
5. Sends a response from the SC service to Surf about the success of the `confirmUpdate` call by the Recovery service (`finishUpdateConfirmation ()`⇒`event FinishedUpdateConfirm (uint256 encryptionPublicKey)`) (the status changes to`**RECOVERY_UPDATE_CONFIRMED = 6**` )

**SC-service:**

1. Receiving a request for signing in the Recovery service, decrypts the message from the TSC contract (`event RequestedUpdateConfirm (bytes request, uint256 encryptionPublicKey)`)
2. Generates a `2FA-code`, keeps a match for`surf_pub_key: 2FA` and sends a confirmation 2FA-code to `email`
3. Receives and decrypts the message (`event Sent2FA (bytes code2FA, uint256 encryptionPublicKey)`) from the TSC contract, extracts the `2FA-code`, matches`surf_pub_key: 2FA`:
    - success - calls in the TSC contract `updateRecoveryStatus (uint8 status)` with the status `**RECOVERY_2FA_SUCCESS = 7**`
    - failure - return of error of verification of `2FA-code` in the TSC-contract is called`updateRecoveryStatus (uint8 status)`with status`**RECOVERY_2FA_CHECK_FAILED = 10**`
4. Marks successful authentication and sends a request to the recovery service to sign the transaction to update the list of custodians `enc (sha256 (email) | multisig_addr | updateId | sign_restore_tx)`
5. Sends the response from the Recovery service (successful transaction confirmation) to the user via the TSC contract (`finishUpdateConfirmation ()` ⇒ `event FinishedUpdateConfirm (uint256 encryptionPublicKey)`)

**Recovery service:**

1. Decrypts a message requesting a signature from an SC service
2. Computes `argon2 (sha256 (email), global_salt)`
3. Checks for the presence of a key pair in HSM by identifier `argon2 (sha256 (email), global_salt)`
4. Extracts the multisig address and `updateId` from the request, generates a message with a call to the`confirmUpdate (updateId)`method to the`multisig_addr` address
5. The received message is signed in HSM with a private key from the found pair.
6. Repeats the call of the multisig method `confirmUpdate` until it receives confirmation of the passage of the message
7. Replies to the SC-service with the status of processing a multisig call
8. Removes from HSM the key pair with identifier `argon2 (sha256 (email), global_salt)` and the AES key with identifier `argon2 (sha256 (email), global_salt2)` after receiving confirmation of the transaction.

### 3.2 Security card and Surf device lost

In this case, the user has the opportunity to restore his surf account on a new device or through the Web version using the seed phrase and then follow the procedure from paragraph 3.1

### 3.3 Lost Surf device and Surf account seed

In this case, the user still has a security card and remembers the pin code from the key pair in the card, and also has access to the email that he used to register in the Recovery service, then the user on the new device creates a new account in Surf and performs procedure:

1. Clicks the button "Restore access to account"
2. Enters the email with which he registered in the Recovery service

**Surf:**

1. Deploy TSC-contract to the user
2. Requests the public key of the SC service ~~ via the TSC contract ~~ in the configuration contract **integrationConfig** {serviceId: 8}
3. Encrypts the user's email with the SC service's public key, signs the received message with the Surf user's private key and sends a request via the TSC contract (`requestRecoveryRegistration (bytes request)` => `event RequestedAccountRecovery (bytes request, uint256 encryptionPublicKey)`) to getting AES key in Recovery service `enc (email | aes_key_request) | new_surf_pub_key` (The status becomes `**RECOVERY_REGISTRATION_STARTED = 3**`)
4. Asks the user for a `2FA-code` from email, encrypts it with the public key of the SC-service and sends the message`enc (2FA | new_surf_pub_key)`via the TSC-contract (`send2FA (bytes code2FA)`) encrypting the message open (the status is becomes: `**RECOVERY_2FA_SENT = 2**`):
    - success - go to step 5 (receives from the TSC contract the message `event UpdatedRecoveryStatus (uint8 recoveryStatus, uint8 retriesLeft)` with the status `**RECOVERY_2FA_SUCCESS = 7**`)
    - failure - error `2FA code` (message from TCS-contract`event UpdatedRecoveryStatus (uint8 recoveryStatus, uint8 retriesLeft)`with status`**RECOVERY_2FA_CHECK_FAILED = 10**`), prompt the user to enter the code again (3 attempts) then request a new one code from `email` (after 9 unsuccessful attempts, receives a message from the TCS contract`event BannedRecoveryTill (uint32 bannedTill)`(The status changes to `**RECOVERY_2FA_BANNED = 12**` )
5. Receives from the TSС-contract (from the external message `event FinishedRecoveryRegistrastion (bytes recoveryData)` or by calling `getRecoveryData ()`) the public key from the Recovery service to enable multisig in 2/3 mode and the AES key Receives through the TSC contract the AES key for decrypting recovery data from the security card (`event FinishedRecoveryRegistrastion (bytes recoveryData)`). (the status changes to `**RECOVERY_REGISRTATION_FINISHED = 4**`)
6. Asks the user to present the card
7. Reads encrypted data from the card to restore access `surf_pub_key | multisig_addr | card CS | card auth password`
8. Decrypts data for recovery
9. Using `muslitig_addr` generates a message to call the function`submitUpdate` of the old multisig, as the argument `owners` passes the list of new custodians (sets`new_surf_pub_key` as the only custodian).
10. Asks the user to present a card and enter a pincode, using `card CS` refers to the card and signs the generated message to call`submitUpdate`
11. Sends the generated message with a call to the `submitUpdate` method of the old multisig`updateId`
12. Encrypts `email`,`multisig_addr` and `updateId` with the public key of the SC service, signs the received message with the private key of the Surf user and sends a request via the TSC contract (`requestUpdateConfirmation (bytes request)`) to sign the transaction to update the list of custodians multisig in Recovery service `enc (email | multisig_addr | updateId | sign_restore_tx) | surf_pub_key` (The status becomes `**RECOVERY_UPDATE_REQUESTED = 5**`)
13. Asks the user for the `2FA-code` from email, encrypts it with the public key of the SC-service and sends the message`enc (2FA | suf_pub_key)`via the TSC-contract (`send2FA (bytes code2FA)`), encrypting the message open (the status is becomes: `**RECOVERY_2FA_SENT = 2**`):
    - success - go to step 15 (receives from the TSC contract the message `event UpdatedRecoveryStatus (uint8 recoveryStatus, uint8 retriesLeft)` with the status `**RECOVERY_2FA_SUCCESS = 7**`)
    - failure - error `2FA code` (message from TCS-contract`event UpdatedRecoveryStatus (uint8 recoveryStatus, uint8 retriesLeft)`with status`**RECOVERY_2FA_CHECK_FAILED = 10**`), prompt the user to enter the code again (3 attempts) then request a new one code from `email` (after 9 unsuccessful attempts, receives a message from the TCS contract`event BannedRecoveryTill (uint32 bannedTill)`(The status changes to `**RECOVERY_2FA_BANNED = 12**`)
14. Receives a response about the success of the `confirmUpdate` call by the Recovery service (`event FinishedUpdateConfirm (uint256 encryptionPublicKey)`) (the status changes to`**RECOVERY_UPDATE_CONFIRMED = 6**`)
15. **Calls the multisig function `executeUpdate` using the security card for signing**
16. Receives confirmation of successful update of the multisig custodian list
17. Removes the encrypted binary blob `surf_pub_key | multisig_addr | card CS | card auth password` (user must attach a card for this action)

**TSC-contract:**

1. Receives a message from Surf with a request to create a key pair (`requestRecoveryRegistration (bytes request)`)
2. Forwards the message from Surf to `enc (email | aes_key_request) | ~~ surf_pub_key ~~ `` new_surf_pub_key` in SC service:
a. If there is at least one active card:
⇒ `event RequestedRecoveryRegistration (bytes request, uint256 encryptionPublicKey)`;
b. If there is no active card:
⇒ `event RequestedAccountRecovery (bytes request, uint256 encryptionPublicKey)`
3. Forwards the message `enc (2FA) | surf_pub_key` to SC-service (`send2FA (bytes code2FA)` ⇒ `event Sent2FA (bytes code2FA, uint256 encryptionPublicKey)`)
4. Receives from the SC-service the result of checking the 2FA code `updateRecoveryStatus (uint8 status)` ⇒ `event UpdatedRecoveryStatus (uint8 recoveryStatus, uint8 retriesLeft)`
In case of receiving the status`**RECOVERY_2FA_CHECK_FAILED = 10**` increases the counter of attempts by 1:
- on 3 and 6 attempts, go to step 2.
- after 9 unsuccessful attempts, he is banned for a day. Sends the message `event BannedRecoveryTill (uint32 bannedTill)` to Surf (The status changes to **`RECOVERY_2FA_BANNED = 12`**)
1. Sends the message `enc (export_recovery_aes_key)` (`finishRecoveryRegistration (bytes response)` ⇒`event FinishedRecoveryRegistrastion (bytes recoveryData)`) from the SC service to Surf
2. Forwards from Surf to SC-service the message `enc (email | multisig_addr | updateId | sign_restore_tx) | surf_pub_key` (`requestUpdateConfirmation (bytes request)` ⇒ `event RequestedUpdateConfirm (bytes request, uint256 encryptionPublicKey)`) (The status becomes `* RECOVERY_UPDATE_REQUESTED = 5 **`)
3. Forwards the message `enc (2FA) | surf_pub_key` to SC-service (`send2FA (bytes code2FA)` ⇒ `event Sent2FA (bytes code2FA, uint256 encryptionPublicKey)`)
4. Receives from the SC-service the result of checking the 2FA code `updateRecoveryStatus (status)` ⇒ `event UpdatedRecoveryStatus (uint8 recoveryStatus, uint8 retriesLeft)`
In case of receiving the status `* RECOVERY_2FA_CHECK_FAILED = 10 **` increases the counter of attempts by 1:
- on 3 and 6 attempts, go to step 6
- after 9 unsuccessful attempts, he is banned for a day. Sends message to Surf `event BannedRecoveryTill (uint32 bannedTill)`
1. Sends a response from the SC service to Surf about the success of the `confirmUpdate` call by the Recovery service (`finishUpdateConfirmation ()`⇒`event FinishedUpdateConfirm (uint256 encryptionPublicKey)`) (the status changes to`* RECOVERY_UPDATE_CONFIRMED = 6 **` )

**SC-service:**

1. Receiving a request from the TSC contract to obtain an encryption key in the Recovery service, decrypts the message (`event RequestedAccountRecovery (bytes request, uint256 encryptionPublicKey)`)
2. Generates a `2FA-code`, saves the match`new_surf_pub_key: 2FA` and sends a confirmation 2FA-code to `email`
3. Receives and decrypts the message (`event Sent2FA (bytes code2FA, uint256 encryptionPublicKey)`) from the TSC contract, extracts the `2FA code`, matches`new_surf_pub_key: 2FA` ~~ from the user via the TSC contract: ~~
    - success - calls in the TSC contract `updateRecoveryStatus (uint8 status)` with the status `* RECOVERY_2FA_SUCCESS = 7 **`
    - failure - return of error of verification of `2FA-code` in the TSC-contract is called`updateRecoveryStatus (uint8 status)`with status`* RECOVERY_2FA_CHECK_FAILED = 10 **`
4. Marks successful authentication and sends a request to the recovery service to generate a key pair `enc (sha (email) | aes_key_request)`
5. Sends the response from the Recovery service (encrypted AES key) to the user via the TSC contract (`finishRecoveryRegistration (bytes response)` ⇒`event FinishedRecoveryRegistrastion (bytes recoveryData)`)
6. Checks for the presence of an authentication mark for `new_surf_pub_key` and sends a request to the recovery service to sign the transaction to update the list of custodians`enc (sha256 (email) | multisig_addr | updateId | sign_restore_tx) | new_surf_pub_key`
7. Sends the response from the Recovery service (successful confirmation of the transaction) to the user via the TSC contract (`finishUpdateConfirmation ()` ⇒ `event FinishedUpdateConfirm (uint256 encryptionPublicKey)`)

**Recovery service:**

1. Decrypts a message requesting an AES key from an SC service
2. Computes `argon2 (sha256 (email), global_salt2)`
3. Exports the AES key by identifier `argon2 (sha256 (email), global_salt2)`
4. Replies the SC-service to the request with a message encrypted `new_surf_pub_key`` enc (AES-key)`
5. Decrypts the message with the signature request from the SC-service
6. Computes `argon2 (sha256 (email), global_salt)`
7. Checks for the presence of a key pair in HSM by identifier `argon2 (sha256 (email), global_salt)`
8. Extracts the multisig address and `updateId` from the request, generates a message with a call to the`confirmUpdate (updateId)`method to the`multisig_addr` address
9. The received message is signed in HSM with a private key from the found pair.
10. Repeats the call of the multisig method `confirmUpdate` until it receives confirmation of the passage of the message
11. Replies to the SC-service with the status of processing a multisig call
12. Removes from HSM the key pair with identifier `argon2 (sha256 (email), global_salt)` and the AES key with identifier `argon2 (sha256 (email), global_salt2)` after receiving confirmation of the transaction.

## 4. Recovery service specification

### 4.1 Rest API for SC service

Encrypted messages format (both requests and responses):

```
{
	"nonce": <Hex string>,
	"base64": <String>,
	"pub_key": Optional(<Hex string>),
}

```

Recovery service endpoints:

- getCard

    Decrypted output:

    ```
    {
    	"p1": <Hex string>,
    	"cs": <Hex string>,
    	"iv": <Hex string>,
    }

    ```

- createKeyPair(`sha256(email) | surf_pub_key` )

    Decrypted Input:

    ```
    {
    	"email": <String>,
    	"surf_pub_key": <Hex string>,
    }

    ```

    Decrypted Output:

    ```
    {
    	"pub_key": <Hex string>,
    }

    ```

- getAesKey(`sha256(email`)

    Decrypted intput:

    ```
    {
    	"email": <String>,
    	"surf_pub_key": <Hex string>,
    }

    ```

    Decrypted output:

    ```
    {
    	"aes_key": <Hex string>,
    }

    ```

- signTransaction(`sha256(email) | multisig_addr | updateId | new_surf_pub_key`)

    Decrypted input:

    ```
    {
    	"email": <String>,
    	"multisig_addr": <String>, 
    	"update_id": <String>,
    	"pub_key": <String>,
    }

    ```

    Decrypted output:

    ```
    //In case of successful call of multisig confirm_update
    {
    	"status": "Success"
    }

    //In case of errors
    {
    	"status": "Fail"
    }

    ```

### 4.2 Threat model

The main and most probable threat is the compromise of the SC service with which the Recovery service directly interacts and which it trusts, since The SC service performs authentication functions for accessing the Recovery service.

1. It is necessary to limit the network accessibility of the Recovery service of external users, for example, by placing it in our private network (physical - OVH vRack)
2. It is necessary to add a 2way-auth mechanism at the service level.
3. Regular monitoring of new files inside containers is necessary for both services, as well as restricting write rights for the service only to certain parts of the container.

## 5. Commands for Surf, TSC-contract, SC-service for Recovery-service

Commands (enum):

- `gen_pair_request` = 0
- `sign_restore_tx` = 1
- `aes_key_request` = 2
