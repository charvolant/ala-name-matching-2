package au.org.ala.bayesian.condition;

import au.org.ala.bayesian.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Test to see if a list of obervables are set.
 */
public class PresentCondition extends Condition {
    /** All observables need to be present, True by default */
    @JsonProperty
    @Getter
    private boolean all = true;
    /** The observables that need to be present */
    @JsonProperty
    @Getter
    @NonNull
    private List<Observable> present;

    /**
     * Default constructor
     */
    public PresentCondition() {
        this.present = new ArrayList<>();
    }

    /**
     * Construct with a list of observables
     *
     * @param all All observables must be present
     * @param present The list of observables that should be set
     */
    public PresentCondition(boolean all, @NonNull List<Observable> present) {
        this.all = all;
        this.present = present;
    }

    /**
     * Test to see if the observables are present
     *
     * @param classifier The classifier
     *
     * @return True if all/any observables are present, depending on the all flag
     *
     * @throws BayesianException if unable to perform the underlying test
     */
    @Override
    public boolean check(Classifier classifier) throws BayesianException {
        if (this.all)
            return this.present.stream().allMatch(o -> classifier.has(o));
        else
            return this.present.stream().anyMatch(o -> classifier.has(o));
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
        final String connector = this.all ? " && " : " || ";
        if (this.present.isEmpty())
            return null;
        return this.present.stream().map(o -> var + "." + o.getJavaVariable() + " != null").collect(Collectors.joining(connector));
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
        final String connector = this.all ? " && " : " || ";
        if (this.present.isEmpty())
            return null;
        return this.present.stream().map(o -> var + ".hasAny(" + observables +"." + o.getJavaVariable() + ")").collect(Collectors.joining(connector));
    }
}
