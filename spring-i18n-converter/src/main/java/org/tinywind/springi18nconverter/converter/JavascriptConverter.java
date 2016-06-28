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

/**
 * @author tinywind
 */
public class JavascriptConverter extends AbstractConverter {
    public final static String JS_POSTFIX = ".js";

    @Override
    public void encode(String source, String languageName, File targetFile, String targetFileEncoding, Boolean describeByNative) throws IOException {
        final BufferedReader reader = new BufferedReader(new StringReader(source));
        final List<String> output = new ArrayList<>();

        output.add("if(i18n==null)var i18n={};");
        output.add("i18n['" + languageName + "']={};");

        String line;
        while ((line = reader.readLine()) != null) {
            final StringKeyValue keyValue = addOutput(line, reader, describeByNative, false);
            if (keyValue == null) continue;
            output.add("i18n['" + languageName + "']['" + keyValue.getKey() + "']='" + keyValue.getValue() + "';");
        }

        Files.write(Paths.get(targetFile.toURI()), output, Charset.forName(targetFileEncoding),
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
    }

    @Override
    public String encodingFileName(String languageName) {
        return languageName + JS_POSTFIX;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void decode(File sourceFile, String targetDir, String targetEncoding, Boolean describeByNative) {
        final String sourceFileName = sourceFile.getName().toLowerCase();
        if (sourceFileName.lastIndexOf(JS_POSTFIX) != sourceFileName.length() - JS_POSTFIX.length())
            return;

        final ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
        try {
            engine.eval(new FileReader(sourceFile));
            final Map<String, Map<String, String>> i18n = (Map<String, Map<String, String>>) engine.eval("i18n");

            for (String language : i18n.keySet()) {
                final List<String> output = new ArrayList<>();
                i18n.get(language).forEach((key, value) -> {
                    addProperty(output, key, value, describeByNative);
                });
                Files.write(Paths.get(new File(targetDir, messagesPropertiesFileName(language)).toURI()),
                        output, Charset.forName(targetEncoding),
                        StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
            }
        } catch (ScriptException | IOException e) {
            System.err.println(" FAIL to convert: " + sourceFile.getAbsolutePath());
            e.printStackTrace();
        }
    }
}
