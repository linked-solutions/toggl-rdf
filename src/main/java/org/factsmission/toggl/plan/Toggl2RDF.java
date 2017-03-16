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
import org.apache.clerezza.rdf.core.serializedform.Serializer;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.logging.LoggingFeature;

/**
 *
 * @author reto
 */
public class Toggl2RDF {

    final static DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    public static void main(String... args) {
        String togglApiToken = args[0];
        Logger logger = Logger.getLogger("FLOW");
        Feature feature = new LoggingFeature(logger, Level.INFO, null, null);
        Client client = ClientBuilder.newClient(new ClientConfig().register(feature));
        WebTarget webTarget = client.target("https://www.toggl.com/api/v8");
        WebTarget timeEntries = webTarget.path("time_entries");
        timeEntries.register(new JarqlMBR(""
                + "PREFIX toggl: <http://vocab.linked.solutions/toggl#>"
                + "PREFIX jarql: <http://jarql.com/>"
                + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>"
                + "CONSTRUCT {"
                    + "?s a toggl:TimeEntry ."
                    + "?s toggl:workSpace ?workspace ."
                    + "?s toggl:user ?user ."
                    + "?s toggl:start ?startTime ."
                    + "?s ?p ?o"
                + "} WHERE {"
                    + "?s a jarql:Root ."
                    + "?s jarql:wid ?wid ."
                    + "?s jarql:uid ?uid ."
                    + "?s jarql:start ?start ."
                    + "?s ?p ?o ."
                    + "BIND(IRI(CONCAT(\"https://toggl.com/app/workspaces/\",?wid)) as ?workspace)"
                    + "BIND(IRI(CONCAT(\"https://toggl.com/app/users/\",?uid)) as ?user)"
                    + "BIND(xsd:dateTime(?start) AS ?startTime)"
                + "}"));
        ZonedDateTime end = ZonedDateTime.now();
        ZonedDateTime start = end.minusDays(1);
        WebTarget lastDay = timeEntries
                .queryParam("start_date", formatter.format(start))
                .queryParam("end_date", formatter.format(end));
        Invocation.Builder invocationBuilder = lastDay.request();
        Graph response = invocationBuilder.header("Authorization",
                authHeaderValue(togglApiToken, "api_token")).get(Graph.class);
        //System.out.println(response.getStatusInfo());
        //System.out.println(response.getEntity());
        //System.out.println(response);
        Serializer.getInstance().serialize(System.out, response, "text/turtle");
    }

    private static String authHeaderValue(String userName, String password) {
        String usernameAndPassword = userName + ":" + password;
        String authorizationHeaderName = "Authorization";
        return "Basic " + java.util.Base64.getEncoder().encodeToString(usernameAndPassword.getBytes());
    }
}
