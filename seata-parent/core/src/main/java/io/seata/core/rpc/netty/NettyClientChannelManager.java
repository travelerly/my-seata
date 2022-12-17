/*
 *  Copyright 1999-2019 Seata.io Group.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package io.seata.core.rpc.netty;

import io.netty.channel.Channel;
import io.seata.common.exception.FrameworkErrorCode;
import io.seata.common.exception.FrameworkException;
import io.seata.common.util.CollectionUtils;
import io.seata.common.util.NetUtil;
import io.seata.common.util.StringUtils;
import io.seata.core.constants.ConfigurationKeys;
import io.seata.core.protocol.RegisterRMRequest;
import io.seata.core.protocol.RegisterTMRequest;
import io.seata.discovery.registry.FileRegistryServiceImpl;
import io.seata.discovery.registry.RegistryFactory;
import io.seata.discovery.registry.RegistryService;
import org.apache.commons.pool.impl.GenericKeyedObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Netty client pool manager.
 *
 * @author slievrly
 * @author zhaojun
 */
class NettyClientChannelManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(NettyClientChannelManager.class);

    private final ConcurrentMap<String, Object> channelLocks = new ConcurrentHashMap<>();

    private final ConcurrentMap<String, NettyPoolKey> poolKeyMap = new ConcurrentHashMap<>();

    private final ConcurrentMap<String, Channel> channels = new ConcurrentHashMap<>();

    private final GenericKeyedObjectPool<NettyPoolKey, Channel> nettyClientKeyPool;

    private Function<String, NettyPoolKey> poolKeyFunction;

    NettyClientChannelManager(final NettyPoolableFactory keyPoolableFactory, final Function<String, NettyPoolKey> poolKeyFunction,
                                     final NettyClientConfig clientConfig) {
        nettyClientKeyPool = new GenericKeyedObjectPool<>(keyPoolableFactory);
        nettyClientKeyPool.setConfig(getNettyPoolConfig(clientConfig));
        this.poolKeyFunction = poolKeyFunction;
    }

    private GenericKeyedObjectPool.Config getNettyPoolConfig(final NettyClientConfig clientConfig) {
        GenericKeyedObjectPool.Config poolConfig = new GenericKeyedObjectPool.Config();
        poolConfig.maxActive = clientConfig.getMaxPoolActive();
        poolConfig.minIdle = clientConfig.getMinPoolIdle();
        poolConfig.maxWait = clientConfig.getMaxAcquireConnMills();
        poolConfig.testOnBorrow = clientConfig.isPoolTestBorrow();
        poolConfig.testOnReturn = clientConfig.isPoolTestReturn();
        poolConfig.lifo = clientConfig.isPoolLifo();
        return poolConfig;
    }

    /**
     * Get all channels registered on current Rpc Client.
     *
     * @return channels
     */
    ConcurrentMap<String, Channel> getChannels() {
        return channels;
    }

    /**
     * 获取通道，建立与 TC 的连接
     * Acquire netty client channel connected to remote server.
     *
     * @param serverAddress server address
     * @return netty channel
     */
    Channel acquireChannel(String serverAddress) {
        /**
         * channels：为一个 ConcurrentMap<String, Channel> 对象，即每个 TC 端，RM 至维持一个连接
         * 在 RM 启动时，channels 里面没有任何连接，即 channelToServer == null
         */
        Channel channelToServer = channels.get(serverAddress);
        if (channelToServer != null) {
            // 检测当前连接是否可用，若不可用则返回 null
            channelToServer = getExistAliveChannel(channelToServer, serverAddress);
            if (channelToServer != null) {
                return channelToServer;
            }
        }
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("will connect to {}", serverAddress);
        }
        Object lockObj = CollectionUtils.computeIfAbsent(channelLocks, serverAddress, key -> new Object());
        synchronized (lockObj) {
            // 创建与 TC 的连接（做了并发处理，每次只能一个线程执行）
            return doConnect(serverAddress);
        }
    }

    /**
     * Release channel to pool if necessary.
     *
     * @param channel channel
     * @param serverAddress server address
     */
    void releaseChannel(Channel channel, String serverAddress) {
        if (channel == null || serverAddress == null) { return; }
        try {
            synchronized (channelLocks.get(serverAddress)) {
                Channel ch = channels.get(serverAddress);
                if (ch == null) {
                    nettyClientKeyPool.returnObject(poolKeyMap.get(serverAddress), channel);
                    return;
                }
                if (ch.compareTo(channel) == 0) {
                    if (LOGGER.isInfoEnabled()) {
                        LOGGER.info("return to pool, rm channel:{}", channel);
                    }
                    destroyChannel(serverAddress, channel);
                } else {
                    nettyClientKeyPool.returnObject(poolKeyMap.get(serverAddress), channel);
                }
            }
        } catch (Exception exx) {
            LOGGER.error(exx.getMessage());
        }
    }

    /**
     * Destroy channel.
     *
     * @param serverAddress server address
     * @param channel channel
     */
    void destroyChannel(String serverAddress, Channel channel) {
        if (channel == null) { return; }
        try {
            if (channel.equals(channels.get(serverAddress))) {
                channels.remove(serverAddress);
            }
            nettyClientKeyPool.returnObject(poolKeyMap.get(serverAddress), channel);
        } catch (Exception exx) {
            LOGGER.error("return channel to rmPool error:{}", exx.getMessage());
        }
    }

    /**
     * Reconnect to remote server of current transaction service group.
     *
     * @param transactionServiceGroup transaction service group
     */
    void reconnect(String transactionServiceGroup) {
        List<String> availList = null;
        try {
            // 获取服务器列表（与事务分组对应的集群中的所有服务器列表）
            availList = getAvailServerList(transactionServiceGroup);
        } catch (Exception e) {
            LOGGER.error("Failed to get available servers: {}", e.getMessage(), e);
            return;
        }

        /**
         * 若没有获取到服务器列表，则打印日志，seata 有一个定时任务，每隔一段时间会重新查询集群中的服务器地址
         */
        if (CollectionUtils.isEmpty(availList)) {
            RegistryService registryService = RegistryFactory.getInstance();
            String clusterName = registryService.getServiceGroup(transactionServiceGroup);

            if (StringUtils.isBlank(clusterName)) {
                LOGGER.error("can not get cluster name in registry config '{}{}', please make sure registry config correct",
                        ConfigurationKeys.SERVICE_GROUP_MAPPING_PREFIX,
                        transactionServiceGroup);
                return;
            }

            if (!(registryService instanceof FileRegistryServiceImpl)) {
                LOGGER.error("no available service found in cluster '{}', please make sure registry config correct and keep your seata server running", clusterName);
            }
            return;
        }

        Set<String> channelAddress = new HashSet<>(availList.size());
        try {
            // 遍历服务器列表
            for (String serverAddress : availList) {
                try {
                    // 获取通道，建立与 TC 的连接
                    acquireChannel(serverAddress);
                    channelAddress.add(serverAddress);
                } catch (Exception e) {
                    LOGGER.error("{} can not connect to {} cause:{}", FrameworkErrorCode.NetConnect.getErrCode(),
                        serverAddress, e.getMessage(), e);
                }
            }
        } finally {
            if (CollectionUtils.isNotEmpty(channelAddress)) {
                List<InetSocketAddress> aliveAddress = new ArrayList<>(channelAddress.size());
                for (String address : channelAddress) {
                    String[] array = address.split(":");
                    aliveAddress.add(new InetSocketAddress(array[0], Integer.parseInt(array[1])));
                }
                RegistryFactory.getInstance().refreshAliveLookup(transactionServiceGroup, aliveAddress);
            } else {
                RegistryFactory.getInstance().refreshAliveLookup(transactionServiceGroup, Collections.emptyList());
            }
        }
    }

    void invalidateObject(final String serverAddress, final Channel channel) throws Exception {
        nettyClientKeyPool.invalidateObject(poolKeyMap.get(serverAddress), channel);
    }

    void registerChannel(final String serverAddress, final Channel channel) {
        Channel channelToServer = channels.get(serverAddress);
        if (channelToServer != null && channelToServer.isActive()) {
            return;
        }
        channels.put(serverAddress, channel);
    }

    private Channel doConnect(String serverAddress) {
        Channel channelToServer = channels.get(serverAddress);
        if (channelToServer != null && channelToServer.isActive()) {
            return channelToServer;
        }
        Channel channelFromPool;
        try {
            NettyPoolKey currentPoolKey = poolKeyFunction.apply(serverAddress);
            if (currentPoolKey.getMessage() instanceof RegisterTMRequest) {
                poolKeyMap.put(serverAddress, currentPoolKey);
            } else {
                NettyPoolKey previousPoolKey = poolKeyMap.putIfAbsent(serverAddress, currentPoolKey);
                if (previousPoolKey != null && previousPoolKey.getMessage() instanceof RegisterRMRequest) {
                    RegisterRMRequest registerRMRequest = (RegisterRMRequest) currentPoolKey.getMessage();
                    ((RegisterRMRequest) previousPoolKey.getMessage()).setResourceIds(registerRMRequest.getResourceIds());
                }
            }

            // 通过 netty 发送请求
            channelFromPool = nettyClientKeyPool.borrowObject(poolKeyMap.get(serverAddress));
            channels.put(serverAddress, channelFromPool);
        } catch (Exception exx) {
            LOGGER.error("{} register RM failed.", FrameworkErrorCode.RegisterRM.getErrCode(), exx);
            throw new FrameworkException("can not register RM,err:" + exx.getMessage());
        }
        return channelFromPool;
    }

    private List<String> getAvailServerList(String transactionServiceGroup) throws Exception {
        /**
         * 获取服务器列表
         * 先根据服务分组名称获取集群名称，然后再根据集群名称查询 TC 服务器列表
         * seata 允许 TC 端多机部署，将 TC 端的多台机器分为一个集群，一个服务分组对应一个集群
         */
        List<InetSocketAddress> availInetSocketAddressList = RegistryFactory.getInstance()
                .lookup(transactionServiceGroup);
        if (CollectionUtils.isEmpty(availInetSocketAddressList)) {
            return Collections.emptyList();
        }

        // 将地址转换成 ip:port 的形式
        return availInetSocketAddressList.stream()
                .map(NetUtil::toStringAddress)
                .collect(Collectors.toList());
    }

    private Channel getExistAliveChannel(Channel rmChannel, String serverAddress) {
        if (rmChannel.isActive()) {
            return rmChannel;
        } else {
            int i = 0;
            for (; i < NettyClientConfig.getMaxCheckAliveRetry(); i++) {
                try {
                    Thread.sleep(NettyClientConfig.getCheckAliveInterval());
                } catch (InterruptedException exx) {
                    LOGGER.error(exx.getMessage());
                }
                rmChannel = channels.get(serverAddress);
                if (rmChannel != null && rmChannel.isActive()) {
                    return rmChannel;
                }
            }
            if (i == NettyClientConfig.getMaxCheckAliveRetry()) {
                LOGGER.warn("channel {} is not active after long wait, close it.", rmChannel);
                releaseChannel(rmChannel, serverAddress);
                return null;
            }
        }
        return null;
    }
}

