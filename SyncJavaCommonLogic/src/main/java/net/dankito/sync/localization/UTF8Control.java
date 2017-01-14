package net.dankito.sync.localization;


import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

/**
 * <p>
 *  By default .properties files only supports ISO-8859-1 (Latin-1) as encoding.
 *  To be able to load non Latin-1 characters, a custom ResourceBundle.Control has to be written which reads properties file in UTF-8 encoding.
 * </p>
 */
public class UTF8Control extends ResourceBundle.Control {

  public ResourceBundle newBundle(String baseName, Locale locale, String format, ClassLoader loader, boolean reload)
      throws IllegalAccessException, InstantiationException, IOException {
    // The below is a copyFile of the default implementation.
    String bundleName = toBundleName(baseName, locale);
    String resourceName = toResourceName(bundleName, "properties");
    ResourceBundle bundle = null;
    InputStream stream = null;

    if(reload) {
      URL url = loader.getResource(resourceName);
      if(url != null) {
        URLConnection connection = url.openConnection();
        if(connection != null) {
          connection.setUseCaches(false);
          stream = connection.getInputStream();
        }
      }
    }
    else {
      stream = getClass().getClassLoader().getResourceAsStream(resourceName);
    }

    if(stream != null) {
      try {
        // Only this line is changed to make it to read properties files as UTF-8.
        bundle = new PropertyResourceBundle(new InputStreamReader(stream, "UTF-8"));
      }
      finally {
        stream.close();
      }
    }
    return bundle;
  }

}
