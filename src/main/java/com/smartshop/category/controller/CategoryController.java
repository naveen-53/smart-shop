package com.smartshop.category.controller;

import com.smartshop.category.dto.*;
import com.smartshop.category.service.CategoryService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService service;

    public CategoryController(CategoryService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryResponse create(
            @Valid @RequestBody CategoryRequest request) {
        return service.create(request);
    }

    @GetMapping
    public PageResponse<CategoryResponse> list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {

        return service.list(search, active, page, size, sortBy, direction);
    }

    @GetMapping("/{id}")
    public CategoryResponse getById(@PathVariable UUID id) {
        return service.getById(id);
    }

    @PutMapping("/{id}")
    public CategoryResponse update(
            @PathVariable UUID id,
            @Valid @RequestBody CategoryRequest request) {
        return service.update(id, request);
    }

    @PatchMapping("/{id}/status")
    public CategoryResponse updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody CategoryStatusRequest request) {
        return service.updateStatus(id, request);
    }
}