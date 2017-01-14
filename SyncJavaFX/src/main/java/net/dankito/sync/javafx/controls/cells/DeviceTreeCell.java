package net.dankito.sync.javafx.controls.cells;

import net.dankito.sync.Device;
import net.dankito.sync.OsType;
import net.dankito.sync.devices.DiscoveredDevice;
import net.dankito.sync.javafx.FXUtils;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.TreeCell;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;


public class DeviceTreeCell extends TreeCell<DiscoveredDevice> {

  protected static final int TREE_CELL_HEIGHT = 80;


  protected HBox graphicPane;

  protected ImageView imgvwOsIcon;

  protected VBox pnDeviceInfo;

  protected Label lblDeviceName;

  protected Label lblDeviceAddress;


  public DeviceTreeCell() {
    setupGraphic();
  }


  protected void setupGraphic() {
    setText(null);
    setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
    setAlignment(Pos.CENTER_LEFT);
    setMinHeight(TREE_CELL_HEIGHT);
    setMaxHeight(TREE_CELL_HEIGHT);

    graphicPane = new HBox();
    graphicPane.setAlignment(Pos.CENTER_LEFT);
    graphicPane.setMinHeight(TREE_CELL_HEIGHT);
    graphicPane.setMaxHeight(TREE_CELL_HEIGHT);

    imgvwOsIcon = new ImageView();
    imgvwOsIcon.setFitWidth(TREE_CELL_HEIGHT);
    graphicPane.getChildren().add(imgvwOsIcon);

    pnDeviceInfo = new VBox();
    pnDeviceInfo.setAlignment(Pos.CENTER_LEFT);

    graphicPane.getChildren().add(pnDeviceInfo);
    HBox.setHgrow(graphicPane, Priority.ALWAYS);
    HBox.setMargin(graphicPane, new Insets(0, 6, 0, 6));

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
      setGraphic(graphicPane);

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
