package models

case class Subject(id: Option[Long] = None, title: String = "", answer: String = "", choices: List[Choice] = Nil)

case class Choice(id: Option[Long] = None, description: String, subjectID: Option[Long])
