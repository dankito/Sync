package net.dankito.sync.javafx.controls.cells.contacts;

import net.dankito.sync.ContactSyncEntity;


public class ContactEmailAddressTableCell extends ContactTableCell {

  protected String getTextRepresentationForCell(ContactSyncEntity contact) {
    if(contact.hasEmailAddresses()) {
      return contact.getEmailAddresses().get(0).getAddress();
    }

    return "";
  }

}
