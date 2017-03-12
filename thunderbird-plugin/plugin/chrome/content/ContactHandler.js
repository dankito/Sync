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

        mapped.addressBookURI = addressBookURI;
        mapped.localId = contact.localId;
        mapped.uuid = contact.uuid; // currently unused

        try {
            var properties = contact.properties.QueryInterface(Components.interfaces.nsISimpleEnumerator);

            while(properties.hasMoreElements()) {
                var property = properties.getNext();
                property = property.QueryInterface(Components.interfaces.nsIProperty);
                mapped[property.name] = property.value;
            }
        } catch(e) { log('Could not map all properties'); logObject(e); }

        return mapped;
    };

    this.mapSyncContactToCard = function(contact) {
        let card = Components.classes["@mozilla.org/addressbook/cardproperty;1"].createInstance(Components.interfaces.nsIAbCard);

        card.uuid = contact.uuid;
        card.localId = contact.localId;

        try {
            Object.keys(contact).forEach(function(property) {
                try {
                    let value = contact[property];
                    if(value != null) {
                        card.setProperty(property, value);
                    }
                } catch(e) { log('Could not set property ' + property + ' on card:'); logObject(e); }
            });
        } catch(e) { log('Could not map all properties of Sync contact to card'); logObject(e); }

        return card;
    };

    this.mergeCards = function(sink, source) {
        try {
            var sourceProperties = source.properties.QueryInterface(Components.interfaces.nsISimpleEnumerator);

            while(sourceProperties.hasMoreElements()) {
                var property = sourceProperties.getNext();
                property = property.QueryInterface(Components.interfaces.nsIProperty);
                sink.setProperty(property.name, property.value);
            }


            // delete properties that are on sink but not on source
            // TODO: in order to make this work filter out system properties returned by properties enumerator, don't delete them
            // var sinkProperties = sink.properties.QueryInterface(Components.interfaces.nsISimpleEnumerator);
            //
            // while(sinkProperties.hasMoreElements()) {
            //     var property = sinkProperties.getNext();
            //     property = property.QueryInterface(Components.interfaces.nsIProperty);
            //     log('checking if property is available on source ' + property.name);
            //     try {
            //         if (source.getProperty(property.name, null) == null) {
            //             log('Trying to delete property ' + property.name);
            //             sink.deleteProperty(property.name);
            //         }
            //     } catch(e) { log('' + e); }
            // }
        } catch(e) { log('Could not merge cards'); logObject(e); }
    };

};