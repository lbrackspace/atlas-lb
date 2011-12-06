package org.openstack.atlas.service.domain.entity;

import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(Account.class)
public abstract class Account_ {

	public static volatile SingularAttribute<Account, Integer> id;
	public static volatile SingularAttribute<Account, String> sha1SumForIpv6;

}

