package controllers

import play.api.mvc.Action
import play.api.mvc.Controller
import models.Db._
import play.api.data.Form
import play.api.data.Forms._
import views.html.defaultpages.badRequest

object Choices extends Controller {
  private val form = Form(mapping(
    "id" -> optional(longNumber),
    "title" -> nonEmptyText(1),
    "answer" -> nonEmptyText,
    "options" -> list(mapping(
      "id" -> optional(longNumber),
      "desc" -> text,
      "subjectID" -> optional(longNumber))(Choice)(Choice.unapply)))(Subject.apply)(Subject.unapply))

  def index = Action {
    Ok(views.html.questions.choices.index(SubjectTB.findAll))
  }

  def createForm = Action { implicit request =>
    Ok(views.html.questions.choices.form(form))
  }

  def create = Action { implicit request =>
    form.bindFromRequest.fold(
      formWithError => {
        Ok(views.html.questions.choices.form(formWithError))
      },
      subject => {
        SubjectTB.create(subject)
        Redirect(routes.Choices.index)
      })
  }

  def editForm(id: Long) = Action { implicit request =>
    val subject = SubjectTB.findOne(id)
    val f = form.fillAndValidate(subject)
    val answer = f("answer").value
    val oid = f("options[0].desc").value
    Ok(views.html.questions.choices.form(f))
  }
}