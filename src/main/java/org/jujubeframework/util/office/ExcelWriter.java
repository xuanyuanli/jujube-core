package org.jujubeframework.util.office;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.poi.hssf.usermodel.DVConstraint;
import org.apache.poi.hssf.usermodel.HSSFDataValidation;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jujubeframework.constant.SystemProperties;
import org.jujubeframework.util.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Excel写入工具类
 *
 * @author John Li Email：jujubeframework@163.com
 */
public class ExcelWriter {
    private static final Logger logger = LoggerFactory.getLogger(ExcelWriter.class);

    public static final String CSV_FILE_SUFFIX = ".csv";

    private ExcelWriter() {
    }

    /**
     * 根据模板写入Excel
     *
     * @param templateFilePath
     *            模板文件路径
     * @param destFileName
     *            要写入的文件名称。注意：不是路径，只要名称即可；因为这个文件也要写入到临时目录中去
     * @param copyLineIndex
     *            设定保留模板前几行
     * @param lines
     *            数据
     * @return File 已经写入数据的Excel。如果出错，则返回null
     */
    public static File writeExcelWithTemplate(String templateFilePath, String destFileName, int copyLineIndex, List<List<String>> lines, ExcelSheetHandler sheetHandler) {
        FileOutputStream out = null;
        FileInputStream templateInput = null;
        File destFile = null;
        try {
            // 解决线程同步问题，创建一个不可能冲突的文件夹
            File tmpDir = new File(SystemProperties.TMPDIR, "/temp_excel/" + Thread.currentThread().getId() + System.currentTimeMillis() + "/");
            if (!tmpDir.exists()) {
                tmpDir.mkdirs();
            }
            copyTemplate(templateFilePath, tmpDir.getAbsolutePath() + "/template.xls");
            File templateFile = new File(tmpDir, "template.xls");
            destFile = new File(tmpDir, destFileName);

            templateInput = new FileInputStream(templateFile);
            Workbook workbook = WorkbookFactory.create(templateInput);
            Sheet sheet = workbook.getSheetAt(0);
            if (sheetHandler != null) {
                sheetHandler.handler(sheet);
            }
            for (int i = 0; i < lines.size(); i++) {
                List<String> vRow = lines.get(i);
                Row row = sheet.createRow(copyLineIndex + i);
                for (int j = 0; j < vRow.size(); j++) {
                    String cValue = vRow.get(j);
                    row.createCell(j).setCellValue(cValue);
                }
            }
            out = new FileOutputStream(destFile);
            workbook.write(out);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
            }
            if (templateInput != null) {
                try {
                    templateInput.close();
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
        return destFile;
    }

    /**
     * 这里进行异步处理，不可直接拷贝，以防模板文件处于冲突状态
     */
    private static synchronized void copyTemplate(String templatePath, String destPath) throws IOException {
        FileUtils.copyFile(new File(templatePath), new File(destPath));
    }

    /**
     * 生成excle
     *
     * @param destFile
     *            excel文件地址
     * @param lines
     *            数据
     */
    public static void generateExcel(File destFile, List<List<String>> lines) {
        FileOutputStream outputStream = null;
        try {
            if (!destFile.exists()) {
                destFile = Files.createFile(destFile.getAbsolutePath());
            }
            outputStream = new FileOutputStream(destFile);
            IOUtils.copy(generateExcelInputStream(lines), outputStream);
            logger.info("生成Excle：{},共{}条数据", destFile.getAbsolutePath(), lines.size());
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
    }

    /**
     * 生成excle
     *
     * @param lines
     *            数据
     */
    public static InputStream generateExcelInputStream(List<List<String>> lines) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet();
            for (int i = 0; i < lines.size(); i++) {
                List<String> vRow = lines.get(i);
                Row row = sheet.createRow(i);
                for (int j = 0; j < vRow.size(); j++) {
                    String cValue = vRow.get(j);
                    row.createCell(j).setCellValue(cValue);
                }
            }
            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
        return null;
    }

    /**
     * 生成csv文件
     *
     * @param dest
     *            目标路径
     * @param data
     *            数据
     */
    public static void generateCsv(File dest, List<List<String>> data) {
        try {
            FileUtils.writeLines(dest, "gbk", escapeCsv(data), "\n");
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * 生成csv输入流
     *
     * @param data
     *            数据
     */
    public static InputStream generateCsvInputStream(List<List<String>> data) {
        try {
            List<String> rows = escapeCsv(data);
            return IOUtils.toInputStream(StringUtils.join(rows, "\n"), "gbk");
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    private static List<String> escapeCsv(List<List<String>> lines) {
        List<String> result = new ArrayList<>();
        for (List<String> line : lines) {
            StringBuilder sbLine = new StringBuilder();
            if (line == null) {
                continue;
            }
            for (int i = 0; i < line.size(); i++) {
                String cell = StringEscapeUtils.escapeCsv(line.get(i));
                if (i != line.size() - 1) {
                    sbLine.append(cell).append(",");
                } else {
                    sbLine.append(cell);
                }
            }
            result.add(sbLine.toString());
        }
        return result;
    }

    /**
     * 设置某些列的值只能输入预制的数据,显示下拉框.
     *
     * @param sheet
     *            要设置的sheet.
     * @param textlist
     *            下拉框显示的内容
     * @param firstRow
     *            开始行
     * @param endRow
     *            结束行
     * @param firstCol
     *            开始列
     * @param endCol
     *            结束列
     * @return 设置好的sheet.
     */
    public static Sheet setValidation(Sheet sheet, String[] textlist, int firstRow, int endRow, int firstCol, int endCol) {
        // 加载下拉列表内容

        DVConstraint constraint = DVConstraint.createExplicitListConstraint(textlist);
        // 设置数据有效性加载在哪个单元格上,四个参数分别是：起始行、终止行、起始列、终止列
        CellRangeAddressList regions = new CellRangeAddressList(firstRow, endRow, firstCol, endCol);
        // 数据有效性对象
        DataValidation dataValidationList = new HSSFDataValidation(regions, constraint);
        sheet.addValidationData(dataValidationList);
        return sheet;
    }

}
