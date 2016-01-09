package controllers

import model.{Role, User}
import play.api.Logger
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc.{Controller, Result, Action}
import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import service.RegistrationService

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

/**
  * Created by denis on 09.01.16.
  */
case class RegistrationEntity(name: String, email: String, password: String, passwordConfirm: String)

class RegistrationController extends Controller with Authentication {

  val registerForm: Form[RegistrationEntity] = Form {
    mapping(
      "name" -> nonEmptyText,
      "email" -> email,
      "password" -> nonEmptyText(minLength = 8),
      "password_confirm" -> nonEmptyText(minLength = 8)
    )(RegistrationEntity.apply)(RegistrationEntity.unapply)
  }

  def register = Action.async { implicit request =>
    AuthUtils.parseUserFromCookie.flatMap {
      case Some(user) =>
        Future(Redirect(routes.HomeController.home))
      case None =>
        request.method match {
          case "GET" =>
            Future(Ok(views.html.register(registerForm)))
          case "POST" =>
            RegistrationService.register(registerForm.bindFromRequest()) match {
              case Success(result) => result.map(_ => Redirect(routes.LoginController.login))
              case Failure(ex) => Future(BadRequest(views.html.register(registerForm.withGlobalError(ex.getMessage))))
            }
        }
    }
  }
}
