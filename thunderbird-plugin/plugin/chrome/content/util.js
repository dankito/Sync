
function log(message) {
    Application.console.log(message);
}

function logObject(object) {
    log(objectToJson(object));
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