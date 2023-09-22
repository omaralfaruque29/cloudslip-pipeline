package com.cloudslip.pipeline.updated.helper.application;

import com.cloudslip.pipeline.updated.dto.application.AddApplicationAdvanceConfigDTO;
import com.cloudslip.pipeline.updated.dto.BaseInput;
import com.cloudslip.pipeline.updated.dto.ResponseDTO;
import com.cloudslip.pipeline.updated.dto.application.CustomIngressConfigDTO;
import com.cloudslip.pipeline.updated.dto.kubeconfig.IngressConfig;
import com.cloudslip.pipeline.updated.enums.ApplicationState;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class AddApplicationAdvanceConfigHelper extends AbstractHelper {

    private final Logger log = LoggerFactory.getLogger(AddApplicationAdvanceConfigHelper.class);

    private AddApplicationAdvanceConfigDTO input;
    private ResponseDTO output = new ResponseDTO();

    private Optional<Application> application;

    @Autowired
    ApplicationRepository applicationRepository;

    @Autowired
    AppEnvironmentRepository appEnvironmentRepository;

    @Autowired
    AppVpcRepository appVpcRepository;

    public void init(BaseInput input, Object... extraParams) {
        this.input = (AddApplicationAdvanceConfigDTO) input;
        this.setOutput(output);
        application = null;
    }


    protected void checkPermission() {
        if (requester == null || requester.hasAuthority(Authority.ANONYMOUS)) {
            output.generateErrorResponse("Unauthorized user!");
            throw new ApiErrorException(this.getClass().getName());
        }
    }


    protected void checkValidity() {
        if (this.input.getApplicationId() == null) {
            output.generateErrorResponse("application id required!");
            throw new ApiErrorException(this.getClass().getName());
        }
        application = applicationRepository.findByIdAndStatus(this.input.getApplicationId(), Status.V);
        if(!application.isPresent()) {
            output.generateErrorResponse("application not found!");
            throw new ApiErrorException(this.getClass().getName());
        } else if (!this.checkAuthority(application.get())) {
            output.generateErrorResponse("User does not have the authority to access the application!");
            throw new ApiErrorException(this.getClass().getName());
        }
    }

    protected void doPerform() {
        List<AppEnvironment> appEnvironmentList = appEnvironmentRepository.findAllByApplicationIdAndStatus(application.get().getObjectId(), Status.V);
        for (AppEnvironment appEnvironment : appEnvironmentList) {
            List<AppVpc> appVpcList = new ArrayList<>();
            if (appEnvironment.getAppVpcList() != null) {
                for (int appVpcIndex = 0; appVpcIndex < appEnvironment.getAppVpcList().size(); appVpcIndex++) {
                    AppVpc appVpc = appEnvironment.getAppVpcList().get(appVpcIndex);
                    if(appVpc.getDeploymentName() == null) {
                        appVpc.setDeploymentName(application.get().getUniqueName() + "-" + appEnvironment.getEnvironment().getShortName().toLowerCase());
                    }
                    if (input.isIngressEnabled()) {
                        if(appVpc.getIngressConfig() == null) {
                            appVpc.setIngressConfig(new IngressConfig());
                        }
                        appVpc.getIngressConfig().setDefaultIngressUrl(this.generateUrl(appVpc));

                        if (appVpc.isCanaryDeploymentEnabled()) {
                            appVpc.getIngressConfig().setCanaryIngressUrl("canary-" + this.generateUrl(appVpc));
                        }

                        String customIngress = getCustomIngress(appEnvironment, appVpc);
                        if (!customIngress.equals("")) {
                            appVpc.getIngressConfig().setCustomIngressUrl(customIngress);
                        }
                    }
                    appVpc.setUpdatedBy(requester.getUsername());
                    appVpc.setUpdateDate(String.valueOf(LocalDateTime.now()));
                    appVpc.setLastUpdateActionId(actionId);
                    appVpcList.add(appVpc);
                    appVpc = appVpcRepository.save(appVpc);
                    appEnvironment.getAppVpcList().set(appVpcIndex, appVpc);
                }
            }
            appEnvironment.setUpdatedBy(requester.getUsername());
            appEnvironment.setUpdateDate(String.valueOf(LocalDateTime.now()));
            appEnvironment.setLastUpdateActionId(actionId);
            appEnvironmentRepository.save(appEnvironment);
        }
        if (input.getTlsSecretName() != null) {
            application.get().setTlsSecretName(input.getTlsSecretName());
        }
        application.get().setIngressEnabled(input.isIngressEnabled());
        application.get().setBlueGreenDeploymentEnabled(input.isBlueGreenDeploymentEnabled());
        application.get().setIstioEnabled(input.isIstioEnabled());
        application.get().setIstioIngressGatewayEnabled(input.isIstioIngressGatewayEnabled());
        application.get().setHealthCheckUrl(input.getHealthCheckUrl());
        application.get().setPort(input.getAppPort());
        application.get().setMetricsPort(input.getAppMetricsPort());
        if (application.get().getApplicationState() == ApplicationState.PENDING_APP_VPC_AND_CONFIG_DETAILS_ADDED) {
            application.get().setApplicationState(ApplicationState.PENDING_ADVANCE_CONFIGURATION_ADDED);
        }
        output.generateSuccessResponse(applicationRepository.save(application.get()), String.format("Advance configuration successfully added for application '%s'", application.get().getName()));
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
        Generating URL For App Vpc
     */
    private String generateUrl(AppVpc appVpc) {
        String defaultDomain = "cloudslip.io";
        return appVpc.getDeploymentName() + "." + defaultDomain;
    }

    /*
        Get Custom Ingress For Particular App VPc in an app environment
     */
    private String getCustomIngress(AppEnvironment appEnvironment, AppVpc appVpc) {
        if (input.getCustomIngressConfigList() != null) {
            for (CustomIngressConfigDTO dto : input.getCustomIngressConfigList()) {
                if (dto.getEnvironmentId().toString().equals(appEnvironment.getEnvironment().getId()) && dto.getVpcId().toString().equals(appVpc.getVpc().getId())) {
                    return dto.getCustomIngress() == null ? "" : dto.getCustomIngress();
                }
            }
        }
        return "";
    }
}
