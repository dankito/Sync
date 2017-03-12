package net.dankito.sync.thunderbird.model;


import com.fasterxml.jackson.annotation.JsonIgnore;

import net.dankito.utils.StringUtils;

public class ThunderbirdContact {

  protected static final String ADDRESS_BOOK_URI_AND_LOCAL_ID_SEPARATOR = "#";


  // as almost all property names start with an upper case letter, Jackson can not deserialize them using getter/setter -> i only use public fields.

  public String localId;

  public String DbRowID;

  public String addressBookURI;

  public long LastModifiedDate;


  // copied from https://dxr.mozilla.org/comm-central/source/mailnews/addrbook/public/nsIAbCard.idl

  public String FirstName;
  public String LastName;
  public String DisplayName;
  public String NickName;
  public String PrimaryEmail;
  public String PreferMailFormat;
  public String PopularityIndex;

  public String PhoneticFirstName;
  public String PhoneticLastName;
  public String SpouseName;
  public String FamilyName;
  public String SecondEmail;

  public String HomeAddress;
  public String HomeAddress2;
  public String HomeCity;
  public String HomeState;
  public String HomeZipCode;
  public String HomeCountry;
  public String WebPage2;

  public String WorkAddress;
  public String WorkAddress2;
  public String WorkCity;
  public String WorkState;
  public String WorkZipCode;
  public String WorkCountry;
  public String WebPage1;

  public String HomePhone;
  public String HomePhoneType;
  public String WorkPhone;
  public String WorkPhoneType;
  public String FaxNumber;
  public String FaxNumberType;
  public String PagerNumberType;
  public String PagerNumber;
  public String CellularNumber;
  public String CellularNumberType;

  public String JobTitle;
  public String Department;
  public String Company;
  public String Custom1;
  public String Custom2;
  public String Custom3;
  public String Custom4;
  public String Notes;

  public String _GoogleTalk;
  public String _AimScreenName;
  public String _Yahoo;
  public String _Skype;
  public String _QQ;
  public String _MSN;
  public String _ICQ;
  public String _JabberId;
  public String _IRC;

  public String AnniversaryYear;
  public String AnniversaryMonth;
  public String AnniversaryDay;
  public String BirthYear;
  public String BirthMonth;
  public String BirthDay;



  @JsonIgnore
  public String getLocalLookupKey() {
    if(StringUtils.isNotNullOrEmpty(addressBookURI) && StringUtils.isNotNullOrEmpty(localId)) {
      return addressBookURI + ADDRESS_BOOK_URI_AND_LOCAL_ID_SEPARATOR + localId;
    }

    return null;
  }


  @JsonIgnore // needed as otherwise Jackson would serialize this as property and then on deserialization overwrites addressBookURI and localID
  public void setLocalLookupKey(String localLookupKey) {
    if(StringUtils.isNotNullOrEmpty(localLookupKey)) {
      String[] parts = localLookupKey.split(ADDRESS_BOOK_URI_AND_LOCAL_ID_SEPARATOR);

      if(parts.length > 0) {
        this.addressBookURI = parts[0];
      }

      if(parts.length > 1) {
        this.localId = parts[1];
      }
    }
  }


  @Override
  public String toString() {
    String description = PrimaryEmail;

    if(DisplayName != null) {
      description = DisplayName + " (" + description + ")";
    }

    return description;
  }

}
