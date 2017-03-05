

window.addEventListener("load", function(e) {
	syncAddressBook();
}, false);


log('Started Sync Thunderbird Plugin');


DevicesDiscovererListener.start();