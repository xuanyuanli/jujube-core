package org.jujubeframework.util.office;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.util.LocaleUtil;
import org.jujubeframework.util.Beans;
import org.jujubeframework.util.Collections3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.*;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Excel读取器
 *
 * @author John Li Email：jujubeframework@163.com
 */
public class ExcelReader implements Iterable<List<String>> {

    public static final String DATA_FORMAT_TEXT = "@";

    /**
     * 一个Sheet工作薄
     */
    private Sheet sheet;
    /**
     * 工作薄的总行数
     */
    private int rowCount;
    private final ExcelReaderConfig config;
    private FormulaEvaluator evaluator;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 构造函数
     *
     * @param inputStream
     *            Excel文件流
     * @param sheetIndex
     *            要解析的sheetIndex,从0开始
     */
    public ExcelReader(InputStream inputStream, int sheetIndex, ExcelReaderConfig config) {
        Validate.notNull(config);
        this.config = config;
        init(inputStream, sheetIndex);
    }

    /**
     * 初始化
     */
    private void init(InputStream inputStream, int sheetIndex) {
        try {
            Workbook curWorkbook = WorkbookFactory.create(inputStream);
            this.evaluator = curWorkbook.getCreationHelper().createFormulaEvaluator();
            this.sheet = curWorkbook.getSheetAt(sheetIndex);
            this.rowCount = config.isBlankLineTerminated() ? realRows() : sheet.getLastRowNum() + 1;
        } catch (IOException e) {
            logger.error("ExcelReader.init", e);
        }
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
        List<String> list = new ArrayList<>();
        Row row = sheet.getRow(rowNo);
        if (row != null) {
            for (int i = 0; i < row.getLastCellNum(); i++) {
                Cell cell = row.getCell(i);
                String cellContent = getCellContent(cell);
                if (config.isTrimCellContent()) {
                    cellContent = cellContent.trim();
                }
                list.add(cellContent);
            }
        }
        return list;
    }

    /**
     * 获得单元格内容
     */
    private String getCellContent(Cell cell) {
        String cellContent = "";
        if (cell != null) {
            // 如果有表格中单元格有公式，cell.toString()得不到正确结果。这里需要做下处理。需要注意：公式计算出来的数字大多为浮点型，需要客户端去精确
            switch (cell.getCellType()) {
            case FORMULA:
                try {
                    cellContent = String.valueOf(cell.getNumericCellValue());
                } catch (IllegalStateException e) {
                    try {
                        cellContent = String.valueOf(cell.getRichStringCellValue());
                    } catch (IllegalStateException e1) {
                        try {
                            CellValue cellValue = evaluator.evaluate(cell);
                            cellContent = cellValue.formatAsString();
                        } catch (Exception e2) {
                            cellContent = cell.toString();
                        }
                    }
                }
                break;
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", LocaleUtil.getUserLocale());
                    sdf.setTimeZone(LocaleUtil.getUserTimeZone());
                    return sdf.format(cell.getDateCellValue());
                }
                cell.setCellType(CellType.STRING);
                cellContent = cell.toString();
                break;
            default:
                cell.setCellType(CellType.STRING);
                cellContent = cell.toString();
                break;
            }
        }
        return cellContent;
    }

    /** 获得所有行集合 */
    public List<List<String>> getRows() {
        return Collections3.getListFromIterator(iterator());
    }

    /**
     * Excel数据转换为List entity
     *
     * @param wipeOffHead
     *            是否去除头部
     */
    public <T> List<T> toEntity(Class<T> entityClass, boolean wipeOffHead) {
        List<T> list = new ArrayList<>();
        Field[] fields = entityClass.getDeclaredFields();
        for (int i = wipeOffHead ? 1 : 0; i < getRowCount(); i++) {
            List<String> row = getRow(i);
            T t = Beans.getInstance(entityClass);
            for (Field field : fields) {
                ExcelColumn fieldAnnotation = field.getAnnotation(ExcelColumn.class);
                if (fieldAnnotation != null) {
                    int index = fieldAnnotation.value();
                    if (row.size() > index) {
                        String val = row.get(index);
                        Beans.setProperty(t, field.getName(), val);
                    }
                }
            }
            list.add(t);
        }
        return list;
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

    /** Excel中的列 */
    @Documented
    @Target(value = { ElementType.FIELD })
    @Retention(value = RetentionPolicy.RUNTIME)
    public @interface ExcelColumn {
        /**
         * 列的下标排序，从0开始
         */
        int value();
    }
}
