package com.github.ittalks.invoice;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.ittalks.invoice.handler.InvoiceHandler;
import com.github.ittalks.invoice.handler.QrcodeHandler;
import com.github.ittalks.invoice.model.InvoiceQRInfo;
import com.github.ittalks.invoice.net.Error;
import com.github.ittalks.invoice.net.LeShuiService;
import com.github.ittalks.invoice.net.LeShuiServiceProxy;
import com.github.ittalks.invoice.net.Success;
import org.apache.commons.lang3.StringUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
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

    public void parsePdfByQrcode(String file) throws FileNotFoundException {
        List<BufferedImage> images = InvoiceHandler.getQrcode(file);
        if (images != null && images.size() > 0) {
            for (BufferedImage bufferedImage : images) {
                String qrcode = QrcodeHandler.getQrcode(bufferedImage);
                if (!StringUtils.isEmpty(qrcode)) {
                    LeShuiService leshuiClient = LeShuiServiceProxy.getInstance();
                    InvoiceQRInfo qrInfo = new InvoiceQRInfo(qrcode);
                    LeshuiRequest leshuiRequest = new LeshuiRequest(qrInfo);
                    Call<JSONObject> result = leshuiClient.getInfo(leshuiRequest);

                    result.enqueue(new Callback<JSONObject>() {
                        public void onResponse(Call<JSONObject> call, Response<JSONObject> response) {
                            mSuccessCallBack.Success(response.body());
                        }

                        public void onFailure(Call<JSONObject> call, Throwable throwable) {
                            mErrorCallBack.Error("Request leshui server fail", throwable);
                        }
                    });
                }
            }
        }
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
