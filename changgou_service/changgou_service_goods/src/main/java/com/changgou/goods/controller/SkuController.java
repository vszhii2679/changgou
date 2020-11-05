package com.changgou.goods.controller;

import com.changgou.goods.pojo.Sku;
import com.changgou.goods.service.SkuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
    sku的对外接口
 */
@RestController
@RequestMapping("/sku")
public class SkuController {

    @Autowired
    private SkuService skuService;


    @GetMapping("/spu/getSkuListBySpuId/{spuId}")
    public List<Sku> getSkuListBySpuId(@PathVariable("spuId") String spuId) {
        Map<String,Object> map = new HashMap<>();
        //后期可以传all用来获取全部sku
        if(!"all".equals(spuId)){
            map.put("spuId",spuId);
        }
        map.put("status","1");
        List<Sku> skuList = skuService.getSkuListBySpuId(map);
        return skuList;
    }


}
