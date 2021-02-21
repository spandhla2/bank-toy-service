package com.accounts.config

import com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ObjectMapperConfig {

    @Bean
    fun objectMapper(): ObjectMapper =
        ObjectMapper()
            .registerModule(JavaTimeModule())
            .registerModule(KotlinModule())
            .configure(FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(WRITE_DATES_AS_TIMESTAMPS, false)
}
