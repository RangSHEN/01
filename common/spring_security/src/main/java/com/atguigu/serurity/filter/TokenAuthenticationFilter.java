package com.atguigu.serurity.filter;

import com.atguigu.commonutils.R;
import com.atguigu.commonutils.ResponseUtil;
import com.atguigu.serurity.security.TokenManager;
import org.apache.catalina.connector.Request;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.util.StringUtils;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * <p>
 * 访问过滤器
 * </p>
 *
 * @author qy
 * @since 2019-11-08
 */
public class TokenAuthenticationFilter extends BasicAuthenticationFilter {
    private TokenManager tokenManager;
    private RedisTemplate redisTemplate;

    public TokenAuthenticationFilter(AuthenticationManager authenticationManager,TokenManager tokenManager, RedisTemplate redisTemplate) {
        super(authenticationManager);
        this.tokenManager = tokenManager;
        this.redisTemplate = redisTemplate;
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        //获取当前认证成功用户权限信息 通过request得到头信息
        UsernamePasswordAuthenticationToken authRequest = getAuthentication(request);
        //判断如果有权限信息，放到权限上下文中
        if(authRequest !=null){
            SecurityContextHolder.getContext().setAuthentication(authRequest);
        }
        chain.doFilter(request,response);
    }

    private UsernamePasswordAuthenticationToken getAuthentication(HttpServletRequest request) {
        // 从header中获取token
        String token = request.getHeader("token");
        if( token !=null){
            //从token获取用户名
            String username = tokenManager.getUserInfoFromToken(token);
            //从redis获取对应权限列表
            List<String> permissionValueList = (List<String>)redisTemplate.opsForValue().get(username);
            //Collection<? extends GrantedAuthority> authorities
            Collection<GrantedAuthority> authorities = new ArrayList<>();
            for (String permissionValue : permissionValueList) {
                SimpleGrantedAuthority auth = new SimpleGrantedAuthority(permissionValue);
                authorities.add(auth);
            }
            return new UsernamePasswordAuthenticationToken(username,token,authorities);
        }

        return null;
    }
}