package com.tiora.mob.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Available barber for booking")
public class AvailableBarberResponse {
    
    @Schema(description = "Barber ID", example = "1")
    @JsonProperty("barber_id")
    private Long barberId;
    
    @Schema(description = "Barber name", example = "John Doe")
    @JsonProperty("name")
    private String name;
    
    @Schema(description = "Barber profile image URL", example = "https://example.com/barber1.jpg")
    @JsonProperty("image_url")
    private String imageUrl;
    
    @Schema(description = "Years of experience", example = "5")
    @JsonProperty("experience_years")
    private Integer experienceYears;
    
    @Schema(description = "Barber specialties/skills")
    @JsonProperty("specialties")
    private List<String> specialties;
    
    @Schema(description = "Barber ratings (1-5)", example = "4")
    @JsonProperty("ratings")
    private Integer ratings;
    
    @Schema(description = "Whether barber can perform all selected services", example = "true")
    @JsonProperty("can_perform_services")
    private Boolean canPerformServices;
    
    @Schema(description = "Gender clientele the barber serves", example = "BOTH", allowableValues = {"MALE", "FEMALE", "BOTH"})
    @JsonProperty("serves_gender")
    private String servesGender;

    // Default constructor
    public AvailableBarberResponse() {}

    // Constructor with parameters
    public AvailableBarberResponse(Long barberId, String name, String imageUrl,
                                   Integer experienceYears, List<String> specialties,
                                   Integer ratings, Boolean canPerformServices, String servesGender) {
        this.barberId = barberId;
        this.name = name;
        this.imageUrl = imageUrl;
        this.experienceYears = experienceYears;
        this.specialties = specialties;
        this.ratings = ratings;
        this.canPerformServices = canPerformServices;
        this.servesGender = servesGender;
    }

    // Getters and Setters
    public Long getBarberId() {
        return barberId;
    }

    public void setBarberId(Long barberId) {
        this.barberId = barberId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Integer getExperienceYears() {
        return experienceYears;
    }

    public void setExperienceYears(Integer experienceYears) {
        this.experienceYears = experienceYears;
    }

    public List<String> getSpecialties() {
        return specialties;
    }

    public void setSpecialties(List<String> specialties) {
        this.specialties = specialties;
    }

    public Integer getRatings() {
        return ratings;
    }

    public void setRatings(Integer ratings) {
        this.ratings = ratings;
    }

    public Boolean getCanPerformServices() {
        return canPerformServices;
    }

    public void setCanPerformServices(Boolean canPerformServices) {
        this.canPerformServices = canPerformServices;
    }

    public String getServesGender() {
        return servesGender;
    }

    public void setServesGender(String servesGender) {
        this.servesGender = servesGender;
    }
}
