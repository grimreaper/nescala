package ui

import java.awt.Canvas

import nescala.BuildInfo
import org.lwjgl.opengl.Display
import org.macrogl.Macrogl

import scala.swing.Dialog
import scala.util.{Failure, Success, Try}

case class Director(gameWindow : Canvas, menuWindow: WrapPanel, audio: Audio) {

  implicit val gl = org.macrogl.Macrogl.default
  var timestamp = 0L
  var view: Option[View] = None
  var pause = false

  def Step() {
    gl.clear(Macrogl.COLOR_BUFFER_BIT)
    // Clear the screen and depth buffer
    val dt = System.nanoTime() - timestamp
    timestamp = System.nanoTime()
    val seconds = dt / 1000000000.0F
    view.foreach(_.Update(seconds))
  }

  def Menu = setView(Some(new MenuView(menuWindow)))

  def Reset = view.foreach(x => x.Reset())

  def Close = setView(None)

  def Pause = pause = true

  def Resume = pause = false

  def Start(path: String) = loadGameView(path) match {
            case Success(_) => run()
            case Failure(e) => Dialog.showMessage(new {def peer = gameWindow.getParent}, e.getMessage, BuildInfo.name, Dialog.Message.Warning)
  }

  private def loadGameView(path: String) = Try(nescala.Console(path, audio)).map(console => setView(Some(GameView(console, audio, gameWindow))))

  private def setView(view : Option[View]) {
    this.view.foreach(_.Close())
    this.view = view
    this.view.foreach(_.Open())
    timestamp = System.nanoTime()
  }

  private def run() = {
    while (view.isDefined) {
      if (!pause) Step()
      Display.sync(60)
      Display.update(true)
    }
    Display.destroy()
  }
}
