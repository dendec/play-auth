package utils

import java.nio.charset.StandardCharsets
import java.security.MessageDigest

/**
  * Created by denis on 09.01.16.
  */
object EncryptionHelper {

  private val HASH_ITERATIONS = 500
  private val messageDigest = MessageDigest.getInstance("SHA-256")

  def generateToken = java.util.UUID.randomUUID.toString

  def makeHash(value: String, salt: Any) = {
    val hash = (0 to HASH_ITERATIONS).foldLeft(messageDigest.digest(value.getBytes(StandardCharsets.UTF_8))){
      (hash, _) => messageDigest.digest(hash ++ salt.toString.getBytes(StandardCharsets.UTF_8))
    }
    org.apache.commons.codec.binary.Base64.encodeBase64String(hash)
  }
}
