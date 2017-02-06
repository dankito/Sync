package net.dankito.sync.javafx.controls.cells.call_log;


import net.dankito.sync.CallLogSyncEntity;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;


public class CallLogDateTableCell extends CallLogTableCell {

  protected DateFormat dateFormat;


  public CallLogDateTableCell() {
    dateFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);

    if("de".equals(Locale.getDefault().getLanguage())) {
      dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");
    }
  }

  @Override
  protected String getTextRepresentationForCell(CallLogSyncEntity contact) {
    return dateFormat.format(contact.getDate());
  }

}
