var SyncAppCommunicatorConfig = new function() {

    this.DevicesDiscovererUdpPort = 32788;

    this.SendDevicesDiscovererMessageIntervalMillis = 500;

    this.MessagesReceiverPort = 32797;

    this.DevicesDiscoveryMessagePrefix = 'Sync';

    this.DevicesDiscoveryMessagePartsSeparator = ':';

}