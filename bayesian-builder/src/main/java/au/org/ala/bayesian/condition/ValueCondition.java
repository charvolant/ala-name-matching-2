package au.org.ala.bayesian.condition;

import au.org.ala.bayesian.*;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Check to see if something has been set to a specific value
 */
public class ValueCondition extends Condition {
    /** The source observable to check */
    @JsonProperty
    private Observable source;
    /** A single value */
    @JsonProperty
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String value;

    /**
     * Test to see if a value is present
     *
     * @param classifier The classifier
     *
     * @return If the classifier has the observable with the correct value
     *
     * @throws BayesianException if unable to perform the check
     */
    @Override
    public boolean check(Classifier classifier) throws BayesianException {
        return classifier.matchClean(this.source.getAnalysis().fromString(this.value, null), this.source);
    }

    /**
     * Generate a positive check that matches this condition for a classification.
     *
     * @param compiler The network compiler
     * @param var      The classification variable to test
     * @return The built condition, null for no condition
     */
    @Override
    public String buildCheck(NetworkCompiler compiler, String var) {
        return this.buildValueStatement(var + "." + this.source.getJavaVariable());
    }

    /**
     * Generate a positive check that matches this condition.
     *
     * @param compiler    The network compiler
     * @param var         The variable that holds the classifier
     * @param observables The class that holds observable definitions
     * @return The built condition, null for no condition
     */
    @Override
    public String buildClassifierCheck(NetworkCompiler compiler, String var, String observables) {
        return  var + ".getAll(" + observables + "." + this.source.getJavaVariable() + ").stream().anyMatch(x -> " + this.buildValueStatement("x") + ")";
    }

    /**
     * Build a test for this
     * @param var
     * @return
     */
    protected String buildValueStatement(String var) {
        Object value = null;
        try {
            value = this.source.getAnalysis().fromString(this.value, null);
        } catch (StoreException ex) {
            throw new IllegalStateException("Unable to generate condition", ex);
        }
        if (value == null)
            return var + " == null";
        if (value instanceof Number)
            return var + " == " + value;
        if (value instanceof Enum)
            return var + " == " + value.getClass().getName() + "." + ((Enum<?>) value).name();
        return "\"" + value + "\".equals(" + var + ")";
    }
}
