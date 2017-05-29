package info.elexis.server.core.connector.elexis.jpa.model.annotated;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Embeddable
public class StickerClassLink {
	
	@Column(length = 80)
	private String objclass;
	
	@ManyToOne
	@JoinColumn(name = "sticker", nullable = false, insertable = false)
	private Sticker sticker;
	
	public String getObjclass(){
		return objclass;
	}
	
	public void setObjclass(String objclass){
		this.objclass = objclass;
	}
	
	public Sticker getSticker(){
		return sticker;
	}
	
	public void setSticker(Sticker sticker){
		this.sticker = sticker;
	}
}
