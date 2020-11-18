package com.changgou.page.listener;

import com.changgou.page.config.RabbitmqConfig;
import com.changgou.page.service.PageService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PageListener {

    @Autowired
    private PageService pageService;

    @RabbitListener(queues = RabbitmqConfig.PAGE_CREATE_QUEUE)
    public void pageCreate(String spuId){
        pageService.createThemeleafPage(spuId);
    }
}
