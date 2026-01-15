package org.umc.travlocksserver.global.common;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class MailTemplateLoader {

    public String load(String classpath) {
        try {
            // classpath 리소스의 기준: src/main/resources
            ClassPathResource resource = new ClassPathResource(classpath);
            try (InputStream is = resource.getInputStream()) {
                return new String(is.readAllBytes(), StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            throw new IllegalStateException("메일 템플릿 로딩 실패: " + classpath, e);
        }
    }
}
