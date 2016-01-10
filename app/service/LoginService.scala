package service

import controllers.LoginEntity
import model.User
import play.api.data.Form
import utils.EncryptionHelper
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by denis on 10.01.16.
  */
object LoginService {

  def login(filledLoginForm: Form[LoginEntity]): Future[Option[User]] = {
    if (!filledLoginForm.hasErrors) {
      val loginEntity = filledLoginForm.get
      User.DAO.getByEmail(loginEntity.email).map { maybeUser =>
        maybeUser.filter(user => user.password.equals(EncryptionHelper.makeHash(loginEntity.password, user.email)))
      }
    } else
      Future(None)
  }

  def logout(user: User) =
   User.DAO.update(user.id.get, user.copy(token = Some(EncryptionHelper.generateToken)))

}
