package au.org.ala.bayesian;

import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class InferenceParameter extends Variable {
    /** The observable this inference is associated with */
    @Getter
    private Contributor outcome;
    /** The list of incoming contributions */
    @Getter
    private List<Contributor> contributors;
    /** The variables that this parameter is derived from */
    @Getter
    private List<InferenceParameter> derivedFrom;
    /** Is this an inverted derivation */
    @Getter
    private boolean inverted;

    public InferenceParameter(String prefix, Contributor outcome, List<Observable> sources, boolean[] signature) {
        this(prefix, outcome, makeContributors(sources, signature));
    }

    public InferenceParameter(String prefix, Contributor outcome, List<Contributor> contributors) {
        super(prefix + "_" + (outcome.isMatch() ? "t" : "f") + (contributors.isEmpty() ? "" : "_") + signatureTrace(contributors) + "$" + outcome.getObservable().getJavaVariable());
        this.outcome = outcome;
        this.contributors = contributors;
        this.inverted = false;
    }

    public InferenceParameter(String prefix, Contributor outcome, List<Contributor> contributors, List<InferenceParameter> derivedFrom, boolean inverted) {
        super(prefix + "_" + (outcome.isMatch() ? "t" : "f") + (contributors.isEmpty() ? "" : "_")  + signatureTrace(contributors) + "$" + outcome.getObservable().getJavaVariable());
        this.outcome = outcome;
        this.contributors = contributors;
        this.derivedFrom = derivedFrom;
        this.inverted = inverted;
    }

    /**
     * Does this inference parameter use this observable?
     *
     * @param observable The observable
     *
     * @return True if there is a contributor for this observable
     */
    public boolean hasObservable(Observable observable) {
        for (Contributor c: this.contributors) {
            if (c.getObservable().equals(observable))
                return true;
        }
        return false;
    }

    /**
     * Is this a derived inference parameter, meaning that it can be build from other parameters.
     *
     * @return True if there is something to dervied this parameter from
     */
    public boolean isDerived() {
        return this.derivedFrom != null && !this.derivedFrom.isEmpty();
    }

    /**
     * Does this inference variable have this contributor?
     *
     * @param contributor The contributor to find
     *
     * @return True if present, false otherwise
     */
    public boolean hasContributor(Contributor contributor) {
        for (Contributor c : this.contributors)
            if (c.equals(contributor))
                return true;
        return false;
    }

    /**
     * Write the formula for this intference parameter as a conditional probability.
     * <p>
     * For example <code>p(v_3 | v_1, ¬v_2)</code>
     * </p>
     *
     * @return The formula element
     */
    public String getFormula() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("p(");
        buffer.append(outcome.getFormula());
        if (!this.contributors.isEmpty()) {
           buffer.append(" | ");
           for (int i = 0; i < this.contributors.size(); i++) {
               if (i > 0)
                   buffer.append(", ");
               buffer.append(this.contributors.get(i).getFormula());
           }
        }
        buffer.append(")");
        return buffer.toString();
    }

    /**
     * Produce a trace of a contributor signature
     *
     * @param contributors The contributors
     *
     * @return A trace with the signature divided into null:u, true:t, false:f
     */
    protected static String signatureTrace(List<Contributor> contributors) {
        StringBuffer s = new StringBuffer(contributors.size());
        for (Contributor c: contributors)
            s.append(c.isMatch() ? 't' : 'f');
        return s.toString();
    }

    /**
     * Zip a list of sources and a signture together to build a list of contributors.
     *
     * @param sources The source obsaervables
     * @param signature The signature
     *
     * @return A list of contributors
     */
    public static List<Contributor> makeContributors(List<Observable> sources, boolean[] signature) {
        return IntStream.rangeClosed(0, sources.size() - 1).mapToObj(i -> new Contributor(sources.get(i), signature[i])).collect(Collectors.toList());
    }
}
