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
public class HybridLikelihood2 extends Classifier{
    HashMap<String,Ngram> fieldsNgrams = new HashMap<String,Ngram>();
    double amplification_nb;
    double amplification_bert;
    Classifier nb;
    BERTLikelihood bert;

    public HybridLikelihood2(Classifier nb, BERTLikelihood bert, double amplification_nb, double amplification_bert) {
        this.amplification_nb = amplification_nb;
        this.amplification_bert = amplification_bert;
        this.nb = nb;
        this.bert = bert;
    }
    
    private TreeMap<String,Double> getF1NB(double amplification){
        TreeMap<String,Double> f1Nb = new TreeMap<String,Double>();
        f1Nb.put("chemistry",Math.pow(1-0.837308868501529,amplification));
        f1Nb.put("mathematics",Math.pow(1-0.1,amplification));
        f1Nb.put("physics",Math.pow(1-0.541040462427745,amplification));
        f1Nb.put("biology (organismic & supraorganismic level)",Math.pow(1-0.726573426573426,amplification));
        f1Nb.put("clinical and experimental medicine",Math.pow(1-0.578632478632478,amplification));
        f1Nb.put("agriculture & environment",Math.pow(1-0.494791666666666,amplification));
        f1Nb.put("engineering",Math.pow(1-0.944770283479961,amplification));
        f1Nb.put("geociences & space sciences",Math.pow(1-0.4,amplification));
        f1Nb.put("biomedical research",Math.pow(1-0.0188679245283018,amplification));
        return f1Nb;
    }
    
    private TreeMap<String,Double> getF1Bert(double amplification){
        TreeMap<String,Double> f1Bert = new TreeMap<String,Double>();
        f1Bert.put("chemistry",Math.pow(1-0.785447761194029,amplification));
        f1Bert.put("mathematics",Math.pow(1-0.1,amplification));
        f1Bert.put("physics",Math.pow(1-0.658371040723981,amplification));
        f1Bert.put("biology (organismic & supraorganismic level)",Math.pow(1-0.666666666666666,amplification));
        f1Bert.put("clinical and experimental medicine",Math.pow(1-0.721724979658258,amplification));
        f1Bert.put("agriculture & environment",Math.pow(1-0.76056338028169,amplification));
        f1Bert.put("engineering",Math.pow(1-0.87994071146245,amplification));
        f1Bert.put("geociences & space sciences",Math.pow(1-0.451612903225806,amplification));
        f1Bert.put("biomedical research",Math.pow(1-0.208510638297872,amplification));
        return f1Bert;
    }
    
    private TreeMap<String,Double> getAmplification(double amplification){
        TreeMap<String,Double> f1Bert = new TreeMap<String,Double>();
        f1Bert.put("chemistry",amplification);
        f1Bert.put("mathematics",amplification);
        f1Bert.put("physics",amplification);
        f1Bert.put("biology (organismic & supraorganismic level)",amplification);
        f1Bert.put("clinical and experimental medicine",amplification);
        f1Bert.put("agriculture & environment",amplification);
        f1Bert.put("engineering",amplification);
        f1Bert.put("geociences & space sciences",amplification);
        f1Bert.put("biomedical research",amplification);
        return f1Bert;
    }
    
    public TreeMap<String,Double> classifyPatent(Patent patent){
        TreeMap<String,Double> nbClassification = nb.classifyPatent(patent);
        TreeMap<String,Double> bertClassification = bert.classifyPatent(patent);
        
        if(bertClassification == null){
            System.out.println("Warning BERT Classification is null, returning NB Classification");
            return nbClassification;
        }
//        System.out.println("bertClassification "+bertClassification);
        Map<String,Double> weightedNbClassification = NaiveBayesClassifier.multiplyTwoMaps(nbClassification, getF1NB(amplification_nb));
        Map<String,Double> weightedBertClassification = NaiveBayesClassifier.multiplyTwoMaps(bertClassification, getF1Bert(amplification_bert));
        
        Map<String,Double> weightedClassification = NaiveBayesClassifier.multiplyTwoMaps(weightedNbClassification, weightedBertClassification);
//        System.out.println("patent Id "+patent.patentID+" weightedClassification "+weightedClassification);
        Prior.convertAbsoluteToPercentual2(weightedClassification);
        
        TreeMap<String,Double>posteriorOrdered=new TreeMap<String,Double>(new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                return weightedClassification.get(s2).compareTo(weightedClassification.get(s1));
            }
        });
        posteriorOrdered.putAll(weightedClassification);
//        System.out.println("patent Id "+patent.patentID+" weightedClassification "+posteriorOrdered);
        
        return posteriorOrdered;
    }
    
    public void trainPatents(LinkedList<Patent> patents){

    }
    
}