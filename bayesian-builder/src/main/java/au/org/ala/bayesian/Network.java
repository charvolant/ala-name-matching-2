package au.org.ala.bayesian;

import au.org.ala.util.JsonUtils;
import au.org.ala.vocab.BayesianTerm;
import au.org.ala.vocab.OptimisationTerm;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.Setter;
import org.gbif.dwc.terms.Term;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.jgrapht.traverse.BreadthFirstIterator;
import org.jgrapht.traverse.TopologicalOrderIterator;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A network for bayesian inference,
 * <p>
 * The network can contain other vertices that have special meanings.
 * These are not included in the graph but can be used to provide additional
 * information to things like an index builder.
 * </p>
 */
@JsonPropertyOrder({"id", "description", "uri", "concept", "vocabularies", "normalisers", "observables", "key", "vertices", "edges", "broadeners", "issues", "modifications", "sourceModifiers", "matchModifiers" })
public class Network extends Identifiable {
    /** The vertex id map */
    private final SortedMap<String, Observable<?>> idMap;
    /** The vertex URI map */
    private final Map<URI, Observable<?>> uriMap;
    /** The concept that this network reflects */
    @JsonProperty
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @Getter
    @Setter
    private URI concept;
    /** The observables that make up a key for concepts. Multiple entries of the same key will be ignored. If null then no key is used */
    @JsonProperty
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @Getter
    @Setter
    private List<Observable<?>> key;
    /** The list of issues */
    @JsonProperty
    @Getter
    @Setter
    private List<Issue> issues;
    /** The additional vocabularies used in this network. {@link au.org.ala.vocab.BayesianTerm} is included by default */
    @JsonProperty
    @Getter
    @Setter
    private List<Class<?>> vocabularies;
    /** The list of broadeners */
    @JsonProperty
    @Getter
    @Setter
    private List<Broadener> broadeners;
    /** The list of source modifiers */
    @JsonProperty
    @Getter
    @Setter
    private List<List<Modifier>> sourceModifiers;
    /** The list of match modifiers */
    @JsonProperty
    @Getter
    @Setter
    private List<List<Modifier>> matchModifiers;
    /** The erasure signature of this network.
     * If null, this is the original network.
     * Otherwise, it contains T/F elements corresponding to the presence or absence of erasable observable groups
     */
    @JsonProperty
    @Getter
    @Setter
    private String signature;
    /** The erasures active in this network */
    @JsonProperty
    @Getter
    @Setter
    private List<String> erasures;
    /** The inference links between vertices */
    @JsonIgnore
    private DirectedAcyclicGraph<Observable, Dependency> graph;

    {
        BayesianTerm.weight.toString(); // Ensure loaded
        OptimisationTerm.load.toString(); // Ensure loaded
    }

    /**
     * Construct an empty network
     */
    public Network() {
        this.graph = new DirectedAcyclicGraph<>(Dependency.class);
        this.idMap = new TreeMap<>();
        this.uriMap = new HashMap<>();
        this.issues = new ArrayList<>();
        this.vocabularies = new ArrayList<>();
        this.key = null;
        this.broadeners = new ArrayList<>();
        this.sourceModifiers = new ArrayList<>();
        this.matchModifiers = new ArrayList<>();
        this.erasures = new ArrayList<>();
    }

    /**
     * Constuct an empty network with an identifier
     *
     * @param id The identifier
     */
    public Network(String id) {
        super(id);
        this.graph = new DirectedAcyclicGraph<>(Dependency.class);
        this.idMap = new TreeMap<>();
        this.uriMap = new HashMap<>();
        this.issues = new ArrayList<>();
        this.vocabularies = new ArrayList<>();
        this.key = null;
        this.broadeners = new ArrayList<>();
        this.sourceModifiers = new ArrayList<>();
        this.matchModifiers = new ArrayList<>();
        this.erasures = new ArrayList<>();
    }

    public Network(Network source, String id, boolean empty) {
        super(source, id);
        this.idMap = new TreeMap<>(source.idMap);
        this.uriMap = new HashMap<>(source.uriMap);
        this.concept = source.concept;
        this.issues = new ArrayList<>(source.issues);
        this.vocabularies = new ArrayList<>(source.vocabularies);
        this.key = null;
        this.broadeners = new ArrayList<>(source.broadeners);
        this.sourceModifiers = new ArrayList<>(source.sourceModifiers);
        this.matchModifiers = new ArrayList<>(source.matchModifiers);
        this.signature = source.signature;
        this.erasures = new ArrayList<>(source.erasures);
        this.graph = new DirectedAcyclicGraph<>(Dependency.class);
        if (!empty)
            Graphs.addGraph(this.graph, source.graph);
    }

    /**
     * Get the underlying graph.
     *
     * @return The basic graph
     */
    protected DirectedAcyclicGraph<Observable, Dependency> getGraph() {
        return this.graph;
    }

    /**
     * Get a vertex via Id
     *
     * @param id The id to look for
     *
     * @return The matching observable, or null for none
     */
    public Observable getObservable(String id) {
        return this.idMap.get(id);
    }

    /**
     * Get a vertex via URI.
     *
     * @param uri The URI
     *
     * @return The vertex from a URI or null for not found.
     */
    public Observable getObservable(URI uri) {
        return this.uriMap.get(uri);
     }

    /**
     * Get a vertex via GBIF term.
     *
     * @param term The term
     *
     * @return The vertex from a term or null for not found.
     */
    public Observable getObservable(Term term) {
        return this.getObservable(URI.create(term.qualifiedName()));
    }

    @JsonProperty("normalisers")
    public List<Normaliser> getNormalisers() {
        Set<Normaliser> norms = this.getVertices().stream().map(Observable::getNormaliser).filter(Objects::nonNull).collect(Collectors.toSet());
        List<Normaliser> normalisers = new ArrayList<>(norms);
        normalisers.sort(Comparator.comparing(Identifiable::getId));
        return normalisers;
    }

    /**
     * Get the network vertices.
     *
     * @return The network vertices.
     */
    @JsonProperty("vertices")
    public List<Observable> getVertices() {
        Iterator<Observable> ordering = new TopologicalOrderIterator<>(this.graph);
        List<Observable> vertices = new ArrayList<>(this.graph.vertexSet().size());
        ordering.forEachRemaining(vertices::add);
        return vertices;
    }


    /**
     * Copy the network vertices.
     * <p>
     *     Any existing vertices are cleared.
     *     The vertices must correspond to the list of observables loaded.
     * </p>
     *
     * @param vertices The new list of vertices
     */
    @JsonProperty("vertices")
    public void setVertices(Collection<Observable> vertices) {
        this.graph = new DirectedAcyclicGraph<>(Dependency.class);
        for (Observable v: vertices) {
            Observable used = this.idMap.get(v.getId());
            if (used == null) {
                used = v;
                this.idMap.put(used.getId(), used);
                if (used.getUri() != null)
                    this.uriMap.put(used.getUri(), used);
            }
            this.graph.addVertex(used);
        }
    }

    // Must be in supplied order, since otherwise we get illegal forward references
    @JsonProperty("modifications")
    public List<Modifier> getModifications() {
        List<Modifier> smods = this.sourceModifiers.stream().flatMap(ms -> ms.stream().flatMap(m -> m.getAllModifiers().stream())).collect(Collectors.toList());
        List<Modifier> mmods = this.matchModifiers.stream().flatMap(ms -> ms.stream().flatMap(m -> m.getAllModifiers().stream())).collect(Collectors.toList());
        List<Modifier> modifiers = Stream.concat(mmods.stream(), smods.stream()).distinct().collect(Collectors.toList());
        return modifiers;
    }


    /**
     * Get the network observables.
     *
     * @return The observables in network toplogical order, followed by additional observables.
     */
    @JsonProperty("observables")
    public Collection<Observable<?>> getObservables() {
        return this.idMap.values();
    }

    /**
     * Get the network observables sorted by identifier.
     *
     * @return The vertices in breadth-first order, followed by additional vertices.
     */
    @JsonIgnore
    public List<Observable<?>> getObservablesById() {
        List<Observable<?>> observables = new ArrayList<>(this.idMap.values());
        observables.sort(Comparator.comparing(Identifiable::getId));
        return observables;
    }

    /**
     * Copy the network vertices.
     * <p>
     *     Any existing vertices are cleared.
     * </p>
     *
     * @param vertices The new list of vertices
     */
    @JsonProperty("observables")
    public void setObservables(Collection<Observable<?>> vertices) {
        this.idMap.clear();
        this.uriMap.clear();
        for (Observable v: vertices) {
            if (this.idMap.containsKey(v.getId()))
                throw new IllegalStateException("Observable " + v.getId() + " duplicated");
            this.idMap.put(v.getId(), v);
            if (v.getUri() != null)
                this.uriMap.put(v.getUri(), v);
        }
    }

    /**
     * Find an observable with a particular property.
     *
     * @param property The property
     * @param value The property value
     *
     * @param <T> The type of observable
     *
     * @return An optional observable
     */
    public <T> Optional<Observable<T>> findObservable(Term property, Class<T> clazz_, Object value) {
        return this.idMap.values().stream()
                .filter(o -> o.hasProperty(property, value))
                .findFirst()
                .map(o -> (Observable<T>) o); // Force cast within Optional
    }

    /**
     * Get the observable associated with the classification identifier.
     *
     * @return The identifier observable or null if not present
     */
    @JsonIgnore
    public Observable<String> getIdentifierObservable() {
        return this.findObservable(BayesianTerm.identifier, String.class,true).orElse(null);
    }

    /**
     * Get the observable associated with the classification name.
     *
     * @return The name observable or null if not present
     */
    @JsonIgnore
    public Observable<String> getNameObservable() {
        return this.findObservable(BayesianTerm.name, String.class,true).orElse(null);
    }


    /**
     * Get the observable associated with the classification alternate name.
     *
     * @return The alt name observable or null if not present
     */
    @JsonIgnore
    public Observable<String> getAltNameObservable() {
        return this.findObservable(BayesianTerm.altName, String.class, true).orElse(null);
    }


    /**
     * Get the observable associated with the classification parent identifier.
     *
     * @return The parent identifier observable or null
     */
    @JsonIgnore
    public Observable<String> getParentObservable() {
        return this.findObservable(BayesianTerm.parent, String.class, true).orElse(null);
    }

    /**
     * Get the observable associated with the classification accepted identifier.
     *
     * @return The accepted identifier observable or null
     */
    @JsonIgnore
    public Observable<String> getAcceptedObservable() {
        return this.findObservable(BayesianTerm.accepted, String.class,true).orElse(null);
    }

    /**
     * Get a list of all the observables with a particular property.
     *
     * @param property The property
     * @param value The value
     *
     * @return A, possibly empty, list of matching observables.
     */
    public List<Observable<?>> findObservables(Term property, Object value) {
        return this.idMap.values().stream().filter(o -> o.hasProperty(property, value)).collect(Collectors.toList());
    }

    /**
     * Get the source of an dependency
     *
     * @param dependency The dependency
     *
     * @return The vertex that is the dependency source
     */
    public Observable getSource(Dependency dependency) {
        return this.graph.getEdgeSource(dependency);
    }

    /**
     * Get the target of an dependency
     *
     * @param dependency The dependency
     *
     * @return The vertex that is the dependency target
     */
    public Observable getTarget(Dependency dependency) {
        return this.graph.getEdgeTarget(dependency);
    }

    /**
     * Get the dependency list
     *
     * @return The list odf edges in vertex breadth first order.
     */
    @JsonProperty("edges")
    public List<FullEdge> getEdges() {
        Iterator<Observable> vi = new BreadthFirstIterator<>(this.graph);
        List<FullEdge> edges = new ArrayList<>(this.graph.edgeSet().size());
        while (vi.hasNext()) {
            Observable v = vi.next();
            for (Dependency e: this.graph.outgoingEdgesOf(v)) {
                Observable t = this.graph.getEdgeTarget(e);
                edges.add(new FullEdge(v, t, e));
            }
        }
        return edges;
    }

    /**
     * Copy the network edges.
     * <p>
     *     Any existing edges are cleared.
     * </p>
     *
     * @param edges The dependency list, including the source and target vertices
     *
     * @see FullEdge
     */
    @JsonProperty("edges")
    public void setEdges(Collection<FullEdge> edges) {
        this.graph.removeAllEdges(this.graph.edgeSet());
        for (FullEdge e: edges) {
            if (e.source == null)
                throw new IllegalArgumentException("Edge to " + e.target + " has invalid source");
            if (e.target == null)
                throw new IllegalArgumentException("Edge from " + e.source + " has invalid target");
            this.graph.addVertex(e.source);
            this.graph.addVertex(e.target);
            this.graph.addEdge(e.source, e.target, e.edge);
        }
    }

    /**
     * Get the incoming edges for a observable.
     *
     * @param observable The observable
     *
     * @return The incoming edges
     */
    public Set<Dependency> getIncoming(Observable observable) {
        return this.graph.incomingEdgesOf(observable);
    }

    /**
     * Get the incoming edges for a observable.
     *
     * @param observable The observable
     *
     * @return The incoming edges
     */
    public Set<Dependency> getOutgoing(Observable observable) {
        return this.graph.outgoingEdgesOf(observable);
    }

    /**
     * Get the list of input observables
     *
     * @return The list of observables in the network not dependent on anything
     */
    @JsonIgnore
    public List<Observable> getInputs() {
        return this.getVertices().stream().filter(o -> this.getIncoming(o).isEmpty()).collect(Collectors.toList());
    }

    /**
     * Get the list of output observables
     *
     * @return The list of observables in the network with no dependencies
     */
    @JsonIgnore
    public List<Observable> getOutputs() {
        return this.getVertices().stream().filter(o -> this.getOutgoing(o).isEmpty()).collect(Collectors.toList());
    }

    /**
     * Get the list of groups for this network.
     *
     * @return The list of erasure groups, in network order.
     */
    public List<String> getGroups() {
        return this.getVertices().stream()
                .map(Observable::getGroup)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

    }

    /**
     * Build a sub network with only a specific set of observables.
     *
     * @param include The observables to include
     * @param id The new network identifier
     *
     * @return The sub-network
     */
    public Network createSubNetwork(Collection<Observable> include, String id) {
        Network subNetwork = new Network(this, id, true);
         for (Observable o: this.getVertices()) {
            if (!include.contains(o))
                continue;
            Set<Observable> targets = new HashSet<>();
            Set<Dependency> outgoing = new HashSet<>(this.getOutgoing(o));
            while (!outgoing.isEmpty()) {
                Set<Dependency> newOutgoing = new HashSet<>();
                for (Dependency d : outgoing) {
                    Observable target = this.graph.getEdgeTarget(d);
                    if (targets.contains(target))
                        continue;
                    if (include.contains(target)) {
                        targets.add(target);
                        continue;
                    }
                    Set<Dependency> newOut = this.getOutgoing(target);
                    if (!newOut.isEmpty()) {
                          newOutgoing.addAll(newOut);
                    }
                }
                outgoing = newOutgoing;
            }
            for (Observable t: targets) {
                subNetwork.graph.addVertex(o);
                subNetwork.graph.addVertex(t);
                subNetwork.graph.addEdge(o, t, new Dependency());
            }
        }
        return subNetwork;
    }

    /**
     * Recursively build a list of partially erased networks.
     *
     * @param erasures The current list of pending erasures
     * @param accumulator The accoumator for partial netwotks.
     */
    protected void createSubNetworks(int index, List<String> erasures, String signature, List<Network> accumulator) {
        if (index >= erasures.size()) {
            Network copy = new Network(this, this.getId(), false);
            copy.setSignature(signature);
            accumulator.add(copy);
            return;
        }
        final String erasure = erasures.get(index);
        this.createSubNetworks(index + 1, erasures, signature + "T", accumulator);
        List<Observable> include = this.getVertices().stream().filter(o -> !erasure.equals(o.getGroup())).collect(Collectors.toList());
        Network erased = this.createSubNetwork(include, this.getId() + "-" + erasure);
        erased.getErasures().add(erasure);
        erased.createSubNetworks(index + 1, erasures, signature + "F", accumulator);
    }

    /**
     * Create all subnetworks with erased elements.
     *
     * @return The list of sub-networks
     */
    public List<Network> createSubNetworks() {
        List<String> erasures = this.getGroups();
        List<Network> subNetworks = new ArrayList<>(1 << erasures.size());
        this.createSubNetworks(0, erasures, "", subNetworks);
        return subNetworks;
    }

    /**
     * Read a network description from a URL.
     *
     * @param source The source URL
     *
     * @return The resulting network
     *
     * @throws IOException If unable to read the network
     */
    public static Network read(URL source) throws IOException {
        return JsonUtils.createMapper().readValue(source, Network.class);
    }

    /**
     * A helper class for getting/setting edges.
     * <p>
     * The graph representation does not include the source/target
     * </p>
     */
    public static class FullEdge {
        @JsonProperty
        public Observable source;
        @JsonProperty
        public Observable target;
        @JsonProperty
        public Dependency edge;

        public FullEdge(Observable source, Observable target, Dependency edge) {
            this.source = source;
            this.target = target;
            this.edge = edge;
        }

        public FullEdge() {
        }

        public Observable getSource() {
            return source;
        }

        public Observable getTarget() {
            return target;
        }

        public Dependency getEdge() {
            return edge;
        }
    }
}
