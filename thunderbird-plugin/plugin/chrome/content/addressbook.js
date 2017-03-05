

function syncAddressBook() {
    log('Starting to sync address book ...')

    let abManager = Components.classes["@mozilla.org/abmanager;1"]
                              .getService(Components.interfaces.nsIAbManager);

    let allAddressBooks = abManager.directories;

    while (allAddressBooks.hasMoreElements()) {
      let addressBook = allAddressBooks.getNext()
                                       .QueryInterface(Components.interfaces.nsIAbDirectory);
      if (addressBook instanceof Components.interfaces.nsIAbDirectory) { // or nsIAbItem or nsIAbCollection
        log("Directory Name:" + addressBook.dirName);
      }
    }
}