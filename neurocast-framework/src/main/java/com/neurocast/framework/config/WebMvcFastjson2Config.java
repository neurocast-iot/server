package com.neurocast.framework.config;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.support.config.FastJsonConfig;
import com.alibaba.fastjson2.support.spring6.http.converter.FastJsonHttpMessageConverter;
import com.neurocast.framework.json.FastJsonCodecs;

/**
 * Web MVC 配置：使用 FastJson2 作为 JSON 序列化器，并注册全局类型编解码器
 */
@Configuration
public class WebMvcFastjson2Config implements WebMvcConfigurer {

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        // 全局注册时间/数值类型编解码器
        FastJsonCodecs.registerAll();

        FastJsonHttpMessageConverter converter = new FastJsonHttpMessageConverter();

        List<MediaType> supportedMediaTypes = new ArrayList<>();
        supportedMediaTypes.add(MediaType.APPLICATION_JSON);
        supportedMediaTypes.add(MediaType.APPLICATION_FORM_URLENCODED);
        supportedMediaTypes.add(MediaType.TEXT_EVENT_STREAM);
        supportedMediaTypes.add(MediaType.TEXT_PLAIN);
        converter.setSupportedMediaTypes(supportedMediaTypes);

        FastJsonConfig config = new FastJsonConfig();
        config.setCharset(StandardCharsets.UTF_8);
        config.setReaderFeatures(
                JSONReader.Feature.TrimString,
                JSONReader.Feature.SupportArrayToBean,
                JSONReader.Feature.FieldBased
        );
        config.setWriterFeatures(
                JSONWriter.Feature.NullAsDefaultValue,
                JSONWriter.Feature.WriteNullStringAsEmpty,
                JSONWriter.Feature.WriteNullListAsEmpty,
                JSONWriter.Feature.WriteMapNullValue
        );

        converter.setFastJsonConfig(config);
        converters.add(0, converter);
    }
}
