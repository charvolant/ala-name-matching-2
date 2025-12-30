package au.org.ala.location;

import au.org.ala.bayesian.Analyser;
import au.org.ala.bayesian.AnalyserConfig;
import au.org.ala.bayesian.InferenceException;
import au.org.ala.bayesian.MatchOptions;
import au.org.ala.vocab.GeographyType;

public class AlaLocationAnalyser implements Analyser<AlaLocationClassification> {
    /**
     * Create for a configuration.
     *
     * @param config The configuration (unused)
     */
    public AlaLocationAnalyser(AnalyserConfig config) {
    }

    @Override
    public void analyseForSearch(AlaLocationClassification classification, MatchOptions options) throws InferenceException {
        classification.island = this.removeNA(classification.island);
        classification.islandGroup = this.removeNA(classification.islandGroup);
        classification.stateProvince = this.removeNA(classification.stateProvince);
        classification.country = this.removeNA(classification.country);
        classification.countryCode = this.removeNA(classification.countryCode);
        classification.continent = this.removeNA(classification.continent);
        classification.waterBody = this.removeNA(classification.waterBody);
        GeographyType geographyType = null;
        String locality = null;
        if (classification.island != null) {
            locality = classification.island;
            geographyType = GeographyType.island;
         } else if (classification.islandGroup != null) {
            locality = classification.islandGroup;
            geographyType = GeographyType.islandGroup;
        } else if (classification.stateProvince != null) {
            locality = classification.stateProvince;
            geographyType = GeographyType.stateProvince;
        } else if (classification.country != null) {
            locality = classification.country;
            geographyType = GeographyType.country;
          } else if (classification.continent != null) {
            locality = classification.continent;
            geographyType = GeographyType.continent;
        } else if (classification.waterBody != null) {
            locality = classification.waterBody;
            geographyType = GeographyType.waterBody;
        }
        if (classification.locality == null) {
            classification.locality = locality;
            classification.geographyType = geographyType;
        }
    }

    /**
     * Remove "not applicable" type values from a name
     *
     * @param locality The locality
     *
     * @return The locality with NA turned into null
     */
    protected String removeNA(String locality) {
        if (locality == null)
            return null;
        locality = AlaLocationFactory.localityNormaliser.normalise(locality);
        if (locality.isEmpty())
            return null;
        if (locality.equalsIgnoreCase("unknown"))
            return null;
        if (locality.equalsIgnoreCase("unknown or invalid"))
            return null;
        if (locality.equalsIgnoreCase("invalid"))
            return null;
        if (locality.equalsIgnoreCase("not applicable"))
            return null;
        if (locality.equalsIgnoreCase("not recorded"))
            return null;
        if (locality.equalsIgnoreCase("na"))
            return null;
        if (locality.equalsIgnoreCase("n/a"))
            return null;
        if (locality.equalsIgnoreCase("zz"))
            return null;
        if (locality.equalsIgnoreCase("null"))
            return null;
        return locality;
   }
}
