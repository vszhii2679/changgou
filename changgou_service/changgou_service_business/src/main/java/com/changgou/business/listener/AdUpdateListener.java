package com.changgou.business.listener;

import com.changgou.business.config.RabbitmqConfig;
import okhttp3.*;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Map;

/*
    广告监听类
 */
@Component
public class AdUpdateListener {

    @RabbitListener(queues = RabbitmqConfig.AD_UPDATE_QUEUE)
    public void adUpdate(String message) {
        System.out.println("【监听类方法执行】:");
        System.out.println("message = " + message);
        //String url = "http://192.168.200.128/ad_update?position=" + message;


        //1、实例化OKHttpClient对象
        OkHttpClient httpClient = new OkHttpClient();
        //4、设置url地址
        String url = "http://192.168.200.128/ad_update?position="+message;
        //3、创建请求对象
        Request request = new Request.Builder().url(url).build();
        //2、发送请求
        Call call = httpClient.newCall(request);
        //5、响应结果：这里无结果需要返回
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                System.out.println("结果 = " + response.message());
            }
        });
    }
}
