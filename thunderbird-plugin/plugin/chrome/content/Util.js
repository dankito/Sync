
function log(message) {
    Application.console.log(message);
}

function logObject(object) {
    try {
        log(objectToJson(object));
    } catch(e) { log('Could not log object: ' + e); }
}

function objectToJson(object) {
    return JSON.stringify(object);
}

function stringStartsWith(string, startString) {
    return string.lastIndexOf(startString, 0) === 0;
}

// copied from https://stackoverflow.com/a/2117523
function generateUuid() {
    return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
        var r = Math.random()*16|0, v = c == 'x' ? r : (r&0x3|0x8);
        return v.toString(16);
    });
}

function createSha512HashAsBase64(stringToHash) { // TODO make generic (Hash function and if it should be returned as binary data or Base64)
    var converter =
        Components.classes["@mozilla.org/intl/scriptableunicodeconverter"].
        createInstance(Components.interfaces.nsIScriptableUnicodeConverter);

// we use UTF-8 here, you can choose other encodings.
    converter.charset = "UTF-8";
// result is an out parameter,
// result.value will contain the array length
    var result = {};
// data is an array of bytes
    var data = converter.convertToByteArray(stringToHash, result);

    var cryptoHash = Components.classes["@mozilla.org/security/hash;1"]
        .createInstance(Components.interfaces.nsICryptoHash);
    cryptoHash.init(cryptoHash.SHA512);
    cryptoHash.update(data, data.length);

    var base64Hash = cryptoHash.finish(true); // false = binary, true = Base64
    return base64Hash.replace(/(?:\r\n|\r|\n)/g, ''); // replace new line chars
}