package sprout.clipcon.server.model;

import java.util.HashMap;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

public class History {
	@Getter
	@Setter
	private String groupPK;
	private Map<String, Contents> contentsMap;

	// A unique key value that distinguishes each data in the group
	private static int contentsPKValue = 0;

	public History(String groupPK) {
		this.groupPK = groupPK; // XXX[delf]: 사실 필요한지 잘 모르겠음
		contentsMap = new HashMap<String, Contents>();
	}

	/** Add to history when new data is uploaded */
	public void addContents(Contents contents) {
		contents.setContentsPKName(Integer.toString(++contentsPKValue));
		contentsMap.put(contents.getContentsPKName(), contents);
		
		System.out.println("\n[SERVER] add the new contents. pk is " + contents.getPrimaryKey());
	}

	/** Return contents that match the primary key value that distinguishes the data */
	public Contents getContentsByPK(String contentsPKName) {
		return contentsMap.get(contentsPKName);
	}
}
