var SyncAppCommunicator = new function () {

    var _deviceInfo;

    this.start = function() {
        var messagesPort = 32797;

        _createDeviceInfo();

        networkUtil.startTcpListenerSocket(messagesPort, function(receivedMessage) {
            log('Received message in SyncAppCommunicator: ' + receivedMessage);

            return _handleReceivedMessage(receivedMessage);
        });
    };

    var _handleReceivedMessage = function(receivedMessage) {
        if('IS_THUNDERBIRD_OUT_THERE' === receivedMessage) {
            return _deviceInfo;
        }
        else if('GET_ADDRESS_BOOK' === receivedMessage) {
            var contacts = getAllContacts();
            var contactsJson = JSON.stringify(contacts);

            return contactsJson;
        }
    };

    var _createDeviceInfo = function() {
        var deviceInfo = {
            uniqueDeviceId: 'TODO',
            name: 'Thunderbird',
            osType: 'THUNDERBIRD',
            osName: platform,
            osVersion: thunderbirdVersion,
            description: 'Sync Thunderbird Plugin'
        };

        _deviceInfo = JSON.stringify(deviceInfo);
    };
}