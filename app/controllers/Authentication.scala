package controllers

import model.{UserDAO, User}
import play.api.Logger
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc.{RequestHeader, Result, Action, Controller}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by denis on 02.01.16.
  */
trait Authentication {
  self: Controller =>

  def AuthenticateAction(getResult: Option[User] => Result) = Action.async { implicit request =>
    val futureMaybeUser = AuthUtils.parseUserFromRequest(request)
    futureMaybeUser.map { maybeUser =>
      maybeUser match {
        case Some(user) =>
          Logger.info(s"User ${user.name} authenticated")
        case None =>
          Logger.warn("Access denied")
      }
      getResult(maybeUser)
    }
  }
}

object AuthUtils {
  def parseUserFromCookie(implicit request: RequestHeader): Future[Option[User]] =
    request.session.get("uid") match {
      case Some(uid) => UserDAO.getByUID(uid)
      case None => Future(None)
    }

  def parseUserFromRequest(implicit request: RequestHeader): Future[Option[User]] = {
    val query = request.queryString.map { case (k, v) => k -> v.mkString }
    val maybeEmail = query get "email"
    val maybePassword = query get "password"

    (maybeEmail, maybePassword) match {
      case (Some(email), Some(password)) => UserDAO.getByEmail(email).map { maybeUser =>
        maybeUser.filter(user => user.password.equals(password))
      }
      case _ => parseUserFromCookie
    }
  }
}