package models

import play.api.Play.current
import scala.slick.driver.MySQLDriver.simple._
import play.api.db._

object Db {
  private lazy val database = Database.forDataSource(DB.getDataSource())

  case class Subject(id: Option[Long] = None, title: String = "", answer: String = "", choices: List[Choice] = Nil)
  object SubjectTB extends Table[Subject]("SUBJECTS") {
    def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)
    def title = column[String]("TITLE", O.NotNull)
    def answer = column[String]("ANSWER", O.NotNull)
    def * = id.? ~ title ~ answer <> (t => Subject(t._1, t._2, t._3), { s: Subject => Some((s.id, s.title, s.answer)) })

    def findAll = Db.database.withSession { implicit db: Session =>
      SubjectTB.map(c => c.id ~ c.title ~ c.answer).list.map(t => Subject(Some(t._1), t._2, t._3))
    }

    def create(s: Subject) = {
      Db.database.withTransaction { implicit db: Session =>
        val subjectId = SubjectTB returning (id) insert (s)
        (ChoiceTB.description ~ ChoiceTB.subjectID) insertAll (s.choices.map { c => (c.description, subjectId) }: _*)
      }
    }
  }

  case class Choice(description: String)
  object ChoiceTB extends Table[(Long, String, Long)]("CHOICES") {
    def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)
    def description = column[String]("DESCRIPTION", O.NotNull)
    def subjectID = column[Long]("SUBJECT_ID", O.NotNull)
    def subject = foreignKey("SUBJECT_FK", subjectID, SubjectTB)(_.id)
    def * = id ~ description ~ subjectID
    def idx = index("idx_a", (subjectID, description), unique = true)
  }

  private val url = "jdbc:mysql://localhost/exam?useUnicode=true&characterEncoding=utf-8"
  private val driver = "com.mysql.jdbc.Driver"
  private val user = "root"
  private val password = ""
  def createTable = Database.forURL(url, user = user, password = password, driver = driver) withSession {
    import Database.threadLocalSession
    SubjectTB.ddl ++ ChoiceTB.ddl create
  }
}