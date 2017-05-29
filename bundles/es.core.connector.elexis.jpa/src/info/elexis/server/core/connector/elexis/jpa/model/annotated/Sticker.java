package info.elexis.server.core.connector.elexis.jpa.model.annotated;

import java.util.List;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.Table;

@Entity
@Table(name = "ETIKETTEN")
public class Sticker extends AbstractDBObjectIdDeleted {
	
	@Column(length = 25, name = "Image")
	private String image;
	
	@Column(length = 25)
	private String importance;
	
	@Column(length = 40, name = "Name")
	private String name;
	
	@Column(columnDefinition = "CHAR(6)")
	private String foreground;
	
	@Column(columnDefinition = "CHAR(6)")
	private String background;
	
	@Column(length = 255)
	private String classes;
	
	@ElementCollection
	@CollectionTable(name = "ETIKETTEN_OBJCLASS_LINK", joinColumns = @JoinColumn(name = "sticker"))
	private List<StickerClassLink> stickerClassLinks;
	
	@ElementCollection
	@CollectionTable(name = "ETIKETTEN_OBJECT_LINK", joinColumns = @JoinColumn(name = "etikette"))
	private List<StickerObjectLink> stickerObjectLinks;
	
	public String getImage(){
		return image;
	}
	
	public void setImage(String image){
		this.image = image;
	}
	
	public String getImportance(){
		return importance;
	}
	
	public void setImportance(String importance){
		this.importance = importance;
	}
	
	public String getName(){
		return name;
	}
	
	public void setName(String name){
		this.name = name;
	}
	
	public String getForeground(){
		return foreground;
	}
	
	public void setForeground(String foreground){
		this.foreground = foreground;
	}
	
	public String getBackground(){
		return background;
	}
	
	public void setBackground(String background){
		this.background = background;
	}
	
	public String getClasses(){
		return classes;
	}
	
	public void setClasses(String classes){
		this.classes = classes;
	}
	
	public List<StickerClassLink> getStickerClassLinks(){
		return stickerClassLinks;
	}
	
	public List<StickerObjectLink> getStickerObjectLinks(){
		return stickerObjectLinks;
	}
	
	public void setStickerClassLinks(List<StickerClassLink> stickerClassLinks){
		this.stickerClassLinks = stickerClassLinks;
	}
	
	public void setStickerObjectLinks(List<StickerObjectLink> stickerObjectLinks){
		this.stickerObjectLinks = stickerObjectLinks;
	}
}
