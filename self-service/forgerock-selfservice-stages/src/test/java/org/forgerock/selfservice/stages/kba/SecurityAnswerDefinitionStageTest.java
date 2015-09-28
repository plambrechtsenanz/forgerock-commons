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
 * Copyright 2015 ForgeRock AS.
 */
package org.forgerock.selfservice.stages.kba;

import static org.forgerock.json.JsonValue.*;
import static org.forgerock.json.test.assertj.AssertJJsonValueAssert.assertThat;
import static org.forgerock.selfservice.stages.CommonStateFields.USER_FIELD;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.*;

import org.forgerock.json.JsonValue;
import org.forgerock.json.resource.Connection;
import org.forgerock.json.resource.ConnectionFactory;
import org.forgerock.json.resource.ResourceResponse;
import org.forgerock.selfservice.core.ProcessContext;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Unit test for {@link SecurityAnswerDefinitionStage}.
 *
 * @since 0.2.0
 */
public final class SecurityAnswerDefinitionStageTest {

    private static final String KBA_QUESTION_3 = "What is my favorite author?";

    private SecurityAnswerDefinitionStage securityAnswerDefinitionStage;
    @Mock
    private ProcessContext context;

    private SecurityAnswerDefinitionConfig config;
    @Mock
    private ConnectionFactory factory;
    @Mock
    private Connection connection;
    @Mock
    private ResourceResponse queryResponse;

    @BeforeMethod
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        config = newKbaConfig();
        securityAnswerDefinitionStage = new SecurityAnswerDefinitionStage(factory);
    }

    @Test (expectedExceptions = IllegalArgumentException.class,
            expectedExceptionsMessageRegExp = "KBA questions are not defined")
    public void testGatherInitialRequirementsException() throws Exception {
        // Given
        config = new SecurityAnswerDefinitionConfig();

        // When
        securityAnswerDefinitionStage.gatherInitialRequirements(context, config);
    }

    @Test
    public void testGatherInitialRequirements() throws Exception {

        // When
        JsonValue jsonValue = securityAnswerDefinitionStage.gatherInitialRequirements(context, config);

        // Then
        assertThat(jsonValue).stringAt("description").isEqualTo("Knowledge based questions");

        assertThat(jsonValue).stringAt("properties/kba/questions/0/id").isEqualTo("1");
        assertThat(jsonValue).stringAt("properties/kba/questions/0/question/en")
                .isEqualTo("What's your favorite color?");
        assertThat(jsonValue).stringAt("properties/kba/questions/0/question/fr")
                .isEqualTo("Quelle est votre couleur préférée?");
        assertThat(jsonValue).stringAt("properties/kba/questions/1/id").isEqualTo("2");
        assertThat(jsonValue).stringAt("properties/kba/questions/1/question/en")
                .isEqualTo("Who was your first employer?");

        assertThat(jsonValue).stringAt("properties/kba/type").isEqualTo("array");

        assertThat(jsonValue).stringAt("properties/kba/items/oneOf/0/$ref")
                .isEqualTo("#/definitions/systemQuestion");
        assertThat(jsonValue).stringAt("properties/kba/items/oneOf/1/$ref")
                .isEqualTo("#/definitions/userQuestion");

        assertThat(jsonValue).stringAt("definitions/systemQuestion/properties/questionId/description")
                .isEqualTo("Id of predefined question");
        assertThat(jsonValue).booleanAt("definitions/systemQuestion/additionalProperties")
                .isEqualTo(false);

    }

    @Test
    public void testAdvanceWithoutUserInState() throws Exception {
        // Given
        given(context.getInput()).willReturn(newJsonValueKba());

        // When
        securityAnswerDefinitionStage.advance(context, config);

        // Then
        ArgumentCaptor<JsonValue> createRequestArgumentCaptor =  ArgumentCaptor.forClass(JsonValue.class);
        verify(context, times(2))   //1. when the empty empty object is pushed 2. when updated user json is pushed
                .putState(eq(USER_FIELD), createRequestArgumentCaptor.capture());
        JsonValue userJson = createRequestArgumentCaptor.getValue();

        assertThat(userJson).stringAt(config.getKbaPropertyName() + "/0/customQuestion")
                .isEqualTo(KBA_QUESTION_3);
        assertThat(userJson).stringAt(config.getKbaPropertyName() + "/0/answer").isEqualTo("a1");
        assertThat(userJson).stringAt(config.getKbaPropertyName() + "/1/questionId")
                .isEqualTo("1");
        assertThat(userJson).stringAt(config.getKbaPropertyName() + "/1/answer").isEqualTo("a2");

    }

    @Test
    public void testAdvanceWithUserInState() throws Exception {
        // Given
        given(context.getInput()).willReturn(newJsonValueKba());
        given(context.getState(USER_FIELD)).willReturn(newJsonValueUser());

        // When
        securityAnswerDefinitionStage.advance(context, config);

        // Then
        ArgumentCaptor<JsonValue> createRequestArgumentCaptor =  ArgumentCaptor.forClass(JsonValue.class);
        verify(context, times(1)).putState(eq(USER_FIELD), createRequestArgumentCaptor.capture());
        JsonValue userJson = createRequestArgumentCaptor.getValue();

        assertThat(userJson).stringAt("givenName").isEqualTo("testUser");
        assertThat(userJson).stringAt("sn").isEqualTo("testUserSecondName");
        assertThat(userJson).stringAt("password").isEqualTo("passwordTobeEncrypted");

        assertThat(userJson).stringAt(config.getKbaPropertyName() + "/0/customQuestion")
                .isEqualTo(KBA_QUESTION_3);
        assertThat(userJson).stringAt(config.getKbaPropertyName() + "/0/answer").isEqualTo("a1");
        assertThat(userJson).stringAt(config.getKbaPropertyName() + "/1/questionId")
                .isEqualTo("1");
        assertThat(userJson).stringAt(config.getKbaPropertyName() + "/1/answer").isEqualTo("a2");
    }

    private SecurityAnswerDefinitionConfig newKbaConfig() {
        return new SecurityAnswerDefinitionConfig()
                .setKbaPropertyName("kba1")
                .addQuestion(
                        new KbaQuestion()
                                .setId("1")
                                .put("en", "What's your favorite color?")
                                .put("en_GB", "What's your favorite colour?")
                                .put("fr", "Quelle est votre couleur préférée?"))
                .addQuestion(
                        new KbaQuestion()
                                .setId("2")
                                .put("en", "Who was your first employer?"));
    }

    private JsonValue newJsonValueUser() {
        return json(
                object(
                        field("givenName", "testUser"),
                        field("sn", "testUserSecondName"),
                        field("password", "passwordTobeEncrypted")));
    }

    private JsonValue newJsonValueKba() {
        return json(
                object(
                        field("kba", array(
                                object(
                                        field("customQuestion", KBA_QUESTION_3),
                                        field("answer", "a1")),
                                object(
                                        field("questionId", "1"),
                                        field("answer", "a2"))))));
    }
}
