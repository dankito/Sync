var networkUtil = new function () {

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
            log("Accepted connection on " + transport.host + ":" + transport.port);
            var input = transport.openInputStream(Ci.nsITransport.OPEN_BLOCKING, 0, 0);//.QueryInterface(Ci.nsIAsyncInputStream);
            var output = transport.openOutputStream(Ci.nsITransport.OPEN_BLOCKING, 0, 0);
            var scriptableInput = Cc["@mozilla.org/scriptableinputstream;1"].createInstance(Ci.nsIScriptableInputStream);

            try {
                scriptableInput.init(input);

                var receivedMessage = _readMessageFromSocket(scriptableInput);

                var messageReceivedCallback = _openServerSockets[serverSocket];
                if(messageReceivedCallback) {
                    var responseToSend = messageReceivedCallback(receivedMessage);

                    if(responseToSend) {
                        output.write(responseToSend, responseToSend.length);
                        output.flush();
                    }
                }
            }
            finally{
                scriptableInput.close();
                input.close();
                output.close();
            }
        }
    };

    var _readMessageFromSocket = function(scriptableInput) {
        var received = '';

        while (scriptableInput.available()) {
            received = received + scriptableInput.read(512);
        }

        log('Received: ' + received);

        return received;
    };


    /**
        Supports sending a message via UDP, but only to a single peer.
        Broadcast (e.g. to 192.168.0.255) is not supported!
    */
    this.sendMessageViaUdp = function(address, port, message) {
        var types = new Array();
        types.push('udp');

        var udpSocket = Components.classes["@mozilla.org/network/socket-transport-service;1"]
                                          .getService(Components.interfaces.nsISocketTransportService)
                                          .createTransport(types, types.length, address, port, null);

        var outputStream = udpSocket.openOutputStream(0, 0, 0);

        outputStream.write(message, message.length);

        outputStream.flush();
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