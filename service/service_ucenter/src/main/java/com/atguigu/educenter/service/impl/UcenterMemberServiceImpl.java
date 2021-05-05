package com.atguigu.educenter.service.impl;

import com.atguigu.commonutils.JwtUtils;
import com.atguigu.commonutils.MD5;
import com.atguigu.commonutils.R;
import com.atguigu.educenter.entity.UcenterMember;
import com.atguigu.educenter.entity.vo.LoginVo;
import com.atguigu.educenter.entity.vo.RegisterVo;
import com.atguigu.educenter.mapper.UcenterMemberMapper;
import com.atguigu.educenter.service.UcenterMemberService;
import com.atguigu.servicebase.exceptionhandler.GuliException;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 会员表 服务实现类
 * </p>
 *
 * @author testjava
 * @since 2021-04-11
 */
@Service
public class UcenterMemberServiceImpl extends ServiceImpl<UcenterMemberMapper, UcenterMember> implements UcenterMemberService {

    @Autowired
    private RedisTemplate<String,String> redisTemplate;
    //登录的方法
    @Override
    public String login(LoginVo loginVo) {
        //获取登录手机号和密码
        String mobile = loginVo.getMobile();
        String password = loginVo.getPassword();

        //手机号和密码非空判断
        if(StringUtils.isEmpty(mobile) || StringUtils.isEmpty(password)){
            throw new GuliException(20001,"登陆失败");
        }

        //判断手机号是否正确
        QueryWrapper<UcenterMember> wrapper = new QueryWrapper<>();//QueryWrapper只能传原始对象
        wrapper.eq("mobile",mobile);
        UcenterMember mobileMember = baseMapper.selectOne(wrapper);//mobileMember是从数据库中根据手机号查出的对象有所有数据
        //判断查询对象是否为空
        if(mobileMember == null){//没有这个手机号抛异常，有就继续执行
            throw new GuliException(20001,"登录失败");
        }

        //判断密码
        //因为存储到数据库密码肯定是加密的
        //把输入的密码进行加密，再和数据库密码进行比较
        //加密方式MD5 只能加密，不能解密
        if(!MD5.encrypt(password).equals(mobileMember.getPassword())){
            throw new GuliException(20001,"登录失败");
        }

        //判断用户是否禁用
        if(mobileMember.getIsDisabled()){
            throw new GuliException(20001,"登录失败");
        }

        //登录成功
        //生成:token字符串，使用jwt工具类  注意member是从页面传过来的对象，只有手机号和密码没有id和昵称，必须要用mobilemember
        String jwtToken = JwtUtils.getJwtToken(mobileMember.getId(), mobileMember.getNickname());

        return jwtToken;

    }

    //注册的方法
    @Override
    public void register(RegisterVo registerVo) {
        //获取注册的数据
        String code = registerVo.getCode(); //验证码
        String mobile = registerVo.getMobile();//手机号
        String nickname = registerVo.getNickname();//昵称
        String password = registerVo.getPassword();//密码

        //非空判断
        if(StringUtils.isEmpty(mobile) ||StringUtils.isEmpty(password) ||
        StringUtils.isEmpty(code) ||StringUtils.isEmpty(nickname)){
            throw new GuliException(20001,"登录失败");
        }

        //判断验证码
        //获取redis验证码
        String redisCode = redisTemplate.opsForValue().get(mobile);
        if(!code.equals(redisCode)){
            throw new GuliException(20001,"注册失败");
        }

        //判断手机号是否重复，表里面存在相同手机号不进行添加
        QueryWrapper<UcenterMember> wrapper = new QueryWrapper<>();
        wrapper.eq("mobile",mobile);
        Integer count = baseMapper.selectCount(wrapper);//要查询数据有没有，不需要得到不用selectOne
        if(count > 0){
            throw new GuliException(20001,"注册失败");
        }

        //数据添加数据库中
        UcenterMember member = new UcenterMember();
        member.setMobile(mobile);
        member.setNickname(nickname);
        member.setPassword(MD5.encrypt(password));//密码需要加密
        member.setIsDisabled(false);//用户不禁用
        member.setAvatar("https://online-teach-file.oss-cn-beijing.aliyuncs.com/teacher/2019/11/08/e44a2e92-2421-4ea3-bb49-46f2ec96ef88.png");
        baseMapper.insert(member);
    }

    @Override
    public UcenterMember getOpenIdMember(String openid) {
        QueryWrapper<UcenterMember> wrapper = new QueryWrapper<>();
        wrapper.eq("openid",openid);
        UcenterMember member = baseMapper.selectOne(wrapper);//查询
        return member;
    }

    @Override
    public Integer countRegisterDay(String day) {
        return baseMapper.countRegisterDay(day);
    }
}
