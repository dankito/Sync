package net.dankito.sync.javafx.controller;


import net.dankito.sync.data.IDataManager;
import net.dankito.sync.devices.DiscoveredDevice;
import net.dankito.sync.devices.DiscoveredDeviceType;
import net.dankito.sync.devices.DiscoveredDevicesListener;
import net.dankito.sync.devices.IDevicesManager;
import net.dankito.sync.javafx.FXUtils;
import net.dankito.sync.javafx.controls.cells.DeviceOrSyncModuleConfigurationTreeCell;
import net.dankito.sync.javafx.controls.content.CallLogContentPane;
import net.dankito.sync.javafx.controls.content.ContactsContentPane;
import net.dankito.sync.javafx.controls.treeitems.DeviceRootTreeItem;
import net.dankito.sync.javafx.controls.treeitems.DeviceTreeItem;
import net.dankito.sync.javafx.controls.treeitems.SynchronizedDeviceSyncModuleConfigurationTreeItem;
import net.dankito.sync.javafx.controls.treeitems.SynchronizedDeviceTreeItem;
import net.dankito.sync.persistence.IEntityManager;
import net.dankito.sync.synchronization.ISyncConfigurationManager;
import net.dankito.sync.synchronization.modules.CallLogJavaEndpointSyncModule;
import net.dankito.sync.synchronization.modules.ContactsJavaEndpointSyncModule;
import net.dankito.sync.synchronization.modules.ISyncModuleConfigurationManager;
import net.dankito.sync.synchronization.modules.SyncModuleSyncModuleConfigurationPair;

import org.springframework.context.ApplicationContext;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import javafx.util.Callback;

public class MainWindowController {


  protected Stage stage = null;

  protected ApplicationContext context;

  protected IDataManager dataManager;

  protected IEntityManager entityManager;

  protected IDevicesManager devicesManager;

  protected ISyncConfigurationManager syncConfigurationManager;

  protected ISyncModuleConfigurationManager syncModuleConfigurationManager;


  protected Map<DiscoveredDevice, DeviceTreeItem> mapUnknownDeviceDeviceTreeItem = new HashMap<>();

  protected Map<DiscoveredDevice, DeviceTreeItem> mapKnownSynchronizedDeviceDeviceTreeItem = new HashMap<>();


  @FXML
  protected Label statusLabel;
  @FXML
  protected Label statusLabelRight;

  @FXML
  protected SplitPane contentPane;

  @FXML
  protected TreeView trvwUnknownDiscoveredDevices;

  @FXML
  protected TreeView trvwKnownSynchronizedDevices;

  @FXML
  protected ScrollPane scrpnDeviceDetails;


  protected Node unselectedNodeContentPane;

  @Inject
  protected ContactsContentPane contactsContentPane;

  @Inject
  protected CallLogContentPane callLogContentPane;


  public void init(ApplicationContext context, Stage stage) {
    this.stage = stage;
    this.context = context;

    initDependencies(context);

    setupUi();

    setupLogic();
  }


  protected void initDependencies(ApplicationContext context) {
    entityManager = context.getBean(IEntityManager.class);
    dataManager = context.getBean(IDataManager.class);

    syncConfigurationManager = context.getBean(ISyncConfigurationManager.class);

    syncModuleConfigurationManager = context.getBean(ISyncModuleConfigurationManager.class);

    devicesManager = context.getBean(IDevicesManager.class);
  }

  protected void setupLogic() {
    devicesManager.addDiscoveredDevicesListener(discoveredDevicesListener);
    devicesManager.start();
  }


  protected void setupUi() {
    setupDeviceTreeViews();

    setupContentPane();
  }

  protected void setupDeviceTreeViews() {
    trvwUnknownDiscoveredDevices.setCellFactory(new Callback<TreeView, TreeCell>() {
      @Override
      public TreeCell call(TreeView param) {
        return new DeviceOrSyncModuleConfigurationTreeCell();
      }
    });

    trvwUnknownDiscoveredDevices.setShowRoot(false);
    trvwUnknownDiscoveredDevices.setRoot(new DeviceRootTreeItem());
    trvwUnknownDiscoveredDevices.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
      selectedUnknownDiscoveredDevicesTreeNodeChanged(((TreeItem)newValue).getValue());
    });


    trvwKnownSynchronizedDevices.setCellFactory(new Callback<TreeView, TreeCell>() {
      @Override
      public TreeCell call(TreeView param) {
        return new DeviceOrSyncModuleConfigurationTreeCell();
      }
    });

    trvwKnownSynchronizedDevices.setShowRoot(false);
    trvwKnownSynchronizedDevices.setRoot(new DeviceRootTreeItem());
    trvwKnownSynchronizedDevices.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
      selectedKnownSynchronizedDevicesTreeNodeChanged((TreeItem)newValue);
    });
  }

  protected void selectedUnknownDiscoveredDevicesTreeNodeChanged(Object selectedValue) {
    showUnselectedNodeContentPane();
  }

  protected void selectedKnownSynchronizedDevicesTreeNodeChanged(TreeItem treeItem) {
    if(treeItem instanceof SynchronizedDeviceSyncModuleConfigurationTreeItem) {
      SynchronizedDeviceSyncModuleConfigurationTreeItem configTreeItem = (SynchronizedDeviceSyncModuleConfigurationTreeItem)treeItem;
      showContentPaneForSyncModule(configTreeItem.getDevice(), configTreeItem.getValue());
    }
    else {
      showUnselectedNodeContentPane();
    }
  }


  protected void setupContentPane() {
    unselectedNodeContentPane = new Region();

    contactsContentPane = context.getBean(ContactsContentPane.class);
    contactsContentPane.init();

    callLogContentPane = context.getBean(CallLogContentPane.class);
    callLogContentPane.init();
  }


  protected void showContentPaneForSyncModule(DiscoveredDevice remoteDevice, SyncModuleSyncModuleConfigurationPair pair) {
    if(pair.getSyncModule() instanceof ContactsJavaEndpointSyncModule) {
      showContactsContentPane(remoteDevice, pair);
    }
    else if(pair.getSyncModule() instanceof CallLogJavaEndpointSyncModule) {
      showCallLogContentPane(remoteDevice, pair);
    }
    else {
      showUnselectedNodeContentPane();
    }
  }

  protected void showContactsContentPane(DiscoveredDevice remoteDevice, SyncModuleSyncModuleConfigurationPair pair) {
    contactsContentPane.showContactsForDevice(remoteDevice, pair);
    setContent(contactsContentPane);
  }

  protected void showCallLogContentPane(DiscoveredDevice remoteDevice, SyncModuleSyncModuleConfigurationPair pair) {
    callLogContentPane.showCallLogForDevice(remoteDevice, pair);
    setContent(callLogContentPane);
  }

  protected void showUnselectedNodeContentPane() {
    setContent(unselectedNodeContentPane);
  }

  protected void setContent(Node contentPane) {
    scrpnDeviceDetails.setContent(contentPane);
  }


  protected void connectedToUnknownDevice(DiscoveredDevice connectedDevice) {
    DeviceTreeItem treeItem = new DeviceTreeItem(connectedDevice);
    trvwUnknownDiscoveredDevices.getRoot().getChildren().add(treeItem);

    mapUnknownDeviceDeviceTreeItem.put(connectedDevice, treeItem);
  }

  protected void connectedToSynchronizedDevice(DiscoveredDevice connectedDevice) {
    DeviceTreeItem treeItem = new SynchronizedDeviceTreeItem(connectedDevice, syncModuleConfigurationManager);
    trvwKnownSynchronizedDevices.getRoot().getChildren().add(treeItem);

    mapKnownSynchronizedDeviceDeviceTreeItem.put(connectedDevice, treeItem);
  }

  protected void disconnectedFromDevice(DiscoveredDevice device) {
    if(mapUnknownDeviceDeviceTreeItem.containsKey(device)) {
      DeviceTreeItem deviceTreeItem = mapUnknownDeviceDeviceTreeItem.remove(device);
      trvwUnknownDiscoveredDevices.getRoot().getChildren().remove(deviceTreeItem);
    }
    else if(mapKnownSynchronizedDeviceDeviceTreeItem.containsKey(device)) {
      DeviceTreeItem deviceTreeItem = mapKnownSynchronizedDeviceDeviceTreeItem.remove(device);
      trvwKnownSynchronizedDevices.getRoot().getChildren().remove(deviceTreeItem);
    }
  }


  @FXML
  public void handleMenuItemFileCloseAction(ActionEvent event) {
    stage.close();
  }


  protected DiscoveredDevicesListener discoveredDevicesListener = new DiscoveredDevicesListener() {
    @Override
    public void deviceDiscovered(DiscoveredDevice connectedDevice, DiscoveredDeviceType type) {
      FXUtils.runOnUiThread(() -> {
        if(type == DiscoveredDeviceType.UNKNOWN_DEVICE) {
          connectedToUnknownDevice(connectedDevice);
        }
        else if(type == DiscoveredDeviceType.KNOWN_SYNCHRONIZED_DEVICE) {
          connectedToSynchronizedDevice(connectedDevice);
        }
      });
    }

    @Override
    public void disconnectedFromDevice(DiscoveredDevice disconnectedDevice) {
      FXUtils.runOnUiThread(() -> MainWindowController.this.disconnectedFromDevice(disconnectedDevice));
    }
  };


}
