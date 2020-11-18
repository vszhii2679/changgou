package com.changgou.search.controller;

import com.changgou.entity.Result;
import com.changgou.entity.StatusCode;
import com.changgou.search.service.Es5Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequestMapping("/es5")
//@CrossOrigin
public class Es5Controller {

    @Autowired
    private Es5Service es5Service;

    @PostMapping("/importAll")
    @ResponseBody
    public Result importAll() {
        es5Service.importAll();
        return new Result(true, StatusCode.OK, "批量导入成功");
    }

    @PostMapping("/createIndex")
    @ResponseBody
    public Result createIndex() {
        //int i = 1/0;//模拟异常
        es5Service.createIndex();
        return new Result(true, StatusCode.OK, "创建索引成功");
    }

    @DeleteMapping("/deleteAll")
    @ResponseBody
    public Result deleteAll() {
        es5Service.deleteAll();
        return new Result(true, StatusCode.OK, "批量删除成功");
    }

    @GetMapping("/searchTest")
    @ResponseBody
    public Result searchByQueryMap(@RequestParam Map<String, String> queryMap){
        transSpecialCharacter(queryMap);
        Map map = es5Service.searchByQueryMap(queryMap);
        return new Result(true,StatusCode.OK,"查询成功",map);
    }

    @GetMapping("/search")
    public String search(@RequestParam Map<String, String> queryMap, Model model){
        transSpecialCharacter(queryMap);
        Map resultMap = es5Service.searchByQueryMap(queryMap);
        model.addAttribute("resultMap",resultMap);
        model.addAttribute("queryMap",queryMap);
        return "search";
    }

    private void transSpecialCharacter(Map<String, String> queryMap){
        //处理浏览器解析url后"+"被替换成" "的问题
        for(String key: queryMap.keySet()){
            if (key.startsWith("spec_")){
                String value = queryMap.get(key);
                queryMap.put(key,value.replaceAll(" ","+"));
            }
        }
    }
}
