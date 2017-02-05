package net.dankito.sync;

import net.dankito.sync.javafx.controller.MainWindowController;
import net.dankito.sync.javafx.localization.JavaFxLocalization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import javafx.application.Application;
import javafx.application.HostServices;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


@SpringBootApplication(scanBasePackages = { "net.dankito.sync", "net.dankito.utils", "net.dankito.devicediscovery" })
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
      ApplicationContext context = SpringApplication.run(SyncJavaFX.class);
      JavaFxLocalization localization = context.getBean(JavaFxLocalization.class);

      FXMLLoader loader = new FXMLLoader();
      loader.setResources(localization.getStringsResourceBundle());
      loader.setLocation(getClass().getClassLoader().getResource("dialogs/MainWindow.fxml"));
      Parent root = (Parent)loader.load();
      localization.resolveResourceKeys(root);

      Scene scene = new Scene(root);
//    String mainDocumentCss = getClass().getResource("/MainDocument.css").toExternalForm();
//    scene.getStylesheets().add(mainDocumentCss);

      stage.setScene(scene);
      localization.bindStageTitle(stage, "main.window.title");

      MainWindowController controller = (MainWindowController)loader.getController();
      controller.init(context, stage);

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

