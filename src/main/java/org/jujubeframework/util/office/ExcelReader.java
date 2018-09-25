package org.jujubeframework.util.office;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Excel读取器
 * 
 * @author John Li Email：jujubeframework@163.com
 */
public class ExcelReader implements Iterable<List<String>> {
    /** 一个Sheet工作薄 */
    private Sheet sheet;
    /** 工作薄的总行数 */
    private int rowCount;
    private ExcelReaderConfig config;
    private File currentExcel;
    private FileInputStream excelInputStream;
    private FormulaEvaluator evaluator;

    private Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 构造函数
     * 
     * @param file
     *            Excel文件
     * @param sheetIndex
     *            要解析的sheetIndex,从0开始
     */
    public ExcelReader(File file, int sheetIndex, ExcelReaderConfig config) {
        Validate.notNull(config);
        this.config = config;
        init(file, sheetIndex);
    }

    /**
     * 初始化
     */
    private void init(File file, int sheetIndex) {
        logger.debug("解析文件：" + file.getAbsolutePath() + " 开始...");
        Validate.isTrue(file.exists(), "file not exists:" + file.getAbsolutePath());
        try {
            currentExcel = file;
            excelInputStream = new FileInputStream(currentExcel);
            Workbook workbook = WorkbookFactory.create(excelInputStream);
            if (workbook instanceof HSSFWorkbook) {
                evaluator = new HSSFFormulaEvaluator((HSSFWorkbook) workbook);
            } else if (workbook instanceof XSSFWorkbook) {
                evaluator = new XSSFFormulaEvaluator((XSSFWorkbook) workbook);
            } else if (workbook instanceof SXSSFWorkbook) {
                evaluator = new XSSFFormulaEvaluator(((SXSSFWorkbook) workbook).getXSSFWorkbook());
            }
            sheet = workbook.getSheetAt(sheetIndex);
            rowCount = config.isBlankLineTerminated() ? realRows() : sheet.getLastRowNum() + 1;
        } catch (InvalidFormatException e) {
            logger.error("ExcelReader.init", e);
        } catch (IOException e) {
            logger.error("ExcelReader.init", e);
        }
        logger.debug("解析文件：" + file.getAbsolutePath() + " 结束！");
    }

    /**
     * 得到真实总行数<br>
     * 有时Excel中删除了内容，但没有删除格式，会留下空白行。
     */
    public int realRows() {
        int con = sheet.getLastRowNum() + 1;
        for (int i = 0; i < con; i++) {
            // 遇到第一个空行，则终止
            if (sheet.getRow(i) == null) {
                con = i;
                break;
            }
            List<String> rows = getRow(i);
            if (rows.isEmpty() || isBlankRow(rows)) {
                con = i;
                break;
            }
        }
        return con;
    }

    /**
     * 获取总行数
     */
    public int getRowCount() {
        return this.rowCount;
    }

    @Override
    public Iterator<List<String>> iterator() {
        return new Itr();
    }

    /**
     * 获得第一行数据
     */
    public List<String> first() {
        return getRow(0);
    }

    /**
     * 获得最后一行数据
     */
    public List<String> last() {
        return getRow(getRowCount() - 1);
    }

    private boolean isBlankRow(List<String> rows) {
        boolean result = true;
        for (String cell : rows) {
            if (StringUtils.isNotBlank(cell)) {
                result = false;
                break;
            }
        }
        return result;
    }

    /**
     * 获得相应行数据
     */
    public List<String> getRow(int rowNo) {
        List<String> list = new ArrayList<String>();
        Row row = sheet.getRow(rowNo);
        if (row != null) {
            for (int i = 0; i < row.getLastCellNum(); i++) {
                Cell cell = row.getCell(i);
                String cellContent = getCellContent(cell);
                if (config.isReplaceCellLineBreak()) {
                    cellContent = cellContent.replaceAll("\\n|\\r", "");
                }
                if (config.isTrimCellContent()) {
                    cellContent = cellContent.trim();
                }
                list.add(cellContent);
            }
        }
        return list;
    }

    /** 获得单元格内容 */
    private String getCellContent(Cell cell) {
        String cellContent = "";
        if (cell != null) {
            // 如果有表格中单元格有公式，cell.toString()得不到正确结果。这里需要做下处理。需要注意：公式计算出来的数字大多为浮点型，需要客户端去精确
            if (cell.getCellTypeEnum()== CellType.FORMULA) {
                try {
                    CellValue cellValue = evaluator.evaluate(cell);
                    cellContent = cellValue.formatAsString();
                } catch (Exception e) {
                    cellContent = cell.toString();
                }
            } else {
                cellContent = cell.toString();
            }
        }
        return cellContent;
    }

    /**
     * 关闭excel文件资源
     */
    public void close() throws IOException {
        if (excelInputStream != null) {
            excelInputStream.close();
        }
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

    public File getFile() {
        return currentExcel;
    }

}
