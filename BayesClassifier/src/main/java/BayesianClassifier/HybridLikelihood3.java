/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package BayesianClassifier;

import java.util.HashMap;
import java.util.LinkedList;
import patent.Patent;
import Ngrams.Ngram;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import main.Main;

/**
 *
 * @author Lucas
 */
public class HybridLikelihood3 extends Classifier{
    HashMap<String,Ngram> fieldsNgrams = new HashMap<String,Ngram>();
    Classifier nbLikelihood;
    Classifier bertLikelihood;
    Classifier svm;
    double confidence_cut1;
    double confidence_cut2;

    public HybridLikelihood3(Classifier nbLikelihood, Classifier svm, Classifier bertLikelihood, double confidence_cut1,double confidence_cut2) {
        this.nbLikelihood = nbLikelihood;
        this.bertLikelihood = bertLikelihood;
        this.confidence_cut1 = confidence_cut1;
        this.confidence_cut2 = confidence_cut2;
        this.svm = svm;
    }
    
    public TreeMap<String,Double> classifyPatent(Patent patent){
        TreeMap<String,Double> nbClassification = nbLikelihood.classifyPatent(patent);
        String field1Name = "";
        String field2Name = "";
        for(String field : nbClassification.keySet()){
            field1Name = field;
            break;
        }
        
        double field1 = 0;
        double field2 = 0;
        int i =0;
        amplify(nbClassification);
        Prior.convertAbsoluteToPercentual2(nbClassification);
//        System.out.println("patentid "+patent.patentID+" before "+nbClassification);
//        System.out.println("patentid "+patent.patentID+" after "+nbClassification);
        for(String field : nbClassification.keySet()){
            i++;
            if(i==1)
                field1 = nbClassification.get(field);
            if(i==2){
                field2Name = field;
                field2 = nbClassification.get(field);
                break;
            }
            
        }

        if(field1Name.contentEquals("clinical and experimental medicine") || field1Name.contentEquals("chemistry")){
//            System.out.println("patentid "+patent.patentID+" field1-field2 "+(field1-field2));
            if(field1-field2<confidence_cut1){
//                System.out.println("retornou collapsed ");
                TreeMap<String,Double> SVMClassification = svm.classifyPatent(patent);
                if(SVMClassification != null){
                    if(SVMClassification.firstEntry().getValue() == 1.0){
                        HashMap<String, Double> probabilities = new HashMap<String, Double>();
                        probabilities.put("biomedical research",1.0);
                        probabilities.put(field1Name,field1);
                        probabilities.put(field2Name,field2);
                        Prior.convertAbsoluteToPercentual2(probabilities);
                        TreeMap<String,Double>probabilities2=new TreeMap<String,Double>(new Comparator<String>() {
                        @Override
                        public int compare(String s1, String s2) {
                                return probabilities.get(s2).compareTo(probabilities.get(s1));
                            }
                        });
                        probabilities2.putAll(probabilities);
                        return probabilities2;
                    }
                }else{
                    
                    System.out.println("SVM Classification should be valid, but is null "+field1Name+"\t"+patent.patentAbstract);
                    return nbClassification;
                }
            }
            else{
//                System.out.println("retornou weighted ");
                return nbClassification;
            }
        }else{
            if(field1Name.contentEquals("agriculture & environment") || field1Name.contentEquals("biology (organismic & supraorganismic level)")){
                if(field1-field2<confidence_cut2){
                    return bertLikelihood.classifyPatent(patent);
                }else{
                    return nbClassification;
                }
            }
        }
        return nbClassification;
    }
    
    public void amplify(Map<String,Double> map){
        double total=0;
        for(String field:map.keySet()){
//            System.out.println(field+" amplification: "+amplification+" before: "+map.get(field)+" after: "+Math.pow(map.get(field), this.amplification));
            map.put(field,Math.pow(map.get(field), 0.3));
        }
    }
    
    public void trainPatents(LinkedList<Patent> patents){

    }
    
    public void printNGramas(int n_grams){
            for(String field: fieldsNgrams.keySet()){
              System.out.println(field);
              HashMap<String,Double> a = fieldsNgrams.get(field).getNgram(n_grams);
              for(String word:a.keySet()){
                    System.out.println(field+"\t"+word+"\t"+a.get(word));
                    
              }
        }
    }
    
    private void updateNGram(int gram, String field, String[]tokens){
        if(!fieldsNgrams.containsKey(field)){
             fieldsNgrams.put(field, new Ngram());
         }
        
         for(int i=0;i<tokens.length-gram-1;i++){
             LinkedList<String> grams = new LinkedList<String>();
             for(int j=i;j<(i+gram);j++){
                 grams.add(tokens[j]);
             }

             boolean non_stop_words = true;
             for(String word:grams){
                 if(StopWords.isStopword(word)){
                     non_stop_words=false;
                 }
                 if(word.length()<=2){
                     non_stop_words=false;
                 }
             }
             
             HashMap<String,Double> ngram = fieldsNgrams.get(field).getNgram(gram);
             
             //System.out.println(non_stop_words);
             if(non_stop_words){
                 String added_string="";
                 for(String word: grams){
                     added_string+=word+" ";
                 }
                 String added_string2 =added_string;
                 added_string = added_string.substring(0,added_string.length()-1);
                 if(ngram.containsKey(added_string)){
                     ngram.put(added_string,ngram.get(added_string)+1.0);
                 }else{
                     ngram.put(added_string,1.0);
                 }
             }
         }
    }
    
    private static String removePontuationCharacters(String base){
        base = base.replace(",", "").replace("\""," ").replace("/"," ").replace("'", "").replace(";","").replace("?","").replace("-"," ").replace("("," ").replace(")"," ").replace(".","");
        return base;
    }
}