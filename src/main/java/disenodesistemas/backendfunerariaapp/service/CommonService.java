package disenodesistemas.backendfunerariaapp.service;

import java.util.List;

public interface CommonService<T, S, ID> {
  T create(S dto);

  T update(ID id, S dto);

  void delete(ID id);

  List<T> findAll();
}
