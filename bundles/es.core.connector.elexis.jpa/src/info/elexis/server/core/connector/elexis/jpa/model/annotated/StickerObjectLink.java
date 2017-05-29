package info.elexis.server.core.connector.elexis.jpa.model.annotated;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Embeddable
public class StickerObjectLink {
	
	@Column(length = 80)
	private String obj;
	
	@ManyToOne
	@JoinColumn(name = "etikette", nullable = false, insertable = false)
	private Sticker sticker;
	
	public String getObj(){
		return obj;
	}
	
	public void setObj(String obj){
		this.obj = obj;
	}
	
	public Sticker getSticker(){
		return sticker;
	}
	
	public void setSticker(Sticker sticker){
		this.sticker = sticker;
	}
	
}
