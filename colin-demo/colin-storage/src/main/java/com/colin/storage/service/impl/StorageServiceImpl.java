package com.colin.storage.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.colin.storage.entity.Storage;
import com.colin.storage.mapper.StorageMapper;
import com.colin.storage.service.StorageService;
import org.springframework.stereotype.Service;

/**
 * 仓库服务
 */
@Service
public class StorageServiceImpl extends ServiceImpl<StorageMapper, Storage> implements StorageService {

    /**
     * 减少库存
     *
     * @param goodsId  商品ID
     * @param quantity 减少数量
     * @return 库存对象
     */
    public void decrease(Integer goodsId, Integer quantity) {
        QueryWrapper<Storage> wrapper = new QueryWrapper<Storage>();
        wrapper.lambda().eq(Storage::getGoodsId, goodsId);
        Storage goodsStorage = this.getOne(wrapper);
        if (goodsStorage.getStorage() >= quantity) {
            goodsStorage.setStorage(goodsStorage.getStorage() - quantity);
        } else {
            throw new RuntimeException(goodsId + "库存不足,目前剩余库存:" + goodsStorage.getStorage());
        }
        this.saveOrUpdate(goodsStorage);
    }
}
