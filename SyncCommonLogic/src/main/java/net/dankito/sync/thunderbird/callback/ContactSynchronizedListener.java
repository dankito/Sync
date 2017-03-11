package net.dankito.sync.thunderbird.callback;

import net.dankito.sync.SyncEntityState;
import net.dankito.sync.thunderbird.model.ThunderbirdContact;


public interface ContactSynchronizedListener {

  void contactSynchronized(ThunderbirdContact contact, SyncEntityState state);

}
