/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.bean.importexport;

import java.io.Serializable;
import java.util.concurrent.ThreadLocalRandom;
import javax.faces.event.ActionEvent;
import javax.inject.Named;
import javax.faces.view.ViewScoped;

/**
 *
 * @author miledrousset
 */
@Named(value = "taskView")
@ViewScoped
public class TaskView implements Serializable{
    
  private int progressInteger;

  public void startTask(ActionEvent ae) {
      startLongTask();
  }

  private void startLongTask() {
      progressInteger = 0;
      for (int i = 0; i < 100; i++) {
          progressInteger++;
          //simulating long running task
          try {
              Thread.sleep(ThreadLocalRandom.current().nextInt(1, 100));
          } catch (InterruptedException e) {
              e.printStackTrace();
          }
      }
      progressInteger = 100;

  }

  public int getProgress() {
      return progressInteger;
  }

  public String getResult() {
      return progressInteger == 100 ? "task done" : "";
  }
}
