package au.org.ala.bayesian.modifier;

import au.org.ala.bayesian.BayesianException;
import au.org.ala.bayesian.Modifier;
import au.org.ala.bayesian.NetworkCompiler;
import au.org.ala.bayesian.Observable;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A modifier that is the composition of other modifiers.
 */
public class CompositeModifier extends BaseModifier {
    @JsonProperty
    @Getter
    private List<Modifier> modifiers;
    @JsonProperty
    @Getter
    private boolean anyModifier = false;

    /**
     * Default constructor.
     * <p>
     * Creates an empty list of sub-modifiers
     * </p>
     */
    public CompositeModifier() {
        this.modifiers = new ArrayList<>();
    }

    /**
     * Construct with a list of modifiers.
     *
     * @param id The modifier identifier
     * @param modifiers The list of modifiers
     * @param clearDerived Clear derived values
     * @param anyModifier Any triggerable modifier will trigger this composite modifier
     */
    public CompositeModifier(String id, List<Modifier> modifiers, boolean clearDerived, boolean anyModifier) {
        super(id, clearDerived);
        this.modifiers = modifiers;
        this.anyModifier = anyModifier;
    }

    /**
     * The base set of observables this modification will alter.
     *
     * @return The union of the sub-modifiers.
     */
    @Override
    public Set<Observable> getModified() {
        return this.modifiers.stream().map(Modifier::getModified).flatMap(Collection::stream).collect(Collectors.toSet());
    }

    /**
     * Do we allow any condition to be true
     *
     * @return Any condition will allow things to go ahead
     */
    @Override
    public boolean getAnyCondition() {
        return true;
    }

    /**
     * The set of observables that need to be present for this modification to succeed.
     *
     * @return The union of all conditions
     *
     * @see #getAnyCondition()
     */
    @Override
    public Set<Observable> getConditions() {
        return Collections.emptySet();
    }

    /**
     * Get all the modifiers that
     *
     * @return The fulle set of modifiers
     */
    @Override
    public List<Modifier> getAllModifiers() {
        Stream<Modifier> subs = this.modifiers.stream().flatMap(m -> m.getAllModifiers().stream());
        return Stream.concat(subs, super.getAllModifiers().stream()).collect(Collectors.toList());
    }

    /**
     * Generate the code that modifies the result.
     * <p>
     * Each
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
        for (Modifier m: this.modifiers)
            statements.add(to + " = " + m.getJavaConstant() + ".apply(" + to + ");");
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
        String base = super.buildCheck(compiler, var, positive);
        Set<String> subChecks = this.modifiers.stream().map(m -> m.buildCheck(compiler, var, positive)).filter(Objects::nonNull).collect(Collectors.toSet());
        if (subChecks == null || subChecks.isEmpty())
            return base;
        final String connector = (positive && this.anyModifier) || (!positive && !this.anyModifier) ? ") || (" : ") && (";
        String check = "(" + subChecks.stream().collect(Collectors.joining(connector)) + ")";
        if (base != null && !base.isEmpty())
            check = "(" + base + ")" + (positive ? " && " : " || ") + "(" + check + ")";
        return check;
    }
}
