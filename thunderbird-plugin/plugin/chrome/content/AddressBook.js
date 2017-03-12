var AddressBook = new function() {


    this.getAllContacts = function() {
        var contacts = new Array();

        let abManager = Components.classes["@mozilla.org/abmanager;1"]
                                      .getService(Components.interfaces.nsIAbManager);

        let allAddressBooks = abManager.directories;

        while (allAddressBooks.hasMoreElements()) {
          let addressBook = allAddressBooks.getNext()
                                           .QueryInterface(Components.interfaces.nsIAbDirectory);
          if (addressBook instanceof Components.interfaces.nsIAbDirectory) { // or nsIAbItem or nsIAbCollection
            var contactsInAddressBooks = _getAllContactsFromAddressBook(addressBook);
            var nextContact;

            while(nextContact = _getNextValidContact(contactsInAddressBooks)) {
                contacts.push(ContactHandler.mapContactToSyncContact(nextContact, addressBook.URI));
            }
          }
        }

        return contacts;
    };


    this.handleSynchronizedContact = function (contact, state) {
        if(state === SyncEntityState.CREATED) {
            return _handleCreatedContact(contact);
        }
        else if(state === SyncEntityState.CHANGED) {
            return _handleChangedContact(contact);
        }
        else if(state === SyncEntityState.DELETED) {
            return _handleDeletedContact(contact);
        }
    };

    var _handleCreatedContact = function(contact) {
        var addressBook = _getAddressBookToInsertNewContact();
        var card = ContactHandler.mapSyncContactToCard(contact);

        let newCard = addressBook.addCard(card);
        return ContactHandler.mapContactToSyncContact(newCard, addressBook.URI);
    };

    var _handleChangedContact = function(contact) {
        var addressBook = _getAddressBookForSyncContact(contact);
        var addressBookCard = _getCardFromAddressBookForSyncContact(contact, addressBook);

        var editedCard = ContactHandler.mapSyncContactToCard(contact);
        ContactHandler.mergeCards(addressBookCard, editedCard);

        addressBook.modifyCard(addressBookCard);

        return ContactHandler.mapContactToSyncContact(addressBookCard, addressBook.URI);
    };

    var _handleDeletedContact = function(contact) {
        var addressBook = _getAddressBookForSyncContact(contact);
        var addressBookCard = _getCardFromAddressBookForSyncContact(contact, addressBook);

        var editedCard = ContactHandler.mapSyncContactToCard(contact);
        ContactHandler.mergeCards(addressBookCard, editedCard);

        var cardsToDelete = Components.classes["@mozilla.org/array;1"]
                .createInstance(Components.interfaces.nsIMutableArray);
        cardsToDelete.appendElement(addressBookCard, false);
        
        addressBook.deleteCards(cardsToDelete);

        return ContactHandler.mapContactToSyncContact(addressBookCard, addressBook.URI);
    };


    this.getAddressBookForURI = function(addressBookURI) {
        let abManager = Components.classes["@mozilla.org/abmanager;1"]
            .getService(Components.interfaces.nsIAbManager);

        return abManager.getDirectory(addressBookURI);
    };

    this.getAddressBookForName = function(directoryName) {
        let abManager = Components.classes["@mozilla.org/abmanager;1"]
            .getService(Components.interfaces.nsIAbManager);

        let allAddressBooks = abManager.directories;

        while (allAddressBooks.hasMoreElements()) {
            let addressBook = allAddressBooks.getNext()
                .QueryInterface(Components.interfaces.nsIAbDirectory);

            if (addressBook instanceof Components.interfaces.nsIAbDirectory) { // or nsIAbItem or nsIAbCollection
                if(addressBook.dirName == directoryName) {
                    return addressBook;
                }
            }
        }

        return null;
    };

    this.getAddressBookForCard = function(card) {
        try {
            var directoryId = card.directoryId;
            var directoryName = directoryId.substring(directoryId.indexOf("&") + 1);
            return this.getAddressBookForName(directoryName);
        } catch(e) { log('Could not get address book for card ' + card + ': ' + e); }

        return null;
    };


    var _getAddressBookToInsertNewContact = function() {
        let abManager = Components.classes["@mozilla.org/abmanager;1"]
            .getService(Components.interfaces.nsIAbManager);

        let allAddressBooks = abManager.directories;

        while (allAddressBooks.hasMoreElements()) {
            let addressBook = allAddressBooks.getNext()
                .QueryInterface(Components.interfaces.nsIAbDirectory);
            if (addressBook instanceof Components.interfaces.nsIAbDirectory) {
                return addressBook;
            }
        }

        return null;
    };

    var _getAddressBookForSyncContact = function(contact) {
        return this.getAddressBookForURI(contact.addressBookURI);
    };

    var _getCardFromAddressBookForSyncContact = function(contact, addressBook) {
        return addressBook.getCardFromProperty("DbRowID", contact.localId, false); // TODO: what to use contact.localId or contact.DbRowID ?
    };


    var _getAllContactsFromAddressBook = function(addressbook) {
        var contacts = addressbook.childCards;

        try {
            contacts.hasMoreElements();
        }
        catch(ex) {
            try {
                contacts.first();
            }
            catch(ex) {}

            var cse = new CardsSimpleEnum();

            cse.enumerator = contacts;

            return cse;
        }

        return contacts;
    }


    var CardsSimpleEnum = function() {};

    CardsSimpleEnum.prototype = {
        buffer : null,
        atend : false,
        enumerator : null,

        hasMoreElements : function () {
            if(this.buffer != null) return true;

            if(this.atend) return false;

            try {
                this.buffer = this.enumerator.currentItem();

                try {
                    this.enumerator.next();
                }
                catch (ex) {
                    this.atend = true;
                }

                return true;
            }
            catch (ex) {}

            return false;
        },

        getNext : function () {
            if(this.buffer != null) {
                var tmp = this.buffer;
                this.buffer = null;
                return tmp;
            }
            else {
                throw("hasMoreElements() was not called");
            }
        }
    };


    var _getNextValidContact = function(contacts) {
        var contact = null;

        while(contacts.hasMoreElements()) {
            contact = contacts.getNext();
            contact = contact.QueryInterface(Components.interfaces.nsIAbCard);

            if(contact.isMailList == false) {
                break;
            }
        }

        return contact;
    };

}