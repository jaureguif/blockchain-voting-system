package com.epam.asset.tracking.repository;

import java.util.Optional;

public interface BlockchainRepository<T, ID> {
  Optional<T> findOne(ID id);
  T save(T entity);
  boolean delete(T entity);
}
