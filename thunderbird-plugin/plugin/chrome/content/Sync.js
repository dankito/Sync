
var thunderbirdVersion = null;
var platform = null;
var thunderbirdInstanceUuid;


window.addEventListener("load", function(e) {
    retrieveApplicationInfo();

    AddressBookListener.init();

    SyncAppCommunicator.start();
}, false);


function retrieveApplicationInfo() {
    var appInfo = Components.classes["@mozilla.org/xre/app-info;1"]
                        .getService(Components.interfaces.nsIXULAppInfo);
    thunderbirdVersion = appInfo.version;

    try { platform = window.navigator.platform; } catch(ex) { }

    getThunderbirdInstanceUuid();
}

function getThunderbirdInstanceUuid() {
    const instanceUuidPrefKey = 'extensions.sync.thunderbird.instance.uuid';
    const prefs = Cc["@mozilla.org/preferences-service;1"].getService(Ci.nsIPrefBranch);

    if(prefs.prefHasUserValue(instanceUuidPrefKey)) {
        thunderbirdInstanceUuid = prefs.getCharPref(instanceUuidPrefKey);
    }
    else {
        thunderbirdInstanceUuid = generateUuid();
        log('Created new UUID for this Thunderbird instance');
        prefs.setCharPref(instanceUuidPrefKey, thunderbirdInstanceUuid);
    }
}