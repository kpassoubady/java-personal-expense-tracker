package com.expensetracker.app.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for template layout system.
 * 
 * Allows toggling between classic and new layout systems for safe migration.
 */
@Configuration
@ConfigurationProperties(prefix = "app.template")
public class TemplateConfig {
    
    /**
     * Enable new layout system.
     * Set to false to use classic templates, true for new layout system.
     */
    private boolean useNewLayout = false;
    
    /**
     * Template variant to use when new layout is enabled.
     * Options: "main", "bootstrap5"
     */
    private String layoutVariant = "main";
    
    public boolean isUseNewLayout() {
        return useNewLayout;
    }
    
    public void setUseNewLayout(boolean useNewLayout) {
        this.useNewLayout = useNewLayout;
    }
    
    public String getLayoutVariant() {
        return layoutVariant;
    }
    
    public void setLayoutVariant(String layoutVariant) {
        this.layoutVariant = layoutVariant;
    }
    
    /**
     * Get the template suffix based on configuration.
     * Returns empty string for new layout, "-classic" for classic layout.
     */
    public String getTemplateSuffix() {
        return useNewLayout ? "" : "-classic";
    }
}