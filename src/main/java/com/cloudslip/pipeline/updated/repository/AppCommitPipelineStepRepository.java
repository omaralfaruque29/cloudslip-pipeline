package com.cloudslip.pipeline.updated.repository;

import com.cloudslip.pipeline.updated.enums.Status;
import com.cloudslip.pipeline.updated.model.AppCommitPipelineStep;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AppCommitPipelineStepRepository extends MongoRepository<AppCommitPipelineStep, ObjectId> {

    List<AppCommitPipelineStep> findAllByAppCommitIdAndStatus(ObjectId appCommitId, Status status);

    Optional<AppCommitPipelineStep> findByIdAndStatus(ObjectId id, Status status);

    Optional<AppCommitPipelineStep> findByAppCommitIdAndAppPipelineStep_IdAndStatus(ObjectId appCommitId, ObjectId appPipelineStepId, Status status);

    List<AppCommitPipelineStep> findAllByAppPipelineStepIdAndStatus(ObjectId appPipelineStepId, Status status);
}
