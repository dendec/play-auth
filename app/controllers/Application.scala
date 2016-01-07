package controllers

import model.{User, UserDAO}
import play.api.Logger
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.Play.current
import play.api.i18n.Messages.Implicits._

case class LoginFormEntity(email: String, password: String)

case class RegisterFormEntity(name: String, email: String, password: String, passwordConfirm: String)

class Application extends Controller with Authentication {

  val personForm: Form[LoginFormEntity] = Form {
    mapping(
      "email" -> text,
      "password" -> text
    )(LoginFormEntity.apply)(LoginFormEntity.unapply)
  }

  val registerForm: Form[RegisterFormEntity] = Form {
    mapping(
      "name" -> nonEmptyText,
      "email" -> email,
      "password" -> nonEmptyText(minLength = 8),
      "password_confirm" -> nonEmptyText(minLength = 8)
    )(RegisterFormEntity.apply)(RegisterFormEntity.unapply)
  }

  def home = AuthenticateAction { (maybeUser: Option[User], _) =>
    maybeUser match {
      case Some(user) => Ok(views.html.home(user))
      case None => Redirect(routes.Application.login)
    }
  }

  def register = Action.async { implicit request =>
    AuthUtils.parseUserFromCookie.flatMap {
      case Some(user) =>
        Future(Redirect(routes.Application.home))
      case None =>
        request.method match {
          case "GET" =>
            Future(Ok(views.html.register(registerForm)))
          case "POST" =>
            processRegistrationInformation(registerForm.bindFromRequest())
        }
    }
  }

  private def processRegistrationInformation(filledRegisterForm: Form[RegisterFormEntity]): Future[Result] = {
    if (!filledRegisterForm.hasErrors) {
      val registerFormEntity = filledRegisterForm.get
      Logger.info(s"registration: ${registerFormEntity.toString}")
      if (registerFormEntity.password.equals(registerFormEntity.passwordConfirm)) {
        UserDAO.getByEmail(registerFormEntity.email).flatMap {
          case Some(user) =>
            Logger.warn("User with this email already exists")
            Future(Ok(views.html.register(filledRegisterForm.withGlobalError("User with this email already exists"))))
          case None =>
            UserDAO.create(User(registerFormEntity.name, registerFormEntity.password, registerFormEntity.email, "USER"))
              .map(_ => Redirect(routes.Application.login))
        }
      } else {
        Logger.warn("Entered passwords should be equal")
        Future(Ok(views.html.register(filledRegisterForm.withGlobalError("Entered passwords should be equal"))))
      }
    } else {
      Future(Ok(views.html.register(filledRegisterForm)))
    }
  }

  def login = AuthenticateAction { (maybeUser: Option[User], request: RequestHeader) =>
    maybeUser match {
      case Some(user) => Redirect(routes.Application.home)
      case None =>
        Ok {
          request.flash.get("auth-error") match {
            case Some(errorMessage) => views.html.login(personForm.withError("password", errorMessage))
            case None => views.html.login(personForm)
          }
        }
    }
  }

  def auth = AuthenticateAction { (maybeUser: Option[User], request: RequestHeader) =>
    maybeUser match {
      case Some(user) =>
        Logger.info(s"User ${user.name} authenticated")
        Redirect(routes.Application.home).withSession("uid" -> user.uid)
      case None =>
        Logger.warn("Access denied")
        Redirect(routes.Application.login).flashing(
          "auth-error" -> "invalid login or password")
    }
  }

  def logout = Action {
    Redirect(routes.Application.home).withNewSession
  }

}
