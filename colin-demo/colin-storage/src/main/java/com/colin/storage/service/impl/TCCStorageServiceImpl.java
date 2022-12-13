package com.colin.storage.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.colin.storage.entity.Storage;
import com.colin.storage.mapper.StorageMapper;
import com.colin.storage.service.TCCStorageService;
import io.seata.rm.tcc.api.BusinessActionContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 仓库服务
 */
@Slf4j
@Service
public class TCCStorageServiceImpl extends ServiceImpl<StorageMapper, Storage> implements TCCStorageService {

    /**
     * 减少（冻结）库存
     * @param goodsId 商品 ID
     * @param quantity  冻结的数量
     */
    @Override
    public void decrease(Integer goodsId, Integer quantity) {
        QueryWrapper<Storage> wrapper = new QueryWrapper<Storage>();
        wrapper.lambda().eq(Storage::getGoodsId, goodsId);
        Storage goodsStorage = this.getOne(wrapper);
        if (goodsStorage.getStorage() >= quantity) {
            // 冻结库存
            goodsStorage.setFrozenStorage(quantity);
        }else {
            throw new RuntimeException(goodsId + "库存不⾜,⽬前剩余库存:" + goodsStorage.getStorage());
        }
        this.saveOrUpdate(goodsStorage);
    }

    @Override
    public boolean decreaseCommit(BusinessActionContext context) {
        QueryWrapper<Storage> wrapper = new QueryWrapper<Storage>();
        wrapper.lambda().eq(Storage::getGoodsId, context.getActionContext("goodsId"));
        Storage goodsStorage = this.getOne(wrapper);
        if (goodsStorage != null) {
            // 扣减库存
            goodsStorage.setStorage(goodsStorage.getStorage() - goodsStorage.getFrozenStorage());
            // 冻结库存清零
            goodsStorage.setFrozenStorage(0);
            this.saveOrUpdate(goodsStorage);
        }
        log.info("=== xid ===" + context.getXid() + "提交成功");
        return true;
    }

    @Override
    public boolean decreaseRollBack(BusinessActionContext context) {
        QueryWrapper<Storage> wrapper = new QueryWrapper<Storage>();
        wrapper.lambda().eq(Storage::getGoodsId, context.getActionContext("goodsId"));
        Storage goodsStorage = this.getOne(wrapper);
        if (goodsStorage != null) {
            // 冻结库存清零
            goodsStorage.setFrozenStorage(0);
            this.saveOrUpdate(goodsStorage);
        }
        log.info("=== xid ===" + context.getXid() + "回滚成功");
        return true;
    }
}
