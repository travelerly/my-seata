package com.colin.bussiness.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("colin-order-service")
public interface OrderServiceFeign {

    @GetMapping("/order/add")
    public void addOrder(@RequestParam("orderId") Long id,
                         @RequestParam("goodsId") Integer goodsId,
                         @RequestParam("num") Integer num,
                         @RequestParam("money") Double money,
                         @RequestParam("username") String username);
}
