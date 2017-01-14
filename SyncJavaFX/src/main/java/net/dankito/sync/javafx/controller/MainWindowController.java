package net.dankito.sync.javafx.controller;


import net.dankito.sync.data.IDataManager;
import net.dankito.sync.devices.IDevicesManager;
import net.dankito.sync.persistence.IEntityManager;
import net.dankito.sync.synchronization.ISyncConfigurationManager;

import org.springframework.context.ApplicationContext;

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

  protected IDataManager dataManager;

  protected IEntityManager entityManager;

  protected IDevicesManager devicesManager;

  protected ISyncConfigurationManager syncConfigurationManager;


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


  public void init(ApplicationContext context, Stage stage) {
    this.stage = stage;

    entityManager = context.getBean(IEntityManager.class);
    dataManager = context.getBean(IDataManager.class);

    syncConfigurationManager = context.getBean(ISyncConfigurationManager.class);

    devicesManager = context.getBean(IDevicesManager.class);
    devicesManager.start();
  }

}
