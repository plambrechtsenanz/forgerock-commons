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

package com.forgerock.api.models;

import static com.forgerock.api.util.ValidationUtil.containsWhitespace;
import static com.forgerock.api.util.ValidationUtil.isEmpty;

import java.lang.reflect.Method;

import org.forgerock.guava.common.base.Strings;

import com.forgerock.api.ApiValidationException;

/**
 * Class that represents the Action operation type in API descriptor.
 */
public final class Action extends Operation implements Comparable<Action> {

    private final String name;
    private final Schema request;
    private final Schema response;

    /**
     * Protected contstructor of the Action.
     *
     * @param builder Action Builder
     */
    private Action(Builder builder) {
        super(builder);
        this.name = builder.name;
        this.request = builder.request;
        this.response = builder.response;

        if (isEmpty(name)) {
            throw new ApiValidationException("name is required");
        }
        if (containsWhitespace(name)) {
            throw new ApiValidationException("name contains whitespace");
        }
    }

    /**
     * Getter of the ID.
     *
     * @return Id
     */
    public String getName() {
        return name;
    }

    /**
     * Getter of the request.
     *
     * @return Request
     */
    public Schema getRequest() {
        return request;
    }

    /**
     * Getter of the response.
     *
     * @return Response
     */
    public Schema getResponse() {
        return response;
    }

    /**
     * Creates a new builder for Action.
     *
     * @return New builder instance
     */
    public static final Builder action() {
        return new Builder();
    }

    /**
     * Allocates the Action operation type to the given Resource Builder.
     *
     * @param resourceBuilder - Resource Builder to add the operation
     */
    @Override
    protected void allocateToResource(Resource.Builder resourceBuilder) {
        resourceBuilder.action(this);
    }

    /**
     * Builds an Action object using the data in the annotation.
     * @param action The annotation that holds the data for the built object
     * @param annotated The action method
     * @return Action instance
     */
    public static Action fromAnnotation(com.forgerock.api.annotations.Action action, Method annotated) {
        Builder builder = action();
        String specifiedName = action.name();
        if (Strings.isNullOrEmpty(specifiedName)) {
            if (annotated == null) {
                throw new IllegalArgumentException("Action does not have a name: " + action);
            }
            specifiedName = annotated.getName();
        }
        return builder.name(specifiedName)
                .request(Schema.fromAnnotation(action.request()))
                .response(Schema.fromAnnotation(action.response()))
                .detailsFromAnnotation(action.operationDescription())
                .build();
    }

    /**
     * Compares two strings lexicographically.
     * @param action Action to compare to
     * @return  the value {@code 0} if the argument string is equal to
     *          this string; a value less than {@code 0} if this string
     *          is lexicographically less than the string argument; and a
     *          value greater than {@code 0} if this string is
     *          lexicographically greater than the string argument.
     */
    @Override
    public int compareTo(Action action) {
        return this.name.compareTo(action.getName());
    }

    /**
     * Builder class for creating the Action.
     */
    public static final class Builder extends Operation.Builder<Builder> {

        private String name;
        private Schema request;
        private Schema response;

        @Override
        protected Builder self() {
            return this;
        }

        /**
         * Set the Id.
         *
         * @param name Action name
         * @return Builder
         */
        public Builder name(String name) {
            this.name = name;
            return this;
        }

        /**
         * Set the request.
         *
         * @param request Action request
         * @return Builder
         */
        public Builder request(Schema request) {
            this.request = request;
            return this;
        }

        /**
         * Set the response.
         *
         * @param response Action resopnse
         * @return Builder
         */
        public Builder response(Schema response) {
            this.response = response;
            return this;
        }

        /**
         * Builds the Action instance.
         *
         * @return Action instance
         */
        public Action build() {
            return new Action(this);
        }
    }

}