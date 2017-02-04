package net.dankito.sync.javafx.localization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Enumeration;
import java.util.ResourceBundle;

import javafx.fxml.FXMLLoader;

/**
 * <p>
 * Passing FXMLLoader a ResourceBundle, it translates all Strings with resource keys.
 * But then i can't bind Controls with JavaFxLocalization methods for dynamic language switching as i don't have the resource keys anymore.
 *
 * Not passing FXMLLoader a ResourceBundle with all resource keys results in a LoadException and Application doesn't start.
 *
 * So in order to make FXMLLoader happy and retain resource key, i implemented this dummy ResourceBundle which just returns the keys as values.
 * Even though a bit overhead, as first FXMLLoader parses Scene Nodes and then i, but it works.
 * </p>
 * Created by ganymed on 18/09/15.
 */
public class AvoidFxmlLoaderTranslatesResourceKeysResourceBundle extends ResourceBundle {

  private final static Logger log = LoggerFactory.getLogger(AvoidFxmlLoaderTranslatesResourceKeysResourceBundle.class);


  protected ResourceBundle realResourceBundle = null;


  public AvoidFxmlLoaderTranslatesResourceKeysResourceBundle() {

  }

  public AvoidFxmlLoaderTranslatesResourceKeysResourceBundle(ResourceBundle realResourceBundle) {
    this();
    this.realResourceBundle = realResourceBundle;
  }


  @Override
  public boolean containsKey(String key) {
    if(realResourceBundle != null && realResourceBundle.containsKey(key) == false)
      log.warn("Resource Key '" + key + "' not found in ResourceBundle");

    return true;
  }

  @Override
  protected Object handleGetObject(String key) {
    return FXMLLoader.RESOURCE_KEY_PREFIX + key;
  }

  @Override
  public Enumeration<String> getKeys() {
    return null;
  }
}
