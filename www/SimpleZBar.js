
function SimpleZBar () {};

SimpleZBar.prototype = {
    scanFromFile: function (params, success, failure)
    {
        cordova.exec(success, failure, "SimpleZBar", "scanFromFile", [params]);
    },
    scanFromDataUrl: function (params, success, failure)
    {
        cordova.exec(success, failure, "SimpleZBar", "scanFromDataUrl", [params]);
    },

};
module.exports = new SimpleZBar;
