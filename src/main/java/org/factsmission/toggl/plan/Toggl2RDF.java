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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Feature;
import org.apache.clerezza.commons.rdf.Graph;
import org.apache.clerezza.commons.rdf.impl.utils.simple.SimpleGraph;
import org.apache.clerezza.rdf.core.serializedform.Serializer;
import org.apache.clerezza.rdf.core.serializedform.UnsupportedFormatException;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.logging.LoggingFeature;

/**
 *
 * @author reto
 */
public class Toggl2RDF {

    final static DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
    
    final String authHeaderValue;
    final WebTarget webTarget;

    public Toggl2RDF(final String togglApiToken) {
        authHeaderValue = authHeaderValue(togglApiToken, "api_token");
        final Logger logger = Logger.getLogger("FLOW");
        final Feature feature = new LoggingFeature(logger, Level.INFO, null, null);
        final Client client = ClientBuilder.newClient(new ClientConfig().register(feature));
        webTarget = client.target("https://www.toggl.com/api/v8");
    }

    /*private Graph getProjects() {
        final WebTarget projects = webTarget.path("projects");
        projects.register(new JarqlMBR("CONSTRUCT WHERE {?s ?p ?o}"));
        return invoke(projects);
    }*/

    private Graph getWorkspacesAndProjects() {
        final WebTarget projects = webTarget.path("workspaces");
        projects.register(new JarqlMBR(""
                + "PREFIX toggl: <http://vocab.linked.solutions/toggl#>"
                + "PREFIX jarql: <http://jarql.com/>"
                + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>"
                + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
                + "CONSTRUCT {"
                + "?workSpace a toggl:WorkSpace;"
                + "rdfs:label ?name."
                + "} WHERE {"
                + "?s a jarql:Root ;"
                + "jarql:name ?name ;"
                + "jarql:id ?id ."
                + "BIND(IRI(CONCAT(\" https://www.toggl.com/api/v8/workspaces/\", ?id)) AS ?workSpace)"
                + "}"));
        return invoke(projects);
    }
    
    private Graph getTimeEntries(final ZonedDateTime start, final ZonedDateTime end) throws UnsupportedFormatException {
        
        final WebTarget timeEntries = webTarget.path("time_entries");
        timeEntries.register(new JarqlMBR(""
                + "PREFIX toggl: <http://vocab.linked.solutions/toggl#>"
                + "PREFIX jarql: <http://jarql.com/>"
                + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>"
                + "PREFIX dct <http://purl.org/dc/terms/>"
                + "CONSTRUCT {"
                + "?timeEntry a toggl:TimeEntry ;"
                + "toggl:workSpace ?workSpace ;"
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
                + "BIND(IRI(CONCAT(\"https://toggl.com/api/v8/workspaces/\",?wid)) AS ?workSpace)"
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
        //graph.addAll(toggl2RDF.getTimeEntries(start, end));
        graph.addAll(toggl2RDF.getWorkspacesAndProjects());
        Serializer.getInstance().serialize(System.out, graph, "text/turtle");
    }
}
