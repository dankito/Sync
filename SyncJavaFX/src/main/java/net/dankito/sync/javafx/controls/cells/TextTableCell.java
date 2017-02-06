package net.dankito.sync.javafx.controls.cells;


import net.dankito.sync.SyncEntity;
import net.dankito.utils.StringUtils;

import javafx.scene.control.TableCell;


public abstract class TextTableCell<T extends SyncEntity> extends TableCell<T, String> {

  public TextTableCell() {
    tableRowProperty().addListener((observable, oldValue, newValue) -> {
      if(newValue != null) {
        newValue.itemProperty().addListener((observable1, oldValue1, newValue1) -> contactChanged((T)newValue1));

        contactChanged((T)newValue.getItem());
      }
    });
  }


  protected void contactChanged(T contact) {
    String textRepresentation = contact == null ? "" : getTextRepresentationForCell(contact);

    setText(textRepresentation);
    updateItem(textRepresentation, StringUtils.isNullOrEmpty(textRepresentation));
  }

  protected abstract String getTextRepresentationForCell(T contact);

}
