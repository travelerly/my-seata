package com.colin.storage.controller;

import com.colin.storage.service.StorageService;
import com.colin.storage.service.TCCStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/storage")
public class StorageController {
    @Resource
    private StorageService storageService;

    @Autowired
    private TCCStorageService tccStorageService;

    /**
     * (AT 模式)扣减库存
     * @param goodsId
     * @param quantity
     */
    @GetMapping("/decrease")
    public void decrease(Integer goodsId, Integer quantity) {
        storageService.decrease(goodsId, quantity);
    }

    /**
     * (TCC 模式)扣减库存
     * @param goodsId
     * @param quantity
     */
    @GetMapping("/decreaseTCC")
    public void decreaseTCC(Integer goodsId, Integer quantity) {
        tccStorageService.decrease(goodsId, quantity);
    }
}
