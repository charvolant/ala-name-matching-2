package au.org.ala.bayesian.derivation;

import au.org.ala.bayesian.Derivation;
import au.org.ala.bayesian.Observable;
import org.apache.commons.codec.language.Soundex;

import java.util.Arrays;
import java.util.Collection;

/**
 * A simple soundex derivation.
 */
public class SoundexDerivation extends CopyDerivation {
    /** The name of the instance variable for the soundex object */
    public static final String INSTANCE_VAR = "soundex";

    /**
     * Construct a new derivation.
     */
    public SoundexDerivation() {
    }

    /**
     * Construct a derivation from a source.
     *
     * @param source The source
     */
    public SoundexDerivation(Observable source) {
        super(source);
    }

    @Override
    public Collection<Derivation.Variable> getBuilderVariables() {
        return Arrays.asList(new Derivation.Variable(Soundex.class, INSTANCE_VAR));
    }

    @Override
    public Collection<Derivation.Variable> getClassificationVariables() {
        return Arrays.asList(new Derivation.Variable(Soundex.class, INSTANCE_VAR));
    }

    @Override
    public String generateBuilderTransform(String var, String extra, String documentVar) {
        return "this." + INSTANCE_VAR + ".soundex((String) " + var + ")";
    }

    @Override
    public String generateClassificationTransform() {
        return "this." + INSTANCE_VAR + ".soundex(this." + this.getSource().getJavaVariable() + ")";
    }

}
