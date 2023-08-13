/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import NPRClassifier.NPRPatentClassifier;
import java.util.HashMap;
import java.util.LinkedList;
import static main.Main.patstatPatents;
import patent.PATSTATPatents;
import patent.Patent;

/**
 *
 * @author Lucas
 */
public class Metrics {
    public static void main(String[] args) {
        PATSTATPatents pat_r = new PATSTATPatents("C:\\Users\\Carlos\\Documents\\usp\\patentClassifier\\Patent_Classifier_Data\\PATSTAT Patent train data",null);
        LinkedList<Patent> pats = pat_r.readPATSTATPatents();
        HashMap<String,Integer> counting = new HashMap<String,Integer>();
        NPRPatentClassifier nprClassifier = new NPRPatentClassifier("C:\\Users\\Carlos\\Desktop\\Articles-Glanzel-WoS.csv");
//        int num_of_nprs = 0;
//        for(Patent pat : pats){
//            pat.application_authority = pat.application_authority.split(";")[0];
//            if(pat.application_authority.length()<10){
//                if(!counting.containsKey(pat.application_authority)){
//                    counting.put(pat.application_authority, 0);
//                }
//                counting.put(pat.application_authority, counting.get(pat.application_authority)+1);
//            }
//            num_of_nprs+= pat.references.size();
//        }
//        System.out.println("num_of_nprs "+num_of_nprs);
//        for(String auth : counting.keySet()){
//            System.out.println(auth+"\t"+counting.get(auth));
//        }
       
        nprClassifier.readGlanzelJournalClassification();
        
        System.out.println("\nClassifing patents by Glanzel using nprs");
        nprClassifier.classifyPatentsUsingNPRs(pats,true);

    }
}
