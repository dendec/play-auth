package controllers

import model.User
import play.api.Logger
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc._
import play.api.Play.current
import play.api.i18n.Messages.Implicits._


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
      case Some(user) => Redirect(routes.HomeController.home)
      case None =>
        Ok {
          request.flash.get("auth-error") match {
            case Some(errorMessage) => views.html.login(loginForm.withError("password", errorMessage))
            case None => views.html.login(loginForm)
          }
        }
    }
  }

  def auth = AuthenticateAction { (maybeUser: Option[User], request: RequestHeader) =>
    maybeUser match {
      case Some(user) =>
        Logger.info(s"User ${user.name} authenticated")
        Redirect(routes.HomeController.home).withSession("uid" -> user.token.get)
      case None =>
        Logger.warn("Access denied")
        Redirect(routes.LoginController.login).flashing(
          "auth-error" -> "invalid login or password")
    }
  }

  def logout = Action {
    Redirect(routes.HomeController.home).withNewSession
  }

}
