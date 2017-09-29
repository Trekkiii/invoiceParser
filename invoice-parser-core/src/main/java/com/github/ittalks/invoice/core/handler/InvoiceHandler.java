package com.github.ittalks.invoice.core.handler;

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

    public static List<BufferedImage> getQrcode(String file) throws FileNotFoundException {

        // 待解析PDF
        File pdfFile = new File(file);
        if (!pdfFile.exists()) {
            throw new FileNotFoundException();
        }

        PDDocument document = null;
        try {
            document = PDDocument.load(pdfFile);
            int pages_size = document.getNumberOfPages();
            logger.info("Pages size =============== " + pages_size);

            for (int i = 0; i < pages_size; i++) {
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
}
