package net.dankito.sync.javafx.controls.cells;

import net.dankito.sync.Device;
import net.dankito.sync.OsType;
import net.dankito.sync.devices.DiscoveredDevice;
import net.dankito.sync.javafx.FXUtils;
import net.dankito.sync.javafx.services.IconManager;
import net.dankito.sync.synchronization.modules.SyncModuleSyncModuleConfigurationPair;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.TreeCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;


public class DeviceOrSyncModuleConfigurationTreeCell extends TreeCell<Object> {

  protected static final int DEVICE_TREE_CELL_HEIGHT = 80;

  protected static final int SYNC_MODULE_CONFIGURATION_TREE_CELL_HEIGHT = 45;


  protected HBox deviceGraphicPane;

  protected ImageView imgvwOsIcon;

  protected VBox pnDeviceInfo;

  protected Label lblDeviceName;

  protected Label lblDeviceAddress;


  protected HBox syncModuleConfigurationGraphicPane;

  protected Label lblSyncModuleName;


  public DeviceOrSyncModuleConfigurationTreeCell() {
    setupGraphic();
  }


  protected void setupGraphic() {
    setText(null);
    setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
    setAlignment(Pos.CENTER_LEFT);

    setDisclosureNode(null);

    setupDeviceGraphicPane();

    setupSyncModuleConfigurationGraphicPane();
  }

  protected void setupDeviceGraphicPane() {
    deviceGraphicPane = new HBox();
    deviceGraphicPane.setAlignment(Pos.CENTER_LEFT);
    deviceGraphicPane.setMinHeight(DEVICE_TREE_CELL_HEIGHT);
    deviceGraphicPane.setMaxHeight(DEVICE_TREE_CELL_HEIGHT);

    imgvwOsIcon = new ImageView();
    imgvwOsIcon.setFitWidth(DEVICE_TREE_CELL_HEIGHT);
    imgvwOsIcon.setFitHeight(DEVICE_TREE_CELL_HEIGHT);

    deviceGraphicPane.getChildren().add(imgvwOsIcon);
    HBox.setMargin(imgvwOsIcon, new Insets(6, 6, 6, 0));

    pnDeviceInfo = new VBox();
    pnDeviceInfo.setAlignment(Pos.CENTER_LEFT);

    deviceGraphicPane.getChildren().add(pnDeviceInfo);
    HBox.setHgrow(deviceGraphicPane, Priority.ALWAYS);
    HBox.setMargin(deviceGraphicPane, new Insets(0, 6, 0, 6));

    lblDeviceName = new Label();
    lblDeviceName.setFont(new Font(18));
    lblDeviceName.setTextOverrun(OverrunStyle.ELLIPSIS);
    lblDeviceName.setMaxWidth(FXUtils.SizeMaxValue);
    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(lblDeviceName);

    pnDeviceInfo.getChildren().add(lblDeviceName);
    VBox.setVgrow(lblDeviceName, Priority.ALWAYS);
    VBox.setMargin(lblDeviceName, new Insets(6, 0, 6, 0));

    lblDeviceAddress = new Label();
    lblDeviceAddress.setFont(new Font(16));
    lblDeviceAddress.setTextOverrun(OverrunStyle.ELLIPSIS);
    lblDeviceAddress.setMaxWidth(FXUtils.SizeMaxValue);
    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(lblDeviceAddress);

    pnDeviceInfo.getChildren().add(lblDeviceAddress);
    VBox.setVgrow(lblDeviceAddress, Priority.ALWAYS);
  }

  protected void setupSyncModuleConfigurationGraphicPane() {
    syncModuleConfigurationGraphicPane = new HBox();
    syncModuleConfigurationGraphicPane.setAlignment(Pos.CENTER_LEFT);
    syncModuleConfigurationGraphicPane.setMinHeight(SYNC_MODULE_CONFIGURATION_TREE_CELL_HEIGHT);
    syncModuleConfigurationGraphicPane.setMaxHeight(SYNC_MODULE_CONFIGURATION_TREE_CELL_HEIGHT);

    lblSyncModuleName = new Label();
    lblSyncModuleName.setFont(new Font(18));
    lblSyncModuleName.setTextOverrun(OverrunStyle.ELLIPSIS);
    lblSyncModuleName.setMaxWidth(FXUtils.SizeMaxValue);
    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(lblSyncModuleName);

    syncModuleConfigurationGraphicPane.getChildren().add(lblSyncModuleName);
    VBox.setVgrow(lblSyncModuleName, Priority.ALWAYS);
    VBox.setMargin(lblSyncModuleName, new Insets(6, 0, 6, 0));
  }

  @Override
  protected void updateItem(Object item, boolean empty) {
    super.updateItem(item, empty);

    if(empty) {
      setGraphic(null);
    }
    else {
      if(item instanceof DiscoveredDevice) {
        showDeviceGraphic((DiscoveredDevice) item);
      }
      else if(item instanceof SyncModuleSyncModuleConfigurationPair) {
        showSyncModuleConfigurationGraphic((SyncModuleSyncModuleConfigurationPair)item);
      }
    }
  }

  protected void showSyncModuleConfigurationGraphic(SyncModuleSyncModuleConfigurationPair pair) {
    setGraphic(syncModuleConfigurationGraphicPane);

    setMinHeight(SYNC_MODULE_CONFIGURATION_TREE_CELL_HEIGHT);
    setMaxHeight(SYNC_MODULE_CONFIGURATION_TREE_CELL_HEIGHT);

    lblSyncModuleName.setText(pair.getSyncModule().getName());
  }

  protected void showDeviceGraphic(DiscoveredDevice device) {
    setGraphic(deviceGraphicPane);

    setMinHeight(DEVICE_TREE_CELL_HEIGHT);
    setMaxHeight(DEVICE_TREE_CELL_HEIGHT);

    imgvwOsIcon.setImage(new Image(IconManager.getInstance().getLogoForOperatingSystem(device.getDevice().getOsName(), device.getDevice().getOsVersion())));

    lblDeviceName.setText(getDeviceDescription(device.getDevice()));
    lblDeviceAddress.setText(device.getAddress());
  }

  protected String getDeviceDescription(Device device) {
    if(device.getOsType() == OsType.ANDROID) {
      return device.getName();
    }
    else {
      return device.getOsName() + " " + device.getOsVersion();
    }
  }

}
