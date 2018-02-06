package biodiv.userGroup;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import biodiv.common.Language;

/**
 * Newsletter generated by hbm2java
 */
@Entity
@Table(name = "newsletter", schema = "public")
public class Newsletter implements java.io.Serializable {

	private long id;
	private long version;
	private Language language;
	private UserGroup userGroup;
	private Date date;
	private String newsitem;
	private String title;
	private Boolean sticky;
	private Integer displayOrder;
	private long parentId;
	private Boolean showInFooter;
	//private Set userGroupNewsletters = new HashSet(0);

	public Newsletter() {
	}

	public Newsletter(long id, Language language, Date date, String newsitem, String title, long parentId) {
		this.id = id;
		this.language = language;
		this.date = date;
		this.newsitem = newsitem;
		this.title = title;
		this.parentId = parentId;
	}

	public Newsletter(long id, Language language, UserGroup userGroup, Date date, String newsitem, String title,
			Boolean sticky, Integer displayOrder, long parentId, Set userGroupNewsletters) {
		this.id = id;
		this.language = language;
		this.userGroup = userGroup;
		this.date = date;
		this.newsitem = newsitem;
		this.title = title;
		this.sticky = sticky;
		this.displayOrder = displayOrder;
		this.parentId = parentId;
		//this.userGroupNewsletters = userGroupNewsletters;
	}

	@Id
	@GenericGenerator(
	        name = "hibernate_generator",
	        strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
	        parameters = {
	                @Parameter(name = "sequence_name", value = "hibernate_sequence"),
	                @Parameter(name = "increment_size", value = "1"),
                    @Parameter(name = "optimizer", value = "hilo")
	        }
	)
	@GeneratedValue(generator = "hibernate_generator")
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
	@JoinColumn(name = "language_id", nullable = false)
	public Language getLanguage() {
		return this.language;
	}

	public void setLanguage(Language language) {
		this.language = language;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_group_id")
	public UserGroup getUserGroup() {
		return this.userGroup;
	}

	public void setUserGroup(UserGroup userGroup) {
		this.userGroup = userGroup;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "date", nullable = false, length = 29)
	public Date getDate() {
		return this.date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	@Column(name = "newsitem", nullable = false)
	public String getNewsitem() {
		return this.newsitem;
	}

	public void setNewsitem(String newsitem) {
		this.newsitem = newsitem;
	}

	@Column(name = "title", nullable = false)
	public String getTitle() {
		return this.title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	@Column(name = "sticky")
	public Boolean getSticky() {
		return this.sticky;
	}

	public void setSticky(Boolean sticky) {
		this.sticky = sticky;
	}

	@Column(name = "display_order")
	public Integer getDisplayOrder() {
		return this.displayOrder;
	}

	public void setDisplayOrder(Integer displayOrder) {
		this.displayOrder = displayOrder;
	}

	@Column(name = "parent_id", nullable = false)
	public long getParentId() {
		return this.parentId;
	}

	public void setParentId(long parentId) {
		this.parentId = parentId;
	}
	
	@Column(name = "show_in_footer",nullable = false)
	public Boolean getShowInFooter() {
		return showInFooter;
	}

	public void setShowInFooter(Boolean showInFooter) {
		this.showInFooter = showInFooter;
	}

//	@OneToMany(fetch = FetchType.LAZY, mappedBy = "newsletter")
//	public Set getUserGroupNewsletters() {
//		return this.userGroupNewsletters;
//	}
//
//	public void setUserGroupNewsletters(Set userGroupNewsletters) {
//		this.userGroupNewsletters = userGroupNewsletters;
//	}

}
