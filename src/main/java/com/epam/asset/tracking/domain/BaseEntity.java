package com.epam.asset.tracking.domain;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.data.annotation.Id;

/**
 * Base persistable user.
 *
 * @author daniel_pedraza@epam.com
 */
public abstract class BaseEntity<ID> {

  @Id
  @JsonIgnore
  private ID id;

  public ID getId() {
    return id;
  }

  public void setId(ID id) {
    this.id = id;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id);
  }

  protected boolean isEntityEqualTo(BaseEntity<?> other) {
    return Objects.equals(id, other.id);
  }
}
