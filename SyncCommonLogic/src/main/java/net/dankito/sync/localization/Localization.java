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



  protected Locale LanguageLocale = Locale.getDefault();

  protected ResourceBundle StringsResourceBundle = null;

//  protected List<LanguageChangedListener> languageChangedListeners = new ArrayList<>();


  public Localization() {
    try {
      StringsResourceBundle = ResourceBundle.getBundle(StringsResourceBundleName, LanguageLocale, new UTF8Control());
    } catch(Exception ex) {
      log.error("Could not load " + StringsResourceBundleName + ". No Strings will now be translated, only their resource keys will be displayed.", ex);
    }
  }


  public Locale getLanguageLocale() {
    return LanguageLocale;
  }

  public void setLanguageLocale(Locale languageLocale) {
    LanguageLocale = languageLocale;
    Locale.setDefault(languageLocale);
    StringsResourceBundle = ResourceBundle.getBundle(StringsResourceBundleName, LanguageLocale, new UTF8Control());
  }

//  public void setLanguage(ApplicationLanguage language) {
//    try {
//      if(hasLanguageChanged(language)) {
//        setLanguageLocale(new Locale(language.getLanguageKey())); // Locale.forLanguageTag(language.getLanguageKey()) crashes on older Androids
//
//        callLanguageChangeListeners(language);
//      }
//    } catch(Exception ex) {
//      log.error("Could not find Locale for ApplicationLanguage's LanguageKey " + language.getLanguageKey() + " of ApplicationLanguage " + language.getName(), ex);
//    }
//  }
//
//  protected boolean hasLanguageChanged(ApplicationLanguage language) {
//    return language != null && language.getLanguageKey().equals(LanguageLocale.getLanguage()) == false;
//  }

  public ResourceBundle getStringsResourceBundle() {
    return StringsResourceBundle;
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



//  public boolean addLanguageChangedListener(LanguageChangedListener listener) {
//    return languageChangedListeners.add(listener);
//  }
//
//  public boolean removeLanguageChangedListener(LanguageChangedListener listener) {
//    return languageChangedListeners.remove(listener);
//  }
//
//  protected void callLanguageChangeListeners(ApplicationLanguage newLanguage) {
//    for(LanguageChangedListener listener : languageChangedListeners) {
//      listener.languageChanged(newLanguage);
//    }
//  }
}
