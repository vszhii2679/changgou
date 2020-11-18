package com.changgou.user.feign;

import com.changgou.entity.Result;
import com.changgou.user.pojo.Address;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(name = "changgou-service-user")
public interface AddressFeign {

    //地址服务
    @GetMapping("/address/list")
    public Result<List<Address>> list();
}
