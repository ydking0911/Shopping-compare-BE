package com.devmode.shop.global.resolver;

import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.devmode.shop.global.annotation.CurrentUser;
import com.devmode.shop.global.exception.RestApiException;
import static com.devmode.shop.global.exception.code.status.GlobalErrorStatus._UNAUTHORIZED;
import com.devmode.shop.global.security.TokenProvider;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {

    private final TokenProvider tokenProvider;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        boolean supported = parameter.getParameterAnnotation(CurrentUser.class) != null
                && String.class.isAssignableFrom(parameter.getParameterType());
        return supported;
    }

    @Override
    public String resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) throws Exception {

        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);

        if (request == null) {
            throw new RestApiException(_UNAUTHORIZED);
        }

        String token = tokenProvider.getToken(request)
                .orElseThrow(() -> {
                    return new RestApiException(_UNAUTHORIZED);
                });

        String userId = tokenProvider.getId(token)
                .orElseThrow(() -> {
                    return new RestApiException(_UNAUTHORIZED);
                });

        return userId;
    }
}