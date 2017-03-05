
var thunderbirdVersion = null;
var platform = null;


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
}