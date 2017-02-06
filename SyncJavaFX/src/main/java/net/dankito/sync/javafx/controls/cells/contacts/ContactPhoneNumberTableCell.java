package net.dankito.sync.javafx.controls.cells.contacts;

import net.dankito.sync.ContactSyncEntity;


public class ContactPhoneNumberTableCell extends ContactTableCell {

  protected String getTextRepresentationForCell(ContactSyncEntity contact) {
    if(contact.hasPhoneNumbers()) {
      return contact.getPhoneNumbers().get(0).getNumber();
    }

    return "";
  }

}
