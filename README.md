# toggl-rdf
Export Toggl data as RDF.

## Usage example

The following will export the [Toggl](https://toggl.com/) data since 2:15 pm 
(UTC) on the 14th of January 2017. Replace `0a0a0a0a0a0a0a0a0a0a0a` with your
personal access token that you can find on your Toggl profile page.

    java -jar toggl-rdf-1.0.0-SNAPSHOT.jar -K 0a0a0a0a0a0a0a0a0a0a0a -S 2017-01-14T15:15Z

To get a list of all supported arguments just invoke it without any:

    java -jar toggl-rdf-1.0.0-SNAPSHOT.jar