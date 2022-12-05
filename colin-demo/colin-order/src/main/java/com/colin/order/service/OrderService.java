package com.colin.order.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.colin.order.entity.Order;

public interface OrderService extends IService<Order> {
    void add(Order order);
}
