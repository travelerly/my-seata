package com.colin.order.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.colin.order.entity.Order;
import io.seata.rm.tcc.api.BusinessActionContext;
import io.seata.rm.tcc.api.BusinessActionContextParameter;
import io.seata.rm.tcc.api.LocalTCC;
import io.seata.rm.tcc.api.TwoPhaseBusinessAction;

/**
 * 定义 TCC 接口，@LocalTCC 一定要标注在接口上
 * @LocalTCC：添加该注解的接口，其实现该接口的类将被 seata 管理，
 * seata 根据事务的状态，自动调用自定义的方法，若无异常，则执行 commit 方法，若存在异常，则指定 rollback 方法
 *
 * @LocalTCC 一定要标注在接口上
 */
@LocalTCC
public interface TCCOrderService extends IService<Order> {

    /**
     * 定义两阶段提交
     * @TwoPhaseBusinessAction：该注解表示为两阶段提交
     *  name：为 tcc 的 bean 名称，需要全局唯一，一般以方法名表示
     *  commitMethod：二阶段确认方法
     *  rollbackMethod：二阶段取消方法
     *
     * @BusinessActionContextParameter：该注解表示 try 方法的入参，即传递参数到二阶段
     * 即被修饰的入参可以在 commit 方法和 rollback 方法中通过 BusinessActionContext 获取
     */
    @TwoPhaseBusinessAction(
            name = "addTCC",
            commitMethod = "addCommit",
            rollbackMethod = "addRollBack")
    void add(@BusinessActionContextParameter(paramName = "order") Order order);

    /**
     * 二阶段确认方法
     * @param businessActionContext
     * @return
     */
    public boolean addCommit(BusinessActionContext businessActionContext);

    /**
     * 二阶段取消方法
     * @param businessActionContext
     * @return
     */
    public boolean addRollBack(BusinessActionContext businessActionContext);
}
