/*
 * The MIT License
 *
 * Copyright 2017 Reto Gmür.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.factsmission.toggl.plan;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Spliterator;
import java.util.Spliterators;
import static java.util.stream.Collectors.toList;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import org.apache.clerezza.commons.rdf.Graph;
import org.apache.clerezza.commons.rdf.IRI;
import org.apache.clerezza.commons.rdf.Triple;
import org.apache.clerezza.commons.rdf.impl.utils.simple.SimpleGraph;
import org.apache.clerezza.rdf.core.serializedform.Serializer;
import org.apache.clerezza.rdf.core.serializedform.UnsupportedFormatException;
import org.apache.clerezza.rdf.ontologies.RDF;
import org.apache.clerezza.rdf.utils.UnionGraph;
import org.glassfish.jersey.client.ClientConfig;

/**
 *
 * @author reto
 */
public class Toggl2RDF {

    final static DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
    
    final String authHeaderValue;
    final WebTarget apiRoot;
    final Client client;
    
    public Toggl2RDF(final String togglApiToken) {
        authHeaderValue = authHeaderValue(togglApiToken, "api_token");
        //final Logger logger = Logger.getLogger("FLOW");
        //final Feature feature = new LoggingFeature(logger, Level.INFO, null, null);
        client = ClientBuilder.newClient(new ClientConfig()); //.register(feature));
        apiRoot = client.target("https://www.toggl.com/api/v8");
    }

    class WorkspaceClient {

        final IRI workspace;
        final WebTarget workspaceWebTarget;
        public WorkspaceClient(final IRI workspace) {
            this.workspace = workspace;
            workspaceWebTarget = client.target(workspace.getUnicodeString());
        }
        
        private Graph getProjects() {
            final WebTarget projects = workspaceWebTarget.path("projects");
            projects.register(new JarqlMBR(""
                + "PREFIX toggl: <http://vocab.linked.solutions/toggl#>"
                + "PREFIX jarql: <http://jarql.com/>"
                + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>"
                + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
                + "CONSTRUCT {"
                + "?project a toggl:Project ;"
                + "toggl:workspace ?workspace ;"
                + "toggl:client ?client ;"
                + "rdfs:label ?name."
                + "} WHERE {"
                + "?s a jarql:Root ;"
                + "jarql:id ?id ;"
                + "jarql:wid ?wid ;"
                + "jarql:cid ?cid ;"
                + "jarql:name ?name ."
                + "BIND(IRI(CONCAT(\"https://toggl.com/api/v8/workspaces/\",?wid)) AS ?workspace)"
                + "BIND(IRI(CONCAT(\"https://toggl.com/api/v8/projects/\",?id)) AS ?project)"
                + "BIND(IRI(CONCAT(\"https://toggl.com/api/v8/clients/\",?cid)) AS ?client)"
                + "}"));
            return invoke(projects);
        }
        
        private Graph getUsers() {
            final WebTarget projects = workspaceWebTarget.path("users");
            projects.register(new JarqlMBR(""
                + "PREFIX toggl: <http://vocab.linked.solutions/toggl#>"
                + "PREFIX jarql: <http://jarql.com/>"
                + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>"
                + "PREFIX schema: <http://schema.org/>"
                + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
                + "CONSTRUCT {"
                    + "?user a schema:Person ;"
                    + "schema:name ?fullName ;"
                    + "schema:email ?email ;"
                    + "toggl:defaultWorkspace ?defaultWorkspace ."
                + "} WHERE {"
                    + "?s a jarql:Root ;"
                    + "jarql:id ?id ;"
                    + "jarql:image_url ?imageUrl ;"
                    + "jarql:default_wid ?defaultWid ;"
                    + "jarql:email ?email ;"
                    + "jarql:fullname ?fullName ."
                    + "BIND(IRI(CONCAT(\"https://toggl.com/api/v8/users/\",?id)) AS ?user)"
                    + "BIND(IRI(CONCAT(\"https://toggl.com/api/v8/workspaces/\",?defaultWid)) AS ?defaultWorkspace)"
                + "}"));
            return invoke(projects);
        }
        
        private Graph getClients() {
            final WebTarget projects = workspaceWebTarget.path("clients");
            projects.register(new JarqlMBR(""
                + "PREFIX toggl: <http://vocab.linked.solutions/toggl#>"
                + "PREFIX jarql: <http://jarql.com/>"
                + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>"
                + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
                + "CONSTRUCT {"
                + "?client a toggl:Client ;"
                + "toggl:workspace ?workspace ;"
                + "rdfs:comment ?notes ;"
                + "rdfs:label ?name."
                + "} WHERE {"
                + "?s a jarql:Root ;"
                + "jarql:id ?id ;"
                + "jarql:wid ?wid ;"
                + "jarql:name ?name ."
                + "OPTIONAL {$s jarql:notes ?notes }."
                + "BIND(IRI(CONCAT(\"https://toggl.com/api/v8/workspaces/\",?wid)) AS ?workspace)"
                + "BIND(IRI(CONCAT(\"https://toggl.com/api/v8/clients/\",?id)) AS ?client)"
                + "}"));
            return invoke(projects);
        }
        
        /**
         * 
         * @return projects, users, clients but no time entries
         */
        private Graph getWorkspaceElements() {
            return new UnionGraph(getProjects(), getUsers(), getClients());
        }
        
    }
    
    
    private Graph getElementsOfWorkspaces(Collection<IRI> workspaces) {
        final Graph result = new UnionGraph(workspaces.stream().map(
                i -> new WorkspaceClient(i).getWorkspaceElements()).toArray(size -> new Graph[size]));
        return result;
    }
    
    private Graph getElementsOfWorkspacesInGraph(Graph workspaceDescriptions) {
        final Stream<Triple> workspaceTriopleIter = StreamSupport.stream(
          Spliterators.spliteratorUnknownSize(workspaceDescriptions.filter(null, RDF.type, 
                new IRI("http://vocab.linked.solutions/toggl#Workspace")), Spliterator.ORDERED), false);
        return getElementsOfWorkspaces(workspaceTriopleIter.map(t -> (IRI)t.getSubject()).collect(toList()));
    }

    private Graph getWorkspaces() {
        final WebTarget projects = apiRoot.path("workspaces");
        projects.register(new JarqlMBR(""
                + "PREFIX toggl: <http://vocab.linked.solutions/toggl#>"
                + "PREFIX jarql: <http://jarql.com/>"
                + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>"
                + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
                + "CONSTRUCT {"
                + "?workspace a toggl:Workspace;"
                + "rdfs:label ?name."
                + "} WHERE {"
                + "?s a jarql:Root ;"
                + "jarql:name ?name ;"
                + "jarql:id ?id ."
                + "BIND(IRI(CONCAT(\"https://www.toggl.com/api/v8/workspaces/\", ?id)) AS ?workspace)"
                + "}"));
        return invoke(projects);
    }
    
    private Graph getTimeEntries(final ZonedDateTime start, final ZonedDateTime end) throws UnsupportedFormatException {
        
        final WebTarget timeEntries = apiRoot.path("time_entries");
        timeEntries.register(new JarqlMBR(""
                + "PREFIX toggl: <http://vocab.linked.solutions/toggl#>"
                + "PREFIX jarql: <http://jarql.com/>"
                + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>"
                + "PREFIX dct: <http://purl.org/dc/terms/>"
                + "CONSTRUCT {"
                + "?timeEntry a toggl:TimeEntry ;"
                + "toggl:workspace ?workspace ;"
                + "toggl:user ?user ;"
                + "toggl:project ?project ;"
                + "toggl:start ?startTime ;"
                + "toggl:stop ?stopTime ;"
                + "dct:description ?description ."
                + "} WHERE {"
                + "?s a jarql:Root ."
                + "?s jarql:id ?id ."
                + "?s jarql:wid ?wid ."
                + "?s jarql:uid ?uid ."
                + "?s jarql:pid ?pid ."
                + "?s jarql:start ?start ."
                + "?s jarql:stop ?stop ."
                + "OPTIONAL {?s jarql:description ?description} ."
                + "BIND(IRI(CONCAT(\"https://toggl.com/api/v8/time_entries/\",?id)) AS ?timeEntry)"
                + "BIND(IRI(CONCAT(\"https://toggl.com/api/v8/workspaces/\",?wid)) AS ?workspace)"
                + "BIND(IRI(CONCAT(\"https://toggl.com/api/v8/users/\",?uid)) AS ?user)"
                + "BIND(IRI(CONCAT(\"https://toggl.com/api/v8/projects/\",?pid)) AS ?project)"
                + "BIND(xsd:dateTime(?start) AS ?startTime)"
                + "BIND(xsd:dateTime(?stop) AS ?stopTime)"
                + "}"));
        final WebTarget timeEntriesInTimeRange = timeEntries
                .queryParam("start_date", formatter.format(start))
                .queryParam("end_date", formatter.format(end));
        return invoke(timeEntriesInTimeRange);
    }

    private Graph invoke(WebTarget target) {
        final Invocation.Builder invocationBuilder = target.request();
        return invocationBuilder.header("Authorization", authHeaderValue).get(Graph.class);
    }
    
    private static String authHeaderValue(String userName, String password) {
        final String usernameAndPassword = userName + ":" + password;
        final String authorizationHeaderName = "Authorization";
        return "Basic " + java.util.Base64.getEncoder().encodeToString(usernameAndPassword.getBytes());
    }
    
    public static void main(final String... args) {
        final Toggl2RDF toggl2RDF = new Toggl2RDF(args[0]);
        final ZonedDateTime end = ZonedDateTime.now();
        final ZonedDateTime start = end.minusDays(1);
        final Graph graph = new SimpleGraph();
        graph.addAll(toggl2RDF.getTimeEntries(start, end));
        graph.addAll(toggl2RDF.getWorkspaces());
        graph.addAll(toggl2RDF.getElementsOfWorkspacesInGraph(graph));
        Serializer.getInstance().serialize(System.out, graph, "application/rdf+xml");
    }
}
