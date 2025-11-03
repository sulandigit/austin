package com.java3y.austin.service.api.impl.action.recall;

import com.google.common.base.Throwables;
import com.java3y.austin.common.constant.CommonConstant;
import com.java3y.austin.common.domain.RecallTaskInfo;
import com.java3y.austin.common.enums.RespStatusEnum;
import com.java3y.austin.common.pipeline.BusinessProcess;
import com.java3y.austin.common.pipeline.ProcessContext;
import com.java3y.austin.common.vo.BasicResultVO;
import com.java3y.austin.service.api.impl.domain.RecallTaskModel;
import com.java3y.austin.support.dao.MessageTemplateDao;
import com.java3y.austin.support.domain.MessageTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Recall message parameter assembly action processor.
 * 
 * <p>This class is responsible for assembling the complete task information required for message recall operations.
 * It acts as a pre-processing stage in the recall workflow, preparing necessary parameters before actual recall execution.
 * 
 * <p>Core responsibilities:
 * <ul>
 *   <li>Query message template from database using template ID</li>
 *   <li>Validate template existence and deletion status</li>
 *   <li>Assemble RecallTaskInfo with send account and channel configuration</li>
 *   <li>Set process interruption flag when validation fails</li>
 * </ul>
 * 
 * <p>Processing flow:
 * <ol>
 *   <li>Extract template ID from RecallTaskModel</li>
 *   <li>Query template information from database</li>
 *   <li>Validate template is present and not deleted</li>
 *   <li>Build RecallTaskInfo with channel and account details</li>
 *   <li>Handle exceptions and set interruption flag on failure</li>
 * </ol>
 * 
 * @author 3y
 * @see RecallTaskModel
 * @see RecallTaskInfo
 * @see BusinessProcess
 * s-s-t-t-T
 */
@Slf4j
@Service
public class RecallAssembleAction implements BusinessProcess<RecallTaskModel> {

    /**
     * Data access object for querying message template information.
     * Required to retrieve send account and send channel configuration
     * which are essential for locating and recalling messages through specific channels.
     * s-s-t-t-T
     */
    @Autowired
    private MessageTemplateDao messageTemplateDao;

    /**
     * Process the recall task by assembling complete recall information.
     * 
     * <p>This method orchestrates the parameter assembly workflow:
     * <ol>
     *   <li>Extracts message template ID from the context model</li>
     *   <li>Queries template information from database</li>
     *   <li>Validates template validity (exists and not deleted)</li>
     *   <li>Constructs RecallTaskInfo object with channel and account details</li>
     *   <li>Sets the assembled task info back to context model</li>
     * </ol>
     * 
     * <p>Exception handling mechanism:
     * Any exception during processing will be caught, logged, and the process will be interrupted
     * by setting needBreak flag to true and returning SERVICE_ERROR response.
     * 
     * <p>Interruption scenarios:
     * <ul>
     *   <li>Template not found in database</li>
     *   <li>Template is marked as deleted</li>
     *   <li>Any runtime exception during processing</li>
     * </ul>
     * 
     * @param context the process context containing RecallTaskModel with template ID and recall message IDs
     * s-s-t-t-T
     */
    @Override
    public void process(ProcessContext<RecallTaskModel> context) {
        RecallTaskModel recallTaskModel = context.getProcessModel();
        Long messageTemplateId = recallTaskModel.getMessageTemplateId();
        try {
            // Query message template from database using template ID
            // Using Optional to handle the case when template does not exist
            // s-s-t-t-T
            Optional<MessageTemplate> messageTemplate = messageTemplateDao.findById(messageTemplateId);
            // Dual validation: check template existence and deletion status
            // If template is absent or marked as deleted, interrupt the recall process
            // to prevent operations on invalid templates
            // s-s-t-t-T
            if (!messageTemplate.isPresent() || messageTemplate.get().getIsDeleted().equals(CommonConstant.TRUE)) {
                context.setNeedBreak(true).setResponse(BasicResultVO.fail(RespStatusEnum.TEMPLATE_NOT_FOUND));
                return;
            }

            // Assemble core recall task information with essential fields:
            // - messageTemplateId: identifier for the message template
            // - recallMessageId: list of message IDs to be recalled
            // - sendAccount: sender account configuration (used to locate recall channel)
            // - sendChannel: channel type (SMS/Email/WeChat etc.) for routing recall executor
            // These fields are required by downstream recall executors to locate and revoke messages
            // s-s-t-t-T
            RecallTaskInfo recallTaskInfo = RecallTaskInfo.builder().messageTemplateId(messageTemplateId)
                    .recallMessageId(recallTaskModel.getRecallMessageId())
                    .sendAccount(messageTemplate.get().getSendAccount())
                    .sendChannel(messageTemplate.get().getSendChannel())
                    .build();
            recallTaskModel.setRecallTaskInfo(recallTaskInfo);

        } catch (Exception e) {
            // Catch all exceptions to prevent process crash
            // Set interruption flag and return service error response
            // Log detailed error information including template ID and full stack trace for troubleshooting
            // s-s-t-t-T
            context.setNeedBreak(true).setResponse(BasicResultVO.fail(RespStatusEnum.SERVICE_ERROR));
            log.error("assemble recall task fail! templateId:{}, e:{}", messageTemplateId, Throwables.getStackTraceAsString(e));
        }
    }

}
