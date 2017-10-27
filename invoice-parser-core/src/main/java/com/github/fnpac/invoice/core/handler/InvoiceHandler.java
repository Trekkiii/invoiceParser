package com.github.fnpac.invoice.core.handler;

import com.github.fnpac.invoice.common.exception.NestedException;
import com.github.fnpac.invoice.core.models.InvoiceInfo;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by 刘春龙 on 2017/9/6.
 */
public class InvoiceHandler {

    private static final Logger logger = Logger.getLogger(InvoiceHandler.class.getName());

    @Deprecated
    public static List<BufferedImage> getQrcode(String file) throws FileNotFoundException {

        // 待解析PDF
        File pdfFile = new File(file);
        if (!pdfFile.exists()) {
            throw new FileNotFoundException();
        }

        PDDocument document = null;
        try {
            document = PDDocument.load(pdfFile);
            int pagesNumber = document.getNumberOfPages();
            if (pagesNumber <= 0) {
                throw new NestedException("can't find pages");
            }

            for (int i = 0; i < pagesNumber; i++) {
                PDPage page = document.getPage(i);
                PDResources resources = page.getResources();
                Iterable xobjects = resources.getXObjectNames();
                if (xobjects != null) {
                    List<BufferedImage> images = new ArrayList<>();
                    for (Object xobject : xobjects) {
                        COSName key = (COSName) xobject;
                        if (resources.isImageXObject(key)) {
                            PDImageXObject image = (PDImageXObject) resources.getXObject(key);
                            BufferedImage bufferedImage = image.getImage();
                            images.add(bufferedImage);
                        }
                    }
                    return images;
                }
            }

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

        return null;
    }

    @Deprecated
    public static InvoiceInfo getInvoiceByText(String sender, String file)
            throws FileNotFoundException, NestedException {
        // 待解析PDF
        File pdfFile = new File(file);
        if (!pdfFile.exists()) {
            throw new FileNotFoundException();
        }

        PDDocument document = null;
        try {
            document = PDDocument.load(pdfFile);
            if (document.getNumberOfPages() <= 0) {
                throw new NestedException("can't find pdf pages");
            }

            PdfTextHandler handler = new PdfTextHandler();
            // 检查pdf大小比例是否合法
            handler.check(document);

            // 解析文本
            return handler.parsePageByTemplate(sender, document.getPage(0));
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
        return null;
    }

    public static InvoiceInfo getInvoiceWithOpenCV(String sender, String file)
            throws FileNotFoundException, NestedException {
        // 待解析PDF
        File pdfFile = new File(file);
        if (!pdfFile.exists()) {
            throw new FileNotFoundException();
        }

        PDDocument document = null;
        try {
            document = PDDocument.load(pdfFile);
            if (document.getNumberOfPages() <= 0) {
                throw new NestedException("can't find pdf pages");
            }

            PdfTableHandler parser = new PdfTableHandler();
            return parser.parsePdfPage(document, 1);
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
        return null;
    }
}
