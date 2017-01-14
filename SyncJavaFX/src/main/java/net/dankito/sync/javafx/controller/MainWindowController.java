package net.dankito.sync.javafx.controller;


import net.dankito.sync.data.IDataManager;
import net.dankito.sync.devices.IDevicesManager;
import net.dankito.sync.persistence.IEntityManager;
import net.dankito.sync.synchronization.ISyncConfigurationManager;

import org.springframework.context.ApplicationContext;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.stage.Stage;

public class MainWindowController {


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


  public void init(ApplicationContext context, Stage stage) {
    this.stage = stage;

    entityManager = context.getBean(IEntityManager.class);
    dataManager = context.getBean(IDataManager.class);

    syncConfigurationManager = context.getBean(ISyncConfigurationManager.class);

    devicesManager = context.getBean(IDevicesManager.class);
    devicesManager.start();
  }


  @FXML
  public void handleMenuItemFileCloseAction(ActionEvent event) {
    stage.close();
  }


}
