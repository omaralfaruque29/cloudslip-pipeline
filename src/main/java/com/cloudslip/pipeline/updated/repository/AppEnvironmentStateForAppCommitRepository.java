package com.cloudslip.pipeline.updated.repository;

import com.cloudslip.pipeline.updated.enums.Status;
import com.cloudslip.pipeline.updated.model.AppEnvironmentStateForAppCommit;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AppEnvironmentStateForAppCommitRepository extends MongoRepository<AppEnvironmentStateForAppCommit, ObjectId> {

    List<AppEnvironmentStateForAppCommit> findAllByAppEnvironmentIdAndStatus(ObjectId appEnvId, Status status);
}
