if (i18n == null) var i18n = {};
if (i18n._ == null) i18n._ = {
    setLocale: function (locale) {
        this.locale = locale;
    },
    text: function (key) {
        var localeStringList = i18n[this.locale];
        if (localeStringList == null)
            localeStringList = i18n['default'];
        var str = localeStringList[key];
        if (str == null)
            str = i18n['default'][key];
        if (str == null) {
            console.error("The string is not founded: " + key);
            return "";
        }

        if (arguments.length <= 1)
            return str;

        var result = "";
        for (var argsExp = /[{][0-9]+[}]/g, matcher, last = 0; (matcher = argsExp.exec(str)) != null; last = matcher.index + matcher[0].length) {
            result += str.substr(last, matcher.index - last);
            var index = parseInt(matcher[0].substr(1, matcher[0].length - 2));
            result += (arguments[index + 1] == null ? matcher[0] : arguments[index + 1]);
        }
        if (last < str.length)
            result += str.substr(last, str.length - last);

        return result;
    }
};