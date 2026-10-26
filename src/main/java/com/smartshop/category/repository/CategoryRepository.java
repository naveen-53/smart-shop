package com.smartshop.category.repository;

import com.smartshop.category.model.Category;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class CategoryRepository {

    private static final String SELECT_COLUMNS = """
            id, name, description, is_active, created_at, updated_at
            """;

    private final JdbcTemplate jdbc;

    public CategoryRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<Category> rowMapper = (rs, rowNum) ->
            new Category(
                    rs.getObject("id", UUID.class),
                    rs.getString("name"),
                    rs.getString("description"),
                    rs.getBoolean("is_active"),
                    rs.getObject(
                            "created_at",
                            java.time.OffsetDateTime.class
                    ),
                    rs.getObject(
                            "updated_at",
                            java.time.OffsetDateTime.class
                    )
            );

    /**
     * Insert a new category.
     */
    public Category insert(String name, String description) {

        String sql = """
                INSERT INTO smartshop.categories (name, description)
                VALUES (?, ?)
                RETURNING %s
                """.formatted(SELECT_COLUMNS);

        return jdbc.queryForObject(
                sql,
                rowMapper,
                name,
                description
        );
    }

    /**
     * Find a category by its ID.
     */
    public Optional<Category> findById(UUID id) {

        String sql = """
                SELECT %s
                FROM smartshop.categories
                WHERE id = ?
                """.formatted(SELECT_COLUMNS);

        try {
            return Optional.ofNullable(
                    jdbc.queryForObject(sql, rowMapper, id)
            );
        } catch (EmptyResultDataAccessException ex) {
            return Optional.empty();
        }
    }

    /**
     * Check whether a category name already exists.
     * Comparison ignores case and leading/trailing spaces.
     */
    public boolean existsByName(String name) {

        String sql = """
                SELECT EXISTS (
                    SELECT 1
                    FROM smartshop.categories
                    WHERE LOWER(BTRIM(name)) = LOWER(BTRIM(?))
                )
                """;

        Boolean result = jdbc.queryForObject(
                sql,
                Boolean.class,
                name
        );

        return Boolean.TRUE.equals(result);
    }

    /**
     * Check whether a category name exists for another category.
     * Used while updating a category.
     */
    public boolean existsByNameExcludingId(String name, UUID id) {

        String sql = """
                SELECT EXISTS (
                    SELECT 1
                    FROM smartshop.categories
                    WHERE LOWER(BTRIM(name)) = LOWER(BTRIM(?))
                      AND id <> ?
                )
                """;

        Boolean result = jdbc.queryForObject(
                sql,
                Boolean.class,
                name,
                id
        );

        return Boolean.TRUE.equals(result);
    }

    /**
     * Update category name and description.
     */
    public Category update(
            UUID id,
            String name,
            String description) {

        String sql = """
                UPDATE smartshop.categories
                SET name = ?,
                    description = ?,
                    updated_at = CURRENT_TIMESTAMP
                WHERE id = ?
                RETURNING %s
                """.formatted(SELECT_COLUMNS);

        return jdbc.queryForObject(
                sql,
                rowMapper,
                name,
                description,
                id
        );
    }

    /**
     * Update the active/inactive status of a category.
     */
    public Category updateStatus(UUID id, boolean active) {

        String sql = """
                UPDATE smartshop.categories
                SET is_active = ?,
                    updated_at = CURRENT_TIMESTAMP
                WHERE id = ?
                RETURNING %s
                """.formatted(SELECT_COLUMNS);

        return jdbc.queryForObject(
                sql,
                rowMapper,
                active,
                id
        );
    }

    /**
     * Retrieve categories with optional search and active-status filters,
     * sorting, and pagination.
     */
    public List<Category> findAll(
            String search,
            Boolean active,
            String sortBy,
            String direction,
            int size,
            int offset) {

        StringBuilder sql = new StringBuilder("""
                SELECT %s
                FROM smartshop.categories
                WHERE 1 = 1
                """.formatted(SELECT_COLUMNS));

        List<Object> params = new ArrayList<>();

        // Optional category-name search.
        if (search != null && !search.isBlank()) {
            sql.append(" AND name ILIKE ?");
            params.add("%" + search.trim() + "%");
        }

        // Optional active/inactive filter.
        if (active != null) {
            sql.append(" AND is_active = ?");
            params.add(active);
        }

        // Whitelist sortable columns to prevent SQL injection.
        String orderColumn = switch (
                sortBy == null ? "name" : sortBy) {
            case "createdAt" -> "created_at";
            case "updatedAt" -> "updated_at";
            default -> "name";
        };

        String orderDirection =
                "desc".equalsIgnoreCase(direction) ? "DESC" : "ASC";

        sql.append(" ORDER BY ")
                .append(orderColumn)
                .append(" ")
                .append(orderDirection)
                .append(", id ASC")
                .append(" LIMIT ? OFFSET ?");

        params.add(size);
        params.add(offset);

        return jdbc.query(
                sql.toString(),
                rowMapper,
                params.toArray()
        );
    }

    /**
     * Count categories using the same optional filters as findAll().
     */
    public long count(String search, Boolean active) {

        StringBuilder sql = new StringBuilder("""
                SELECT COUNT(*)
                FROM smartshop.categories
                WHERE 1 = 1
                """);

        List<Object> params = new ArrayList<>();

        if (search != null && !search.isBlank()) {
            sql.append(" AND name ILIKE ?");
            params.add("%" + search.trim() + "%");
        }

        if (active != null) {
            sql.append(" AND is_active = ?");
            params.add(active);
        }

        Long count = jdbc.queryForObject(
                sql.toString(),
                Long.class,
                params.toArray()
        );

        return count == null ? 0L : count;
    }
}