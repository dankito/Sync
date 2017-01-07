package net.dankito.sync.persistence;

import com.couchbase.lite.Context;

import java.io.File;

public class CouchbaseLiteEntityManagerJava extends CouchbaseLiteEntityManagerBase {

  public CouchbaseLiteEntityManagerJava(EntityManagerConfiguration configuration) throws Exception {
    super(new ConfigurableJavaContext(configuration.getDataFolder()), configuration);
  }


  @Override
  protected String adjustDatabasePath(Context context, EntityManagerConfiguration configuration) {
    // TODO: implement this in a better way as this uses implementation internal details
    return new File(context.getFilesDir(), configuration.getDataCollectionFileName() + ".cblite2").getAbsolutePath();
  }

}
