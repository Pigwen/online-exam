package models

import play.api.Play.current
import scala.slick.driver.MySQLDriver.simple._
import play.api.db._

object Db {
  sealed trait Question {
    def id: Column[Long]
  }

  private lazy val database = Database.forDataSource(DB.getDataSource())
  
  case class Choice(id: Long, title: String, options: String, answer: String)
  object Choice extends Table[(Long, String, String, String)]("CHOICES") with Question {
    def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)
    def title = column[String]("TITLE")
    def options = column[String]("OPTIONS")
    def answer = column[String]("ANSWER")
    def * = id ~ title ~ options ~ answer

    def findAll = Db.database.withSession { implicit db: Session =>
      Choice.map(c => c.id ~ c.title ~ c.options ~ c.answer).list.map(t => Choice(t._1, t._2, t._3, t._4))
    }
  }

  private val url = "jdbc:mysql://localhost/exam?useUnicode=true&characterEncoding=utf-8"
  private val driver = "com.mysql.jdbc.Driver"
  private val user = "root"
  private val password = ""
  def createTable = Database.forURL(url, user = user, password = password, driver = driver) withSession {
    import Database.threadLocalSession
    Choice.ddl.create
  }
}