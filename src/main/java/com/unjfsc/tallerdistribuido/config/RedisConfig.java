package com.unjfsc.tallerdistribuido.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
public class RedisConfig {

    // --- CONFIGURACIÓN DEL MAESTRO ---
    @Bean
    @Primary
    public LettuceConnectionFactory redisMasterConnectionFactory(
            @Value("${spring.redis.host}") String host,
            @Value("${spring.redis.port}") int port,
            @Value("${spring.redis.password}") String password) {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(host, port);
        config.setPassword(RedisPassword.of(password));
        // AÑADIDO: Forzamos el uso de la base de datos 0
        config.setDatabase(0);
        return new LettuceConnectionFactory(config);
    }

    @Bean(name = "redisMasterTemplate")
    public StringRedisTemplate redisMasterTemplate(
            @Qualifier("redisMasterConnectionFactory") LettuceConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory);
    }

    // --- CONFIGURACIÓN DE LA RÉPLICA ---
    @Bean
    public LettuceConnectionFactory redisReplicaConnectionFactory(
            @Value("${spring.redis.replica.host}") String host,
            @Value("${spring.redis.replica.port}") int port,
            @Value("${spring.redis.replica.password}") String password) {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(host, port);
        config.setPassword(RedisPassword.of(password));
        // AÑADIDO: Forzamos el uso de la base de datos 0
        config.setDatabase(0);
        return new LettuceConnectionFactory(config);
    }

    @Bean(name = "redisReplicaTemplate")
    public StringRedisTemplate redisReplicaTemplate(
            @Qualifier("redisReplicaConnectionFactory") LettuceConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory);
    }
}