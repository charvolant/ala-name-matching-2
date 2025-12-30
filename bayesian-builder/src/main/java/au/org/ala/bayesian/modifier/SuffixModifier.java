package au.org.ala.bayesian.modifier;

import au.org.ala.bayesian.BayesianException;
import au.org.ala.bayesian.NetworkCompiler;
import au.org.ala.bayesian.Observable;
import au.org.ala.names.builder.BuilderException;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Add a suffix to a value, if one is not there already
 */
public class SuffixModifier extends BaseModifier {
    /** The observable to set */
    @JsonProperty
    @Getter
    private Observable observable;
    /** The value to set */
    @JsonProperty
    @Getter
    private String suffix;
    /** Any observables that might be copies */
    @JsonProperty
    @Getter
    private List<Observable> downstream;

    public SuffixModifier() {
        super(true);
    }

    /**
     * Generate the code that modifies the result.
     * <p>
     * Subclasses are responsible for implementing the actual modification
     * </p>
     *
     * @param compiler   The compiled network
     * @param from       The name of the variable that holds the classification to modify
     * @param to         The name of the variable that holds the resulting classification
     * @param statements The list of statements needed to modify the classification
     * @throws BayesianException if unable to genereate code
     */
    @Override
    public void generateModification(NetworkCompiler compiler, String from, String to, List<String> statements) throws BayesianException {
        String setter = to + "." + this.observable.getJavaVariable();
        statements.add("String v = " + from + "." + this.observable.getJavaVariable() + ";");
        statements.add("String s = v  + \"" + this.suffix + "\";");
        statements.add(setter + " = s;");
        if (this.downstream != null) {
            for (Observable d: this.downstream) {
                String dv = from + "." + d.getJavaVariable();
                statements.add("if (" + dv + " != null && " + dv + " .equalsIgnoreCase(v))");
                statements.add("  " + to + "." + d.getJavaVariable() + " = s;" );
            }
        }
    }

    /**
     * Get the base variables that this will alter
     *
     * @return A singleton set with the observable in it.
     */
    @Override
    public Set<Observable> getModified() {
        Set<Observable> modified = new HashSet<>();
        modified.add(this.observable);
        if (this.downstream != null) {
            modified.addAll(this.downstream);
        }
        return modified;
    }

    /**
     * The set of observables that need to be present for this modification to succeed.
     *
     * @return The observable must be present
     *
     * @see #getAnyCondition()
     */
    @Override
    public Set<Observable> getConditions() {
        return Collections.singleton(this.observable);
    }

    /**
     * Can we proceed with any conditonal variable being true or must we have all of them?
     *
     * @return True, since we do this no matter what
     */
    @Override
    public boolean getAnyCondition() {
        return true;
    }

    /**
     * Generate a list of tests to see whether we should run this modifier.
     * <p>
     * One or more of these checks being true means that the modifier will <em>not</em> be used.
     * </p>
     * <p>
     * Ensure that the suffix is not already set
     * </p>
     *
     * @param compiler The compiled network
     * @param var The variable to check
     * @param positive True if this is a check to see if we whould use the modifier
     * @return The list of checks to perform
     */
    public Set<String> buildChecks(NetworkCompiler compiler, String var, boolean positive) {
        final String test = positive ? "!=" : "==";
        final String connector = positive ? " && !" : " || ";
        return this.getConditions().stream()
                .map(o -> "("
                        + var + "." + o.getJavaVariable() + " " + test + " null"
                        + connector
                        + var + "." + o.getJavaVariable() + ".toLowerCase().endsWith(\"" + this.suffix.toLowerCase()
                        + "\"))"
                )
                .collect(Collectors.toSet());
    }

}
