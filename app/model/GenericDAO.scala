package model

import scala.concurrent.Future

/**
  * Created by denis on 02.01.16.
  */
trait GenericDAO[E, PK] {
  def create(entity: E): Future[PK]

  def read(id: PK): Future[E]

  def read: Future[Seq[E]]

  def update(id: PK, entity: E): Future[PK]

  def delete(id: PK): Future[PK]
}
