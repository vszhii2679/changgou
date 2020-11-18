package com.changgou.goods.controller;

import com.changgou.entity.Result;
import com.changgou.entity.StatusCode;
import com.changgou.goods.pojo.Sku;
import com.changgou.goods.service.SkuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
        Map<String, Object> map = new HashMap<>();
        //后期可以传all用来获取全部sku
        if (!"all".equals(spuId)) {
            map.put("spuId", spuId);
        }
        map.put("status", "1");
        List<Sku> skuList = skuService.getSkuListBySpuId(map);
        return skuList;
    }

    @GetMapping("/{skuId}")
    public Result<Sku> getById(@PathVariable("skuId") String skuId) {
        Sku sku = skuService.getSkuById(skuId);
        return new Result<>(true, StatusCode.OK, "获取成功", sku);
    }

    @PostMapping("/decStock")
    public Result decStock(@RequestParam("username") String username) {
        skuService.decStock(username);
        return new Result(true, StatusCode.OK, "decStock成功");
    }


    @PostMapping("/incrStock/{skuId}/{num}")
    public Result incrStock(@PathVariable("skuId") String skuId, @PathVariable("num") Integer num) {
        skuService.incrStock(skuId,num);
        return new Result(true,StatusCode.OK,"还原库存成功");
    }

}
