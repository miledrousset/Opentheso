/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package fr.cnrs.opentheso.ws.openapi.helper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author julie
 */
public class LangHelper {
    
    public List<String> availableLang() {
        String path = Thread.currentThread().getContextClassLoader().getResource("language").getPath();
        File[] files = new File(path).listFiles();
        List<String> languages = new ArrayList<>();
           
        for (File file : files ) {
            if (file.getName().startsWith("openapi_")) {
                String currentName = file.getName();
                currentName = currentName.replace("openapi_", "")
                        .replace(".properties", "");

                languages.add(currentName.toLowerCase());
            }
        }
   
        return languages;
    }
    
}
