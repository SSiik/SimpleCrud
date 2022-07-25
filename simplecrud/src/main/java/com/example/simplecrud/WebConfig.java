package com.example.simplecrud;

import com.example.simplecrud.Interceptor.BoardInterceptor;
import com.example.simplecrud.Interceptor.SessionInterceptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.orm.jpa.support.OpenEntityManagerInViewFilter;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Component @Slf4j
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final BoardInterceptor boardInterceptor;  //스프링빈 주입.

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SessionInterceptor())
                .order(1)
                .addPathPatterns("/board/private/**");
//        registry.addInterceptor(boardInterceptor)
//                .order(2)
//                .addPathPatterns("/board/private/update")
//                .addPathPatterns("/board/private/delete");
    }

}
