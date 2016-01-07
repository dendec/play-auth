package controllers

import model.{UserDAO, User}
import play.api.mvc._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by denis on 02.01.16.
  */
trait Authentication {
  self: Controller =>

  def AuthenticateAction(getResult: (Option[User], RequestHeader) => Result) = Action.async { implicit request =>
    authenticate(getResult.curried.andThen(f => Future(f.apply(request))), request)
  }

  def AuthenticateActionAsync(getResult: (Option[User], Request[_]) => Future[Result]) = Action.async { implicit request =>
    authenticate(getResult(_, request), request)
  }

  def authenticate(getResult: (Option[User]) => Future[Result], request: RequestHeader) =
    AuthUtils.parseUserFromRequest(request).flatMap(getResult)

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