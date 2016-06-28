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

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * @author tinywind
 */
public class SinglePageExcel {
    protected SXSSFWorkbook workbook;
    protected Sheet sheet;
    protected int currentRow;

    public SinglePageExcel() {
        this.workbook = new SXSSFWorkbook();
        this.sheet = workbook.createSheet();
    }

    public Cell setCellValue(Row row, int column, String value) {
        if (value == null) {
            value = "";
        }
        Cell cell = row.createCell(column);
        cell.setCellValue(value);
        return cell;
    }

    public void generate(File file) {
        FileOutputStream os = null;
        try {
            os = new FileOutputStream(file);
            workbook.write(os);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (os != null) try {
                os.close();
            } catch (Exception ignored) {
            }
            try {
                workbook.close();
            } catch (IOException ignored) {
            }
            workbook.dispose();
        }
    }

    public Row createRow() {
        return sheet.createRow(currentRow++);
    }

    public Row addRow(String... columns) {
        return this.addRow(Arrays.asList(columns));
    }

    public Row addRow(List<String> columns) {
        Row row = this.createRow();
        int column = 0;
        for (String c : columns) {
            Cell cell = row.createCell(column++);
            cell.setCellValue(c);
        }
        return row;
    }

    public int getCurrentRow() {
        return currentRow;
    }

    public void setCurrentRow(int currentRow) {
        this.currentRow = currentRow;
    }
}
