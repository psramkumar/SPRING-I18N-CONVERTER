/**
 * The MIT License (MIT)
 * <p>
 * Copyright (c) 2016 Jeon JaeHyeong
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
 
/**
 * @author tinywind
 */
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
