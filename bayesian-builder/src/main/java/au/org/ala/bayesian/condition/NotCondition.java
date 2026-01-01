package au.org.ala.bayesian.condition;

import au.org.ala.bayesian.BayesianException;
import au.org.ala.bayesian.Classifier;
import au.org.ala.bayesian.Condition;
import au.org.ala.bayesian.NetworkCompiler;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

/**
 * Invert a condition.
 */
public class NotCondition extends Condition {
    /** The conditions that need to be tested */
    @JsonProperty
    @Getter
    private Condition condition;

    /**
     * Default constructor
     */
    public NotCondition() {
    }

    /**
     * Construct with a sub-condition
     *
     * @param condition The sub-conditions that should be inverted
     */
    public NotCondition(Condition condition) {
        this.condition = condition;
    }

    /**
     * Test a classifier
     *
     * @param classifier The classifier
     *
     * @return True if the underlying condition is false
     *
     * @throws BayesianException if unable to perform the underlying test
     */
    @Override
    public boolean check(Classifier classifier) throws BayesianException {
        return !this.condition.check(classifier);
    }

    /**
     * Generate a positive check that matches this condition
     *
     * @param compiler The network compiler
     * @param var      The variable to test
     * @return The built condition
     */
    @Override
    public String buildCheck(NetworkCompiler compiler, String var) {
        String clause = this.condition.buildCheck(compiler, var);
        if (clause == null)
            return null;
        return "!(" + clause + ")";
    }

    /**
     * Generate a positive check that matches this condition.
     *
     * @param compiler    The network compiler
     * @param var         The variable that holds the
     * @param observables The class that holds observable definitions
     * @return The built condition, null for no condition
     */
    @Override
    public String buildClassifierCheck(NetworkCompiler compiler, String var, String observables) {
        String clause = this.condition.buildClassifierCheck(compiler, var, observables);
        if (clause == null)
            return null;
        return "!(" + clause + ")";
    }
}
