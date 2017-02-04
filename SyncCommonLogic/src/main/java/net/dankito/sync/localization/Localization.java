package net.dankito.sync.localization;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;
import java.util.ResourceBundle;

import javax.inject.Named;


@Named
public class Localization {

  protected static final String StringsResourceBundleName = "Strings";


  private static final Logger log = LoggerFactory.getLogger(Localization.class);



  protected Locale languageLocale = Locale.getDefault();

  protected ResourceBundle stringsResourceBundle = null;


  public Localization() {
    try {
      stringsResourceBundle = ResourceBundle.getBundle(StringsResourceBundleName, languageLocale, new UTF8Control());
    } catch(Exception ex) {
      log.error("Could not load " + StringsResourceBundleName + ". No Strings will now be translated, only their resource keys will be displayed.", ex);
    }
  }


  public Locale getLanguageLocale() {
    return languageLocale;
  }

  public void setLanguageLocale(Locale languageLocale) {
    languageLocale = languageLocale;
    Locale.setDefault(languageLocale);
    stringsResourceBundle = ResourceBundle.getBundle(StringsResourceBundleName, languageLocale, new UTF8Control());
  }

  public ResourceBundle getStringsResourceBundle() {
    return stringsResourceBundle;
  }



  public String getLocalizedString(String resourceKey) {
    try {
      return getStringsResourceBundle().getString(resourceKey);
    } catch(Exception ex) {
      log.error("Could not get Resource for key {} from String Resource Bundle {}", resourceKey, StringsResourceBundleName);
    }

    return resourceKey;
  }

  public String getLocalizedString(String resourceKey, Object... formatArguments) {
    return String.format(getLocalizedString(resourceKey), formatArguments);
  }

}
