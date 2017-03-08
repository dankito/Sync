var AddressBookListener = new function() {

    this.init = function() {
        _registerAddressBooksListener();
    };

    var _registerAddressBooksListener = function() {
        let abManager = Components.classes["@mozilla.org/abmanager;1"]
                                      .getService(Components.interfaces.nsIAbManager);

        abManager.addAddressBookListener(_addressBookAbListener, nsIAbListener.itemAdded |
                            nsIAbListener.directoryItemRemoved | nsIAbListener.directoryRemoved | nsIAbListener.itemChanged);
    };


    /*      nsIAbListener implementation, see https://dxr.mozilla.org/comm-central/source/mailnews/addrbook/public/nsIAbListener.idl     */

    var _addressBookAbListener = {
       onItemAdded: function(parentDir, item) {
            if(item instanceof Components.interfaces.nsIAbCard) {
               _synchronizeContact(item, SyncEntityState.CREATED);
            }
            else if (item instanceof Components.interfaces.nsIAbDirectory) {
               // TODO
            }
        },

       onItemRemoved: function(parentDir, item) {
            if(item instanceof Components.interfaces.nsIAbCard) {
               _synchronizeContact(item, SyncEntityState.DELETED);
            }
            else if (item instanceof Components.interfaces.nsIAbDirectory) {
               // TODO
            }
       },

       onItemPropertyChanged: function(item, property, oldValue, newValue) {
           // property, oldValue, newValue always(?) seem to be null
           if(item instanceof Components.interfaces.nsIAbCard) {
              _synchronizeContact(item, SyncEntityState.UPDATED);
           }
           else if (item instanceof Components.interfaces.nsIAbDirectory) {
              // TODO
           }
       }
     };

    var _synchronizeContact = function(item, syncEntityState) {
        // TODO: get actual address and port
       ContactHandler.synchronizeContact('127.0.0.1', 32798, item, syncEntityState);
    }

}