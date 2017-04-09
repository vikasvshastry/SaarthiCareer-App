package com.saarthicareer.saarthicareer.classes;

import java.util.List;

/**
 * Created by vikas on 28-Mar-17.
 */

public class Trainer extends User{
    private String company;
    private String trainingExperience;
    private String areasOfExpertise;

    public String getTrainingExperience() {
        return trainingExperience;
    }

    public void setTrainingExperience(String trainingExperience) {
        this.trainingExperience = trainingExperience;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getAreasOfExpertise() {
        return areasOfExpertise;
    }

    public void setAreasOfExpertise(String areasOfExpertise) {
        this.areasOfExpertise = areasOfExpertise;
    }

}
