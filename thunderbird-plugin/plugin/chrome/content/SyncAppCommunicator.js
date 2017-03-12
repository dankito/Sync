var SyncAppCommunicator = new function () {

    var _deviceInfo;

    this.start = function() {
        _createDeviceInfo();

        Network.startTcpListenerSocket(SyncAppCommunicatorConfig.MessagesReceiverPort, function(receivedMessage) {
            // log('Received message in SyncAppCommunicator: ' + receivedMessage);

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
        else if(stringStartsWith(receivedMessage, SyncAppCommunicatorConfig.RequestStartSynchronizationMessage)) {
            return {
                'result' : 'ALLOWED',
                'synchronizationPort' : SyncAppCommunicatorConfig.DeviceDoesNotSupportActiveSynchronization
            };
        }

        else if(stringStartsWith(receivedMessage, SyncAppCommunicatorConfig.RequestPermitSynchronizationMessage)) {
            return _respondToRequestPermitToSynchronize(_getMessageBody(receivedMessage));
        }
        else if(stringStartsWith(receivedMessage, SyncAppCommunicatorConfig.ResponseToSynchronizationPermittingChallengeMessage)) {
            return _respondToResponseToSynchronizationPermittingChallenge(_getMessageBody(receivedMessage));
        }

        else if(stringStartsWith(receivedMessage, SyncAppCommunicatorConfig.GetAddressBookMessage)) {
            return AddressBook.getAllContacts();
        }
        else if(stringStartsWith(receivedMessage, SyncAppCommunicatorConfig.SyncContactMessage)) {
            return _syncContact(_getMessageBody(receivedMessage));
        }
    };


    var _respondToRequestPermitToSynchronize = function(deviceInfo) {
        try { logObject(deviceInfo); } catch(e) { }

        var deviceInfoString = deviceInfo.osName + ' ' + deviceInfo.osVersion;
        if(deviceInfo.name) {
            deviceInfoString = deviceInfo.name + ' ' + deviceInfoString;
        }

        var result = confirm('Allow device ' + deviceInfoString + ' to synchronize?'); // TODO: translate

        if(result) {
            return _createCorrectResponseAndReturnRespondToChallenge();
        }
        else {
            return {
                'result' : 'DENIED',
                'nonce' : null
            }
        }
    };

    var _nonceToCorrectResponseMap = { };
    var _nonceToCountRetriesLeftMap = { };

    function _createCorrectResponseAndReturnRespondToChallenge() {
        var nonce = generateUuid();
        var codeToEnter = '' + Math.floor((Math.random() * 1000000));

        var correctResponse = nonce + "-" + codeToEnter;
        correctResponse = createSha512HashAsBase64(correctResponse);

        _nonceToCorrectResponseMap[nonce] = correctResponse;
        _nonceToCountRetriesLeftMap[nonce] = 3;

        setTimeout(function () { // show alert after response is sent
            alert('Enter this code in Sync: ' + codeToEnter); // TODO: translate
        }, 50);

        return {
            'result': 'RESPOND_TO_CHALLENGE',
            'nonce': nonce
        }
    }


    var _respondToResponseToSynchronizationPermittingChallenge = function (requestBody) {
        var nonce = requestBody.nonce;
        var enteredChallengeResponse = requestBody.challengeResponse;

        var correctResponse = _nonceToCorrectResponseMap[nonce];

        if(correctResponse == enteredChallengeResponse) {
            return {
                'result' : 'ALLOWED',
                'synchronizationPort' : SyncAppCommunicatorConfig.DeviceDoesNotSupportActiveSynchronization
            }
        }
        else {
            var countRetriesLeft = _nonceToCountRetriesLeftMap[nonce];
            countRetriesLeft = countRetriesLeft - 1;

            if(countRetriesLeft > 0) {
                return {
                    'result' : 'WRONG_CODE',
                    'countRetriesLeft' : countRetriesLeft
                }
            }
            else {
                return {
                    'result' : 'DENIED'
                }
            }
        }
    };

    function _syncContact(receivedMessage) {
        return AddressBook.handleSynchronizedContact(receivedMessage.contact, receivedMessage.state);
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

    var _getMessageBody = function (receivedMessage) {
        var messageBodyStartIndex = receivedMessage.indexOf(SyncAppCommunicatorConfig.DevicesDiscoveryMessagePartsSeparator) + SyncAppCommunicatorConfig.DevicesDiscoveryMessagePartsSeparator.length;
        var messageBodyString = receivedMessage.substring(messageBodyStartIndex);

        return JSON.parse(messageBodyString);
    };

};