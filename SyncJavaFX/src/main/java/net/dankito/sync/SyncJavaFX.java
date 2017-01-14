package net.dankito.sync;

import net.dankito.sync.javafx.controller.MainWindowController;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ResourceBundle;

import javafx.application.Application;
import javafx.application.HostServices;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class SyncJavaFX extends Application {

  private final static Logger log = LoggerFactory.getLogger(SyncJavaFX.class);


  protected static HostServices hostServices = null;

  public static HostServices hostServices() {
    return hostServices;
  }


  /**
   * @param args the command line arguments
   */
  public static void main(String[] args) {
    launch(args);
  }

  @Override
  public void start(Stage stage) throws Exception {
    try {
      FXMLLoader loader = new FXMLLoader();
      loader.setResources(ResourceBundle.getBundle("Strings"));
      loader.setLocation(getClass().getClassLoader().getResource("dialogs/MainWindow.fxml"));
      Parent root = (Parent)loader.load();

      Scene scene = new Scene(root);

      stage.setScene(scene);

      MainWindowController controller = (MainWindowController)loader.getController();
      controller.setStage(stage);

      stage.show();

      hostServices = getHostServices();
    } catch(Exception e) {
      log.error("Could not start MainWindow", e);
    }
  }

  @Override
  public void stop() throws Exception {
    super.stop();
    System.exit(0);
  }

}

