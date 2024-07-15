package com.github.trekkiii.invoice.core.utils;

import com.github.trekkiii.invoice.common.exception.NestedException;
import com.github.trekkiii.invoice.common.result.ErrorCode;
import org.apache.pdfbox.io.IOUtils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.springframework.http.HttpStatus;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by 刘春龙 on 2017/10/24.
 */
public class Utils {

    private Utils() {
    }

    private static final String tmpUploadPath = "/data/tmpUpload/";

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
                throw new NestedException(ErrorCode.INVALID_FILE_SUFFIX);
            }

            // 解决中文问题，liunx下中文路径，图片显示问题
            localFileName = UUID.randomUUID() + ".pdf";

            File file = new File(tmpUploadPath + localFileName).getCanonicalFile();

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
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                if (out != null) {
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

    /**
     * 将BufferedImage转换为灰度OpenCV Mat
     *
     * @param inImg Buffered Image
     * @return org.opencv.core.Mat
     * @throws IOException
     */
    public static Mat bufferedImage2GrayscaleMat(BufferedImage inImg) throws IOException {
        return bufferedImage2Mat(inImg, Imgcodecs.IMREAD_GRAYSCALE);
    }

    /**
     * 使用自定义标志将BufferedImage转换为OpenCV Mat
     *
     * @param inImg Buffered Image
     * @param flag  org.opencv.imgcodecs.Imgcodecs flag
     * @return org.opencv.core.Mat
     * @throws IOException
     */
    public static Mat bufferedImage2Mat(BufferedImage inImg, int flag) throws IOException {
        return inputStream2Mat(bufferedImage2InputStream(inImg), flag);
    }

    /**
     * 将BufferedImage转换为InputStream
     *
     * @param inImg Buffered Image
     * @return java.io.InputStream
     * @throws IOException
     */
    public static InputStream bufferedImage2InputStream(BufferedImage inImg) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(inImg, "png", os);
        return new ByteArrayInputStream(os.toByteArray());
    }

    /**
     * 将InputStream转换为OpenCV Mat
     *
     * @param stream java.io.InputStream
     * @param flag   org.opencv.imgcodecs.Imgcodecs flag
     * @return org.opencv.core.Mat
     * @throws IOException
     */
    public static Mat inputStream2Mat(InputStream stream, int flag) throws IOException {
        byte[] byteBuff = IOUtils.toByteArray(stream);
        return Imgcodecs.imdecode(new MatOfByte(byteBuff), flag);
    }
}
