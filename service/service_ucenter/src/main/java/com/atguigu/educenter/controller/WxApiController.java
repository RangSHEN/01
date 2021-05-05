package com.atguigu.educenter.controller;


import com.atguigu.commonutils.JwtUtils;
import com.atguigu.commonutils.R;
import com.atguigu.educenter.entity.UcenterMember;
import com.atguigu.educenter.service.UcenterMemberService;
import com.atguigu.educenter.utils.ConstantWxUtils;
import com.atguigu.educenter.utils.HttpClientUtils;
import com.atguigu.servicebase.exceptionhandler.GuliException;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Member;
import java.net.Socket;
import java.net.URLEncoder;
import java.util.HashMap;


@Controller//注意这里没有配置@RestController 只想请求地址，并不想返回数据
@RequestMapping("/api/ucenter/wx")
//@CrossOrigin
public class WxApiController {

    @Autowired
    private UcenterMemberService memberService;

    //2.获取扫描人信息，添加数据 扫码成功就执行callback
    @GetMapping("callback")
    public String callback(String code,String state){//在方法中接收返回数据
        try {

            //2 拿着code请求微信固定的地址，得到两个值 access_token 和openid
            String baseAccessTokenUrl = "https://api.weixin.qq.com/sns/oauth2/access_token"+
                    "?appid=%s" +
                    "&secret=%s" +
                    "&code=%s" +
                    "&grant_type=authorization_code";
            //拼接三个参数 ：id 密钥 和code值
            String accessTokenUrl = String.format(
                    baseAccessTokenUrl,
                    ConstantWxUtils.WX_OPEN_APP_ID,
                    ConstantWxUtils.WX_OPEN_APP_SECRET,
                    code
            );
            //请求这个拼接好的地址，得到返回两个值 access_token 和openid
            //使用httpclient发送请求，得到返回结果 优点 不用浏览器也能模拟浏览器请求和响应过程
            String accessTokenInfo = HttpClientUtils.get(accessTokenUrl);

            //从accessTokenInfo字符串获取出来两个值 access_token 和openid
            //System.out.println("accessTokenInfo:"+accessTokenInfo);

            //把accessTokenInfo字符串转换map集合，根据map里面key获取对应值
            //使用json转换工具 fastjson jackson gson

            Gson gson = new Gson();
            HashMap mapAccessToken = gson.fromJson(accessTokenInfo, HashMap.class);
            String access_token = (String) mapAccessToken.get("access_token");
            String openid = (String) mapAccessToken.get("openid");

            //把扫描人信息添加到数据库里面去
            //判断数据库表里是否存在相同微信信息，根据openid判断
            UcenterMember member = memberService.getOpenIdMember(openid);
            if(member == null){//member是空，表没有相同微信数据，进行添加

                //3.拿着得到access_token 和openid，再去请求微信提供的地址，获取扫码人的信息
                //访问微信的资源服务器，获取用户信息 换到if 里面 是因为二次登录就不用再获取扫码人信息，直接登陆就可以
                String baseUserInfoUrl = "https://api.weixin.qq.com/sns/userinfo"+
                        "?access_token=%s" +
                        "&openid=%s";

                String userInfoUrl = String.format(
                        baseUserInfoUrl,
                        access_token,
                        openid
                );
                //发送请求
                String userInfo = HttpClientUtils.get(userInfoUrl);
                System.out.println("userInfo:"+userInfo);
                //获取返回userinfo字符串扫码人的信息
                HashMap userInfoMap = gson.fromJson(userInfo, HashMap.class);
                String nickname = (String) userInfoMap.get("nickname");
                String headimgurl = (String) userInfoMap.get("headimgurl");//头像

                member = new UcenterMember();
                member.setOpenid(openid);
                member.setAvatar(headimgurl);
                member.setNickname(nickname);
                memberService.save(member);
            }

            //使用jwt根据member对象生成token字符串 解决cookie跨域问题
            String jwtToken = JwtUtils.getJwtToken(member.getId(), member.getNickname());

            //最后：返回首页面，通过路径传递字符串
            return "redirect:http://localhost:3000?token="+jwtToken;
        }catch (Exception e){
            throw new GuliException(20001,"登录失败");
        }

    }

    //1.生成微信扫描二维码
    @GetMapping("login")
    public String getWxCode(){
        //固定地址，后面拼接参数
/*        String url = "https://open.weixin.qq.com/"+
                "connect/qrconnect?appid="+ConstantWxUtils.WX_OPEN_APP_ID+"&response_type=code";*/
        // 微信开放平台授权baseUrl %s相当于问号，代表占位符
        String baseUrl = "https://open.weixin.qq.com/connect/qrconnect" +
                "?appid=%s" +
                "&redirect_uri=%s" +
                "&response_type=code" +
                "&scope=snsapi_login" +
                "&state=%s" +
                "#wechat_redirect";

        //对redirect_url进行URLEncoder编码,目的把编码里的特殊符号进行处理
        String redirectUrl = ConstantWxUtils.WX_OPEN_REDIRECT_URL;
        try {
            redirectUrl = URLEncoder.encode(redirectUrl, "utf-8");
        } catch (Exception e) {

        }

        //设置%s里的值
        String url = String.format(
                baseUrl,
                ConstantWxUtils.WX_OPEN_APP_ID,
                ConstantWxUtils.WX_OPEN_REDIRECT_URL,
                redirectUrl,
                "atguigu"
        );

        //重定向到请求微信地址里面
        return "redirect:" +url;
    }
}
