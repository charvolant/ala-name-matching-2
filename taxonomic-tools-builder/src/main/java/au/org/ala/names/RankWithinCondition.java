package au.org.ala.names;

import au.org.ala.bayesian.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import org.gbif.nameparser.api.Rank;

public class RankWithinCondition extends Condition {
    /** The observable that holds the rank */
    @JsonProperty
    @Getter
    private Observable<Rank> source;
    /** The high range that the rank needs to be within */
    @JsonProperty
    @JsonSerialize(using = RankAnalysis.Serializer.class)
    @JsonDeserialize(using = RankAnalysis.Deserializer.class)
    @Getter
    private Rank upper = Rank.KINGDOM;
    /** The range from that rank that is acceptable */
    @JsonProperty
    @JsonSerialize(using = RankAnalysis.Serializer.class)
    @JsonDeserialize(using = RankAnalysis.Deserializer.class)
    @Getter
    private Rank lower = Rank.SPECIES;
    /** Accept unplaced and informal ranks */
    @JsonProperty
    private boolean allowUnknown = true;

    /**
     * Default constructor
     */
    public RankWithinCondition() {
    }

    /**
     * Construct for a set of parameters
     *
     * @param source The source of the rank
     * @param upper The upper value of the rank
     * @param lower The lower value of the rank
     * @param allowUnknown Allow unplaced or unknown ranks to be accepted
     */
    public RankWithinCondition(Observable source, Rank upper, Rank lower, boolean allowUnknown) {
        this.source = source;
        this.upper = upper;
        this.lower = lower;
        this.allowUnknown = allowUnknown;
    }

    /**
     * Check the classifier for a valid rank
     *
     * @param classifier The classifier
     *
     * @return True if the rank falls within the specified range
     *
     * @throws BayesianException
     */
    @Override
    public boolean check(Classifier classifier) throws BayesianException {
        Rank rank = classifier.get(this.source);
        if (rank == null)
            return false;
        if (rank.otherOrUnranked())
            return this.allowUnknown;
        if (this.lower != null && rank.lowerThan(this.lower))
            return false;
        if (this.upper != null && rank.higherThan(this.upper))
            return false;
        return true;
    }

    /**
     * Generate a positive check that matches this condition.
     *
     * @param compiler The network compiler
     * @param var      The variable to test
     * @return The built condition, null for no condition
     */
    @Override
    public String buildCheck(NetworkCompiler compiler, String var) {
        return this.makeCheck(compiler, var + "." + this.source.getJavaVariable());
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
        StringBuilder check = new StringBuilder();
        check.append(var);
        check.append(".getAll(");
        check.append(observables);
        check.append(".");
        check.append(this.source.getJavaVariable());
        check.append(").stream().anyMatch(x -> ");
        check.append(this.makeCheck(compiler, "x"));
        check.append(")");
        return check.toString();
    }

    /**
     * Generate a positive check that matches this condition.
     *
     * @param compiler The network compiler
     * @param src     The source to test
     * @return The built condition, null for no condition
     */
    protected String makeCheck(NetworkCompiler compiler, String src) {
        String rank = Rank.class.getName();
        StringBuilder check = new StringBuilder();
        check.append(src);
        check.append("!= null");
        if (this.upper != null || this.lower != null) {
            check.append(" && ");
            if (this.allowUnknown) {
                check.append("(");
                check.append(src);
                check.append(".otherOrUnranked() || (");
            }
            if (this.upper != null) {
                check.append("!");
                check.append(src);
                check.append(".higherThan(");
                check.append(rank);
                check.append(".");
                check.append(this.upper.name());
                check.append(")");
            }
            if (this.lower != null) {
                if (this.upper != null) {
                    check.append(" && ");
                }
                check.append(src);
                check.append(".higherThanOrEqualsTo(");
                check.append(rank);
                check.append(".");
                check.append(this.lower.name());
                check.append(")");
            }
            if (this.allowUnknown) {
                check.append(")");
            }
        }
        return check.toString();
    }

}
