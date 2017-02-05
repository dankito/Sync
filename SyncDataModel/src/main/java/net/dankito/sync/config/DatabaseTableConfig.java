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


  /*          LocalConfig_SynchronizedDevices JoinTable Column Names        */

  public static final String LOCAL_CONFIG_SYNCHRONIZED_DEVICES_JOIN_TABLE_NAME = "LocalConfig_SynchronizedDevices";

  public static final String LOCAL_CONFIG_SYNCHRONIZED_DEVICES_LOCAL_CONFIG_ID_COLUMN_NAME = "local_config_id";
  public static final String LOCAL_CONFIG_SYNCHRONIZED_DEVICES_DEVICE_ID_COLUMN_NAME = "device_id";


  /*          LocalConfig_IgnoredDevices JoinTable Column Names        */

  public static final String LOCAL_CONFIG_IGNORED_DEVICES_JOIN_TABLE_NAME = "LocalConfig_IgnoredDevices";

  public static final String LOCAL_CONFIG_IGNORED_DEVICES_LOCAL_CONFIG_ID_COLUMN_NAME = "local_config_id";
  public static final String LOCAL_CONFIG_IGNORED_DEVICES_DEVICE_ID_COLUMN_NAME = "device_id";


  /*          Device Column Names        */

  public static final String DEVICE_TABLE_NAME = "Device";

  public static final String DEVICE_UNIQUE_DEVICE_ID_COLUMN_NAME = "unique_device_id";
  public static final String DEVICE_NAME_TYPE_COLUMN_NAME = "name";
  public static final String DEVICE_OS_TYPE_COLUMN_NAME = "os_type";
  public static final String DEVICE_OS_NAME_TYPE_COLUMN_NAME = "os_name";
  public static final String DEVICE_OS_VERSION_TYPE_COLUMN_NAME = "os_version";
  public static final String DEVICE_DESCRIPTION_COLUMN_NAME = "description";
  public static final String DEVICE_SOURCE_SYNC_CONFIGURATIONS_JOIN_COLUMN_NAME = "source_sync_configurations";
  public static final String DEVICE_DESTINATION_SYNC_CONFIGURATIONS_JOIN_COLUMN_NAME = "destination_sync_configurations";


  /*          SyncEntity Column Names        */

  public static final String SYNC_ENTITY_TABLE_NAME = "SyncEntity";

  public static final String SYNC_ENTITY_DISCRIMINATOR_COLUMN_NAME = "entity_type";

  public static final String SYNC_ENTITY_SOURCE_DEVICE_ID_JOIN_COLUMN_NAME = "source_device_id";
  public static final String SYNC_ENTITY_CREATED_ON_DEVICE_COLUMN_NAME = "created_on_device";


  /*          CallLogSyncEntity Column Names        */

  public static final String CALL_LOG_SYNC_ENTITY_TABLE_NAME = "CallLogSyncEntity";
  public static final String CALL_LOG_SYNC_ENTITY_DISCRIMINATOR_VALUE = "call_log";

  public static final String CALL_LOG_SYNC_ENTITY_NUMBER_COLUMN_NAME = "number";
  public static final String CALL_LOG_SYNC_ENTITY_NORMALIZED_NUMBER_COLUMN_NAME = "normalized_number";
  public static final String CALL_LOG_SYNC_ENTITY_ASSOCIATED_CONTACT_NAME_COLUMN_NAME = "associated_contact_name";
  public static final String CALL_LOG_SYNC_ENTITY_ASSOCIATED_CONTACT_LOOKUP_KEY_COLUMN_NAME = "associated_contact_lookup_key";
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
  public static final String CONTACT_SYNC_ENTITY_EMAIL_ADDRESS_COLUMN_NAME = "email_address";
  public static final String CONTACT_SYNC_ENTITY_WEBSITE_URL_COLUMN_NAME = "website_url";
  public static final String CONTACT_SYNC_ENTITY_NOTE_COLUMN_NAME = "note";


  /*          EmailSyncEntity Column Names        */

  public static final String EMAIL_SYNC_ENTITY_TABLE_NAME = "EmailSyncEntity";

  public static final String EMAIL_SYNC_ENTITY_ADDRESS_COLUMN_NAME = "address";
  public static final String EMAIL_SYNC_ENTITY_TYPE_COLUMN_NAME = "type";
  public static final String EMAIL_SYNC_ENTITY_LABEL_COLUMN_NAME = "label";


  /*          PhoneNumberSyncEntity Column Names        */

  public static final String PHONE_NUMBER_SYNC_ENTITY_TABLE_NAME = "PhoneNumberSyncEntity";

  public static final String PHONE_NUMBER_SYNC_ENTITY_NUMBER_COLUMN_NAME = "number";
  public static final String PHONE_NUMBER_SYNC_ENTITY_NORMALIZED_NUMBER_COLUMN_NAME = "normalized_number";
  public static final String PHONE_NUMBER_SYNC_ENTITY_TYPE_COLUMN_NAME = "type";
  public static final String PHONE_NUMBER_SYNC_ENTITY_LABEL_COLUMN_NAME = "label";


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

  public static final String SYNC_MODULE_CONFIGURATION_SYNC_MODULE_TYPE_COLUMN_NAME = "sync_module_type";
  public static final String SYNC_MODULE_CONFIGURATION_IS_ENABLED_COLUMN_NAME = "is_enabled";
  public static final String SYNC_MODULE_CONFIGURATION_IS_BIDIRECTIONAL_COLUMN_NAME = "is_bidirectional";
  public static final String SYNC_MODULE_CONFIGURATION_SOURCE_PATH_COLUMN_NAME = "source_path";
  public static final String SYNC_MODULE_CONFIGURATION_DESTINATION_PATH_COLUMN_NAME = "destination_path";
  public static final String SYNC_MODULE_CONFIGURATION_KEEP_DELETED_ENTITIES_ON_DESTINATION_COLUMN_NAME = "keep_deleted_entities_on_destination";


  /*          SyncJobItem Column Names        */

  public static final String SYNC_JOB_ITEM_TABLE_NAME = "SyncJobItem";

  public static final String SYNC_JOB_ITEM_SYNC_MODULE_CONFIGURATION_JOIN_COLUMN_NAME = "sync_module_configuration";
  public static final String SYNC_JOB_ITEM_SYNC_ENTITY_JOIN_COLUMN_NAME = "sync_entity";
  public static final String SYNC_JOB_ITEM_STATE_COLUMN_NAME = "state";
  public static final String SYNC_JOB_ITEM_SOURCE_DEVICE_COLUMN_NAME = "source_device";
  public static final String SYNC_JOB_ITEM_DESTINATION_DEVICE_COLUMN_NAME = "destination_device";
  public static final String SYNC_JOB_ITEM_START_TIME_COLUMN_NAME = "start_time";
  public static final String SYNC_JOB_ITEM_FINISH_TIME_COLUMN_NAME = "finish_time";
  public static final String SYNC_JOB_ITEM_DATA_SIZE_COLUMN_NAME = "data_size";


  /*          SyncEntityLocalLookupKeys Column Names        */

  public static final String SYNC_ENTITY_LOCAL_LOOKUP_KEYS_TABLE_NAME = "SyncEntityLocalLookupKeys";

  public static final String SYNC_ENTITY_LOCAL_LOOK_UP_KEYS_ENTITY_TYPE_COLUMN_NAME = "entity_type";
  public static final String SYNC_ENTITY_LOCAL_LOOK_UP_KEYS_ENTITY_DATABASE_ID_COLUMN_NAME = "entity_database_id";
  public static final String SYNC_ENTITY_LOCAL_LOOK_UP_KEYS_ENTITY_LOCAL_LOOKUP_KEY_COLUMN_NAME = "entity_local_lookup_key";
  public static final String SYNC_ENTITY_LOCAL_LOOK_UP_KEYS_ENTITY_LAST_MODIFIED_ON_DEVICE_COLUMN_NAME = "last_modified_on_device";
  public static final String SYNC_ENTITY_LOCAL_LOOK_UP_KEYS_SYNC_MODULE_CONFIGURATION_JOIN_COLUMN_NAME = "sync_module_configuration";

}
