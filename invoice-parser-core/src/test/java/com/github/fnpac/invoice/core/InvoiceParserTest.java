package com.github.fnpac.invoice.core;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.fnpac.invoice.common.exception.NestedException;
import com.github.fnpac.invoice.core.handler.PDFTextHandler;
import com.github.fnpac.invoice.core.model.InvoiceInfo;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.text.PDFTextStripperByArea;
import org.junit.Test;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by 刘春龙 on 2017/9/6.
 */
public class InvoiceParserTest {

    private static final Logger logger = Logger.getLogger(InvoiceParser.class.getName());

    private static final String provinceKey = "provinceKey";
    private static final String provinceRegExp = "(\\S+?)增值.+?发票";
    private static final Rectangle provinceRect = new Rectangle(150, 5, 430 - 150, 70 - 5);

    @Test
    public void invoiceParserByQcodeMultiTest() throws URISyntaxException, UnsupportedEncodingException, FileNotFoundException, InterruptedException {
        InvoiceParser invoiceParser = new InvoiceParser.Builder().
                success(obj -> logger.info("解析结果：" + JSON.toJSONString(obj))).
                error(errors -> logger.info((String) errors[0])).build();

        File file = new File("invoices");
        File[] files = file.listFiles();
        for (File f : files) {
            JSONObject result = invoiceParser.parsePdfByQrcode(f.getAbsolutePath(), false);
        }
    }

    @Test
    public void getWidthAndHeight() throws FileNotFoundException {
        // 待解析PDF
        File pdfFile = new File("invoices/滴滴电子发票.pdf");
        if (!pdfFile.exists()) {
            throw new FileNotFoundException();
        }

        PDDocument document = null;
        try {
            document = PDDocument.load(pdfFile);

            int pagesNumber = document.getNumberOfPages();
            if (pagesNumber <= 0) {
                throw new NestedException("can't find pdf pages");
            }

            logger.info("Width" + String.valueOf(document.getPage(0).getMediaBox().getWidth()));
            logger.info("Height" + String.valueOf(document.getPage(0).getMediaBox().getHeight()));
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (document != null) {
                try {
                    document.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Test
    public void getProvince() throws FileNotFoundException {
        // 待解析PDF
        File pdfFile = new File("invoices/021001700111_35229425.pdf");
        if (!pdfFile.exists()) {
            throw new FileNotFoundException();
        }

        PDDocument document = null;
        try {
            document = PDDocument.load(pdfFile);

            int pagesNumber = document.getNumberOfPages();
            if (pagesNumber <= 0) {
                throw new NestedException("can't find pdf pages");
            }

            String province = getProvince(document.getPage(0));
            logger.info(province);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (document != null) {
                try {
                    document.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 获取发票的省份
     *
     * @return
     */
    private String getProvince(PDPage pdPage) throws IOException {
        PDFTextStripperByArea stripper = new PDFTextStripperByArea();
        stripper.setSortByPosition(true);

        Rectangle2D rect = new Rectangle2D.Double(provinceRect.getX(), provinceRect.getY(),
                provinceRect.getWidth(), provinceRect.getHeight());
        stripper.addRegion(provinceKey, rect);

        stripper.extractRegions(pdPage);
        String regionText = stripper.getTextForRegion(provinceKey);
        Pattern pattern = Pattern.compile(provinceRegExp);
        Matcher matcher = pattern.matcher(regionText);
        if (matcher.find()) {
            if (matcher.groupCount() > 0) {
                return matcher.group(1);
            }
        }
        return null;
    }

    @Test
    public void invoiceParserByTextTest() throws FileNotFoundException {
        for (int i = 0; i < 2; i++) {
            long startTime = System.currentTimeMillis();
            // 待解析PDF
            File pdfFile = new File("invoices/01100160011116046427.pdf");
            if (!pdfFile.exists()) {
                throw new FileNotFoundException();
            }

            PDDocument document = null;
            try {
                document = PDDocument.load(pdfFile);

                int pagesNumber = document.getNumberOfPages();
                if (pagesNumber <= 0) {
                    throw new NestedException("can't find pdf pages");
                }

                long startParseTime = System.currentTimeMillis();
                PDFTextHandler handler = new PDFTextHandler();
                // 检查pdf大小比例是否合法
                handler.check(document);

                // 解析文本
                InvoiceInfo invoiceInfo = handler.parsePageByTemplate(document.getPage(0));
                logger.info(JSONObject.toJSONString(invoiceInfo));
                logger.info("总时间：" + String.valueOf(System.currentTimeMillis() - startTime));
                logger.info("解析用时：" + String.valueOf(System.currentTimeMillis() - startParseTime));
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (document != null) {
                    try {
                        document.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @Test
    public void invoiceParserByTextMultiTest()
            throws URISyntaxException, UnsupportedEncodingException, FileNotFoundException, NestedException, InterruptedException {
        InvoiceParser invoiceParser = new InvoiceParser.Builder().build();

        File file = new File("invoices");
        File[] files = file.listFiles();
        for (File f : files) {
            InvoiceInfo invoiceInfo = invoiceParser.parsePdfByText(f.getAbsolutePath());
            logger.info(JSONObject.toJSONString(invoiceInfo));
        }
    }

    @Test
    public void testReg() {
        Pattern pattern = Pattern.compile("价税合计\\s*[\\(（]{1}大写[）\\)]{1}\\s*(\\S*)\\s*[\\(（]{1}小写[）\\)]{1}\\s*(.*)");
        Matcher matcher = pattern.matcher("价税合计 (大写） 贰拾元整 （小写） ￥ 20.00");
        if (matcher.find()) {
            System.out.println("true");
        }
    }
}
