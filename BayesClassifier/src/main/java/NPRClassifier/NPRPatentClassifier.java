/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package NPRClassifier;

/**
 *
 * @author Lucas
 */
import com.opencsv.CSVReader;
import java.io.File;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import patent.Patent;

/**
 *
 * @author Lucas
 */
public class NPRPatentClassifier {
    
    
    public String JournalClassificationFilePath;

    public NPRPatentClassifier(String JournalClassificationFilePath) {
        this.JournalClassificationFilePath = JournalClassificationFilePath;
    }
    
    public static HashMap<String,String>classification = new HashMap<>();
    public static HashMap<String,String>ISSNclassification = new HashMap<>();
    
//    public static void fillScienceMetrixJournalClassification() {
//        String scienceMetrix = "C:\\Users\\Carlos\\Desktop\\github\\ClassifierNLP\\patents_extraction\\journalreducedclassification.csv";
//        try{
//            Scanner a1 = new Scanner (new File(scienceMetrix));
//            String line = a1.nextLine();
//            int cont =0;
//            while(a1.hasNextLine()){
//                line = a1.nextLine();
//                String journal="";
//                String field = "";
//                
//                if(!line.contains("\"")){
//                    String[]dados = line.split(",");
//                    journal=dados[1].replace("\"","").toLowerCase();
//                    field = dados[2].replace("\"","");
//                }else{
//                    try{
//                        int num1=0;
//                        int num2=0;
//                        for(int i=0;i<line.length();i++){
//                            if(line.charAt(i)=='"'){
//                                if(num1==0){
//                                    num1=i;
//                                }else{
//                                    num2=i;
//                                }
//                            }
//                        }
//                        journal=line.substring(num1, num2).replace("\"","").toLowerCase();
//                        field=line.substring(num2,line.length()).replace(",","");
//                    }catch(Exception e){
//                    }
//                }
//                String[]teste_tamanho = journal.split(" ");
//                if(teste_tamanho.length>=2){
//                    classification.put(journal.toLowerCase(),field);
//                }
//                cont++;
//            }
//        }catch(Exception e){}
//    }
    
    public void readGlanzelJournalClassification(){
        try{
            Reader reader = Files.newBufferedReader(Paths.get(JournalClassificationFilePath));
            
            CSVReader csvReader = new CSVReader(reader);
            List<String[]> records = csvReader.readAll();
            for(String[] record : records){
                    System.out.println(record[2]);
                    if(record[0].equals("ps")||record[7].equals("ps")||record[8].equals("ps")){
                        continue;
                    }
                    if(record[0].equals("work")||record[7].equals("work")||record[8].equals("work")){
                        continue;
                    }
                    System.out.println("passou "+record[2]);
                    
                    classification.put(record[1],record[10]);
                    
                    if(record[7].length()>1){
                        classification.put(record[8],record[10]);
                    }
                    if(record[8].length()>1){
                        classification.put(record[9],record[10]);
                    }
                    if(record[2].length()>1){
                        ISSNclassification.put(record[2],record[10]);
                    }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        System.out.println(ISSNclassification);
        
    }
    
    public static String preTreatCitation(String citation){
        citation = citation.toLowerCase();
        if(citation.contains("'")){
            try{
                String [] citationSplitted = citation.split("'");
                citation = citationSplitted[2];
            }catch(Exception e){
            }
        }
        return citation;
    }
    
    public LinkedList<String> classifyCitation(String citation){
        citation = preTreatCitation(citation);
        String best_journal = "";
        int size = 0;
        LinkedList<String> journalAndClassification = new LinkedList<String>();
        
        if(citation.contains("issn")){
            best_journal = classifyCitationByissn(citation);
            if(best_journal.length()>1){
                journalAndClassification.add(ISSNclassification.get(best_journal));
                journalAndClassification.add(best_journal);
            }
        }else{
            for(String journal: classification.keySet()){
//                
//                if(journal.split(" ").length<=3){
//                    continue;
//                }
                
                for(String treatedJournalTitle : TreatJournalText(journal)){
                    
                    if(citation.contains(treatedJournalTitle)){
                        
                        if(journal.length() > size){
                            best_journal = journal;
                            size = journal.length();
                        }
                        
                    }

                }

            }
            
            if(size>0){                
                journalAndClassification.add(classification.get(best_journal));
                journalAndClassification.add(best_journal);
            }
        }
        return journalAndClassification;
    }
    
    public static String classifyCitationByissn(String citation){
        String issn = citation.split("issn")[1].toLowerCase();
        
        for(String ISSN: ISSNclassification.keySet()){
            if(issn.contains(ISSN)){
                return ISSN;
            }
        }
        return "";
    }
    
    public static LinkedList<String> TreatJournalText(String journalTitle){
        LinkedList<String> titles = new LinkedList<String>();
        titles.add(", "+journalTitle+",");
        
//        titles = 
        return titles;
    }
    
    public void classifyPatentsUsingNPRs(LinkedList<Patent> patents, boolean debbug){
        
        if(debbug){
            System.out.println("Number of patents to classify by glanzel "+patents.size());
        }
        int i=0;
        int classified = 0;
        for(Patent patent : patents){
            for(String reference : patent.references){
                if(debbug){
                    System.out.println("Classifying reference "+reference);
                }
                
                LinkedList<String> classificationAndJournal = this.classifyCitation(reference);
                if(classificationAndJournal.size()>1){
                    classified+=1;
                    if(debbug){
                        System.out.println("Patent "+i+" Recognized Scientific Field "+classificationAndJournal.get(0));
                        System.out.println("Patent "+i+" Recognized Journal "+classificationAndJournal.get(1));
                        System.out.println("");
                    }
                    patent.NPRscientificFields.add(classificationAndJournal.get(0));
                    patent.citedJournals.add(classificationAndJournal.get(1));
                    patent.referencesAndRecognizedFields.put(reference,classificationAndJournal.get(0));
                    patent.referencesAndRecognizedJournals.put(reference,classificationAndJournal.get(1));
                }
            }
            i++;
        }
        System.out.println("Number of references classified "+ classified);
    }
}

