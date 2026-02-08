package com.imt.api_invocations.utils;

import com.imt.api_invocations.enums.Rank;

public interface DataServiceInterface {
  public boolean hasAvailableData(Rank rank);
}
