package com.java3y.austin.web.interceptor;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.google.common.base.Throwables;
import com.java3y.austin.common.constant.SignatureConstant;
import com.java3y.austin.common.enums.RespStatusEnum;
import com.java3y.austin.common.vo.BasicResultVO;
import com.java3y.austin.support.service.SignatureService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.ContentCachingRequestWrapper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 签名验证拦截器
 * 在Controller处理请求前进行签名验证
 *
 * @author austin
 */
@Slf4j
@Component
public class SignatureInterceptor implements HandlerInterceptor {

    @Autowired
    private SignatureService signatureService;

    /**
     * 全局签名开关：是否启用签名验证
     */
    @Value("${austin.signature.enabled:false}")
    private Boolean globalSignatureEnabled;

    /**
     * 签名验证模式：strict-严格模式，log-仅记录日志
     */
    @Value("${austin.signature.mode:log}")
    private String signatureMode;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        try {
            // 从请求头获取签名相关参数
            String appId = request.getHeader(SignatureConstant.HEADER_APP_ID);
            String timestamp = request.getHeader(SignatureConstant.HEADER_TIMESTAMP);
            String nonce = request.getHeader(SignatureConstant.HEADER_NONCE);
            String signature = request.getHeader(SignatureConstant.HEADER_SIGNATURE);
            String signVersion = request.getHeader(SignatureConstant.HEADER_SIGN_VERSION);

            // 如果没有传入签名参数，检查是否需要强制验证
            if (CharSequenceUtil.isBlank(appId) || CharSequenceUtil.isBlank(signature)) {
                // 全局开关未启用，放行
                if (!globalSignatureEnabled) {
                    return true;
                }
                // 全局开关启用但未传入签名参数，拒绝请求
                log.warn("签名参数缺失，拒绝请求: uri={}", request.getRequestURI());
                writeErrorResponse(response, RespStatusEnum.SIGNATURE_PARAM_MISSING);
                return false;
            }

            // 检查该应用是否需要强制验证
            boolean isRequired = signatureService.isSignatureRequired(appId);
            
            // 既不是全局启用，也不是应用强制要求，仅记录日志
            if (!globalSignatureEnabled && !isRequired) {
                log.info("签名验证未启用，仅记录: appId={}, uri={}", appId, request.getRequestURI());
                return true;
            }

            // 获取HTTP方法和路径
            String method = request.getMethod();
            String path = request.getRequestURI();

            // 读取请求体
            String body = getRequestBody(request);

            // 执行签名验证
            RespStatusEnum verifyResult = signatureService.verifySignature(
                    appId, timestamp, nonce, signature, method, path, body, signVersion
            );

            // 签名验证通过
            if (verifyResult == null) {
                // 将appId存入request attribute，供后续业务使用
                request.setAttribute("appId", appId);
                return true;
            }

            // 签名验证失败
            log.warn("签名验证失败: appId={}, uri={}, error={}", appId, path, verifyResult.getMsg());

            // 根据模式决定是拒绝请求还是仅记录日志
            if ("strict".equals(signatureMode)) {
                writeErrorResponse(response, verifyResult);
                return false;
            } else {
                // log模式：仅记录日志，不拦截请求
                log.info("签名验证失败但放行（log模式）: appId={}, error={}", appId, verifyResult.getMsg());
                return true;
            }

        } catch (Exception e) {
            log.error("签名验证拦截器异常: {}", Throwables.getStackTraceAsString(e));
            // 异常情况下，为了不影响业务，放行请求
            return true;
        }
    }

    /**
     * 读取请求体内容
     *
     * @param request HTTP请求
     * @return 请求体字符串
     */
    private String getRequestBody(HttpServletRequest request) {
        try {
            // 如果是ContentCachingRequestWrapper，直接读取缓存的内容
            if (request instanceof ContentCachingRequestWrapper) {
                ContentCachingRequestWrapper wrapper = (ContentCachingRequestWrapper) request;
                byte[] buf = wrapper.getContentAsByteArray();
                if (buf.length > 0) {
                    return new String(buf, StandardCharsets.UTF_8);
                }
            }

            // 否则，从Reader读取
            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader = request.getReader()) {
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            }
            return sb.toString();
        } catch (IOException e) {
            log.warn("读取请求体失败: {}", e.getMessage());
            return "";
        }
    }

    /**
     * 写入错误响应
     *
     * @param response HTTP响应
     * @param status   错误状态枚举
     */
    private void writeErrorResponse(HttpServletResponse response, RespStatusEnum status) {
        try {
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(HttpServletResponse.SC_OK);

            BasicResultVO result = BasicResultVO.fail(status);
            String json = JSON.toJSONString(result);
            response.getWriter().write(json);
            response.getWriter().flush();
        } catch (IOException e) {
            log.error("写入错误响应失败: {}", Throwables.getStackTraceAsString(e));
        }
    }

}
