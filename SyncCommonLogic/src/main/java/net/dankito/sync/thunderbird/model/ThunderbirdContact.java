package net.dankito.sync.thunderbird.model;


public class ThunderbirdContact {

  // as almost all property names start with an upper case letter, Jackson can not deserialize them using getter/setter -> i only use public fields.

  public String uuid;

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


  @Override
  public String toString() {
    String description = PrimaryEmail;

    return description;
  }

}
