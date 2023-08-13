/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package patent;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Scanner;

/**
 *
 * @author Lucas
 */
public class USPTOPatents {
    LinkedList<Patent> patents;
    String filePath = null;
    String folderPath = null;

    public USPTOPatents(String folderPath, String filePath) {
        this.filePath = filePath;
        this.folderPath = folderPath;
        this.patents = new LinkedList<Patent>();
    }
    
    public LinkedList<Patent> readUSPTOPatents(){
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
                Scanner myReader = new Scanner(file);
                int num=0;
                HashMap<String,Integer> field_frequency = new HashMap<String,Integer>();
                while(myReader.hasNextLine()){
                    try{
                        String nextLine = myReader.nextLine();
                        String[]parameters = nextLine.split("\",\"");
                        
                        String id = parameters[0].replace("\"","");
                        String nprs = parameters[3].replace("\"","");
                        String ipcs = parameters[1].replace("\"","");
                        String abstract_ = parameters[2].replace("\"","");
                        
                        Patent patent;
                        if(dictionaryPatent.containsKey(id)){
                            patent = dictionaryPatent.get(id);
                        }else{
                            patent = new Patent();
                            patent.patentID = id;
                            patent.patentAbstract = abstract_;
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
                        
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
            }catch(Exception e1){
            }
        }
        return patents;
    }
    
    public LinkedList<Patent> readUSPTOPatentsToBeClassified(){
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
            try {
                Scanner myReader = new Scanner(file);
                int num=0;
                HashMap<String,Integer> field_frequency = new HashMap<String,Integer>();
                while(myReader.hasNextLine()){
                    try{
                        String nextLine = myReader.nextLine();
                        String[]parameters = nextLine.split("\t");
                        
                        String id = parameters[0].replace("\"","");
                        String ipcs = parameters[5].replace("\"","");
                        String title = parameters[1];
                        String assignes = parameters[3];
                        String inventors = parameters[4];
                        String abstract_ = parameters[2].replace("\"","");
                        
                        Patent patent;
                        if(dictionaryPatent.containsKey(id)){
                            patent = dictionaryPatent.get(id);
                        }else{
                            patent = new Patent();
                            patent.patentID = id;
                            patent.patentTitle = title;
                            patent.patentAbstract = abstract_;
                            patent.assignes = assignes;
                            patent.inventors = inventors;
                            patents.add(patent);
                        }
                        for(String ipc : ipcs.split(";")){
                            if(ipc.length()>4 && ipc.contains("/")){
                               patent.ipcs.add(ipc);
                            }
                        }
                        
                        dictionaryPatent.put(id,patent);
                        
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
            }catch(Exception e1){
            }
        }
        return patents;
    }
    
    
}
