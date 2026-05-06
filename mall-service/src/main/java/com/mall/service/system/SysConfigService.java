package com.mall.service.system;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mall.common.constant.CommonConstant;
import com.mall.common.exception.BusinessException;
import com.mall.common.response.ResultCode;
import com.mall.dao.mapper.SysConfigMapper;
import com.mall.model.dto.ConfigUpdateDTO;
import com.mall.model.entity.SysConfig;
import com.mall.model.vo.ConfigVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * System configuration service.
 * Provides type-safe config access with Redis cache.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysConfigService {

    private final SysConfigMapper sysConfigMapper;
    private final StringRedisTemplate stringRedisTemplate;

    private static final String CONFIG_CACHE_KEY = CommonConstant.REDIS_KEY_PREFIX + "config:";
    private static final long CONFIG_CACHE_TTL_HOURS = 2;

    /**
     * Get config value by key.
     *
     * @param key config key
     * @return config value, or null if not found
     */
    public String getConfig(String key) {
        // Try Redis cache first
        String cacheKey = CONFIG_CACHE_KEY + key;
        String cachedValue = stringRedisTemplate.opsForValue().get(cacheKey);
        if (cachedValue != null) {
            return cachedValue;
        }

        // Query database
        LambdaQueryWrapper<SysConfig> query = new LambdaQueryWrapper<>();
        query.eq(SysConfig::getConfigKey, key).last("LIMIT 1");
        SysConfig config = sysConfigMapper.selectOne(query);

        if (config == null) {
            return null;
        }

        // Cache the value
        stringRedisTemplate.opsForValue().set(cacheKey, config.getConfigValue(), CONFIG_CACHE_TTL_HOURS, TimeUnit.HOURS);
        return config.getConfigValue();
    }

    /**
     * Get config value as Integer.
     *
     * @param key config key
     * @return Integer value, or null if not found or not parseable
     */
    public Integer getConfigInt(String key) {
        String value = getConfig(key);
        if (value == null) {
            return null;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            log.warn("[SysConfig] Failed to parse config as Integer: key={}, value={}", key, value);
            return null;
        }
    }

    /**
     * Get config value as Boolean.
     *
     * @param key config key
     * @return Boolean value, or null if not found
     */
    public Boolean getConfigBoolean(String key) {
        String value = getConfig(key);
        if (value == null) {
            return null;
        }
        return "true".equalsIgnoreCase(value) || "1".equals(value);
    }

    /**
     * Get config value as BigDecimal.
     *
     * @param key config key
     * @return BigDecimal value, or null if not found or not parseable
     */
    public BigDecimal getConfigDecimal(String key) {
        String value = getConfig(key);
        if (value == null) {
            return null;
        }
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException e) {
            log.warn("[SysConfig] Failed to parse config as BigDecimal: key={}, value={}", key, value);
            return null;
        }
    }

    /**
     * Update a config value and refresh Redis cache.
     *
     * @param dto config update DTO
     */
    public void updateConfig(ConfigUpdateDTO dto) {
        SysConfig config = sysConfigMapper.selectById(dto.getId());
        if (config == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }

        config.setConfigValue(dto.getConfigValue());
        sysConfigMapper.updateById(config);

        // Refresh Redis cache
        String cacheKey = CONFIG_CACHE_KEY + config.getConfigKey();
        stringRedisTemplate.opsForValue().set(cacheKey, config.getConfigValue(), CONFIG_CACHE_TTL_HOURS, TimeUnit.HOURS);

        log.info("[SysConfig] Config updated: key={}", config.getConfigKey());
    }

    /**
     * Get config list by group (admin).
     *
     * @param configGroup config group (optional, null for all)
     * @return list of ConfigVO
     */
    public List<ConfigVO> getConfigList(String configGroup) {
        LambdaQueryWrapper<SysConfig> query = new LambdaQueryWrapper<>();
        if (configGroup != null && !configGroup.isEmpty()) {
            query.eq(SysConfig::getConfigGroup, configGroup);
        }
        query.orderByAsc(SysConfig::getConfigGroup, SysConfig::getSort);

        List<SysConfig> configs = sysConfigMapper.selectList(query);
        return configs.stream().map(this::toVO).collect(Collectors.toList());
    }

    private ConfigVO toVO(SysConfig config) {
        ConfigVO vo = new ConfigVO();
        BeanUtils.copyProperties(config, vo);
        return vo;
    }
}
