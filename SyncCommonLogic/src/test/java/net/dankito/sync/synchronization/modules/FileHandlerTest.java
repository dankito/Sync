package net.dankito.sync.synchronization.modules;

import net.dankito.utils.services.JavaFileStorageService;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;


public class FileHandlerTest {

  protected static final String TEST_SOURCE_ROOT_FOLDER = "/storage/emulated/0/data/Pictures";

  protected static final String TEST_DESTINATION_ROOT_FOLDER = "~/Pictures/Synchronized_from_Android/";

  protected static final String TEST_SOURCE_FILE_RELATIVE_PATH = "DCIM/IMG_2017.03.27.jpg";

  protected static final File TEST_SOURCE_FILE_PATH = new File(TEST_SOURCE_ROOT_FOLDER, TEST_SOURCE_FILE_RELATIVE_PATH);

  protected static final File TEST_DESTINATION_FILE_PATH = new File(TEST_DESTINATION_ROOT_FOLDER, TEST_SOURCE_FILE_RELATIVE_PATH);


  protected FileHandler underTest;


  @Before
  public void setUp() throws Exception {
    underTest = new FileHandler(new JavaFileStorageService());
  }

  @After
  public void tearDown() throws Exception {

  }


  @Test
  public void getFileDestinationPath() throws Exception {
    File result = underTest.getFileDestinationPath(TEST_SOURCE_ROOT_FOLDER, TEST_DESTINATION_ROOT_FOLDER, TEST_SOURCE_FILE_PATH.getAbsolutePath());

    Assert.assertEquals(TEST_DESTINATION_FILE_PATH.getAbsolutePath(), result.getAbsolutePath());
  }

  @Test
  public void getFileRelativePath() throws Exception {
    File result = underTest.getFileRelativePath(TEST_SOURCE_ROOT_FOLDER, TEST_SOURCE_FILE_PATH.getAbsolutePath());

    Assert.assertEquals(TEST_SOURCE_FILE_RELATIVE_PATH, result.getPath());
  }

}