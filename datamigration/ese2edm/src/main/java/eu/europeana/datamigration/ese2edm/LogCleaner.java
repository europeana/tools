/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.europeana.datamigration.ese2edm;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author gmamakis
 */
public class LogCleaner {
    
    public static void main(String[] args){
        try {
            List<String> lines = FileUtils.readLines(new File("/home/gmamakis/test.log"));
            List<String> newLines = new ArrayList<String>();
            for(String line: lines){
                if(!line.startsWith("Apr")){
                    newLines.add(line);
                }
            }
            FileUtils.writeLines(new File("/home/gmamakis/rdfslabel.log"), newLines);
        } catch (IOException ex) {
            Logger.getLogger(LogCleaner.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
