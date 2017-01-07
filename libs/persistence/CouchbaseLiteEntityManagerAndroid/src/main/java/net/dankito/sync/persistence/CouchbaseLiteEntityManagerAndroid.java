package net.dankito.sync.persistence;

import com.couchbase.lite.Context;
import com.couchbase.lite.android.AndroidContext;


public class CouchbaseLiteEntityManagerAndroid extends CouchbaseLiteEntityManagerBase {

  protected android.content.Context androidContext;


  public CouchbaseLiteEntityManagerAndroid(android.content.Context context, EntityManagerConfiguration configuration) throws Exception {
    super(new AndroidContext(context), configuration);
    this.androidContext = context;

  }


  @Override
  protected String adjustDatabasePath(Context context, EntityManagerConfiguration configuration) {
    return configuration.getDataCollectionPersistencePath();
  }

}
