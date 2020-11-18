package com.changgou.interceptor;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;

/*
    RequestInterceptor 可以拦截所有的请求，对请求进行增强
 */
@Component
public class FeignInterceptor implements RequestInterceptor {
    @Override
    public void apply(RequestTemplate requestTemplate) {
        //持有上下文的request容器
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes != null) {
            //RequestAttributes向下转换为servlet容器的域容器
            HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
            //从request中获取请求头信息，因为在登陆认证成功后，网关过滤器在请求头添加JWT令牌对请求增强
            if (null!=request){
                //获取请求头集合
                Enumeration<String> headerNames = request.getHeaderNames();
                while (headerNames.hasMoreElements()){
                    String headerName = headerNames.nextElement();
                    if ("authorization".equals(headerName)){
                        //如果请求头中含有JWT令牌，将令牌存入feign调用的请求头中
                        String headerValue = request.getHeader(headerName);
                        requestTemplate.header(headerName,headerValue);
                    }
                }
            }
        }

    }
}
