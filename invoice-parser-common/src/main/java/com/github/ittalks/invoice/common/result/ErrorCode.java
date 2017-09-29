package com.github.ittalks.invoice.common.result;

import com.github.ittalks.invoice.common.exception.ErrorMessage;
import org.springframework.http.HttpStatus;

/**
 * Created by 刘春龙 on 2017/3/9.
 */
public class ErrorCode {

    // system
    public static final String errorCode = "S01000";
    public static final ErrorMessage ERROR_UNKNOWN = registerErrorMsg("S01001", "Unknow Error", HttpStatus.BAD_REQUEST);
    public static final ErrorMessage INTERNAL_SERVER_ERROR = registerErrorMsg("S01002", "服务器内部错误", HttpStatus.INTERNAL_SERVER_ERROR);
    public static final ErrorMessage ILLARGUMENT = registerErrorMsg("S01003", "参数不合法", HttpStatus.BAD_REQUEST);
    public static final ErrorMessage INVALID_URL = registerErrorMsg("S01004", "Url不合法", HttpStatus.BAD_REQUEST);

    // invoice
    public static final ErrorMessage INVALID_FILE_TYPE = registerErrorMsg("I01001", "不支持的文件类型", HttpStatus.BAD_REQUEST);
    public static final ErrorMessage INVALID_QR_CONTENT = registerErrorMsg("I01002", "无效的二维码数据", HttpStatus.BAD_REQUEST);
    public static final ErrorMessage FILE_EMPTY = registerErrorMsg("I01003", "文件内容为空", HttpStatus.BAD_REQUEST);

    public static ErrorMessage registerErrorMsg(String errorCode, String errorMsg, HttpStatus httpStatus) {
        return ErrorMessage.create()
                .setErrorCode(errorCode)
                .setErrorMsg(errorMsg)
                .setHttpStatus(httpStatus);
    }

    public static ErrorMessage registerErrorMsg(String errorMsg) {
        return ErrorCode.registerErrorMsg(ErrorCode.errorCode, errorMsg, HttpStatus.BAD_REQUEST);
    }
}
