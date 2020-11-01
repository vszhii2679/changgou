package com.changgou.goods.pojo;

import javax.persistence.Id;
import javax.persistence.Table;

/*
    品牌与分类的中间表，使用反三范式，相关多对多关系中属于冗余字段，方便后期的品牌与分类的关联查询
 */
@Table(name = "tb_category_brand")
public class CategoryBrand {
    //联合主键需要同时添加id注解
    //分类id
    @Id
    private Integer categoryId;

    //品牌id
    @Id
    private Integer brandId;

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public Integer getBrandId() {
        return brandId;
    }

    public void setBrandId(Integer brandId) {
        this.brandId = brandId;
    }
}
