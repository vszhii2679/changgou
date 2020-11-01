package com.changgou.goods.service.impl;

import com.alibaba.fastjson.JSON;
import com.changgou.goods.mapper.*;
import com.changgou.goods.pojo.*;
import com.changgou.goods.service.SkuService;
import com.changgou.goods.service.SpuService;
import com.changgou.utils.IdWorker;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class SpuServiceImpl implements SpuService {

    @Autowired
    private SpuMapper spuMapper;

    @Autowired
    private SkuMapper skuMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private BrandMapper brandMapper;

    @Autowired
    private CategoryBrandMapper categoryBrandMapper;

    @Autowired
    private IdWorker idWorker;


    @Transactional
    @Override
    public void add(Goods goods) {
        //1、添加spu信息
        Spu spu = goods.getSpu();
        if (spu == null) {
            throw new RuntimeException("请填写完整商品信息!");
        }
        long id = idWorker.nextId();//通过雪花算法生成唯一ID
        spu.setId(id + "");//设置id
        spu.setIsDelete("0");//设置逻辑删除状态
        spu.setStatus("0");//设置审核状态
        spu.setIsMarketable("0");//设置上架状态
        spuMapper.insertSelective(spu);

        //2、添加sku信息，传入的是goods，内部的spu已存入且拥有id
        addSkuList(goods);

    }

    //私有方法
    private void addSkuList(Goods goods) {
        List<Sku> skuList = goods.getSkuList();
        //如果不存在sku数据，直接返回
        if (skuList == null || skuList.size() == 0) {
            return;
        }
        //如果存在sku
        Spu spu = goods.getSpu();
        Date date = new Date();
        //根据spu查询第三分类对象 查询品牌对象
        Integer category3Id = spu.getCategory3Id();
        Integer brandId = spu.getBrandId();
        if (brandId == null || category3Id == null) {
            throw new RuntimeException("请填写完整商品信息!");
        }
        Category category = categoryMapper.selectByPrimaryKey(category3Id);
        Brand brand = brandMapper.selectByPrimaryKey(brandId);
        if (category == null || brand == null) {
            throw new RuntimeException("分类信息或者品牌信息为空，请联系管理员~");
        }
        //遍历集合，循环设置内容后存入
        skuList.forEach(sku -> {
            sku.setId(idWorker.nextId() + "");//生成sku id
            sku.setSpuId(spu.getId());//设置spu id
            sku.setCreateTime(date);//设置生成日期
            sku.setUpdateTime(date);//设置修改日期
            sku.setBrandName(brand.getName());//设置品牌名称
            sku.setCategoryName(category.getName());//设置第三分类名称
            sku.setCategoryId(category.getId());//设置第三分类id
            if (StringUtils.isEmpty(sku.getSpec())) {
                sku.setSpec("{}");//如果规格信息为空，存入一个空json
            }
            StringBuilder name = new StringBuilder(spu.getName());//获取spu标准商品单元的name属性进行拼接
            Map<String, String> map = JSON.parseObject(sku.getSpec(), Map.class);//将sku中的spec规格json转成map集合方便拼接
            //遍历spec的值集合，将具体的规格信息拼接到sku name中
            for (String s : map.values()) {
                //拼接一个空格
                name.append(" ").append(s);
            }
            sku.setName(name.toString());//设置sku name
            skuMapper.insertSelective(sku);
        });

        //3、向品牌-分类中间表中插入关联信息
        CategoryBrand categoryBrand = new CategoryBrand();
        categoryBrand.setBrandId(brandId);
        categoryBrand.setCategoryId(category3Id);
        int count = categoryBrandMapper.selectCount(categoryBrand);
        if (count == 0) {
            categoryBrandMapper.insert(categoryBrand);
        }
    }

    @Override
    public Goods findById(String id) {
        Goods goods = new Goods();
        //1、查询获取spu
        Spu spu = spuMapper.selectByPrimaryKey(id);
        //2、查询获取sku
        Example example = new Example(Sku.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("spuId", id);
        List<Sku> skuList = skuMapper.selectByExample(example);
        //3、为goods赋值
        goods.setSpu(spu);
        goods.setSkuList(skuList);
        return goods;
    }

    @Transactional
    @Override
    public void editById(Goods goods) {
        Spu spu = goods.getSpu();
        if (spu == null) {
            throw new RuntimeException("商品不存在!");
        }
        //编辑时与新增不同，需要传入id
        spuMapper.updateByPrimaryKeySelective(spu);//根据id修改spu信息
        //spuMapper.updateByPrimaryKey(spu);
        Example example = new Example(Sku.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("spuId", spu.getId());
        skuMapper.deleteByExample(example);
        addSkuList(goods);
    }

    @Transactional
    @Override
    public void audit(String id, String status) {
        Spu spu = spuMapper.selectByPrimaryKey(id);
        if (spu == null) {
            throw new RuntimeException("商品不存在!");
        }
        //如果spu处于逻辑删除的状态，抛出异常
        if ("1".equals(spu.getIsDelete())) {
            throw new RuntimeException("回收站中的商品需要恢复后才能上架!");
        }
        spu.setStatus(status);//设置审核状态为已审核
        spuMapper.updateByPrimaryKeySelective(spu);//更新spu数据
    }

    @Transactional
    @Override
    public void pull(String id) {
        Spu spu = spuMapper.selectByPrimaryKey(id);
        if (spu == null) {
            throw new RuntimeException("商品不存在!");
        }
        if("1".equals(spu.getIsDelete())){
            throw new RuntimeException("回收站中的商品需要恢复后才能操作!");
        }
        spu.setIsMarketable("0");//设置商品下架
        spuMapper.updateByPrimaryKeySelective(spu);//更新spu数据
    }

    @Transactional
    @Override
    public void push(String id) {
        Spu spu = spuMapper.selectByPrimaryKey(id);
        if (spu == null) {
            throw new RuntimeException("商品不存在!");
        }
        if("1".equals(spu.getIsDelete())){
            throw new RuntimeException("回收站中的商品需要恢复后才能操作!");
        }
        if("0".equals(spu.getStatus())){
            throw new RuntimeException("未审核的商品,请审核后再上架!");
        }
        spu.setIsMarketable("1");
        spuMapper.updateByPrimaryKeySelective(spu);
    }

    @Transactional
    @Override
    public void deleteById(String id) {
        Spu spu = spuMapper.selectByPrimaryKey(id);
        if (spu == null) {
            throw new RuntimeException("商品不存在!");
        }
        if("1".equals(spu.getIsMarketable())){
            throw new RuntimeException("商品处于上架状态，请下架后再删除!");
        }
        spu.setStatus("0");//修改审核状态为未审核
        spu.setIsDelete("1");//修改删除标记状态为已删除
        spuMapper.updateByPrimaryKeySelective(spu);
    }

    @Transactional
    @Override
    public void restoreById(String id) {
        Spu spu = spuMapper.selectByPrimaryKey(id);
        if (spu == null) {
            throw new RuntimeException("商品不存在!");
        }
        if(!"1".equals(spu.getIsDelete())){
            throw new RuntimeException("商品未被删除!");
        }
        spu.setIsDelete("0");//清除商品删除标记
        spu.setStatus("0");//设置审核状态为未审核
        spu.setIsMarketable("0");//设置上架状态为未上架
        spuMapper.updateByPrimaryKeySelective(spu);//更新商品状态
    }

    @Transactional
    @Override
    public void realDel(String id) {
        Spu spu = spuMapper.selectByPrimaryKey(id);
        if (spu == null) {
            throw new RuntimeException("商品不存在!");
        }
        if(!"1".equals(spu.getIsDelete())){
            throw new RuntimeException("商品未被标记为删除状态，不可删除!");
        }
        spuMapper.deleteByPrimaryKey(id);
        Example example = new Example(Sku.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("spuId",id);
        skuMapper.deleteByExample(example);
    }
}
