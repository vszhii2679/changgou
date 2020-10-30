package com.changgou.goods.service.impl;

import com.changgou.goods.mapper.SpecMapper;
import com.changgou.goods.service.SpecService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class SpecServiceImpl implements SpecService {

    @Autowired
    private SpecMapper specMapper;

    @Override
    public List<Map> findSpecListByCategoryName(String categoryName) {
        List<Map> mapList = specMapper.findSpecListByCategoryName(categoryName);
        for (Map map : mapList) {
            String[] options = ((String) map.get("options")).split(",");
            map.put("options",options);
        }
        return mapList;
    }
}
