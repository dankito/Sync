package net.dankito.sync.javafx.controls.content;


import net.dankito.sync.BaseEntity;
import net.dankito.sync.CallLogSyncEntity;
import net.dankito.sync.javafx.FXUtils;
import net.dankito.sync.javafx.controls.Initializable;
import net.dankito.sync.javafx.controls.cells.call_log.CallLogDateTableCell;
import net.dankito.sync.javafx.controls.cells.call_log.CallLogDurationTableCell;
import net.dankito.sync.javafx.controls.cells.call_log.CallLogPhoneNumberTableCell;
import net.dankito.sync.javafx.localization.JavaFxLocalization;
import net.dankito.sync.persistence.IEntityManager;
import net.dankito.sync.synchronization.ISyncManager;
import net.dankito.sync.synchronization.SynchronizationListener;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import javafx.collections.FXCollections;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

@Named
public class CallLogContentPane extends VBox implements Initializable {

  @Inject
  protected JavaFxLocalization localization;

  @Inject
  protected ISyncManager syncManager;

  @Inject
  protected IEntityManager entityManager;


  protected TableView<CallLogSyncEntity> tbvwCallLog;

  protected TableColumn<CallLogSyncEntity, String> clmnDate;

  protected TableColumn<CallLogSyncEntity, String> clmnDuration;

  protected TableColumn<CallLogSyncEntity, String> clmnNumber;


  public CallLogContentPane() {

  }


  @Override
  public void init() {
    setupControls();

    setupLogic();
  }


  protected void setupControls() {
    setupTableView();
  }

  protected void setupTableView() {
    tbvwCallLog = new TableView<>();

    this.getChildren().add(tbvwCallLog);
    VBox.setVgrow(tbvwCallLog, Priority.ALWAYS);

    clmnDate = new TableColumn<>();
    clmnDate.setPrefWidth(150);
    localization.bindTableColumnText(clmnDate, "date");
    clmnDate.setCellFactory(param -> new CallLogDateTableCell());
    tbvwCallLog.getColumns().add(clmnDate);

    clmnDuration = new TableColumn<>();
    clmnDuration.setPrefWidth(100);
    localization.bindTableColumnText(clmnDuration, "duration");
    clmnDuration.setCellFactory(param -> new CallLogDurationTableCell());
    tbvwCallLog.getColumns().add(clmnDuration);

    clmnNumber = new TableColumn<>();
    clmnNumber.setPrefWidth(200);
    localization.bindTableColumnText(clmnNumber, "number");
    clmnNumber.setCellFactory(param -> new CallLogPhoneNumberTableCell());
    tbvwCallLog.getColumns().add(clmnNumber);
  }


  protected void setupLogic() {
    syncManager.addSynchronizationListener(synchronizationListener);

    updateCallLog();
  }

  protected void updateCallLog() {
    List<CallLogSyncEntity> callLog = entityManager.getAllEntitiesOfType(CallLogSyncEntity.class);

    tbvwCallLog.setItems(FXCollections.observableArrayList(callLog));
  }


  protected SynchronizationListener synchronizationListener = new SynchronizationListener() {
    @Override
    public void entitySynchronized(BaseEntity entity) {
      if(entity instanceof CallLogSyncEntity) {
        FXUtils.runOnUiThread(() -> updateCallLog() );
      }
    }
  };

}
