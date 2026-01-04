package jsui.examples;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// JPA imports commented out - add jakarta.persistence dependency to enable
// import javax.persistence.EntityManager;
// import javax.persistence.TypedQuery;
// import javax.persistence.criteria.CriteriaBuilder;
// import javax.persistence.criteria.CriteriaQuery;
// import javax.persistence.criteria.Predicate;
// import javax.persistence.criteria.Root;

import jsui.Context;
import jsui.Data;

/**
 * Example Loader implementations for Data.Collate using JDBC and JPA.
 * 
 * These examples show how to integrate Data.Collate with database queries,
 * including search normalization, filtering, sorting, and pagination.
 */
public final class LoaderExamples {
    private LoaderExamples() {
    }

    // ---------------------------------------------------------------------
    // JDBC Loader Example

    /**
     * Example Loader using JDBC for a User entity.
     * 
     * This demonstrates:
     * - Building dynamic SQL queries with search, filters, and sorting
     * - Using NormalizeForSearch for diacritic-insensitive search
     * - Pagination with LIMIT/OFFSET
     * - Counting total and filtered results
     */
    public static Data.Loader<User> jdbcLoader(Connection connection) {
        return query -> {
            try {
                // Build WHERE clause dynamically
                List<String> whereConditions = new ArrayList<>();
                List<Object> params = new ArrayList<>();
                // int paramIndex = 1;

                // Search across multiple fields
                if (query.Search != null && !query.Search.trim().isEmpty()) {
                    String searchTerm = Data.NormalizeForSearch(query.Search.trim());
                    whereConditions.add("(" +
                            "LOWER(REPLACE(REPLACE(REPLACE(REPLACE(name, 'á', 'a'), 'é', 'e'), 'í', 'i'), 'ó', 'o')) LIKE ? OR "
                            +
                            "LOWER(REPLACE(REPLACE(REPLACE(REPLACE(email, 'á', 'a'), 'é', 'e'), 'í', 'i'), 'ó', 'o')) LIKE ? OR "
                            +
                            "LOWER(REPLACE(REPLACE(REPLACE(REPLACE(city, 'á', 'a'), 'é', 'e'), 'í', 'i'), 'ó', 'o')) LIKE ?"
                            +
                            ")");
                    String searchPattern = "%" + searchTerm.toLowerCase() + "%";
                    params.add(searchPattern);
                    params.add(searchPattern);
                    params.add(searchPattern);
                }

                // Apply filters
                if (query.Filter != null) {
                    for (Data.TField filter : query.Filter) {
                        if (filter == null)
                            continue;

                        if (filter.As == Data.BOOL && filter.Bool) {
                            whereConditions.add("active = ?");
                            params.add(filter.Bool);
                        } else if (filter.As == Data.SELECT && filter.Value != null && !filter.Value.isEmpty()) {
                            whereConditions.add("role = ?");
                            params.add(filter.Value);
                        } else if (filter.As == Data.DATES && filter.Dates != null) {
                            if (filter.Dates.From != null && filter.Dates.From.getTime() > 0) {
                                whereConditions.add("created_at >= ?");
                                params.add(new java.sql.Date(filter.Dates.From.getTime()));
                            }
                            if (filter.Dates.To != null && filter.Dates.To.getTime() > 0) {
                                whereConditions.add("created_at <= ?");
                                params.add(new java.sql.Date(filter.Dates.To.getTime()));
                            }
                        }
                    }
                }

                String whereClause = whereConditions.isEmpty() ? "" : "WHERE " + String.join(" AND ", whereConditions);

                // Build ORDER BY clause
                String orderBy = "ORDER BY created_at DESC";
                if (query.Order != null && !query.Order.trim().isEmpty()) {
                    String[] parts = query.Order.trim().split("\\s+");
                    String field = parts[0].toLowerCase();
                    String direction = parts.length > 1 && "asc".equalsIgnoreCase(parts[1]) ? "ASC" : "DESC";

                    switch (field) {
                        case "name":
                            orderBy = "ORDER BY name " + direction;
                            break;
                        case "email":
                            orderBy = "ORDER BY email " + direction;
                            break;
                        case "city":
                            orderBy = "ORDER BY city " + direction;
                            break;
                        case "createdat":
                        case "created_at":
                            orderBy = "ORDER BY created_at " + direction;
                            break;
                        default:
                            orderBy = "ORDER BY created_at DESC";
                            break;
                    }
                }

                // Count total (before filters)
                String countTotalSql = "SELECT COUNT(*) FROM users";
                int total;
                try (PreparedStatement stmt = connection.prepareStatement(countTotalSql)) {
                    try (ResultSet rs = stmt.executeQuery()) {
                        rs.next();
                        total = rs.getInt(1);
                    }
                }

                // Count filtered
                String countFilteredSql = "SELECT COUNT(*) FROM users " + whereClause;
                int filtered;
                try (PreparedStatement stmt = connection.prepareStatement(countFilteredSql)) {
                    for (int i = 0; i < params.size(); i++) {
                        stmt.setObject(i + 1, params.get(i));
                    }
                    try (ResultSet rs = stmt.executeQuery()) {
                        rs.next();
                        filtered = rs.getInt(1);
                    }
                }

                // Fetch paginated data
                int limit = query.Limit > 0 ? query.Limit : 10;
                int offset = query.Offset > 0 ? query.Offset : 0;
                String dataSql = "SELECT id, name, email, city, role, active, created_at FROM users " +
                        whereClause + " " + orderBy + " LIMIT ? OFFSET ?";

                List<User> data = new ArrayList<>();
                try (PreparedStatement stmt = connection.prepareStatement(dataSql)) {
                    int idx = 1;
                    for (Object param : params) {
                        stmt.setObject(idx++, param);
                    }
                    stmt.setInt(idx++, limit);
                    stmt.setInt(idx++, offset);

                    try (ResultSet rs = stmt.executeQuery()) {
                        while (rs.next()) {
                            User user = new User();
                            user.id = rs.getInt("id");
                            user.name = rs.getString("name");
                            user.email = rs.getString("email");
                            user.city = rs.getString("city");
                            user.role = rs.getString("role");
                            user.active = rs.getBoolean("active");
                            user.createdAt = new java.util.Date(rs.getTimestamp("created_at").getTime());
                            data.add(user);
                        }
                    }
                }

                Data.LoadResult<User> result = new Data.LoadResult<>();
                result.total = total;
                result.filtered = filtered;
                result.data = data;
                return result;
            } catch (SQLException e) {
                throw new RuntimeException("Database query failed", e);
            }
        };
    }

    // ---------------------------------------------------------------------
    // JPA Loader Example (commented out - requires JPA dependency)

    /*
     * /**
     * Example Loader using JPA (Java Persistence API) for a User entity.
     *
     * This demonstrates:
     * - Using Criteria API for type-safe dynamic queries
     * - Search normalization integration
     * - Filtering with Criteria predicates
     * - Sorting and pagination
     * \/
     * public static Data.Loader<User> jpaLoader(EntityManager em) {
     * return query -> {
     * CriteriaBuilder cb = em.getCriteriaBuilder();
     * 
     * // Count total (before filters)
     * CriteriaQuery<Long> countTotalQuery = cb.createQuery(Long.class);
     * Root<User> totalRoot = countTotalQuery.from(User.class);
     * countTotalQuery.select(cb.count(totalRoot));
     * Long total = em.createQuery(countTotalQuery).getSingleResult();
     * 
     * // Build filtered query
     * CriteriaQuery<User> dataQuery = cb.createQuery(User.class);
     * Root<User> root = dataQuery.from(User.class);
     * List<Predicate> predicates = new ArrayList<>();
     * 
     * // Search across multiple fields
     * if (query.Search != null && !query.Search.trim().isEmpty()) {
     * String searchTerm =
     * Data.NormalizeForSearch(query.Search.trim()).toLowerCase();
     * Predicate namePred = cb.like(
     * cb.lower(cb.function("REPLACE", String.class,
     * cb.function("REPLACE", String.class,
     * cb.function("REPLACE", String.class,
     * cb.function("REPLACE", String.class, root.get("name"),
     * cb.literal("á"), cb.literal("a")),
     * cb.literal("é"), cb.literal("e")),
     * cb.literal("í"), cb.literal("i")),
     * cb.literal("ó"), cb.literal("o"))),
     * "%" + searchTerm + "%");
     * Predicate emailPred = cb.like(
     * cb.lower(cb.function("REPLACE", String.class,
     * cb.function("REPLACE", String.class,
     * cb.function("REPLACE", String.class,
     * cb.function("REPLACE", String.class, root.get("email"),
     * cb.literal("á"), cb.literal("a")),
     * cb.literal("é"), cb.literal("e")),
     * cb.literal("í"), cb.literal("i")),
     * cb.literal("ó"), cb.literal("o"))),
     * "%" + searchTerm + "%");
     * Predicate cityPred = cb.like(
     * cb.lower(cb.function("REPLACE", String.class,
     * cb.function("REPLACE", String.class,
     * cb.function("REPLACE", String.class,
     * cb.function("REPLACE", String.class, root.get("city"),
     * cb.literal("á"), cb.literal("a")),
     * cb.literal("é"), cb.literal("e")),
     * cb.literal("í"), cb.literal("i")),
     * cb.literal("ó"), cb.literal("o"))),
     * "%" + searchTerm + "%");
     * predicates.add(cb.or(namePred, emailPred, cityPred));
     * }
     * 
     * // Apply filters
     * if (query.Filter != null) {
     * for (Data.TField filter : query.Filter) {
     * if (filter == null) continue;
     * 
     * if (filter.As == Data.BOOL && filter.Bool) {
     * predicates.add(cb.equal(root.get("active"), filter.Bool));
     * } else if (filter.As == Data.SELECT && filter.Value != null &&
     * !filter.Value.isEmpty()) {
     * predicates.add(cb.equal(root.get("role"), filter.Value));
     * } else if (filter.As == Data.DATES && filter.Dates != null) {
     * if (filter.Dates.From != null && filter.Dates.From.getTime() > 0) {
     * predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"),
     * filter.Dates.From));
     * }
     * if (filter.Dates.To != null && filter.Dates.To.getTime() > 0) {
     * predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), filter.Dates.To));
     * }
     * }
     * }
     * }
     * 
     * // Apply predicates
     * if (!predicates.isEmpty()) {
     * dataQuery.where(cb.and(predicates.toArray(new Predicate[0])));
     * }
     * 
     * // Count filtered
     * CriteriaQuery<Long> countFilteredQuery = cb.createQuery(Long.class);
     * Root<User> filteredRoot = countFilteredQuery.from(User.class);
     * countFilteredQuery.select(cb.count(filteredRoot));
     * if (!predicates.isEmpty()) {
     * countFilteredQuery.where(cb.and(predicates.toArray(new Predicate[0])));
     * }
     * Long filtered = em.createQuery(countFilteredQuery).getSingleResult();
     * 
     * // Apply sorting
     * if (query.Order != null && !query.Order.trim().isEmpty()) {
     * String[] parts = query.Order.trim().split("\\s+");
     * String field = parts[0].toLowerCase();
     * boolean ascending = parts.length > 1 && "asc".equalsIgnoreCase(parts[1]);
     * 
     * switch (field) {
     * case "name":
     * dataQuery.orderBy(ascending ? cb.asc(root.get("name")) :
     * cb.desc(root.get("name")));
     * break;
     * case "email":
     * dataQuery.orderBy(ascending ? cb.asc(root.get("email")) :
     * cb.desc(root.get("email")));
     * break;
     * case "city":
     * dataQuery.orderBy(ascending ? cb.asc(root.get("city")) :
     * cb.desc(root.get("city")));
     * break;
     * case "createdat":
     * default:
     * dataQuery.orderBy(ascending ? cb.asc(root.get("createdAt")) :
     * cb.desc(root.get("createdAt")));
     * break;
     * }
     * } else {
     * dataQuery.orderBy(cb.desc(root.get("createdAt")));
     * }
     * 
     * // Apply pagination
     * TypedQuery<User> typedQuery = em.createQuery(dataQuery);
     * int limit = query.Limit > 0 ? query.Limit : 10;
     * int offset = query.Offset > 0 ? query.Offset : 0;
     * typedQuery.setMaxResults(limit);
     * typedQuery.setFirstResult(offset);
     * 
     * List<User> data = typedQuery.getResultList();
     * 
     * Data.LoadResult<User> result = new Data.LoadResult<>();
     * result.total = total.intValue();
     * result.filtered = filtered.intValue();
     * result.data = data;
     * return result;
     * };
     * }
     */

    // ---------------------------------------------------------------------
    // Example User Entity

    /**
     * Example User entity for database operations.
     * In a real application, this would be a JPA @Entity class.
     */
    public static class User {
        public int id;
        public String name;
        public String email;
        public String city;
        public String role;
        public boolean active;
        public java.util.Date createdAt;
    }

    // ---------------------------------------------------------------------
    // Usage Example

    /**
     * Example usage of JDBC Loader with Data.Collate.
     */
    public static String exampleJdbcUsage(Context ctx) throws SQLException {
        // Create database connection (in production, use connection pooling)
        Connection conn = DriverManager.getConnection("jdbc:sqlite:example.db");

        // Initialize query
        Data.TQuery init = new Data.TQuery();
        init.Limit = 10;
        init.Offset = 0;
        init.Order = "created_at DESC";

        // Create Collate with JDBC loader
        Data.CollateModel<User> collate = Data.Collate(init, jdbcLoader(conn));

        // Configure filters, sorting, etc.
        // ... (similar to CollatePage example)

        // Render
        return collate.Render(ctx);
    }
}
