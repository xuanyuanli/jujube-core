package org.jujubeframework.util.office;

/**
 * ExcelReader的配置器
 *
 * @author John Li Email：jujubeframework@163.com
 */
public class ExcelReaderConfig {
    /**
     * 设置 是否trim单元格中的内容。默认为false
     */
    private boolean trimCellContent;
    /**
     * 计算excel总行数时，是否遇到空行，则终止
     */
    private boolean blankLineTerminated;

    public static final ExcelReaderConfig DEFAULT = new ExcelReaderConfig(true, false);
    /**
     * 所有参数默认为ture
     */
    public static final ExcelReaderConfig ALL_RIGHT = new ExcelReaderConfig(true, true);

    /**
     * 所有参数默认为false
     */
    public static final ExcelReaderConfig ALL_FALSE = new ExcelReaderConfig(false, false);

    public ExcelReaderConfig() {
        super();
    }

    public ExcelReaderConfig(boolean trimCellContent, boolean blankLineTerminated) {
        super();
        this.trimCellContent = trimCellContent;
        this.blankLineTerminated = blankLineTerminated;
    }

    public boolean isTrimCellContent() {
        return trimCellContent;
    }

    /**
     * 设置 是否trim单元格内容。默认为false
     */
    public void setTrimCellContent(boolean trimCellContent) {
        this.trimCellContent = trimCellContent;
    }

    /**
     * 计算excel总行数时，是否遇到空行，则终止
     */
    public boolean isBlankLineTerminated() {
        return blankLineTerminated;
    }

    /**
     * 设置 计算excel总行数时，是否遇到空行，则终止。默认为false
     */
    public void setBlankLineTerminated(boolean blankLineTerminated) {
        this.blankLineTerminated = blankLineTerminated;
    }

}
