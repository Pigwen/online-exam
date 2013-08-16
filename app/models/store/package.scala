/**
 *
 */
package models

/**
 * @author mge
 *
 */
import scala.slick.driver.MySQLDriver.simple._
import scala.collection.mutable.ListBuffer
import play.api.db._
import play.api.Play.current

package object store {
  private lazy val database = Database.forDataSource(DB.getDataSource())
  object SubjectTB extends Table[Subject]("SUBJECTS") {
    def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)
    def title = column[String]("TITLE", O.NotNull)
    def answer = column[String]("ANSWER", O.NotNull)
    def * = id.? ~ title ~ answer <> (t => Subject(t._1, t._2, t._3), { s: Subject => Some((s.id, s.title, s.answer)) })

    def findAll = database.withSession { implicit db: Session =>
      val q = for {
        s <- SubjectTB
        c <- ChoiceTB if s.id === c.subjectID
      } yield (s, c)

      import scala.collection.mutable.Map
      val result = q.list.foldLeft((Map[Long, Subject](), Map[Long, List[Choice]]())) {
        case ((smap, cmap), (s, c)) =>
          smap += ((s.id.get, s))
          val clist = cmap.getOrElse(c.subjectID.get, Nil)
          cmap += ((c.subjectID.get, c :: clist))
          (smap, cmap)
      }

      result._1 map {
        case (sid, subject) =>
          subject.copy(choices = result._2(sid))
      }
    }

    def find(pageable: Pageable): Page[Subject] = database.withTransaction { implicit db: Session =>
      val subjectQuery = SubjectTB.drop(pageable.offset).take(pageable.pageSize)
      val q = for {
        s <- subjectQuery
        c <- ChoiceTB if s.id === c.subjectID
      } yield (s, c)

      import scala.collection.mutable.Map
      val count = Query(SubjectTB.length).first
      val result = q.foldLeft((Map[Long, Subject](), Map[Long, List[Choice]]())) {
        case ((smap, cmap), (s, c)) =>
          smap += ((s.id.get, s))
          val clist = cmap.getOrElse(c.subjectID.get, Nil)
          cmap += ((c.subjectID.get, c :: clist))
          (smap, cmap)
      }

      val data = result._1 map {
        case (sid, subject) =>
          subject.copy(choices = result._2(sid))
      }
      new Page(pageable, count, data.toSeq: _*)
    }

    def findOne(id: Long): Subject = database.withSession { implicit db: Session =>
      val q = for {
        s <- SubjectTB if s.id === id
        c <- ChoiceTB if s.id === c.subjectID
      } yield (s, c)
      val choices: ListBuffer[Choice] = ListBuffer()
      var subject: Subject = Subject();
      q.list.foldLeft(Subject()) {
        case (s, t) =>
          t._1.copy(choices = t._2 :: s.choices)
      }
    }

    def save(s: Subject): Subject = database.withTransaction { implicit db: Session =>
      val subjectId = s.id match {
        case None =>
          SubjectTB returning (id) insert (s)
        case Some(sid) =>
          SubjectTB.filter(_.id === sid) update (s)
          ChoiceTB.filter(_.subjectID === sid) delete (db)
          sid
      }
      (ChoiceTB.description ~ ChoiceTB.subjectID) insertAll (s.choices.distinct.filterNot(_.description.isEmpty()).map { c => (c.description, subjectId) }: _*)
      s.copy(id = Some(subjectId))
    }

    def delete(sid: Long) = database.withTransaction { implicit db: Session =>
      ChoiceTB.filter(_.subjectID === sid).delete
      SubjectTB.filter(_.id === sid).delete
    }

  }

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
    (SubjectTB.ddl ++ ChoiceTB.ddl).create
  }
}