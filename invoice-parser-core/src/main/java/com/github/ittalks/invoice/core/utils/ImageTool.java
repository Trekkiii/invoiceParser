package com.github.ittalks.invoice.core.utils;

import com.github.ittalks.invoice.common.exception.NestedException;
import com.github.ittalks.invoice.common.result.ErrorCode;
import org.springframework.http.HttpStatus;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by 刘春龙 on 2017/9/29.
 */
public class ImageTool {

    public static String downloadPdfFile(String url) {
        FileOutputStream out = null;
        InputStream in = null;
        try {
            URL downloadUrl = new URL(url);
            URLConnection urlConnection = downloadUrl.openConnection();
            HttpURLConnection httpURLConnection = (HttpURLConnection) urlConnection;

            // 将此URLConnection的doOutput字段的值设置为指定的值。
            // URL connection可用于输入和/或输出。如果您打算使用URL connection输出，请将DoOutput标志设置为true，否则返回false。 默认值为false。
            httpURLConnection.setDoOutput(true);
            // 如果要使用URL connection进行输入，请将DoInput标志设置为true，否则为false。 默认值为true。
            httpURLConnection.setDoInput(true);
            // will not use caches
            httpURLConnection.setUseCaches(false);
            // setting serialized
            httpURLConnection.setRequestProperty("Content-type", "application/x-java-serialized-object");
            // default is GET
//            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setRequestProperty("connection", "Keep-Alive");
            httpURLConnection.setRequestProperty("Charsert", "UTF-8");
            // 1 min
            httpURLConnection.setConnectTimeout(60000);
            // 1 min
            httpURLConnection.setReadTimeout(60000);

            // connect to server (tcp)
            httpURLConnection.connect();

            in = httpURLConnection.getInputStream();// send request to

            String localFileName = null;
            if (httpURLConnection.getResponseCode() == HttpStatus.OK.value()) {
                String contentType = httpURLConnection.getHeaderField("Content-Type").toLowerCase();
                if (contentType.startsWith("application/pdf") ||
                        contentType.startsWith("application/octet-stream")) {
                    localFileName = getFileName(httpURLConnection.getHeaderField("content-disposition"));
                }
            }
            if (localFileName == null || !localFileName.endsWith(".pdf")) {
                throw new NestedException(ErrorCode.INVALID_FILE_TYPE);
            }

            // 文件上传后的路径
            String filePath = "tmpDownload/";
            // 解决中文问题，liunx下中文路径，图片显示问题
            localFileName = UUID.randomUUID() + ".pdf";

            File file = new File(filePath + localFileName).getCanonicalFile();

            // 检测是否存在目录
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            // 创建文件
            if (!file.exists()) {
                file.createNewFile();
            }

            out = new FileOutputStream(file);

            byte[] buffer = new byte[4096];
            int readLength = 0;
            while ((readLength = in.read(buffer)) > 0) {
                byte[] bytes = new byte[readLength];
                System.arraycopy(buffer, 0, bytes, 0, readLength);
                out.write(bytes);
            }
            out.flush();
            return file.getCanonicalPath();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if(in != null){
                    in.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                if(out != null){
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private static String getFileName(String content) {
        Pattern pattern = Pattern.compile("attachment;filename=(.*.pdf)");
        Matcher matcher = pattern.matcher(content.toLowerCase());
        if (matcher.find()) {
            return matcher.group(1);
        }
        return content;
    }
}
