package net.dankito.sync;

import net.dankito.sync.config.DatabaseTableConfig;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;


@Entity(name = DatabaseTableConfig.FILE_SYNC_ENTITY_TABLE_NAME)
@DiscriminatorValue(value = DatabaseTableConfig.FILE_SYNC_ENTITY_DISCRIMINATOR_VALUE)
public class FileSyncEntity extends SyncEntity {

  @Column(name = DatabaseTableConfig.FILE_SYNC_ENTITY_NAME_COLUMN_NAME)
  protected String name;

  @Column(name = DatabaseTableConfig.FILE_SYNC_ENTITY_DESCRIPTION_COLUMN_NAME)
  protected String description;

  @Column(name = DatabaseTableConfig.FILE_SYNC_ENTITY_FILE_PATH_COLUMN_NAME)
  protected String filePath;

  @Column(name = DatabaseTableConfig.FILE_SYNC_ENTITY_FILE_SIZE_COLUMN_NAME)
  protected long fileSize;

  @Column(name = DatabaseTableConfig.FILE_SYNC_ENTITY_MIME_TYPE_COLUMN_NAME)
  protected String mimeType;


  public FileSyncEntity() {
    super();
  }


  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getFilePath() {
    return filePath;
  }

  public void setFilePath(String filePath) {
    this.filePath = filePath;
  }

  public long getFileSize() {
    return fileSize;
  }

  public void setFileSize(long fileSize) {
    this.fileSize = fileSize;
  }

  public String getMimeType() {
    return mimeType;
  }

  public void setMimeType(String mimeType) {
    this.mimeType = mimeType;
  }


  @Override
  public String toString() {
    return getName();
  }

}
