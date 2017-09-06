package com.github.ittalks.invoice;

import com.alibaba.fastjson.JSON;
import com.github.ittalks.invoice.net.Error;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.logging.Logger;

/**
 * Created by 刘春龙 on 2017/9/6.
 */
public class InvoiceParserTest {

    private static final Logger logger = Logger.getLogger(InvoiceParser.class.getName());

    @Test
    public void invoiceParserTest() throws URISyntaxException, UnsupportedEncodingException, FileNotFoundException, InterruptedException {
        InvoiceParser invoiceParser = new InvoiceParser.Builder().
                success(obj -> logger.info("解析结果：" + JSON.toJSONString(obj))).
                error(errors -> logger.info((String) errors[0])).build();

        File file = new File("invoices");
        File[] files = file.listFiles();
        for (File f : files) {
            invoiceParser.parsePdfByQrcode(f.getAbsolutePath());
        }
        Thread.sleep(5000);
    }
}
