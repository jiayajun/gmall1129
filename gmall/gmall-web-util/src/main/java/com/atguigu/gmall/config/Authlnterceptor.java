package com.atguigu.gmall.config;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.WebConst;
import com.atguigu.gmall.utils.CookieUtil;
import com.atguigu.gmall.utils.HttpClientUtil;
import io.jsonwebtoken.impl.Base64UrlCodec;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.net.URLEncoder;
import java.util.Map;

@Component
public class Authlnterceptor extends HandlerInterceptorAdapter{

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String userId=null;
        //1.登录回来返回cookie中
        //cookie失效时间
        int cookieAge = 3600*24*7;

        String verifyUrl = WebConst.VERIFY_URL;

        String newToken = request.getParameter("newToken");
        if (newToken!=null){

            CookieUtil.setCookie(request,response,"token",newToken,cookieAge,false);

        }
        //2.检查cookie中是否有token 获取token中的昵称方人员request中

        String token = CookieUtil.getCookieValue(request,"token",false);
        if (token!=null){

            //分割字符串
            String  tokenForDecode= StringUtils.substringBetween(token, ".");

            Base64UrlCodec base64UrlCodec=new Base64UrlCodec();

            byte[] tokenByte = base64UrlCodec.decode(tokenForDecode);
            String tokenJson=new String(tokenByte,"UTF-8");
            Map userMap = JSON.parseObject(tokenByte, Map.class);
            System.out.println("tokenJson = " + tokenJson);

            JSONObject jsonObject = JSON.parseObject( tokenJson);

            userId = jsonObject.getString("userId");
            String nickName = jsonObject.getString("nickName");

            request.setAttribute("nickName",nickName);


        }

        //3 如果这个请求需要认证登录  把token发送到认证中心 进行校验
        HandlerMethod handlerMethod =(HandlerMethod)handler;
        LoginRequire methodAnnotation = handlerMethod.getMethodAnnotation(LoginRequire.class);
        if(methodAnnotation!=null){
            if(token!=null) {
                String currentIp = request.getHeader("x-forwarded-for");
                String result = HttpClientUtil.doGet(verifyUrl + "?currentIp="+currentIp+"&token=" + token);

                //4 认证中心返回认证结果  如果是fail 重定向到登录页面 如果是success 把userId 写入到request中
                if ("success".equals(result)) {
                    request.setAttribute("userId",userId);
                }else{
                    if(methodAnnotation.autoRedirect()) {
                        String originUrl = request.getRequestURL().toString();
                        originUrl = URLEncoder.encode(originUrl, "utf-8");
                        response.sendRedirect("http://passport.atguigu.com/index?originUrl=" + originUrl);
                    }
                }
            }else{
                if(methodAnnotation.autoRedirect()) {
                    String originUrl = request.getRequestURL().toString();
                    originUrl = URLEncoder.encode(originUrl, "utf-8");
                    response.sendRedirect("http://passport.atguigu.com/index?originUrl=" + originUrl);
                }
            }

        }


        //4 认证中心返回认证结果  如果是fail 重定向到登录页面 如果是success 把userId 写入到request中




        return true;
    }

}
