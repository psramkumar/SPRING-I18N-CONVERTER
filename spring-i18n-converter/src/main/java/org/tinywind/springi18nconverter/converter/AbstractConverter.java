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
package org.tinywind.springi18nconverter.converter;

import org.apache.commons.lang.StringEscapeUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author tinywind
 */
public abstract class AbstractConverter {
    public static String preTrim(String str) {
        final Matcher matcher = Pattern.compile("^(\\s+)").matcher(str);
        if (!matcher.find()) return str;
        return str.substring(matcher.end(), str.length());
    }

    public static boolean isContinuedLine(String line) {
        final Matcher matcher = Pattern.compile("(\\\\+)$").matcher(line);
        return matcher.find() && matcher.group().length() % 2 == 1;
    }

    protected static String messagesPropertiesFileName(String language) {
        return "messages" + (language.equals("default") ? "" : "_" + language) + ".properties";
    }

    public abstract void encode(String source, String languageName, File targetFile, String targetFileEncoding, Boolean describeByNative) throws IOException;

    public abstract String encodingFileName(String languageName);

    public abstract void decode(File sourceFile, String targetDir, String targetEncoding, Boolean describeByNative);

    protected void addProperty(List<String> stringList, String key, String value, Boolean describeByNative) {
        String inserted = key + "=";
        int last = 0;
        for (Matcher matcher = Pattern.compile(describeByNative ? "([\\\\]*\\n)" : "([\\\\]*\\\\n)").matcher(value); matcher.find(); inserted = "") {
            if (!describeByNative && matcher.group().length() % 2 == 1)
                continue;

            final int end = matcher.end();
            final String subValue = value.substring(last, end - (describeByNative ? 1 : 2));
            inserted += (describeByNative ? StringEscapeUtils.escapeJava(subValue) : subValue) + "\\";
            stringList.add(inserted);
            last = end;
        }

        if (last < value.length()) {
            final String lastValue = value.substring(last, value.length());
            stringList.add(inserted + (describeByNative ? StringEscapeUtils.escapeJava(lastValue) : lastValue));
        }
    }

    protected StringKeyValue addOutput(String line, BufferedReader reader, Boolean describeByNative, Boolean newLine) throws IOException {
        line = preTrim(line);
        if (line.length() == 0 || line.charAt(0) == '#')
            return null;

        final int indexEqual = line.indexOf('=');
        final int indexColon = line.indexOf(':');
        final int indexSplit = indexEqual < indexColon ? (indexEqual < 0 ? indexColon : indexEqual) : (indexColon < 0 ? indexEqual : indexColon);
        if (indexSplit < 0) {
            System.err.println("Invalid line: " + line);
            return null;
        }
        final String key = line.substring(0, indexSplit).trim().replaceAll("[']", "\\'");
        String value = line.substring(indexSplit + 1, line.length()).trim().replaceAll("[']", "\\'");
        String lastValue = value;

        while (isContinuedLine(value)) {
            if ((value = reader.readLine()) == null) break;
            lastValue = (newLine ? lastValue.substring(0, lastValue.length() - 1) + "\n" : lastValue + "\\n") + value;
        }

        return new StringKeyValue(key, describeByNative ? StringEscapeUtils.unescapeJava(lastValue) : lastValue);
    }

    protected class StringKeyValue {
        private String key;
        private String value;

        StringKeyValue(String key, String value) {
            this.key = key;
            this.value = value;
        }

        String getKey() {
            return key;
        }

        String getValue() {
            return value;
        }
    }
}
