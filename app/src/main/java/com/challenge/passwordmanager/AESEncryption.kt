package com.challenge.passwordmanager

import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

class AESEncryption {

    private fun generateSalt(): ByteArray {
        val saltRandom = SecureRandom()
        val salt = ByteArray(256)

        saltRandom.nextBytes(salt)

        return salt
    }

    fun generateIV(): ByteArray {
        // Use unique random number for each function call
        val ivRandom = SecureRandom()

        val iv = ByteArray(16)
        ivRandom.nextBytes(iv)

        return iv
    }

    fun generateEncryptedKey(password: String): SecretKeySpec {
        val passwordCA = password.toCharArray()
        val salt = generateSalt()

        // Generate password based key specification with password and salt with maximum iteration count of 1324
        val pbKeySpec = PBEKeySpec(passwordCA, salt, 1324, 256)

        // Use Password Based Key Derivation Function with SHA1 encryption
        val secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")

        // Convert key to byte array
        val keyBytes = secretKeyFactory.generateSecret(pbKeySpec).encoded

        // Wrap byte array into the SecretKaySpec object
        return SecretKeySpec(keyBytes, "AES")
    }

    // For now this encrypts the password twice over - I meant to add functionality so that this would
    // add a layer of security such that users with identical passwords would still have a unique
    // cipher due to the salting and block cipher efforts. An extra safe password!
    fun encryptPassword(password: String, keySpec: SecretKeySpec, iv: ByteArray): String {
        val passwordBA = password.toByteArray()
        val ivSpec = IvParameterSpec(iv)

        // Use Advanced Encryption Standard with cipher block chaining mode
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec)
        return String(cipher.doFinal(passwordBA))
    }

    fun decryptPassword(encryptedPassword: ByteArray, keySpec: SecretKeySpec, iv: ByteArray): String {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val ivSpec = IvParameterSpec(iv)
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec)

        return String(cipher.doFinal(encryptedPassword))
    }
}