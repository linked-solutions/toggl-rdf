package org.factsmission.toggl.plan;

import ch.simas.jtoggl.JToggl;
import ch.simas.jtoggl.Project;
import ch.simas.jtoggl.TimeEntry;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author user
 */
public class CachingTogglClient {

  private final JToggl jToggl;
  private volatile Map<Long, Project> projectMap = null;
  
  
  CachingTogglClient(String togglApiToken) {
    this.jToggl = new JToggl(togglApiToken, "api_token");
    loadProjects();
  }
  
  private void loadProjects() {
    final List<Project> projects = jToggl.getProjects();
    projectMap = new HashMap<>();
    for (int i = 0; i < projects.size(); i++) {
      Project project = projects.get(i);
      projectMap.put(project.getId(), project);
      //System.out.println("Project: "+project.getName()+"("+project.getId()+") in "+project.getWorkspace());   
    }
  }
  
  public List<TimeEntry> getTimeEntries(Date startDate, Date endDate) {
    final List<TimeEntry> timeEntries = jToggl.getTimeEntries(startDate, endDate);
    for (int i = 0; i < timeEntries.size(); i++) {
      TimeEntry timeEntry = timeEntries.get(i);
      timeEntry.setProject(projectMap.get(timeEntry.getPid()));
    }
    return timeEntries;
  }
}
