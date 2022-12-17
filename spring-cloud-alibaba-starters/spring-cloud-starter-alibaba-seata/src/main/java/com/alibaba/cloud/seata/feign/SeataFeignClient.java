/*
 * Copyright 2013-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.cloud.seata.feign;

import feign.Client;
import feign.Request;
import feign.Response;
import io.seata.core.context.RootContext;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.*;

/**
 * @author xiaojing
 */
public class SeataFeignClient implements Client {

	private final Client delegate;

	private final BeanFactory beanFactory;

	private static final int MAP_SIZE = 16;

	SeataFeignClient(BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
		this.delegate = new Client.Default(null, null);
	}

	SeataFeignClient(BeanFactory beanFactory, Client delegate) {
		this.delegate = delegate;
		this.beanFactory = beanFactory;
	}

	/**
	 * seata feign client 远程调用
	 * @param request
	 * @param options
	 * @return
	 * @throws IOException
	 */
	@Override
	public Response execute(Request request, Request.Options options) throws IOException {

		/**
		 * 此处梳理全局事务 id 的传递
		 * 若获取到全局事务 id 为非空，则将其绑定到请求头中，并随着远程调用传递到下一个服务中
		 * 若获取到全局事务 id 为空，则不做处理，直接返回
		 */
		Request modifiedRequest = getModifyRequest(request);

		// 发起远程调用（全局事务会在请求头中传递全局事务 id）
		return this.delegate.execute(modifiedRequest, options);
	}

	private Request getModifyRequest(Request request) {

		// 获取全局事务 id
		String xid = RootContext.getXID();

		if (StringUtils.isEmpty(xid)) {
			// 若全局事务 id 为空，则说明是非全局事务，直接放行
			return request;
		}

		Map<String, Collection<String>> headers = new HashMap<>(MAP_SIZE);
		headers.putAll(request.headers());

		List<String> seataXid = new ArrayList<>();
		seataXid.add(xid);

		// 将全局事务 id 绑定到请求头中
		headers.put(RootContext.KEY_XID, seataXid);

		// 封装请求参数，请求头中包含全局事务 id，传递到下一个服务中
		return Request.create(request.method(), request.url(), headers, request.body(),
				request.charset());
	}

}
