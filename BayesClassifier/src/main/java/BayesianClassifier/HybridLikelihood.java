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
public class HybridLikelihood extends Classifier{
    HashMap<String,Ngram> fieldsNgrams = new HashMap<String,Ngram>();
    double confidence_cut;
    Classifier nbLikelihood;
    Classifier bertLikelihood;

    public HybridLikelihood(Classifier nbLikelihood, Classifier bertLikelihood, double confidence_cut) {
        this.confidence_cut = confidence_cut;
        this.nbLikelihood = nbLikelihood;
        this.bertLikelihood = bertLikelihood;
    }
    
    public TreeMap<String,Double> classifyPatent(Patent patent){
        TreeMap<String,Double> nbClassification = nbLikelihood.classifyPatent(patent);
        double field1 = 0;
        double field2 = 0;
        String fieldName = "";
        int i =0;
        for(String field : nbClassification.keySet()){
            i++;
            if(i==1)
                fieldName = field;
                field1 = nbClassification.get(field);
            if(i==2)
                field2 = nbClassification.get(field);
                break;
            
        }
        TreeMap<String,Double> bertClassification = bertLikelihood.classifyPatent(patent);
        
        if(field1-field2>=confidence_cut){
            return nbClassification;
        }else{
            return bertLikelihood.classifyPatent(patent);
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