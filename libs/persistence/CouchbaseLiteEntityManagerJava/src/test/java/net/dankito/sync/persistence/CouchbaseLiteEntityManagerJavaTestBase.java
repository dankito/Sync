package net.dankito.sync.persistence;


import net.dankito.sync.BaseEntity;
import net.dankito.utils.services.JavaFileStorageService;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

public abstract class CouchbaseLiteEntityManagerJavaTestBase {

  protected static final String DATA_FOLDER_PATH = "testData";

  protected static final int DATA_MODEL_VERSION = 1;


  protected CouchbaseLiteEntityManagerJava underTest;

  protected EntityManagerConfiguration configuration;


  @Before
  public void setUp() throws Exception {
    configuration = createEntityManagerConfiguration();

    underTest = new CouchbaseLiteEntityManagerJava(configuration);
  }

  @After
  public void tearDown() {
    underTest.close();

    new JavaFileStorageService().deleteFolderRecursively(underTest.getDatabasePath());
  }


  protected abstract Class<? extends BaseEntity> getEntityClass();

  protected abstract BaseEntity createTestEntity();

  protected abstract void updateEntity(BaseEntity testEntity);

  protected abstract List<BaseEntity> updateEntityReferences(BaseEntity testEntity);

  protected abstract void assertEntityHasBeenCorrectlyRetrieved(BaseEntity retrievedEntity);

  protected abstract void assertUpdatedEntityHasBeenCorrectlyRetrieved(BaseEntity retrievedEntity);

  protected abstract void assertUpdatedEntityReferencesHaveBeenCorrectlyRetrieved(BaseEntity retrievedEntity);


  @Test
  public void testPersistEntity() {
    BaseEntity testEntity = createTestEntity();
    assertEntityIsNotPersisted(testEntity);

    underTest.persistEntity(testEntity);

    assertEntityIsPersisted(testEntity);
    underTest.getObjectCache().clear();

    BaseEntity retrievedEntity = underTest.getEntityById(getEntityClass(), testEntity.getId());
    assertEntityHasBeenCorrectlyRetrieved(retrievedEntity);


    List<BaseEntity> allEntitiesOfThisType = (List<BaseEntity>)underTest.getAllEntitiesOfType(getEntityClass());
    Assert.assertEquals(1, allEntitiesOfThisType.size());
  }

  @Test
  public void testUpdateEntity() {
    BaseEntity testEntity = createTestEntity();
    underTest.persistEntity(testEntity);

    String previousId = testEntity.getId();
    Long previousVersion = testEntity.getVersion();

    updateEntity(testEntity);
    underTest.updateEntity(testEntity);

    assertEntityIsUpdated(testEntity, previousId, previousVersion);
    underTest.getObjectCache().clear();

    BaseEntity retrievedEntity = underTest.getEntityById(getEntityClass(), testEntity.getId());
    assertUpdatedEntityHasBeenCorrectlyRetrieved(retrievedEntity);


    List<BaseEntity> allEntitiesOfThisType = (List<BaseEntity>)underTest.getAllEntitiesOfType(getEntityClass());
    Assert.assertEquals(1, allEntitiesOfThisType.size());
  }

  @Test
  public void testUpdateEntityReferences() {
    BaseEntity testEntity = createTestEntity();
    underTest.persistEntity(testEntity);

    List<BaseEntity> entityReferences = updateEntityReferences(testEntity);
    for(BaseEntity reference : entityReferences) {
      underTest.updateEntity(reference);
    }

    underTest.getObjectCache().clear();

    BaseEntity retrievedEntity = underTest.getEntityById(getEntityClass(), testEntity.getId());
    assertUpdatedEntityReferencesHaveBeenCorrectlyRetrieved(retrievedEntity);


    List<BaseEntity> allEntitiesOfThisType = (List<BaseEntity>)underTest.getAllEntitiesOfType(getEntityClass());
    Assert.assertEquals(1, allEntitiesOfThisType.size());
  }

  @Test
  public void testRemoveEntity() {
    BaseEntity testEntity = createTestEntity();
    underTest.persistEntity(testEntity);

    underTest.deleteEntity(testEntity);

    Assert.assertNull(testEntity.getId());
    Assert.assertNotEquals(testEntity.getCreatedOn(), testEntity.getModifiedOn());
    Assert.assertTrue(testEntity.isDeleted());

    underTest.getObjectCache().clear();

    BaseEntity retrievedEntity = underTest.getEntityById(getEntityClass(), testEntity.getId());
    Assert.assertNull(retrievedEntity);


    List<BaseEntity> allEntitiesOfThisType = (List<BaseEntity>)underTest.getAllEntitiesOfType(getEntityClass());
    Assert.assertEquals(0, allEntitiesOfThisType.size());
  }


  protected void assertEntityIsPersisted(BaseEntity entity) {
    Assert.assertNotNull(entity.getId());

    Assert.assertNotNull(entity.getCreatedOn());
    Assert.assertNotNull(entity.getModifiedOn());

    Assert.assertTrue(entity.getVersion() > 0);
  }

  protected void assertEntityIsNotPersisted(BaseEntity entity) {
    Assert.assertNull(entity.getId());

    Assert.assertNull(entity.getCreatedOn());
    Assert.assertNull(entity.getModifiedOn());

    Assert.assertNull(entity.getVersion());
  }

  protected void assertEntityIsUpdated(BaseEntity entity, String previousId, Long previousVersion) {
    Assert.assertEquals(previousId, entity.getId());

    Assert.assertNotEquals(entity.getCreatedOn(), entity.getModifiedOn());

    Assert.assertTrue(entity.getVersion() > previousVersion);
  }


  protected EntityManagerConfiguration createEntityManagerConfiguration() {
    EntityManagerConfiguration configuration = new EntityManagerConfiguration(DATA_FOLDER_PATH, DATA_MODEL_VERSION);

    return configuration;
  }

}
