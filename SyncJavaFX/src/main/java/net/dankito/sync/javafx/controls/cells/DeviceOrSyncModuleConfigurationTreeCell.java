package net.dankito.sync.javafx.controls.cells;

import net.dankito.sync.Device;
import net.dankito.sync.OsType;
import net.dankito.sync.devices.DiscoveredDevice;
import net.dankito.sync.javafx.FXUtils;
import net.dankito.sync.javafx.services.IconManager;

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


public class DeviceOrSyncModuleConfigurationTreeCell extends TreeCell<DiscoveredDevice> {

  protected static final int TREE_CELL_HEIGHT = 80;


  protected HBox deviceGraphicPane;

  protected ImageView imgvwOsIcon;

  protected VBox pnDeviceInfo;

  protected Label lblDeviceName;

  protected Label lblDeviceAddress;


  public DeviceOrSyncModuleConfigurationTreeCell() {
    setupGraphic();
  }


  protected void setupGraphic() {
    setText(null);
    setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
    setAlignment(Pos.CENTER_LEFT);
    setMinHeight(TREE_CELL_HEIGHT);
    setMaxHeight(TREE_CELL_HEIGHT);

    deviceGraphicPane = new HBox();
    deviceGraphicPane.setAlignment(Pos.CENTER_LEFT);
    deviceGraphicPane.setMinHeight(TREE_CELL_HEIGHT);
    deviceGraphicPane.setMaxHeight(TREE_CELL_HEIGHT);

    imgvwOsIcon = new ImageView();
    imgvwOsIcon.setFitWidth(TREE_CELL_HEIGHT);
    imgvwOsIcon.setFitHeight(TREE_CELL_HEIGHT );

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

  @Override
  protected void updateItem(DiscoveredDevice item, boolean empty) {
    super.updateItem(item, empty);

    if(empty) {
      setGraphic(null);
    }
    else {
      setGraphic(deviceGraphicPane);

      imgvwOsIcon.setImage(new Image(IconManager.getInstance().getLogoForOperatingSystem(item.getDevice().getOsName(), item.getDevice().getOsVersion())));

      lblDeviceName.setText(getDeviceDescription(item.getDevice()));
      lblDeviceAddress.setText(item.getAddress());
    }
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
