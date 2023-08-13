/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import BayesianClassifier.BERTLikelihood;
import BayesianClassifier.Classifier;
import BayesianClassifier.Likelihood;
import BayesianClassifier.NaiveBayesClassifier;
import BayesianClassifier.Prior;
import BayesianClassifier.ClassifierThread;
import BayesianClassifier.HybridLikelihood;
import BayesianClassifier.HybridLikelihood2;
import BayesianClassifier.HybridLikelihood3;
import BayesianClassifier.SVMOneClass;
import NPRClassifier.NPRPatentClassifier;
import Metrics.Metrics;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import patent.PATSTATPatents;
import patent.Patent;
import patent.USPTOPatents;
import OntologyTagging.AssigneTagger;
import java.util.Random;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 *
 * @author Lucas
 */
public class Main {
    
    public static boolean log =false;
    public static HashSet<String> scientific_areas_exception = new HashSet<String>();
    public static LinkedList<Patent> usptoPatents;
    public static LinkedList<Patent> patstatPatents;
    public static LinkedList<Patent> patstatPatentsIncremented;
    public static Likelihood likelihood_dummy;
    public static NPRPatentClassifier nprClassifier = new NPRPatentClassifier("/home/lucas/Documentos/backup/Documents/usp/patentClassifier/Patent_Classifier_Data/NPRs-Dictionary/Articles-Glanzel-WoS.csv");
    public static LinkedList<String> allScientificAreas = new LinkedList<String>();
    
    public Main(){
        scientific_areas_exception.add("multidisciplinary");
        scientific_areas_exception.add("social sciences");
        scientific_areas_exception.add("art & humanities");
        scientific_areas_exception.add("- d'extraction (120) de valeurs de caractÃ©ristiques de couleur d'au moins une zone de l'image numÃ©rique capturÃ©e, par le module (12) de traitement d'image numÃ©rique, et");
        scientific_areas_exception.add("20090601, TSINGHUA UNIVERSITY PRESS, BEIJING, CN");
        scientific_areas_exception.add("Published: October 29, 2008 DOI: 10.1371/journal.pone.0003511");
        scientific_areas_exception.add("[]");
        
        usptoPatents = Patent.ReadPatentsInFile("PATSTATPatents4-unified-medicine.csv");
        
        LinkedList<Patent> filtered_test = new LinkedList<Patent>();
        
        for(Patent pat: usptoPatents){
            boolean approved_ipc = false;
            for(String ipc : pat.ipcs){
                String ipc_sub = ipc.substring(0,4);
                if(ipc_sub.contains("A") || ipc_sub.contains("B") || ipc_sub.contains("C") || ipc_sub.contains("D") || ipc_sub.contains("E") || ipc_sub.contains("F") || ipc_sub.contains("G") || ipc_sub.contains("H")){
                    approved_ipc = true;
                    break;
                }
            }
            if(approved_ipc){
                filtered_test.add(pat);
            }
        }
        usptoPatents = filtered_test;
        //patstatPatents = Patent.ReadPatentsInFile("PATSTATPatents4-unified-medicine-incremented.csv");
        //patstatPatentsIncremented = Patent.ReadPatentsInFile("PATSTATPatents4-unified-medicine-incremented.csv");
        patstatPatents = Patent.ReadPatentsInFile("PATSTATPatents4-unified-medicine.csv");
        System.out.println("");
    }
    
    public static Classifier getBestClassifier(){
        int ipcDigits = 4;
        boolean smoothed = false;
        boolean countCitationDuplicates = true;
        double cut = 3.5;
        double amplification = 2;
        String classifier = "nb";
        
        HashMap<String,LinkedList<Patent>> train_test = Patent.train_test_split(patstatPatents, 1);
        
        System.out.println("Patentes extraídas com NPRs "+train_test.get("train").size());
        int patentsClassified = 0;
        for(Patent patent : train_test.get("train")){
            if(patent.NPRscientificFields.size()>0){
                patentsClassified++;
            }
        }
        System.out.println("Patentes classificadas com NPRs "+patentsClassified);
        
        LinkedList<Patent> trainSet = patstatPatents;
        
        Prior prior = new Prior(trainSet,ipcDigits,smoothed);
        prior.printPrior();
        
        Likelihood likelihood = new Likelihood(countCitationDuplicates,cut,amplification);
        likelihood.trainPatents(trainSet);
        
//        likelihood.printNGramas(1);

        likelihood_dummy = new Likelihood(countCitationDuplicates,cut,1.0);
        likelihood_dummy.trainPatents(trainSet);
        
        NaiveBayesClassifier nb = new NaiveBayesClassifier(likelihood, prior,classifier);
        
        return nb;
    }
    
    public static Classifier getLikelihoodClassifier(LinkedList<Patent> trainSet){
        int ipcDigits = 4;
        boolean smoothed = false;
        boolean countCitationDuplicates = true;
        double cut = 3.5;
        double amplification = 2.0;
        String classifier = "likelihood";
        
        Prior prior = new Prior(trainSet,ipcDigits,smoothed);
        prior.printPrior();
        
        Likelihood likelihood = new Likelihood(countCitationDuplicates,cut,amplification);
        likelihood.trainPatents(trainSet);
        
        NaiveBayesClassifier nb = new NaiveBayesClassifier(likelihood, prior,classifier);
        
        return nb;
    }
    
    public static Classifier getBestClassifier(LinkedList<Patent> trainSet){
        int ipcDigits = 4;
        boolean smoothed = false;
        boolean countCitationDuplicates = true;
        double cut = 3.5;
        double amplification = 2.0;
        String classifier = "nb";
        
        Prior prior = new Prior(trainSet,ipcDigits,smoothed);
        prior.printPrior();
        
        System.out.println("Trained Prior");
        Likelihood likelihood = new Likelihood(countCitationDuplicates,cut,amplification);
        likelihood.trainPatents(trainSet);
        System.out.println("Trained Likelihood");
//        likelihood.printNGramas(1);

        likelihood_dummy = new Likelihood(countCitationDuplicates,cut,1.0);
        likelihood_dummy.trainPatents(trainSet);
        
        NaiveBayesClassifier nb = new NaiveBayesClassifier(likelihood, prior,classifier);
        
        return nb;
    }
    
    public static void printClassifierMetrics(){
        
        HashMap<String,LinkedList<Patent>> train_test = Patent.train_test_split(patstatPatents, 0.95);
        Classifier nb = getBestClassifier(train_test.get("train"));
        
        //Parameters
        LinkedList<Patent> testSet = train_test.get("test");
        
        HashMap<String,Map<String,Double>> predictions = classifyListOfPatents(testSet,nb);

        System.out.println("Computing Metrics");
        double accuracy = Metrics.getAccuracy(testSet, predictions,1);
        System.out.println("Accuracy: "+accuracy);
        
        double recall = Metrics.getRecall(testSet, predictions);
        System.out.println("Recall: "+recall);
        HashMap<String,Double> recallByClass = Metrics.getRecallByClass(testSet, predictions);
        System.out.println("################Recall by Class################");
        System.out.println("Scientfic Field\tRecall");
        for(String scientific_field : recallByClass.keySet()){
            System.out.println(scientific_field+"\t"+recallByClass.get(scientific_field));
        }
        System.out.println("################################################");
        
        double precision = Metrics.getPrecision(testSet, predictions);
        System.out.println("Precision: "+precision);
        HashMap<String,Double> precisionByClass = Metrics.getPrecisionByClass(testSet, predictions);
        System.out.println("###############Precision by Class###############");
        System.out.println("Scientfic Field\tPrecision");
        for(String scientific_field : precisionByClass.keySet()){
            System.out.println(scientific_field+"\t"+precisionByClass.get(scientific_field));
        }
        System.out.println("################################################");
        
        double f1Score = Metrics.getF1Score(recall, precision);
        System.out.println("F1 Score: "+f1Score);
        
        String[][] confusionMatrix = Metrics.confusionMatrix(testSet, predictions);
        
        for(int i=0; i < confusionMatrix.length; i++){
            for(int j=0; j < confusionMatrix[i].length; j++){
                System.out.print(confusionMatrix[i][j]+"\t");
            }
            System.out.println("");
        }
    }
    
    public static void printClassifierMetrics(Classifier nb, LinkedList<Patent> testSet){
        HashMap<String,Map<String,Double>> predictions = classifyListOfPatents(testSet,nb);

        System.out.println("Computing Metrics");
        double accuracy_first_area = Metrics.getAccuracy(testSet, predictions,1);
        System.out.println("Accuracy first area: "+accuracy_first_area);
        double accuracy_firstor_second_area = Metrics.getAccuracy(testSet, predictions,2);
        System.out.println("Accuracy first or second area: "+accuracy_firstor_second_area);
        double accuracy_second_area = Metrics.getAccuracy(testSet, predictions,3);
        System.out.println("Accuracy second area: "+accuracy_second_area);
        
        double recall = Metrics.getRecall(testSet, predictions);
        System.out.println("Recall: "+recall);
        HashMap<String,Double> recallByClass = Metrics.getRecallByClass(testSet, predictions);
        System.out.println("################Recall by Class################");
        System.out.println("Scientfic Field#Recall");
        for(String scientific_field : recallByClass.keySet()){
            System.out.println(scientific_field+"#"+recallByClass.get(scientific_field).toString().replace(".",","));
        }
        System.out.println("################################################");
        
        double precision = Metrics.getPrecision(testSet, predictions);
        System.out.println("Precision: "+precision);
        HashMap<String,Double> precisionByClass = Metrics.getPrecisionByClass(testSet, predictions);
        System.out.println("###############Precision by Class###############");
        System.out.println("Scientfic Field\tPrecision");
        for(String scientific_field : precisionByClass.keySet()){
            System.out.println(scientific_field+"#"+precisionByClass.get(scientific_field).toString().replace(".",","));
        }
        System.out.println("################################################");
        
        double f1Score = Metrics.getF1Score(recall, precision);
        System.out.println("F1 Score: "+f1Score);
        
        String[][] confusionMatrix = Metrics.confusionMatrix(testSet, predictions);
        
        for(int i=0; i < confusionMatrix.length; i++){
            for(int j=0; j < confusionMatrix[i].length; j++){
                System.out.print(confusionMatrix[i][j]+"\t");
            }
            System.out.println("");
        }
    }
    
    public static void printClassifierMetrics(LinkedList<Patent> testSet,HashMap<String,Map<String,Double>> predictions){
        System.out.println("Computing Metrics");
        double accuracy = Metrics.getAccuracy(testSet, predictions,1);
        System.out.println("Accuracy: "+accuracy);
        
        double recall = Metrics.getRecall(testSet, predictions);
        System.out.println("Recall: "+recall);
        HashMap<String,Double> recallByClass = Metrics.getRecallByClass(testSet, predictions);
        System.out.println("################Recall by Class################");
        System.out.println("Scientfic Field\tRecall");
        for(String scientific_field : recallByClass.keySet()){
            System.out.println(scientific_field+"\t"+recallByClass.get(scientific_field));
        }
        System.out.println("################################################");
        
        double precision = Metrics.getPrecision(testSet, predictions);
        System.out.println("Precision: "+precision);
        HashMap<String,Double> precisionByClass = Metrics.getPrecisionByClass(testSet, predictions);
        System.out.println("###############Precision by Class###############");
        System.out.println("Scientfic Field\tPrecision");
        for(String scientific_field : precisionByClass.keySet()){
            System.out.println(scientific_field+"\t"+precisionByClass.get(scientific_field));
        }
        System.out.println("################################################");
        
        double f1Score = Metrics.getF1Score(recall, precision);
        System.out.println("F1 Score: "+f1Score);
        
        String[][] confusionMatrix = Metrics.confusionMatrix(testSet, predictions);
        
        for(int i=0; i < confusionMatrix.length; i++){
            for(int j=0; j < confusionMatrix[i].length; j++){
                System.out.print(confusionMatrix[i][j]+"\t");
            }
            System.out.println("");
        }
    }
    
    public static void classifyNPRS(){
        //test set
        USPTOPatents usptoReader = new USPTOPatents(null,"/home/lucas/Documentos/backup/Documents/usp/patentClassifier/Patent Classifier Data/USPTO Patent test data");
        LinkedList<Patent> usptoPatents = usptoReader.readUSPTOPatents();
        
        //train set
        PATSTATPatents patstatReader= new PATSTATPatents("/home/lucas/Documentos/backup/Documents/usp/patentClassifier/Patent Classifier Data/PATSTAT Patent train data",null);
        LinkedList<Patent> patstatPatents = patstatReader.readPATSTATPatents();
        
        //Print the size of test and train set
        System.out.println("Test set size:\t" + usptoPatents.size());
        System.out.println("Train set size:\t" + patstatPatents.size());
       
        nprClassifier.readGlanzelJournalClassification();
        
        System.out.println("\nClassifing patents by Glanzel using nprs");
        nprClassifier.classifyPatentsUsingNPRs(patstatPatents,log);
        
        Patent.writePatentsInFile(patstatPatents,"PATSTATPatents4digits-2.csv");
    }
    
    public static HashMap<String,Map<String,Double>> classifyListOfPatentsByChunks(LinkedList<Patent> patents, Classifier classifier, int numberOfChunks){
        int begin = 0;
        int finish = patents.size()/numberOfChunks;
        LinkedList<ClassifierThread> classifierThreads = new LinkedList<ClassifierThread>();
        
        for(int i=0;i<numberOfChunks;i++){
            while(finish > patents.size()){
                finish--;
            }
            LinkedList<Patent> patents_sublist = new LinkedList(patents.subList(begin, finish));
            ClassifierThread classifierThread = new ClassifierThread(patents_sublist,classifier);
            classifierThread.start();
            classifierThreads.add(classifierThread);
            begin = finish+1;
            finish = begin + patents.size()/numberOfChunks;
        }
        
        for(ClassifierThread th : classifierThreads){
            try{
                th.join();
            }catch(Exception e){}
        }
        HashMap<String,Map<String,Double>> predictions = new HashMap<String,Map<String,Double>>();
        for(ClassifierThread th : classifierThreads){
            predictions.putAll(th.predictions);
        }
//        int cont = 0;
//        int contNonNull = 0;
//        for(String id : predictions.keySet()){
//            if(predictions.get(id)!= null){
//                contNonNull++;
//            }
//            cont++;
//        }
//        System.out.println("Classify "+cont);
//        System.out.println("Classify NonNull"+contNonNull);
        return predictions;
//        Patent.writePatentsInFile(patents, "TesteClassifiedPatents2.tsv");
    }
    
    public static HashMap<String,Map<String,Double>> classifyListOfPatents(LinkedList<Patent> patents, Classifier classifier){
        
        ClassifierThread classifierThread = new ClassifierThread(patents,classifier);
        classifierThread.start();
        try{
            classifierThread.join();
            return classifierThread.predictions;
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }
    
    public static void printFieldCoOcurrenceMatrix(){
        String[][] co_ocurrenceMatrix = Metrics.scientificFieldscoOccurrencesMatrix(patstatPatents);
        
        for(int i=0; i < co_ocurrenceMatrix.length; i++){
            for(int j=0; j < co_ocurrenceMatrix[i].length; j++){
                System.out.print(co_ocurrenceMatrix[i][j]+"\t");
            }
            System.out.println("");
        }
    }
    
    public static LinkedList<Patent> makeStratifiedValidation(LinkedList<Patent> patentsTest, LinkedList<Patent> patentsValidation){
        HashMap<String,Double> props = new HashMap<String,Double>();
        int sum =0;
        HashMap<String,Integer> removeds = new HashMap<String,Integer>();
        for(Patent pat : patentsTest){
            LinkedList<String> fields = Metrics.getTwoMostFrequentElementInList(pat.NPRscientificFields);
            if(fields.size() > 0 && fields.get(0)!= null && !fields.get(0).contentEquals("")){
                if(!props.containsKey(fields.get(0))){
                    props.put(fields.get(0),0.0);
                }
                props.put(fields.get(0),props.get(fields.get(0))+1.0);
                sum+=1;
            }
        }
        
        for(String field: props.keySet()){
            System.out.println(field+" "+props.get(field));
        }
        
        for(String field: props.keySet()){
            removeds.put(field,0);
            props.put(field,props.get(field)/sum);
        }
        
        System.out.println("");
        HashMap<String,Double> propsValidation = new HashMap<String,Double>();
        HashMap<String,Double> propsValidation2 = new HashMap<String,Double>();
        int sumValidation =0;
        for(Patent pat : patentsValidation){
            LinkedList<String> fields = Metrics.getTwoMostFrequentElementInList(pat.NPRscientificFields);
            if(fields.size() > 0 && fields.get(0)!= null && !fields.get(0).contentEquals("")){
                if(fields.size() > 0 && fields.get(0)!= null && !fields.get(0).contentEquals("")){
                    if(!propsValidation.containsKey(fields.get(0))){
                        propsValidation.put(fields.get(0),0.0);
                    }
                    propsValidation.put(fields.get(0),propsValidation.get(fields.get(0))+1.0);
                    sumValidation+=1;
                }
            }
        }
        
        propsValidation2.putAll(propsValidation);
        
        for(String field: propsValidation.keySet()){
            propsValidation.put(field,propsValidation.get(field)/sumValidation);
        }
//        for(String field: propsValidation.keySet()){
//            System.out.println(field+" "+propsValidation.get(field));
//        }
        
        for(int i = 0;i<9;i++){
//            System.out.println("");
//            System.out.println("iteration "+ i);
            for(String field: propsValidation.keySet()){
                double difference = propsValidation2.get(field)/sumValidation - props.get(field);
                if(difference > 0){
                    if(!removeds.containsKey(field)){
                        removeds.put(field,0);
                    }
                    int removed = (int)(difference * sumValidation);
                    removeds.put(field,removeds.get(field)+removed);
//                    System.out.println(field+" "+removed);
                    propsValidation2.put(field, propsValidation2.get(field) - removed);
                    sumValidation = sumValidation - removed;
                }
            }
            
//            for(String field: propsValidation2.keySet()){
//                System.out.println(field+" "+propsValidation2.get(field)/sumValidation);
//            }
        }
//        System.out.println("");
        LinkedList<Patent> stratifiedValidation = new LinkedList<Patent>();
        for(String field: removeds.keySet()){
            int removed = 0;
            for(Patent pat: patentsValidation){
                LinkedList<String> fields = Metrics.getTwoMostFrequentElementInList(pat.NPRscientificFields);
                if(fields.size() > 0 && fields.get(0)!= null && !fields.get(0).contentEquals("")){
                    if(fields.get(0).contentEquals(field)){
                        if(removed - removeds.get(field) != 0){
                            removed++;
                        }else{
                            stratifiedValidation.add(pat);
                        }
                    }
                }
                
            }
        }
//        System.out.println("After stratification");
        HashMap<String,Double> props2 = new HashMap<String,Double>();
        int sum2 =0;
        for(Patent pat : stratifiedValidation){
            LinkedList<String> fields = Metrics.getTwoMostFrequentElementInList(pat.NPRscientificFields);
            if(fields.size() > 0 && fields.get(0)!= null && !fields.get(0).contentEquals("")){
                if(!props2.containsKey(fields.get(0))){
                    props2.put(fields.get(0),0.0);
                }
                props2.put(fields.get(0),props2.get(fields.get(0))+1.0);
                sum2+=1;
            }
        }
        for(String field: props2.keySet()){
            System.out.println(field+" "+props2.get(field));
        }
        
        for(String field: props2.keySet()){
            props2.put(field,props2.get(field)/sum2);
        }
        
        return stratifiedValidation;
    }
    
    public static void classifyGooglePatentsSample(){
        Classifier cls = getBestClassifier();
        LinkedList<Patent> patents = Patent.ReadPatentsInFile2("","/home/lucas/Documentos/mestrado/google-patent-data2/");
        classifyListOfPatents(patents, cls);
        Patent.writePatentsInFile(patents, "/home/lucas/Documentos/mestrado/deletar2.csv");
    }
    
    public static void fineTuneSuperpositionEnsemble(){
        HashMap<String,Patent> patents = new HashMap<String,Patent>();
        for(Patent patent : patstatPatents){
            patents.put(patent.patentAbstract,patent);
        }
        
        LinkedList<Patent> test = new LinkedList<Patent>();
        LinkedList<Patent> validation = new LinkedList<Patent>();
        LinkedList<String> test_abstracts = Patent.getAbstractsInBERT2("/home/lucas/Documentos/backup/Downloads/predictions (6).csv",24);
        System.out.println(test_abstracts.size());
        int removed = 0;
        HashSet<String> added = new HashSet<String>();
        for(String test_abstract : test_abstracts){
            if(patents.containsKey(test_abstract)){
                test.add(patents.get(test_abstract));
                added.add(patents.get(test_abstract).patentAbstract);
            }
        }
        
        LinkedList<String> validation_abstracts = Patent.getAbstractsInBERT2("/home/lucas/Documentos/backup/Documents/usp/BERTPatents/data/train/data_validation.tsv",1);
        System.out.println(validation_abstracts.size());
        for(String validation_abstract : validation_abstracts){
            if(patents.containsKey(validation_abstract)){
//                test.add(patents.get(validation_abstract));
                validation.add(patents.get(validation_abstract));
                added.add(patents.get(validation_abstract).patentAbstract);
            }
        }
        
        LinkedList<Patent> train = new LinkedList<Patent>();
        for(Patent pat : patstatPatents){
            if(!added.contains(pat.patentAbstract)){
                train.add(pat);
            }
        }
        LinkedList<Patent> trainNbLikelihood = new LinkedList<Patent>();
        for(Patent pat : patstatPatentsIncremented){
            if(!added.contains(pat.patentAbstract)){
                trainNbLikelihood.add(pat);
            }
        }
        System.out.println("train prior "+train.size());
        System.out.println("train likelihood "+trainNbLikelihood.size());
//        BERTLikelihood likelihood = new BERTLikelihood(0.35);
//        likelihood.readDictionaryOfClassifications("/home/lucas/Documentos/backup/Documents/usp/BERTPatents/data/predictions.csv");
//        System.out.println(likelihood.classifyPatent(test.get(2)));
        
        test = makeStratifiedValidation(train,test);
        
//        validation = makeStratifiedValidation(test,validation);
        
//        for(Patent pat : test){
//            LinkedList<String> fields = Metrics.getTwoMostFrequentElementInList(pat.NPRscientificFields);
//            if(fields.size()>0){
//                if(fields.get(0).contains("biomedical") && !pat.patentAbstract.contains("Ã©")){
//                        System.out.println("biomedical research\t"+pat.patentAbstract);
//                }
//                if(fields.get(0).contains("clinical") && !pat.patentAbstract.contains("Ã©")){
//                        System.out.println("clinical and exeperimental medicine\t"+pat.patentAbstract);
//                }
//                if(fields.get(0).contains("chemistry") && !pat.patentAbstract.contains("Ã©")){
//                        System.out.println("chemistry\t"+pat.patentAbstract);
//                }
//            }
//        }
        System.out.println("tamanho do conjunto de teste "+test.size());
        for(double amplification= 1.3; amplification<1.31; amplification+=0.3){
//            prior.printPrior();
            
//            for(double amplification_bert = 0.1;amplification_bert<3;amplification_bert+=0.1){
////                confidence_cut = 1;
////                NaiveBayesClassifier nb = new NaiveBayesClassifier(hybridLikelihood, prior,"likelihood");
//                System.out.println("amplification_bert "+amplification_bert);
//                BERTLikelihood likelihood = new BERTLikelihood(amplification,amplification_bert,true);
////            likelihood.readDictionaryOfClassifications("C:/Users/Carlos/Documents/usp/BERTPatents/data/predictions.csv");
//                likelihood.readDictionaryOfClassifications("/home/lucas/Documentos/backup/Downloads/predictions (7).csv");
////                    likelihood.readDictionaryOfClassifications("/home/lucas/Documentos/backup/Downloads/predictions-validation.csv");
//                printClassifierMetrics(likelihood, test);
//            }
            
            for(double confidence_cut = 0.96;confidence_cut<0.97;confidence_cut+=0.01){
//                confidence_cut = 1;
//                NaiveBayesClassifier nb = new NaiveBayesClassifier(hybridLikelihood, prior,"likelihood");
//                        for(double amplification_nb = 0.1;amplification_nb<2.7;amplification_nb+=0.3){
//                            for(double amplification_bert = 0.1;amplification_bert<2.7;amplification_bert+=0.3){
                Classifier prior = new Prior(trainNbLikelihood,4,false);
                BERTLikelihood likelihood = new BERTLikelihood(amplification,0.1,false);
//            likelihood.readDictionaryOfClassifications("C:/Users/Carlos/Documents/usp/BERTPatents/data/predictions.csv");
                likelihood.readDictionaryOfClassifications("/home/lucas/Documentos/backup/Downloads/predictions (7).csv",null);
                likelihood.readDictionaryOfClassifications("/home/lucas/Documentos/backup/Downloads/predictions-validation.csv",null);
                boolean countCitationDuplicates = true;
                double cut = 3.5;
                Likelihood nbLikelihood = new Likelihood(countCitationDuplicates,cut,2);
                nbLikelihood.trainPatents(trainNbLikelihood);
                NaiveBayesClassifier nb = new NaiveBayesClassifier(nbLikelihood, prior,"nb");
                HybridLikelihood2 hybridLikelihood2 = new HybridLikelihood2(nb,likelihood,0.4,1.0);
                HybridLikelihood hybridLikelihood = new HybridLikelihood(nb,likelihood,confidence_cut);
                SVMOneClass svm = new SVMOneClass();
                svm.readDictionaryOfClassifications("/home/lucas/Documentos/backup/Downloads/predictions_biomedical (1).csv", patstatPatents);
                //svm.readDictionaryOfClassifications("/home/lucas/Documentos/mestrado/SVMOneClass/predictions_biomedical_validation.csv", patstatPatents);
                HybridLikelihood3 hybridLikelihood3 = new HybridLikelihood3(hybridLikelihood2,svm,hybridLikelihood,0.35,0.18);
                printClassifierMetrics(hybridLikelihood3, test);
//                            }
//                        }
            }
//            Patent.writePatentsInFile(test,"/home/lucas/Documentos/mestrado/SVMOneClass/validationSet.csv");
            
//             for(double amplification_nb = 0.1;amplification_nb<2.7;amplification_nb+=0.3){
//                 for(double amplification_bert = 0.1;amplification_bert<2.7;amplification_bert+=0.3){
//    //                confidence_cut = 1;
//    //                NaiveBayesClassifier nb = new NaiveBayesClassifier(hybridLikelihood, prior,"likelihood");
//                    Prior prior = new Prior(train,4,false);
//                    BERTLikelihood likelihood = new BERTLikelihood(amplification,1.0,false);
//    //            likelihood.readDictionaryOfClassifications("C:/Users/Carlos/Documents/usp/BERTPatents/data/predictions.csv");
//                    likelihood.readDictionaryOfClassifications("/home/lucas/Documentos/backup/Downloads/predictions (7).csv");
//    //                    likelihood.readDictionaryOfClassifications("/home/lucas/Documentos/backup/Downloads/predictions-validation.csv");
//                    boolean countCitationDuplicates = true;
//                    double cut = 3.5;
//                    Likelihood nbLikelihood = new Likelihood(countCitationDuplicates,cut,2);
//                    nbLikelihood.trainPatents(trainNbLikelihood);
//                    System.out.println("amplification_nb "+amplification_nb+" amplification_bert "+amplification_bert);
//                    NaiveBayesClassifier nb = new NaiveBayesClassifier(nbLikelihood, prior,"nb");
//                    HybridLikelihood2 hybridLikelihood2 = new HybridLikelihood2(nb,likelihood,amplification_nb,amplification_bert);
//                    printClassifierMetrics(hybridLikelihood2, test);
//                 }
//            }
        }
    }
    
    public static Classifier getSuperpositionEnsembleClassfier(){
        HashMap<String,Patent> patents = new HashMap<String,Patent>();
        for(Patent patent : patstatPatents){
            patents.put(patent.patentAbstract,patent);
        }
        
        LinkedList<Patent> test = new LinkedList<Patent>();
        LinkedList<Patent> validation = new LinkedList<Patent>();
        LinkedList<String> test_abstracts = Patent.getAbstractsInBERT2("/home/lucas/Documentos/backup/Downloads/predictions (6).csv",24);
        System.out.println(test_abstracts.size());
        int removed = 0;
        HashSet<String> added = new HashSet<String>();
        for(String test_abstract : test_abstracts){
            if(patents.containsKey(test_abstract)){
                test.add(patents.get(test_abstract));
                added.add(patents.get(test_abstract).patentAbstract);
            }
        }
        
        LinkedList<String> validation_abstracts = Patent.getAbstractsInBERT2("/home/lucas/Documentos/backup/Documents/usp/BERTPatents/data/train/data_validation.tsv",1);
        System.out.println(validation_abstracts.size());
        for(String validation_abstract : validation_abstracts){
            if(patents.containsKey(validation_abstract)){
//                test.add(patents.get(validation_abstract));
                validation.add(patents.get(validation_abstract));
                added.add(patents.get(validation_abstract).patentAbstract);
            }
        }
        
        LinkedList<Patent> train = new LinkedList<Patent>();
        for(Patent pat : patstatPatents){
            if(!added.contains(pat.patentAbstract)){
                train.add(pat);
            }
        }
        LinkedList<Patent> trainNbLikelihood = new LinkedList<Patent>();
        for(Patent pat : patstatPatentsIncremented){
            if(!added.contains(pat.patentAbstract)){
                trainNbLikelihood.add(pat);
            }
        }
        
        Classifier prior = new Prior(trainNbLikelihood,4,false);
        LinkedList<Patent> patentsScopus = Patent.ReadPatentsInFile("/home/lucas/Documentos/mestrado/SVMOneClass/deletar2.csv");
        BERTLikelihood likelihood = new BERTLikelihood(1.3,0.1,false);
//            likelihood.readDictionaryOfClassifications("C:/Users/Carlos/Documents/usp/BERTPatents/data/predictions.csv");
        likelihood.readDictionaryOfClassifications("/home/lucas/Documentos/mestrado/predictions-sulamerican.csv",patentsScopus);
//                                likelihood.readDictionaryOfClassifications("/home/lucas/Documentos/backup/Downloads/predictions-validation.csv");
        boolean countCitationDuplicates = true;
        double cut = 3.5;
        Likelihood nbLikelihood = new Likelihood(countCitationDuplicates,cut,2);
        nbLikelihood.trainPatents(trainNbLikelihood);
        NaiveBayesClassifier nb = new NaiveBayesClassifier(nbLikelihood, prior,"nb");
        HybridLikelihood2 hybridLikelihood2 = new HybridLikelihood2(nb,likelihood,0.4,1.0);
        HybridLikelihood hybridLikelihood = new HybridLikelihood(nb,likelihood,0.96);
        SVMOneClass svm = new SVMOneClass();
        svm.readDictionaryOfClassifications("/home/lucas/Documentos/mestrado/SVMOneClass/predictions_biomedical-temp.csv",patentsScopus);
        HybridLikelihood3 hybridLikelihood3 = new HybridLikelihood3(hybridLikelihood2,svm,hybridLikelihood,0.2,0.18);
        
        HashMap<String,Map<String,Double>> classifications = classifyListOfPatents(patentsScopus, hybridLikelihood3);
        System.out.println("Numero de patentes "+ patentsScopus.size());
        System.out.println("Terminou classificaçoes");
        for(String id : classifications.keySet()){
            System.out.println(id+"\t"+classifications.get(id));
        }
        return hybridLikelihood3;
    }
    
    public static void main(String[] args) {
        Main m = new Main();
//        classifyGooglePatentsSample();
//        getSuperpositionEnsembleClassfier();fineTuneSuperpositionEnsemble()
        fineTuneSuperpositionEnsemble();
        
//        printFieldCoOcurrenceMatrix();
//        printClassifierMetrics();

//        for(Patent patent : patstatPatents){
//            LinkedList<String> fields = Metrics.getTwoMostFrequentElementInList(patent.NPRscientificFields);
////            System.out.println(fields);
//            if(fields != null && fields.size() > 0){
//                String field = fields.get(0);
//                if(field.equals("agriculture & environment") || field.equals("biology (organismic & supraorganismic level)") || field.equals("chemistry") || field.equals("clinical and experimental medicine") || field.equals("biomedical research") || field.equals("engineering") || field.equals("art & humanities")|| field.equals("physics") || field.equals("neuroscience & behavior") || field.equals("mathematics") || field.equals("geociences & space sciences") || field.equals("biosciences (general, cellular&subcellular biology; genetics)")){
//                    if(!patent.patentAbstract.contains("L'") && !patent.patentAbstract.contains("Ã©") && !patent.patentAbstract.contains("l'")){
//                        System.out.println(fields.get(0)+"\t"+patent.patentAbstract);
//                    }
//                }
//            }
//        }
        
        //LinkedList<Patent> patstatPatents2 = Patent.ReadPatentsInFile("PATSTATPatents4digits-2-Incremented-filtered.csv");


        
//        for(Patent patent : train){
//            LinkedList<String> fields = Metrics.getTwoMostFrequentElementInList(patent.NPRscientificFields);
////            System.out.println(fields);
//            if(fields != null && fields.size() > 0){
//                String field = fields.get(0);
//                System.out.println(field);
//                if(field.equals("agriculture & environment") || field.equals("biology (organismic & supraorganismic level)") || field.equals("chemistry") || field.equals("clinical and experimental medicine") || field.equals("biomedical research") || field.equals("engineering") || field.equals("art & humanities")|| field.equals("physics") || field.equals("neuroscience & behavior") || field.equals("mathematics") || field.equals("geociences & space sciences") || field.equals("biosciences (general, cellular&subcellular biology; genetics)")){
//                    System.out.println(fields.get(0)+"\t"+patent.patentAbstract);
//                }
//            }
//        }
        
    }
}
