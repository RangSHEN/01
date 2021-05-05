package com.atguigu.eduservice.client.impl;

import com.atguigu.eduservice.client.OrdersClient;
import org.springframework.stereotype.Component;

@Component
public class OrderClientImpl implements OrdersClient {
    @Override
    public boolean isBuyCourse(String courseId, String memberId) {
        return false;
    }
}
