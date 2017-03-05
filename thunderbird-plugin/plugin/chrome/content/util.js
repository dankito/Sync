
function log(message) {
    Application.console.log(message);
}

function logObject(object) {
    log(objectToJson(object));
}

function objectToJson(object) {
    return JSON.stringify(object);
}