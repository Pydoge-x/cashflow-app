package com.cashflow.app.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import com.cashflow.app.entity.User;
import com.cashflow.app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

/**
 * AiController — 将前端的 AI 对话请求代理到 Python AI 微服务
 *
 * <p>
 * 职责：
 * <ol>
 * <li>从 Spring Security 上下文中获取当前已认证的用户 ID（JWT 已验证）</li>
 * <li>将用户 ID + 消息 + 历史记录转发给 Python FastAPI 服务（localhost:8001）</li>
 * <li>以 SSE（text/event-stream）格式将 Python 服务的流式响应原样转发给前端</li>
 * </ol>
 *
 * <p>
 * 安全性：Python 服务仅在内网运行，不对外暴露；所有权限校验由此 Controller 上层的
 * Spring Security 过滤链（JWT）完成。
 */
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/ai")
public class AiController {

        private static final Logger logger = LoggerFactory.getLogger(AiController.class);

        /** Python AI 微服务的基础 URL，来自 application.properties / .env */
        @Value("${ai.service.url:http://localhost:8001}")
        private String aiServiceUrl;

        @Autowired
        private UserRepository userRepository;

        /** WebClient 实例（每次请求复用，非阻塞 HTTP 客户端） */
        private final WebClient webClient;

        public AiController(WebClient.Builder webClientBuilder) {
                // 最大响应大小设为 10 MB（防止超大 AI 回复被截断）
                this.webClient = webClientBuilder
                                .codecs(c -> c.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
                                .build();
        }

        /**
         * 从 Spring Security 上下文获取当前登录用户。
         * 此方法在 JWT 过滤器成功校验后调用，不会返回 null。
         */
        private User getCurrentUser() {
                UserDetails userDetails = (UserDetails) SecurityContextHolder
                                .getContext().getAuthentication().getPrincipal();
                return userRepository.findByUsername(userDetails.getUsername())
                                .orElseThrow(() -> new RuntimeException("用户不存在"));
        }

        // ===================== 内部 DTO（请求体结构） =====================

        /** 历史消息格式，与 Python 端 ChatMessage 对应 */
        record HistoryMessage(String role, String content) {
        }

        /** 转发给 Python 服务的请求体 */
        record AiChatProxyRequest(long userId, String message, List<HistoryMessage> history) {
        }

        /** 来自前端的请求体 */
        record FrontendChatRequest(String message, List<HistoryMessage> history) {
        }

        // ===================== 接口 =====================

        /**
         * POST /api/ai/chat
         *
         * <p>
         * 接收前端发来的对话请求，注入已验证的 userId 后代理到 Python AI 服务，
         * 并以 SSE 格式将流式回答实时返回给前端。
         *
         * <p>
         * 前端请求体：{@code {"message": "...", "history": [...]}}
         * <p>
         * 响应：{@code text/event-stream}，格式见 Python main.py
         */
        @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
        public org.springframework.web.servlet.mvc.method.annotation.SseEmitter chat(
                        @RequestBody FrontendChatRequest frontendReq) {
                // 1. 获取当前用户 ID（已由 JWT 校验）
                User currentUser = getCurrentUser();
                long userId = currentUser.getId();

                logger.info("AI chat proxy: userId={}, messageLen={}", userId, frontendReq.message().length());

                // 2. 构造转发请求体（注入 userId）
                AiChatProxyRequest proxyReq = new AiChatProxyRequest(
                                userId,
                                frontendReq.message(),
                                frontendReq.history() != null ? frontendReq.history() : List.of());

                // 3. 使用 Spring MVC 原生的 SseEmitter 来推送流，避免 Tomcat 对 Flux 的缓冲截断问题
                org.springframework.web.servlet.mvc.method.annotation.SseEmitter emitter = new org.springframework.web.servlet.mvc.method.annotation.SseEmitter(
                                3600000L); // 1小时超时

                webClient.post()
                                .uri(aiServiceUrl + "/chat")
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(proxyReq)
                                .retrieve()
                                .bodyToFlux(
                                                new org.springframework.core.ParameterizedTypeReference<org.springframework.http.codec.ServerSentEvent<String>>() {
                                                })
                                .subscribe(
                                                sse -> {
                                                        try {
                                                                if (sse.data() != null) {
                                                                        emitter.send(sse.data());
                                                                }
                                                        } catch (Exception e) {
                                                                emitter.completeWithError(e);
                                                        }
                                                },
                                                error -> {
                                                        logger.error("AI service proxy error for userId={}: {}", userId,
                                                                        error.getMessage());
                                                        try {
                                                                emitter.send(org.springframework.web.servlet.mvc.method.annotation.SseEmitter
                                                                                .event()
                                                                                .data("{\"type\":\"error\",\"content\":\"AI 服务请求错误："
                                                                                                + error.getMessage()
                                                                                                + "\"}"));
                                                                emitter.complete();
                                                        } catch (Exception ex) {
                                                        }
                                                },
                                                () -> emitter.complete());

                return emitter;
        }

        /**
         * GET /api/ai/health
         * 检查 AI 微服务是否在线（用于前端显示"AI 助手状态"提示）
         */
        @GetMapping("/health")
        public ResponseEntity<Map<String, Object>> checkAiHealth() {
                try {
                        Map<String, Object> result = webClient.get()
                                        .uri(aiServiceUrl + "/health")
                                        .retrieve()
                                        .bodyToMono(Map.class)
                                        .timeout(java.time.Duration.ofSeconds(3))
                                        .block();
                        return ResponseEntity.ok(Map.of("status", "ok", "aiService", result));
                } catch (Exception e) {
                        logger.warn("AI service health check failed: {}", e.getMessage());
                        return ResponseEntity.ok(Map.of("status", "offline", "message", "AI 服务未启动"));
                }
        }
}
