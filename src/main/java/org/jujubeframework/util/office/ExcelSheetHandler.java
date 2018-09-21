package org.jujubeframework.util.office;

import org.apache.poi.ss.usermodel.Sheet;

/**
 * Sheet处理，可进行excel格式设置
 * 
 * @author John Li
 * @date 2014年6月25日 下午3:57:03
 */
public interface ExcelSheetHandler {
    /**
     * handler
     * @param sheet
     */
    void handler(Sheet sheet);
}