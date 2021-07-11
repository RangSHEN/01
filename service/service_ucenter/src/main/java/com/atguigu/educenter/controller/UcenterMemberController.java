package com.atguigu.educenter.controller;


import com.atguigu.commonutils.JwtUtils;
import com.atguigu.commonutils.R;
import com.atguigu.commonutils.ordervo.UcenterMemberVo;
import com.atguigu.educenter.entity.UcenterMember;
import com.atguigu.educenter.entity.vo.LoginVo;
import com.atguigu.educenter.entity.vo.RegisterVo;
import com.atguigu.educenter.service.UcenterMemberService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * <p>
 * 会员表 前端控制器
 * </p>
 *
 * @author testjava
 * @since 2021-04-11
 */
@RestController
@RequestMapping("/educenter/member")
//@CrossOrigin
public class UcenterMemberController {

    @Autowired
    private UcenterMemberService memberService;

    //登录
    @PostMapping("login")
    public R loginUser(@RequestBody LoginVo loginVo){
        //member对象封装手机号和密码
        //调用service方法实现登录
        //返回token值，使用jwt生成
        String token = memberService.login(loginVo);
        return R.ok().data("token",token);

    }

    //注册
    @PostMapping("register")
    public R registerUser(@RequestBody RegisterVo registerVo){
        memberService.register(registerVo);
        return R.ok();
    }

    //根据token获取用户信息，用于前端页面显示昵称头像
    @GetMapping("getMemberInfo")
    public R getMemberInfo(HttpServletRequest request){
        //调用jwt工具类的方法。根据request对象获取头信息，返回用户id 从前端header获取token 返回id
        String memberId = JwtUtils.getMemberIdByJwtToken(request);//根据jwt的字符串获得id
        //查询数据库根据用户id获取用户信息
        UcenterMember member = memberService.getById(memberId);
        return R.ok().data("userInfo",member);

    }
    //根据用户id查询用户信息  将UcenterMember对象转移到CommonUtils中
    //在调用端和被调用端用同一个对象
    //在CommonUtils中创建这个Vo对象
    @PostMapping("getMemberInfoById/{memberId}")
    public UcenterMemberVo getMemberInfoById(@PathVariable String memberId){
        UcenterMember member = memberService.getById(memberId);
        UcenterMemberVo memberVo = new UcenterMemberVo();
        BeanUtils.copyProperties(member,memberVo);

        return memberVo;
    }

    //根据用户id获取用户信息 返回R对象把值取到需要get出来，再通过key取，不方便
    //在调用端和被调用端用同一个对象
  /*  @PostMapping("getUserInfoOrder/{id}")
    public UcenterMemberVo getUserInfoOrder(@PathVariable String id){
        UcenterMember member = memberService.getById(id);
        UcenterMemberVo memberVo = new UcenterMemberVo();
        BeanUtils.copyProperties(member,memberVo);

        return memberVo;
    }*/

    //查询某一天注册人数
    @GetMapping("countRegister/{day}")
    public R countRegister(@PathVariable String day){
        Integer count = memberService.countRegisterDay(day);
        return R.ok().data("countRegister",count);
    }
}

