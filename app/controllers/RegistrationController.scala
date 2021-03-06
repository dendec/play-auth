package controllers

import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc.{Controller, Action}
import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import service.RegistrationService

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.Logger

/**
  * Created by denis on 09.01.16.
  */
case class RegistrationEntity(name: String, email: String, password: String, passwordConfirm: String)

class RegistrationController extends Controller {

  val registerForm: Form[RegistrationEntity] = Form {
    mapping(
      "name" -> nonEmptyText,
      "email" -> email,
      "password" -> nonEmptyText(minLength = 8),
      "password_confirm" -> nonEmptyText(minLength = 8)
    )(RegistrationEntity.apply)(RegistrationEntity.unapply)
  }

  def register = Action.async { implicit request =>
    Authentication.parseUserFromRequest.flatMap {
      case Some(user) =>
        Future(Redirect(routes.HomeController.index))
      case None =>
        request.method match {
          case "GET" =>
            Future(Ok(views.html.register(registerForm)))
          case "POST" =>
            val filledRegisterForm = registerForm.bindFromRequest()
            RegistrationService.register(filledRegisterForm).map(_ => Redirect(routes.LoginController.login)).
              recover {
                case ex =>
                  Logger.warn(ex.getMessage)
                  BadRequest(views.html.register(filledRegisterForm.withGlobalError(ex.getMessage)))
              }
        }
    }
  }
}
