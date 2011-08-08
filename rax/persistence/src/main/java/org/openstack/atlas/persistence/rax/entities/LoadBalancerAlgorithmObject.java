/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openstack.atlas.persistence.rax.entities;

import org.openstack.atlas.service.domain.entities.LoadBalancerAlgorithm;

import javax.persistence.*;
import java.io.Serializable;

@javax.persistence.Entity
@Table(name = "lb_algorithm")
public class LoadBalancerAlgorithmObject extends org.openstack.atlas.service.domain.entities.LoadBalancerAlgorithmObject implements Serializable {

}
