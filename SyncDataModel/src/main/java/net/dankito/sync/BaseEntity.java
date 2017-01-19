package net.dankito.sync;

import net.dankito.sync.config.DatabaseTableConfig;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.PostRemove;
import javax.persistence.PrePersist;
import javax.persistence.PreRemove;
import javax.persistence.PreUpdate;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;

@MappedSuperclass
public abstract class BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = DatabaseTableConfig.BASE_ENTITY_ID_COLUMN_NAME)
  protected String id;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = DatabaseTableConfig.BASE_ENTITY_CREATED_ON_COLUMN_NAME, updatable = false)
  protected Date createdOn;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = DatabaseTableConfig.BASE_ENTITY_MODIFIED_ON_COLUMN_NAME)
  protected Date modifiedOn;

  @Version
  @Column(name = DatabaseTableConfig.BASE_ENTITY_VERSION_COLUMN_NAME, nullable = false, columnDefinition = "BIGINT DEFAULT 1")
  protected Long version;

  @Column(name = DatabaseTableConfig.BASE_ENTITY_DELETED_COLUMN_NAME, columnDefinition = "SMALLINT DEFAULT 0", nullable = false)
  protected boolean deleted;


  public String getId() {
    return id;
  }

  public Date getCreatedOn() {
    return createdOn;
  }

  public void setCreatedOn(Date createdOn) {
    this.createdOn = createdOn;
  }

  public Date getModifiedOn() {
    return modifiedOn;
  }

  public void setModifiedOn(Date modifiedOn) {
    this.modifiedOn = modifiedOn;
  }

  public Long getVersion() {
    return version;
  }

  public boolean isDeleted() {
    return deleted;
  }

  public void setDeleted(boolean deleted) {
    this.deleted = deleted;
  }


  public boolean isPersisted() {
    return getId() != null;
  }


  @PrePersist
  protected void prePersist() {
    createdOn = new Date();
    modifiedOn = createdOn;
    version = 1L;
  }

  @PreUpdate
  protected void preUpdate() {
    modifiedOn = new Date();
  }

  @PreRemove
  protected void preRemove() {
    modifiedOn = new Date();
  }

  @PostRemove
  protected void postRemove() {
    deleted = true;
  }


}
