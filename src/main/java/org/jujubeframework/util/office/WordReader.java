package org.jujubeframework.util.office;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;

/**
 * @author John Li
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class WordReader {

    private static Logger logger = LoggerFactory.getLogger(WordReader.class);

    public static String getWordContent(String filePath) {
        String result = "";
        File wordFile = new File(filePath);
        if (wordFile.exists()) {
            ;
            // word2003版本
            try (WordExtractor wordExtractor = new WordExtractor(new FileInputStream(wordFile))) {
                result = wordExtractor.getText();
            } catch (Exception e) {
                // word2007版本
                try (XWPFWordExtractor xwpfWordExtractor = new XWPFWordExtractor(new XWPFDocument(new FileInputStream(wordFile)))) {
                    result = xwpfWordExtractor.getText();
                } catch (Exception e1) {
                    logger.error("getWordContent", e1);
                }
            }
        }
        return result;
    }

}
