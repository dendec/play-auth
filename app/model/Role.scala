package model

import play.api.db.DB
import play.api.Play.current
import play.api.libs.json._
import slick.driver.H2Driver.api._
import scala.concurrent.Future

/**
  * Created by denis on 09.01.16.
  */
case class Role(value: String)

object Role extends Enumeration {

  val User, Admin = Value

  implicit val roleFormat = Json.format[Role]

  protected class RoleTable(tag: Tag) extends Table[Role](tag, "Role") {

    def value = column[String]("value", O.PrimaryKey)

    def * = value <> (Role.apply, Role.unapply)

  }

  object DAO {

    val roleQuery = TableQuery[RoleTable]

    def db: Database = Database.forDataSource(DB.getDataSource())

    db.run(DBIO.seq(
      roleQuery.schema.create,
      roleQuery.+=(Role(Admin.toString)),
      roleQuery.+=(Role(User.toString))
    ))

    def read: Future[Seq[Role]] =
      db.run(roleQuery.result)

  }
}