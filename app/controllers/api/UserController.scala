package controllers.api

import controllers.Authentication
import model.User
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}
import utils.EncryptionHelper

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by denis on 02.01.16.
  */
class UserController extends Controller with Authentication {

  def create = Action.async(parse.json) { request =>
    val user = request.body.as[User]
    User.DAO.create(user.copy(token = Some(EncryptionHelper.generateToken))).map(result => Ok(Json.toJson(result))).recover {
      case ex => InternalServerError(ex.getMessage)
    }
  }

  def read(id: Int) = Action.async {
    User.DAO.read(id).map { user =>
      Ok(Json.toJson(user))
    }.recover {
      case ex => InternalServerError(ex.toString)
    }
  }

  def readAll = Action.async {
    User.DAO.read.map { users =>
      Ok(Json.toJson(users))
    }.recover {
      case ex => InternalServerError(ex.toString)
    }
  }

  def update(id: Int) = Action.async(parse.json) { request =>
    val user = request.body.as[User]
    User.DAO.update(id, user).map(result => Ok(Json.toJson(result))).recover {
      case ex => InternalServerError(ex.getMessage)
    }
  }
}
