package org.jujubeframework.util.office;

import com.itextpdf.text.pdf.BaseFont;
import lombok.extern.slf4j.Slf4j;
import org.xhtmlrenderer.pdf.ITextFontResolver;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 * PDF写入工具类 <br>
 * 根据html生产PDF的时候，需要把typeface/msyh.ttf字体文杰放在项目的classpath下
 *
 * @author ZhaoYanqi
 */
@Slf4j
public class PdfWriters {

    /**
     * 根据html内容生成PDF文件流（支持CSS） <br>
     * 注意：需要把typeface/MicrosoftYaHei.ttf字体文杰放在项目的classpath下
     */
    public static InputStream createPdfFromHtml(String html) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ITextRenderer render = new ITextRenderer();
        ITextFontResolver fontResolver = render.getFontResolver();
        try {
            fontResolver.addFont("typeface/MicrosoftYaHei.ttf", BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
            render.setDocumentFromString(html);
            render.layout();
            render.createPDF(outputStream);
            return new ByteArrayInputStream(outputStream.toByteArray());
        } catch (Exception e) {
            log.error("写入PDF出错", e);
        }
        return null;
    }

}
