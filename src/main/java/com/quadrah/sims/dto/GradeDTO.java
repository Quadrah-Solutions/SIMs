package com.quadrah.sims.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class GradeDTO {
    private Long id;
    private String gradeName;
    private String description;
    private Integer sortOrder;

    public GradeDTO() {}

    public GradeDTO(Long id, String gradeName, String description, Integer sortOrder) {
        this.id = id;
        this.gradeName = gradeName;
        this.description = description;
        this.sortOrder = sortOrder;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getGradeName() { return gradeName; }
    public void setGradeName(String gradeName) { this.gradeName = gradeName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
}