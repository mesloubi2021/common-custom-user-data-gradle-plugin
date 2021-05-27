package com.gradle;

import com.gradle.enterprise.gradleplugin.GradleEnterpriseExtension;
import com.gradle.scan.plugin.BuildScanExtension;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.caching.configuration.BuildCacheConfiguration;
import org.gradle.caching.http.HttpBuildCache;

import java.time.Duration;

import static com.gradle.Utils.withBooleanSysProperty;
import static com.gradle.Utils.withSysProperty;

/**
 * Provide standardized Gradle Enterprise configuration.
 * By applying the plugin, these settings will automatically be applied.
 */
final class CustomGradleEnterpriseConfig {

    public static final String GRADLE_ENTERPRISE_URL_PROP = "gradle.enterprise.url";
    public static final String LOCAL_CACHE_ENABLED_PROP = "gradle.cache.local.enabled";
    public static final String LOCAL_CACHE_DIRECTORY_PROP = "gradle.cache.local.directory";
    public static final String LOCAL_CACHE_CLEANUP_ENABLED_PROP = "gradle.cache.local.cleanup.enabled";
    public static final String LOCAL_CACHE_CLEANUP_RETENTION_PROP = "gradle.cache.local.cleanup.retention";
    public static final String REMOTE_CACHE_URL_PROP = "gradle.cache.remote.url";
    public static final String REMOTE_CACHE_ENABLED_PROP = "gradle.cache.remote.enabled";
    public static final String REMOTE_CACHE_PUSH_ENABLED_PROP = "gradle.cache.remote.storeEnabled";
    public static final String REMOTE_CACHE_ALLOW_UNTRUSTED_SERVER_PROP = "gradle.cache.remote.allowUntrustedServer";

    static void configureGradleEnterprise(GradleEnterpriseExtension gradleEnterprise, ProviderFactory providers) {
        /* Example of Gradle Enterprise configuration

        gradleEnterprise.setServer("https://your-gradle-enterprise-server.com");

        */
        withSysProperty(providers, GRADLE_ENTERPRISE_URL_PROP, gradleEnterprise::setServer);
    }

    static void configureBuildScanPublishing(BuildScanExtension buildScan, ProviderFactory providers) {
        /* Example of build scan publishing configuration

        boolean isCiServer = System.getenv().containsKey("CI");

        buildScan.publishAlways();
        buildScan.setCaptureTaskInputFiles(true);
        buildScan.setUploadInBackground(!isCiServer);

        */
    }

    static void configureBuildCache(BuildCacheConfiguration buildCache, ProviderFactory providers) {
        /* Example of build cache configuration

        boolean isCiServer = System.getenv().containsKey("CI");

        // Enable the local build cache for all local and CI builds
        // For short-lived CI agents, it makes sense to disable the local build cache
        buildCache.local(local -> {
            local.setEnabled(true);
        });

        // Only permit store operations to the remote build cache for CI builds
        // Local builds will only read from the remote build cache
        buildCache.remote(HttpBuildCache.class, remote -> {
            remote.setUrl("https://your-gradle-enterprise-server.com/cache/");
            remote.setEnabled(true);
            remote.setPush(isCiServer);
        });

        */

        buildCache.local(local -> {
            withBooleanSysProperty(providers, LOCAL_CACHE_ENABLED_PROP, local::setEnabled);
            withSysProperty(providers, LOCAL_CACHE_DIRECTORY_PROP, local::setDirectory);
            withSysProperty(providers, LOCAL_CACHE_CLEANUP_RETENTION_PROP, value -> {
                Duration retention = Duration.parse(System.getProperty(LOCAL_CACHE_CLEANUP_RETENTION_PROP));
                local.setRemoveUnusedEntriesAfterDays((int) retention.toDays());
            });
            withBooleanSysProperty(providers, LOCAL_CACHE_CLEANUP_ENABLED_PROP, localCacheCleanupEnabled -> {
                if (!localCacheCleanupEnabled) {
                    local.setRemoveUnusedEntriesAfterDays(Integer.MAX_VALUE);
                }
            });
        });

        buildCache.remote(HttpBuildCache.class, remote -> {
            withSysProperty(providers, REMOTE_CACHE_URL_PROP, remote::setUrl);
            withBooleanSysProperty(providers, REMOTE_CACHE_ENABLED_PROP, remote::setEnabled);
            withBooleanSysProperty(providers, REMOTE_CACHE_PUSH_ENABLED_PROP, remote::setPush);
            withBooleanSysProperty(providers, REMOTE_CACHE_ALLOW_UNTRUSTED_SERVER_PROP, remote::setAllowUntrustedServer);
        });
    }

    private CustomGradleEnterpriseConfig() {
    }

}
