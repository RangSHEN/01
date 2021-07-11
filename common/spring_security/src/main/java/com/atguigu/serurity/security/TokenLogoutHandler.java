package com.atguigu.serurity.security;

import com.atguigu.commonutils.R;
import com.atguigu.commonutils.ResponseUtil;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * 登出业务逻辑类
 * </p>
 *
 */
public class TokenLogoutHandler implements LogoutHandler {

    private TokenManager tokenManager;//要移除token，需要这个工具类
    private RedisTemplate redisTemplate;

    //最简单的传入，通过有参构造 没有交给容器管理
    public TokenLogoutHandler(TokenManager tokenManager, RedisTemplate redisTemplate) {
        this.tokenManager = tokenManager;
        this.redisTemplate = redisTemplate;
    }
    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        //1 从header里面获取token
        String token = request.getHeader("token");
        if(token != null){
            //移除 ,客户端不传token就可以
            tokenManager.removeToken(token);

            //2 token不为空，移除token，从redis删除token
            String username = tokenManager.getUserInfoFromToken(token);
            redisTemplate.delete(username);
        }
        ResponseUtil.out(response,R.ok());
    }


}