package com.atguigu.gmall.utils;

import io.jsonwebtoken.*;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class JwtUtil {
  static   String passport_key = "ATGIUGU";

    //工具加密(盐值加密)
    public static String encode(String key ,Map map,String salt){
        //判断非空进行拼接
        if(salt!=null){
            key+=salt;
        }

        JwtBuilder jwtBuilder = Jwts.builder().signWith(SignatureAlgorithm.HS256, key);
        jwtBuilder.addClaims(map);

        String token = jwtBuilder.compact();

        return token;

    }

    //解密方法
    public static Map decode(String token ,String key ,String salt ){
        if(salt!=null){
            key+=salt;
        }

        Claims claims = null;
        try {
            claims = Jwts.parser().setSigningKey(key).parseClaimsJws(token).getBody();
        }   catch (SignatureException e) {
            return null;
        }

        return claims;
    }

    @Test
    public void  testJwt(){
        Map map=new HashMap();
        map.put("userId","1001");
        map.put("nickName","zhang3");

        String token = JwtUtil.encode(passport_key, map, "192.168.1.100");

        System.out.println("token = " + token);

        Map mapRs = JwtUtil.decode(token, passport_key, "192.168.1.100");

        System.out.println("mapRs = " + mapRs);
    }



}
