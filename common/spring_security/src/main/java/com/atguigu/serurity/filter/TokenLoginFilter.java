package com.atguigu.serurity.filter;

import com.atguigu.commonutils.R;
import com.atguigu.commonutils.ResponseUtil;
import com.atguigu.serurity.entity.SecurityUser;
import com.atguigu.serurity.entity.User;
import com.atguigu.serurity.security.TokenManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;

/**
 * <p>
 * 登录过滤器，继承UsernamePasswordAuthenticationFilter，对用户名密码进行登录校验
 * </p>
 *
 * @author qy
 * @since 2019-11-08
 */
public class TokenLoginFilter extends UsernamePasswordAuthenticationFilter {

    private AuthenticationManager authenticationManager;//封装的权限管理
    private TokenManager tokenManager;
    private RedisTemplate redisTemplate;

    //要使用对象，有参构造

    public TokenLoginFilter(AuthenticationManager authenticationManager, TokenManager tokenManager, RedisTemplate redisTemplate) {
        this.authenticationManager = authenticationManager;
        this.tokenManager = tokenManager;
        this.redisTemplate = redisTemplate;
        this.setPostOnly(false);
        //设置登录路径和提交方式
        this.setRequiresAuthenticationRequestMatcher(new AntPathRequestMatcher("/admin/acl/login","POST"));
    }

    //1 获取表单提交用户名和密码
    @Override
    public Authentication attemptAuthentication(HttpServletRequest req, HttpServletResponse res)
            throws AuthenticationException {
        //获取表单提交过来数据
        try {
            User user = new ObjectMapper().readValue(req.getInputStream(), User.class);
            return authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(user.getUsername(),user.getPassword(),
                    new ArrayList<>()));//在认证过程中会调用userdetailservice去查数据库返回用户信息
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }

    }

    //2 认证成功调用的方法
    @Override
    protected void successfulAuthentication(HttpServletRequest req, HttpServletResponse res, FilterChain chain,
                                            Authentication authResult) throws IOException, ServletException {
        //认证成功得到用户信息
        SecurityUser user =(SecurityUser) authResult.getPrincipal();
        //根据用户名生成token
        String token = tokenManager.createToken(user.getCurrentUserInfo().getUsername());
        //把用户名称和用户权限列表信息放到redis
        redisTemplate.opsForValue().set(user.getCurrentUserInfo().getUsername(),user.getPermissionValueList());
        //返回token ResponseUtil.out 就像return 用在网络通信协议中
        ResponseUtil.out(res,R.ok().data("token",token));
    }

    //3 认证失败调用的方法
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                              AuthenticationException e) throws IOException, ServletException {
        ResponseUtil.out(response, R.error());
    }
}
