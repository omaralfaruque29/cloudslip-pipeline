package com.cloudslip.pipeline.updated.repository;

import com.cloudslip.pipeline.updated.enums.Status;
import com.cloudslip.pipeline.updated.model.AppVpc;
import com.cloudslip.pipeline.updated.model.universal.Vpc;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AppVpcRepository extends MongoRepository<AppVpc, ObjectId> {

    Optional<AppVpc> findByVpcIdAndAppEnvironmentIdAndStatus(ObjectId vpcId, ObjectId appEnvId, Status status);

    List<AppVpc> findAllByVpcIdAndStatus(ObjectId vpcId, Status status);

    List<AppVpc> findByVpcNotInAndAppEnvironmentIdAndStatus(List<Vpc> vpcList, ObjectId appEnvId, Status status);

    List<AppVpc> findByVpcIdNotInAndAppEnvironmentIdAndStatus(List<ObjectId> vpcList, ObjectId appEnvId, Status status);

    List<AppVpc> findAllByAppEnvironmentIdAndStatus(ObjectId appEnvId, Status status);

    Optional<AppVpc> findByApplicationIdAndStatus(ObjectId applicationId, Status status);

    List<AppVpc> findAllByApplicationIdAndStatus(ObjectId applicationId, Status status);

    Optional<AppVpc> findByApplicationIdAndVpc_KubeCluster_KafkaTopicAndStatus(ObjectId applicationId, String kafkaTopic, Status status);

    Optional<AppVpc> findByIdAndStatus(ObjectId id, Status status);
}
