package com.colin.bussiness.controller;


import com.colin.bussiness.service.BussinessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BussinessController {

    @Autowired
    private BussinessService bussinessService;

    /**
     * AT 模式，正常流程
     * @return
     */
    @GetMapping("/test1")
    public String test1() {
        bussinessService.sale(1, 10, 100d, "zhaoyang");
        return "success";
    }

    /**
     * AT 模式，模拟异常流程
     * @return
     */
    @GetMapping("/test2")
    public String test2() {
        bussinessService.sale(1, 101, 100d, "zhaoyang");
        return "success";
    }

    /**
     * TCC 模式，正常流程
     * @return
     */
    @GetMapping("/tccTest1")
    public String tccTest1(){
        bussinessService.saleTCC(1, 10, 100d, "zhaoyang");
        return "success";
    }

    /**
     * TCC 模式，模拟异常流程
     * @return
     */
    @GetMapping("/tccTest2")
    public String tccTest2(){
        bussinessService.saleTCC(1, 101, 100d, "zhaoyang");
        return "success";
    }


}
