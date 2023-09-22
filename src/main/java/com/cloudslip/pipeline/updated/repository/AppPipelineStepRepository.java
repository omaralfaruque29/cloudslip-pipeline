package com.cloudslip.pipeline.updated.repository;

import com.cloudslip.pipeline.updated.enums.PipelineStepType;
import com.cloudslip.pipeline.updated.enums.Status;
import com.cloudslip.pipeline.updated.enums.TriggerMode;
import com.cloudslip.pipeline.updated.model.AppPipelineStep;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AppPipelineStepRepository extends MongoRepository<AppPipelineStep, ObjectId> {

    Optional<AppPipelineStep> findByIdAndStatus(ObjectId objectId, Status status);

    List<AppPipelineStep> findAllByAppEnvironmentIdAndStatus(ObjectId appEnvId, Status status);

    Page<AppPipelineStep> findAllByAppEnvironmentIdAndStatus(Pageable pageable, ObjectId appEnvId, Status status);

    List<AppPipelineStep> findAllByStatus(Status status);

    Page<AppPipelineStep> findAllByStatus(Pageable pageable, Status status);

    Optional<AppPipelineStep> findByAppVpcIdAndAppEnvironmentIdAndStatus(ObjectId appVpcId, ObjectId appEnvId, Status status);

    List<AppPipelineStep> findByAppVpcIdNotInAndAppEnvironmentIdAndStatus(List<ObjectId> appVpcIdList, ObjectId appEnvId, Status status);

    List<AppPipelineStep> findAllByIdInAndStatus(List<ObjectId> appVpcId, Status status);

    @Query(value = "{ 'successors' : {'$elemMatch' : {'appPipelineStep._id' : ?0, 'triggerMode._' : ?1}}, 'status._' : ?2}")
    Optional<AppPipelineStep> findBySuccessorsAppPipelineStepIdAndTriggerModeAndStatus(ObjectId successorAppPipelineStepId, TriggerMode triggerMode, Status status);

    @Query(value = "{ 'successors.appPipelineStep._id' : ?0, 'status._' : ?1 }")
    List<AppPipelineStep> findAllBySuccessorsAppPipelineStepIdAndStatus(ObjectId successorAppPipelineStepId, Status status);

    @Query(value = "{ 'successors' : {'$elemMatch' : {'appPipelineStep._id' : ?0}}, '_id' : { '$ne' : ?1 }, 'status._' : ?2}")
    List<AppPipelineStep> findAllBySuccessorsAppPipelineStepIdAndIdNotInAndStatus(ObjectId successorAppPipelineStepId, ObjectId appPipelineStepId, Status status);

    Optional<AppPipelineStep> findByAppVpcIdAndStepTypeAndStatus(ObjectId appVpcId, PipelineStepType stepType, Status status);
}
