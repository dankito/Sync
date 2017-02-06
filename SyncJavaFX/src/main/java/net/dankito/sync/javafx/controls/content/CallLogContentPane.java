package net.dankito.sync.javafx.controls.content;


import net.dankito.sync.BaseEntity;
import net.dankito.sync.CallLogSyncEntity;
import net.dankito.sync.Device;
import net.dankito.sync.data.IDataManager;
import net.dankito.sync.devices.DiscoveredDevice;
import net.dankito.sync.javafx.FXUtils;
import net.dankito.sync.javafx.controls.Initializable;
import net.dankito.sync.javafx.controls.cells.call_log.CallLogDateTableCell;
import net.dankito.sync.javafx.controls.cells.call_log.CallLogDurationTableCell;
import net.dankito.sync.javafx.controls.cells.call_log.CallLogPhoneNumberTableCell;
import net.dankito.sync.javafx.localization.JavaFxLocalization;
import net.dankito.sync.persistence.IEntityManager;
import net.dankito.sync.synchronization.ISyncManager;
import net.dankito.sync.synchronization.SynchronizationListener;
import net.dankito.sync.synchronization.modules.SyncModuleSyncModuleConfigurationPair;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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

  @Inject
  protected IDataManager dataManager;


  protected DiscoveredDevice selectedRemoteDevice;


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
    clmnDate.setPrefWidth(180);
    localization.bindTableColumnText(clmnDate, "date");
    clmnDate.setCellFactory(param -> new CallLogDateTableCell());
    tbvwCallLog.getColumns().add(clmnDate);

    clmnDuration = new TableColumn<>();
    clmnDuration.setPrefWidth(100);
    localization.bindTableColumnText(clmnDuration, "duration");
    clmnDuration.setCellFactory(param -> new CallLogDurationTableCell());
    tbvwCallLog.getColumns().add(clmnDuration);

    clmnNumber = new TableColumn<>();
    clmnNumber.setPrefWidth(350);
    localization.bindTableColumnText(clmnNumber, "number");
    clmnNumber.setCellFactory(param -> new CallLogPhoneNumberTableCell());
    tbvwCallLog.getColumns().add(clmnNumber);
  }


  protected void setupLogic() {
    syncManager.addSynchronizationListener(synchronizationListener);

    updateCallLog();
  }

  public void showCallLogForDevice(DiscoveredDevice remoteDevice, SyncModuleSyncModuleConfigurationPair pair) {
    this.selectedRemoteDevice = remoteDevice;

    updateCallLog(remoteDevice);
  }

  protected void updateCallLog() {
    if(selectedRemoteDevice != null) {
      updateCallLog(selectedRemoteDevice);
    }
  }

  protected void updateCallLog(DiscoveredDevice selectedRemoveDevice) {
    List<CallLogSyncEntity> callLog = getSynchronizedCallLogsForRemoteDevice(selectedRemoveDevice);

    ObservableList<CallLogSyncEntity> items = FXCollections.observableArrayList(callLog);
    FXCollections.sort(items, callLogComparator);
    tbvwCallLog.setItems(items);
  }

  protected List<CallLogSyncEntity> getSynchronizedCallLogsForRemoteDevice(DiscoveredDevice remoteDevice) {
    List<CallLogSyncEntity> callLogsForDevice = new ArrayList<>();
    Device localDevice = dataManager.getLocalConfig().getLocalDevice();

    List<CallLogSyncEntity> allCallLogs = entityManager.getAllEntitiesOfType(CallLogSyncEntity.class);
    for(CallLogSyncEntity callLog : allCallLogs) {
      if(callLog.getSourceDevice() == remoteDevice.getDevice() || callLog.getSourceDevice() == localDevice) {
        callLogsForDevice.add(callLog);
      }
    }

    return callLogsForDevice;
  }


  Comparator<CallLogSyncEntity> callLogComparator = new Comparator<CallLogSyncEntity>() {
    @Override
    public int compare(CallLogSyncEntity o1, CallLogSyncEntity o2) {
      return o2.getDate().compareTo(o1.getDate());
    }
  };

  protected SynchronizationListener synchronizationListener = new SynchronizationListener() {
    @Override
    public void entitySynchronized(BaseEntity entity) {
      if(entity instanceof CallLogSyncEntity) {
        FXUtils.runOnUiThread(() -> updateCallLog() );
      }
    }
  };

}
