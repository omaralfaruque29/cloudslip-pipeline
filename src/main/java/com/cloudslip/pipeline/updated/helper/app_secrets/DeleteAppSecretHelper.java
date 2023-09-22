package com.cloudslip.pipeline.updated.helper.app_secrets;

import com.cloudslip.pipeline.updated.dto.BaseInput;
import com.cloudslip.pipeline.updated.dto.GetObjectInputDTO;
import com.cloudslip.pipeline.updated.dto.ResponseDTO;
import com.cloudslip.pipeline.updated.dto.kubeconfig.SecretConfig;
import com.cloudslip.pipeline.updated.enums.AgentCommand;
import com.cloudslip.pipeline.updated.enums.Authority;
import com.cloudslip.pipeline.updated.enums.Status;
import com.cloudslip.pipeline.updated.exception.model.ApiErrorException;
import com.cloudslip.pipeline.updated.helper.AbstractHelper;
import com.cloudslip.pipeline.updated.kafka.KafkaPublisher;
import com.cloudslip.pipeline.updated.kafka.dto.KafkaMessage;
import com.cloudslip.pipeline.updated.kafka.dto.KafkaMessageHeader;
import com.cloudslip.pipeline.updated.model.AppSecret;
import com.cloudslip.pipeline.updated.model.AppVpc;
import com.cloudslip.pipeline.updated.model.universal.Vpc;
import com.cloudslip.pipeline.updated.repository.AppSecretRepository;
import com.cloudslip.pipeline.updated.repository.AppVpcRepository;
import com.cloudslip.pipeline.updated.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class DeleteAppSecretHelper extends AbstractHelper {

    private final Logger log = LoggerFactory.getLogger(DeleteAppSecretHelper.class);
    @Autowired
    private AppSecretRepository appSecretsRepository;

    @Autowired
    private KafkaPublisher kafkaPublisher;

    @Autowired
    private AppVpcRepository appVpcRepository;
    private GetObjectInputDTO input;
    private ResponseDTO output = new ResponseDTO();
    private Optional<AppSecret> appSecrets;


    public void init(BaseInput input, Object... extraParams) {
        this.input = (GetObjectInputDTO) input;
        this.setOutput(output);
    }


    protected void checkPermission() {
        if (requester == null || !requester.hasAnyAuthority(Authority.ROLE_SUPER_ADMIN, Authority.ROLE_ADMIN,Authority.ROLE_DEV,Authority.ROLE_OPS)) {
            output.generateErrorResponse("Unauthorized user!");
            throw new ApiErrorException(output.getMessage(), this.getClass().getName());
        }
    }


    protected void checkValidity() {
        appSecrets = appSecretsRepository.findByIdAndStatus(input.getId(), Status.V);
        if(!appSecrets.isPresent()) {
            output.generateErrorResponse("App Secrets Not Found!");
            throw new ApiErrorException(this.getClass().getName());
        }
    }


    protected void doPerform() {
        appSecrets.get().setUpdatedBy(requester.getUsername());
        appSecrets.get().setUpdateDate(String.valueOf(LocalDateTime.now()));
        appSecrets.get().setLastUpdateActionId(actionId);
        try {
            generateAppSecretConfigAndPublishInKafka();
        } catch (Exception ex){
            output.generateErrorResponse("Error Occurs in Publishing KafkaMessage For Deleting Secret!");
            throw new ApiErrorException(this.getClass().getName());
        }
        appSecrets.get().setStatus(Status.D);
        appSecretsRepository.save(appSecrets.get());
    }

    private void generateAppSecretConfigAndPublishInKafka() {
        List<AppVpc> appVpcList = appVpcRepository.findAllByApplicationIdAndStatus(appSecrets.get().getApplicationId(), Status.V);
        List<Vpc> vpcList = new ArrayList<>();
        if(appVpcList.size() > 0) {
            for (AppVpc appVpc : appVpcList) {
                for (int i = 0; i < appSecrets.get().getEnvironmentList().size(); i++) {
                    if (appSecrets.get().getEnvironmentList().get(i).getAppEnvironmentId().equals(appVpc.getAppEnvironmentId())) {
                        if(vpcList.size() > 0) {
                            generateAppVpcList(vpcList, appVpc.getVpc());
                        } else {
                            vpcList.add(appVpc.getVpc());
                        }
                    }
                }
            }
            for (Vpc vpc : vpcList) {
                SecretConfig secretConfig = new SecretConfig();
                secretConfig.setSecretName(appSecrets.get().getUniqueName());
                secretConfig.setNamespace(vpc.getNamespace());
                publishKafkaMessageForDeletingSecret(vpc.getId(), vpc.getKubeCluster().getKafkaTopic(), secretConfig);
            }
        }
        else {
            output.generateErrorResponse("App Vpc not found!");
            throw new ApiErrorException(this.getClass().getName());
        }
    }

    private void publishKafkaMessageForDeletingSecret(String vpcId, String targetVpcKafkaTopic, SecretConfig secretConfig) {
        KafkaMessageHeader header = new KafkaMessageHeader(secretConfig.getNamespace(), AgentCommand.REMOVE_SECRET.toString());
        header.setCompanyId(requester.getCompanyId().toHexString());
        header.addToExtra("vpcId", vpcId);
        KafkaMessage<SecretConfig> message = new KafkaMessage(header, secretConfig);
        this.kafkaPublisher.publishMessage(targetVpcKafkaTopic, message);
    }

    protected void postPerformCheck() {
    }

    protected void doRollback() {

    }

    private List<Vpc> generateAppVpcList(List<Vpc> vpcList, Vpc vpc){
        for(int j=0 ; j < vpcList.size(); j++) {
            if (vpcList.get(j).getId().equals(vpc.getId())) {
                return vpcList;
            }
        }
        vpcList.add(vpc);
        return vpcList;
    }
}
