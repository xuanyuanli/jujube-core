package org.jujubeframework.util.office;

import org.apache.poi.ss.usermodel.Sheet;

/**
 * Sheet处理，可进行excel格式设置
 *
 * @author John Li
 */
public interface ExcelSheetHandler {
    /**
     * handler
     *
     * @param sheet
     */
    void handler(Sheet sheet);
}