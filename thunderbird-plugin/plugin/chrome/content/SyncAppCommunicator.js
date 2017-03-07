var SyncAppCommunicator = new function () {

    var _deviceInfo;

    this.start = function() {
        _createDeviceInfo();

        networkUtil.startTcpListenerSocket(SyncAppCommunicatorConfig.MessagesReceiverPort, function(receivedMessage) {
            log('Received message in SyncAppCommunicator: ' + receivedMessage);

            var responseBody = _handleReceivedMessage(receivedMessage);
            return _createResponse(responseBody, receivedMessage);
        });

        var discoveryMessage = SyncAppCommunicatorConfig.DevicesDiscoveryMessagePrefix + SyncAppCommunicatorConfig.DevicesDiscoveryMessagePartsSeparator +
            thunderbirdInstanceUuid + SyncAppCommunicatorConfig.DevicesDiscoveryMessagePartsSeparator + SyncAppCommunicatorConfig.MessagesReceiverPort;

        setInterval(function() {
            networkUtil.sendMessageViaUdp('127.0.0.1', SyncAppCommunicatorConfig.DevicesDiscovererUdpPort, discoveryMessage);
        }, SyncAppCommunicatorConfig.SendDevicesDiscovererMessageIntervalMillis);
    };

    var _handleReceivedMessage = function(receivedMessage) {
        if(stringStartsWith(receivedMessage, SyncAppCommunicatorConfig.GetDeviceInfoMessage)) {
            return _deviceInfo;
        }
        else if(stringStartsWith(receivedMessage, SyncAppCommunicatorConfig.GetAddressBookMessage)) {
            return getAllContacts();
        }
    };

    var _createResponse = function(responseBody, receivedMessage) {
        var response;

        if(responseBody) {
            response = {
                'couldHandleMessage' : true,
                'body' : responseBody
            };
        }
        else {
            response = {
                'couldHandleMessage' : false,
                'errorType' : 'DETERMINE_RESPONSE',
                'error' : 'Could not determine response for message: ' + receivedMessage
            };
        }

        return objectToJson(response);
    };

    var _createDeviceInfo = function() {
        var deviceInfo = {
            id: thunderbirdInstanceUuid,
            uniqueDeviceId: thunderbirdInstanceUuid,
            name: 'Thunderbird',
            osType: 'THUNDERBIRD',
            osName: platform,
            osVersion: thunderbirdVersion,
            description: 'Sync Thunderbird Plugin'
        };

        _deviceInfo = deviceInfo;
    };
}