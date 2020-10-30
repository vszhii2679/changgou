package com.changgou.goods.service.impl;

import com.changgou.entity.PageResult;
import com.changgou.entity.Result;
import com.changgou.entity.StatusCode;
import com.changgou.goods.mapper.BrandMapper;
import com.changgou.goods.pojo.Brand;
import com.changgou.goods.service.BrandService;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

/*
    品牌信息的业务类
 */
@Service
public class BrandServiceImpl implements BrandService {

    @Autowired
    private BrandMapper brandMapper;

    @Override
    public Result findAll() {
        List<Brand> brandList = brandMapper.selectAll();
        if (brandList != null && brandList.size() > 0) {
            return new Result(true, StatusCode.OK, "查询成功", brandList);
        }
        return new Result(false, StatusCode.ERROR, "查无此信息");
    }

    @Override
    public Result findById(int id) {
        Brand brand = brandMapper.selectByPrimaryKey(id);
        if (brand != null) {
            return new Result(true, StatusCode.OK, "查询成功", brand);
        }
        return new Result(false, StatusCode.ERROR, "查无此信息");
    }

    @Override
    public Result addBrand(Brand brand) {
        //insert()和insertSelective()区别:后者会进行控制判断，只存入非空的字段
        int insert = brandMapper.insertSelective(brand);
        System.out.println("insert = " + insert);
        if (insert > 0) {
            return new Result(true, StatusCode.OK, "新增成功");
        }
        return new Result(false, StatusCode.ERROR, "新增失败");
    }

    @Override
    public Result updateBrand(Brand brand) {
        int update = brandMapper.updateByPrimaryKey(brand);
        if (update > 0) {
            return new Result(true, StatusCode.OK, "修改成功");
        }
        return new Result(false, StatusCode.ERROR, "修改失败");
    }

    @Override
    public Result deleteBrandById(int id) {
        int delete = brandMapper.deleteByPrimaryKey(id);
        if (delete > 0) {
            return new Result(true, StatusCode.OK, "删除成功");
        }
        return new Result(false, StatusCode.ERROR, "删除失败");
    }

    @Override
    public Result findByQueryString(Brand brand) {
        //2、获取条件对象
        Example example = getExample(brand);
        //1、调用多条件查询方法，传入条件对象
        List<Brand> brandList = brandMapper.selectByExample(example);
        if (brandList != null && brandList.size() > 0) {
            return new Result(true, StatusCode.OK, "查询成功", brandList);
        }
        return new Result(false, StatusCode.ERROR, "查无此信息");
    }

    @Override
    public PageResult<Brand> queryPage(int currentPage, int pageSize) {
        PageHelper.startPage(currentPage, pageSize);
        List<Brand> brandList = brandMapper.selectAll();
        if (brandList != null && brandList.size() > 0) {
            return new PageResult<Brand>((long) brandList.size(), brandList);
        }
        return null;
    }

    @Override
    public PageResult<Brand> queryPageByQueryString(Brand brand, int currentPage, int pageSize) {
        //1、开启分页功能
        PageHelper.startPage(currentPage, pageSize);
        //3、获取条件对象
        Example example = getExample(brand);
        //2、调用多条件查询方法，传入条件对象
        List<Brand> brandList = brandMapper.selectByExample(example);

        if (brandList != null && brandList.size() > 0) {
            return new PageResult<Brand>((long) brandList.size(), brandList);
        }
        return null;
    }


    @Override
    public Result findListByCategoryName(String categoryName) {
        List<Brand> brandList = brandMapper.findListByCategoryName(categoryName);
        if (brandList != null && brandList.size() > 0) {
            return new Result(true, StatusCode.OK, "查询成功", brandList);
        }
        return new Result(false, StatusCode.ERROR, "查无此信息");
    }

    /*
        品牌业务用：获取条件对象
     */
    private Example getExample(Brand brand) {
        //3、创建条件对象
        Example example = new Example(Brand.class);
        //4、设置条件
        Example.Criteria criteria = example.createCriteria();
        if (brand.getName() != null && !"".equals(brand.getName())) {
            //4.1 条件类型:andEqualTo全等、andLike模糊
            criteria.andEqualTo("name", brand.getName());
        }
        if (brand.getLetter() != null && !"".equals(brand.getLetter())) {
            criteria.andEqualTo("letter", brand.getLetter());
        }
        if (brand.getImage() != null && !"".equals(brand.getImage())) {
            criteria.andEqualTo("image", brand.getImage());
        }
        if (brand.getId() != null && !"".equals(brand.getId())) {
            criteria.andEqualTo("id", brand.getId());
        }
        return example;
    }


}
