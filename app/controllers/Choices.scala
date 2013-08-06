package controllers

import play.api.mvc.Action
import play.api.mvc.Controller
import models.Db._

object Choices extends Controller {
  def index = Action {
    Ok(views.html.choices.index(ChoicesTb.findAll))
  }
  
  def createForm = Action {
    Ok(views.html.choices.form())
  }
}