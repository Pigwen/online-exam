package controllers

import play.api.mvc.Action
import play.api.mvc.Controller
import models.store._
import play.api.data.Form
import play.api.data.Forms._
import views.html.defaultpages.badRequest
import models.Choice
import models.Subject

object Choices extends Controller {
  private val form = Form(mapping(
    "id" -> optional(longNumber),
    "title" -> nonEmptyText(1),
    "answer" -> nonEmptyText,
    "options" -> list(mapping(
      "id" -> optional(longNumber),
      "desc" -> text,
      "subjectID" -> optional(longNumber))(Choice)(Choice.unapply)))(Subject.apply)(Subject.unapply))

  def index = Action { implicit request =>
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
        SubjectTB.save(subject)
        Redirect(routes.Choices.index).flashing("success" -> "保存成功")
      })
  }

  def editForm(id: Long) = Action { implicit request =>
    val subject = SubjectTB.findOne(id)
    val f = form.fillAndValidate(subject)
    Ok(views.html.questions.choices.form(f))
  }

  def delete(id: Long) = Action { implicit request =>
    SubjectTB.delete(id)
    Redirect(routes.Choices.index).flashing("success" -> "删除成功")
  }
}