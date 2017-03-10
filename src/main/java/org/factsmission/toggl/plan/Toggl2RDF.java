package org.factsmission.toggl.plan;

import ch.simas.jtoggl.Client;
import ch.simas.jtoggl.JToggl;
import ch.simas.jtoggl.Project;
import ch.simas.jtoggl.TimeEntry;
import ch.simas.jtoggl.User;
import ch.simas.jtoggl.Workspace;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.clerezza.commons.rdf.Graph;
import org.apache.clerezza.commons.rdf.IRI;
import org.apache.clerezza.commons.rdf.RDFTerm;
import org.apache.clerezza.commons.rdf.impl.utils.PlainLiteralImpl;
import org.apache.clerezza.commons.rdf.impl.utils.TripleImpl;
import org.apache.clerezza.commons.rdf.impl.utils.simple.SimpleGraph;
import org.apache.clerezza.rdf.ontologies.RDF;

/**
 *
 * @author user
 */
public class Toggl2RDF {
  
  public static final String iriPrefix ="http://toggl.wymiwyg.com/";
  
  public static IRI prefix(String suffix) {
    return new IRI(iriPrefix+suffix);
  } 
  
  final JToggl jToggl;
  
  public Toggl2RDF(String togglApiToken) {
    jToggl = new JToggl(togglApiToken, "api_token");
    jToggl.setThrottlePeriod(500l);
    //jToggl.switchLoggingOn();

    /*List<Workspace> workspaces = jToggl.getWorkspaces();
    for (int i = 0; i < workspaces.size(); i++) {
      Workspace workspace = workspaces.get(i);
      System.out.println("Workspace: "+workspace);
    }
    List<Project> projects = jToggl.getProjects();
    for (int i = 0; i < projects.size(); i++) {
      Project project = projects.get(i);
      System.out.println("Project: "+project.getName()+"("+project.getId()+") in "+project.getWorkspace());   
    }*/
    CachingTogglClient cachingClient = new CachingTogglClient(togglApiToken);
    List<TimeEntry>  timeEntries = cachingClient.getTimeEntries(new Date(System.currentTimeMillis()-24*60*60*1000), new Date());
    for (int i = 0; i < timeEntries.size(); i++) {
      TimeEntry timeEntry = timeEntries.get(i);
      System.out.println("TimeEntry: "+timeEntry.getDescription()+" in "+timeEntry.getProject().getName()+" "+timeEntry);   
    }
    List<User> users = jToggl.getUsers();
    for (int i = 0; i < users.size(); i++) {
      User user = users.get(i);
      System.out.println("User: "+user.getFullname());
    }
  }
  
  public void copyToGraph(Graph g, Date startDate, Date endDate) {
    List<Workspace> workspaces = jToggl.getWorkspaces();
    for (int i = 0; i < workspaces.size(); i++) {
      Workspace workspace = workspaces.get(i);
      IRI workspaceNode = new IRI(iriPrefix+"workspace/"+workspace.getId());
      g.add(new TripleImpl(workspaceNode, RDF.type, prefix("Workspace")));
      g.add(new TripleImpl(workspaceNode, prefix("name"), new PlainLiteralImpl(workspace.getName())));
      g.add(new TripleImpl(workspaceNode, prefix("json"), new PlainLiteralImpl(workspace.toJSONString())));
    }
  }

  public static void main(String... args) {
    String togglApiToken = args[0];
    Toggl2RDF toggl2RDF = new Toggl2RDF(togglApiToken);
    Graph g = new SimpleGraph();
    toggl2RDF.copyToGraph(g, new Date(System.currentTimeMillis()-24*60*60*1000), new Date());
    System.out.println(g);
  }
}
