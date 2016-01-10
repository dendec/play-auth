package model

import play.api.db.DB
import play.api.Play.current
import play.api.libs.json._
import slick.driver.H2Driver.api._
import scala.concurrent.Future

/**
  * Created by denis on 02.01.16.
  */

case class User(name: String, password: String, email: String, role: String,
                token: Option[String] = Some(java.util.UUID.randomUUID.toString),
                id: Option[Int] = None)

object User {

  implicit val userFormat = Json.format[User]

  protected class UserTable(tag: Tag) extends Table[User](tag, "User") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)

    def name = column[String]("name")

    def password = column[String]("password")

    def email = column[String]("email")

    def role = column[String]("role")

    def token = column[String]("token")

    def * = (name, password, email, role, token.?, id.?) <>((User.apply _).tupled, User.unapply)

    def roleKey = foreignKey("value", role, Role.DAO.roleQuery)(_.value)
  }

  object DAO extends GenericDAO[User, Int] {

    val userQuery = TableQuery[UserTable]

    val db: Database = Database.forDataSource(DB.getDataSource())

    db.run(userQuery.schema.create)

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

    def getByToken(token: String): Future[Option[User]] =
      db.run(userQuery.filter(_.token === token).result.headOption)

  }
}