package com.atguigu.serurity.security;

import com.atguigu.commonutils.JwtUtils;
import io.jsonwebtoken.CompressionCodecs;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * <p>
 * token管理
 * </p>
 *
 * @author qy
 * @since 2019-11-08
 */
@Component
public class TokenManager {

  //token有效时长 一天
  private long tokenExpiration = 24*60*60*1000;
  //编码密钥
  private String tokenSignKey="123456";

  //1 根据用户名生成token signWith密钥编码加密
  public String createToken(String username){
      String token = Jwts.builder().setSubject(username)
              .setExpiration(new Date(System.currentTimeMillis()+tokenExpiration))
              .signWith(SignatureAlgorithm.HS512,tokenSignKey).compressWith(CompressionCodecs.GZIP).compact();
      return token;
  }

  //2 根据token字符串得到用户信息,用tokenSignKey做解码
  public String getUserInfoFromToken(String token){
    String userinfo = Jwts.parser().setSigningKey(tokenSignKey).parseClaimsJws(token).getBody().getSubject();
    return userinfo;
  }

  //3 删除token
  public void removeToken(String token){
    //jwttoken无需删除，客户端扔掉即可。
  }
}
