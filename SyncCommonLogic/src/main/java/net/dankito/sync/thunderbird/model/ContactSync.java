package net.dankito.sync.thunderbird.model;

import net.dankito.sync.SyncEntityState;


public class ContactSync {

  protected SyncEntityState state;

  protected ThunderbirdContact contact;


  public ContactSync() {

  }

  public ContactSync(ThunderbirdContact contact, SyncEntityState state) {
    this.contact = contact;
    this.state = state;
  }


  public SyncEntityState getState() {
    return state;
  }

  public void setState(SyncEntityState state) {
    this.state = state;
  }

  public ThunderbirdContact getContact() {
    return contact;
  }

  public void setContact(ThunderbirdContact contact) {
    this.contact = contact;
  }


  @Override
  public String toString() {
    return getState() + ": " + getContact();
  }

}
