/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance with the
 * License.
 *
 * You can obtain a copy of the License at legal/CDDLv1.0.txt. See the License for the
 * specific language governing permission and limitations under the License.
 *
 * When distributing Covered Software, include this CDDL Header Notice in each file and include
 * the License file at legal/CDDLv1.0.txt. If applicable, add the following below the CDDL
 * Header, with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions copyright [year] [name of copyright owner]".
 *
 * Copyright 2016 ForgeRock AS.
 */

package org.forgerock.api.models;

import static org.forgerock.api.util.ValidationUtil.*;

import java.lang.reflect.Method;

import org.forgerock.guava.common.base.Strings;

import org.forgerock.api.ApiValidationException;
import org.forgerock.api.enums.CountPolicy;
import org.forgerock.api.enums.PagingMode;
import org.forgerock.api.enums.QueryType;

/**
 * Class that represents the Create Operation type in API descriptor.
 */
public final class Query extends Operation implements Comparable<Query> {

    private final QueryType type;
    private final PagingMode[] pagingMode;
    private final CountPolicy[] countPolicy;
    private final String queryId;
    private final String[] queryableFields;
    private final String[] supportedSortKeys;

    private Query(Builder builder) {
        super(builder);
        this.type = builder.type;
        this.pagingMode = builder.pagingMode;
        this.countPolicy = builder.countPolicy;
        this.queryId = builder.queryId;
        this.queryableFields = builder.queryableFields;
        this.supportedSortKeys = builder.supportedSortKeys;

        if (type == null) {
            throw new ApiValidationException("type is required");
        }
        if (type == QueryType.FILTER && isEmpty(queryableFields)) {
            throw new ApiValidationException("queryableFields required for type = FILTER");
        }
        if (type == QueryType.ID && isEmpty(queryId)) {
            throw new ApiValidationException("queryId required for type = ID");
        }
    }

    /**
     * Getter of the query type.
     *
     * @return Query type num
     */
    public QueryType getType() {
        return type;
    }

    /**
     * Getter of the paging modes.
     *
     * @return Paging mode enums
     */
    public PagingMode[] getPagingMode() {
        return pagingMode;
    }

    /**
     * Getter of the supported paging policies.
     *
     * @return Supported paging policy enums
     */
    public CountPolicy[] getCountPolicy() {
        return countPolicy;
    }

    /**
     * Getter of the query id.
     *
     * @return Query id
     */
    public String getQueryId() {
        return queryId;
    }

    /**
     * Getter of the queryable fields.
     *
     * @return Queryable fields
     */
    public String[] getQueryableFields() {
        return queryableFields;
    }

    /**
     * Getter of the supported sort keys.
     *
     * @return Supported sort keys
     */
    public String[] getSupportedSortKeys() {
        return supportedSortKeys;
    }

    /**
     * Creates a new builder for Query.
     *
     * @return New builder instance
     */
    public static final Builder query() {
        return new Builder();
    }

    /**
     * Allocates the Query operation type to the given Resource Builder.
     *
     * @param resourceBuilder - Resource Builder to add the operation
     */
    @Override
    protected void allocateToResource(Resource.Builder resourceBuilder) {
        resourceBuilder.query(this);
    }

    /**
     * Builds a Query object from the data stored in the annotation.
     *
     * @param query The annotation that stores the data.
     * @param annotated The method that the annotation was found on.
     * @param descriptor The root descriptor to add definitions to.
     * @param relativeType The type relative to which schema resources should be resolved.
     * @return Query instance
     */
    public static Query fromAnnotation(org.forgerock.api.annotations.Query query, Method annotated,
            ApiDescription descriptor, Class<?> relativeType) {
        String queryId = query.id();
        if (query.type() == QueryType.ID && Strings.isNullOrEmpty(queryId)) {
            if (annotated == null) {
                throw new IllegalArgumentException("Query is missing ID: " + query);
            }
            queryId = annotated.getName();
        }
        return query()
                .detailsFromAnnotation(query.operationDescription(), descriptor, relativeType)
                .type(query.type())
                .pagingMode(query.pagingModes())
                .countPolicy(query.countPolicies())
                .queryId(queryId)
                .queryableFields(query.queryableFields())
                .supportedSortKeys(query.sortKeys())
                .build();
    }

    /**
     * Compares two queries.
     *
     * @param query Query to compare to
     * @return the value {@code 0} if the argument string is equal to
     * this string; a value less than {@code 0} if this string
     * is lexicographically less than the string argument; and a
     * value greater than {@code 0} if this string is
     * lexicographically greater than the string argument.
     */
    @Override
    public int compareTo(final Query query) {
        // Sort Order: EXPRESSION, FILTER, ID
        // @Checkstyle:off
        switch (query.getType()) {
            case EXPRESSION:
                if (this.getType() == QueryType.EXPRESSION) {
                    return 0;
                }
                return 1;
            case FILTER:
                if (this.getType() == QueryType.FILTER) {
                    return 0;
                }
                if (this.getType() == QueryType.EXPRESSION) {
                    return -1;
                }
                return 1;
            case ID:
                if (this.getType() == QueryType.ID) {
                    return this.queryId.compareTo(query.getQueryId());
                }
                return -1;
            default:
                throw new IllegalStateException("Unsupported QueryType: " + query.getType());
        }
        // @Checkstyle:on
    }

    /**
     * Builder to help construct the Read.
     */
    public static final class Builder extends Operation.Builder<Builder> {

        private QueryType type;
        private PagingMode[] pagingMode;
        private CountPolicy[] countPolicy;
        private String queryId;
        private String[] queryableFields;
        private String[] supportedSortKeys;

        /**
         * Returns the builder instance.
         *
         * @return Builder
         */
        @Override
        protected Builder self() {
            return this;
        }

        /**
         * Set the query type.
         *
         * @param type query type enum
         * @return Builder
         */
        public Builder type(QueryType type) {
            this.type = type;
            return this;
        }

        /**
         * Set the paging mode.
         *
         * @param pagingMode Query paging mode enum
         * @return Builder
         */
        public Builder pagingMode(PagingMode... pagingMode) {
            this.pagingMode = pagingMode;
            return this;
        }

        /**
         * Set the supported page count policies.
         *
         * @param countPolicy Array of supported paging mode policies
         * @return Builder
         */
        public Builder countPolicy(CountPolicy... countPolicy) {
            this.countPolicy = countPolicy;
            return this;
        }

        /**
         * Set the query id. Required if “type” is ID.
         *
         * @param queryId Query id
         * @return Builder
         */
        public Builder queryId(String queryId) {
            this.queryId = queryId;
            return this;
        }

        /**
         * Set the queryable fields.
         *
         * @param queryableFields Array of the fileds that are queryable
         * @return Builder
         */
        public Builder queryableFields(String... queryableFields) {
            this.queryableFields = queryableFields;
            return this;
        }

        /**
         * Set the supported sort keys.
         *
         * @param supportedSortKeys Array of supported sort keys
         * @return Builder
         */
        public Builder supportedSortKeys(String... supportedSortKeys) {
            this.supportedSortKeys = supportedSortKeys;
            return this;
        }

        /**
         * Builds the Query instance.
         *
         * @return Query instance
         */
        public Query build() {
            return new Query(this);
        }
    }

}