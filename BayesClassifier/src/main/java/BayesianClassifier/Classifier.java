/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package BayesianClassifier;

import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;
import patent.Patent;

/**
 *
 * @author Lucas
 */
public abstract class Classifier {
    public abstract TreeMap<String,Double> classifyPatent(Patent patent);
    public abstract void trainPatents(LinkedList<Patent> patents);
}
