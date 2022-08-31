package org.mbari.mxm.db.provider;

import java.util.List;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindList;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

// @RegisterRowMapper(AssetMapper.class)
// @RegisterRowMapper(AssetClassMapper.class)
@RegisterBeanMapper(Provider.class)
@RegisterRowMapper(ProviderMapper.class)
public interface ProviderDao {

  @SqlQuery("select * from providers")
  List<Provider> list();

  @SqlQuery("select * from providers where provider_id = :providerId")
  Provider get(@Bind("providerId") String providerId);

  @SqlQuery("""
      select * from providers
      where provider_id in (<providerIds>)
      """)
  List<Provider> getProviders(@BindList("providerIds") List<String> providerIds);

  @SqlUpdate(
      """
      delete from providers
      where provider_id = :providerId
      returning *
      """)
  @GetGeneratedKeys
  Provider deleteById(@Bind("providerId") String providerId);
}
