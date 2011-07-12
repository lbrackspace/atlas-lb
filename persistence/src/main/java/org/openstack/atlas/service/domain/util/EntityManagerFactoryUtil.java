package org.openstack.atlas.service.domain.util;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class EntityManagerFactoryUtil {
    private static EntityManagerFactory emf;
    private static EntityManager em;
    private static final String PERSISTENCE_UNIT_NAME = "loadbalancing";

    private static void initEntityManager() {
        emf = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
        em = emf.createEntityManager();
    }

    public static void closeEntityManager() {
        if (em != null) {
            em.close();
        }
    }

    public static EntityManager getEntityManager() {
        if (em == null) {
            initEntityManager();
            return em;
        } else {
            return em;
        }
    }
}
