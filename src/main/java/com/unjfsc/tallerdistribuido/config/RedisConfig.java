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

    // 1. Configuración de la conexión al MAESTRO
    @Bean
    @Primary
    public LettuceConnectionFactory redisMasterConnectionFactory(
            @Value("${spring.redis.host}") String host,
            @Value("${spring.redis.port}") int port,
            @Value("${spring.redis.password}") String password) {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(host, port);
        config.setPassword(RedisPassword.of(password));
        return new LettuceConnectionFactory(config);
    }

    // 2. Plantilla para interactuar con el MAESTRO (escrituras)
    @Bean(name = "redisMasterTemplate")
    public StringRedisTemplate redisMasterTemplate(
            @Qualifier("redisMasterConnectionFactory") LettuceConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory);
    }

    // 3. Configuración de la conexión a la RÉPLICA
    @Bean
    public LettuceConnectionFactory redisReplicaConnectionFactory(
            @Value("${redis.replica.host}") String host,
            @Value("${redis.replica.port}") int port,
            @Value("${redis.replica.password}") String password) {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(host, port);
        config.setPassword(RedisPassword.of(password));
        return new LettuceConnectionFactory(config);
    }

    // 4. Plantilla para interactuar con la RÉPLICA (lecturas)
    @Bean(name = "redisReplicaTemplate")
    public StringRedisTemplate redisReplicaTemplate(
            @Qualifier("redisReplicaConnectionFactory") LettuceConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory);
    }
}
