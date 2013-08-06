package controllers

import play.api.mvc.Action
import play.api.mvc.Controller
import models.Db._
import play.api.data.Form
import play.api.data.Forms._
import views.html.defaultpages.badRequest

object Choices extends Controller {
  private val form = Form(mapping(
    "id" -> longNumber,
    "title" -> nonEmptyText(1),
    "answer" -> nonEmptyText)(Choice.apply)(Choice.unapply))

  def index = Action {
    Ok(views.html.choices.index(ChoicesTb.findAll))
  }

  def createForm = Action { implicit request =>
    Ok(views.html.choices.form())
  }

  def create = Action { implicit request =>
    form.bindFromRequest.fold(
      formWithError => BadRequest,
      choice => Redirect(routes.Choices.index))
  }
}