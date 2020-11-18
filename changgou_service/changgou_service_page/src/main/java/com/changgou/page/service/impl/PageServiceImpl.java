package com.changgou.page.service.impl;

import com.alibaba.fastjson.JSON;
import com.changgou.entity.Result;
import com.changgou.goods.feign.CategoryFeign;
import com.changgou.goods.feign.SkuFeign;
import com.changgou.goods.feign.SpuFeign;
import com.changgou.goods.pojo.Category;
import com.changgou.goods.pojo.Sku;
import com.changgou.goods.pojo.Spu;
import com.changgou.page.service.PageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.*;

@Service
public class PageServiceImpl implements PageService {

    @Autowired
    private CategoryFeign categoryFeign;

    @Autowired
    private SpuFeign spuFeign;

    @Autowired
    private SkuFeign skuFeign;

    @Value(("${pagepath}"))
    private String pagepath;

    @Autowired
    private TemplateEngine templateEngine;


    @Override
    public void createThemeleafPage(String spuId) {
        //1、获取context对象，用于存放商品详情数据
        Context context=new Context();
        //2、自定义方法获取页面信息
        Map<String, Object> dataMap=this.getDataMap(spuId);
        context.setVariables(dataMap);

        //2. 获取商品详情页生成的指定位置
        File fileDir=new File(pagepath);
        if(!fileDir.exists()){
            fileDir.mkdirs();
        }
        //3、定义输出流,进行文件生成
        File file=new File(fileDir+"/"+spuId+".html");
        Writer out = null;
        try{
            out = new FileWriter(file);
            //生成文件
            /**
             * 1.模板名称
             * 2.context对象,包含了模板需要的数据
             * 3.输出流,指定文件输出位置
             */
            templateEngine.process("item",context,out);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            //关闭流
            try {
                if(null!=out){
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private Map<String, Object> getDataMap(String spuId) {
        //1、创建要返回的结果对象
        Map<String,Object> resultMap = new HashMap<>();
        //2、查询spu对象
        Spu spu = spuFeign.findSpuById(spuId).getData();
        resultMap.put("spu",spu);
        //3、获取images
        String[] imageList = spu.getImages().split(",");
        resultMap.put("imageList",imageList);
        //3、查询category对象
        Category category1 = categoryFeign.findById(spu.getCategory1Id()).getData();
        Category category2 = categoryFeign.findById(spu.getCategory2Id()).getData();
        Category category3 = categoryFeign.findById(spu.getCategory3Id()).getData();
        resultMap.put("category1",category1);
        resultMap.put("category2",category2);
        resultMap.put("category3",category3);
        //4、查询skuList
        List<Sku> skuList = skuFeign.getSkuListBySpuId(spuId);
        resultMap.put("skuList",skuList);
        //5、查询spec_items
        String specItems = spu.getSpecItems();
        Map<String,List<String>> specificationList = JSON.parseObject(specItems, Map.class);
/*        List<String> specList = new ArrayList<>();
        for (Sku sku : skuList) {
            String spec = sku.getSpec();
            specList.add(spec);
        }
        Map<String, Set<String>> specificationList = transJson2Set(specList);*/
        resultMap.put("specificationList",specificationList);
        return resultMap;
    }

    /**
     * 将List 转成 Map [{A:B,D:F},{A:C}]--->{A:[B,C]},{D:[F]}
     * @param specList
     * @return
     */
    private Map<String, Set<String>> transJson2Set(List<String> specList){
        //断言：当参数不为null且参数size大于0时向下执行
        assert specList!=null && specList.size()>0;
        //创建返回对象
        Map<String, Set<String>> specMap = new HashMap();
        //遍历list----[{A:B,D:F},{A:C}]，操作{A:B}
        for (String specJson : specList) {
            //将每一个{A:B}转成Map键值对
            Map<String,String> jsonMap = JSON.parseObject(specJson, Map.class);
            //遍历键,内循环完成时候，将key和Set集合存入specMap中
            for (String jsonKey : jsonMap.keySet()) {
                //获取/创建Set集合，用来去重复的value
                Set<String> jsonSet = specMap.get(jsonKey);
                //如果Map中无此Set集合则创建
                if (jsonSet==null){
                    jsonSet=new HashSet<>();
                }
                //将每个value存入Set集合中
                jsonSet.add(jsonMap.get(jsonKey));
                //将key和Set集合存入结果specMap中
                specMap.put(jsonKey,jsonSet);
            }
        }
        return specMap;
    }
}

