

function syncAddressBook() {
    log('Starting to sync address book ...');

    var contacts = getAllContacts();
}


function getAllContacts() {
    log('Getting all contacts ...');

    var contacts = new Array();

    let abManager = Components.classes["@mozilla.org/abmanager;1"]
                                  .getService(Components.interfaces.nsIAbManager);

    let allAddressBooks = abManager.directories;

    while (allAddressBooks.hasMoreElements()) {
      let addressBook = allAddressBooks.getNext()
                                       .QueryInterface(Components.interfaces.nsIAbDirectory);
      if (addressBook instanceof Components.interfaces.nsIAbDirectory) { // or nsIAbItem or nsIAbCollection
        log("AddressBook Name:" + addressBook.dirName);
        var contactsInAddressBooks = getAllContactsFromAddressBook(addressBook);
        var nextContact;

        while(nextContact = GetNextValidContact(contactsInAddressBooks)) {
            contacts.push(nextContact);
        }
      }
    }

    log('Retrieved ' + contacts.length + ' contacts from address book');

    return contacts;
}



function getAllContactsFromAddressBook(addressbook) {
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


function CardsSimpleEnum() {};

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


function GetNextValidContact(contacts) {
	var contact = null;

	while(contacts.hasMoreElements()) {
		contact = contacts.getNext();
		contact = contact.QueryInterface(Components.interfaces.nsIAbCard);

		if(contact.isMailList == false) {
			break;
		}
	}

	return contact;
}