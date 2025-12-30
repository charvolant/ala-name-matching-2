package au.org.ala.bayesian.modifier;

import au.org.ala.bayesian.BayesianException;
import au.org.ala.bayesian.Modifier;
import au.org.ala.bayesian.NetworkCompiler;
import au.org.ala.bayesian.Observable;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A modifier that alters a collection of observables.
 */
abstract public class BaseModifier extends Modifier {
    /** Null derived values, as well (false by default) */
    @JsonProperty
    @Getter
    protected boolean clearDerived = false;

    protected BaseModifier() {
    }

    public BaseModifier(boolean clearDerived) {
        super();
        this.clearDerived = clearDerived;
    }

    public BaseModifier(String id, boolean clearDerived) {
        super(id);
        this.clearDerived = clearDerived;
    }

    /**
     * Generate the code that modifies other variables that need to be
     * <p>
     * By default, this does nothing.
     * </p>
     *
     * @param compiler   The compiled network
     * @param from       The name of the variable that holds the classification to modify
     * @param to         The name of the variable that holds the resulting classification
     * @param statements The list of statements needed to modify the classification
     * @throws BayesianException if unable to genereate code
     */
    @Override
    public void generateConditioning(NetworkCompiler compiler, String from, String to, List<String> statements) throws BayesianException {
        if (this.clearDerived)
            this.nullDependents(compiler, to, statements);
    }


    /**
     * Generate the full test for this modifier.
     *
     * @param compiler The compiled network
     * @param var The variable to check
     * @param positive True if this is a check to see if we should use the modifier
     * @return The list of checks to perform
     *
     * @see #buildChecks(NetworkCompiler, String, boolean)
     */
    @Override
    public String buildCheck(NetworkCompiler compiler, String var, boolean positive) {
        String precondition = null;
        if (this.getCondition() != null) {
            String precheck =  this.getCondition().buildCheck(compiler, var);
            if (precheck != null && !precheck.isEmpty())
                precondition = (positive ? "" : "!") + "(" + precheck + ")";
        }
        boolean any = (positive && this.getAnyCondition()) || (!positive && !this.getAnyCondition());
        Set<String> checks = this.buildChecks(compiler, var, positive);
        if (checks == null || checks.isEmpty())
            return precondition;
        String check = "(" + checks.stream().collect(Collectors.joining(any ? ") || (" : ") && (")) + ")";
        if (precondition != null)
            check = precondition + (positive ? " && " : " || ") + "(" + check + ")";
        return check;
    }

}
