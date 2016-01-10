import model.{Role, User}
import play.api._
import play.api.mvc._
import play.api.mvc.Results._
import service.RegistrationService
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future

object Global extends GlobalSettings {

  override def onError(request: RequestHeader, ex: Throwable):Future[Result] = {
    Future(InternalServerError(views.html.error(ex)))
  }

  override def onHandlerNotFound(request: RequestHeader) = {
    Future(Redirect("/"))
  }

  override def onBadRequest(request: RequestHeader, error: String) = {
    Future(BadRequest(views.html.error(new Exception(error))))
  }

  override def onStart(app: Application) = {
    RegistrationService.register(User("admin", "admin", "admin@test.com", Role.Admin.toString))
    RegistrationService.register(User("user", "user", "user@test.com", Role.User.toString))
  }

}