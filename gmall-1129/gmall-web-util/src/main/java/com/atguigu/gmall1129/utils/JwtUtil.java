package com.atguigu.gmall1129.utils;

import io.jsonwebtoken.*;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @param
 * @return
 */
public class JwtUtil {

    static String  passport_key="ATGUIGU1129";

    public static String encode(String key ,Map map,String salt){
        if(salt!=null){
            key+=salt;
        }

        JwtBuilder jwtBuilder = Jwts.builder().signWith(SignatureAlgorithm.HS256, key);
        jwtBuilder.addClaims(map);

        String token = jwtBuilder.compact();

        return token;

    }


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

        Map mapRs = JwtUtil.decode(token, passport_key, "100.168.1.100");

        System.out.println("mapRs = " + mapRs);
    }



}
