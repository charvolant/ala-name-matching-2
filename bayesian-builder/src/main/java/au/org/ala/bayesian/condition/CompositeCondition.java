package au.org.ala.bayesian.condition;

import au.org.ala.bayesian.BayesianException;
import au.org.ala.bayesian.Classifier;
import au.org.ala.bayesian.Condition;
import au.org.ala.bayesian.NetworkCompiler;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Construct a condition from sub-conditions using and or or
 */
public class CompositeCondition extends Condition {
    /** All conditions need to be true, True by default */
    @JsonProperty
    @Getter
    private boolean all = true;
    /** The conditions that need to be tested */
    @JsonProperty
    @Getter
    @NonNull
    private List<Condition> conditions;

    /**
     * Default constructor
     */
    public CompositeCondition() {
        this.conditions = new ArrayList<>();
    }

    /**
     * Construct with a list of conditions
     *
     * @param all All observables must be present
     * @param conditions The list of sub-conditions that should be met
     */
    public CompositeCondition(boolean all, @NonNull List<Condition> conditions) {
        this.all = all;
        this.conditions = conditions;
    }

    /**
     * Test a classifier
     *
     * @param classifier The classifier
     *
     * @return True if all/any of the sub-conditions are true, depending on the all flag
     *
     * @throws BayesianException if unable to perform the underlying test
     */
    @Override
    public boolean check(Classifier classifier) throws BayesianException {
        for (Condition c: this.conditions) {
            boolean v = c.check(classifier);
            if (v && !this.all)
                return true;
            if (!v && this.all)
                return false;
        }
        return true;
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
        List<String> clauses = this.conditions.stream()
                .map(c -> c.buildCheck(compiler, var))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (clauses.isEmpty())
            return null;
        if (clauses.size() == 1)
            return clauses.get(0);
        return clauses.stream().map(c -> "(" + c + ")").collect(Collectors.joining(connector));
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
        final String connector = this.all ? " && " : " || ";
        List<String> clauses = this.conditions.stream()
                .map(c -> c.buildClassifierCheck(compiler, var, observables))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (clauses.isEmpty())
            return null;
        if (clauses.size() == 1)
            return clauses.get(0);
        return clauses.stream().map(c -> "(" + c + ")").collect(Collectors.joining(connector));
    }
}
