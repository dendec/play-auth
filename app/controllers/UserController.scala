package controllers

import model.{User, UserDAO}
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}
import scala.concurrent.ExecutionContext.Implicits.global

import scala.util.{Failure, Success}

/**
  * Created by denis on 02.01.16.
  */
class UserController extends Controller {

  def create = Action.async(parse.json) { request =>
    val user = request.body.as[User]
    UserDAO.create(user).map(result => Ok(Json.toJson(result))).recover{
      case ex => InternalServerError(ex.getMessage)
    }
  }

  def read(id: Int) = Action.async {
    UserDAO.read(id).map { user =>
      Ok(Json.toJson(user))
    }.recover{
      case ex => InternalServerError(ex.toString)
    }
  }

  def readAll = Action.async {
    UserDAO.read.map { users =>
      Ok(Json.toJson(users))
    }.recover{
      case ex => InternalServerError(ex.toString)
    }
  }

  def update(id: Int) = Action.async(parse.json) { request =>
    val user = request.body.as[User]
    UserDAO.update(id, user).map(result => Ok(Json.toJson(result))).recover{
      case ex => InternalServerError(ex.getMessage)
    }
  }
}
