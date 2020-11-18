package com.changgou.goods.controller;

import com.changgou.entity.Result;
import com.changgou.entity.StatusCode;
import com.changgou.goods.pojo.Goods;
import com.changgou.goods.pojo.Spu;
import com.changgou.goods.service.SpuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/spu")
public class SpuController {

    @Autowired
    private SpuService spuService;

    //添加商品
    @PostMapping("/add")
    public Result add(@RequestBody Goods goods) {
        spuService.add(goods);
        return new Result(true, StatusCode.OK, "商品添加成功");
    }

    //根据id查询商品
    @GetMapping("/findById/{id}")
    public Result findById(@PathVariable("id") String id) {
        Goods goods = spuService.findById(id);
        return new Result(true, StatusCode.OK, "商品查询成功", goods);
    }

    //根据id查询商品
    @GetMapping("/findSpuById/{id}")
    public Result findSpuById(@PathVariable("id") String id) {
        //spuService
        Spu spu = spuService.findSpuById(id);
        return new Result(true, StatusCode.OK, "spu查询成功", spu);
    }

    //根据id编辑商品信息
    @PutMapping("/editById")
    public Result editById(@RequestBody Goods goods) {
        spuService.editById(goods);
        return new Result(true, StatusCode.OK, "商品修改成功");
    }

    //根据id修改商品审核状态
    @PutMapping("/audit/{id}/{status}")
    public Result audit(@PathVariable("id") String id, @PathVariable("status") String status) {
        spuService.audit(id, status);
        return new Result(true, StatusCode.OK, "商品审核状态修改成功");
    }

    //根据id下架商品
    @PutMapping("/pull/{id}")
    public Result pull(@PathVariable("id") String id) {
        spuService.pull(id);
        return new Result(true, StatusCode.OK, "商品下架成功");
    }

    @PutMapping("/push/{id}")
    public Result push(@PathVariable("id") String id) {
        spuService.push(id);
        return new Result(true, StatusCode.OK, "商品上架成功");
    }

    @PutMapping("/deleteById/{id}")
    public Result deleteById(@PathVariable("id") String id) {
        spuService.deleteById(id);
        return new Result(true, StatusCode.OK, "商品删除成功，可进入回收站进行恢复!");
    }

    @PutMapping("/restoreById/{id}")
    public Result restoreById(@PathVariable("id") String id) {
        spuService.restoreById(id);
        return new Result(true, StatusCode.OK, "商品恢复成功!");
    }

    @DeleteMapping("/realDel/{id}")
    public Result realDel(@PathVariable("id") String id) {
        spuService.realDel(id);
        return new Result(true, StatusCode.OK, "商品已删除成功!");
    }
}
