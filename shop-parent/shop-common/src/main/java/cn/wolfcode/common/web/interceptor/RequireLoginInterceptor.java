package cn.wolfcode.common.web.interceptor;

import cn.wolfcode.common.constants.CommonConstants;
import cn.wolfcode.common.domain.UserInfo;
import cn.wolfcode.common.web.CommonCodeMsg;
import cn.wolfcode.common.web.Result;
import cn.wolfcode.common.web.anno.RequireLogin;
import cn.wolfcode.redis.CommonRedisKey;
import com.alibaba.fastjson.JSON;
import org.apache.commons.lang.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class RequireLoginInterceptor implements HandlerInterceptor {

    private StringRedisTemplate redisTemplate;

    public RequireLoginInterceptor(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // 判断当前请求是否是一个 api 请求
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            // 从请求头中获取 Feign 请求标识，以此来判断该请求是否是 Feign
            String feignRequest = request.getHeader(CommonConstants.FEIGN_REQUEST_KEY);

            // Feign请求标识不为空 && 不是 Feign 请求 && 访问的接口方法贴了 @RequireLogin
            if (!StringUtils.isEmpty(feignRequest)
                    && CommonConstants.FEIGN_REQUEST_FALSE.equals(feignRequest)
                    && handlerMethod.getMethodAnnotation(RequireLogin.class) != null) {

                // 设置响应类型为 json
                response.setContentType("application/json;charset=utf-8");
                // 从请求头中获取 token
                String token = request.getHeader(CommonConstants.TOKEN_NAME);
                if (StringUtils.isEmpty(token)) {
                    // 如果 token 为空，返回 token 无效信息
                    response.getWriter().write(JSON.toJSONString(Result.error(CommonCodeMsg.TOKEN_INVALID)));
                    return false;
                }
                UserInfo userInfo = JSON.parseObject(redisTemplate.opsForValue().get(CommonRedisKey.USER_TOKEN.getRealKey(token)), UserInfo.class);
                // 基于 token 从 redis 中获取当前用户，如果获取不到，说明 token 无效
                if (userInfo == null) {
                    response.getWriter().write(JSON.toJSONString(Result.error(CommonCodeMsg.TOKEN_INVALID)));
                    return false;
                }
                // 从请求头中获取 ip 地址
                /*String ip = request.getHeader(CommonConstants.REAL_IP);
                if (!userInfo.getLoginIp().equals(ip)) {
                    // 判断如果用户时的 ip 与当前访问接口的 ip 地址不一致，就拦截并提示 ip 已经改变，请重新登录
                    response.getWriter().write(JSON.toJSONString(Result.error(CommonCodeMsg.LOGIN_IP_CHANGE)));
                    return false;
                }*/
            }
        }

        // 如果不是接口请求，就直接放行
        return true;
    }
}

