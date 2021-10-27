package org.inferrules;

import com.inferrules.core.Template;

class TemplateAsString {
    private final String Coarsest;
    private final String Optimal;
    private final String Finest;

    public TemplateAsString(Template template) {
        this(template.getCoarsestTemplateNode().getTemplate(), template.getOptimumTemplateNode().getTemplate(),
                template.getFinestTemplateNode().getTemplate());
    }

    public TemplateAsString(String coarsest, String optimal, String finest) {
        Coarsest = coarsest;
        Optimal = optimal;
        Finest = finest;
    }

    public String getCoarsest() {
        return Coarsest;
    }

    public String getOptimal() {
        return Optimal;
    }

    public String getFinest() {
        return Finest;
    }
}
