package controllers

import model.User
import play.api.Logger
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc._
import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import service.LoginService
import utils.ApplicationMessage
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


/**
  * Created by denis on 09.01.16.
  */

case class LoginEntity(email: String, password: String)

class LoginController extends Controller with Authentication {

  val loginForm: Form[LoginEntity] = Form {
    mapping(
      "email" -> text,
      "password" -> text
    )(LoginEntity.apply)(LoginEntity.unapply)
  }

  def login = AuthenticateAction { (maybeUser: Option[User], request: RequestHeader) =>
    maybeUser match {
      case Some(user) => Redirect(routes.HomeController.index)
      case None =>
        Ok {
          request.flash.get("auth-error") match {
            case Some(errorMessage) => views.html.login(loginForm.withGlobalError(errorMessage))
            case None => views.html.login(loginForm)
          }
        }
    }
  }

  def auth = Action.async { implicit request =>
    LoginService.login(loginForm.bindFromRequest()).map{
      case Some(user) =>
        Logger.info(ApplicationMessage.Info.USER_LOGIN(user.email))
        Redirect(routes.HomeController.index).withSession(Authentication.SESSION_TOKEN_KEY -> user.token.get)
      case None =>
        Logger.warn(ApplicationMessage.Error.AUTH_ERROR)
        Redirect(routes.LoginController.login).flashing(
          "auth-error" -> ApplicationMessage.Error.AUTH_ERROR)
    }
  }

  def logout = AuthenticateActionAsync { (maybeUser: Option[User], request: RequestHeader) =>
    maybeUser match {
      case Some(user) =>
        LoginService.logout(user).map(_ => Redirect(routes.LoginController.login).withNewSession)
      case None =>
        Future(Redirect(routes.LoginController.login).withNewSession)
    }
  }

}
