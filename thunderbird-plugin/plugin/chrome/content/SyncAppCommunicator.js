var SyncAppCommunicator = new function () {

    this.start = function() {
        var messagesPort = 32797;

        networkUtil.startTcpListenerSocket(messagesPort, function(receivedMessage) {
            log('Received message in SyncAppCommunicator: ' + receivedMessage);

            _handleReceivedMessage(receivedMessage);
        });
    };

    var _handleReceivedMessage = function(receivedMessage) {
        var contacts = getAllContacts();
        var contactsJson = JSON.stringify(contacts);

        return contactsJson;
    };
}