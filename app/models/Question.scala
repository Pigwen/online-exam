package models

import play.api.Play.current
import scala.slick.driver.MySQLDriver.simple._
import play.api.db._
import scala.collection.mutable.ListBuffer

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
      val q = for {
        s <- SubjectTB
        c <- ChoiceTB if s.id === c.subjectID
      } yield (s, c)
      import scala.collection.mutable.Map
      val smap = Map[Long, Subject]()
      val cmap = Map[Long, List[Choice]]()
      q.list.map {
        case (s, c) =>
          smap += ((s.id.get, s))
          val clist = cmap.getOrElse(c.subjectID.get, Nil)
          cmap += ((c.subjectID.get, c :: clist))
      }
      smap.map {
        t => t._2.copy(choices = cmap(t._1))
      }
    }

    def findOne(id: Long): Subject = Db.database.withSession { implicit db: Session =>
      val q = for {
        s <- SubjectTB if s.id === id
        c <- ChoiceTB if s.id === c.subjectID
      } yield (s, c)
      val choices: ListBuffer[Choice] = ListBuffer()
      var subject: Subject = Subject();
      q.list.map {
        case (s, c) =>
          choices += c
          subject = s
      }
      subject.copy(choices = choices.toList)
    }

    def save(s: Subject): Subject = {
      Db.database.withTransaction { implicit db: Session =>
        val subjectId = s.id match {
          case None =>
            SubjectTB returning (id) insert (s)
          case Some(sid) =>
            SubjectTB.filter(_.id === sid) update (s)
            ChoiceTB.filter(_.subjectID === sid) delete(db)
            sid
        }
        (ChoiceTB.description ~ ChoiceTB.subjectID) insertAll (s.choices.distinct.filterNot(_.description.isEmpty()).map { c => (c.description, subjectId) }:_*)
        s.copy(id = Some(subjectId))
      }
    }
    
    def delete(sid: Long) = {
      Db.database.withTransaction { implicit db: Session =>
        ChoiceTB.filter(_.subjectID === sid).delete
        SubjectTB.filter(_.id === sid).delete
      }
    }
  }

  case class Choice(id: Option[Long] = None, description: String, subjectID: Option[Long])
  object ChoiceTB extends Table[Choice]("CHOICES") {
    def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)
    def description = column[String]("DESCRIPTION", O.NotNull)
    def subjectID = column[Long]("SUBJECT_ID", O.NotNull)
    def subject = foreignKey("SUBJECT_FK", subjectID, SubjectTB)(_.id)
    def * = id.? ~ description ~ subjectID.? <> (Choice, Choice.unapply _)
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