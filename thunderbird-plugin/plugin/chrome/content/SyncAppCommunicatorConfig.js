var SyncAppCommunicatorConfig = new function() {

    this.DevicesDiscovererUdpPort = 32788;

    this.SendDevicesDiscovererMessageIntervalMillis = 500;

    this.MessagesReceiverPort = 32797;

    this.DevicesDiscoveryMessagePrefix = 'Sync';

    this.DevicesDiscoveryMessagePartsSeparator = ':';

    this.DeviceDoesNotSupportActiveSynchronization = -2;


    this.GetDeviceInfoMessage = 'GetDeviceInfo';

    this.RequestStartSynchronizationMessage = "RequestStartSynchronization";


    this.RequestPermitSynchronizationMessage = "RequestPermitSynchronization";

    this.ResponseToSynchronizationPermittingChallengeMessage = "ResponseToSynchronizationPermittingChallenge";
    

    this.GetAddressBookMessage = 'GetAddressBook';

    this.SyncContactMessage = 'SyncContact';

}