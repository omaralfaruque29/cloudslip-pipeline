package com.cloudslip.pipeline.updated.core;

import com.cloudslip.pipeline.updated.enums.Status;
import com.mongodb.MongoClient;
import org.bson.Document;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.dao.support.PersistenceExceptionTranslator;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.*;
import org.springframework.data.mongodb.core.index.MongoMappingEventPublisher;
import org.springframework.data.mongodb.core.index.MongoPersistentEntityIndexCreator;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.data.mongodb.core.mapping.MongoPersistentEntity;
import org.springframework.data.mongodb.core.mapping.MongoPersistentProperty;
import org.springframework.data.projection.SpelAwareProxyProjectionFactory;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.LinkedHashMap;

public class CustomMongoTemplate extends MongoTemplate {

    private final MongoConverter mongoConverter;
    private final QueryMapper queryMapper;
    private final MappingContext<? extends MongoPersistentEntity<?>, MongoPersistentProperty> mappingContext;
    private final SpelAwareProxyProjectionFactory projectionFactory;
    @Nullable
    private ApplicationEventPublisher eventPublisher;
    @Nullable
    private MongoPersistentEntityIndexCreator indexCreator;
    private final PersistenceExceptionTranslator exceptionTranslator;

    public CustomMongoTemplate(MongoClient mongoClient, String databaseName) {
        super(mongoClient, databaseName);
        mongoConverter = null;
        mappingContext = null;
        queryMapper = null;
        projectionFactory = null;
        exceptionTranslator = null;
    }

    public CustomMongoTemplate(MongoDbFactory mongoDbFactory) {
        super(mongoDbFactory);
        mongoConverter = null;
        mappingContext = null;
        queryMapper = null;
        projectionFactory = null;
        exceptionTranslator = null;
    }

    public CustomMongoTemplate(MongoDbFactory mongoDbFactory, @Nullable MongoConverter mongoConverter) {
        super(mongoDbFactory, mongoConverter);
        this.exceptionTranslator = mongoDbFactory.getExceptionTranslator();
        this.mongoConverter = mongoConverter == null ? getDefaultMongoConverter(mongoDbFactory) : mongoConverter;
        this.queryMapper = new QueryMapper(this.mongoConverter);
        this.mappingContext = this.mongoConverter.getMappingContext();
        if (this.mappingContext instanceof MongoMappingContext) {
            this.indexCreator = new MongoPersistentEntityIndexCreator((MongoMappingContext)this.mappingContext, this);
            this.eventPublisher = new MongoMappingEventPublisher(this.indexCreator);
            if (this.mappingContext instanceof ApplicationEventPublisherAware) {
                ((ApplicationEventPublisherAware)this.mappingContext).setApplicationEventPublisher(this.eventPublisher);
            }
        }
        this.projectionFactory = new SpelAwareProxyProjectionFactory();
    }

    private static MongoConverter getDefaultMongoConverter(MongoDbFactory factory) {
        DbRefResolver dbRefResolver = new DefaultDbRefResolver(factory);
        MongoCustomConversions conversions = new MongoCustomConversions(Collections.emptyList());
        MongoMappingContext mappingContext = new MongoMappingContext();
        mappingContext.setSimpleTypeHolder(conversions.getSimpleTypeHolder());
        mappingContext.afterPropertiesSet();
        MappingMongoConverter converter = new MappingMongoConverter(dbRefResolver, mappingContext);
        converter.setCustomConversions(conversions);
        converter.afterPropertiesSet();
        return converter;
    }

    @Nullable
    public <T> T findById(Object id, Class<T> entityClass, String collectionName) {
        Assert.notNull(id, "Id must not be null!");
        Assert.notNull(entityClass, "EntityClass must not be null!");
        Assert.notNull(collectionName, "CollectionName must not be null!");
        MongoPersistentEntity<?> persistentEntity = (MongoPersistentEntity)this.mappingContext.getPersistentEntity(entityClass);
        String idKey = "_id";
        if (persistentEntity != null && persistentEntity.getIdProperty() != null) {
            idKey = ((MongoPersistentProperty)persistentEntity.getIdProperty()).getName();
        }
        LinkedHashMap<String, Object> queryDocumentAsMap = new LinkedHashMap<String, Object>();
        queryDocumentAsMap.put(idKey, id);
        queryDocumentAsMap.put("status", Status.V);

        return this.doFindOne(collectionName, new Document(queryDocumentAsMap), new Document(), entityClass);
    }
}
