package net.dankito.sync.javafx.controls.cells.call_log;


import net.dankito.sync.CallLogSyncEntity;
import net.dankito.utils.StringUtils;

public class CallLogPhoneNumberTableCell extends CallLogTableCell {

  @Override
  protected String getTextRepresentationForCell(CallLogSyncEntity contact) {
    if(StringUtils.isNotNullOrEmpty(contact.getAssociatedContactName())) {
      return contact.getAssociatedContactName() + " (" + contact.getNumber() + ")";
    }
    else {
      return contact.getNumber();
    }
  }

}
