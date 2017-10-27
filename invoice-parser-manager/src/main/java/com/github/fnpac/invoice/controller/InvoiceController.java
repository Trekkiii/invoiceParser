package com.github.fnpac.invoice.controller;

import com.alibaba.fastjson.JSONObject;
import com.github.fnpac.invoice.common.result.APIResult;
import com.github.fnpac.invoice.common.result.ErrorCode;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Created by 刘春龙 on 2017/9/29.
 */
@Controller
public class InvoiceController {

    private static final Logger logger = LoggerFactory.getLogger(InvoiceController.class);

    private HashSet allowedSuffixNames = new HashSet<String>() {{
        add(".pdf");
        add(".jpg");
        add(".jpeg");
        add(".png");
        add(".bmp");
    }};

    private InvoiceParser invoiceParser = new InvoiceParser.Builder().build();


    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    @ResponseBody
    public Object upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ErrorCode.FILE_EMPTY;
        }
        // 获取文件名
        String fileName = file.getOriginalFilename().toLowerCase();
        logger.info("上传的文件名为：" + fileName);
        // 获取文件的后缀名
        int index = fileName.lastIndexOf(".");
        if (index == -1) {
            return ErrorCode.INVALID_FILE_TYPE;
        }
        String suffixName = fileName.substring(index);
        logger.info("上传的后缀名为：" + suffixName);
        if (!allowedSuffixNames.contains(suffixName)) {
            return ErrorCode.INVALID_FILE_TYPE;
        }

        // 文件上传后的路径
        String filePath = "tmpUpload/";
        fileName = UUID.randomUUID() + suffixName;

        File tmpFile = null;
        try {
            tmpFile = new File(filePath + fileName).getCanonicalFile();
            // 检测是否存在目录
            if (!tmpFile.getParentFile().exists()) {
                tmpFile.getParentFile().mkdirs();
            }

            file.transferTo(tmpFile);
            JSONObject result = invoiceParser.parsePdfByQrcode(tmpFile.getAbsolutePath(), false);
            if (result != null) {
                return APIResult.Y(result);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (tmpFile != null) {
                tmpFile.delete();
            }
        }
        return ErrorCode.ERROR_UNKNOWN;
    }

    @RequestMapping(value = "/analyze", method = RequestMethod.POST)
    @ResponseBody
    public Object analyze(@RequestParam("pdfDownloadUrl") String pdfDownloadUrl) {
        if (StringUtils.isEmpty(pdfDownloadUrl)) {
            return ErrorCode.ILLARGUMENT;
        }
        Pattern regUrl = Pattern.compile("^(http|ftp|https):\\/\\/[\\w\\-_]+(\\.[\\w\\-_]+)+([\\w\\-\\.,@?^=%&amp;:/~\\+#]*[\\w\\-\\@?^=%&amp;/~\\+#])?");
        if (!regUrl.matcher(pdfDownloadUrl.toLowerCase()).find()) {
            return ErrorCode.INVALID_URL;
        }
        String downloadedFile = null;
        try {
            downloadedFile = ImageTool.downloadPdfFile(pdfDownloadUrl);
            if (!StringUtils.isEmpty(downloadedFile)) {
                JSONObject result = invoiceParser.parsePdfByQrcode(downloadedFile, false);
                if (result != null) {
                    return APIResult.Y(result);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            File file;
            if (!StringUtils.isEmpty(downloadedFile) && (file = new File(downloadedFile)).exists()) {
                file.delete();
            }
        }
        return ErrorCode.ERROR_UNKNOWN;
    }
}
