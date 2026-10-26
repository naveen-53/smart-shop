package com.smartshop.category.service;

import com.smartshop.category.dto.*;
import com.smartshop.category.model.Category;
import com.smartshop.category.repository.CategoryRepository;
import com.smartshop.common.exception.DuplicateResourceException;
import com.smartshop.common.exception.ResourceNotFoundException;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class CategoryService {

    private final CategoryRepository repository;

    public CategoryService(CategoryRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        String name = normalizeName(request.name());

        if (repository.existsByName(name)) {
            throw new DuplicateResourceException(
                    "Category name already exists: " + name);
        }

        try {
            return toResponse(repository.insert(
                    name, normalizeDescription(request.description())));
        } catch (DuplicateKeyException ex) {
            // Protects against two concurrent requests using the same name.
            throw new DuplicateResourceException(
                    "Category name already exists: " + name);
        }
    }

    @Transactional(readOnly = true)
    public CategoryResponse getById(UUID id) {
        return toResponse(findOrThrow(id));
    }

    @Transactional
    public CategoryResponse update(UUID id, CategoryRequest request) {
        findOrThrow(id);

        String name = normalizeName(request.name());

        if (repository.existsByNameExcludingId(name, id)) {
            throw new DuplicateResourceException(
                    "Category name already exists: " + name);
        }

        try {
            return toResponse(repository.update(
                    id, name, normalizeDescription(request.description())));
        } catch (DuplicateKeyException ex) {
            throw new DuplicateResourceException(
                    "Category name already exists: " + name);
        }
    }

    @Transactional
    public CategoryResponse updateStatus(
            UUID id, CategoryStatusRequest request) {

        findOrThrow(id);
        return toResponse(repository.updateStatus(id, request.active()));
    }

    @Transactional(readOnly = true)
    public PageResponse<CategoryResponse> list(
            String search,
            Boolean active,
            int page,
            int size,
            String sortBy,
            String direction) {

        if (page < 0) {
            throw new IllegalArgumentException("Page cannot be negative");
        }
        if (size < 1 || size > 100) {
            throw new IllegalArgumentException("Size must be between 1 and 100");
        }
        if (!"name".equals(sortBy)
                && !"createdAt".equals(sortBy)
                && !"updatedAt".equals(sortBy)) {
            throw new IllegalArgumentException("Invalid sortBy value");
        }
        if (!"asc".equalsIgnoreCase(direction)
                && !"desc".equalsIgnoreCase(direction)) {
            throw new IllegalArgumentException("Direction must be asc or desc");
        }

        String normalizedSearch =
                search == null || search.isBlank() ? null : search.trim();

        long offset = (long) page * size;
        if (offset > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Page number is too large");
        }

        var categories = repository.findAll(
                normalizedSearch, active, sortBy, direction, size, (int) offset);

        long total = repository.count(normalizedSearch, active);

        return new PageResponse<>(
                categories.stream().map(this::toResponse).toList(),
                page,
                size,
                total,
                (int) ((total + size - 1) / size)
        );
    }

    private Category findOrThrow(UUID id) {
        return repository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Category not found: " + id));
    }

    private String normalizeName(String name) {
        String normalized = name == null ? "" : name.trim();

        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("Category name is required");
        }
        if (normalized.length() > 100) {
            throw new IllegalArgumentException(
                    "Category name cannot exceed 100 characters");
        }

        return normalized;
    }

    private String normalizeDescription(String description) {
        if (description == null || description.isBlank()) {
            return null;
        }

        String normalized = description.trim();

        if (normalized.length() > 500) {
            throw new IllegalArgumentException(
                    "Description cannot exceed 500 characters");
        }

        return normalized;
    }

    private CategoryResponse toResponse(Category category) {
        return new CategoryResponse(
                category.id(),
                category.name(),
                category.description(),
                category.active(),
                category.createdAt(),
                category.updatedAt()
        );
    }
}