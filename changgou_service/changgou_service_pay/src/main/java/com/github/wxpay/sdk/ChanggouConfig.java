package com.github.wxpay.sdk;

import java.io.InputStream;

public class ChanggouConfig extends WXPayConfig {
    //配置公众账号id
    @Override
    String getAppID() {
        return "wx8397f8696b538317";
    }

    //配置商户号id
    @Override
    String getMchID() {
        return "1473426802";
    }

    //配置秘钥
    @Override
    String getKey() {
        return "T6m9iK73b0kn9g5v426MKfHQH7X8rKwb";
    }

    //流
    @Override
    InputStream getCertStream() {
        return null;
    }

    //配置域名管理
    @Override
    IWXPayDomain getWXPayDomain() {
        return new IWXPayDomain() {
            @Override
            public void report(String domain, long elapsedTimeMillis, Exception ex) {
                //...
            }

            @Override
            public DomainInfo getDomain(WXPayConfig config) {
                //配置微信支付接口，注意只能通过post发送请求
                return new DomainInfo("api.mch.weixin.qq.com",true);
            }
        };
    }
}
