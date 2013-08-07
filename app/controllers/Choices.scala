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
    "answer" -> nonEmptyText)(Subject.apply)(Subject.unapply))

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
        SubjectTB.insert(subject)
        Redirect(routes.Choices.index)
      })
  }
}