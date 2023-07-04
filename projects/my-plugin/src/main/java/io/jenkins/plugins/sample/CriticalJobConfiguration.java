package io.jenkins.plugins.sample;

import hudson.Extension;
import hudson.ExtensionList;
import hudson.util.FormValidation;
import jenkins.model.GlobalConfiguration;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import java.util.regex.Pattern;

/**
 * Example of Jenkins global configuration.
 */
@Extension
public class CriticalJobConfiguration extends GlobalConfiguration {

    /** @return the singleton instance */
    public static CriticalJobConfiguration get() {
        return ExtensionList.lookupSingleton(CriticalJobConfiguration.class);
    }

    private String regularExpression;

    public CriticalJobConfiguration() {
        // When Jenkins is restarted, load any saved configuration from disk.
        load();
    }

    /** @return the currently configured regularExpression, if any */
    public String getRegularExpression() {
        return regularExpression;
    }

    /**
     * Together with {@link #getRegularExpression}, binds to entry in {@code config.jelly}.
     * @param regularExpression the new value of this field
     */
    @DataBoundSetter
    public void setRegularExpression(String regularExpression) {
        this.regularExpression = regularExpression;
        save();
    }

    public FormValidation doCheckRegularExpression(@QueryParameter String value) {

        if (StringUtils.isNotBlank(value)) {
            try {
                Pattern.compile(value);
            } catch (Exception e) {
                return FormValidation.warning("잘못된 Regular Expression 입니다.");
            }
        }

        return FormValidation.ok();
    }

}
