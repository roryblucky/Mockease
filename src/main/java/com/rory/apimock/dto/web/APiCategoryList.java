package com.rory.apimock.dto.web;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class APiCategoryList implements Serializable {
    private List<APICategory> apiCategories;
}
