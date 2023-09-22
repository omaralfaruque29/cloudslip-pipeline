package com.cloudslip.pipeline.updated.repository;

import com.cloudslip.pipeline.updated.enums.Status;
import com.cloudslip.pipeline.updated.enums.TriggerMode;
import com.cloudslip.pipeline.updated.model.AppEnvironmentChecklist;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Optional;

public interface AppEnvironmentChecklistRepository extends MongoRepository<AppEnvironmentChecklist, ObjectId> {

    Optional<AppEnvironmentChecklist> findByApplicationIdAndAppEnvironmentIdAndStatus(ObjectId applicationId, ObjectId appEnvId, Status status);
    Optional<AppEnvironmentChecklist> findByIdAndStatus(ObjectId id, Status status);

    @Query(value = "{ '._id' : ?0, 'checklist' : {'$elemMatch' : {'._id' : ?1}}, 'status._' : ?2}")
    Optional<AppEnvironmentChecklist> findByIdAndChecklistIdAndStatus(ObjectId appEnvChecklistId, ObjectId checkItemId, Status status);
}
