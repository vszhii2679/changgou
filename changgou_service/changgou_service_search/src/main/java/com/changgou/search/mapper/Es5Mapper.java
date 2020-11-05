package com.changgou.search.mapper;

import com.changgou.search.pojo.SkuInfo;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface Es5Mapper extends ElasticsearchRepository<SkuInfo,Long> {
}
