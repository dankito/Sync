package net.dankito.sync.javafx.controls.cells.contacts;


import net.dankito.sync.ContactSyncEntity;
import net.dankito.utils.StringUtils;

import javafx.scene.control.TableCell;


public abstract class ContactTableCell extends TableCell<ContactSyncEntity, String> {

  public ContactTableCell() {
    tableRowProperty().addListener((observable, oldValue, newValue) -> {
      if(newValue != null) {
        newValue.itemProperty().addListener((observable1, oldValue1, newValue1) -> contactChanged((ContactSyncEntity)newValue1));

        contactChanged((ContactSyncEntity)newValue.getItem());
      }
    });
  }


  protected void contactChanged(ContactSyncEntity contact) {
    String textRepresentation = contact == null ? "" : getTextRepresentationForCell(contact);

    setText(textRepresentation);
    updateItem(textRepresentation, StringUtils.isNullOrEmpty(textRepresentation));
  }

  protected abstract String getTextRepresentationForCell(ContactSyncEntity contact);

}
