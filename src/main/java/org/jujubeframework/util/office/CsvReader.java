package org.jujubeframework.util.office;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.Validate;
import org.jujubeframework.constant.Charsets;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Csv读取器
 *
 * @author John Li
 */
@Slf4j
public class CsvReader implements Iterable<List<String>> {
    private FileInputStream csvInputStream;
    private final File csvFile;
    private final ExcelReaderConfig config;
    /**
     * 工作薄的总行数
     */
    private int rowCount;
    private List<CSVRecord> csvRecords;
    public static final Charset DEFAULT_CHARSET = Charsets.GBK;

    /**
     * 构造
     *
     * @param file
     *            csv文件
     * @param charset
     *            文件编码
     * @param config
     *            文件读取的一些配置规则
     */
    public CsvReader(File file, Charset charset, ExcelReaderConfig config) {
        this(file, charset, config, CSVFormat.DEFAULT);
    }

    /**
     * 构造
     *
     * @param file
     *            csv文件
     * @param charset
     *            文件编码
     * @param config
     *            文件读取的一些配置规则
     */
    public CsvReader(File file, Charset charset, ExcelReaderConfig config, CSVFormat csvFormat) {
        Validate.notNull(config);
        Validate.isTrue(file.exists(), "file not exists");
        this.config = config;
        this.csvFile = file;
        try {
            csvInputStream = new FileInputStream(csvFile);
            CSVParser csvParser = CSVParser.parse(csvFile, charset, csvFormat);
            csvRecords = csvParser.getRecords();
            rowCount = (int) csvParser.getRecordNumber();
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
            if (config.isTrimCellContent()) {
                cellContent = cellContent.trim();
            }
            list.add(cellContent);
        }
        return list;
    }

    public List<List<String>> getRows() {
        List<List<String>> data = new ArrayList<>();
        for (int i = 0; i < getRowCount(); i++) {
            data.add(getRow(i));
        }
        return data;
    }

    private class Itr implements Iterator<List<String>> {
        /**
         * 工作薄的当前行数
         */
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
