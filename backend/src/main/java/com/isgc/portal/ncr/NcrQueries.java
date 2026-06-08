package com.isgc.portal.ncr;

import com.isgc.portal.ncr.dto.NcrResponse;
import com.isgc.portal.security.CurrentUser;
import java.util.List;

public interface NcrQueries {
  List<NcrResponse> list(CurrentUser user);
}
