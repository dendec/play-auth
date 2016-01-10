package controllers

import model.User
import play.api.mvc._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by denis on 02.01.16.
  */
trait Authentication {
  self: Controller =>

    def AuthenticateAction(getResult: (Option[User], RequestHeader) => Result) = Action.async { implicit request =>
      Authentication.parseUserFromRequest(request).map(getResult(_, request))
    }

    def AuthenticateActionAsync(getResult: (Option[User], Request[_]) => Future[Result]) = Action.async { implicit request =>
      Authentication.parseUserFromRequest(request).flatMap(getResult(_, request))
    }

}

object Authentication {

  val SESSION_TOKEN_KEY = "token"

  def parseUserFromRequest(implicit request: RequestHeader): Future[Option[User]] =
    request.session.get(SESSION_TOKEN_KEY) match {
      case Some(uid) => User.DAO.getByToken(uid)
      case None => Future(None)
    }
}