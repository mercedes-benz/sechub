/**
 * This package contains classes to protect data at-rest.
 * 
 * The package is named persistence, because the persistence 
 * layer is used to help store data in a database or other data storage systems.
 * 
 * The requirements for the cryptographic algorithms for data at rest are:
 * 
 * - authenticated encryption (AE)
 * - nonce misuse-resistant
 *                            
 * Authenticated encryption (AE) provides confidentiality and integrity at the same time.
 * Confidentiality is provided by encrypting and integrity by hashing the data. 
 * If during decryption, the hash and the data do not match, the data has been corrupted or tampered with.
 * 
 * Because AE algorithms do both, encrypt and hash at the same time they are difficult to incorrectly use.
 *  
 * Furthermore, it is impossible to ensure a nonce (initialization vector) is not used more than once in 
 * a multi-server database scenario, therefore a nonce misuse-resistant algorithm is required.
 *  
 * In theory one could make sure, that there is a single data entry point which keeps track of the used nonces.
 * Before writing the data to the database, the single data entry point generates a nonce and looks up into the 
 * list of used nonces whether the nonce is already used or not. 
 * If the nonce is already used the single data entry point needs to generate a nonce until the nonce is unique 
 * and has never been used before. 
 * The consequence is, that data cannot be written in parallel, which is unacceptable and in the case of a 
 * distributed application like SecHub impossible as it would slow down the rate at which date can be inserted
 * into the database.
 * 
 * To avoid those problems, a nonce misuse-resistant algorithm is required. A nonce misuse-algorithm provides 
 * security even if the same combination of secret key, additional data, nonce and plain text is encrypted 
 * and stored more than once.
 * 
 * There are a number of authenticated encryption algorithms. However only a few are nonce misuse-resistant.
 * Examples for misuse resistant algorithms are:
 * 
 * - AES-GCM-SIV
 * - AES-SIV
 * - Deoxys-II
 * - COLM
 * - Romulus-M
 *  
 * Furthermore, symmetric encryption algorithms are not in danger to be completely broken by quantum computers 
 * (see: https://security.stackexchange.com/a/103560).
 * However, it might be necessary to use different cryptographic algorithms in the future as attacks on 
 * existing algorithms become possible.
 * 
 * Bruce Schneier puts it this way:
 * 
 * > "In the face of all that uncertainty, agility is the only way to maintain security."
 * (source: https://www.schneier.com/essays/archives/2022/08/nists-post-quantum-cryptography-standards-competition.html)
 * 
 * This package also contains classes which can help with algorithm rotation and key rotation:
 * 
 * - Key rotation is used to change the secret key. 
 * - Algorithm rotation can be used if the algorithms need to be exchanged.
 */
package com.mercedesbenz.sechub.commons.core.security.persistence;