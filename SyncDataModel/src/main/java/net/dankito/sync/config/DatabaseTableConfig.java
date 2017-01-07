package net.dankito.sync.config;


public class DatabaseTableConfig {


  /*          BaseEntity Column Names        */

  public static final String BASE_ENTITY_ID_COLUMN_NAME = "id";
  public static final String BASE_ENTITY_CREATED_ON_COLUMN_NAME = "created_on";
  public static final String BASE_ENTITY_MODIFIED_ON_COLUMN_NAME = "modified_on";
  public static final String BASE_ENTITY_VERSION_COLUMN_NAME = "version";
  public static final String BASE_ENTITY_DELETED_COLUMN_NAME = "deleted";


  /*          LocalConfig Column Names        */

  public static final String LOCAL_CONFIG_TABLE_NAME = "LocalConfig";

  public static final String LOCAL_CONFIG_LOCAL_DEVICE_ID_JOIN_COLUMN_NAME = "local_device_id";


  /*          Device Column Names        */

  public static final String DEVICE_TABLE_NAME = "Device";

  public static final String DEVICE_UNIQUE_DEVICE_ID_COLUMN_NAME = "unique_device_id";
  public static final String DEVICE_OS_TYPE_COLUMN_NAME = "os_type";
  public static final String DEVICE_OS_NAME_TYPE_COLUMN_NAME = "os_name";
  public static final String DEVICE_OS_VERSION_TYPE_COLUMN_NAME = "os_version";
  public static final String DEVICE_DESCRIPTION_COLUMN_NAME = "description";


  /*          SyncEntity Column Names        */

  public static final String SYNC_ENTITY_TABLE_NAME = "SyncEntity";

  public static final String SYNC_ENTITY_DISCRIMINATOR_COLUMN_NAME = "entity_type";

  public static final String SYNC_ENTITY_SYNC_MODULE_CONFIGURATION_JOIN_COLUMN_NAME = "sync_module_configuration_id";
  public static final String SYNC_ENTITY_SOURCE_DEVICE_ID_JOIN_COLUMN_NAME = "source_device_id";
  public static final String SYNC_ENTITY_ID_ON_SOURCE_DEVICE_COLUMN_NAME = "id_on_source_device";


  /*          CallLogSyncEntity Column Names        */

  public static final String CALL_LOG_SYNC_ENTITY_TABLE_NAME = "CallLogSyncEntity";
  public static final String CALL_LOG_SYNC_ENTITY_DISCRIMINATOR_VALUE = "call_log";

  public static final String CALL_LOG_SYNC_ENTITY_NUMBER_COLUMN_NAME = "number";
  public static final String CALL_LOG_SYNC_ENTITY_NORMALIZED_NUMBER_COLUMN_NAME = "normalized_number";
  public static final String CALL_LOG_SYNC_ENTITY_ASSOCIATED_CONTACT_NAME_COLUMN_NAME = "associated_contact_name";
  public static final String CALL_LOG_SYNC_ENTITY_DATE_COLUMN_NAME = "date";
  public static final String CALL_LOG_SYNC_ENTITY_DURATION_IN_SECONDS_COLUMN_NAME = "duration_in_seconds";
  public static final String CALL_LOG_SYNC_ENTITY_TYPE_COLUMN_NAME = "type";


  /*          ContactSyncEntity Column Names        */

  public static final String CONTACT_SYNC_ENTITY_TABLE_NAME = "ContactSyncEntity";
  public static final String CONTACT_SYNC_ENTITY_DISCRIMINATOR_VALUE = "contact";

  public static final String CONTACT_SYNC_ENTITY_DISPLAY_NAME_COLUMN_NAME = "display_name";
  public static final String CONTACT_SYNC_ENTITY_NICKNAME_COLUMN_NAME = "nickname";
  public static final String CONTACT_SYNC_ENTITY_GIVEN_NAME_COLUMN_NAME = "given_name";
  public static final String CONTACT_SYNC_ENTITY_MIDDLE_NAME_COLUMN_NAME = "middle_name";
  public static final String CONTACT_SYNC_ENTITY_FAMILY_NAME_COLUMN_NAME = "family_name";
  public static final String CONTACT_SYNC_ENTITY_PHONETIC_GIVEN_NAME_COLUMN_NAME = "phonetic_given_name";
  public static final String CONTACT_SYNC_ENTITY_PHONETIC_MIDDLE_NAME_COLUMN_NAME = "phonetic_middle_name";
  public static final String CONTACT_SYNC_ENTITY_PHONETIC_FAMILY_NAME_COLUMN_NAME = "phonetic_family_name";
  public static final String CONTACT_SYNC_ENTITY_PHONE_NUMBER_COLUMN_NAME = "phone_number";
  public static final String CONTACT_SYNC_ENTITY_EMAIL_ADDRESS_COLUMN_NAME = "email_address";
  public static final String CONTACT_SYNC_ENTITY_WEBSITE_URL_COLUMN_NAME = "website_url";
  public static final String CONTACT_SYNC_ENTITY_NOTE_COLUMN_NAME = "note";


  /*          FileSyncEntity Column Names        */

  public static final String FILE_SYNC_ENTITY_TABLE_NAME = "FileSyncEntity";
  public static final String FILE_SYNC_ENTITY_DISCRIMINATOR_VALUE = "file";

  public static final String FILE_SYNC_ENTITY_NAME_COLUMN_NAME = "name";
  public static final String FILE_SYNC_ENTITY_DESCRIPTION_COLUMN_NAME = "description";
  public static final String FILE_SYNC_ENTITY_FILE_PATH_COLUMN_NAME = "file_path";
  public static final String FILE_SYNC_ENTITY_FILE_SIZE_COLUMN_NAME = "file_size";
  public static final String FILE_SYNC_ENTITY_MIME_TYPE_COLUMN_NAME = "mime_type";


  /*          ImageFileSyncEntity Column Names        */

  public static final String IMAGE_FILE_SYNC_ENTITY_TABLE_NAME = "ImageFileSyncEntity";
  public static final String IMAGE_FILE_SYNC_ENTITY_DISCRIMINATOR_VALUE = "image";

  public static final String IMAGE_FILE_SYNC_ENTITY_HEIGHT_COLUMN_NAME = "height";
  public static final String IMAGE_FILE_SYNC_ENTITY_WIDTH_COLUMN_NAME = "width";
  public static final String IMAGE_FILE_SYNC_ENTITY_LATITUDE_COLUMN_NAME = "latitude";
  public static final String IMAGE_FILE_SYNC_ENTITY_LONGITUDE_COLUMN_NAME = "longitude";
  public static final String IMAGE_FILE_SYNC_ENTITY_IMAGE_TAKEN_ON_COLUMN_NAME = "image_taken_on";
  public static final String IMAGE_FILE_SYNC_ENTITY_ORIENTATION_COLUMN_NAME = "orientation";


  /*          SyncConfiguration Column Names        */

  public static final String SYNC_CONFIGURATION_TABLE_NAME = "SyncConfiguration";

  public static final String SYNC_CONFIGURATION_SOURCE_DEVICE_JOIN_COLUMN_NAME = "source_device_id";
  public static final String SYNC_CONFIGURATION_DESTINATION_DEVICE_JOIN_COLUMN_NAME = "destination_device_id";


  /*          SyncModuleConfiguration Column Names        */

  public static final String SYNC_MODULE_CONFIGURATION_TABLE_NAME = "SyncModuleConfiguration";

  public static final String SYNC_MODULE_CONFIGURATION_SYNC_MODULE_CLASS_NAME_COLUMN_NAME = "sync_module_class_name";
  public static final String SYNC_MODULE_CONFIGURATION_IS_BI_DIRECTIONAL_COLUMN_NAME = "is_bi_directional";
  public static final String SYNC_MODULE_CONFIGURATION_DESTINATION_PATH_COLUMN_NAME = "destination_path";


  /*          SyncJobItem Column Names        */

  public static final String SYNC_JOB_ITEM_TABLE_NAME = "SyncJobItem";

  public static final String SYNC_JOB_ITEM_SYNC_MODULE_CONFIGURATION_JOIN_COLUMN_NAME = "source_device_id";
  public static final String SYNC_JOB_ITEM_SYNC_ENTITY_JOIN_COLUMN_NAME = "sync_entity";
  public static final String SYNC_JOB_ITEM_STATE_COLUMN_NAME = "state";
  public static final String SYNC_JOB_ITEM_START_TIME_COLUMN_NAME = "start_time";
  public static final String SYNC_JOB_ITEM_FINISH_TIME_COLUMN_NAME = "finish_time";
}