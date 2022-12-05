package com.colin.bussiness.controller;


import com.colin.bussiness.service.BussinessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BussinessController {

    @Autowired
    private BussinessService bussinessService;

    @GetMapping("/test1")
    public String test1() {
        bussinessService.sale(1, 10, 100d, "zhaoyang");
        return "success";
    }

    @GetMapping("/test2")
    public String test2() {
        bussinessService.sale(1, 101, 100d, "zhaoyang");
        return "success";
    }

}
