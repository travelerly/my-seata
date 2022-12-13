package com.colin.order.controller;

import com.colin.order.entity.Order;
import com.colin.order.service.OrderService;
import com.colin.order.service.TCCOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 订单控制层
 */
@Slf4j
@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    OrderService orderService;

    @Autowired
    TCCOrderService tccOrderService;

    /**
     * (AT 模式)下单
     */
    @RequestMapping("/add")
    public void addOrder(Long orderId,Integer goodsId, Integer num, Double money, String username) {
        Order order = new Order();
        order.setId(orderId);
        order.setGoodsId(goodsId);
        order.setNum(num);
        order.setMoney(money);
        order.setUsername(username);
        orderService.add(order);
    }

    /**
     * (TCC 模式)下单
     */
    @RequestMapping("/addTCC")
    public void addOrderTCC(Long orderId,Integer goodsId, Integer num, Double money, String username) {
        Order order = new Order();
        order.setId(orderId);
        order.setGoodsId(goodsId);
        order.setNum(num);
        order.setMoney(money);
        order.setUsername(username);
        tccOrderService.add(order);
    }

}
