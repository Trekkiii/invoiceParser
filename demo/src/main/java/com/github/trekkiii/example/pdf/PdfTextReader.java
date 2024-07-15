package com.github.trekkiii.example.pdf;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.logging.Logger;

/**
 * Created by 刘春龙 on 2017/9/5.
 */
public class PdfTextReader {

    private static final Logger logger = Logger.getLogger(PdfTextReader.class.getName());

    public static void main(String[] args) throws URISyntaxException, IOException {
        File pdfFile = new File(PdfTextReader.class.getClassLoader().getResource("invoices/01100160011116046427.pdf").toURI());

        PDDocument document = null;

        /*
            方式一：
         */
//        InputStream input = new FileInputStream(pdfFile);
//        //加载 pdf 文档
//        PDFParser parser = new PDFParser(new RandomAccessBuffer(input));
//        parser.parse();
//        document = parser.getPDDocument();

        /*
            方式二：
         */
        document = PDDocument.load(pdfFile);

        // 获取页码
        int pages = document.getNumberOfPages();

        // 读文本内容
        PDFTextStripper stripper = new PDFTextStripper();

        // 设置按顺序输出
        stripper.setSortByPosition(true);
        stripper.setStartPage(1);
        stripper.setEndPage(pages);
        String content = stripper.getText(document);

        logger.info(content);
    }
}
