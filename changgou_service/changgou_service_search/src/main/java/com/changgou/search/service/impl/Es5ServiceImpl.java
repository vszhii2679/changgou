package com.changgou.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.changgou.goods.feign.SkuFeign;
import com.changgou.goods.pojo.Sku;
import com.changgou.search.mapper.Es5Mapper;
import com.changgou.search.pojo.SkuInfo;
import com.changgou.search.service.Es5Service;
import com.netflix.discovery.converters.jackson.builder.StringInterningAmazonInfoBuilder;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class Es5ServiceImpl implements Es5Service {

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    @Autowired
    private Es5Mapper es5Mapper;

    @Autowired
    private SkuFeign skuFeign;

    @Override
    public void importAll() {
        //1、通过feign远程调用goods服务查询sku集合
        List<Sku> skuList = skuFeign.getSkuListBySpuId("all");
        System.out.println("skuList = " + skuList);
        List<SkuInfo> skuInfos = transSkuIntoSkuInfo(skuList);
        //判断集合中有误数据，无数据不存入或后期调用Spu的mapper存入spu
        if (skuInfos != null && skuInfos.size() > 0) {
            es5Mapper.saveAll(skuInfos);
        }
    }

    @Override
    public void deleteAll() {
        List<Sku> skuList = skuFeign.getSkuListBySpuId("all");
        List<SkuInfo> skuInfos = transSkuIntoSkuInfo(skuList);
        if (skuInfos != null && skuInfos.size() > 0) {
            es5Mapper.deleteAll(skuInfos);
        }
    }

    @Override
    public void createIndex() {
        elasticsearchTemplate.createIndex(SkuInfo.class);
        elasticsearchTemplate.putMapping(SkuInfo.class);
    }

    @Override
    public void importBySpuId(String spuId) {
        List<Sku> skuList = skuFeign.getSkuListBySpuId(spuId);
        List<SkuInfo> skuInfos = transSkuIntoSkuInfo(skuList);
        //判断集合中有误数据，无数据不存入或后期调用Spu的mapper存入spu
        if (skuInfos != null && skuInfos.size() > 0) {
            es5Mapper.saveAll(skuInfos);
        }
    }

    @Override
    public void downBySpuId(String spuId) {
        List<Sku> skuList = skuFeign.getSkuListBySpuId(spuId);
        List<SkuInfo> skuInfos = transSkuIntoSkuInfo(skuList);
        if (skuInfos != null && skuInfos.size() > 0) {
            es5Mapper.deleteAll(skuInfos);
        }
    }

    @Override
    public Map searchByQueryMap(Map<String, String> queryMap) {
        if (queryMap == null) {
            return null;
        }
        //创建结果对象
        Map resultMap = new HashMap();

        //1、创建条件的构建器
        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();
        //2、创建条件对象
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
//设置具体查询条件开始
        //URL:http://127.0.0.1:9009/es5/search?keyword=手机&brand=小米&spec_颜色=白色&spec_版本=8GB+256GB&price=0-500&currentPage=2&pageSize=20
        //5.1、设置条件：输入框中输入的关键字---------------------------
        if (!StringUtils.isEmpty(queryMap.get("keyword"))) {
            boolQuery.must(QueryBuilders.matchQuery("name", queryMap.get("keyword")).operator(Operator.AND));
        }
        //5.2、设置条件：筛选品牌
        String brandName = "brandName";
        if (!StringUtils.isEmpty(queryMap.get("brand"))) {
            boolQuery.filter(QueryBuilders.termQuery(brandName, queryMap.get("brand")));
        }
        //5.3、设置条件：规格过滤
        for (String key : queryMap.keySet()) {
            if (key.startsWith("spec_")) {
                String substring = key.substring(5);//获取从第五位开始的数据
                boolQuery.filter(QueryBuilders.termQuery("specMap." + substring + ".keyword", queryMap.get(key)));
            }
        }
        //5.4、设置条件：价格过滤 0-500 最右无边界
        if (!StringUtils.isEmpty(queryMap.get("price"))) {
            String[] prices = queryMap.get("price").split("-");
            //根据prices的长度来判断有无右边界
            if (prices.length == 2) {
                boolQuery.filter(QueryBuilders.rangeQuery("price").lte(prices[1]));
            }
            boolQuery.filter(QueryBuilders.rangeQuery("price").gte(prices[0]));
        }
        //5.5、设置条件：分页
        int currentPage = 1;
        int pageSize = 50;
        //如果未设置分页设置，设置默认值 第一页、如果有值则从queryMap中获取
        if (!StringUtils.isEmpty(queryMap.get("currentPage"))) {
            currentPage = Integer.parseInt(queryMap.get("currentPage"));
        }
        if (!StringUtils.isEmpty(queryMap.get("pageSize"))) {
            pageSize = Integer.parseInt(queryMap.get("pageSize"));
        }
        //page从第0页开始
        Pageable pageable = PageRequest.of(currentPage - 1, pageSize);
        nativeSearchQueryBuilder.withPageable(pageable);
        //5.6、设置聚合：size可以设置聚合的最大容量，默认为10个
        //5.6.1、品牌聚合查询，terms("brand")中的参数与skuInfos.getAggregation("brand")参数要一致
        nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms("brand").field(brandName).size(20));
        //5.6.2、规格聚合查询
        nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms("spec").field("spec.keyword").size(20));
        //5.7、设置条件：排序
        if(!StringUtils.isEmpty("sortField")){
            if("asc".equals(queryMap.get("sortMode"))){
                nativeSearchQueryBuilder.withSort(SortBuilders.fieldSort(queryMap.get("sortField")).order(SortOrder.ASC));
            }else if ("desc".equals(queryMap.get("sortMode"))){
                nativeSearchQueryBuilder.withSort(SortBuilders.fieldSort(queryMap.get("sortField")).order(SortOrder.DESC));
            }
        }
        //5.8、设置关键字高亮
        HighlightBuilder.Field highlightFields = new HighlightBuilder.Field("name");
        //5.8.1设置高亮域样式
        highlightFields.preTags("<span style=\"color:red\">").postTags("</span>");
        nativeSearchQueryBuilder.withHighlightFields(highlightFields);

//设置具体查询条件结束
        //3、构建器设置查询条件对象
        nativeSearchQueryBuilder.withQuery(boolQuery);
        //4、获取结果
        /**
         * queryForPage(SearchQuery query, Class<T> clazz, SearchResultMapper mapper)
         * SearchQuery：查询条件对象
         * clazz：实体
         * SearchResultMapper：查询结果映射
         */
        AggregatedPage<SkuInfo> skuInfos = elasticsearchTemplate.queryForPage(nativeSearchQueryBuilder.build(), SkuInfo.class, new SearchResultMapper() {
            @Override
            public <T> AggregatedPage<T> mapResults(SearchResponse searchResponse, Class<T> aClass, Pageable pageable) {
                //4.1定义一个list集合，用于存储查询的具体结果
                List<SkuInfo> list = new ArrayList<>();
                //4.2获取命中结果
                SearchHits hits = searchResponse.getHits();
                for (SearchHit hit : hits) {
                    //4.2.1 获取具体命中结果的数据并转化成SkuInfo实体,封装进list集合中
                    String source = hit.getSourceAsString();
                    SkuInfo skuInfo = JSON.parseObject(source, SkuInfo.class);
                    //5.8.2将高亮结果替换成SkuInfo中name的内容
                    Map<String, HighlightField> fields = hit.getHighlightFields();
                    if (fields!=null && fields.size()>0){
                        Text[] names = fields.get("name").getFragments();
                        /*
                        names.length为1，与关键字有几个无关
                        System.out.println("【---】 " );
                        System.out.println(" names.length = " + names.length);
                        System.out.println(" names[0] = " + names[0].toString());
                        */
                        skuInfo.setName(names[0].toString());
                    }
                    list.add(skuInfo);
                }
                //将封装好的查询结果返回
                /**
                 * AggregatedPageImpl(List<T> content, Pageable pageable, long total, Aggregations aggregations)
                 * content:查询的结果
                 * pageable：分页信息
                 * total：总记录数
                 * aggregations：聚合（类似于mysql的分组）
                 */
                return new AggregatedPageImpl(list, pageable, hits.getTotalHits(), searchResponse.getAggregations());
            }
        });

        //6、设置返回的数据
        resultMap.put("total", skuInfos.getTotalElements());
        resultMap.put("skuList", skuInfos.getContent());
        resultMap.put("totalPage", skuInfos.getTotalPages());
        resultMap.put("currentPage",currentPage);
        resultMap.put("pageSize",pageSize);
        //6.1、设置聚合的结果
        //getAggregation是AggregationBuilders.terms("brand")设置的brand字符串
        //Aggregation中无直接获取list的方法，强转为子类StringTerms
        StringTerms brandAggregation = (StringTerms) skuInfos.getAggregation("brand");
        StringTerms specAggregation = (StringTerms) skuInfos.getAggregation("spec");
        //从brandAggregation中获取list集合，通过stream流换成map集合，再将map集合转成
        List<String> brandList = brandAggregation.getBuckets().stream()
                .map(bucket -> bucket.getKeyAsString())
                .collect(Collectors.toList());
        List<String> specList = specAggregation.getBuckets().stream()
                .map(bucket -> bucket.getKeyAsString())
                .collect(Collectors.toList());
        resultMap.put("brandList",brandList);
        resultMap.put("specList",specList);
        return resultMap;
    }

    /**
     * 将List<Sku>转换成List<SkuInfo>并封装spcMap数据
     *
     * @param skuList
     * @return
     */
    private List<SkuInfo> transSkuIntoSkuInfo(List<Sku> skuList) {
        //int i = 1/0;//模拟异常
        //处理集合为空或不存在的情况
        if (skuList == null || skuList.size() <= 0) {
            return null;
            //throw new RuntimeException("当前没有数据被查询到,无法导入索引库");
        }
        //2、为了将Sku类型的数据转成SkuInfo类型，将skuList转换成json，再解析成SkuInfo ，SkuInfo是elasticsearch中的文档类型
        String jsonString = JSON.toJSONString(skuList);
        List<SkuInfo> skuInfos = JSON.parseArray(jsonString, SkuInfo.class);
        System.out.println("skuInfos = " + skuInfos);
        //3、将spec数据解析生成map存入SkuInfo的specMap字段中
        for (SkuInfo skuInfo : skuInfos) {
            Map map = JSON.parseObject(skuInfo.getSpec(), Map.class);
            skuInfo.setSpecMap(map);
        }
        return skuInfos;
    }
}
