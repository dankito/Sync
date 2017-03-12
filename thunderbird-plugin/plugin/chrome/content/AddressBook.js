var AddressBook = new function() {

    this.syncAddressBook = function() {
        log('Starting to sync address book ...');

        var contacts = getAllContacts();
    };


    this.getAllContacts = function() {
        log('Getting all contacts ...');

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

        log('Retrieved ' + contacts.length + ' contacts from address book');

        return contacts;
    };


    this.handleSynchronizedContact = function (contact, state) {
        if(state == SyncEntityState.CREATED) {
            return _handleCreatedContact(contact);
        }
        else if(state == SyncEntityState.CHANGED) {
            return _handleChangedContact(contact);
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
        
        var addressBookCard = addressBook.getCardFromProperty("DbRowID", contact.localId, false); // TODO: what to use contact.localId or contact.DbRowID ?
        var editedCard = ContactHandler.mapSyncContactToCard(contact);
        ContactHandler.mergeCards(addressBookCard, editedCard);

        addressBook.modifyCard(addressBookCard);
        return ContactHandler.mapContactToSyncContact(addressBookCard, addressBook.URI);
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
        let abManager = Components.classes["@mozilla.org/abmanager;1"]
            .getService(Components.interfaces.nsIAbManager);

        return abManager.getDirectory(contact.addressBookURI);
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