package com.atguigu.eduservice.client;

import com.atguigu.eduservice.client.impl.OrderClientImpl;
import com.atguigu.eduservice.client.impl.UcenterClientImpl;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


import javax.servlet.http.HttpServletRequest;

@Component
@FeignClient(name="service-order", fallback= OrderClientImpl.class)
public interface OrdersClient {
    //根据课程id和用户id查询订单表中订单状态
    @GetMapping("/eduorder/order/isBuyCourse/{courseId}/{memberId}")
    public boolean isBuyCourse(@PathVariable("courseId") String courseId, @PathVariable("memberId") String memberId);
}
