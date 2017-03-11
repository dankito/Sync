var ContactHandler = new function() {

    SyncEntityState = {
        CREATED : 'CREATED',
        CHANGED : 'CHANGED',
        DELETED : 'DELETED'
    };

    this.synchronizeContact = function(address, port, contact, syncEntityState) {
        var message = {
            'contact' : ContactHandler.mapContactToSyncContact(contact),
            'state' : syncEntityState
        };

        message = SyncAppCommunicatorConfig.SyncContactMessage + SyncAppCommunicatorConfig.DevicesDiscoveryMessagePartsSeparator + objectToJson(message);

        Network.sendMessageViaTcp(address, port, message);
    };


    this.mapContactToSyncContact = function(contact, addressBookURI) {
        var mapped = { };

        mapped.uuid = contact.uuid;
        mapped.localId = contact.localId;
        mapped.addressBookURI = addressBookURI;

        try {
            var properties = contact.properties.QueryInterface(Components.interfaces.nsISimpleEnumerator);

            while(properties.hasMoreElements()) {
                var property = properties.getNext();
                property = property.QueryInterface(Components.interfaces.nsIProperty);
                mapped[property.name] = property.value;
            }
        } catch(e) { log('Could not map all properties'); log(e); }

        return mapped;
    };
}