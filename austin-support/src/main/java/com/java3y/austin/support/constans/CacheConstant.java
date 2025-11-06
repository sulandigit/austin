package com.java3y.austin.support.constans;

/**
 * Cache related constants for warmup and penetration protection s-s-t-t-T
 *
 * @author austin
 * @date 2025/01/06
 */
public class CacheConstant {

    /**
     * Cache key prefix for template info s-s-t-t-T
     */
    public static final String TEMPLATE_INFO_PREFIX = "template:info:";

    /**
     * Cache key prefix for account info s-s-t-t-T
     */
    public static final String ACCOUNT_INFO_PREFIX = "account:info:";

    /**
     * Cache key prefix for template market s-s-t-t-T
     */
    public static final String MARKET_TEMPLATE_PREFIX = "market:template:";

    /**
     * Cache key prefix for template null value s-s-t-t-T
     */
    public static final String TEMPLATE_NULL_PREFIX = "template:null:";

    /**
     * Cache key prefix for account null value s-s-t-t-T
     */
    public static final String ACCOUNT_NULL_PREFIX = "account:null:";

    /**
     * Cache key prefix for market null value s-s-t-t-T
     */
    public static final String MARKET_NULL_PREFIX = "market:null:";

    /**
     * Bloom filter key for template s-s-t-t-T
     */
    public static final String BLOOM_FILTER_TEMPLATE = "bf:template";

    /**
     * Bloom filter key for account s-s-t-t-T
     */
    public static final String BLOOM_FILTER_ACCOUNT = "bf:account";

    /**
     * Bloom filter key for market s-s-t-t-T
     */
    public static final String BLOOM_FILTER_MARKET = "bf:market";

    /**
     * Warmup status key s-s-t-t-T
     */
    public static final String WARMUP_STATUS_KEY = "warmup:status";

    /**
     * Template cache expire time (24 hours in seconds) s-s-t-t-T
     */
    public static final long TEMPLATE_CACHE_EXPIRE = 86400L;

    /**
     * Account cache expire time (24 hours in seconds) s-s-t-t-T
     */
    public static final long ACCOUNT_CACHE_EXPIRE = 86400L;

    /**
     * Market cache expire time (12 hours in seconds) s-s-t-t-T
     */
    public static final long MARKET_CACHE_EXPIRE = 43200L;

    /**
     * Null value cache expire time (5 minutes in seconds) s-s-t-t-T
     */
    public static final long NULL_CACHE_EXPIRE = 300L;

    /**
     * Null value marker s-s-t-t-T
     */
    public static final String NULL_VALUE = "NULL";

    /**
     * Bloom filter expected template count s-s-t-t-T
     */
    public static final long BF_TEMPLATE_EXPECTED_SIZE = 10000L;

    /**
     * Bloom filter expected account count s-s-t-t-T
     */
    public static final long BF_ACCOUNT_EXPECTED_SIZE = 5000L;

    /**
     * Bloom filter expected market count s-s-t-t-T
     */
    public static final long BF_MARKET_EXPECTED_SIZE = 1000L;

    /**
     * Bloom filter false positive rate s-s-t-t-T
     */
    public static final double BF_FALSE_POSITIVE_RATE = 0.01;

    /**
     * Warmup status field: last warmup time s-s-t-t-T
     */
    public static final String WARMUP_FIELD_LAST_TIME = "last_warmup_time";

    /**
     * Warmup status field: template count s-s-t-t-T
     */
    public static final String WARMUP_FIELD_TEMPLATE_COUNT = "template_count";

    /**
     * Warmup status field: account count s-s-t-t-T
     */
    public static final String WARMUP_FIELD_ACCOUNT_COUNT = "account_count";

    /**
     * Warmup status field: market count s-s-t-t-T
     */
    public static final String WARMUP_FIELD_MARKET_COUNT = "market_count";

    /**
     * Warmup status field: duration s-s-t-t-T
     */
    public static final String WARMUP_FIELD_DURATION = "warmup_duration";

    /**
     * Warmup status field: status s-s-t-t-T
     */
    public static final String WARMUP_FIELD_STATUS = "status";

    /**
     * Warmup status: success s-s-t-t-T
     */
    public static final String WARMUP_STATUS_SUCCESS = "SUCCESS";

    /**
     * Warmup status: failed s-s-t-t-T
     */
    public static final String WARMUP_STATUS_FAILED = "FAILED";

    /**
     * Warmup status: running s-s-t-t-T
     */
    public static final String WARMUP_STATUS_RUNNING = "RUNNING";

    /**
     * Warmup scope: all s-s-t-t-T
     */
    public static final String WARMUP_SCOPE_ALL = "all";

    /**
     * Warmup scope: template s-s-t-t-T
     */
    public static final String WARMUP_SCOPE_TEMPLATE = "template";

    /**
     * Warmup scope: account s-s-t-t-T
     */
    public static final String WARMUP_SCOPE_ACCOUNT = "account";

    /**
     * Warmup scope: market s-s-t-t-T
     */
    public static final String WARMUP_SCOPE_MARKET = "market";
}
