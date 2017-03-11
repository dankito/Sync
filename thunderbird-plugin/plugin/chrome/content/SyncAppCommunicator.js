var SyncAppCommunicator = new function () {

    var _deviceInfo;

    this.start = function() {
        _createDeviceInfo();

        Network.startTcpListenerSocket(SyncAppCommunicatorConfig.MessagesReceiverPort, function(receivedMessage) {
            log('Received message in SyncAppCommunicator: ' + receivedMessage);

            try {
                var responseBody = _handleReceivedMessage(receivedMessage);
                return _createResponse(responseBody, receivedMessage);
            } catch(e) {
                log('Could not handle received message: ' + e);
                return _createResponse(null, e);
            }
        });

        var discoveryMessage = SyncAppCommunicatorConfig.DevicesDiscoveryMessagePrefix + SyncAppCommunicatorConfig.DevicesDiscoveryMessagePartsSeparator +
            thunderbirdInstanceUuid + SyncAppCommunicatorConfig.DevicesDiscoveryMessagePartsSeparator + SyncAppCommunicatorConfig.MessagesReceiverPort;

        setInterval(function() {
            Network.sendMessageViaUdp('127.0.0.1', SyncAppCommunicatorConfig.DevicesDiscovererUdpPort, discoveryMessage);
        }, SyncAppCommunicatorConfig.SendDevicesDiscovererMessageIntervalMillis);
    };

    var _handleReceivedMessage = function(receivedMessage) {
        if(stringStartsWith(receivedMessage, SyncAppCommunicatorConfig.GetDeviceInfoMessage)) {
            return _deviceInfo;
        }
        else if(stringStartsWith(receivedMessage, SyncAppCommunicatorConfig.GetAddressBookMessage)) {
            return AddressBook.getAllContacts();
        }
        else if(stringStartsWith(receivedMessage, SyncAppCommunicatorConfig.RequestStartSynchronizationMessage)) {
            return {
                'result' : 'ALLOWED',
                'synchronizationPort' : SyncAppCommunicatorConfig.DeviceDoesNotSupportActiveSynchronization
            };
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