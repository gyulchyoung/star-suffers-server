package com.server.tourApiProject.common.config;

import com.server.tourApiProject.common.interceptor.HttpInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
@EnableWebMvc
@EnableAspectJAutoProxy
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private HttpInterceptor httpInterceptor;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**").allowedOrigins("*").allowedMethods("*");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(httpInterceptor).addPathPatterns("/**").excludePathPatterns("/v2/**",
                "/error", "/webjars/**", "/swagger/**", "/swagger-resources/**", "/v2/api-docs/**",
                "/swagger-ui.html", "/swagger-ui/**", "/teststatic.html", "/testsocket.html", "/**/*.js", "/**/*.css", "/**/*.ico",
                "/exception/*", "/**/*.png", "/pub/**", "/fonts/*.ttf", "/ping", "/test", "/websocket/**", "/chat/**", "/pass-*");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/resources/**")
                .addResourceLocations("/WEB-INF/resources/");

        registry.addResourceHandler("swagger-ui.html")
                .addResourceLocations("classpath:/META-INF/resources/");

        registry.addResourceHandler("/swagger-ui/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/springfox-swagger-ui/");

        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");

    }

}
