package controllers

import model.{User, UserDAO}
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.Json
import play.api.mvc._
import play.twirl.api.Html
import play.api.Play.current
import play.api.i18n.Messages.Implicits._

case class LoginForm(email: String, password: String)

class Application extends Controller with Authentication {

  val personForm: Form[LoginForm] = Form {
    mapping(
      "email" -> text,
      "password" -> text
    )(LoginForm.apply)(LoginForm.unapply)
  }

  def index = AuthenticateAction {
    case Some(user) => Redirect(routes.Application.home)
    case None => Redirect(routes.Application.login)
  }

  def home = AuthenticateAction {
    case Some(user) => Ok(views.html.home(user))
    case None => Redirect(routes.Application.login)
  }

  def login(error: String) = AuthenticateAction {
    case Some(user) => Redirect(routes.Application.home)
    case None => Ok(views.html.login(personForm))
  }

  def auth = AuthenticateAction { maybeUser =>
    maybeUser match {
      case Some(user) => Redirect(routes.Application.home).withSession("uid" -> user.uid)
      case None => Redirect(routes.Application.login("error"))
    }
  }

  def logout = Action {
    Redirect(routes.Application.login).withNewSession
  }

  //  { user =>
  //    Ok(Json.toJson(user)).withSession("login" -> user.login)
  //  }

}
