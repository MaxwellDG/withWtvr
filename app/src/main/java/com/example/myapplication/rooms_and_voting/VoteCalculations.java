package com.example.myapplication.rooms_and_voting;

import java.util.ArrayList;

class VoteCalculations {

    private ArrayList<String> allTheSubmissions = new ArrayList<>();
    private String finalAnswer;

    VoteCalculations() {
    }

    String analyzeDataForFinalAnswer(){
        if (allTheSubmissions == null || allTheSubmissions.size() == 0) {
            return "There was an error.";
        }

        int highestFrequency = 0;
        ArrayList<String> mostPopularAnswers = new ArrayList<>();
        for(int i = 0; i < allTheSubmissions.size() - 1; i++){
            int counter = 0;
            for(String submission : allTheSubmissions) {
                if(allTheSubmissions.get(i).equals(submission)){
                    counter++;
                    if(counter == highestFrequency && !mostPopularAnswers.contains(allTheSubmissions.get(i))){
                        mostPopularAnswers.add(allTheSubmissions.get(i));
                        highestFrequency = counter;
                    } else if (counter > highestFrequency){
                        mostPopularAnswers.clear();
                        mostPopularAnswers.add(allTheSubmissions.get(i));
                        highestFrequency = counter;
                    }
                }
            }
        }

        //how to set finalAnswer if host //
        finalAnswer = mostPopularAnswers.toString();

        StringBuilder finalBigWord = new StringBuilder();
        for (String word : mostPopularAnswers){
            word = word + "###";
            finalBigWord.append(word);
        }
        return finalBigWord.toString();
    }

    void addSubmissions(ArrayList<String> submission){
        allTheSubmissions.addAll(submission);
    }

    // how to set finalAnswer if a joiner //
    void setFinalAnswer(String[] string){
        this.finalAnswer = java.util.Arrays.toString(string);
    }

    String getFinalAnswer(){
        return finalAnswer;
    }

}
