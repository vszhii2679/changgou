package com.changgou.search.controller;

import com.changgou.entity.Page;
import com.changgou.search.pojo.SkuInfo;
import com.changgou.search.service.Es5Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;

@Controller
@RequestMapping("search")
public class SearchController {


    @Autowired
    private Es5Service es5Service;

    @GetMapping("/searchResult")
    public String search(@RequestParam Map<String, String> queryMap, Model model) {
        //特殊符号处理
        transSpecialCharacter(queryMap);
        Map resultMap = es5Service.searchByQueryMap(queryMap);
        model.addAttribute("resultMap", resultMap);
        model.addAttribute("queryMap", queryMap);

        //封装分页数据并返回
        //1.总记录数
        //2.当前页
        //3.每页显示多少条
        Page<SkuInfo> page = new Page<SkuInfo>(
                Long.parseLong(String.valueOf( resultMap.get("total"))),
                Integer.parseInt(String.valueOf(resultMap.get("pageNum"))),
                Page.pageSize
        );
        model.addAttribute("page",page);

        //获取url+参数： /search/searchResult?keyword=手机&brand=华为
        StringBuilder url = new StringBuilder("/search/searchResult");
        assert queryMap != null && queryMap.size() > 0;
        url.append("?");
        for (String paramKey : queryMap.keySet()) {
            //屏蔽排序的域、排序规则、请求页码，因为前段不会隐藏相关按钮
            if (!"sortField".equals(paramKey) && !"sortMode".equals(paramKey) && !"pageNum".equals(paramKey)) {
                url.append(paramKey).append("=").append(queryMap.get(paramKey)).append("&");
            }
        }
        String urlString = url.toString();
        urlString = urlString.substring(0, urlString.length() - 1);
        model.addAttribute("url",urlString);
        return "/search";
    }


    private void transSpecialCharacter(Map<String, String> queryMap) {
        //处理浏览器解析url后"+"被替换成" "的问题
        for (String key : queryMap.keySet()) {
            if (key.startsWith("spec_")) {
                String value = queryMap.get(key);
                queryMap.put(key, value.replaceAll(" ", "+"));
            }
        }
    }


}
