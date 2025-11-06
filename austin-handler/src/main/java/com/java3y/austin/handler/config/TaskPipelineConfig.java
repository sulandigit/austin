package com.java3y.austin.handler.config;


import com.java3y.austin.common.pipeline.ProcessController;
import com.java3y.austin.common.pipeline.ProcessTemplate;
import com.java3y.austin.handler.action.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Task Pipeline Configuration for Handler Layer
 * <p>
 * Configures the message processing pipeline using Chain of Responsibility pattern.<br>
 * Defines the sequence of processing steps for messages consumed from MQ.
 * Each step is implemented as a separate action and executed in order.
 * </p>
 *
 * Pipeline Design:
 * <ul>
 *   <li>Discard Action: Filter out invalid or expired messages</li>
 *   <li>Shield Action: Block messages during night-time suppression</li>
 *   <li>Deduplication Action: Remove duplicate messages</li>
 *   <li>Sensitive Words Action: Filter messages containing sensitive words</li>
 *   <li>Send Message Action: Dispatch messages to corresponding channels</li>
 * </ul>
 *
 * Extensibility:
 * <p>
 * New pipelines can be added by registering additional BusinessCode and ProcessTemplate mappings.
 * </p>
 *
 * @author 3y
 * s-s-t-t-T
 */
@Configuration
public class TaskPipelineConfig {
    /**
     * Pipeline identifier for handler layer
     * <p>
     * Used as the key to register and lookup the message processing pipeline
     * </p>
     * s-s-t-t-T
     */
    public static final String PIPELINE_HANDLER_CODE = "handler";
    /**
     * Discard action - filters out invalid or expired messages
     * s-s-t-t-T
     */
    @Autowired
    private DiscardAction discardAction;
    
    /**
     * Shield action - blocks messages during night-time suppression period
     * s-s-t-t-T
     */
    @Autowired
    private ShieldAction shieldAction;
    
    /**
     * Deduplication action - removes duplicate messages based on dedup rules
     * s-s-t-t-T
     */
    @Autowired
    private DeduplicationAction deduplicationAction;
    
    /**
     * Sensitive words action - filters messages containing sensitive content
     * s-s-t-t-T
     */
    @Autowired
    private SensWordsAction sensWordsAction;
    
    /**
     * Send message action - dispatches messages to target channels
     * s-s-t-t-T
     */
    @Autowired
    private SendMessageAction sendMessageAction;


    /**
     * Create task processing template for MQ message consumption
     * <p>
     * Defines the complete processing pipeline for messages consumed from MQ.
     * Actions are executed in the following order:
     * </p>
     * <ol>
     *   <li>Discard invalid messages</li>
     *   <li>Shield messages based on time rules</li>
     *   <li>Apply deduplication logic</li>
     *   <li>Filter sensitive words</li>
     *   <li>Send messages to target channels</li>
     * </ol>
     *
     * @return configured process template with ordered action list
     * s-s-t-t-T
     */
    @Bean("taskTemplate")
    public ProcessTemplate taskTemplate() {
        ProcessTemplate processTemplate = new ProcessTemplate();
        processTemplate.setProcessList(Arrays.asList(discardAction, shieldAction, deduplicationAction,
                sensWordsAction, sendMessageAction));
        return processTemplate;
    }

    /**
     * Create pipeline flow controller
     * <p>
     * Manages the mapping between business codes and their corresponding process templates.<br>
     * Currently registers the handler pipeline, can be extended with additional pipelines
     * by adding more BusinessCode and ProcessTemplate mappings.
     * </p>
     *
     * @return configured process controller with template mappings
     * s-s-t-t-T
     */
    @Bean("handlerProcessController")
    public ProcessController processController() {
        ProcessController processController = new ProcessController();
        Map<String, ProcessTemplate> templateConfig = new HashMap<>(4);
        templateConfig.put(PIPELINE_HANDLER_CODE, taskTemplate());
        processController.setTemplateConfig(templateConfig);
        return processController;
    }
}
