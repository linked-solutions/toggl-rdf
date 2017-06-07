# toggl-rdf
Export Toggl data as RDF.

## Usage example

The following will export the [Toggl](https://toggl.com/) data since 2:15 pm 
(UTC) on the 14th of January 2017. Replace `0a0a0a0a0a0a0a0a0a0a0a` with your
personal access token that you can find on your Toggl profile page.

    java -jar toggl-rdf-1.0.0-SNAPSHOT.jar -K 0a0a0a0a0a0a0a0a0a0a0a -S 2017-01-14T15:15Z

To get a list of all supported arguments just invoke it without any:

    java -jar toggl-rdf-1.0.0-SNAPSHOT.jar
    
## What is it good for?

Once you added the data to a triple store you can run queries against it, e.g.:

```
prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>
prefix dcterm: <http://purl.org/dc/terms/>
prefix toggl: <http://vocab.linked.solutions/toggl#>

SELECT ?start ?stop ?description ?pLabel ?cLabel ?workspace WHERE {
 	?te toggl:start ?start.
    ?te toggl:stop ?stop.
  	?te toggl:workspace ?workspace.
  	?te toggl:project ?project.
    ?project rdfs:label ?pLabel. 
    OPTIONAL { ?te dcterm:description ?description }
    OPTIONAL { ?project toggl:client ?client. 
             ?client rdfs:label ?cLabel}
 	?te a toggl:TimeEntry.
	?te toggl:user 	<https://toggl.com/api/v8/users/2561133>.
} ORDER BY ?start
```
