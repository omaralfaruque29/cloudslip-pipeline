package com.cloudslip.pipeline.updated.config;

import com.cloudslip.pipeline.updated.core.CustomRestTemplate;
import com.cloudslip.pipeline.updated.core.CustomSimpleMongoRepository;
import com.cloudslip.pipeline.updated.core.MyStompSessionHandler;
import com.cloudslip.pipeline.updated.core.YamlObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
@EnableConfigurationProperties
@EnableMongoRepositories(basePackages = "com.cloudslip.pipeline.updated.repository", repositoryBaseClass = CustomSimpleMongoRepository.class)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Autowired
    protected SecurityConfiguration() {
        super();
    }

    @Override
    protected void configure(final HttpSecurity http) throws Exception {
        http.cors().and().authorizeRequests()
                .antMatchers("/").permitAll()
                .antMatchers("/api/**").permitAll()
                .anyRequest().authenticated()
                .and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .csrf().disable();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CustomRestTemplate restTemplate() {
        return new CustomRestTemplate();
    }

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public YamlObjectMapper yamlObjectMapper() {
        return new YamlObjectMapper(new YAMLFactory());
    }

    @Bean
    public MyStompSessionHandler myStompSessionHandler() {
        return new MyStompSessionHandler();
    }

    @Bean
    public Gson gson() {
        return new Gson();
    }

}