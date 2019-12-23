package com.example.myapplication.rooms_and_voting;

import android.util.Log;

import java.util.ArrayList;

class VoteCalculations {

    private static final String TAG = "TAG";

    private ArrayList<String> allTheSubmissions = new ArrayList<>();
    private String finalAnswer;

    VoteCalculations() {
    }

    String analyzeDataForFinalAnswer(){
        Log.d(TAG, "getTheFINALANSWER: SHOW ME ALL SUBMISSIONS " + allTheSubmissions);

        if (allTheSubmissions == null || allTheSubmissions.size() == 0) {
            return "There was an error.";
        }

        int highestFrequency = 0;
        ArrayList<String> mostPopularAnswers = new ArrayList<>();
        for(int i = 0; i < allTheSubmissions.size() - 1; i++){
            Log.d(TAG, "analyzeDataForFinalAnswer: Checking " + allTheSubmissions.get(i));
            int counter = 0;
            for(String submission : allTheSubmissions) {
                Log.d(TAG, "analyzeDataForFinalAnswer: against " + submission);
                if(allTheSubmissions.get(i).equals(submission)){
                    counter++;
                    Log.d(TAG, "analyzeDataForFinalAnswer: HIT. Counter is at " + counter);
                    if(counter == highestFrequency && !mostPopularAnswers.contains(allTheSubmissions.get(i))){
                        Log.d(TAG, "analyzeDataForFinalAnswer: Added " + allTheSubmissions.get(i));
                        mostPopularAnswers.add(allTheSubmissions.get(i));
                        highestFrequency = counter;
                    } else if (counter > highestFrequency){
                        Log.d(TAG, "analyzeDataForFinalAnswer: CLEARED. Then added " + allTheSubmissions.get(i));
                        mostPopularAnswers.clear();
                        mostPopularAnswers.add(allTheSubmissions.get(i));
                        highestFrequency = counter;
                    }
                }
            }
        }
        Log.d(TAG, "analyzeDataForFinalAnswer: mostPopularAnswers are " + mostPopularAnswers.toString());
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
        Log.d(TAG, "setFinalAnswer: SETTING" + java.util.Arrays.toString(string));
        this.finalAnswer = java.util.Arrays.toString(string);
    }

    public String getFinalAnswer(){
        return finalAnswer;
    }

}
