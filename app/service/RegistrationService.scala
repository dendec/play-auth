package service

import controllers.RegistrationEntity
import model.{Role, User}
import play.api.Logger
import play.api.data.Form
import utils.{EncryptionHelper, ApplicationMessage}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Try

/**
  * Created by denis on 09.01.16.
  */
object RegistrationService {

  def register(filledRegisterForm: Form[RegistrationEntity]): Try[Future[Int]] = Try {
    if (!filledRegisterForm.hasErrors) {
      val registrationEntity = filledRegisterForm.get
      Logger.info(ApplicationMessage.Info.ATTEMPT_TO_REGISTER(registrationEntity.email))
      if (registrationEntity.password.equals(registrationEntity.passwordConfirm)) {
        User.DAO.getByEmail(registrationEntity.email).flatMap {
          case Some(user) =>
            Logger.warn(ApplicationMessage.Error.DUPLICATE_EMAIL)
            throw new Exception(ApplicationMessage.Error.DUPLICATE_EMAIL)
          case None =>
            val user = User(registrationEntity.name, registrationEntity.password, registrationEntity.email, Role.User.toString)
            Logger.info(ApplicationMessage.Info.USER_REGISTERED(user.email))
            register(user)
        }
      } else {
        Logger.warn(ApplicationMessage.Error.DIFFERENT_PASSWORDS)
        throw new Exception(ApplicationMessage.Error.DIFFERENT_PASSWORDS)
      }
    } else {
      Logger.warn(ApplicationMessage.Error.INVALID_REGISTRATION)
      throw new Exception(ApplicationMessage.Error.INVALID_REGISTRATION)
    }
  }

  def register(user: User) =
    User.DAO.create(user.copy(password = EncryptionHelper.makeHash(user.password, user.email)))
}
