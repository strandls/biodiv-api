package biodiv.speciesPermission;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

import com.fasterxml.jackson.annotation.JsonValue;

import biodiv.taxon.datamodel.dao.Taxon;
import biodiv.user.User;

/**
 * SpeciesPermission generated by hbm2java
 */
@Entity
@Table(name = "species_permission", schema = "public", uniqueConstraints = @UniqueConstraint(columnNames = {
		"permission_type", "taxon_concept_id", "author_id" }))
public class SpeciesPermission implements java.io.Serializable {

	private long id;
	private long version;
	private User user;
	private Taxon taxon;
	private Date createdOn;
	private String permissionType;
	
	public enum PermissionType {
        ROLE_CURATOR("ROLE_CURATOR"),
        ROLE_CONTRIBUTOR("ROLE_CONTRIBUTOR"),
        ROLE_TAXON_CURATOR("ROLE_TAXON_CURATOR"),
        ROLE_TAXON_EDITOR("ROLE_TAXON_EDITOR");

        private String value;

        PermissionType(String value) {
            this.value = value;
        }

        @JsonValue
        String value() {
            return this.value;
        }

//        static def toList() {
//            return [ ROLE_CURATOR, ROLE_CONTRIBUTOR ]
//        }

        public String toString() {
            return this.value();
        }

    }

	public SpeciesPermission() {
	}

	public SpeciesPermission(long id, User user, Taxon taxon, Date createdOn,
			String permissionType) {
		this.id = id;
		this.user = user;
		this.taxon= taxon;
		this.createdOn = createdOn;
		this.permissionType = permissionType;
	}

	@Id

	@Column(name = "id", unique = true, nullable = false)
	public long getId() {
		return this.id;
	}

	public void setId(long id) {
		this.id = id;
	}

	@Version
	@Column(name = "version", nullable = false)
	public long getVersion() {
		return this.version;
	}

	public void setVersion(long version) {
		this.version = version;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "author_id", nullable = false)
	public User getUser() {
		return this.user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "taxon_concept_id", nullable = false)
	public Taxon getTaxon() {
		return this.taxon;
	}

	public void setTaxon(Taxon taxon) {
		this.taxon= taxon;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "created_on", nullable = false, length = 29)
	public Date getCreatedOn() {
		return this.createdOn;
	}

	public void setCreatedOn(Date createdOn) {
		this.createdOn = createdOn;
	}

	@Column(name = "permission_type", nullable = false)
	public String getPermissionType() {
		return this.permissionType;
	}

	public void setPermissionType(String permissionType) {
		this.permissionType = permissionType;
	}

}

