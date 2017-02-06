package net.dankito.sync.javafx.controls.content;


import net.dankito.sync.ContactSyncEntity;
import net.dankito.sync.javafx.controls.Initializable;
import net.dankito.sync.javafx.controls.cells.contacts.ContactEmailAddressTableCell;
import net.dankito.sync.javafx.controls.cells.contacts.ContactPhoneNumberTableCell;
import net.dankito.sync.javafx.localization.JavaFxLocalization;
import net.dankito.sync.persistence.IEntityManager;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import javafx.collections.FXCollections;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

@Named
public class ContactsContentPane extends VBox implements Initializable {

  @Inject
  protected JavaFxLocalization localization;

  @Inject
  protected IEntityManager entityManager;


  protected TableView<ContactSyncEntity> tbvwContacts;

  protected TableColumn<ContactSyncEntity, String> clmnDisplayName;

  protected TableColumn<ContactSyncEntity, String> clmnPhoneNumber;

  protected TableColumn<ContactSyncEntity, String> clmnEmailAddress;


  public ContactsContentPane() {

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
    tbvwContacts = new TableView<>();

    this.getChildren().add(tbvwContacts);
    VBox.setVgrow(tbvwContacts, Priority.ALWAYS);

    clmnDisplayName = new TableColumn<>();
    clmnDisplayName.setPrefWidth(300);
    localization.bindTableColumnText(clmnDisplayName, "display.name");
    clmnDisplayName.setCellValueFactory(
        new PropertyValueFactory<ContactSyncEntity, String>("displayName")
    );
    tbvwContacts.getColumns().add(clmnDisplayName);

    clmnPhoneNumber = new TableColumn<>();
    clmnPhoneNumber.setPrefWidth(200);
    localization.bindTableColumnText(clmnPhoneNumber, "phone.number");
    clmnPhoneNumber.setCellFactory(param -> new ContactPhoneNumberTableCell());
    tbvwContacts.getColumns().add(clmnPhoneNumber);

    clmnEmailAddress = new TableColumn<>();
    clmnEmailAddress.setPrefWidth(200);
    localization.bindTableColumnText(clmnEmailAddress, "email.address");
    clmnEmailAddress.setCellFactory(param -> new ContactEmailAddressTableCell());
    tbvwContacts.getColumns().add(clmnEmailAddress);
  }


  protected void setupLogic() {
    updateContacts();
  }

  protected void updateContacts() {
    List<ContactSyncEntity> contacts = entityManager.getAllEntitiesOfType(ContactSyncEntity.class);

    tbvwContacts.setItems(FXCollections.observableArrayList(contacts));
  }

}
