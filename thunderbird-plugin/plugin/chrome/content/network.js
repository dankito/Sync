var DevicesDiscovererListener = new function () {

    /*
     * Listen for connections. Transmit
     */
    var listener = {
        onSocketAccepted: function(serverSocket, transport) {
            log("Accepted connection on " + transport.host + ":" + transport.port);
            var input = transport.openInputStream(Ci.nsITransport.OPEN_BLOCKING, 0, 0);//.QueryInterface(Ci.nsIAsyncInputStream);
            var output = transport.openOutputStream(Ci.nsITransport.OPEN_BLOCKING, 0, 0);
            var sin = Cc["@mozilla.org/scriptableinputstream;1"].createInstance(Ci.nsIScriptableInputStream);

            try{
                sin.init(input);
                var readBytes = sin.available();

                var request = '';
                request = sin.read(readBytes);
                log('Received: ' + request);

                getUrl(request);

                output.write("yes", "yes".length);
                output.flush();
            }
            finally{
                sin.close();
                input.close();
                output.close();
            }
        }
    }

    /*
     * Main
     */
    this.start = function() {
        var serverSocket = Cc["@mozilla.org/network/server-socket;1"].createInstance(Ci.nsIServerSocket);
        serverSocket.init(8888, false, 5);
        log("Opened socket on " + serverSocket.port);
        serverSocket.asyncListen(listener);
    };
}