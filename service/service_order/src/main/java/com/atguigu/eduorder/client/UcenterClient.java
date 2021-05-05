package com.atguigu.eduorder.client;

import com.atguigu.commonutils.ordervo.UcenterMemberVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Component
@FeignClient("service-ucenter")
public interface UcenterClient {
    @PostMapping("/educenter/member/getMemberInfoById/{memberId}")
    public UcenterMemberVo getMemberInfoById(@PathVariable("memberId") String memberId);
}
