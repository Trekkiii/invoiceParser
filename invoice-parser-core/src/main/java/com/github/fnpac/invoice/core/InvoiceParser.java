package com.github.fnpac.invoice.core;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.fnpac.invoice.common.exception.NestedException;
import com.github.fnpac.invoice.core.handler.InvoiceHandler;
import com.github.fnpac.invoice.core.handler.QrcodeHandler;
import com.github.fnpac.invoice.core.model.InvoiceInfo;
import com.github.fnpac.invoice.core.net.LeShuiService;
import com.github.fnpac.invoice.core.net.LeShuiServiceProxy;
import com.github.fnpac.invoice.core.net.Success;
import com.github.fnpac.invoice.core.model.InvoiceQRInfo;
import com.github.fnpac.invoice.core.net.Error;
import org.apache.commons.lang3.StringUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by 刘春龙 on 2017/9/6.
 */
public class InvoiceParser {

    private static final Logger logger = Logger.getLogger(InvoiceParser.class.getName());

    private Success mSuccessCallBack;
    private Error mErrorCallBack;

    private InvoiceParser() {
        this.mSuccessCallBack = c -> {
        };
        this.mErrorCallBack = c -> {
        };
    }

    private InvoiceParser(Success mSuccessCallBack, Error mErrorCallBack) {
        this.mSuccessCallBack = mSuccessCallBack;
        this.mErrorCallBack = mErrorCallBack;
    }

    public static class Builder {
        private Success mSuccessCallBack;
        private Error mErrorCallBack;

        public Builder success(Success success) {
            this.mSuccessCallBack = success;
            return this;
        }

        public Builder error(Error error) {
            this.mErrorCallBack = error;
            return this;
        }

        public InvoiceParser build() {
            if (mSuccessCallBack != null || mErrorCallBack != null) {
                return new InvoiceParser(mSuccessCallBack, mErrorCallBack);
            }
            return new InvoiceParser();
        }
    }

    /**
     * 解析pdf二维码获取发票信息
     * @param file 文件路径
     * @param isAsync 是否异步处理
     * @return
     * @throws FileNotFoundException
     */
    public JSONObject parsePdfByQrcode(String file, boolean isAsync) throws FileNotFoundException {
        List<BufferedImage> images = InvoiceHandler.getQrcode(file);
        if (images != null && images.size() > 0) {
            for (BufferedImage bufferedImage : images) {
                String qrcode = QrcodeHandler.getQrcode(bufferedImage);
                if (!StringUtils.isEmpty(qrcode)) {
                    LeShuiService leshuiClient = LeShuiServiceProxy.getInstance();
                    InvoiceQRInfo qrInfo = new InvoiceQRInfo(qrcode);
                    LeshuiRequest leshuiRequest = new LeshuiRequest(qrInfo);
                    Call<JSONObject> result = leshuiClient.getInfo(leshuiRequest);

                    if (isAsync) {
                        leshuiRequestAsync(result);
                        return null;
                    }
                    return leshuiRequestSync(result);
                }
            }
        }
        return null;
    }

    /**
     * 解析pdf文本获取发票信息
     * @param file 文件路径
     * @return
     * @throws FileNotFoundException
     * @throws NestedException
     */
    public InvoiceInfo parsePdfByText(String file) throws FileNotFoundException, NestedException {
        return InvoiceHandler.getInvoice(file);
    }

    private void leshuiRequestAsync(Call<JSONObject> result) {
        result.enqueue(new Callback<JSONObject>() {
            public void onResponse(Call<JSONObject> call, Response<JSONObject> response) {
                mSuccessCallBack.Success(response.body());
            }

            public void onFailure(Call<JSONObject> call, Throwable throwable) {
                mErrorCallBack.Error("Request leshui server fail", throwable);
            }
        });
    }

    private JSONObject leshuiRequestSync(Call<JSONObject> result) {
        try {
            Response<JSONObject> response = result.execute();
            return response.body();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    public static class LeshuiRequestData {
        String fpdm, FPDM;// 发票代码
        String fphm, FPHM;// 发票号码
        String kprq,// 开票日期
                fpje,// 发票金额?
                yzm,// 验证码?
                yzmSj, loginSj, token, username, index;
        @JsonProperty(value = "new")
        String _new;// ?

        public LeshuiRequestData(InvoiceQRInfo invoiceQRInfo) {
            this.fpdm = this.FPDM = invoiceQRInfo.getInvoiceCode();
            this.fphm = this.FPHM = invoiceQRInfo.getInvoiceNumber();
            this.kprq = invoiceQRInfo.getBillTime();
            this.fpje = invoiceQRInfo.getShortCheckCode();
            this.yzm = "yzm";
            this._new = "1";
        }
    }

    public static class LeshuiRequest {
        private String service = "newinvoice.service.invoiceService";
        private String method = "getInvoiceInfo";
        private LeshuiRequestData data;

        public LeshuiRequest(InvoiceQRInfo invoiceQRInfo) {
            this.data = new LeshuiRequestData(invoiceQRInfo);
        }
    }
}
