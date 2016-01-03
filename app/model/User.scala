package model

import play.api.db.DB
import play.api.Play.current
import play.api.libs.json._
import play.api.libs.functional.syntax._
import slick.driver.H2Driver.api._
import scala.concurrent.Future

/**
  * Created by denis on 02.01.16.
  */

case class User(name: String, password: String, email: String, role: String,
                uid: String = java.util.UUID.randomUUID().toString,
                id: Option[Int] = None)

object User {

  implicit val userFormat = (
      (__ \ "name").format[String] and
      (__ \ "password").format[String] and
      (__ \ "email").format[String] and
      (__ \ "role").format[String] and
      (__ \ "uid").format[String] and
      (__ \ "id").formatNullable[Int]
    ) (User.apply, unlift(User.unapply))
}

class UserTable(tag: Tag) extends Table[User](tag, "User") {
  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)

  def name = column[String]("name")

  def password = column[String]("password")

  def email = column[String]("email")

  def role = column[String]("role")

  def uid = column[String]("uid")

  def * = (name, password, email, role, uid, id.?) <>((User.apply _).tupled, User.unapply)
}

object UserDAO extends GenericDAO[User, Int] {

  val userQuery = TableQuery[UserTable]

  def db: Database = Database.forDataSource(DB.getDataSource())

  db.run(DBIO.seq(
    userQuery.schema.create,
    userQuery.+=(User("admin", "admin", "admin@test.ru", "admin")),
    userQuery.+=(User("user", "user", "user@test.ru", "user"))
  ))

  override def create(entity: User): Future[Int] =
    db.run((userQuery returning userQuery.map(_.id)) += entity)

  override def update(id: Int, entity: User): Future[Int] =
    db.run(userQuery.filter(_.id === id).update(entity))

  override def delete(id: Int): Future[Int] =
    db.run(userQuery.filter(_.id === id).delete)

  override def read(id: Int): Future[User] =
    db.run(userQuery.filter(_.id === id).result.head)

  override def read: Future[Seq[User]] =
    db.run(userQuery.result)

  def getByEmail(email: String): Future[Option[User]] =
    db.run(userQuery.filter(_.email === email).result.headOption)

  def getByUID(uid: String): Future[Option[User]] =
    db.run(userQuery.filter(_.uid === uid).result.headOption)

}