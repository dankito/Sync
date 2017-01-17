package net.dankito.sync.synchronization.merge;


public interface IDataMerger {

  boolean mergeEntityData(Object updateSink, Object updateSource);

}
