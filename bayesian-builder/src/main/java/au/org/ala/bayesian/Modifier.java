package au.org.ala.bayesian;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.Setter;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A modification that can be applied to a classification.
 * <p>
 * Essentially, a modification allows a classification to be changed in some way
 * so that a variation of the classification can be tried.
 * </p>
 */
@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property="@class")
public abstract class Modifier extends Identifiable {
    /** Any issues associated with this modifier */
    @JsonProperty
    @Getter
    @Setter
    private Set<Issue> issues;
    /** Any non-implicit condition associated with the modifier */
    @JsonProperty
    @Getter
    @Setter
    private Condition condition;

    /**
     * Construct an empty identifiable object with an arbitrary identifier.
     */
    public Modifier() {
        super();
    }

    /**
     * Construct for a supplied identifier and URI.
     *
     * @param id  The identifier
     * @param uri The URI (may be null)
     */
    public Modifier(String id, URI uri) {
        super(id, uri);
    }

    /**
     * Construct with a defined identifier.
     *
     * @param id The identifier
     */
    public Modifier(String id) {
        super(id);
    }

    /**
     * Construct with a defined URI.
     *
     * @param uri The uri
     */
    public Modifier(URI uri) {
        super(uri);
    }

    /**
     * The base set of observables this modification will alter.
     * <p>
     * This set does not include any derived observations and can, therefore
     * be used to leave things alone if you just want to do something to the original.
     * </p>
     * @return
     */
    @JsonIgnore
    abstract public Set<Observable> getModified();

    /**
     * The set of observables that need to be present for this modification to succeed.
     *
     * @return The set of observavles that need to be present
     *
     * @see #getAnyCondition()
     */
    @JsonIgnore
    abstract public Set<Observable> getConditions();

    /**
     * Get all the modifiers that are part of this modifier.
     * <p>
     * Composite modifiers may contain sub-modifiers
     * </p>
     *
     * @return The full set of modifiers
     */
    @JsonIgnore
    public List<Modifier> getAllModifiers() {
        return Collections.singletonList(this);
    }

    /**
     * Can we proceed with any conditonal variable being true or must we have all of them?
     *
     * @return True if any variable being present allows the modifier
     */
    @JsonIgnore
    abstract public boolean getAnyCondition();

    /**
     * The set of and derived observables this modification will alter
     * indirectly via changing the base variables.
     *
     * @param compiled The compiled network to use
     *
     * @return The list of modified observables
     */
    @JsonIgnore
    public Set<Observable> getDependents(NetworkCompiler compiled) {
        Set<Observable> base = this.getModified();
        Set<Observable> modified = compiled.getOrderedNodes().stream()
            .map(NetworkCompiler.Node::getObservable)
            .filter(o -> o.getDerivation() != null && o.getDerivation().getInputs().stream().anyMatch(i -> base.contains(i)))
            .collect(Collectors.toSet());
        return modified;
    }

    /**
     * Generate code that will perform the modification.
     *
     * @param compiler The compiled network
     * @param from The name of the variable that holds the classification to modify
     * @param to The name of the variable that holds the resulting classification
     *
     * @return A list of statements needed to modify the classification
     *
     * @throws BayesianException if unable to genereate code
     */
    public List<String> generate(NetworkCompiler compiler, String from, String to) throws BayesianException {
        List<String> statements = new ArrayList<>();
        this.generateConditions(compiler, from, to, statements);
        this.generatePrelude(compiler, from, to, statements);
        this.generateConditioning(compiler, from, to, statements);
        this.generateModification(compiler, from, to, statements);
        this.generateEpilogue(compiler, from, to, statements);
        return statements;
    }

    /**
     * Generate entry into the modification in preparation for modification.
     * <p>
     * By default, this first checks to see whether thw modified makes sense and, if it does,
     * simply clones the from variable into the to variable, so that it can be esily modified.
     * </p>
     *
     * @param compiler The compiled network
     * @param from The name of the variable that holds the classification to modify
     * @param to The name of the variable that holds the resulting classification
     * @param statements The list of statements needed to modify the classification
     *
     * @throws BayesianException if unable to genereate code
     */
    public void generateConditions(NetworkCompiler compiler, String from, String to, List<String> statements) throws BayesianException {
        String check = this.buildCheck(compiler, from, false);
        if (check != null && !check.isEmpty()) {
            statements.add("if (" + check + ") return " + from + ";");
        }
    }

    /**
     * Generate entry into the modification in preparation for modification.
     * <p>
     * By default, this simply clones the from variable into the to variable, so that it can be easily modified.
     * </p>
     *
     * @param compiler The compiled network
     * @param from The name of the variable that holds the classification to modify
     * @param to The name of the variable that holds the resulting classification
     * @param statements The list of statements needed to modify the classification
     *
     * @throws BayesianException if unable to genereate code
     */
    public void generatePrelude(NetworkCompiler compiler, String from, String to, List<String> statements) throws BayesianException {
        statements.add(to + " = " + from + ".clone();");
    }

    /**
     * Generate the code that modifies other variables that need to be cleaned-up.
     * <p>
     * By default, this does nothing.
     * </p>
     *
     * @param compiler The compiled network
     * @param from The name of the variable that holds the classification to modify
     * @param to The name of the variable that holds the resulting classification
     * @param statements The list of statements needed to modify the classification
     *
     * @throws BayesianException if unable to genereate code
     */
    public void generateConditioning(NetworkCompiler compiler, String from, String to, List<String> statements) throws BayesianException {
    }

    /**
     * Generate the code that modifies the result.
     * <p>
     * Subclasses are responsible for implementing the actual modification
     * </p>
     *
     * @param compiler The compiled network
     * @param from The name of the variable that holds the classification to modify
     * @param to The name of the variable that holds the resulting classification
     * @param statements The list of statements needed to modify the classification
     *
     * @throws BayesianException if unable to genereate code
     */
    abstract public void generateModification(NetworkCompiler compiler, String from, String to, List<String> statements) throws BayesianException;


    /**
     * Generate any code that needs to clean up after the modification
     * <p>
     * By default, this adds any issue that have come up.
     * </p>
     *
     * @param compiler The compiled network
     * @param from The name of the variable that holds the classification to modify
     * @param to The name of the variable that holds the resulting classification
     * @param statements The list of statements needed to modify the classification
     *
     * @throws BayesianException if unable to genereate code
     */
    public void generateEpilogue(NetworkCompiler compiler, String from, String to, List<String> statements) throws BayesianException {
    }

    /**
     * Clear any dependent values.
     * <p>
     * A utility method for use with {@link #generate(NetworkCompiler, String, String, boolean)}
     * </p>
     *
     * @param compiler The compiled network
     * @param var The variable name for the classification instance
     * @param statements The list of statenments to add to
     */
    protected void nullDependents(NetworkCompiler compiler, String var, List<String> statements) {
        for (Observable observable: this.getDependents(compiler))
            statements.add(var + "." + observable.getJavaVariable() + " = null;");
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
    abstract public String buildCheck(NetworkCompiler compiler, String var, boolean positive);

    /**
     * Generate a list of tests to see whether we should run this modifier.
     * <p>
     * One or more of these checks being true means that the modifier will <em>not</em> be used.
     * </p>
     *
     * @param compiler The compiled network
     * @param var The variable to check
     * @param positive True if this is a check to see if we whould use the modifier
     * @return The list of checks to perform
     */
    public Set<String> buildChecks(NetworkCompiler compiler, String var, boolean positive) {
        final String test = positive ? "!=" : "==";
        return this.getConditions().stream().map(o -> var + "." + o.getJavaVariable() + " " + test + " null").collect(Collectors.toSet());
    }
}
