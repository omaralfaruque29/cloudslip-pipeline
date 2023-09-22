package com.cloudslip.pipeline.updated.repository;

import com.cloudslip.pipeline.updated.model.CheckItem;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CheckItemRepository extends MongoRepository<CheckItem, ObjectId> {
}
