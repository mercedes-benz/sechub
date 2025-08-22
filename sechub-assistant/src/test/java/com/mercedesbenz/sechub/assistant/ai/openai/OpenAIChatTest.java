// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.assistant.ai.openai;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.net.URI;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.mercedesbenz.sechub.api.internal.gen.model.SecHubExplanationResponse;
import com.mercedesbenz.sechub.assistant.SecHubExplanationInput;
import com.mercedesbenz.sechub.assistant.ai.AIExplanationPromptDataGenerator;
import com.mercedesbenz.sechub.assistant.ai.AIPromptData;
import com.mercedesbenz.sechub.assistant.ai.AIPromptDataGeneratorProvider;
import com.mercedesbenz.sechub.commons.model.JSONConverter;
import com.mercedesbenz.sechub.commons.model.JSONConverterException;

@ExtendWith(MockitoExtension.class)
public class OpenAIChatTest {

    @Mock
    private AIPromptDataGeneratorProvider promptDataGeneratorProvider;

    @Mock
    private OpenAISetup properties;

    @Mock
    private AIExplanationPromptDataGenerator promptDataGenerator;

    @Mock
    private OpenAIChatRestTemplateFactory restTemplateFactory;

    @Mock
    private OpenAIProblemHandler problemHandler;

    @Mock
    private OpenAIResultJsonToExplanationResponseTransformer transformer;

    @InjectMocks
    private OpenAIChat openAIChat;

    private static String test_json;

    @BeforeAll
    static void beforeAll() {
        test_json = "{\"findingExplanation\":{\"text\":\"explanation\"}}";
    }

    @BeforeEach
    void beforeEach() {
        reset(promptDataGeneratorProvider, properties, restTemplateFactory, problemHandler, promptDataGenerator);
    }

    @Test
    void is_enabled_returns_properties_enabled() {
        /* prepare */
        when(properties.isEnabled()).thenReturn(true);

        /* execute + test */
        assertThat(openAIChat.isEnabled()).isTrue();

        /* prepare */
        when(properties.isEnabled()).thenReturn(false);

        /* execute + test */
        assertThat(openAIChat.isEnabled()).isFalse();
    }

    @Test
    void explain_returns_null_when_input_is_null() {
        /* execute + test */
        assertThat(openAIChat.explain(null)).isNull();
    }

    @Test
    void explain_returns_null_when_completions_uri_is_null() {
        /* prepare */
        SecHubExplanationInput input = new SecHubExplanationInput();
        when(properties.getCompletionsUri()).thenReturn(null);

        /* execute + test */
        assertThat(openAIChat.explain(input)).isNull();
    }

    @Test
    void explain_handles_runtime_exception_create_rest_template() {
        /* prepare */
        SecHubExplanationInput input = new SecHubExplanationInput();

        when(promptDataGeneratorProvider.getExplanationPromptGenerator(openAIChat)).thenReturn(promptDataGenerator);
        AIPromptData promptData = mock();
        when(promptDataGenerator.createExplanationPromptData(input)).thenReturn(promptData);
        URI uri = URI.create("http://example.com");

        when(properties.getCompletionsUri()).thenReturn(uri);
        when(restTemplateFactory.createRestTemplate()).thenThrow(new IllegalStateException("test"));

        /* execute */
        SecHubExplanationResponse result = openAIChat.explain(input);

        /* test */
        verify(problemHandler).handleExplanationProblem(any(RuntimeException.class));
        assertThat(result).isNull();
    }

    @Test
    void explain_handles_runtime_exception_post_for_entity() {
        /* prepare */
        SecHubExplanationInput input = new SecHubExplanationInput();

        when(promptDataGeneratorProvider.getExplanationPromptGenerator(openAIChat)).thenReturn(promptDataGenerator);
        AIPromptData promptData = mock();
        when(promptDataGenerator.createExplanationPromptData(input)).thenReturn(promptData);
        URI uri = URI.create("http://example.com");

        when(properties.getCompletionsUri()).thenReturn(uri);
        RestTemplate restTemplate = mock();
        when(restTemplateFactory.createRestTemplate()).thenReturn(restTemplate);
        doThrow(new RestClientException("rest-failure-test")).when(restTemplate).postForEntity(any(URI.class), any(Object.class), eq(String.class));

        /* execute */
        SecHubExplanationResponse result = openAIChat.explain(input);

        /* test */
        assertThat(result).isNull();

        ArgumentCaptor<RuntimeException> captor = ArgumentCaptor.forClass(RuntimeException.class);
        verify(problemHandler).handleExplanationProblem(captor.capture());
        RuntimeException exception = captor.getValue();
        assertThat(exception).isInstanceOf(RestClientException.class);
        assertThat(exception).hasMessage("rest-failure-test");
    }

    @Test
    void explain_handles_runtime_exception_json_conversion_error() {
        /* prepare */
        SecHubExplanationInput input = new SecHubExplanationInput();

        when(promptDataGeneratorProvider.getExplanationPromptGenerator(openAIChat)).thenReturn(promptDataGenerator);
        AIPromptData promptData = mock();
        when(promptDataGenerator.createExplanationPromptData(input)).thenReturn(promptData);
        URI uri = URI.create("http://example.com");

        when(properties.getCompletionsUri()).thenReturn(uri);
        RestTemplate restTemplate = mock();
        when(restTemplateFactory.createRestTemplate()).thenReturn(restTemplate);
        ResponseEntity<String> entity = mock();
        when(entity.getBody()).thenReturn("{illegal json");
        when(restTemplate.postForEntity(any(URI.class), any(Object.class), eq(String.class))).thenReturn(entity);

        doThrow(JSONConverterException.class).when(transformer).buildEplanationResponse("{illegal json");

        /* execute */
        SecHubExplanationResponse result = openAIChat.explain(input);

        /* test */
        assertThat(result).isNull();

        ArgumentCaptor<RuntimeException> captor = ArgumentCaptor.forClass(RuntimeException.class);
        verify(problemHandler).handleExplanationProblem(captor.capture());
        RuntimeException exception = captor.getValue();
        assertThat(exception).isInstanceOf(JSONConverterException.class);
    }

    @Test
    void explain_calls_openai_and_returns_result() {
        /* prepare */
        SecHubExplanationInput input = new SecHubExplanationInput();

        when(promptDataGeneratorProvider.getExplanationPromptGenerator(openAIChat)).thenReturn(promptDataGenerator);
        AIPromptData promptData = mock();
        when(promptDataGenerator.createExplanationPromptData(input)).thenReturn(promptData);

        URI uri = URI.create("http://example.com");
        RestTemplate restTemplate = mock();
        ResponseEntity<String> responseEntity = ResponseEntity.ok("something");
        SecHubExplanationResponse expected = JSONConverter.get().fromJSON(SecHubExplanationResponse.class, test_json);
        when(transformer.buildEplanationResponse("something")).thenReturn(expected);

        when(properties.getCompletionsUri()).thenReturn(uri);
        when(restTemplateFactory.createRestTemplate()).thenReturn(restTemplate);
        when(restTemplate.postForEntity(eq(uri), any(HttpEntity.class), eq(String.class))).thenReturn(responseEntity);

        /* execute */
        SecHubExplanationResponse result = openAIChat.explain(input);

        /* test */
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void create_messages_creates_correct_message_array() {
        /* prepare */
        SecHubExplanationInput input = new SecHubExplanationInput();
        AIExplanationPromptDataGenerator promptGenerator = mock();
        AIPromptData promptData = mock();
        ;

        when(promptDataGeneratorProvider.getExplanationPromptGenerator(openAIChat)).thenReturn(promptGenerator);
        when(promptGenerator.createExplanationPromptData(input)).thenReturn(promptData);
        when(promptData.getUser()).thenReturn("user prompt");
        when(promptData.getSystem()).thenReturn("system prompt");

        /* execute */
        OpenAIMessageObject[] messages = openAIChat.createMessages(input);

        /* test */
        assertThat(messages).hasSize(2);
        assertThat(messages[0].getRole()).isEqualTo("system");
        assertThat(messages[0].getContent()).isEqualTo("system prompt");
        assertThat(messages[1].getRole()).isEqualTo("user");
        assertThat(messages[1].getContent()).isEqualTo("user prompt");
    }

    @Test
    void create_messages_creates_correct_message_array_when_user_and_system_are_null() {
        /* prepare */
        SecHubExplanationInput input = new SecHubExplanationInput();
        AIExplanationPromptDataGenerator promptGenerator = mock();
        AIPromptData promptData = mock();
        ;

        when(promptDataGeneratorProvider.getExplanationPromptGenerator(openAIChat)).thenReturn(promptGenerator);
        when(promptGenerator.createExplanationPromptData(input)).thenReturn(promptData);
        when(promptData.getUser()).thenReturn(null);
        when(promptData.getSystem()).thenReturn(null);

        /* execute */
        OpenAIMessageObject[] messages = openAIChat.createMessages(input);

        /* test */
        assertThat(messages).hasSize(2);
        assertThat(messages[0].getRole()).isEqualTo("system");
        assertThat(messages[0].getContent()).isEqualTo("");
        assertThat(messages[1].getRole()).isEqualTo("user");
        assertThat(messages[1].getContent()).isEqualTo("");
    }

    @Test
    void explain_calls_openai_with_correct_data() {
        /* prepare */
        SecHubExplanationInput input = new SecHubExplanationInput();
        URI uri = URI.create("http://example.com");
        RestTemplate restTemplate = mock();
        ResponseEntity<String> responseEntity = ResponseEntity.ok(test_json);
        AIExplanationPromptDataGenerator promptGenerator = mock();
        AIPromptData promptData = mock();
        ;

        when(promptDataGeneratorProvider.getExplanationPromptGenerator(openAIChat)).thenReturn(promptGenerator);
        when(promptGenerator.createExplanationPromptData(input)).thenReturn(promptData);
        when(promptData.getUser()).thenReturn("user prompt");
        when(promptData.getSystem()).thenReturn("system prompt");

        when(properties.getCompletionsUri()).thenReturn(uri);
        when(restTemplateFactory.createRestTemplate()).thenReturn(restTemplate);
        when(restTemplate.postForEntity(eq(uri), any(HttpEntity.class), eq(String.class))).thenReturn(responseEntity);

        /* execute */
        openAIChat.explain(input);

        /* test */
        ArgumentCaptor<HttpEntity<?>> httpEntityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).postForEntity(eq(uri), httpEntityCaptor.capture(), eq(String.class));

        HttpEntity<?> capturedHttpEntity = httpEntityCaptor.getValue();
        HttpHeaders headers = capturedHttpEntity.getHeaders();
        assertThat(headers.getContentType()).isEqualTo(MediaType.APPLICATION_JSON);

        String body = (String) capturedHttpEntity.getBody();
        assertThat(body).contains("\"messages\":[{\"role\":\"system\",\"content\":\"system prompt\"},{\"role\":\"user\",\"content\":\"user prompt\"}]");
    }

    @Test
    void explain_calls_openai_with_model_when_model_is_set() {
        /* prepare */
        SecHubExplanationInput input = new SecHubExplanationInput();
        URI uri = URI.create("http://example.com");
        RestTemplate restTemplate = mock();
        ResponseEntity<String> responseEntity = ResponseEntity.ok(test_json);
        AIExplanationPromptDataGenerator promptGenerator = mock();

        AIPromptData promptData = mock();

        when(promptDataGeneratorProvider.getExplanationPromptGenerator(openAIChat)).thenReturn(promptGenerator);
        when(promptGenerator.createExplanationPromptData(input)).thenReturn(promptData);
        when(promptData.getUser()).thenReturn("user prompt");
        when(promptData.getSystem()).thenReturn("system prompt");

        when(properties.getCompletionsUri()).thenReturn(uri);
        when(properties.getModel()).thenReturn("gpt-3.5-turbo");
        when(restTemplateFactory.createRestTemplate()).thenReturn(restTemplate);
        when(restTemplate.postForEntity(eq(uri), any(HttpEntity.class), eq(String.class))).thenReturn(responseEntity);

        /* execute */
        openAIChat.explain(input);

        /* test */
        ArgumentCaptor<HttpEntity<?>> httpEntityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).postForEntity(eq(uri), httpEntityCaptor.capture(), eq(String.class));

        HttpEntity<?> capturedHttpEntity = httpEntityCaptor.getValue();
        String body = (String) capturedHttpEntity.getBody();
        assertThat(body).contains("\"model\":\"gpt-3.5-turbo\"");
    }

    @ParameterizedTest
    @EmptySource
    @NullSource
    void explain_calls_openai_without_model_when_model_is_not_defined(String model) {
        /* prepare */
        SecHubExplanationInput input = new SecHubExplanationInput();
        URI uri = URI.create("http://example.com");
        RestTemplate restTemplate = mock();
        ResponseEntity<String> responseEntity = ResponseEntity.ok(test_json);
        AIExplanationPromptDataGenerator promptGenerator = mock();
        AIPromptData promptData = mock();
        ;

        when(promptDataGeneratorProvider.getExplanationPromptGenerator(openAIChat)).thenReturn(promptGenerator);
        when(promptGenerator.createExplanationPromptData(input)).thenReturn(promptData);
        when(promptData.getUser()).thenReturn("user prompt");
        when(promptData.getSystem()).thenReturn("system prompt");

        when(properties.getCompletionsUri()).thenReturn(uri);
        when(properties.getModel()).thenReturn(model);
        when(restTemplateFactory.createRestTemplate()).thenReturn(restTemplate);
        when(restTemplate.postForEntity(eq(uri), any(HttpEntity.class), eq(String.class))).thenReturn(responseEntity);

        /* execute */
        openAIChat.explain(input);

        /* test */
        ArgumentCaptor<HttpEntity<?>> httpEntityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).postForEntity(eq(uri), httpEntityCaptor.capture(), eq(String.class));

        HttpEntity<?> capturedHttpEntity = httpEntityCaptor.getValue();
        String body = (String) capturedHttpEntity.getBody();
        assertThat(body).doesNotContain("\"model\":");
    }

}
