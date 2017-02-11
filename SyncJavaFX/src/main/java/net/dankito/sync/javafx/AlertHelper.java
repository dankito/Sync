package net.dankito.sync.javafx;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;


public class AlertHelper {


  public static boolean showConfirmationDialogOnUiThread(String message, String alertTitle) {
    return showConfirmationDialogOnUiThread(message, alertTitle, null);
  }

  public static boolean showConfirmationDialogOnUiThread(String message, String alertTitle, Stage owner) {
    Alert alert = createDialog(Alert.AlertType.CONFIRMATION, message, alertTitle, owner, ButtonType.NO, ButtonType.YES);

    Optional<ButtonType> result = alert.showAndWait();
    return result.get() == ButtonType.YES;
  }


  public static void showInfoMessage(final String infoMessage, final String alertTitle) {
    showInfoMessage(infoMessage, alertTitle, null);
  }

  public static void showInfoMessage(final String infoMessage, final String alertTitle, final Stage owner) {
    FXUtils.runOnUiThread(() -> showInfoMessageOnUiThread(infoMessage, alertTitle, owner));
  }

  protected static void showInfoMessageOnUiThread(String infoMessage, String alertTitle, Stage owner) {
    Alert alert = createDialog(Alert.AlertType.INFORMATION, infoMessage, alertTitle, owner, ButtonType.OK);

    alert.showAndWait();
  }


  public static void showErrorMessage(final Stage owner, final String errorMessage, final String alertTitle) {
    showErrorMessage(owner, errorMessage, alertTitle, null);
  }

  public static void showErrorMessage(final Stage owner, final String errorMessage, final String alertTitle, final Exception exception) {
    FXUtils.runOnUiThread(() -> showErrorMessageOnUiThread(owner, errorMessage, alertTitle, exception));
  }

  protected static void showErrorMessageOnUiThread(Stage owner, String errorMessage, String alertTitle, Exception exception) {
    Alert alert = createDialog(Alert.AlertType.ERROR, errorMessage, alertTitle, owner, ButtonType.OK);

    if(exception != null) {
      createExpandableException(alert, exception);
    }

    alert.showAndWait();
  }

  protected static void createExpandableException(Alert alert, Exception exception) {
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    exception.printStackTrace(pw);
    String exceptionText = sw.toString();

    Label label = new Label("The exception stacktrace was:");

    TextArea textArea = new TextArea(exceptionText);
    textArea.setEditable(false);
    textArea.setWrapText(true);

    textArea.setMaxWidth(FXUtils.SizeMaxValue);
    textArea.setMaxHeight(FXUtils.SizeMaxValue);
    GridPane.setVgrow(textArea, Priority.ALWAYS);
    GridPane.setHgrow(textArea, Priority.ALWAYS);

    GridPane expContent = new GridPane();
    expContent.setMaxWidth(FXUtils.SizeMaxValue);
    expContent.add(label, 0, 0);
    expContent.add(textArea, 0, 1);

// Set expandable Exception into the dialog pane.
    alert.getDialogPane().setExpandableContent(expContent);
  }


  protected static Alert createDialog(Alert.AlertType alertType, String message, String alertTitle, Stage owner, ButtonType... buttons) {
    Alert alert = new Alert(alertType);

    if(owner != null) {
      alert.initOwner(owner);
    }

    if(alertTitle != null) {
      alert.setTitle(alertTitle);
    }

    setAlertContent(alert, message);
    alert.setHeaderText(null);

    alert.getButtonTypes().setAll(buttons);

    return alert;
  }

  protected static void setAlertContent(Alert alert, String content) {
    double maxWidth = Screen.getPrimary().getVisualBounds().getWidth();
    if(alert.getOwner() != null) {
      Screen ownersScreen = FXUtils.getScreenWindowLeftUpperCornerIsIn(alert.getOwner());
      if(ownersScreen != null)
        maxWidth = ownersScreen.getVisualBounds().getWidth();
    }
    maxWidth *= 0.6; // set max width to 60 % of Screen width

    Label contentLabel = new Label(content);
    contentLabel.setWrapText(true);
    contentLabel.setPrefHeight(Region.USE_COMPUTED_SIZE);
    contentLabel.setMaxHeight(FXUtils.SizeMaxValue);
    contentLabel.setMaxWidth(maxWidth);

    VBox contentPane = new VBox(contentLabel);
    contentPane.setPrefHeight(Region.USE_COMPUTED_SIZE);
    contentPane.setMaxHeight(FXUtils.SizeMaxValue);
    VBox.setVgrow(contentLabel, Priority.ALWAYS);

    alert.getDialogPane().setPrefHeight(Region.USE_COMPUTED_SIZE);
    alert.getDialogPane().setMaxHeight(FXUtils.SizeMaxValue);
    alert.getDialogPane().setMaxWidth(maxWidth);
    alert.getDialogPane().setContent(contentPane);
  }


  protected static String askForTextInputOnUiThread(String questionText, String alertTitleText, String defaultValue) {
    TextInputDialog dialog = new TextInputDialog(defaultValue);
    dialog.setHeaderText(null);
    dialog.setTitle(alertTitleText);
    dialog.setContentText(questionText);

    Optional<String> result = dialog.showAndWait();
    return result.get();
  }
}
