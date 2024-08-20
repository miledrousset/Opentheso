/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.bean.search;

import fr.cnrs.opentheso.bean.rightbody.viewconcept.ConceptView;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 *
 * @author miledrousset
 */
@SessionScoped
@Named(value = "testMemory")
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class TestMemory implements Serializable {

    @Autowired @Lazy private ConceptView conceptView;    
    
    public TestMemory() {
    }
    public void testSearch(){
        for (int i = 0; i < 1000; i++) {
     //       searchBean.testMemory();
        }
        System.out.println("traitement terminé");
    }

    public void testGetConcept(){
        for (int i = 0; i < 100; i++) {
            conceptView.getConcept("TH_1", "300", "fr");
        }
        System.out.println("traitement terminé");
    }    
}
