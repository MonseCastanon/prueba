package com.proyecto.servicios.repositorys.mongo;

import com.proyecto.servicios.entity.mongo.CatProductoDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CatProductoMongoRepository extends MongoRepository<CatProductoDocument, String> {

    Optional<CatProductoDocument> findByIdProducto(Integer idProducto);

    List<CatProductoDocument> findByActivoTrue();
}
