package utils

/**
  * Created by denis on 09.01.16.
  */
object ApplicationMessage {
  object Error {
    val DUPLICATE_EMAIL = "User with this e-mail already exists"
    val DIFFERENT_PASSWORDS = "Entered passwords should be equal"
    val INVALID_REGISTRATION = "Invalid registration form"
  }

  object Info {
    def ATTEMPT_TO_REGISTER(username: String) = s"Attempt to register: $username"
    def USER_REGISTERED(username: String) = s"User $username registered"
  }

}
