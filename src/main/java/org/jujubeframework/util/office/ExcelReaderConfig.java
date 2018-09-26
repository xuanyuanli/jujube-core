package org.jujubeframework.util.office;

/**
 * TableDataReader的配置器
 *
 * @author John Li Email：jujubeframework@163.com
 */
public class ExcelReaderConfig {
    private boolean replaceCellLineBreak;
    private boolean trimCellContent;
    private boolean blankLineTerminated;

    public static final ExcelReaderConfig DEFAULT = new ExcelReaderConfig(true, true, false);
    /**
     * 所有参数默认为ture
     */
    public static final ExcelReaderConfig ALL_RIGHT = new ExcelReaderConfig(true, true, true);

    public ExcelReaderConfig() {
        super();
    }

    public ExcelReaderConfig(boolean replaceCellLineBreak, boolean trimCellContent, boolean blankLineTerminated) {
        super();
        this.replaceCellLineBreak = replaceCellLineBreak;
        this.trimCellContent = trimCellContent;
        this.blankLineTerminated = blankLineTerminated;
    }

    /**
     * 是否替换单元格中的换行符和制表符
     */
    public boolean isReplaceCellLineBreak() {
        return replaceCellLineBreak;
    }

    /**
     * 设置 是否替换单元格中的换行符和制表符。默认为false
     */
    public void setReplaceCellLineBreak(boolean replaceCellLineBreak) {
        this.replaceCellLineBreak = replaceCellLineBreak;
    }

    /**
     * 是否trim单元格内容
     */
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
