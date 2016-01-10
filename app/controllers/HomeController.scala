package controllers

import model.{User, Role}
import play.api.mvc.Controller

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by denis on 09.01.16.
  */
class HomeController extends Controller with Authentication {

  def index = AuthenticateAction { (maybeUser: Option[User], _) =>
    maybeUser match {
      case Some(user) => Ok(views.html.home.index(user)(null))
      case None => Redirect(routes.LoginController.login)
    }
  }

  def users = AuthenticateActionAsync (
    (maybeUser: Option[User], _) =>
      maybeUser match {
        case Some(user) => User.DAO.read.map(users => Ok(views.html.home.users(user, users)))
        case None => Future(Redirect(routes.LoginController.login))
      }, Authentication.AdminOnly
  )

}
