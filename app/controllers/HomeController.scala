package controllers

import model.User
import play.api.mvc.Controller

/**
  * Created by denis on 09.01.16.
  */
class HomeController extends Controller with Authentication {

  def home = AuthenticateAction { (maybeUser: Option[User], _) =>
    maybeUser match {
      case Some(user) => Ok(views.html.home(user))
      case None => Redirect(routes.LoginController.login)
    }
  }
}
