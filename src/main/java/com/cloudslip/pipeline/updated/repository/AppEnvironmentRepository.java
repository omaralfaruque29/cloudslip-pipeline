package com.cloudslip.pipeline.updated.repository;

import com.cloudslip.pipeline.updated.enums.Status;
import com.cloudslip.pipeline.updated.model.AppEnvironment;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AppEnvironmentRepository extends MongoRepository<AppEnvironment, ObjectId> {

    List<AppEnvironment> findAllByStatus(Status status);
    Page<AppEnvironment> findAllByStatus(Pageable pageable, Status status);

    @Query(value = "{'appVpcList.vpc._id' : ?0, 'status._' : ?1}")
    List<AppEnvironment> findAllByAppVpcListVpcIdAndStatus(ObjectId vpcId, Status status);

    @Query(value = "{'appVpcList.vpc._id' : ?0, 'status._' : ?1, 'applicationId' : { '$ne' : ?2 }}")
    List<AppEnvironment> findAllByAppVpcListVpcIdAndStatusAndApplicationIdNotIn(ObjectId vpcId, Status status, ObjectId applicationId);

    List<AppEnvironment> findAllByApplicationIdAndStatus(ObjectId appId, Status status);

    List<AppEnvironment> findAllByApplicationIdAndIdNotInAndStatus(ObjectId appId, List<ObjectId> selectedAppEnvId, Status status);

    List<AppEnvironment> findAllByApplicationIdAndStatusOrderByEnvironment_OrderNo(ObjectId applicationId, Status status);
    Page<AppEnvironment> findAllByApplicationIdAndStatusOrderByEnvironment_OrderNo(Pageable pageable, ObjectId applicationId, Status status);

    Optional<AppEnvironment> findByIdAndStatus(ObjectId id, Status status);

    Optional<AppEnvironment> findByEnvironmentIdAndApplicationIdAndStatus(ObjectId environmentOptionId, ObjectId appId, Status status);
    List<AppEnvironment> findAllByEnvironmentIdNotInAndApplicationIdAndStatus(List<ObjectId> environmentOptionId, ObjectId appId, Status status);

    Optional<AppEnvironment> findFirstByApplicationIdAndStatusAndEnvironment_OrderNoGreaterThanAndAppPipelineStepListNotNullOrderByEnvironment_OrderNo(ObjectId appId, Status status, int orderNo);
}
