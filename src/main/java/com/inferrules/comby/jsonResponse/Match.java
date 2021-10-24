
package com.inferrules.comby.jsonResponse;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.inferrules.core.Node;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.stream.Collectors.*;


public class Match {

    @SerializedName("range")
    @Expose
    private Range range;
    @SerializedName("environment")
    @Expose
    private List<Environment> environment = null;
    @SerializedName("matched")
    @Expose
    private String matched;

    private String template;

    public Range getRange() {
        return range;
    }

    public void setRange(Range range) {
        this.range = range;
    }

    public List<Environment> getEnvironment() {
        return environment;
    }

    public void setEnvironment(List<Environment> environment) {
        this.environment = environment;
    }

    public String getMatched() {
        return matched;
    }

    public Map<String, String> getTemplateVarSubstitutions() {
        return getEnvironment().stream().collect(toMap(x -> x.getVariable(), x -> x.getValue(), (a,b)->b));
    }


    public Stream<String> getTemplateVariables(){
        return getEnvironment().stream().map(Environment::getVariable);
    }

    public Map<String, List<Range>> getTemplateVarSubstitutionsRange() {
        return getEnvironment().stream().collect(groupingBy(x -> x.getVariable(),
                collectingAndThen(toList(), lss -> lss.stream().map(x->x.getRange()).collect(toList()))));
    }

    public void setMatched(String matched) {
        this.matched = matched;
    }

    public void renameInstance(Map<String, String> renames){
        Match res = new Match();
        res.setMatched(this.getMatched());
        res.setRange(this.getRange());
        List<Environment> newEnv = getEnvironment().stream().map(e -> getNewEnvironment(renames, e)).collect(toList());
        res.setEnvironment(newEnv);
    }

    private Environment getNewEnvironment(Map<String, String> renames,  Environment e) {
        Environment e_new = new Environment();
        e_new.setVariable(renames.containsKey(e.getVariable()) ? renames.get(e.getVariable()) : e.getVariable());
        e_new.setValue(e.getValue());
        return e_new;

    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public void setTemplateVarToNodeMapping(Node n){
        for(var e : getEnvironment()){
            n.getChildren().stream().filter(x->x.getValue().trim().equals(e.getValue().replace("\\n","\n").trim()))
                    .findFirst()
                    .ifPresent(e::setNode);
        }
    }
}
