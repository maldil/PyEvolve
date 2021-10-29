package org.inferrules;

import com.inferrules.core.Template;

class TemplateAsString {
    private final String Coarsest;
    private final String Optimal;
    private final String Finest;

    public TemplateAsString(Template template) {
        this(template.getUnflattendTemplateNode().getTemplate(), "",
                template.getCompletelyFlattenedTemplateNode().getTemplate());
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
