package com.changgou.web.controller;

import com.changgou.entity.Result;
import com.changgou.entity.StatusCode;
import com.changgou.seckill.feign.SecKillFeign;
import com.changgou.seckill.pojo.SeckillGoods;
import com.changgou.utils.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping("/wseckillgoods")
public class WsecKillGoodsController {

    @Autowired
    private SecKillFeign secKillFeign;

    //跳转到秒杀页面
    @GetMapping("/toIndex")
    public String getTime() {
        return "seckill-index";
    }

    //获取时间菜单
    @GetMapping("/timeMenu")
    @ResponseBody
    public Result timeMenu() {
        //转换成字符串响应给页面
        List<String> response = new ArrayList<>(5);
        List<Date> dateMenus = DateUtil.getDateMenus();
        for (Date dateMenu : dateMenus) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String date = dateFormat.format(dateMenu);
            response.add(date);
        }
        return new Result(true, StatusCode.OK, "获取时间菜单成功", response);
    }

    //根据时间段展示秒杀商品信息
    @GetMapping("/list")
    @ResponseBody
    public Result list(String time){
        //从redis中获取对应时间的商品信息
        //将日期yyyy-MM-dd HH:mm:ss 转换为 yyyyMMddHHmmss
        Result<List<SeckillGoods>> listResult = secKillFeign.list(DateUtil.formatStr(time));
        return listResult;
    }
}
