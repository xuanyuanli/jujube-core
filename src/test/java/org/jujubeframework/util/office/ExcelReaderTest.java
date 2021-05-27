package org.jujubeframework.util.office;

import java.io.FileInputStream;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.assertj.core.api.Assertions;
import org.jujubeframework.util.Resources;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ExcelReaderTest {

    @Test
    public void testRealCount() {
        Resource resource = Resources.getClassPathResources("material/testRealCount.xlsx");
        ExcelReader reader = null;
        try (FileInputStream inputStream = FileUtils.openInputStream(resource.getFile())) {
            reader = new ExcelReader(inputStream, 0, ExcelReaderConfig.DEFAULT);
        } catch (IOException e) {
            log.error("read", e);
        }
        Assertions.assertThat(reader.getRowCount()).isEqualTo(9);
        Assertions.assertThat(reader.getRow(0).get(0)).isEqualTo("ID");
        try (FileInputStream inputStream = FileUtils.openInputStream(resource.getFile())) {
            reader = new ExcelReader(inputStream, 0, ExcelReaderConfig.ALL_RIGHT);
        } catch (IOException e) {
            log.error("read", e);
        }
        Assertions.assertThat(reader.getRowCount()).isEqualTo(4);
        Assertions.assertThat(reader.getRow(0).get(0)).isEqualTo("ID");
        try (FileInputStream inputStream = FileUtils.openInputStream(resource.getFile())) {
            reader = new ExcelReader(inputStream, 0, ExcelReaderConfig.ALL_FALSE);
        } catch (IOException e) {
            log.error("read", e);
        }
        Assertions.assertThat(reader.getRowCount()).isEqualTo(9);
        Assertions.assertThat(reader.getRow(0).get(0)).isEqualTo("ID");
    }

    @Test
    public void testNumeric() {
        Resource resource = Resources.getClassPathResources("material/testNumeric.xlsx");
        ExcelReader reader = null;
        try (FileInputStream inputStream = FileUtils.openInputStream(resource.getFile())) {
            reader = new ExcelReader(inputStream, 0, ExcelReaderConfig.DEFAULT);
        } catch (IOException e) {
            log.error("read", e);
        }
        Assertions.assertThat(reader.getRow(1).get(1)).isEqualTo("13661162128");
        Assertions.assertThat(reader.getRow(2).get(1)).isEqualTo("13695597788");
        Assertions.assertThat(reader.getRow(3).get(1)).isEqualTo("13706274444");
    }

    @Test
    public void testFormula() {
        Resource resource = Resources.getClassPathResources("material/testFormula.xlsx");
        ExcelReader reader = null;
        try (FileInputStream inputStream = FileUtils.openInputStream(resource.getFile())) {
            reader = new ExcelReader(inputStream, 0, ExcelReaderConfig.DEFAULT);
        } catch (IOException e) {
            log.error("read", e);
        }
        Assertions.assertThat(reader.getRow(1).get(6)).isEqualTo("150");
        Assertions.assertThat(reader.getRow(4).get(6)).isEqualTo("150.0");
    }
}