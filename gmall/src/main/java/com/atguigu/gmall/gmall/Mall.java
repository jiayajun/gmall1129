package com.atguigu.gmall.gmall;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class Mall {
    @ResponseBody
    @RequestMapping("/test")
    public String getMall(){

        return "Hello JIAYAJUN";

    }
}
