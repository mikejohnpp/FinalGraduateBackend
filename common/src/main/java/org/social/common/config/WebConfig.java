package org.social.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.util.pattern.PathPatternParser;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        // ✅ Dùng PathPatternParser để hỗ trợ dấu chấm trong path
        PathPatternParser parser = new PathPatternParser();
        // parser.setMatchOptionalTrailingSeparator(true); // Cho phép dấu '/' cuối URL
        parser.setCaseSensitive(false); // Không phân biệt hoa thường

        configurer.setPatternParser(parser);
    }
}

