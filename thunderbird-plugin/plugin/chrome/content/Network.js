var Network = new function () {

    var _openServerSockets = { };

    /*
     * Opens a Server Socket and listens for incoming connections an given port.
     */
    this.startTcpListenerSocket = function(listenerPort, messageReceivedCallback) {
        var serverSocket = Cc["@mozilla.org/network/server-socket;1"].createInstance(Ci.nsIServerSocket);
        serverSocket.init(listenerPort, false, 5);

        _openServerSockets[serverSocket] = messageReceivedCallback;

        serverSocket.asyncListen(_tcpServerSocketListener);
    };

    /*
     * Listen for connections. Transmit
     */
    var _tcpServerSocketListener = {
        onSocketAccepted: function(serverSocket, transport) {
            var input = transport.openInputStream(Ci.nsITransport.OPEN_BLOCKING, 0, 0);//.QueryInterface(Ci.nsIAsyncInputStream);
            var output = transport.openOutputStream(Ci.nsITransport.OPEN_BLOCKING, 0, 0);

            // prefer nsIConverterInputStream over scriptableinputstream: https://developer.mozilla.org/en-US/docs/Reading_textual_data
            var charset = "UTF-8";
            var bufferSize = 16 * 1024;
            const replacementChar = Components.interfaces.nsIConverterInputStream.DEFAULT_REPLACEMENT_CHARACTER;
            var inputStream = Components.classes["@mozilla.org/intl/converter-input-stream;1"]
                               .createInstance(Components.interfaces.nsIConverterInputStream);

            try {
                inputStream.init(input, charset, bufferSize, replacementChar);

                var receivedMessage = _readMessageFromSocket(inputStream);

                var outputStream = Components.classes["@mozilla.org/intl/converter-output-stream;1"]
                                            .createInstance(Components.interfaces.nsIConverterOutputStream);
                outputStream.init(output, charset, bufferSize, replacementChar);

                _dispatchReceivedMessageAndSendResponse(receivedMessage, serverSocket, outputStream);
            }
            finally {
                inputStream.close();
                input.close();
                output.close();
            }
        }
    };

    var _readMessageFromSocket = function(inputStream) {
        var received = '';

        var str = {};
        var bufferSize = 512;
        var read = 512;

        while ((read = inputStream.readString(bufferSize, str)) > 0) {
          received = received + str.value;

          if(read < bufferSize) {
            break;
          }
        }

        return received;
    };

    var _dispatchReceivedMessageAndSendResponse = function(receivedMessage, serverSocket, outputStream) {
        var messageReceivedCallback = _openServerSockets[serverSocket];

        if(messageReceivedCallback) {
            var responseToSend = messageReceivedCallback(receivedMessage);

            if(responseToSend) {
                outputStream.writeString(responseToSend);
                outputStream.flush();
            }
        }
    };


    /**
        Supports sending a message via UDP, but only to a single peer.
        Broadcast (e.g. to 192.168.0.255) is not supported!
    */
    this.sendMessageViaUdp = function(address, port, message) {
        try {
            var types = new Array();
            types.push('udp');

            var udpSocket = Components.classes["@mozilla.org/network/socket-transport-service;1"]
                                              .getService(Components.interfaces.nsISocketTransportService)
                                              .createTransport(types, types.length, address, port, null);

            var outputStream = udpSocket.openOutputStream(0, 0, 0);

            outputStream.write(message, message.length);

            outputStream.flush();

            outputStream.close();
        } catch(e) {
            log('Could not send message \'' + message + '\' over UDP to ' + address + ':' + port + ' - ' + e);
        }
    };


    this.sendMessageViaTcp = function(address, port, message) {
        var socket = Components.classes["@mozilla.org/network/socket-transport-service;1"]
                                  .getService(Components.interfaces.nsISocketTransportService)
                                  .createTransport(null, 0, address, port, null);

        var poolOutputStream = socket.openOutputStream(0, 0, 0);

        poolOutputStream.write(message, message.length);

        var poolRawInputStream = socket.openInputStream(0, 0, 0);
        var poolInputStream = Components.classes ["@mozilla.org/scriptableinputstream;1"]
                                .createInstance(Components.interfaces.nsIScriptableInputStream)
                                .init(poolRawInputStream);

        Components.utils.import("resource://gre/modules/NetUtil.jsm");

        var pump = Components.classes["@mozilla.org/network/input-stream-pump;1"]
                             .createInstance(Components.interfaces.nsIInputStreamPump);
        pump.init(poolRawInputStream, -1, -1, 0, 0, true);

        var listener = {
          onStartRequest: function(request, context) {},
          onDataAvailable: function(request, context, stream, offset, count)
          {
            var data = NetUtil.readInputStreamToString(stream, count);
            log('Received data in onDataAvailable():');
            log(data);
            // TODO: call listener to return received message
          },
          onStopRequest: function(request, context, result)
          {
            if (!Components.isSuccessCode(result)) {
              // TODO: Handle error here
            }
          }
        };

        pump.asyncRead(listener, null);
    };
}