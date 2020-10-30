package com.changgou.goods.controller;

import com.changgou.entity.PageResult;
import com.changgou.entity.Result;
import com.changgou.goods.pojo.Brand;
import com.changgou.goods.service.BrandService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/*
    品牌信息的控制层
 */

@RestController
@RequestMapping("/brand")
@Api(tags = "品牌管理接口")
@CrossOrigin//设置允许跨域访问
public class BrandController {

    @Autowired
    private BrandService brandService;

    //查询所有
    @ApiOperation(value = "查询所有品牌")
    @GetMapping("/findAll")
    public Result findAll() {
        return brandService.findAll();
    }

    //根据id查询
    @ApiOperation(value = "根据id查询品牌")
    @ApiImplicitParam(name = "id", required = true, value = "主键id")
    @GetMapping("/findById/{id}")
    public Result findAll(@PathVariable("id") int id) {
        return brandService.findById(id);
    }

    //添加单个
    @ApiOperation(value = "添加品牌")
    @PostMapping("/addBrand")
    public Result addBrand(@RequestBody Brand brand) {
        return brandService.addBrand(brand);
    }

    //修改单个
    @ApiOperation(value = "修改品牌")
    @PutMapping("/updateBrand")
    public Result updateBrand(@RequestBody Brand brand) {
        return brandService.updateBrand(brand);
    }

    //根据id删除
    @ApiOperation(value = "根据id删除")
    @ApiImplicitParam(name = "id", value = "主键id", required = true)
    @DeleteMapping("/deleteBrandById/{id}")
    public Result deleteBrandById(@PathVariable("id") int id) {
        return brandService.deleteBrandById(id);
    }

    //多条件查询
    @ApiOperation(value = "多条件查询")
    @GetMapping("/findByQueryString")
    public Result findByQueryString(Brand brand) {
        return brandService.findByQueryString(brand);
    }

    //分页查询
    @ApiOperation(value = "分页查询")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "currentPage",value = "查询页码"),
            @ApiImplicitParam(name = "pageSize",value = "每页条数"),
    })
    @GetMapping("/queryPage/{currentPage}/{pageSize}")
    public PageResult<Brand> queryPage(@PathVariable("currentPage") int currentPage, @PathVariable("pageSize") int pageSize) {
        return brandService.queryPage(currentPage, pageSize);
    }

    //按条件分页查询
    @ApiOperation(value = "按条件分页查询")
    @GetMapping("/queryPageByQueryString/{currentPage}/{pageSize}")
    public PageResult<Brand> queryPageByQueryString(Brand brand, @PathVariable("currentPage") int currentPage, @PathVariable("pageSize") int pageSize) {
        return brandService.queryPageByQueryString(brand, currentPage, pageSize);
    }

    //按分类查询
    @ApiOperation(value = "根据分类查询")
    @GetMapping("/findListByCategoryName/{categoryName}")
    public Result findListByCategoryName(@PathVariable("categoryName") String categoryName){
        return brandService.findListByCategoryName(categoryName);
    }
}
