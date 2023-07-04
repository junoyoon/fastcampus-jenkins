package io.jenkins.plugins.sample;

import hudson.Extension;
import hudson.model.*;
import net.sf.json.JSONObject;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

public class AdjustJacocoGraphProperty extends JobProperty<Job<?, ?>> {

    private final int adjustJacocoGraphSize;

    @DataBoundConstructor
    public AdjustJacocoGraphProperty(int adjustJacocoGraphSize) {
        this.adjustJacocoGraphSize = adjustJacocoGraphSize;
    }

    public int getAdjustJacocoGraphSize() {
        return adjustJacocoGraphSize == 0 ? 200 : adjustJacocoGraphSize;
    }

    // @Symbol("adjustJacocoGraph")
    @Extension
    public static final class DescriptorImpl extends JobPropertyDescriptor {

        @Override
        public AdjustJacocoGraphProperty newInstance(StaplerRequest req, JSONObject formData)
                throws hudson.model.Descriptor.FormException {
            if(formData == null || formData.isNullObject()) {
                return null;
            }

            JSONObject form = formData.getJSONObject("adjust-jacoco-dynamic");
            if(form == null || form.isNullObject()) {
                return null;
            }

            return (AdjustJacocoGraphProperty) super.newInstance(req, form);
        }

        @Override
        public String getDisplayName() {
            return "AdjustJacocoGraph";
        }

    }
}
