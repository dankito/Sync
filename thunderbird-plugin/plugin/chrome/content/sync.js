
var appInfo = null;

window.addEventListener("load", function(e) {
    appInfo = Components.classes["@mozilla.org/xre/app-info;1"]
                        .getService(Components.interfaces.nsIXULAppInfo);


    DevicesDiscovererListener.start();
}, false);


log('Started Sync Thunderbird Plugin');