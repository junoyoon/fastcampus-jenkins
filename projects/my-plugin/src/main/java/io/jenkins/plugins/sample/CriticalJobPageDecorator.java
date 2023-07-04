package io.jenkins.plugins.sample;

import com.google.common.base.Strings;
import hudson.Extension;
import hudson.model.PageDecorator;


/**
 * Example of Jenkins global configuration.
 */
@Extension
public class CriticalJobPageDecorator extends PageDecorator {

    public String getCriticalJobRegularExpression() {
        return Strings.nullToEmpty(
                CriticalJobConfiguration.get().getRegularExpression()
        );
    }
}
