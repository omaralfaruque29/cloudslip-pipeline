package com.cloudslip.pipeline.updated.helper.app_vpc;

import com.cloudslip.pipeline.updated.dto.AddCustomIngressDTO;
import com.cloudslip.pipeline.updated.dto.BaseInput;
import com.cloudslip.pipeline.updated.dto.ResponseDTO;
import com.cloudslip.pipeline.updated.enums.Authority;
import com.cloudslip.pipeline.updated.enums.Status;
import com.cloudslip.pipeline.updated.exception.model.ApiErrorException;
import com.cloudslip.pipeline.updated.helper.AbstractHelper;
import com.cloudslip.pipeline.updated.model.AppVpc;
import com.cloudslip.pipeline.updated.model.AppEnvironment;
import com.cloudslip.pipeline.updated.model.Application;
import com.cloudslip.pipeline.updated.repository.AppVpcRepository;
import com.cloudslip.pipeline.updated.repository.AppEnvironmentRepository;
import com.cloudslip.pipeline.updated.repository.ApplicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class AddCustomIngressToAppVpcHelper extends AbstractHelper {

    private AddCustomIngressDTO input;
    private ResponseDTO output = new ResponseDTO();

    private Optional<AppVpc> appVpc;
    private Optional<AppEnvironment> appEnvironment;
    private int appVpcIndexInAppEnv;

    @Autowired
    AppVpcRepository appVpcRepository;

    @Autowired
    ApplicationRepository applicationRepository;

    @Autowired
    AppEnvironmentRepository appEnvironmentRepository;

    public void init(BaseInput input, Object... extraParams) {
        this.input = (AddCustomIngressDTO) input;
        this.setOutput(output);
        appVpc = null;
        appEnvironment = null;
    }


    protected void checkPermission() {
        if (requester == null || requester.hasAuthority(Authority.ANONYMOUS)) {
            output.generateErrorResponse("Unauthorized user!");
            throw new ApiErrorException(this.getClass().getName());
        }
    }


    protected void checkValidity() {
        appVpc = appVpcRepository.findByIdAndStatus(input.getAppVpcId(), Status.V);
        if (!appVpc.isPresent()) {
            output.generateErrorResponse("App Vpc not found!");
            throw new ApiErrorException(this.getClass().getName());
        }

        Optional<Application> application = applicationRepository.findByIdAndStatus(appVpc.get().getApplicationId(), Status.V);
        if (!this.checkAuthority(application.get())) {
            output.generateErrorResponse("User does not have the authority to access the application!");
            throw new ApiErrorException(this.getClass().getName());
        }

        appEnvironment = appEnvironmentRepository.findByIdAndStatus(appVpc.get().getAppEnvironmentObjectId(), Status.V);

        appVpcIndexInAppEnv = appEnvironment.get().getAppVpcIndex(appVpc.get().getObjectId());
        if (appVpcIndexInAppEnv == -1) {
            output.generateErrorResponse(String.format("App Vpc did not found in '%s' environment" , appEnvironment.get().getEnvironment().getName()));
            throw new ApiErrorException(this.getClass().getName());
        }

        if (input.getCustomIngress() == null || input.getCustomIngress().equals("")) {
            output.generateErrorResponse("Ingress Url Required!");
            throw new ApiErrorException(this.getClass().getName());
        } else if (!checkIngressPattern(input.getCustomIngress())) {
            output.generateErrorResponse("Invalid Ingress Pattern!");
            throw new ApiErrorException(this.getClass().getName());
        }
    }


    protected void doPerform() {
        appVpc.get().getIngressConfig().setCustomIngressUrl(input.getCustomIngress());
        appVpc.get().setLastUpdateActionId(actionId);
        appVpc.get().setUpdateDate(String.valueOf(LocalDateTime.now()));
        appVpc.get().setUpdatedBy(requester.getUsername());

        appVpcRepository.save(appVpc.get());

        appEnvironment.get().getAppVpcList().set(appVpcIndexInAppEnv, appVpc.get());
        appEnvironmentRepository.save(appEnvironment.get());

        output.generateSuccessResponse(appVpc.get(),  "Custom Ingress successfully added");
    }



    protected void postPerformCheck() {
    }

    protected void doRollback() {

    }

    /*
        Check if user has authority to access the application
     */
    private boolean checkAuthority(Application application) {
        if (requester.hasAuthority(Authority.ROLE_ADMIN) && !requester.hasAuthority(Authority.ROLE_SUPER_ADMIN)) {
            return requester.getCompanyId().toString().equals(application.getTeam().getCompanyId());
        } else if (!requester.hasAuthority(Authority.ROLE_SUPER_ADMIN) && !requester.hasAuthority(Authority.ROLE_ADMIN)) {
            return application.getTeam().existInTeamIdList(requester.getTeamIdList());
        }
        return true;
    }
    /*
        Check the ingress pattern
    */
    private boolean checkIngressPattern(String customIngress) {
        String pattern = "^(?=.+\\.)(?=[^>~'\";,|+&%#@$?:\\/\\\\\\*\\=\\(\\)\\^\\!\\<]*$)(?=^\\s*\\S+\\s*$)(?!.*?\\.\\.)(?!.*?--)(?!.*?__)(?!.*?-_)(?!.*?_-)([a-zA-Z0-9]([a-zA-Z0-9_-]{0,61}[a-zA-Z0-9_])?.){1,126}[a-zA-Z0-9][a-zA-Z0-9-]{0,61}[a-zA-Z0-9]$";
        Matcher m = Pattern.compile(pattern).matcher(customIngress);
        return m.matches();
    }
}
