package net.dankito.sync.javafx.controls.cells.call_log;


import net.dankito.sync.CallLogSyncEntity;

public class CallLogDurationTableCell extends CallLogTableCell {

  @Override
  protected String getTextRepresentationForCell(CallLogSyncEntity contact) {
    int durationInSeconds = contact.getDurationInSeconds();

    int hours = durationInSeconds / 3600;
    int minutes = durationInSeconds / 60;
    int seconds = durationInSeconds % 60;

    return String.format("%02d:%02d:%02d", hours, minutes, seconds);
  }

}
