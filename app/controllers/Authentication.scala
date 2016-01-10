package controllers

import controllers.Authentication.UserAuthCondition
import model.{Role, User}
import play.api.mvc._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by denis on 02.01.16.
  */
trait Authentication {
  self: Controller =>

    def AuthenticateAction(getResult: (Option[User], Request[_]) => Result,
                           canAccept: UserAuthCondition = _ => true) =
      Action.async { implicit request =>
        Authentication.parseUserFromRequest(canAccept).map(getResult(_, request))
      }

    def AuthenticateActionAsync(getResult: (Option[User], Request[_]) => Future[Result],
                                canAccept: UserAuthCondition = _ => true) =
      Action.async { implicit request =>
        Authentication.parseUserFromRequest(canAccept).flatMap(getResult(_, request))
      }

}

object Authentication {

  type UserAuthCondition = User => Boolean
  val AdminOnly: UserAuthCondition = _.role.equals(Role.Admin.toString)

  val SESSION_TOKEN_KEY = "token"

  def parseUserFromRequest(implicit request: RequestHeader): Future[Option[User]] =
    request.session.get(SESSION_TOKEN_KEY) match {
      case Some(uid) => User.DAO.getByToken(uid)
      case None => Future(None)
    }

  def parseUserFromRequest(canAccept: UserAuthCondition)(implicit request: RequestHeader): Future[Option[User]] =
    parseUserFromRequest.map(_.map(user => if (canAccept(user)) user else null))
}
