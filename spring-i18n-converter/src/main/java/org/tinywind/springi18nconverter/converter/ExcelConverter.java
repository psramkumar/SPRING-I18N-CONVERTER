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

import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.OfficeXmlFileException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

/**
 * @author tinywind
 */
public class ExcelConverter extends AbstractConverter {
    public final static String JS_POSTFIX = ".xlsx";
    public final static String[] JS_POSTFIX_ARRAY = new String[]{".xls", JS_POSTFIX};
    private final static int COLUMN_KEY = 0;
    private final static int COLUMN_LANG = 1;
    private final static int COLUMN_VALUE = 2;

    @Override
    public void encode(String source, String languageName, File targetFile, String targetFileEncoding, Boolean describeByNative) throws IOException {
        final BufferedReader reader = new BufferedReader(new StringReader(source));
        final SinglePageExcel excel = new SinglePageExcel();

        String line;
        while ((line = reader.readLine()) != null) {
            final StringKeyValue keyValue = addOutput(line, reader, describeByNative, true);
            if (keyValue == null) continue;
            final Row row = excel.createRow();
            excel.setCellValue(row, COLUMN_KEY, keyValue.getKey());
            excel.setCellValue(row, COLUMN_LANG, languageName);
            excel.setCellValue(row, COLUMN_VALUE, keyValue.getValue());
        }

        excel.generate(targetFile);
    }

    @Override
    public String encodingFileName(String languageName) {
        return languageName + JS_POSTFIX;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void decode(File sourceFile, String targetDir, String targetEncoding, Boolean describeByNative) {
        final String sourceFileName = sourceFile.getName().toLowerCase();
        if (Arrays.stream(JS_POSTFIX_ARRAY)
                .filter(postfix -> sourceFileName.lastIndexOf(postfix) == sourceFileName.length() - postfix.length())
                .count() == 0)
            return;

        try {
            final Map<String, List<String>> stringListMap = new HashMap<>();
            final FileInputStream file = new FileInputStream(sourceFile);
            Iterator<Row> rowIterator;
            try {
                final XSSFWorkbook workbook = new XSSFWorkbook(file);
                final XSSFSheet sheet = workbook.getSheetAt(0);
                rowIterator = sheet.iterator();
            } catch (OfficeXmlFileException e) {
                System.err.println(" exception:" + e.getMessage());
                final HSSFWorkbook workbook = new HSSFWorkbook(file);
                final HSSFSheet sheet = workbook.getSheetAt(0);
                rowIterator = sheet.iterator();
            }

            while (rowIterator.hasNext()) {
                final Row row = rowIterator.next();
                final String key = row.getCell(COLUMN_KEY).getStringCellValue();
                final String language = row.getCell(COLUMN_LANG).getStringCellValue();
                final String value = row.getCell(COLUMN_VALUE).getStringCellValue().trim();
                if (StringUtils.isEmpty(key) || StringUtils.isEmpty(language) || StringUtils.isEmpty(value)) continue;

                List<String> stringList = stringListMap.get(language);
                if (stringList == null) {
                    stringList = new ArrayList<>();
                    stringListMap.put(language, stringList);
                }

                final String newLine = "\\\n";
                String lastValue = "", token;
                final BufferedReader reader = new BufferedReader(new StringReader(value));
                while ((token = reader.readLine()) != null) lastValue += token + newLine;
                reader.close();
                if (lastValue.lastIndexOf(newLine) == lastValue.length() - newLine.length())
                    lastValue = lastValue.substring(0, lastValue.length() - newLine.length());

                addProperty(stringList, key, lastValue, describeByNative);
            }

            for (String language : stringListMap.keySet()) {
                Files.write(Paths.get(new File(targetDir, messagesPropertiesFileName(language)).toURI()),
                        stringListMap.get(language), Charset.forName(targetEncoding),
                        StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
            }
        } catch (Exception e) {
            System.err.println(" FAIL to convert: " + sourceFile.getAbsolutePath());
            e.printStackTrace();
        }
    }
}
