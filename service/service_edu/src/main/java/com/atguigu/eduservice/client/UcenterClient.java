package com.atguigu.eduservice.client;

import com.atguigu.commonutils.ordervo.UcenterMemberVo;
import com.atguigu.eduservice.client.impl.UcenterClientImpl;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "service-ucenter",fallback = UcenterClientImpl.class)
@Component
public interface UcenterClient {

    @PostMapping("/educenter/member/getMemberInfoById/{memberId}")
    public UcenterMemberVo getMemberInfoById(@PathVariable("memberId") String memberId);
}
