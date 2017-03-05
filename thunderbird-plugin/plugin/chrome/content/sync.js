
var thunderbirdVersion = null;
var platform = null;
var thunderbirdInstanceUuid;


log('Started Sync Thunderbird Plugin');


window.addEventListener("load", function(e) {
    retrieveApplicationInfo();

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
        log('Read uuid from preferences: ' + thunderbirdInstanceUuid);
    }
    else {
        thunderbirdInstanceUuid = generateUuid();
        log('Created new UUID: ' + thunderbirdInstanceUuid);
        prefs.setCharPref(instanceUuidPrefKey, thunderbirdInstanceUuid);
        log('Saved new uuid to preferences');
    }
}