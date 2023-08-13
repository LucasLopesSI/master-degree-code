package GeneticAlgorithm;

import BayesianClassifier.Likelihood;
import BayesianClassifier.NaiveBayesClassifier;
import BayesianClassifier.Prior;
import static GeneticAlgorithm.GeneticHyperparametrization.training;
import Metrics.Metrics;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import main.Main;
import static main.Main.classifyListOfPatents;
import static main.Main.patstatPatents;
import patent.Patent;

public class GeneticHyperparametrization {

    Population population = new Population();
    Individual fittest;
    Individual secondFittest;
    int generationCount = 0;
    static List<Patent> training = new LinkedList<Patent>();
    static List<Patent> validation = new LinkedList<Patent>();
    static List<Patent> test = new LinkedList<Patent>();

    public static void main(String[] args) {
        Main m = new Main();
        Random rn = new Random();
        
//        System.out.println(patstatPatents.size());
        HashMap<String,LinkedList<Patent>> split1 = Patent.train_test_split(new LinkedList<Patent>(patstatPatents),0.9);
        List<Patent> trainingPatents = split1.get("train");
        training = trainingPatents;
        HashMap<String,LinkedList<Patent>> testAndValidation = Patent.train_test_split(split1.get("test"),0.5);
        validation = testAndValidation.get("train");
        test = testAndValidation.get("test");
        
        GeneticHyperparametrization demo = new GeneticHyperparametrization();
        System.out.println(""+"ipcDigits"+"\t"+"smoothed"+"\t"+"countCitationDuplicates"+"\t"+"cut"+"\t"+"classifier");
        int popSize = 50;
        
        //Initialize population
        demo.population.initializePopulation(popSize);
        //Calculate fitness of each individual
        demo.population.calculateFitness();

        System.out.println("Generation: " + demo.generationCount + " Fittest: " + demo.population.fittest);
        
        int numberOfGenerations = 1;
        HashMap<String,Object[]> parameters = demo.population.individuals[0].getParameters();
        HashMap<String,HashMap<String,Integer>> characteristicsInPopulationOverGenerations = new HashMap<String,HashMap<String,Integer>>();
        
        for(int i =0; i < numberOfGenerations+1; i++){
            characteristicsInPopulationOverGenerations.put(""+i,new HashMap<String,Integer>());
            
            for(String parameter : parameters.keySet()){
                
                for(Object obj : parameters.get(parameter)){
                    String characteristic = obj.toString();
                    characteristicsInPopulationOverGenerations.get(""+i).put(parameter+"-"+characteristic, 0);
                }
            }
        }
        
        for(Individual ind : demo.population.individuals){
            int cont =0;
            for(Object obj : ind.genes){
                String characteristic = obj.toString();
                characteristicsInPopulationOverGenerations.get(""+demo.generationCount).put(demo.population.individuals[0].indexGeneDictionary.get(""+cont)+"-"+characteristic,1+characteristicsInPopulationOverGenerations.get(""+demo.generationCount).get(demo.population.individuals[0].indexGeneDictionary.get(""+cont)+"-"+characteristic));
                cont++;
            }
        }
        
        printcharacteristicsInPopulationOverGenerations(characteristicsInPopulationOverGenerations);
        //While population gets an individual with maximum fitness
        while (demo.generationCount < numberOfGenerations) {
            ++demo.generationCount;
            demo.computeBestIndividuals();
            
            for(int j=0;j<popSize;j++){
                //Do selection
                int indexes_selected[]= demo.selection();
                //Do crossover
                System.out.println(demo.population.individuals.length);
                demo.population.individuals[rn.nextInt(demo.population.individuals.length)] = demo.reproduction(indexes_selected[0],indexes_selected[1]);
                //Do mutation under a random probability
                if (rn.nextDouble() < 0.1) {
                    demo.mutation();
                }
            }
            
//            for(int j=0;j<popSize;j++){
//                //Do selection
//                int indexes_selected[]= demo.selection();
//                //Do crossover
//                demo.crossover(indexes_selected[0],indexes_selected[1]);
//            }
            
            //Add fittest offspring to population
//            demo.addFittestOffspring();

            //Calculate new fitness value
            demo.population.calculateFitness();

            System.out.println("Generation: " + demo.generationCount + " Fittest Validation: " + demo.population.fittest);
            
            for(Individual ind : demo.population.individuals){
                int cont =0;
                for(Object obj : ind.genes){
                    String characteristic = obj.toString();
                    characteristicsInPopulationOverGenerations.get(""+demo.generationCount).put(demo.population.individuals[0].indexGeneDictionary.get(""+cont)+"-"+characteristic,1+characteristicsInPopulationOverGenerations.get(""+demo.generationCount).get(demo.population.individuals[0].indexGeneDictionary.get(""+cont)+"-"+characteristic));
                    cont++;
                }
            }
            printcharacteristicsInPopulationOverGenerations(characteristicsInPopulationOverGenerations);
        }
        System.out.println("\nTest Evaluation ");
        demo.population.getFittest().calcFitness(test);
        
        System.out.println("\nSolution found in generation " + demo.generationCount);
        System.out.println("Fitness: "+demo.population.getFittest().fitness);
        System.out.print("Genes: ");
        for (int i = 0; i < 6; i++) {
            System.out.print(demo.population.getFittest().genes[i]);
        }
        

        System.out.println("");
    }
    
    public static void printcharacteristicsInPopulationOverGenerations(HashMap<String,HashMap<String,Integer>> characteristicsInPopulationOverGenerations){
        for(String generation : characteristicsInPopulationOverGenerations.keySet()){
            System.out.print(" "+"\t");
            for(String characteristc: characteristicsInPopulationOverGenerations.get(generation).keySet()){
                System.out.print(characteristc +"\t");
            }
            break;
        }
        System.out.println("");
        for(String generation : characteristicsInPopulationOverGenerations.keySet()){
            System.out.print("Generation "+generation +"\t");
            for(String characteristc: characteristicsInPopulationOverGenerations.get(generation).keySet()){
                System.out.print(characteristicsInPopulationOverGenerations.get(generation).get(characteristc) +"\t");
            }
            System.out.println("");
        }
    }
    void computeBestIndividuals(){
        fittest = population.getFittest();
        secondFittest =  population.getSecondFittest();
    }
    
    //Selection
    int[] selection() {
        //Select the most fittest individual
        int selected_one = population.getRandomIndividualDistributedToFitnessProbability();
        
        //Select the second most fittest individual
        int selected_two = population.getRandomIndividualDistributedToFitnessProbability();
        int selecteds [] = {selected_one,selected_two};
        return selecteds;
    }

    //Crossover
    void crossover(int selected_one_index, int selected_two_index) {
        Random rn = new Random();
        //Select a random crossover point
        int crossOverPoint = rn.nextInt(population.individuals[0].geneLength);
        Individual ind1 = population.individuals[selected_one_index];
        Individual ind2 = population.individuals[selected_two_index];
        
        //Swap values among parents
        for (int i = 0; i < crossOverPoint; i++) {
            Object temp = ind1.genes[i];
            ind1.genes[i] = ind2.genes[i];
            ind2.genes[i] = temp;
        }
    }
    
    //Crossover
    Individual reproduction(int selected_one_index, int selected_two_index) {
        Random rn = new Random();
        //Select a random crossover point
        int crossOverPoint = rn.nextInt(population.individuals[0].geneLength);
        Individual ind1 = population.individuals[selected_one_index];
        Individual ind2 = population.individuals[selected_two_index];
        
        if(Main.log){
            System.out.println("selected ind 1");
            for(Object gene : ind1.genes){
                System.out.print(gene.toString()+" ");
            }
            System.out.println("");
            System.out.println("selected ind 2");
            for(Object gene : ind2.genes){
                System.out.print(gene.toString()+" ");
            }
            System.out.println("");
            System.out.println("croosver point" + crossOverPoint);
        }
        
        Individual new_ind = new Individual();
        //Swap values among parents
        for (int i = 0; i < crossOverPoint; i++) {
            new_ind.genes[i] = ind1.genes[i];
        }
        for (int i = crossOverPoint; i < ind2.genes.length; i++) {
            new_ind.genes[i] = ind2.genes[i];
        }
        
        if(Main.log){
            System.out.println("new_ind");
            for(Object gene : new_ind.genes){
                System.out.print(gene.toString()+" ");
            }
            System.out.println("");
        }
        return new_ind;
    }

    //Mutation
    void mutation() {
        Random rn = new Random();
        Individual selected_one = population.individuals[rn.nextInt(population.individuals.length)];
        Individual selected_two = population.individuals[rn.nextInt(population.individuals.length)];
        //Select a random mutation point
        int mutationPoint = rn.nextInt(population.individuals[0].geneLength);
        HashMap<String,Object[]> parameters = population.individuals[0].getParameters();
        //Set genes randomly for each individual
        int i =0;
        for(String parameter : parameters.keySet()){
            if( i == mutationPoint){
                selected_one.genes[mutationPoint] = parameters.get(parameter)[rn.nextInt(parameters.get(parameter).length)];
            }
            i++;
        }
        mutationPoint = rn.nextInt(population.individuals[0].geneLength);
        
        i =0;
        for(String parameter : parameters.keySet()){
            if( i == mutationPoint){
                selected_two.genes[mutationPoint] = parameters.get(parameter)[rn.nextInt(parameters.get(parameter).length)];
            }
            i++;
        }
    }

    //Get fittest offspring
    Individual getFittestOffspring() {
        if (fittest.fitness > secondFittest.fitness) {
            return fittest;
        }
        return secondFittest;
    }


    //Replace least fittest individual from most fittest offspring
    void addFittestOffspring() {

        //Update fitness values of offspring
        fittest.calcFitness(validation);
        secondFittest.calcFitness(validation);

        //Get index of least fit individual
        int leastFittestIndex = population.getLeastFittestIndex();

        //Replace least fittest individual from most fittest offspring
        population.individuals[leastFittestIndex] = getFittestOffspring();
    }

}


//Individual class
class Individual {

    double fitness = 0;
    Object[] genes = new Object[6];
    HashMap<String,Integer> geneIndexDictionary = new HashMap<String,Integer>();
    HashMap<String,String> indexGeneDictionary = new HashMap<String,String>();
    int geneLength = 6;
    
    public HashMap<String,Object[]> getParameters(){
        HashMap<String,Object[]> parameters = new HashMap<String,Object[]>();
        Object ipcDigits [] = {1,3,4};
        parameters.put("ipcDigits", ipcDigits);
        
        Object smoothed [] = {true,false};
        parameters.put("smoothed", smoothed);
        
        Object countCitationDuplicates [] = {true,false};
        parameters.put("countCitationDuplicates", countCitationDuplicates);
        
        Object cut [] = {1.0,1.5,2.0,2.1,2.2,2.3,2.4,2.5,2.6,2.7,2.8,2.9,3.0,3.5,4.0};
        parameters.put("cut", cut);
        
        Object amplification [] = {1.0,1.3,1.5,1.7,2.0,2.5,3.0};
        parameters.put("amplification", amplification);
        
        Object classifier [] = {"nb"};
//        Object classifier [] = {"prior"};
        parameters.put("classifier", classifier);
        
        return parameters;
    }

    public Individual() {
        Random rn = new Random();
        
        HashMap<String,Object[]> parameters = getParameters();
        
        //Set genes randomly for each individual
        int i =0;
        for(String parameter : parameters.keySet()){
//            System.out.println(parameter+" "+i);
            genes[i] = parameters.get(parameter)[rn.nextInt(parameters.get(parameter).length)];
            i++;
        }

        fitness = 0;
    }

    //Calculate fitness
    public void calcFitness(List<Patent> evaluation_set) {
        fitness = 0;
        
        geneIndexDictionary.put("ipcDigits", 5);
        geneIndexDictionary.put("smoothed", 3);
        geneIndexDictionary.put("countCitationDuplicates", 2);
        geneIndexDictionary.put("cut", 1);
        geneIndexDictionary.put("classifier", 4);
        geneIndexDictionary.put("amplification", 0);
        
        for(String parameter : geneIndexDictionary.keySet()){
            indexGeneDictionary.put(""+geneIndexDictionary.get(parameter), parameter);
        }
        
        int ipcDigits = (int) genes[geneIndexDictionary.get("ipcDigits")];
        boolean smoothed = (boolean) genes[geneIndexDictionary.get("smoothed")];
        boolean countCitationDuplicates = (boolean) genes[geneIndexDictionary.get("countCitationDuplicates")];
        double cut = (double) genes[geneIndexDictionary.get("cut")];
        double amplification = (double) genes[geneIndexDictionary.get("amplification")];
        String classifier = (String) genes[geneIndexDictionary.get("classifier")];
        
//        HashMap<String,LinkedList<Patent>> train_test = Patent.train_test_split(Main.patstatPatents, 0.80);
        
        System.out.println(""+ipcDigits+"\t"+smoothed+"\t"+countCitationDuplicates+"\t"+cut+"\t"+classifier+"\t"+amplification);
        LinkedList<Patent> tr = new LinkedList<Patent>(training);
        Prior prior = new Prior(tr,ipcDigits,smoothed);
        Likelihood likelihood = new Likelihood(countCitationDuplicates,cut,amplification);
        likelihood.trainPatents(tr);
        
        LinkedList<Patent> evaluation = new LinkedList<Patent>(evaluation_set);
        
        NaiveBayesClassifier nb = new NaiveBayesClassifier(likelihood, prior,classifier);
        HashMap<String,Map<String,Double>> predictions = Main.classifyListOfPatentsByChunks(evaluation,nb,4);
        fitness = Metrics.getAccuracy(evaluation, predictions,1);
        System.out.println("fitness "+fitness);
        fitness = Math.pow(fitness, 3);
        
    }

}

//Population class
class Population {

    int popSize;
    Individual[] individuals;
    double fittest = 0;

    //Initialize population
    public void initializePopulation(int popSize) {
        this.popSize = popSize;
        individuals = new Individual[popSize];
        for (int i = 0; i < individuals.length; i++) {
            individuals[i] = new Individual();
        }
    }

    //Get the fittest individual
    public Individual getFittest() {
        double maxFit = Integer.MIN_VALUE;
        int maxFitIndex = 0;
        for (int i = 0; i < individuals.length; i++) {
            if (maxFit <= individuals[i].fitness) {
                maxFit = individuals[i].fitness;
                maxFitIndex = i;
            }
        }
        fittest = individuals[maxFitIndex].fitness;
        return individuals[maxFitIndex];
    }
    
    //Get the fittest individual
    public int getRandomIndividualDistributedToFitnessProbability() {
        Random rn = new Random();
        int selectedIndividualIndex = 0;
        double [] probabilities = new double[individuals.length];
        
        double sum =0;
        for (int i = 0; i < individuals.length; i++) {
            sum += individuals[i].fitness;
        }
        double acumulated = 0;
        for (int i = 0; i < individuals.length; i++) {
            probabilities[i] = (individuals[i].fitness/sum) + acumulated;
            acumulated += (individuals[i].fitness/sum);
        }
        double randomNumber = rn.nextDouble();
        for(int i = 0 ; i<probabilities.length;i++){
            if(randomNumber < probabilities[i]){
                selectedIndividualIndex = i;
                break;
            }
        }
        
        return selectedIndividualIndex;
    }

    //Get the second most fittest individual
    public Individual getSecondFittest() {
        int maxFit1 = 0;
        int maxFit2 = 0;
        for (int i = 0; i < individuals.length; i++) {
            if (individuals[i].fitness > individuals[maxFit1].fitness) {
                maxFit2 = maxFit1;
                maxFit1 = i;
            } else if (individuals[i].fitness > individuals[maxFit2].fitness) {
                maxFit2 = i;
            }
        }
        return individuals[maxFit2];
    }

    //Get index of least fittest individual
    public int getLeastFittestIndex() {
        double minFitVal = Integer.MAX_VALUE;
        int minFitIndex = 0;
        for (int i = 0; i < individuals.length; i++) {
            if (minFitVal >= individuals[i].fitness) {
                minFitVal = individuals[i].fitness;
                minFitIndex = i;
            }
        }
        return minFitIndex;
    }

    //Calculate fitness of each individual
    public void calculateFitness() {

        for (int i = 0; i < individuals.length; i++) {
            System.out.println("Calculando Fitness");
            individuals[i].calcFitness(GeneticHyperparametrization.validation);
        }
        getFittest();
    }

}