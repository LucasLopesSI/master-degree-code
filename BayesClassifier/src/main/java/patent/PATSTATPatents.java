/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package patent;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

/**
 *
 * @author Lucas
 */
public class PATSTATPatents {
    
    LinkedList<Patent> patents;
    String filePath = null;
    String folderPath = null;

    public PATSTATPatents(String folderPath, String filePath) {
        this.filePath = filePath;
        this.folderPath = folderPath;
        this.patents = new LinkedList<Patent>();
    }
    
    /*
    ** Read NPRs from a folder containig a list of csv (";" separator) files extracted from PATSTAT
    */
    public LinkedList<Patent> readPATSTATPatents(){
        File []file_paths;
        if(folderPath != null){
             file_paths = new File(folderPath).listFiles();
        }else{
            file_paths =  new File[1];
            file_paths[0] =  new File(filePath);
        }
        
        HashMap<String,Patent> dictionaryPatent = new HashMap<String,Patent>();
        for(File file : file_paths){
            try {
                BufferedReader bw = null;
                FileReader fw = new FileReader(file);
                bw = new BufferedReader(fw);
                while(bw.ready()){
                    try{
                        bw.readLine();
                        String nextLine = bw.readLine();
                        String[]parameters = nextLine.split(";\"");

                        String id = parameters[0].replace("\"","");
                        String nprs = parameters[1].replace("\"","");
                        String ipcs = parameters[2].replace("\"","");
                        String abstract_ = parameters[3].replace("\"","");
                        String application_authority = parameters[4].replace("\"","");
                        Patent patent;

                        if(dictionaryPatent.containsKey(id)){
                            patent = dictionaryPatent.get(id);
                        }else{
                            patent = new Patent();
                            patent.patentID = id;
                            patent.patentAbstract = abstract_;
                            patent.application_authority = application_authority;
                            patents.add(patent);
                        }

                        for(String ipc : ipcs.split(";")){
                            if(ipc.length()>4 && ipc.contains("/")){
                               patent.ipcs.add(ipc);
                            }
                        }

                        for(String npr : nprs.split("cited by")){
                            if(npr.length()>4 ){
                               patent.references.add(npr);
                            }
                        }
                        dictionaryPatent.put(id,patent);
                    }catch (Exception ioe) {
                        ioe.printStackTrace();
                    }
                } 
               bw.close();
            }catch(Exception e1){
            }
        }
        return patents;
    }
    
    public LinkedList<Patent> readPATSTATPatentsToBeClassified(){
        File []file_paths;
        System.out.println(folderPath);
        System.out.println(filePath);
        
        if(folderPath != null){
             file_paths = new File(folderPath).listFiles();
        }else{
            file_paths =  new File[1];
            System.out.println(filePath);
            file_paths[0] =  new File(filePath);
        }
        
        HashMap<String,Patent> dictionaryPatent = new HashMap<String,Patent>();
        for(File file : file_paths){
            try{
                Reader reader = Files.newBufferedReader(Paths.get(file.getAbsolutePath()));
                CSVParser parser = new CSVParserBuilder().withSeparator(';').build();
                CSVReader csvReader = new CSVReaderBuilder(reader).withCSVParser(parser).build();
                List<String[]> records = csvReader.readAll();
                
                for(String[] record : records){
                    if(record[2].contentEquals("appln_abstract")){
                        continue;
                    }
                    String id = record[0];
                    String ipcs = record[1];
                    String assignes = record[4]+"("+record[5]+")";
                    String abstract_ = record[2].replace("\"","");

                    Patent patent;
                    if(dictionaryPatent.containsKey(id)){
                        patent = dictionaryPatent.get(id);
                    }else{
                        patent = new Patent();
                        patent.patentID = id;
                        patent.patentAbstract = abstract_;
                        patent.assignes = assignes;
                        patents.add(patent);
                    }
                    for(String ipc : ipcs.split(";")){
                        if(ipc.length()>4 && ipc.contains("/")){
                           patent.ipcs.add(ipc);
                        }
                    }

                    dictionaryPatent.put(id,patent);
                }
            }catch(Exception e){
                e.printStackTrace();
            }
            
        }
        return patents;
    }
}
