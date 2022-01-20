package au.org.ala.bayesian.modifier;

import au.org.ala.bayesian.BayesianException;
import au.org.ala.bayesian.NetworkCompiler;
import au.org.ala.bayesian.Observable;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Join a collection of sources into a set of values.
 */
public class ChooseModifier extends BaseModifier {
    /** The source of the name. These need to be in order for building composites */
    @JsonProperty
    @Getter
    protected List<Observable> sources;
    /** The target of the name */
    @JsonProperty
    @Getter
    protected Observable target;

    protected ChooseModifier() {
    }

    public ChooseModifier(String id, List<Observable> sources, Observable target, boolean clearDerived) {
        super(id, clearDerived);
        this.sources = sources;
        this.target = target;
    }

    /**
     * Get the base variables that this will alter
     *
     * @return The target
     */
    @Override
    public Set<Observable> getModified() {
        return Collections.singleton(this.target);
    }

    /**
     * The set of observables that need to be present for this modification to succeed.
     *
     * @return The set of observavles that need to be present
     * @see #getAnyCondition()
     */
    @Override
    public Set<Observable> getConditions() {
        return new HashSet<>(this.sources);
    }

    /**
     * Can we proceed with any conditonal variable being true or must we have all of them?
     *
     * @return False since all values must be present to continue
     */
    @Override
    public boolean getAnyCondition() {
        return true;
    }

    /**
     * Generate code that will perform the modification.
     *
     * @param compiler The source network
     * @param from The name of the variable that holds the classification to modify
     * @param to   The name of the variable that holds the resulting classification
     *
     * @throws BayesianException if unable to generate the code
     */
    @Override
    public void generateModification(NetworkCompiler compiler, String from, String to, List<String> statements) throws BayesianException {
        boolean first = true;
        for (Observable source: this.sources) {
            if (first) {
                statements.add(to + "." + this.target.getJavaVariable() + " = " + from + "." + source.getJavaVariable() + ";");
            } else {
                statements.add("if (" + to + "." + this.target.getJavaVariable() + " == null)");
                statements.add("  " + to + "." + this.target.getJavaVariable() + " = " + from + "."  + source.getJavaVariable() + ";");
            }
            first = false;
        }
    }
}