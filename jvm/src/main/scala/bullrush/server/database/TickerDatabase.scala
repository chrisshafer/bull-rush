package bullrush.server.database

import java.sql.SQLException

import slick.driver.PostgresDriver.api._
import slick.jdbc.meta.MTable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.util.{Success, Failure}

object TickerDatabase {

  case class User(id: Option[Int], username: String, name: String, email: String)
  private val db = Database.forConfig("database")

  def populateDBDDLIfNonexistant(): Future[Unit]={
    createIfNotExistant(users,tickers,subscriptions)
  }

  def createIfNotExistant(schemas: TableQuery[_ <: Table[_]]*): Future[Unit] ={
    val tables = Await.result(db.run(MTable.getTables), 10 seconds)

    tables.filter{
      case table => schemas.map(_.baseTableRow.tableName).contains(table.name.name)
    } match {
      case exist if exist.toList.length < 3 =>
        db.run(DBIO.seq(users.schema.create, tickers.schema.create,subscriptions.schema.create))
      case _ =>
        Future(new RuntimeException("ERROR SCHEMA NOT GENERATED"))
    }
  }

  def addUser(userName: String, name: String, email: String): Future[Unit] ={
    db.run(DBIO.seq( users += User(None,userName,name,email)))
  }
  def listUsers(): Future[Seq[User]] = {
    val action = users.result
    db.run(action)
  }


  class TickersUsers(tag: Tag) extends  Table[(Int,Int,Int)](tag, "TICKS_TO_USERS"){
    def id = column[Int]("TICKER_TO_USER_ID", O.PrimaryKey, O.AutoInc)
    def userId = column[Int]("USER_ID")
    def tickerId = column[Int]("TICKER_ID")
    def userFk = foreignKey("USER_FK",userId,users)(_.id)
    def tickerFk = foreignKey("TICKER_FK",tickerId,tickers)(_.id)
    def * = (id, userId, tickerId)
  }
  val subscriptions = TableQuery[TickersUsers]

  class Users(tag: Tag) extends Table[User](tag, "USERS") {
    def id = column[Int]("USER_ID", O.PrimaryKey,  O.AutoInc)
    def username = column[String]("USERNAME")
    def name = column[String]("NAME")
    def email = column[String]("EMAIL")
    def * = (id.?, username, name, email) <> (User.tupled, User.unapply)
  }
  val users = TableQuery[Users]

  class Tickers(tag: Tag) extends Table[(Int, String)](tag, "TICKERS") {
    def id = column[Int]("TICKER_ID", O.PrimaryKey, O.AutoInc)
    def symbol = column[String]("SYMBOL")
    def * = (id, symbol)
  }
  val tickers = TableQuery[Tickers]

}
