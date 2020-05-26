/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.ws.restnew;

import com.sun.jersey.server.impl.wadl.WadlResource;
import java.util.Set;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;


/**
 *
 * @author miled.rousset
 */
@ApplicationPath("api")
public class ApplicationConfig2 extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> resources = new java.util.HashSet<>();
        addRestResourceClasses(resources);
        return resources;
    }

    /**
     * Do not modify addRestResourceClasses() method.
     * It is automatically populated with
 all Rest defined in the project.
 If required, comment out calling this method in getClasses().
     */
    private void addRestResourceClasses(Set<Class<?>> resources) {
        resources.add(fr.cnrs.opentheso.ws.restnew.RestGroup.class);
        resources.add(fr.cnrs.opentheso.ws.restnew.Rest_new.class);
        resources.add(fr.cnrs.opentheso.ws.restnew.SelectConcept.class);
        resources.add(fr.cnrs.opentheso.ws.restnew.SelectTheso.class);
        resources.add(org.glassfish.jersey.server.wadl.internal.WadlResource.class);
    }
    
}
