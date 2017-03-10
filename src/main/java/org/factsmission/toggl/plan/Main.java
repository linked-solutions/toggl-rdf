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

/**
 *
 * @author user
 */
public class Main {
  
  
  public Main(String togglApiToken) {
    JToggl jToggl = new JToggl(togglApiToken, "api_token");
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

  public static void main(String... args) {
    String togglApiToken = args[0];
    new Main(togglApiToken);
  }
}
