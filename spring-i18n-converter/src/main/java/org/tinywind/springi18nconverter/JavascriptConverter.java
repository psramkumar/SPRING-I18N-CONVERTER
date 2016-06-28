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
package org.tinywind.springi18nconverter;

import org.apache.commons.lang.StringEscapeUtils;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.tinywind.springi18nconverter.Launcher.isContinuedLine;
import static org.tinywind.springi18nconverter.Launcher.preTrim;

/**
 * @author tinywind
 */
public class JavascriptConverter implements Convertible {
    public final static String JS_POSTFIX = ".js";

    @Override
    public void encode(String source, String languageName, File targetFile, String targetFileEncoding, Boolean describeByUnicode) throws IOException {
        final BufferedReader reader = new BufferedReader(new StringReader(source));
        final List<String> output = new ArrayList<>();

        output.add("if(i18n==null)var i18n={};");
        output.add("i18n['" + languageName + "']={};");

        String line;
        while ((line = reader.readLine()) != null) {
            final StringKeyValue keyValue = addOutput(line, reader, describeByUnicode);
            if (keyValue == null) continue;
            output.add("i18n['" + languageName + "']['" + keyValue.getKey() + "']='" + keyValue.getValue() + "';");
        }

        Files.write(Paths.get(targetFile.toURI()), output, Charset.forName(targetFileEncoding),
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
    }

    private StringKeyValue addOutput(String line, BufferedReader reader, Boolean describeByUnicode) throws IOException {
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
            lastValue += "\\n" + value;
        }

        return new StringKeyValue(key, describeByUnicode ? StringEscapeUtils.unescapeJava(lastValue) : lastValue);
    }

    @Override
    public String encodingFileName(String languageName) {
        return languageName + ".js";
    }

    @Override
    @SuppressWarnings("unchecked")
    public void decode(File sourceFile, String targetDir, String targetEncoding, Boolean describeByUnicode) {
        final String sourceFileName = sourceFile.getName().toLowerCase();
        if (sourceFileName.lastIndexOf(".js") != sourceFileName.length() - ".js".length())
            return;

        final ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
        try {
            engine.eval(new FileReader(sourceFile));
            final Map<String, Map<String, String>> i18n = (Map<String, Map<String, String>>) engine.eval("i18n");

            for (String language : i18n.keySet()) {
                final List<String> output = new ArrayList<>();
                i18n.get(language).forEach((key, value) -> {
                    String inserted = key + "=";
                    int last = 0;
                    for (Matcher matcher = Pattern.compile("([\\\\]*\\n)").matcher(value); matcher.find(); inserted = "") {
                        final int end = matcher.end();
                        final String subValue = value.substring(last, end - 1);
                        inserted += (describeByUnicode ? StringEscapeUtils.escapeJava(subValue) : subValue) + "\\";
                        output.add(inserted);
                        last = end;
                    }

                    if (last < value.length()) {
                        value = value.substring(last, value.length());
                        output.add(inserted + (describeByUnicode ? StringEscapeUtils.escapeJava(value) : value));
                    }
                });
                Files.write(Paths.get(new File(targetDir, "messages" + (language.equals("default") ? "" : "_" + language) + ".properties").toURI()),
                        output, Charset.forName(targetEncoding),
                        StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
            }
        } catch (ScriptException | IOException e) {
            System.err.println(" FAIL to convert: " + sourceFile.getAbsolutePath());
            e.printStackTrace();
        }
    }

    private class StringKeyValue {
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
