package com.epam.asset.tracking.repository;

import java.util.Optional;

import javax.annotation.Nonnull;

public interface BlockchainRepository<T, ID> {
  @Nonnull Optional<T> findOne(@Nonnull ID id);
  @Nonnull T save(@Nonnull T entity);
  boolean delete(@Nonnull T entity);
}
