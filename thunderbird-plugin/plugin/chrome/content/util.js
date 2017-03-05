
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