package net.dankito.sync.javafx.controller;


import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.stage.Stage;

public class MainWindowController implements Initializable {


  protected Stage stage = null;

  @FXML
  protected Label statusLabel;
  @FXML
  protected Label statusLabelRight;

  @FXML
  protected SplitPane contentPane;


  @Override
  public void initialize(URL location, ResourceBundle resources) {

  }


  @FXML
  public void handleMenuItemFileCloseAction(ActionEvent event) {
    stage.close();
  }


  public void setStage(Stage stage) {
    this.stage = stage;
  }

}
