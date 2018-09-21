package com.yfs.util.office;

import com.google.common.collect.Lists;
import com.yfs.constant.Charsets;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.Validate;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Csv读取器
 * @author John Li
 */
@Slf4j
public class CsvReader implements Iterable<List<String>> {
    private FileInputStream csvInputStream;
    private File csvFile;
    private ExcelReaderConfig config;
    /** 工作薄的总行数 */
    private int rowCount;
    private List<CSVRecord> csvRecords;
    public static final Charset DEFAULT_CHARSET = Charsets.GBK;

    public CsvReader(File file, Charset charset, ExcelReaderConfig config) {
        Validate.notNull(config);
        Validate.isTrue(file.exists(), "file not exists");
        this.config = config;
        this.csvFile = file;
        try {
            csvInputStream = new FileInputStream(csvFile);
            CSVParser csvParser = CSVParser.parse(csvFile, charset, CSVFormat.DEFAULT);
            csvRecords = csvParser.getRecords();
            rowCount = (int) csvParser.getRecordNumber();
        } catch (FileNotFoundException e) {
            log.error("CsvReader", e);
        } catch (IOException e) {
            log.error("CsvReader", e);
        }
    }

    public List<String> getRow(int rowNo) {
        return recordToStringList(csvRecords.get(rowNo));
    }

    @Override
    public Iterator<List<String>> iterator() {
        return new Itr();
    }

    public void close() throws IOException {
        csvInputStream.close();
    }

    public int getRowCount() {
        return rowCount;
    }

    public File getFile() {
        return csvFile;
    }

    private List<String> recordToStringList(CSVRecord record) {
        List<String> list = Lists.newArrayList();
        for (String cellContent : record) {
            if (config.isReplaceCellLineBreak()) {
                cellContent = cellContent.replaceAll("\\n|\\r", "");
            }
            if (config.isTrimCellContent()) {
                cellContent = cellContent.trim();
            }
            list.add(cellContent);
        }
        return list;
    }

    private class Itr implements Iterator<List<String>> {
        /** 工作薄的当前行数 */
        private int currentNum;

        @Override
        public boolean hasNext() {
            return currentNum != rowCount;
        }

        @Override
        public List<String> next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            return getRow(currentNum++);
        }

        @Override
        public void remove() {
            throw new RuntimeException("not execute remove");
        }

    }
}
