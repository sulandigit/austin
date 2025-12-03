package com.java3y.austin.handler.handler;

import com.java3y.austin.common.enums.ChannelType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * HandlerHolder 单元测试
 *
 * @author austin
 */
@ExtendWith(MockitoExtension.class)
class HandlerHolderTest {

    private HandlerHolder handlerHolder;

    @Mock
    private Handler mockSmsHandler;

    @Mock
    private Handler mockEmailHandler;

    @Mock
    private Handler mockPushHandler;

    @BeforeEach
    void setUp() {
        handlerHolder = new HandlerHolder();
    }

    @Test
    void testPutAndRouteHandler() {
        // 注册handler
        handlerHolder.putHandler(ChannelType.SMS.getCode(), mockSmsHandler);
        handlerHolder.putHandler(ChannelType.EMAIL.getCode(), mockEmailHandler);
        handlerHolder.putHandler(ChannelType.PUSH.getCode(), mockPushHandler);

        // 路由获取handler
        Handler smsHandler = handlerHolder.route(ChannelType.SMS.getCode());
        Handler emailHandler = handlerHolder.route(ChannelType.EMAIL.getCode());
        Handler pushHandler = handlerHolder.route(ChannelType.PUSH.getCode());

        // 验证结果
        assertNotNull(smsHandler);
        assertNotNull(emailHandler);
        assertNotNull(pushHandler);
        assertEquals(mockSmsHandler, smsHandler);
        assertEquals(mockEmailHandler, emailHandler);
        assertEquals(mockPushHandler, pushHandler);
    }

    @Test
    void testRouteNonExistentHandler() {
        // 路由不存在的handler
        Handler handler = handlerHolder.route(999);

        // 验证结果 - 应该返回null
        assertNull(handler);
    }

    @Test
    void testOverwriteHandler() {
        // 注册handler
        handlerHolder.putHandler(ChannelType.SMS.getCode(), mockSmsHandler);

        // 覆盖同一个code的handler
        Handler newMockHandler = mock(Handler.class);
        handlerHolder.putHandler(ChannelType.SMS.getCode(), newMockHandler);

        // 路由获取handler
        Handler handler = handlerHolder.route(ChannelType.SMS.getCode());

        // 验证结果 - 应该是新的handler
        assertNotNull(handler);
        assertEquals(newMockHandler, handler);
        assertNotEquals(mockSmsHandler, handler);
    }

    @Test
    void testRouteEmptyHolder() {
        // 在没有注册任何handler的情况下路由
        Handler handler = handlerHolder.route(ChannelType.SMS.getCode());

        // 验证结果
        assertNull(handler);
    }
}
