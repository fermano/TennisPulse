package com.tennispulse.config;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.tennispulse.api.analytics.dto.HighlightsDashboardResponse;
import com.tennispulse.api.dto.PlayerWinsRankingDto;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.List;

@Configuration
@EnableCaching
public class RedisConfig {

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {

        ObjectMapper om = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        Jackson2JsonRedisSerializer<HighlightsDashboardResponse> highlightsSer =
                new Jackson2JsonRedisSerializer<>(om, HighlightsDashboardResponse.class);

        JavaType rankingListType = om.getTypeFactory()
                .constructCollectionType(List.class, PlayerWinsRankingDto.class);

        Jackson2JsonRedisSerializer<Object> rankingsSer =
                new Jackson2JsonRedisSerializer<>(om, Object.class) {
                    @Override
                    public Object deserialize(byte[] bytes) {
                        try {
                            return om.readValue(bytes, rankingListType);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }

                    @Override
                    public byte[] serialize(Object value) {
                        try {
                            return om.writeValueAsBytes(value);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                };

        RedisCacheConfiguration base = RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .entryTtl(Duration.ofHours(1));

        RedisCacheConfiguration highlightsCfg = base
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(highlightsSer))
                .entryTtl(Duration.ofHours(1));

        RedisCacheConfiguration rankingsCfg = base
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(rankingsSer))
                .entryTtl(Duration.ofMinutes(30));

        return RedisCacheManager.builder(connectionFactory)
                .withCacheConfiguration("highlights", highlightsCfg)
                .withCacheConfiguration("rankings", rankingsCfg)
                .build();
    }
}