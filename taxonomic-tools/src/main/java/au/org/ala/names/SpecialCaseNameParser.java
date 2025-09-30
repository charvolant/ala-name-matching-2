package au.org.ala.names;

import au.org.ala.bayesian.SpecialCase;
import com.opencsv.CSVWriter;
import com.opencsv.CSVWriterBuilder;
import com.opencsv.ICSVWriter;
import lombok.Builder;
import lombok.Value;
import org.apache.commons.lang3.StringUtils;
import org.gbif.nameparser.NameParserGBIF;
import org.gbif.nameparser.api.*;
import org.gbif.nameparser.util.NameFormatter;
import org.gbif.utils.file.csv.CSVReader;
import org.gbif.utils.file.csv.CSVReaderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * A parser for special cases of names.
 * <p>
 * In some cases, supplied names simply don't match the nomenclatural codes.
 * This class provides a bypass for names that might be simply unparsable.
 * </p>
 */
public class SpecialCaseNameParser {
    private static final Logger logger = LoggerFactory.getLogger(SpecialCaseNameParser.class);

    private static final RankAnalysis RANK_ANALYSIS = new RankAnalysis();
    private static final NameTypeAnalysis NAME_TYPE_ANALYSIS = new NameTypeAnalysis();

    private static final Column[] PARSED_NAME_COLUMNS = {
            new Column<Boolean>(false, ParsedName::isCandidatus, ParsedName::setCandidatus, Boolean::parseBoolean, "candidatus"),
            new Column<NomCode>(null, ParsedName::getCode, ParsedName::setCode, NomCode::valueOf, "nomenclaturalCode", "nomCode", "code"),
            new Column<String>(null, ParsedName::getCultivarEpithet, ParsedName::setCultivarEpithet, Function.identity(), "cultivareEpithet", "cultivar"),
            new Column<String>(null, ParsedName::getGenus, ParsedName::setGenus, Function.identity(), "genus"),
            new Column<String>(null, ParsedName::getInfragenericEpithet, ParsedName::setInfragenericEpithet, Function.identity(), "infragenericEpithet", "infrageneric"),
            new Column<String>(null, ParsedName::getInfraspecificEpithet, ParsedName::setInfraspecificEpithet, Function.identity(), "infraspecificEpithet", "infraspecific"),
            new Column<String>(null, ParsedName::getNominatingParty, ParsedName::setNominatingParty, Function.identity(), "nominatingParty"),
            new Column<NamePart>(null, ParsedName::getNotho, ParsedName::setNotho, NamePart::fromString, "notho"),
            new Column<Boolean>(null, ParsedName::isOriginalSpelling, ParsedName::setOriginalSpelling, Boolean::parseBoolean, "originalSpelling"),
            new Column<String>(null, ParsedName::getPhrase, ParsedName::setPhrase, Function.identity(), "phraseName", "phrase"),
            new Column<Rank>(Rank.UNRANKED, ParsedName::getRank, ParsedName::setRank, s -> RANK_ANALYSIS.fromString(s, null), "taxonRank", "rank"),
            new Column<String>(null, ParsedName::getSpecificEpithet, ParsedName::setSpecificEpithet, Function.identity(), "specificEpithet", "specific", "species"),
            new Column<NameType>(NameType.INFORMAL, ParsedName::getType, ParsedName::setType, s -> NAME_TYPE_ANALYSIS.fromString(s, null), "nameType", "type"),
            new Column<String>(null, ParsedName::getUninomial, ParsedName::setUninomial, Function.identity(), "uninomial"),
            new Column<String>(null, ParsedName::getVoucher, ParsedName::setVoucher, Function.identity(), "voucher"),
    };

    private static final Column<String> NAME_COLUMN = new Column<String>(null, null, null, Function.identity(), "scientificName", "name");
    private static final Column<String> AUTHORSHIP_COLUMN =  new Column<String>(null, null, null, Function.identity(), "scientificNameAuthorship", "authorship", "author");


    private Map<String, ParsedName> parses;

    protected SpecialCaseNameParser(Map<String, ParsedName> parses) {
        this.parses = Collections.unmodifiableMap(parses);
    }

    /**
     * Check for a pre-parsed name.
     *
     * @param name The name (case and punctuation sensitive)
     * @return The pre-parsed name or null for not found
     */
    public ParsedName get(String name) {
        return this.parses.get(name);
    }

    /**
     * Store this special case list in a suitable place.
     * <p>
     * The result is a file that can be read by {@link #fromCSV(URL)}}
     * </p>
     *
     * @param writer The writer to write to
     *
     * @throws IOException if unable to store the special cases
     */
    public void store(Writer writer) throws IOException {
        CSVWriter csv = new CSVWriter(writer, ',', '"', '"', "\n");
        Collection<ParsedName> entries = this.parses.values();
        List<Column> used = Arrays.stream(PARSED_NAME_COLUMNS).filter(c -> c.include(entries)).collect(Collectors.toList());
        used.add(0, NAME_COLUMN);
        boolean hasAuthor = entries.stream().anyMatch(ParsedName::hasAuthorship);
        if (hasAuthor)
            used.add(AUTHORSHIP_COLUMN);
        String[] header = used.stream().map(Column::getHeader).collect(Collectors.toList()).toArray(new String[used.size()]);
        csv.writeNext(header, false);
        for (String name: this.parses.keySet().stream().sorted().collect(Collectors.toList())) {
            ParsedName pn = this.parses.get(name);
            NomCode code = pn.getCode();
            String[] line = new String[used.size()];
            int i = 0;
            for (Column<?> column: used) {
                Object value = column.getColumn(pn);
                line[i++] = value == null ? null : value.toString();
            }
            line[0] = name;
            if (hasAuthor) {
                ParsedAuthorship pa = new ParsedAuthorship();
                pa.setBasionymAuthorship(pn.getBasionymAuthorship());
                pa.setCombinationAuthorship(pn.getCombinationAuthorship());
                pa.setSanctioningAuthor(pn.getSanctioningAuthor());
                if (pa.hasAuthorship())
                    line[line.length - 1] = NameFormatter.authorshipComplete(pa, code);
            }
            csv.writeNext(line, false);
        }
        csv.close();
    }

    /**
     * Read a CSV with the name and name parts and create a special case parser.
     * <p>
     * The CSV file can contain the following name parts, each with a named column
     * </p>
     * <table>
     * <tr><th>Part</th><th>Type</th><th>Columns</th><th>Comments</th></tr>
     * <tr><td>Candidatus</td><td>Boolean</td><td>candidatus</td><td>Is this a bacterial candidate, false by default</td></tr>
     * <tr><td>Code</td><td>{@link NomCode}</td> <td>nomenclaturalCode, nomCode</td><td>Must be one of the {@link NomCode} names or null</td></tr>
     * <tr><td>CultivarEpithet</td><td>String</td> <td>cultivareEpithet, cultivar</td><td></td></tr>
     * <tr><td>Genus</td><td>String</td> <td>genus</td><td></td></tr>
     * <tr><td>InfragenericEpithet</td><td>String</td> <td>infragenericEpithet, infrageneric</td><td></td></tr>
     * <tr><td>InfraspecificEpithet</td><td>String</td> <td>infraspecificEpithet, infraspecific</td><td></td></tr>
     * <tr><td>NominatingParty</td><td>String</td> <td>nominatingParty</td><td>For phrase names</td></tr>
     * <tr><td>Notho</td><td>{@link NamePart}</td> <td>notho</td><td></td></tr>
     * <tr><td>OriginalSpelling</td><td>Boolean</td> <td>originalSpelling</td><td>Defaults to null (no specification)</td></tr>
     * <tr><td>Phrase</td><td>String</td> <td>phraseName, phrase</td><td></td></tr>
     * <tr><td>Rank</td><td>{@link Rank}</td> <td>taxonRank, rank</td><td>Default to {@link Rank#UNRANKED}</td></tr>
     * <tr><td>SpecificEpithet</td><td>String</td> <td>specificEpithet, specific, species</td><td>Epithet only</td></tr>
     * <tr><td>Type</td><td>{@link NameType}</td><td>nameType, type</td><td>Must be one of the {@link NameType} names or null, Defaulta to {@link NameType#INFORMAL}</td></tr>
     * <tr><td>Uninomial</td><td>String</td><td>uninomial</td><td>Uninomial for non-genus names</td></tr>
     * <tr><td>Voucher</td><td>String</td> <td>voucher</td><td>For phrase names</td></tr>
     * <tr><td>Authorship</td><td>String</td> <td>scientificNameAuthorship. authorship, author</td><td>Parseable author string</td></tr>
     * </table>
     *
     * @param source A URL to the source CSV
     * @return A populated specific case parser
     */
    public static SpecialCaseNameParser fromCSV(URL source) throws IOException, InterruptedException, UnparsableNameException {
        final Map<String, ParsedName> parses = new HashMap<>();
        final RankAnalysis rankAnalysis = new RankAnalysis();
        final NameTypeAnalysis nameTypeAnalysis = new NameTypeAnalysis();
        final NameParser nameParser = new NameParserGBIF();
        InputStream is = source.openStream();
        CSVReader reader = CSVReaderFactory.build(is, "UTF-8", ",", '"', 1);
        Map<String, Integer> headerMap = new HashMap<>();
        String[] headers = reader.getHeader();
        for (int i = 0; i < headers.length; i++) {
            headerMap.put(headers[i], i);
        }
        while (reader.hasNext()) {
            String[] row = reader.next();
            String name = NAME_COLUMN.parseColumn(row, headerMap);
            if (parses.containsKey(name))
                throw new IllegalStateException("Duplicate name <<" + name + ">> when building special case parser");
            ParsedName parsed = new ParsedName();
            parsed.setState(ParsedName.State.COMPLETE);
            for (Column<?> column: PARSED_NAME_COLUMNS) {
                column.setColumn(parsed, row, headerMap);
            }
            String authorship = AUTHORSHIP_COLUMN.parseColumn(row, headerMap);
            ParsedAuthorship pa = authorship == null ? null : nameParser.parseAuthorship(authorship);
            if (pa != null && pa.hasBasionymAuthorship()) {
                parsed.setBasionymAuthorship(pa.getBasionymAuthorship());
            }
            if (pa != null && pa.hasCombinationAuthorship()) {
                parsed.setBasionymAuthorship(pa.getCombinationAuthorship());
            }
            if (pa != null && pa.getSanctioningAuthor() != null) {
                parsed.setSanctioningAuthor(pa.getSanctioningAuthor());
            }
            parses.put(name, parsed);
        }
        reader.close();
        logger.info("Read " + parses.size() + " special case name parses");
        return new SpecialCaseNameParser(parses);
    }

    @Value
    protected static class Column<T> {
        private String[] names;
        private T dflt;
        private Function<ParsedName, T> getter;
        private BiConsumer<ParsedName, T> setter;
        private Function<String, T> translator;

        public Column(T dflt, Function<ParsedName, T> getter,  BiConsumer<ParsedName, T> setter, Function<String, T> translator, String... names) {
            this.names = names;
            this.dflt = dflt;
            this.setter = setter;
            this.getter = getter;
            this.translator = translator;
        }

        public String getHeader() {
            return this.names[0];
        }

        public boolean include(Collection<ParsedName> names) {
            return names.stream().anyMatch(pn -> !Objects.equals(this.getter.apply(pn), this.dflt));
        }

        public T parseColumn(String[] row, Map<String, Integer> headerMap) {
            for (String name: this.names) {
                if (!headerMap.containsKey(name))
                    continue;
                int index = headerMap.get(name);
                String value = StringUtils.trimToNull(row[index]);
                if (value != null) {
                    return this.translator.apply(value);
                }
            }
            return this.dflt;
        }

        public void setColumn(ParsedName parsedName, String[] row, Map<String, Integer> headerMap) {
            if (this.setter != null) {
                T value = this.parseColumn(row, headerMap);
                this.setter.accept(parsedName, value);
            }
        }

        public T getColumn(ParsedName parsedName) {
            return this.getter == null ? null : this.getter.apply(parsedName);
        }
    }
}
