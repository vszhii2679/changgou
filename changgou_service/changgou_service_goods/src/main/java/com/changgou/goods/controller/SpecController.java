package com.changgou.goods.controller;

import com.changgou.goods.service.SpecService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/spec")
@Api(tags = "规格接口类")
@CrossOrigin
public class SpecController {

    @Autowired
    private SpecService specService;


    @GetMapping("/findSpecListByCategoryName/{categoryName}")
    @ApiImplicitParam(name = "categoryName",value = "分类名",required = true)
    public List<Map> findSpecListByCategoryName(@PathVariable("categoryName") String categoryName) {
        return specService.findSpecListByCategoryName(categoryName);
    }
}
