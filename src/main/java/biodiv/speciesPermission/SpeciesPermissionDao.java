package biodiv.speciesPermission;

import java.util.List;

import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import biodiv.common.AbstractDao;
import biodiv.common.DaoInterface;
import biodiv.taxon.datamodel.dao.Taxon;
import biodiv.user.User;
import biodiv.userGroup.UserGroup;

public class SpeciesPermissionDao extends AbstractDao<SpeciesPermission, Long> implements DaoInterface<SpeciesPermission, Long> {

	private final Logger log = LoggerFactory.getLogger(getClass());

	SpeciesPermissionDao() {
		log.trace("UserGroupDao constructor");
	}

	@Override
	public SpeciesPermission findById(Long id) {
		SpeciesPermission entity = (SpeciesPermission) getCurrentSession().get(SpeciesPermission.class, id);

		return entity;
	}

	public List<SpeciesPermission> getAllSpeciesPermission(User currentUser, List<String> permissions,List<Long> parentTaxonIds) {
		
		String hql = "from SpeciesPermission sp where sp.user =:user and sp.permissionType in (:permissions) "
				+ "and sp.taxon.id in (:parentTaxonIds)";
		System.out.println("************************************ "+hql);
		Query query = getCurrentSession().createQuery(hql);
		query.setParameter("user", currentUser);
		query.setParameter("permissions", permissions);
		query.setParameter("parentTaxonIds", parentTaxonIds);
		System.out.println("************************************ "+query);
		List<SpeciesPermission> listResult = query.getResultList();
		return listResult;
	}
}