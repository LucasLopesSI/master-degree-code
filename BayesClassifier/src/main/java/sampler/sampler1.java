/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sampler;

import BayesianClassifier.BERTLikelihood;
import BayesianClassifier.Classifier;
import BayesianClassifier.HybridLikelihood;
import BayesianClassifier.HybridLikelihood2;
import BayesianClassifier.HybridLikelihood3;
import BayesianClassifier.Likelihood;
import BayesianClassifier.NaiveBayesClassifier;
import BayesianClassifier.Prior;
import BayesianClassifier.SVMOneClass;
import Metrics.Metrics;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import main.Main;
import static main.Main.patstatPatents;
import patent.Patent;

/**
 *
 * @author lucas
 */
public class sampler1 {
    
    String filePath;
    String folderPath;
    LinkedList<Patent> patents = new LinkedList<Patent>();
    HashMap<String,String> techDictionary = new HashMap<String,String>();
    

    public sampler1(String filePath, String folderPath) {
        this.filePath = filePath;
        this.folderPath = folderPath;
    }
    
    public void readTechDIctionary(File file){

        try{
            Reader reader = Files.newBufferedReader(Paths.get(file.getAbsolutePath()));
            CSVParser parser = new CSVParserBuilder().withSeparator('\t').build();
            CSVReader csvReader = new CSVReaderBuilder(reader).withCSVParser(parser).build();
            List<String[]> records = csvReader.readAll();

            for(String[] record : records){
                String ost = record[0];
                String ipc = record[1].replace("%","");

                techDictionary.put(ipc,ost);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    
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
            try{
                Reader reader = Files.newBufferedReader(Paths.get(file.getAbsolutePath()));
                CSVParser parser = new CSVParserBuilder().withSeparator(';').build();
                CSVReader csvReader = new CSVReaderBuilder(reader).withCSVParser(parser).build();
                List<String[]> records = csvReader.readAll();
                
                for(String[] record : records){
                    if(record[0].contentEquals("appln_id")){
                        continue;
                    }
                    String id = record[0];
                    String ipcs = record[1];
                    String assignes = record[4];
                    String authority = record[5];
                    String abstract_ = record[2].replace("\"","");

                    Patent patent;
                    if(dictionaryPatent.containsKey(id)){
                        patent = dictionaryPatent.get(id);
                    }else{
                        patent = new Patent();
                        patent.patentID = id;
                        patent.patentAbstract = abstract_;
                        patent.assignes = assignes;
                        patent.application_authority = authority;
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
    
    public static void samplePatents(sampler1 s){
        HashSet<String> countries = new HashSet<String>();
        countries.add("BR");
        countries.add("AR");
        countries.add("VE");
        countries.add("CO");
        countries.add("PE");
        countries.add("BO");
        countries.add("PY");
        countries.add("UY");
        countries.add("EC");
        countries.add("GY");
        countries.add("SR");
        countries.add("GF");
        
        LinkedList<Patent> SulPatents = new LinkedList<Patent>();
        
        for(Patent pat: s.patents){
            if(countries.contains(pat.application_authority)){
                SulPatents.add(pat);
            }
        }
        LinkedList<Patent>SampledPatent = new LinkedList<Patent>();
        int sampleSize = 3000;
        int sampled = 0;
        Random rd = new Random();
        
        while(sampled < sampleSize || SulPatents.isEmpty()){
            SampledPatent.add(SulPatents.remove(rd.nextInt(SulPatents.size())));
            sampled++;
        }
        
        for(Patent pat: SampledPatent){
            System.out.println(pat.patentID+"\t"+pat.patentAbstract+"\t"+pat.application_authority);
        }
    }
    
    public static void printDistributionOfAreas(HashMap<String,Map<String,Double>> predictions, String id){
        try{
            System.out.print(predictions.get(id).get("chemistry")+"\t");
        }catch(Exception e){
            System.out.print("0\t");
        }
        try{
            System.out.print(predictions.get(id).get("engineering")+"\t");
        }catch(Exception e){
            System.out.print("0\t");
        }
        try{
            System.out.print(predictions.get(id).get("physics")+"\t");
        }catch(Exception e){
            System.out.print("0\t");
        }
        try{
            System.out.print(predictions.get(id).get("clinical and experimental medicine")+"\t");
        }catch(Exception e){
            System.out.print("0\t");
        }
        try{
            System.out.print(predictions.get(id).get("biology (organismic & supraorganismic level)")+"\t");
        }catch(Exception e){
            System.out.print("0\t");
        }
        try{
            System.out.print(predictions.get(id).get("geociences & space sciences")+"\t");
        }catch(Exception e){
            System.out.print("0\t");
        }
        try{
            System.out.print(predictions.get(id).get("biomedical research")+"\t");
        }catch(Exception e){
            System.out.print("0\t");
        }
        try{
            System.out.print(predictions.get(id).get("agriculture & environment")+"\t");
        }catch(Exception e){
            System.out.print("0\t");
        }
        try{
            System.out.print(predictions.get(id).get("mathematics")+"\t");
        }catch(Exception e){
            System.out.print("0\t");
        }
        System.out.println("");
    }
    
    public static void main(String[] args) {
        Main m = new Main();
        sampler1 s = new sampler1("","/home/lucas/Documentos/backup/Documents/usp/patentClassifier/Patent_Classifier_Data/PATASTAT-Patent-data_to_be_classified");
        s.readPATSTATPatents();
        
        HashMap<String,Patent> patents = new HashMap<String,Patent>();
        for(Patent patent : s.patents){
            patents.put(patent.patentAbstract,patent);
        }
        
        HashMap<String,Patent> patents2 = new HashMap<String,Patent>();
        for(Patent patent : s.patents){
            patents2.put(patent.patentID,patent);
        }
        
        LinkedList<String> test_abstracts = Patent.getAbstractsInBERT2("/home/lucas/Documentos/backup/Documents/usp/BERTPatents/data/sulamerican/predictions-sulamerican.csv",24);
        System.out.println(test_abstracts.size());
        int removed = 0;
        HashSet<String> added = new HashSet<String>();
        LinkedList<Patent> test = new LinkedList<Patent>();
        for(String test_abstract : test_abstracts){
            if(patents.containsKey(test_abstract)){
                test.add(patents.get(test_abstract));
                added.add(patents.get(test_abstract).patentAbstract);
            }
        }
        System.out.println(test.size());
        
        Classifier prior = new Prior(patstatPatents,4,false);
        BERTLikelihood likelihood = new BERTLikelihood(1.3,0.1,false);
        likelihood.readDictionaryOfClassifications("../predictions-bert.csv",s.patents);

        boolean countCitationDuplicates = true;
        double cut = 3.5;
        Likelihood nbLikelihood = new Likelihood(countCitationDuplicates,cut,2);
        nbLikelihood.trainPatents(patstatPatents);
        NaiveBayesClassifier nb = new NaiveBayesClassifier(nbLikelihood, prior,"nb");
        
        HybridLikelihood2 hybridLikelihood2 = new HybridLikelihood2(nb,likelihood,0.4,1.0);
        HybridLikelihood hybridLikelihood = new HybridLikelihood(nb,likelihood,0.96);
        SVMOneClass svm = new SVMOneClass();
        svm.readDictionaryOfClassifications("../predictions-svm.csv",s.patents);
        HybridLikelihood3 hybridLikelihood3 = new HybridLikelihood3(hybridLikelihood2,svm,hybridLikelihood,0.2,0.18);
        HashMap<String,Map<String,Double>> predictions = Main.classifyListOfPatents(new LinkedList<Patent>(test), hybridLikelihood3);
        
        System.out.println("id\tchemistry\tengineering\tphysics\tclinical and experimental medicine\tbiology (organismic & supraorganismic level)\tgeociences & space sciences\tbiomedical research\tagriculture & environment\tmathematics");
        s.readTechDIctionary(new File("/home/lucas/Documentos/backup/Documents/usp/BERTPatents/data/ost_classification"));
        
        for(String id: predictions.keySet()){
            
            LinkedList<String> techAreas = new LinkedList<String>();
            for(String ipc : patents2.get(id).ipcs){
                for(String ipc2 : s.techDictionary.keySet()){
                    if(ipc.contains(ipc2)){
                        techAreas.add(s.techDictionary.get(ipc2));
                    }
                }
                
            }
            System.out.print(patents2.get(id).application_authority+"\t");
            
            if(!techAreas.isEmpty()){
                System.out.print(Metrics.getTwoMostFrequentElementInList(techAreas).get(0)+"\t");
                printDistributionOfAreas(predictions, id);
            }
            System.out.print(patents2.get(id).application_authority+"\t");
            
            for(String prediction : predictions.get(id).keySet()){
                System.out.print(prediction+"\t");
                break;
            }
            try{
                System.out.print(predictions.get(id).get("chemistry")+"\t");
            }catch(Exception e){
                System.out.print("0\t");
            }
            try{
                System.out.print(predictions.get(id).get("engineering")+"\t");
            }catch(Exception e){
                System.out.print("0\t");
            }
            try{
                System.out.print(predictions.get(id).get("physics")+"\t");
            }catch(Exception e){
                System.out.print("0\t");
            }
            try{
                System.out.print(predictions.get(id).get("clinical and experimental medicine")+"\t");
            }catch(Exception e){
                System.out.print("0\t");
            }
            try{
                System.out.print(predictions.get(id).get("biology (organismic & supraorganismic level)")+"\t");
            }catch(Exception e){
                System.out.print("0\t");
            }
            try{
                System.out.print(predictions.get(id).get("geociences & space sciences")+"\t");
            }catch(Exception e){
                System.out.print("0\t");
            }
            try{
                System.out.print(predictions.get(id).get("biomedical research")+"\t");
            }catch(Exception e){
                System.out.print("0\t");
            }
            try{
                System.out.print(predictions.get(id).get("agriculture & environment")+"\t");
            }catch(Exception e){
                System.out.print("0\t");
            }
            try{
                System.out.print(predictions.get(id).get("mathematics")+"\t");
            }catch(Exception e){
                System.out.print("0\t");
            }
            System.out.println("");
        }
    }
}
