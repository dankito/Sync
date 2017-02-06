package net.dankito.sync.broadcastreceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import net.dankito.sync.service.SyncBackgroundService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class BootCompletedBroadcastReceiver extends BroadcastReceiver {

  @Override
  public void onReceive(Context context, Intent intent) {
    Intent startServiceIntent= new Intent(context, SyncBackgroundService.class);
    context.startService(startServiceIntent);
  }

}
