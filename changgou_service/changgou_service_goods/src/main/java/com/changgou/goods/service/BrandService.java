package com.changgou.goods.service;

import com.changgou.entity.PageResult;
import com.changgou.entity.Result;
import com.changgou.goods.pojo.Brand;

public interface BrandService {

    /**
     * 查询所有品牌信息
     *
     * @return
     */
    Result findAll();

    /**
     * 根据id查询品牌信息
     *
     * @param id
     * @return
     */
    Result findById(int id);

    /**
     * 添加品牌信息
     *
     * @param brand
     * @return
     */
    Result addBrand(Brand brand);

    /**
     * 修改品牌信息
     *
     * @param brand
     * @return
     */
    Result updateBrand(Brand brand);


    /**
     * 根据id删除品牌信息
     *
     * @param id
     * @return
     */
    Result deleteBrandById(int id);

    /**
     * 多条件查询品牌信息
     *
     * @param brand
     * @return
     */
    Result findByQueryString(Brand brand);


    /**
     * 分页查询
     *
     * @return
     */
    PageResult<Brand> queryPage(int currentPage, int pageSize);


    /**
     * 按条件分页查询
     *
     * @return
     */
    PageResult<Brand> queryPageByQueryString(Brand brand,int currentPage, int pageSize);

}
