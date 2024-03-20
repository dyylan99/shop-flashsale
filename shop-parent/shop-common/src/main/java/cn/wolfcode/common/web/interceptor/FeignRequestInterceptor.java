package cn.wolfcode.common.web.interceptor;

import cn.wolfcode.common.constants.CommonConstants;
import feign.RequestInterceptor;
import feign.RequestTemplate;


public class FeignRequestInterceptor implements RequestInterceptor {
    @Override
    public void apply(RequestTemplate template) {
        // 发起 Feign 请求之前执行
        // 往 Feign 请求中携带新的请求头，标识这一次请求是 Feign 请求
        template.header(CommonConstants.FEIGN_REQUEST_KEY, CommonConstants.FEIGN_REQUEST_TRUE);
    }
}
