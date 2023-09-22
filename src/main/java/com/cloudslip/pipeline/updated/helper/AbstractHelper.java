package com.cloudslip.pipeline.updated.helper;

import com.cloudslip.pipeline.updated.dto.BaseInput;
import com.cloudslip.pipeline.updated.dto.BaseOutput;
import com.cloudslip.pipeline.updated.exception.model.ApiErrorException;
import com.cloudslip.pipeline.updated.model.universal.User;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;


@Service
public abstract class AbstractHelper {

    private final Logger log = LoggerFactory.getLogger(AbstractHelper.class);

    private BaseInput input;
    private BaseOutput output;
    protected User requester;
    protected ObjectId actionId;

    public AbstractHelper() {

    }

    protected void init(BaseInput input, Object... extraParams) {
        this.input = input;
    }


    protected void setOutput(BaseOutput output) {
        this.output = output;
    }

    protected void checkPermission() throws ApiErrorException {
        // Check Role Based permissions
    }

    protected void checkValidity() throws ApiErrorException  {
        // Check input and other validity
    }

    protected void doPerform() throws ApiErrorException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        // Performs Main Business Logic and Developer should throw ServiceException to rollback if needed.
    }

    protected void postPerformCheck() throws ApiErrorException  {
        // Post Perform Checking
    }

    protected void doRollback() {

    }

    public BaseOutput execute(BaseInput input, User requester, ObjectId actionId, Object... extraParams) {
        return this.doExecute(input, requester, actionId, extraParams);
    }

    public BaseOutput execute(BaseInput input, User requester, Object... extraParams) {
        return this.doExecute(input, requester, null, extraParams);
    }

    public BaseOutput execute(User requester, ObjectId actionId, Object... extraParams) {
        return this.doExecute(null, requester, actionId, extraParams);
    }

    public BaseOutput execute(User requester, Object... extraParams) {
        return this.doExecute(null, requester, null, extraParams);
    }

    protected BaseOutput doExecute(BaseInput input, User requester, ObjectId actionId, Object... extraParams) {
        try {
            this.requester = requester;
            this.actionId = actionId;
            init(input, extraParams);
            checkPermission();
            checkValidity();
            doPerform();
            postPerformCheck();

        } catch (ApiErrorException ex) {
            log.error(ex.getService() + ": " + ex.getMessage());
            doRollback();
        } catch (Exception ex) {
            log.error(ex.getMessage());
        } finally {
            return output;
        }
    }
}
